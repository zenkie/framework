/*
 * Created on 2004-11-18
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package nds.export;

import java.text.*;
import oracle.sql.BLOB;
import java.util.*;
import java.sql.*;

import nds.log.Logger;
import nds.log.LoggerManager;
import nds.query.*;
import nds.control.event.NDSEventException;
import nds.control.util.ValueHolder;
import nds.excel.*;
import nds.report.ReportUtils;
import nds.util.*;

import java.io.*;
/**
 *Currently Only Locale.China supported
 */
public class ExportJob {
    private static Logger logger= LoggerManager.getInstance().getLogger(ExportJob.class.getName());
    private final static int MAX_REPORT_LINES= 65535; //最大导出行数    
	private int id=-1;
	private String no;
	private int userId;
	private String ipAddress;
	private java.util.Date creationDate;
	private java.util.Date modifiedDate;
	private int modifierId;
	private String exportType;
	private String tableName;
	private String tableDesc;
	private String sqlText;
	private String sqlDesc;
	private String location;
	private String fileName;
	private String is_ak_used; // Y/N
	private String is_pk_used;
	private Object queryObj;
	private String colsName;
	private String seperator;
	private int priority; 
	private int clientId;
	private String show_coldesc; // Y/N whether show column description on the first row or not
	private final static String SQL_INSERT="insert into spo_export(id, no, userid, ipaddress, creationdate,modifieddate,modifierid,exporttype,tablename,tabledesc,sqltext,sqldesc,"+
	"location,filename,is_ak_used,is_pk_used,query_obj,colsname,seperator, show_coldesc, priority, ad_client_id)values(?,?,?,?,?,?,?,?,?,?,?,"+
	"?,?,?,?,?,?,?,?,?,?,?)";
	private final static String SQL_GET="select id, no, userid, ipaddress, creationdate,modifieddate,modifierid,exporttype,tablename,tabledesc,sqltext,sqldesc,"+
	"location,filename,is_ak_used,is_pk_used,query_obj,colsname,seperator, show_coldesc,priority, ad_client_id from spo_export where id=?";
	private final static String SQL_DELETE="delete from spo_export where id=?";
	private final static String SQL_GET_USER="select name from users where id=?";
	private final static String SQL_LOG="insert into spo_export_log(id,ad_client_id, no, userid, ipaddress, creationdate,modifieddate,modifierid,exporttype,tablename,tabledesc,sqltext,sqldesc,"+
	"location,filename,is_ak_used,is_pk_used,query_obj,colsname,seperator, show_coldesc, priority, starttime,state, statemsg, duration) select id,ad_client_id,no, userid,ipaddress, creationdate,modifieddate,modifierid,exporttype,tablename,tabledesc,sqltext,sqldesc,"+
	"location,filename,is_ak_used,is_pk_used,query_obj,colsname,seperator, show_coldesc ,priority,?,?,?,? from spo_export where id=?";
	public ExportJob(int id){
		// read from db
		this.id=id;
	}
	/**
	 * Load from db according to id, which must not be -1
	 * @throws Exception if load failed
	 */
	public void loadFromDB() throws Exception{
		if( id ==-1 ) throw new Exception("id may not be -1!");
		Connection conn=QueryEngine.getInstance().getConnection();
		PreparedStatement st=null;
		ResultSet rs=null;
		try{
			int i=2;
			st=conn.prepareStatement(SQL_GET);
			st.setInt(1,this.id);
			rs=st.executeQuery();
			if ( !rs.next()) throw new NDSException("Not found job with id="+ id);
			no=rs.getString(i++);
			userId= rs.getInt(i++);
			this.ipAddress= rs.getString(i++);
			this.creationDate=rs.getTimestamp(i++);
			this.modifiedDate= rs.getTimestamp(i++);
			this.modifierId= rs.getInt(i++);
			this.exportType= rs.getString(i++);
			this.tableName = rs.getString(i++);
			this.tableDesc= rs.getString(i++);
			this.sqlText = rs.getString(i++);
			this.sqlDesc = rs.getString(i++);
			this.location= rs.getString(i++);
			this.fileName = rs.getString(i++);
			this.is_ak_used= rs.getString(i++);
			this.is_pk_used= rs.getString(i++);
			
			InputStream is=rs.getBinaryStream(i++);
			ObjectInputStream p = new ObjectInputStream(is); 
			queryObj =  p.readObject(); 
			
			this.colsName= rs.getString(i++);
			this.seperator= rs.getString(i++);
			this.show_coldesc=rs.getString(i++);
			this.priority= rs.getInt(i++);
			this.clientId= rs.getInt(i++);
			logger.debug("load from db(id="+ id+ ")");
			//logger.debug("sqlText:"+ this.sqlText);
			logger.debug("query:"+ ((QueryRequest)this.queryObj).getSQLForReport(Tools.getBoolean(is_pk_used,true), Tools.getBoolean(is_ak_used,true)));
		}finally{
			try{rs.close();}catch(Exception e){}
			try{st.close();}catch(Exception e){}
			try{conn.close();}catch(Exception e){}
		}
	}
	/**
	 * Execute specified job, log the information to db
	 * @throws Exception
	 */
	public void execute() throws Exception{
		long bt= System.currentTimeMillis();
		int state= 0; // 0 for ok, others for error
		String stateMsg="OK";
		// check file existance
		File file=new File(location+"/"+ fileName);
		if (file.exists()){
			state=1;
			stateMsg="Specified filename exists, not override.";
		}else{
			try{
				if("excel".equalsIgnoreCase(exportType)){
					exportToExcel();
				}else{
					exportToStoredProcedure();
				}
				//log file size
				stateMsg += " ("+this.formatSize(file.length())+")";
			}catch(Exception e){
				stateMsg=e.getMessage();
				state= -1;
			}
		}
		logJob( bt, state, stateMsg);
	}
	

