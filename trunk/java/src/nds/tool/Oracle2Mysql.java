package nds.tool;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;

import nds.log.Logger;
import nds.log.LoggerManager;
import nds.query.UpdateException;
import nds.schema.Column;
import nds.schema.Table;
import nds.schema.TableManager;
import nds.util.Configurations;

/*在oracle 向 mysql 同步

1. 初始化：
   设定需要同步的表，考虑正确性，使用java 运行
   将mysql 对应的表删除，按照 Oracle 的表结构建立，并生成相应的全部数据
   添加trigger on 各个表，在新增修改删除时建立相应的mysql 语句，放在某表队列中

   java nds.tool.Oracle2Mysql -p trans.properties

   trans.properties 参数
   table.list=<table1>,<table2>
   err.log= /path/to/err.log
   # if false , and table exists table will be truncated firstly, else will be dropped and recreate
   table.delete= false
   oracle.uri=jdbc:oracle:thin:nds4/abc123@localhost:1521:test
   oracle.sqlplus = /path/to/sqlplus
   mysql.uri=jdbc:mysql:thin:nds4/abc123@localhost:1521:test
   trigger.file=/path/to/trigger.sql
*/
public class Oracle2Mysql {
    public final static String DEFAULT_PROPERTY_FILE="/trans.properties";
    private transient Logger logger= LoggerManager.getInstance().getLogger(Oracle2Mysql.class.getName());
    Configurations conf;
    TableManager tm;
    Connection conOracle, conMysql;
    PrintStream logSQL;
    public Oracle2Mysql(){}
    public Oracle2Mysql(Configurations conf) {
        this.conf= conf;
    }
    /**
     * do actions
     */
    public void start() throws Exception{
        ArrayList al= getTableList();
        logSQL= createSQLOutput();
        try{
            conMysql= connectMysql();
            conOracle= connectOracle();
            loadTableSchema();

            Table tb;
            for( int i=0;i< al.size();i++){
                tb=tm.getTable((String) al.get(i));
                if(tb !=null)handleTable( tb);
                else logger.error("Table named "+ (String) al.get(i) + " could not be found");
            }
        }finally{
            logSQL.close();
        }
    }
    private PrintStream createSQLOutput()throws Exception{
        FileOutputStream fos=new FileOutputStream(conf.getProperty("trigger.file"));
        PrintStream ps=new PrintStream(fos,true);
        return ps;
    }
    private void loadTableSchema() throws Exception{
        tm=nds.schema.TableManager.getInstance();
        // yfzhu changed at 2003-09-22 to load table path from nds.properties
        Properties props=conf.getConfigurations("schema").getProperties();
        tm.init(props);
    }
    private Connection connectMysql() throws Exception{
        Class.forName("com.mysql.jdbc.Driver").newInstance();
        Connection conn = DriverManager.getConnection(conf.getProperty("mysql.uri" ));
        return conn;
    }
    private Connection connectOracle() throws Exception{
        Class.forName("oracle.jdbc.driver.OracleDriver").newInstance();
        Connection conn = DriverManager.getConnection(conf.getProperty("oracle.uri" ));
        return conn;
    }

