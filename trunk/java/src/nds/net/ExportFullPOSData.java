package nds.net;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;

import nds.query.QueryEngine;
import nds.schema.Column;
import nds.schema.Table;
import nds.schema.TableManager;
import nds.util.CommandExecuter;
import nds.util.Tools;

/**
 * 2003-07-25 增加将所有需要导出的基础数据，整理到一个单独的目录:
 *    需要导出的基础数据，有如下特征：
 *    对于 table.getDispatchType() ＝= table.DISPATCH_ALL，统一放置到 $download_root/full/目录下
 *     设置文件名为tablename，最后调用系统命令zippath合并到一个文件并压缩。
 *    对于 table.getDispatchType() ＝= table.DISPATCH_SPEC
 *      在每一个$download_root/full/$shop目录下建立，以tableName命名各个导出并合并并压缩
 *
 *
 *
 */
public class ExportFullPOSData extends ThreadProcess {
    private final static SimpleDateFormat expFileNameFormatter
            =new SimpleDateFormat("yyyyMMddHHmm");

    private String downloadRootDir;
    private String expFileName;
    private String tmpDir;
    public ExportFullPOSData() {

    }
    public void init(Properties props) {
        // getting property needed
        if(props ==null)props=new Properties();
        downloadRootDir = props.getProperty("PosDB.Download.RootDir", "f:/act/posdb/download");
        tmpDir =props.getProperty("PosDB.TmpDir", "f:/act/posdb/tmp");
        TableManager tm = TableManager.getInstance() ;
        if(! tm.isInitialized() ){
            //"directory"
            tm.init(props);
        }
    }

    public void execute() {
        QueryEngine engine;
        Connection conn=null;
        Statement stmt=null;
        try{

            conn= QueryEngine.getInstance().getConnection();
            this.handleDispathAllTables(conn);

            expFileName= expFileNameFormatter.format(new java.util.Date())+".sql";
            ResultSet rs=null;
            stmt= conn.createStatement();
            rs=stmt.executeQuery("select id,no from vshop");
            int id; String no;
            while( rs.next() ){
                id= rs.getInt(1);
                no= rs.getString(2);
                this.handleDispatchSpecTables(id, no, conn);
            }
            // gzip all files
            // start a command like :
            // "find /tmp/download -name 200211301231.sql | gzip"
            String outputFileName=tmpDir +"/" + expFileName +".out";
            CommandExecuter exec= new CommandExecuter(outputFileName);
            String cmd=  "sh /zippath " +downloadRootDir +"/full";
            exec.run(cmd);
            logger.debug("Result in executing " +cmd+":" + Tools.getFileContent(outputFileName ));

        }catch(Exception e){
            logger.error("Error exporting data.", e);
        }finally{
            try{if( stmt !=null) stmt.close();}catch(Exception e2){}
            try{if( conn!=null)conn.close();}catch(Exception  ee){}
        }

    }
    private void CreateFolder(String path){
        try{
            File file= new File(path);
            if (!file.exists() ) file.mkdirs() ;
        }catch(Exception e){
            ;
        }
    }

