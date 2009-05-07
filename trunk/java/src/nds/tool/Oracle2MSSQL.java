package nds.tool;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.CallableStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
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
import nds.util.Tools;

/*在oracle 向 mssql 同步

1. 初始化：
   设定需要同步的表，考虑正确性，使用java 运行
   将mssql 对应的表删除，按照 Oracle 的表结构建立，并生成相应的全部数据
   添加trigger on 各个表，在新增修改删除时建立相应的mssql 语句，放在某表队列中

   java nds.tool.Oracle2MSSQL -p trans.properties

   trans.properties 参数
   table.list=<table1>,<table2>
   err.log= /path/to/err.log
   # if false , and table exists table will be truncated firstly, else will be dropped and recreate
   table.delete= false
   oracle.uri=jdbc:oracle:thin:nds4/abc123@localhost:1521:test
   oracle.sqlplus = /path/to/sqlplus
   msql.uri=jdbc:mssql:thin:nds4/abc123@localhost:1521:test
   # maximum number of rows to inserts, -1 means unlimited
   table.maxinserts= -1
   trigger.file=/path/to/trigger.sql
   
   @changelog 
   yfzhu 2004-12-16 add support for incremental migration.
   Before migration, the maximum id in destination db will be read out, only
   id greater than that id will be inserted into the dest table. 
   Currently we do not consider record modified or deleted in the source table.  
   
   Add special call after migration
   procedure.finish=sp_upload        
*/
public class Oracle2MSSQL {
    public final static String DEFAULT_PROPERTY_FILE="/trans.properties";
    private transient Logger logger= LoggerManager.getInstance().getLogger(Oracle2MSSQL.class.getName());
    Configurations conf;
    TableManager tm;
    Connection conOracle, conMssql;
    PrintStream logSQL, logTableCreation;
    boolean noData=false;
    public Oracle2MSSQL(){}
    public Oracle2MSSQL(Configurations conf) {
        this.conf= conf;
    }
    /**
     * do actions
     */
    public void start() throws Exception{
        noData="true".equalsIgnoreCase(conf.getProperty("table.nodata", "false"));
        ArrayList al= getTableList();
        logSQL= createSQLOutput();
        logTableCreation= this.createTableCreationScriptStream() ;
        try{
            if(!noData){
                conMssql = connectMssql();
                conOracle = connectOracle();
            }
            loadTableSchema();

            Table tb;
            for( int i=0;i< al.size();i++){
                tb=tm.getTable((String) al.get(i));
                if(tb !=null)handleTable( tb);
                else logger.error("Table named "+ (String) al.get(i) + " could not be found");
            }
            finishMigration();
        }finally{
            logSQL.close();
            logTableCreation.close();
        }
    }
    /**
     * Finish this time data migration
     * will call procedure named in "procedure.finish", note this procedure should
     * has no parameter
     */
    private void finishMigration(){
    	CallableStatement cstmt=null;
    	String proc=conf.getProperty("procedure.finish");
    	if(proc==null || proc.trim().length()==0) return;
    	try{
    	cstmt=this.conMssql.prepareCall( proc );
    	cstmt.executeUpdate();
    	}catch(Exception e){
    		logger.error("Could not call stored procedure " + proc, e); 
    	}finally{
    		if(cstmt!=null)try{cstmt.close();}catch(Exception e){}
    	}
    	
    }
    private PrintStream createTableCreationScriptStream()throws Exception{
        FileOutputStream fos=new FileOutputStream(conf.getProperty("table.sqlfile"));
        PrintStream ps=new PrintStream(fos,true);
        return ps;
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
    private Connection connectMssql() throws Exception{
        Class.forName("com.microsoft.jdbc.sqlserver.SQLServerDriver").newInstance();
        Connection conn = DriverManager.getConnection(conf.getProperty("mssql.uri" ));
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
            }else{
                logger.debug("Table "+tb.getName().toLowerCase()+ " already exists, and not overwrite specified, only data piped in"  );
                pipeData(tb);
            }
        }else{
            createTable(tb);
            createTrigger(tb);
            pipeData(tb);
        }

    }
    private void createTrigger(Table tb) throws Exception{
        logger.info("Create Trigger for table "+ tb.getName().toLowerCase());
        if( tb.getName().toLowerCase().equalsIgnoreCase("mssql_dump")) throw new Error("You can not add trigger on mssql_dump");
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
    "CREATE OR REPLACE TRIGGER tri_ms_"+table+" after insert or delete or update on "+table+" for each row "+LINE_SEPERATOR+
    "declare "+LINE_SEPERATOR+
    "      sqlText varchar2(2000);"+LINE_SEPERATOR+
    "      typ char(1);"+LINE_SEPERATOR+
    "   begin"+LINE_SEPERATOR+
    "    -- THIS IS GENERATED BY Oracle2MSSQL FOR MIGRATING ORACLE DATA TO MSSQL"+LINE_SEPERATOR+
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
    "    insert into mssql_dump(id,tablename, objectid, action, sqltext) values (seq_mssql_dump.nextval, '"+table+"', :new."+pk+", typ, sqlText);"+LINE_SEPERATOR+
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

        String sql="create table "+ tb.getName().toLowerCase()+"(";
        String addition="";
        ArrayList idxs= new ArrayList();
        ArrayList al= getDBColumns(tb);

        int pkId= tb.getPrimaryKey().getId();
        int akId= -1;
        if( tb.getAlternateKey() !=null) akId=  tb.getAlternateKey().getId();
        for(int i=0;i< al.size(); i++){
            Column col= (Column) al.get(i);
            sql += (i>0?", ":"")+ col.getName().toLowerCase()+" ";
            switch( col.getType() ){
                case Column.DATE:
                    sql+= "datetime";
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
                 addition += ",constraint pk_"+tb.getName().toLowerCase() +  "  primary key ("+ col.getName().toLowerCase()+")"; //since nullable no pk now
//                 addition.add("create ,UNIQUE KEY PK_"+tb.getName().toLowerCase() +  " ("+ col.getName().toLowerCase()+")";
             }else if ( col.getId() == akId){
                 //addition += ",KEY IDX_AK"+tb.getName().toLowerCase()+ " ("+ col.getName().toLowerCase()+")";
                 idxs.add("create index idx_ak"+tb.getName().toLowerCase()+ " on " +tb.getName().toLowerCase()+ " ("+ col.getName().toLowerCase()+")");
             }else if( col.getName().toLowerCase().toLowerCase().indexOf("id")> 0){
                 idxs.add("create index idx_"+tb.getName().toLowerCase()+"_" +idxs.size()+ " on " +tb.getName().toLowerCase()+ " ("+ col.getName().toLowerCase()+")");
             }
        }

        sql+= addition+ ")";
        idxs.add(0, sql);

        doUpdate(idxs, conMssql);
    }
    private void dropTable( Table tb) throws Exception{
        doUpdate("drop table "+ tb.getName().toLowerCase(), conMssql);
    }
    private void truncateTable(Table tb) throws Exception{
        //logger.info("Truncate table "+ tb.getName().toLowerCase());
        doUpdate("truncate table "+ tb.getName().toLowerCase(), conMssql);
    }
    private void doUpdate(String sql, Connection con )  throws UpdateException{
        Vector v= new Vector();
        v.addElement(sql);
        doUpdate( v, con);
    }
    private int doUpdate(Collection vec, Connection con) throws UpdateException{
        String sql = null;

        if( noData==true){
            for ( Iterator it= vec.iterator();it.hasNext();){
                sql = (String) it.next();
                logger.debug(sql);
                logTableCreation.println(sql+";");
            }
            return 0;
        }
        Statement stmt=null;
        int count = 1; // start from 1 for user's convenience
        Iterator ite = vec.iterator() ;
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
            throw new UpdateException("row "+ count+" failed:"+e.getLocalizedMessage());
        }finally{
            try{stmt.close();}catch(Exception ea){}
        }
        return count;
    }
    /**
     * Check if table exists in mssql db
     */
    private boolean destTableExists( Table tb) throws Exception{
        Statement stmt=null;
        ResultSet rs=null;
        boolean b=false;
        try{
            stmt= conMssql.createStatement();
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
    /**
     *
     * @param al ArrayList
     * @return SQLType
     */
    private int[] getSQLTypes(ResultSetMetaData mt) throws SQLException{
        int[] a= new int[mt.getColumnCount()];
        for( int i=1;i<= mt.getColumnCount();i++) {
            a[i-1]= mt.getColumnType(i);
        }
        return a;
    }
    
    /**
     * Only migration id greater then maxid of dest table
     * Note dest table must has primary ke as Id (incremental)
     * @param tb
     * @throws Exception
     */
    public void pipeData(Table tb) throws Exception{
        if( noData==true) return;
        ResultSet rs=null;
        PreparedStatement pstmt=null;
        String sql="" , quest="";
        ArrayList al= getDBColumns(tb);
        int count= al.size();
        int c=0;
        int maxInsertCount= Tools.getInt( conf.getProperty("table.maxinserts", "-1"), -1);
        for (int i=0;i< al.size();i++){
            sql += (i>0?",":"") + ((Column)al.get(i)).getName().toLowerCase() ;
            quest += (i>0?",?":"?") ;
        }
        int maxDestId= getMaxTableId(conMssql,tb);
        Exception exp=null;
        try{
        	
        	
        rs=conOracle.createStatement().executeQuery("select count(*) from "+ tb.getName().toLowerCase() +" where id>" + maxDestId );
        if( rs.next() ){
            logger.debug("Table "+ tb.getName().toLowerCase() + " has "+ rs.getInt(1)+ " rows to be migrate, maximum " + (maxInsertCount==-1?"unlimited":maxInsertCount+"") + " rows will be inserted.");
        }
        rs.close();
        long timeStart= System.currentTimeMillis();
        pstmt= conMssql.prepareStatement("insert into "+ tb.getName().toLowerCase()+ " ( "+sql+ ") values ("+ quest+")");
        logger.debug("select "+ sql + " from "+ tb.getName().toLowerCase()+ " where id>"+ maxDestId);
        rs= conOracle.createStatement().executeQuery("select "+ sql + " from "+ tb.getName().toLowerCase()+" where id>"+ maxDestId);
        int[] colTypes = getSQLTypes(rs.getMetaData());
        while( rs.next() ){
            //try{
            	c++;
                if( c > maxInsertCount && maxInsertCount!=-1) break;
                for(int i=0;i<count ;i++){
                    Object o=rs.getObject(i + 1);
                    if ( rs.wasNull() )
                        pstmt.setNull(i+1, colTypes[i] );
                    else
                        pstmt.setObject(i+1, o);
                }
                pstmt.executeUpdate();
                if ( c % 1000==0) logger.debug( c+ " rows inserted.");
            //}catch(Exception e2){
            //	logger.debug("Could not insert record id=" + rs.getInt("ID")+":"+ e2.getMessage());
            //}
        }
        logger.debug("Time elapes "+ ((System.currentTimeMillis() - timeStart)/1000.0)+ " seconds for migrating "+ tb.getName().toLowerCase());
        }catch(Exception e2){
        	logger.debug("Could not insert record id=" + rs.getInt("ID")+":"+ e2.getMessage());
        	// according to titan's request, rollback all records
        	PreparedStatement pstmt2=null;
        	try{
        		pstmt2=conMssql.prepareStatement("delete from "+ tb.getName().toLowerCase()+ " where id>"+ maxDestId);
        		pstmt2.executeUpdate();
        	}catch(Exception e3){
        		logger.error("Could not delete inserted data for table " +tb.getName().toLowerCase() +" where id>" + maxDestId, e3);
        		
        	}finally{
        		try{pstmt2.close();}catch(Exception e){}
        	}
        	
        	exp=e2;
        }
        finally{
            if( pstmt !=null){try{ pstmt.close();}catch(Exception e){}}
            if( rs !=null){try{ rs.close();}catch(Exception e){}}
        }
        if( exp!=null) throw exp;
        
    }
    /**
     * Get max table id of the specified table, note table must has id as its primary key
     * @param con
     * @param table
     * @return 
     * @throws Exception
     */
    private int getMaxTableId(Connection con , Table table) throws Exception{
        ResultSet rs=null;
        PreparedStatement pstmt=null;
        int maxId=Integer.MIN_VALUE;
        try{
        	pstmt= con.prepareStatement("select maxid from possetting where tablename='" + table.getName().toLowerCase()+"'");
        	rs=pstmt.executeQuery();
        	if ( rs.next()) maxId= rs.getInt(1);
        }finally{
            if( pstmt !=null){try{ pstmt.close();}catch(Exception e){}}
            if( rs !=null){try{ rs.close();}catch(Exception e){}}
        }
        return maxId;
        // 12-16 min id
    }
    private static void usage() {
        System.err.println("Usage:\n  java nds.util.Oracle2MSSQL [-c properties]");
        System.err.println("\nOptions:");
        System.err.println("  -c : indicates the property file");
        System.exit(1);
    }

    public static void main(String[] argument) throws Exception{
        Oracle2MSSQL m=null;
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
          m= new Oracle2MSSQL(confs);

      }catch(Exception e){
          e.printStackTrace();
          usage();
          return;
       }
       m.start();
    }
    private static final String LINE_SEPERATOR=System.getProperty("line.separator");
}
