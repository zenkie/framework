package nds.net;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Properties;

import nds.query.QueryEngine;
import nds.util.CommandExecuter;
import nds.util.Tools;

/**
 * Export POS data to .gz file, and mv them to POS specified folder
 * 导出过程如下:
 * 有一张操作日志表 posexpdata ( id, shopId, shopGroupId, sqlText),记录在总部的关于门店的数据日志.
 * (注意对于门店重新初始化获得的大量数据,将通过另外的程序来完成.)
 * ExportPOSData 在执行时, 针对每一家门店进行数据生成
 *  for each pos
 *    dump following data to a file named $net.PosDB.Download.RootDir/$(customer.no)/yyyymmddHHMM.sql
 *    ( note all sql files have the same name[execution start time])
 *    select distinct posexpdata.id, sqltext from posexpdata, shopgroupR
 *           where posexpdata.shopid=$1 or posexpdata.shopid=-1
 *                 or (shopgroupR.shopId= $1 and posexpdata.shopgroupId=shopgroupR.shopgroupId )
 *           order by posexpdata.id asc
 *  next
 *  gzip all file has the same name
 *
 * 门店的定义:
 *   PosExpData的字段说明： shopID 如果为-1,表示所有的门店都接收此数据，否则对应于指定的门店
 *   CustomerSort 表中CustomerSort=1 的字段对应的ID(目前是 1, 2), 对应到Customer表
 *     create view vshop as select * from customer where (customersortid=1 or customersortid=2)
 *
 * 2003-07-25 增加将所有需要导出的基础数据，整理到一个单独的目录:
 *    需要导出的基础数据，有如下特征：
 *    对于 table.getDispatchType() ＝= table.DISPATCH_ALL，统一放置到 $download_root/full/目录下
 *     设置文件名为tablename，最后调用系统命令zippath合并到一个文件并压缩。
 *    对于 table.getDispatchType() ＝= table.DISPATCH_SPEC
 *      在每一个$download_root/full/$shop目录下建立，以tableName命名各个导出并合并并压缩
 *    放置到nds.net.ExportFullPOSData

 *  @changelog
在我们的oracle系统中有一个下发数据的存储队列，其中有仓库的配货出库单（在配货出库信息单提交时生成到队列）和门店的货品流转单（在门店上传总部时生成到队列），以及基本信息如产品、客户等。另外有一个java守护进程，每隔半小时将下发队列的数据取出，打包成门店可下载文件，再通过同步进程传递到mit3相应目录。

java守护进程取出待下发数据后，会将队列清空。问题就出在这里。

当守护进程开始取下发数据时，假设队列有1000条数据。整个打包过程的时间最长约需要4分钟。在系统高峰运行时间，数据量大的时候，时间更长。这时系统的其他模块可能产生了新的下发数据。也就是说，在打包100条数据的时候，又有一些下发数据生成了。而打包进程并不知道这些数据。等打包结束。java进程调用了清空方法，导致100多条数据都被清除。造成了数据的丢失。

我们修正了这个bug，并于2004-04-24部署完成。改进的方法是，在打包前，记录下发队列的最后一条数据的位置。在打包完成后，仅清除记录位置前（包括记录位置本身）的数据。

 *
 */
public class ExportPOSData extends ThreadProcess {
    private final static SimpleDateFormat expFileNameFormatter
            =new SimpleDateFormat("yyyyMMddHHmm");

    private String downloadRootDir;
    private String expFileName;
    private String tmpDir;
    private QueryEngine engine;
    private ExportFullPOSData fpd= new ExportFullPOSData();
    private String cmdRootPath;

    public ExportPOSData() {

    }
    public void init(Properties props) {
        // getting property needed
        if(props ==null)props=new Properties();
        downloadRootDir = props.getProperty("PosDB.Download.RootDir", "f:/act/posdb/download");
        tmpDir =props.getProperty("PosDB.TmpDir", "f:/act/posdb/tmp");
        cmdRootPath= props.getProperty("cmd.root", "/");
        fpd.init(props);
    }
    /**
    * Create one shop exp data
    * @param maxId only data whose id is less or equal than maxId will be loaded
    */
    private void executeOne(int shopId, String shopNo, Connection conn, long maxId) {
        ResultSet rs=null;
        PreparedStatement stmt=null;
        PrintWriter out=null;
        try{
        String path= downloadRootDir + "/" + shopNo ;
        String file=path +"/" + expFileName;

        String exportSQL="select  posexpdata.id, sqltext from posexpdata " +
            "where posexpdata.id<= "+ maxId + " and ( posexpdata.shopid=? or posexpdata.shopid=-1 or posexpdata.shopgroupid=-1 "+
                 " or (posexpdata.shopgroupid in ( select shopgroupId from shopgroupR " +
                      " where shopId=? ))) " +
            "order by posexpdata.id asc";

        stmt=conn.prepareStatement(exportSQL);
        stmt.setInt(1, shopId);
        stmt.setInt(2, shopId);
        rs= stmt.executeQuery();

        String data;
        int count=0;

        while( rs.next() ){
            if( count ==0){
                CreateFolder(path);
                out = new PrintWriter(new BufferedWriter(new FileWriter(file)));
            }
            data= rs.getString(2);
            out.println(data);
            count ++;
        }
        //if(count >0)logger.info("Total " + count+ " records exported for Shop:" +
        //            shopNo+ " to " +file );
        }catch(Exception e){
            logger.error("Export POS data for " + shopNo+ "failed", e);
        }finally{
            try{ if(stmt !=null) stmt.close();}catch(Exception e2){e2.printStackTrace() ;}
            if(out !=null)out.close();
        }
    }
    public void execute() {
        fpd.execute();
//        if(true) return;


        Connection conn=null;
        Statement stmt=null;
        try{

            conn= QueryEngine.getInstance().getConnection();
            ResultSet rs=null;
            // yfzhu 2004-04-24 get max id from posexpdata
            long maxId=-1;
            stmt= conn.createStatement();
            rs=stmt.executeQuery("select max(id) from posexpdata");
            if ( rs.next()){
                maxId= rs.getLong(1);
            }
            rs.close();
            stmt.close();

            expFileName= expFileNameFormatter.format(new java.util.Date())+".sql";
            stmt= conn.createStatement();
            rs=stmt.executeQuery("select id,no from vshop");
            int id; String no;
            while( rs.next() ){
                id= rs.getInt(1);
                no= rs.getString(2);
                this.executeOne(id, no, conn,maxId);
            }
            // gzip all files
            // start a command like :
            // "find /tmp/download -name 200211301231.sql | gzip"
            String outputFileName=tmpDir +"/" + expFileName +".out";
            CommandExecuter exec= new CommandExecuter(outputFileName);
            String cmd=  "sh "+ cmdRootPath+ "/zipsql " +downloadRootDir +" "+expFileName;
            exec.run(cmd);
            logger.debug("Result in executing " +cmd+":" + Tools.getFileContent(outputFileName ));
            // delete all in posexpdata
            stmt.close();
            stmt= conn.createStatement();
            int d=stmt.executeUpdate("delete from posexpdata where id<="+ maxId);
            logger.debug("Total " + d+ " records deleted in posexpdata");

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


}