    /**
     * @return elements are String for table name
     * @throws Error if no table to migrate
     */
    private ArrayList getTableList(){
        String s= conf.getProperty("table.list" );
        StringTokenizer st=new StringTokenizer(s,",");
        ArrayList al=new ArrayList();
        while(st.hasMoreTokens() ){
            al.add( st.nextToken());
        }
        if ( al.size() ==0) throw new Error("Not found any table to migrate");
        return al;
    }
    /**
     * do one table
     */
    public void handleTable(Table tb) throws Exception {
        logger.info("Handle table "+ tb.getName().toLowerCase());
        if( destTableExists(tb)){
            if ("true".equalsIgnoreCase(conf.getProperty("table.overwrite", "false"))){
                // drop or truncate
                if( "false".equalsIgnoreCase(conf.getProperty("table.delete", "false"))){
                    // truncate
                    truncateTable(tb);
                }else{
                    // drop and recreate with trigger in trigger.file
                    dropTable(tb);
                    createTable(tb);
                    createTrigger(tb);
                }
                 pipeData(tb);
            }else
                logger.debug("Table "+tb.getName().toLowerCase()+ " already exists, and not overwrite specified"  );
        }else{
            createTable(tb);
            createTrigger(tb);
            pipeData(tb);
        }

    }
    private void createTrigger(Table tb) throws Exception{
        logger.info("Create Trigger for table "+ tb.getName().toLowerCase());
        if( tb.getName().toLowerCase().equalsIgnoreCase("mysql_dump")) throw new Error("You can not add trigger on mysql_dump");
        /**
         * These string will be different for each table, sample:
         *  table="users"
         *  update_col="id='||  :new.id ||', name=''' || :new.name || '''"
         *  row_condition="id=' || :new.id";
         *  insert_col="(id, name) values (' || :new.id ||',''' || :new.name ||'''
         *  pk="id";
         */
        String table, update_col,row_condition,insert_col,pk;
        table= tb.getName().toLowerCase();
        pk= tb.getPrimaryKey().getName().toLowerCase();
        row_condition= pk+ "=' || :new."+pk;
        update_col="";
        insert_col="";
        String insert_head="(";
        ArrayList al= getDBColumns(tb);
        for(int i=0;i< al.size(); i++){
            Column col= (Column) al.get(i);
            if( i>0){
                update_col += "||',";
                insert_head +=",";
                insert_col +=",";
            }else{

            }
            update_col += col.getName().toLowerCase()+"=";
            insert_head += col.getName().toLowerCase();

            switch( col.getType() ){
                case Column.DATE:
                    update_col+= "''' || to_char(:new."+ col.getName().toLowerCase()+", 'YYYY-MM-DD H24:MI:SS') || ''''";
                    insert_col +="''' || to_char(:new."+ col.getName().toLowerCase()+", 'YYYY-MM-DD H24:MI:SS') || '''";
                    break;
                case Column.NUMBER :
                case Column.DATENUMBER :
                    update_col+= "' || :new."+ col.getName().toLowerCase()+" || ''";
                    insert_col +="' || :new."+ col.getName().toLowerCase()+" || '";
                    break;
                case Column.STRING :
                    update_col+= "''' || to_sqldata(:new."+ col.getName().toLowerCase()+") || ''''";
                    insert_col+="''' || to_sqldata(:new."+ col.getName().toLowerCase()+") || '''";
                    break;
                default :
                    throw new Exception("Unknow column type of "+ tb.getName().toLowerCase() + "."+ col.getName().toLowerCase() +":"+ col.getType() );
            }

        }
        insert_col=insert_head + ") values ("+ insert_col+")'";

        String triggerDef=
    "CREATE OR REPLACE TRIGGER tri_mig_"+table+" after insert or delete or update on "+table+" for each row "+LINE_SEPERATOR+
    "declare "+LINE_SEPERATOR+
    "      sqlText varchar2(2000);"+LINE_SEPERATOR+
    "      typ char(1);"+LINE_SEPERATOR+
    "   begin"+LINE_SEPERATOR+
    "    -- THIS IS GENERATED BY ORACLE2MYSQL FOR MIGRATING ORACLE DATA TO MYSQL"+LINE_SEPERATOR+
    "    -- ACT SHANGHAI CHINA, ALL RIGHT RESERVED"+LINE_SEPERATOR+
    "    if updating then"+LINE_SEPERATOR+
    "        sqlText :='update "+table+" set "+update_col+" ||' where "+row_condition+";"+LINE_SEPERATOR+
    "         typ :='u';"+LINE_SEPERATOR+
    "    else "+LINE_SEPERATOR+
    "       if deleting  then"+LINE_SEPERATOR+
    "        sqlText :='delete from "+table+" where "+row_condition+";"+LINE_SEPERATOR+
    "        typ :='d';"+LINE_SEPERATOR+
    "       else "+LINE_SEPERATOR+
    "          if inserting then"+LINE_SEPERATOR+
    "            sqlText :='insert into "+table+" "+insert_col+";"+LINE_SEPERATOR+
    "            typ:='a';"+LINE_SEPERATOR+
    "          end if;"+LINE_SEPERATOR+
    "       end if;"+LINE_SEPERATOR+
    "    end if;"+LINE_SEPERATOR+
    "    insert into mysql_dump(id,tablename, objectid, action, sqltext) values (seq_mysql_dump.nextval, '"+table+"', :new."+pk+", typ, sqlText);"+LINE_SEPERATOR+
    "   end tri_mig_"+table+";"+LINE_SEPERATOR+
    "/ ";
        logSQL.println(triggerDef);
    }