	private void logJob(long startTime, int state, String stateMsg){
		Connection conn=null;
		PreparedStatement st=null;
		//ResultSet rs=null;
		java.sql.Date d= new java.sql.Date(startTime);
		try{
			int i=1;
			conn=QueryEngine.getInstance().getConnection();
			st=conn.prepareStatement(SQL_LOG);
			st.setTimestamp(i++,new Timestamp(startTime));
			st.setInt(i++, state);
			st.setString(i++, stateMsg);
			st.setLong(i++, (System.currentTimeMillis()- startTime)/60000); // in minutes
			st.setInt(i++, this.id);
			st.executeUpdate();
			st.close();
			
			st= conn.prepareStatement(SQL_DELETE);
			st.setInt(1, id);
			st.executeUpdate();
			st.close();
		}catch(Exception e){
			logger.error("Could not log job: id="+ this.id+", startTime:"+ d+", state="+ state+",stateMsg="+ stateMsg, e);
		}finally{
			//try{rs.close();}catch(Exception e){}
			//try{st.close();}catch(Exception e){}
			try{conn.close();}catch(Exception e){}
		}
		logger.debug("job no="+ this.no + " startTime="+new Timestamp(startTime) +",duration="+  ( (System.currentTimeMillis()- startTime)/60000) +" min, statemsg="+stateMsg );
		
	}
	/**
	 * Export to excel file according to parameters
	 * @throws Exception
	 */
	private void exportToExcel() throws Exception{
        if ( QueryEngine.getInstance().getTotalResultRowCount((QueryRequest)queryObj)> MAX_REPORT_LINES )
	     	throw new QueryException("将导出的记录行数大于允许值("+MAX_REPORT_LINES+")，请分页导出！");

		ExportExcel ee=new nds.excel.ExportExcel(Locale.CHINA);
		ee.setParameter("sql", this.sqlText);
		ee.setParameter("location", this.location);
		ee.setParameter("filename", this.fileName);
		ee.setParameter("ak", new Boolean(Tools.getBoolean(this.is_ak_used,true)));
		ee.setParameter("pk", new Boolean(Tools.getBoolean(this.is_pk_used,true)));
		ee.setParameter("query", this.queryObj);
		ee.run();
		//gc
		System.gc();
		if (ee.getLastError() !=null){
			// found error, log to exception
			throw ee.getLastError();
		}
	}
	