    private String createQuery(Table table , int shopId, Connection conn) throws Exception{
        String sql= createQuery( table, conn);
        sql += " where "+ table.getDispatchColumn().getName() +
               " in ( select shopgroupId from shopgroupR where shopid=" + shopId+ ")";
        return sql;
    }
    private String createQuery(Table table , Connection conn) throws Exception{
        String sql="select 'insert into " + table.getDispatchTableName() + "(";
        String v=") values ('";
        Column col;int i=0;
        for(Iterator it= table.getAllColumns().iterator() ;it.hasNext();){
            col=(Column)it.next();
            sql += ( i > 0 ? "," :"")+ col.getName() ;
            v += ( i > 0 ? " || ',' || " :" || ")+ getColumnToString(col);
            i++;
        }
        sql += v + "|| ');' from "+ table.getName();
        return sql;
    }
    private String getColumnToString(Column col){
        String s="";
        switch(col.getType() ){
            case Column.DATE:
                s= "nvl(to_char("+ col.getName() + ", '''yyyy-MM-dd HH:mm:ss'''), 'null')";
                break;
            case Column.NUMBER:
            case Column.DATENUMBER:
                s= "nvl(" + col.getName() +",0)";
                break;
            case Column.STRING:
                s= "nvl('''' || replace( replace("+ col.getName()+ ", '\\','\\\\'),'''','\\''')  || '''','null')";
                break;
            default:
                s= col.getName();
        }
        return s;
    }
    private void handleDispatchSpecTables(int shopId, String shopNo, Connection conn) throws Exception{
        String sql=null;
        String file= downloadRootDir + "/full/" + shopNo+ ".sql" ;
        PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(file)));
        try {
            ArrayList tables= getDispatchTables(Table.DISPATCH_SPEC  );
            int i;Table table;
            Statement stmt=null;
            ResultSet rs=null;String  s ; int j;
            for(i=0;i< tables.size();i++){
                table= (Table)tables.get(i);
                sql= createQuery(table, shopId, conn);  // like select xxx from table
                stmt= conn.createStatement();
                rs=stmt.executeQuery(sql);
                j=0;
                out.println("TRUNCATE TABLE "+ table.getDispatchTableName()  + ";");
                out.println("LOCK TABLES "+ table.getDispatchTableName() + " WRITE;");
                while( rs.next() ){
                    s=rs.getString(1);
                    j++;
                    out.println(s);
                }
                out.println("UNLOCK TABLES ;");
                out.flush();
//                logger.debug("Total " + j + " exported from " + table.getName() + " fro shopNo=" + shopNo);
                rs.close() ;
                stmt.close() ;
            }
            out.close();
        }
        catch (Exception ex) {
            logger.error("Error parsing:"+ sql, ex);
            out.close();
        }

    }

    private void handleDispathAllTables(Connection conn) throws Exception{
        String sql=null;
        String file= downloadRootDir + "/full/full.sql" ;
        PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(file)));
        try {
            ArrayList tables= getDispatchTables(Table.DISPATCH_ALL );
            int i;Table table;
            Statement stmt=null;
            ResultSet rs=null;String s ; int j;
            for(i=0;i< tables.size();i++){
                table= (Table)tables.get(i);

                sql= createQuery(table, conn);// like select xxx from table

                stmt= conn.createStatement();
                rs=stmt.executeQuery(sql);
                j=0;
                out.println("TRUNCATE TABLE "+ table.getDispatchTableName() + ";");
                out.println("LOCK TABLES "+ table.getDispatchTableName() + " WRITE;");
                while( rs.next() ){
                    s=rs.getString(1);
                    j++;
                    out.println(s);
                }
                out.println("UNLOCK TABLES ;");
                logger.debug("Total " + j + " exported from " + table.getName() );
                out.flush();
                rs.close() ;
                stmt.close() ;
            }
            // add new time infor to appsetting table
            out.println("update appsetting set  value='"+ expFileNameFormatter.format(new java.util.Date()) + ".sql.gz' where name='LastDownloadFile';");

            out.close();
        }
        catch (Exception ex) {
            logger.error("Error parsing " + sql , ex);
            out.close();
        }
    }
    /**
     * @param dispatchType DISPATCH_ALL or DISPATCH_SPEC
     * @return elements are Table
     *
     */
    private ArrayList getDispatchTables(int dispatchType){
        ArrayList al =new ArrayList();
        TableManager tm = TableManager.getInstance() ;
        Table t;
        for(Iterator it=tm.getAllTables().iterator();it.hasNext() ; ){
            t=(Table) it.next() ;
            if( t.getDispatchType()== dispatchType )
                al.add(t);
        }
        return al;
    }


}