    /**
     * @return only columns of valid ( not virtual columns), and contains no (,
     */
    private ArrayList getDBColumns(Table tb){
        ArrayList al= tb.getAllColumns();
        for(int i=al.size()-1;i>-1;i--){
            Column col= (Column)al.get(i);
            if( col.isVirtual() || col.getName().toLowerCase().indexOf(".")>0 || col.getName().toLowerCase().indexOf(")")>0)
                al.remove(i);
        }
        return al;
    }
    private void createTable(Table tb) throws Exception{
        logger.info("Create table "+ tb.getName().toLowerCase());
        String sql="create table "+ tb.getName().toLowerCase()+"(";
        String addition="";
        ArrayList al= getDBColumns(tb);

        int pkId= tb.getPrimaryKey().getId();
        int akId= -1;
        if( tb.getAlternateKey() !=null) akId=  tb.getAlternateKey().getId();
        for(int i=0;i< al.size(); i++){
            Column col= (Column) al.get(i);
            sql += (i>0?", ":"")+ col.getName().toLowerCase()+" ";
            switch( col.getType() ){
                case Column.DATE:
                    sql+= "date";
                    break;
                case Column.NUMBER :
                case Column.DATENUMBER :
                    if( col.getScale() > 0)
                        sql+= "numeric("+ (col.getLength()-col.getScale() -1)+","+ col.getScale()+")";
                    else
                        sql+= "numeric("+ col.getLength()+")";
                    break;
                case Column.STRING :
                    sql+= "varchar("+ col.getLength() +")";
                    break;
                default :
                    throw new Exception("Unknow column type of "+ tb.getName().toLowerCase() + "."+ col.getName().toLowerCase() +":"+ col.getType() );
            }
             // not sensitive
             //sql+=  (col.isNullable() ? " ":" NOT NULL ");
             if( col.getId() == pkId){
                 //addition += ",PRIMARY KEY ("+ col.getName().toLowerCase()+")"; since nullable no pk now
                 addition += ",UNIQUE KEY PK_"+tb.getName().toLowerCase().toUpperCase() +  " ("+ col.getName().toLowerCase()+")";
             }else if ( col.getId() == akId){
                 addition += ",KEY IDX_AK"+tb.getName().toLowerCase().toUpperCase()+ " ("+ col.getName().toLowerCase()+")";
             }else if( col.getName().toLowerCase().toLowerCase().indexOf("id")> 0){
                 addition += ",KEY IDX_"+tb.getName().toLowerCase().toUpperCase()+"_"+ col.getName().toLowerCase().toUpperCase() + " ("+ col.getName().toLowerCase()+")";
             }
        }
        if ( addition.length() > 0) sql+= addition+ ")";
        doUpdate(sql, conMysql);
    }
    private void dropTable( Table tb) throws Exception{
        logger.info("Drop table "+ tb.getName().toLowerCase());
        doUpdate("drop table "+ tb.getName().toLowerCase()+ " cascade", conMysql);
    }
    private void truncateTable(Table tb) throws Exception{
        //logger.info("Truncate table "+ tb.getName().toLowerCase());
        doUpdate("truncate table "+ tb.getName().toLowerCase(), conMysql);
    }
    private void doUpdate(String sql, Connection con )  throws UpdateException{
        Vector v= new Vector();
        v.addElement(sql);
        doUpdate( v, con);
    }
    public int doUpdate(Vector vec, Connection con) throws UpdateException{
        Statement stmt=null;
        int count = 1; // start from 1 for user's convenience
        Iterator ite = vec.iterator() ;
        String sql = null;
        try{
            stmt = con.createStatement();
            while(ite.hasNext() ){
                sql = (String)ite.next() ;
                logger.debug(sql);
                stmt.executeUpdate(sql);
                count ++;
            }
        }catch(Exception e){
            logger.error(sql, e) ;
            throw new UpdateException("在执行第"+ count+"条记录时遇到异常："+e.getLocalizedMessage());
        }finally{
            try{stmt.close();}catch(Exception ea){}
        }
        return count;
    }
    /**
     * Check if table exists in mysql db
     */
    private boolean destTableExists( Table tb) throws Exception{
        Statement stmt=null;
        ResultSet rs=null;
        boolean b=false;
        try{
            stmt= conMysql.createStatement();
            rs=stmt.executeQuery("select count(*) from "+ tb.getName().toLowerCase());
            b=true;
        }catch(Exception e){
            //logger.debug("Error query table "+ tb.getName().toLowerCase()+":"+ e);
            b= false;
        }finally{
            if( rs!=null) try{ rs.close();}catch(Exception e){}
            if( stmt!=null) try{ stmt.close();}catch(Exception e){}
        }
        return b;
    }
    public void pipeData(Table tb) throws Exception{
        ResultSet rs=null;
        PreparedStatement pstmt=null;
        String sql="" , quest="";
        ArrayList al= tb.getAllColumns();
        int count= al.size();
        for (int i=0;i< al.size();i++){
            sql += (i>0?",":"") + ((Column)al.get(i)).getName().toLowerCase() ;
            quest += (i>0?",?":"?") ;
        }
        try{
        rs=conOracle.createStatement().executeQuery("select count(*) from "+ tb.getName().toLowerCase() );
        if( rs.next() ){
            logger.debug("Table "+ tb.getName().toLowerCase() + " has "+ rs.getInt(1)+ " rows");
        }
        rs.close();
        long timeStart= System.currentTimeMillis();
        pstmt= conMysql.prepareStatement("insert into "+ tb.getName().toLowerCase()+ " ( "+sql+ ") values ("+ quest+")");
        rs= conOracle.createStatement().executeQuery("select "+ sql + " from "+ tb.getName().toLowerCase());
        while( rs.next() ){
            for(int i=0;i<count ;i++)
                pstmt.setObject(i+1, rs.getObject(i+1)) ;
            pstmt.executeUpdate();
        }
        logger.debug("Time elapes "+ ((System.currentTimeMillis() - timeStart)/1000.0)+ " seconds for migrating "+ tb.getName().toLowerCase());
        }finally{
            if( pstmt !=null){try{ pstmt.close();}catch(Exception e){}}
            if( rs !=null){try{ rs.close();}catch(Exception e){}}
        }
    }
    private static void usage() {
        System.err.println("Usage:\n  java nds.util.Oracle2Mysql [-p properties]");
        System.err.println("\nOptions:");
        System.err.println("  -p : indicates the property file");
        System.exit(1);
    }

    public static void main(String[] argument) throws Exception{
        Oracle2Mysql m=null;
        try{
          String propfile = DEFAULT_PROPERTY_FILE;

          if(argument !=null)for (int i = 0; i < argument.length; i++) {
            if (argument[i].equals("-c")) {
              if (i + 1 < argument.length){
                  propfile = argument[i+1];
                  break;
              }
            }
          }
          //if(propfile==null) propfile=DEFAULT_PROPERTY_FILE;
          System.setProperty("applicationPropertyFile",propfile);
          InputStream is= new FileInputStream(propfile);
          Configurations confs = new Configurations(is);
          LoggerManager.getInstance().init(confs.getProperties(),true);
          m= new Oracle2Mysql(confs);

      }catch(Exception e){
          e.printStackTrace();
          usage();
          return;
       }
       m.start();
    }
    private static final String LINE_SEPERATOR=System.getProperty("line.separator");
}