	private String getUserNameById(int uid){
		Connection conn=null;
		PreparedStatement st=null;
		ResultSet rs=null;
		String n="NA";
		try{
			conn=QueryEngine.getInstance().getConnection();
			st=conn.prepareStatement(SQL_GET_USER);
			st.setInt(1, uid);
			rs=st.executeQuery();
			if(rs.next()){
				n= rs.getString(1);
			}
		}catch(Exception e){
			logger.error("Could not get user name(id="+ uid+")", e);
		}finally{
			try{rs.close();}catch(Exception e){}
			try{st.close();}catch(Exception e){}
			try{conn.close();}catch(Exception e){}
		}
		return n;
		
	}
	/**
	 * Use stored procedure to export data
	 * @throws Exception
	 */
	private void exportToStoredProcedure() throws Exception{
		String userName= getUserNameById(this.getUserId());
		QueryRequest qRequest= (QueryRequest) this.queryObj;
        File svrDir = new File(location);
        if(!svrDir.isDirectory()){
            svrDir.mkdir();
        }
    	//Create a empty file, so oracle can write data in it
    	FileWriter fwb = new FileWriter(location+"/"+ fileName);
    	fwb.write("");
    	fwb.close();
        
        svrDir = new File(location+"/desc");
        if(!svrDir.isDirectory()){
            svrDir.mkdir();
        }
		String descPath = location+"/desc/"+ fileName;//svrPath + File.separator + "desc" + File.separator + fileName;
		// write description into description file 
        FileWriter fw = new FileWriter(descPath);
        fw.write(this.tableDesc + ":"+ this.sqlDesc);
        fw.close();
        
		SPResult res=null;
	    ArrayList params=new ArrayList();
	    params.add(this.sqlText);
	    params.add(location);
	    params.add(fileName);
	    String header=null;
	    if("html".equalsIgnoreCase(exportType)){
	    	header=(qRequest == null)?"":ReportUtils.getHeaderHtml(Tools.getBoolean(this.is_pk_used, true),Tools.getBoolean(this.is_ak_used,true),qRequest);
		    params.add(Tools.getBoolean(show_coldesc,true)?header:"");
	        params.add(new Integer("0"));
	        params.add("0");
	        params.add("0");
	        res = QueryEngine.getInstance().executeStoredProcedure("REPORTCREATEHTML",params,true);
	    }else{
	    	header= (qRequest == null)?"":ReportUtils.getHeader(
	    			Tools.getBoolean(this.is_pk_used, true),Tools.getBoolean(this.is_ak_used,true),this.seperator, qRequest);
	    	if (this.colsName!=null) header= this.colsName;
	    	
	    	params.add(Tools.getBoolean(show_coldesc,true)?header:"");
	    	params.add(this.seperator);
	        params.add(new Integer("0"));
	        params.add("0");
	        params.add("0");
	        res = QueryEngine.getInstance().executeStoredProcedure("REPORTCREATETXT",params,true);
	    }
	    
        if(! res.isSuccessful()){
            throw new NDSException(res.getMessage());
        }
        
		
	}
	/**
	 * Store information to database
	 * @throws Exception
	 */
	public void store() throws Exception{
		if( id !=-1 ) throw new Exception("id is not -1!");
		Connection conn=QueryEngine.getInstance().getConnection();
		PreparedStatement st=conn.prepareStatement(SQL_INSERT);
		no=QueryEngine.getInstance().getSheetNo("SPO_EXPORT" , clientId);
		id=QueryEngine.getInstance().getSequence("SPO_EXPORT");
		try{
			int i=1;
			st.setInt(i++, id);
			st.setString(i++, no);
			st.setInt(i++, userId);
			st.setString(i++, ipAddress);
			st.setTimestamp(i++, new java.sql.Timestamp( this.creationDate.getTime()));
			st.setTimestamp(i++, new java.sql.Timestamp( this.modifiedDate.getTime()));
			st.setInt(i++, modifierId);
			st.setString(i++, exportType);
			st.setString(i++, tableName);
			st.setString(i++, tableDesc);
			st.setString(i++,sqlText);
			st.setString(i++,sqlDesc);
			st.setString(i++,location);
			st.setString(i++,fileName);
			st.setString(i++,is_ak_used);
			st.setString(i++,is_pk_used);
			
			byte[] b=Tools.toByteArray((Serializable)queryObj);
			ByteArrayInputStream bi = 
			      new ByteArrayInputStream(b); 
			
			st.setBinaryStream(i++, bi, b.length );
			st.setString(i++,colsName);
			st.setString(i++,seperator);
			st.setString(i++, show_coldesc);
			st.setInt(i++, priority);
			st.setInt(i++, clientId);
			
			logger.debug("save to db (id="+id +")");
			//logger.debug("sqlText:"+ this.sqlText);
			logger.debug("query:"+ ((QueryRequest)this.queryObj).getSQLForReport(Tools.getBoolean(is_pk_used,true), Tools.getBoolean(is_ak_used,true)));
			
			st.executeUpdate();
		}finally{
			try{st.close();}catch(Exception e){}
			try{conn.close();}catch(Exception e){}
		}
		
	}
	/**
	 * @return Returns the colsName.
	 */
	public String getColsName() {
		return colsName;
	}
	/**
	 * @param colsName The colsName to set.
	 */
	public void setColsName(String colsName) {
		this.colsName = colsName;
	}
	
	/**
	 * @return Returns the creationDate.
	 */
	public java.util.Date getCreationDate() {
		return creationDate;
	}
	/**
	 * @param creationjava.util.Date The creationjava.util.Date to set.
	 */
	public void setCreationDate(java.util.Date creationDate) {
		this.creationDate = creationDate;
	}
	/**
	 * @return Returns the exportType.
	 */
	public String getExportType() {
		return exportType;
	}
	/**
	 * @param exportType The exportType to set.
	 */
	public void setExportType(String exportType) {
		this.exportType = exportType;
	}
	/**
	 * @return Returns the fileName.
	 */
	public String getFileName() {
		return fileName;
	}
	/**
	 * @param fileName The fileName to set.
	 */
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	/**
	 * @return Returns the id.
	 */
	public int getId() {
		return id;
	}
	/**
	 * @param id The id to set.
	 */
	public void setId(int id) {
		this.id = id;
	}
	/**
	 * @return Returns the ipAddress.
	 */
	public String getIpAddress() {
		return ipAddress;
	}
	/**
	 * @param ipAddress The ipAddress to set.
	 */
	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}
	/**
	 * @return Returns the is_ak_used.
	 */
	public String getIs_ak_used() {
		return is_ak_used;
	}
	/**
	 * @param is_ak_used The is_ak_used to set.
	 */
	public void setIs_ak_used(String is_ak_used) {
		this.is_ak_used = is_ak_used;
	}
	/**
	 * @return Returns the is_pk_used.
	 */
	public String getIs_pk_used() {
		return is_pk_used;
	}
	/**
	 * @param is_pk_used The is_pk_used to set.
	 */
	public void setIs_pk_used(String is_pk_used) {
		this.is_pk_used = is_pk_used;
	}
	/**
	 * @return Returns the location.
	 */
	public String getLocation() {
		return location;
	}
	/**
	 * @param location The location to set.
	 */
	public void setLocation(String location) {
		this.location = location;
	}
	/**
	 * @return Returns the modifiedDate.
	 */
	public java.util.Date getModifiedDate() {
		return modifiedDate;
	}
	/**
	 * @param modifiedjava.util.Date The modifiedjava.util.Date to set.
	 */
	public void setModifiedDate(java.util.Date modifiedDate) {
		this.modifiedDate = modifiedDate;
	}
	/**
	 * @return Returns the modifierId.
	 */
	public int getModifierId() {
		return modifierId;
	}
	/**
	 * @param modifierId The modifierId to set.
	 */
	public void setModifierId(int modifierId) {
		this.modifierId = modifierId;
	}
	/**
	 * @return Returns the no.
	 */
	public String getNo() {
		return no;
	}
	/**
	 * @param no The no to set.
	 */
	public void setNo(String no) {
		this.no = no;
	}
	/**
	 * @return Returns the queryObj.
	 */
	public Object getQueryObj() {
		return queryObj;
	}
	/**
	 * @param queryObj The queryObj to set.
	 */
	public void setQueryObj(Object queryObj) {
		this.queryObj = queryObj;
	}
	/**
	 * @return Returns the seperator.
	 */
	public String getSeperator() {
		return seperator;
	}
	/**
	 * @param seperator The seperator to set.
	 */
	public void setSeperator(String seperator) {
		this.seperator = seperator;
	}
	/**
	 * @return Returns the sqlDesc.
	 */
	public String getSqlDesc() {
		return sqlDesc;
	}
	/**
	 * @param sqlDesc The sqlDesc to set.
	 */
	public void setSqlDesc(String sqlDesc) {
		this.sqlDesc = sqlDesc;
	}
	/**
	 * @return Returns the sqlText.
	 */
	public String getSqlText() {
		return sqlText;
	}
	/**
	 * @param sqlText The sqlText to set.
	 */
	public void setSqlText(String sqlText) {
		this.sqlText = sqlText;
	}
	/**
	 * @return Returns the tableDesc.
	 */
	public String getTableDesc() {
		return tableDesc;
	}
	/**
	 * @param tableDesc The tableDesc to set.
	 */
	public void setTableDesc(String tableDesc) {
		this.tableDesc = tableDesc;
	}
	/**
	 * @return Returns the tableName.
	 */
	public String getTableName() {
		return tableName;
	}
	/**
	 * @param tableName The tableName to set.
	 */
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
	/**
	 * @return Returns the userId.
	 */
	public int getUserId() {
		return userId;
	}
	/**
	 * @param userId The userId to set.
	 */
	public void setUserId(int userId) {
		this.userId = userId;
	}
	/**
	 * @return Returns the show_coldesc.
	 */
	public String getShow_coldesc() {
		return show_coldesc;
	}
	/**
	 * @param show_coldesc The show_coldesc to set.
	 */
	public void setShow_coldesc(String show_coldesc) {
		this.show_coldesc = show_coldesc;
	}
	/**
	 * @return Returns the priority.
	 */
	public int getPriority() {
		return priority;
	}
	/**
	 * @param priority The priority to set.
	 */
	public void setPriority(int priority) {
		this.priority = priority;
	}
	/**
     * Format int size into String presentation, if size is in byte range, format as "xxx 字节"
     * if size is in kilo byte, format as "xx.xx KB", if size is in mega bytes range, format as
     * "xx.xx MB"
     * @param sizeBytes - the size in unit of byte
     * @return String with unit reshaped
    */
    private static DecimalFormat format=new DecimalFormat("#,##0.00") ;
    private String formatSize(long size){

        String ret;
		if( size <0) ret="N/A";
		else if( size < 1024) ret= size+" B";
		else if( size > 1024 && size <( 1024*1024)){
			double f=size / 1024.0;
            ret= (format.format(f)+ " KB");

		}else{
			double f=size / (1024.0 * 1024);
            ret= (format.format(f)+ " MB");

        }
        return ret;
    }	
	/**
	 * @return Returns the clientId.
	 */
	public int getClientId() {
		return clientId;
	}
	/**
	 * @param clientId The clientId to set.
	 */
	public void setClientId(int clientId) {
		this.clientId = clientId;
	}
}
