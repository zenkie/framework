/*
 * Agile Control Technologies Ltd,. CO.
 * http://www.agileControl.com
 */
package nds.schema;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.io.Writer;
import java.net.URL;
import java.sql.*;
import java.util.*;
import nds.util.*;
import nds.query.*;
import nds.control.event.NDSEventException;
import nds.model.dao.*;

import nds.model.*;
import org.hibernate.*;
import org.hibernate.cfg.Configuration;
import nds.log.*;

import java.sql.*;

import org.apache.log4j.Level;
 
/**
 * Will open a session connect to hsql,import tables to oracle 
 * 
 * This version will copy attributes of objects such as table, column, limitvalue, into oracle 
 * event if object exists. 
 * 
 * Under construction (2008-06-03)
 * @author yfzhu@agilecontrol.com
 * @since 3.0
 */
public class SchemaImport2 {
	private static Logger logger= LoggerManager.getInstance().getLogger(SchemaImport2.class.getName());
	
	private Session destSession, srcSession;
	private SessionFactory sf ; // dest Session factory;
	private Transaction trans; // for dest session
	private java.sql.Connection srcConn, destConn;
	private QueryEngine engine;
	private Writer output;
	private long beginTime;
	public void outputMessage(String s) throws java.io.IOException{
		output.write(s + StringUtils.LINE_SEPARATOR);
		logger.debug(s);
	}
	public void rollback() throws Exception{
		if(trans!=null)
		trans.rollback();
		else
			destConn.rollback();
		outputMessage("Transaction rolled back");
	}
	public void commit()throws Exception{
 		destSession.flush();
		if(trans!=null){
			trans.commit();
		}else
			destConn.commit();
		outputMessage("Transaction committed");
	}
	/**
	 * Create connection according to file
	 * @param file like "/path/db", note db should not has extension
	 * @return
	 * @throws Exception
	 */
	private Connection  readFromFile(String file) throws Exception{
		Connection conn= DriverManager.getConnection("jdbc:hsqldb:file:"+ file ,"sa","");
		//outputMessage("HSQL DB loaded:jdbc:hsqldb:file:"+ file);
		return conn;
	}
	
	/**
	 * Destroy hsql and oracle session
	 * 
	 */
	public void destroy() throws Exception {
		try{
			afterTransfer();
		}catch(Exception e){
			logger.error("Fail to enable triggers:"+e);
		}
	
		try{
				if(destSession!=null && destSession.isOpen()) {
					destConn =destSession.close(); // so dest connection may be null, if created by session, or from outside, so need connection close from outside too
				}
			}catch(Exception e){}
			// shutdown hsql file db
			
			try{
				if(srcSession!=null && srcSession.isOpen()) {
					srcConn = srcSession.close();
					if(srcConn!=null){
						ArrayList al=new ArrayList();
						al.add("shutdown");
						engine.getInstance().doUpdate(al, srcConn);
						try{
							srcConn.close();
						}catch(Exception e){}
					}
				}
			}catch(Exception e){}
			// do not need to close dest connection, since it may be set from outside, not created by session.
			/*
			if(destConn!=null && destConn.isClosed()==false){
				try{destConn.close();}catch(Exception e){}
			}*/

			sf.close();
			outputMessage("Schema import process finished. Time elapse:"+ ((System.currentTimeMillis()- this.beginTime)/1000)+ " seconds");
	}
	/**
	 * Init hsql db configurations
	 * @param map at least following elements should exists:
	 *     "srcdbfile" - (String)script file of the hsql, which contains all tables schema definition,
	 * 			must has extension as "script", and in the same directory ".properties" should also exist
	 * 	   "hsqlxml" - (URL) the hsql configure xml file		
	 *     
	 * @param scriptFile 
	 */
	public void init(java.net.URL hsqlConfigXMLURL, String scriptDbFile, Writer output, Connection destConnection) throws Exception{
		this.beginTime= System.currentTimeMillis();
		this.output=output;
		outputMessage("Schema import process initializing...");		
		engine=  QueryEngine.getInstance();
		if( destConnection !=null){
			destConn= destConnection;
			destSession= AdTableDAO.getInstance().createSession(destConn);
			
			//disable ad_table and ad_column triggers and fks first
			beforeTransfer();
			outputMessage("Using connection from outside");
		}else{
			destSession= AdTableDAO.getInstance().createSession();
			if(!destSession.isConnected()){
				logger.debug("Reconnect to dest.");
				destSession.reconnect();
			}
			destConn=destSession.connection();
			//disable ad_table and ad_column triggers and fks first
			beforeTransfer();
			trans=destSession.beginTransaction();
			outputMessage("Using connection from internal");
		}
		
		sf= new Configuration().configure(hsqlConfigXMLURL).buildSessionFactory();
		
		logger.debug("Session factory loaded according to "+hsqlConfigXMLURL);
		srcConn= readFromFile(scriptDbFile);
		srcSession=sf.openSession(srcConn);
		
	}
	/**
	 * Disable triggers and fks or not, should be called before and after transaction 
	 * @throws Exception
	 */
	private void beforeTransfer() throws Exception{
		List al=new ArrayList();
		engine.executeStoredProcedure("ad_schema_before_import", al, false, destConn);
	}

	private void afterTransfer() throws Exception{
		List al=new ArrayList();
		engine.executeStoredProcedure("ad_schema_after_import", al, false, destConn);
	}

	public void transferTable(String tableName) throws Exception{
		//List al= srcSession.find("from AdTable a where a.Name='"+tableName.toUpperCase()+"'");
		List al= srcSession.createQuery("from AdTable a where a.Name='"+tableName.toUpperCase()+"'").list();		
		if( al.size()>0){
			transferTable( ((AdTable) al.get(0)).getId());
		}else{
			outputMessage("Table definition "+ tableName +" exists.");
		}
		
	}
	private Integer getDestinationTableSequence(String tableName) throws Exception{
		return new Integer(engine.getSequence(tableName, this.destConn));
	}
	/**
	 * 
	 * @param className
	 * @param filter
	 * @return -1 if not found
	 * @throws Exception
	 */
	private Integer findDestObjectId(String className, String filter) throws Exception{
		//List al= destSession.find("select a.Id from " + className+" a where "+ filter);
		List al= destSession.createQuery("select a.Id from " + className+" a where "+ filter).list();		
		if(al.size()>=1){
			return new Integer(Tools.getInt(al.get(0), -1));
		}
		return new Integer(-1);
		
		/*Iterator it=destSession.iterate("select max(a.Id) from " + className+" a where "+ filter);
		if(it!=null){
			Integer v=  (Integer) it.next();
			if(v!=null) return v; 
		}
		return new Integer(-1);*/
	}
	/**
	 * 
	 * @param tableId
	 * @return table id in destination database
	 * @throws Exception
	 */
	public Integer transferTable(Integer tableId)throws Exception{
		if( tableId ==null) return null;
		AdTableDAO adTableDAO=AdTableDAO.getInstance();
		AdTable tb=adTableDAO.load(tableId,srcSession);
		Integer id;
		//check existance in dest table
		
		id=findDestObjectId("AdTable", "a.Name='"+ tb.getName()+"'");
		if(id.intValue()!=-1){
			//logger.debug("Found table of name:"+ tb.getName()+", srcid="+ tableId+",dest="+ id);
			return id; // return id in dest db
		}
		outputMessage("Transfer table "+ tb.getName()+ " definition");
		
		tb.getAdAliastableSet().size();
		tb.getAdRefbytableSet().size();
		tb.getAdColumnSet().size();
		
		srcSession.evict(tb);
		tb.setId(getDestinationTableSequence("ad_table"));
		destSession.save(tb);
		//destSession.flush();
		
		// transfer category
		tb.setAdTableCategoryId(transferTableCategory( tb.getAdTableCategoryId()));
		//real table
		tb.setRealtableId(transferTable(tb.getRealtableId()));
		tb.setDirectoryId(transferDirectory(tb.getDirectoryId()));
		tb.setCreationDate( new java.util.Date());
		tb.setModifiedDate( new java.util.Date());

		AdTable tbSrc=adTableDAO.load(tableId,srcSession);		
		//alias table
		for(Iterator it=tbSrc.getAdAliastableSet().iterator();it.hasNext();){
			transferAliasTable(((AdAliastable)it.next()).getId());
		}
		//refby table
		for(Iterator it=tbSrc.getAdRefbytableSet().iterator();it.hasNext();){
			transferRefByTable(((AdRefbytable)it.next()).getId());
		}
		
		ArrayList cols=new ArrayList();
		//columns
		for(Iterator it= tbSrc.getAdColumnSet().iterator();it.hasNext();){
			AdColumn col= (AdColumn)it.next();
			cols.add(col.getId());
			//logger.debug("found column :id="+ col.getId() + ", name="+ col.getName()+" in src table "+ tb.getName());
		}
		for(int i=0;i< cols.size();i++){
			Integer srcColId= (Integer) cols.get(i); // load before column transfered.
			id= transferColumn( srcColId); // id is the new id of the specified column(col)
			if( !srcColId.equals(id) ){
				if(srcColId.equals(tb.getAkColumnId())) tb.setAkColumnId(id);
				if(srcColId.equals(tb.getDkColumnId())) tb.setDkColumnId(id);
				if(srcColId.equals(tb.getDispColumnId())) tb.setDispColumnId(id);
				if(srcColId.equals(tb.getParentColumnId())) tb.setParentColumnId(id);
				if(srcColId.equals(tb.getPkColumnId())) tb.setPkColumnId(id);
				if(srcColId.equals(tb.getSummaryColumnId())) tb.setSummaryColumnId(id);
				
			}
		}
		
		destSession.saveOrUpdate(tb);
		
		return tb.getId();
	}
	/**
	 * Warn of user if column is miss match from source to destination
	 * @param scrColumn
	 * @throws Exception
	 */
	private void warnIfColumnMissMatch(AdColumn srcColumn, Integer destColumnId ) throws Exception{
		AdColumn destColumn= AdColumnDAO.getInstance().load(destColumnId, destSession);
		StringBuffer sb=new StringBuffer();
		if(!destColumn.getColType().equalsIgnoreCase(srcColumn.getColType())) sb.append("Column "+ srcColumn.getDbName()+" coltype mismatch: src="+srcColumn.getColType()+", dest="+destColumn.getColType());
		
		if(sb.length()>1)this.outputMessage("Warn:"+sb.toString());
	}
	/**
	 * 
	 * @param columnId src column id
	 * @return
	 * @throws Exception
	 */
	private Integer transferColumn(Integer columnId) throws Exception{
		//table
		if( columnId ==null) return null;
		AdColumnDAO dao=AdColumnDAO.getInstance();
		AdColumn col= dao.load(columnId,srcSession);
		
		// make sure the table has been transferred first
		logger.debug("transfer table of :"+ col.getName()+", id="+ col.getId()+", table id="+ col.getAdTableId());
		transferTable(col.getAdTableId());
		
		Integer id=findDestObjectId("AdColumn", "a.Name='"+ col.getName()+"'");
		//check existance in dest
		if(id.intValue()!=-1){
			// 存在这样的情况: 字段同名，但类型不同，或者FK不同，系统应该提醒，这种情况发生在两个数据库的基础表已经被修改的情况，
			// 例如，本次导入的c_crossorder 的 c_bpartner，一个具有将 name 设置为varchar型, 一个设置为char型（limitvaluegroup），
			// 一个是下拉筐设计，一个是输入设计
			//logger.debug("column "+ col.getName() + "(id="+ columnId+") found in dest session, with id="+ id);
			warnIfColumnMissMatch(col, id);
			return id;
		}
		
		srcSession.evict(col);
		col.setId(getDestinationTableSequence("ad_column"));
		//table, confirm the table id is updated before insert to db
		col.setAdTableId(transferTable( col.getAdTableId()));
		destSession.save(col);
		//fk
		col.setRefColumnId(transferColumn(col.getRefColumnId()));
		// limit value group
		col.setAdLimitValueGroupId(transferLimitValueGroup(col.getAdLimitValueGroupId()));
		col.setCreationDate( new java.util.Date());
		col.setModifiedDate( new java.util.Date());
		destSession.saveOrUpdate(col);
		return col.getId();
	}
	private Integer transferRefByTable(Integer refByTableId)throws Exception{
		if(refByTableId ==null) return null;
		AdRefbytableDAO dao= AdRefbytableDAO.getInstance();
		AdRefbytable o= dao.load(refByTableId, srcSession);
		Integer id=findDestObjectId("AdRefbytable", "a.AdTableId="+
				transferTable(o.getAdTableId())+" and a.AdRefbyTableId="+ 
				transferTable(o.getAdRefbyTableId())+" and a.AdRefbyColumnId="+
				transferColumn(o.getAdRefbyColumnId()));
		//		check existance in dest
		if(id.intValue()!=-1) return id;
		

		srcSession.evict(o);
		o.setId(getDestinationTableSequence("ad_refbytable"));
		destSession.save(o);
		//destSession.flush();

		
		o.setAdTableId(transferTable(o.getAdTableId()));
		o.setAdRefbyColumnId( transferColumn(o.getAdRefbyColumnId()));
		o.setAdRefbyTableId(transferTable(o.getAdRefbyTableId()));
		o.setCreationDate( new java.util.Date());
		o.setModifiedDate( new java.util.Date());
		
		destSession.saveOrUpdate(o);
		return o.getId();
	}

	private Integer transferLimitValueGroup(Integer lmgId) throws Exception{
		if(lmgId ==null) return null;
		AdLimitValueGroupDAO dao= AdLimitValueGroupDAO.getInstance();
		AdLimitValueGroup o= dao.load(lmgId, srcSession);
		Integer id=findDestObjectId("AdLimitValueGroup", "a.Name='"+ o.getName()+"'");
		//check existance in dest
		if(id.intValue()!=-1 )return id;
		o.getAdLimitValueSet().size();
		
		srcSession.evict(o);
		o.setId(getDestinationTableSequence("AD_LIMITVALUE_GROUP"));
		o.setCreationDate( new java.util.Date());
		o.setModifiedDate( new java.util.Date());
		
		destSession.save(o);
		//destSession.flush();

		// value
		for(Iterator it= o.getAdLimitValueSet().iterator();it.hasNext();){
			transferLimitValue(o.getId(), ((AdLimitValue)it.next()).getId());
		}
		
		return o.getId();		
	}
	private Integer transferLimitValue(Integer destLimitValueGroupId, Integer lmId) throws Exception{
		if(lmId ==null) return null;
		AdLimitValueDAO dao=AdLimitValueDAO.getInstance();
		
		AdLimitValue o= dao.load(lmId, srcSession);
		srcSession.evict(o);
		o.setId(getDestinationTableSequence("AD_LIMITVALUE"));
		o.setAdLimitValueGroupId(destLimitValueGroupId);
		o.setCreationDate( new java.util.Date());
		o.setModifiedDate( new java.util.Date());
		
		destSession.save(o);
		//destSession.flush();
		
		return o.getId();		
	}
	/**
	 * Here, src directory and dest directory has the same name. The warning should be given
	 * if directoies have different table name(ad_table_id)
	 * @param src
	 * @param destId
	 * @throws Exception
	 */
	private void warnIfDirectoryMissMatch(Directory src, Integer destId) throws Exception{
		Directory dest= DirectoryDAO.getInstance().load(destId, destSession);
		StringBuffer sb=new StringBuffer();
		if(src.getAdTableId()==null || destId==null ){
			if(src.getAdTableId()!=null || destId!=null){
				this.outputMessage("Warn: directory with name '"+ src.getName()+"' mismatch between src and dest.");
			}
			return; 
		}
		// both are not null
		// check table names
		AdTableDAO dao= AdTableDAO.getInstance();
		AdTable destTable= dao.load( dest.getAdTableId(), destSession);
		AdTable srcTable = dao.load(src.getAdTableId(), srcSession);
		if(!srcTable.getName().equalsIgnoreCase(destTable.getName())){
			this.outputMessage("Warn: directory with name '"+ src.getName()+"' not designate same table. (src="+ srcTable.getName()+",dest="+ destTable.getName()+")");
		}
	}
	private Integer transferDirectory(Integer dirId)throws Exception{
		if(dirId ==null) return null;
		DirectoryDAO dao= DirectoryDAO.getInstance();
		
		Directory o= dao.load(dirId, srcSession);
		//logger.debug("src dir="+ dirId+ ", name="+o.getName());
		//check existance in dest
		Integer id=findDestObjectId("Directory", "a.Name='"+ o.getName()+"'");
		//logger.debug("dst dir="+ id);

		//check existance in dest
		if(id.intValue()!=-1 ){
			warnIfDirectoryMissMatch(o,id);
			return id;
		}
		srcSession.evict(o);
		o.setId(getDestinationTableSequence("DIRECTORY"));
		o.setAdTableCategoryId(transferTableCategory(o.getAdTableCategoryId()));
		o.setAdTableId(transferTable(o.getAdTableId()));
		o.setCreationDate( new java.util.Date());
		o.setModifiedDate( new java.util.Date());
		/*org.apache.log4j.Logger lg= org.apache.log4j.Logger.getLogger("org.hibernate");
		Level lv= lg.getLevel();
		lg.setLevel(Level.DEBUG);*/
		destSession.save(o);
		//destSession.flush();
		//lg.setLevel(lv);
		return o.getId();		
		
	}
	private Integer transferTableCategory(Integer categoryId )throws Exception{
		if(categoryId ==null) return null;
		AdTableCategoryDAO dao= AdTableCategoryDAO.getInstance();
		
		//check existance in dest
		AdTableCategory o= dao.load(categoryId, srcSession);
		Integer id=findDestObjectId("AdTableCategory", "a.Name='"+ o.getName()+"'");
		
		//check existance in dest
		if(id.intValue()!=-1 ){
			//logger.debug("Found tablecategory of name:"+ o.getName()+", srcid="+ categoryId+",dest="+ id);
			return id;
		}
		srcSession.evict(o);
		o.setId(getDestinationTableSequence("AD_TABLECATEGORY"));
		o.setCreationDate( new java.util.Date());
		o.setModifiedDate( new java.util.Date());
		o.setAdTableSet(null);
		
		destSession.save(o);
		// so the destination session will no longer refer to the tables
		//destSession.evict(o);
		//destSession.flush();
		return o.getId();		
		
	}
	private Integer transferSubSystem(Integer systemId )throws Exception{
		if(systemId ==null) return null;
		AdSubSystemDAO dao= AdSubSystemDAO.getInstance();
		
		//check existance in dest
		AdSubSystem o= dao.load(systemId, srcSession);
		Integer id=findDestObjectId("AdSubSystem", "a.Name='"+ o.getName()+"'");
		
		//check existance in dest
		if(id.intValue()!=-1 ){
			//logger.debug("Found tablecategory of name:"+ o.getName()+", srcid="+ categoryId+",dest="+ id);
			return id;
		}
		srcSession.evict(o);
		o.setId(getDestinationTableSequence("AD_SUBSYSTEM"));
		o.setCreationDate( new java.util.Date());
		o.setModifiedDate( new java.util.Date());
		
		destSession.save(o);
		// so the destination session will no longer refer to the tables
		//destSession.evict(o);
		//destSession.flush();
		return o.getId();		
		
	}	
	private Integer transferAliasTable(Integer aliasTableId)throws Exception{
		if(aliasTableId ==null) return null;
		AdAliastableDAO dao= AdAliastableDAO.getInstance();
		
		//check existance in dest
		AdAliastable o= dao.load(aliasTableId, srcSession);
		Integer id=findDestObjectId("AdAliastable", "a.Name='"+ o.getName()+"'");
		//check existance in dest
		if(id.intValue()!=-1 ){
			return id;
		}
		srcSession.evict(o);
		o.setId(getDestinationTableSequence("AD_ALIASTABLE"));
		
		destSession.save(o);
		//destSession.flush();
		// real table
		o.setRealtableId(transferTable(o.getRealtableId()));
		o.setCreationDate( new java.util.Date());
		o.setModifiedDate( new java.util.Date());
		 
		destSession.saveOrUpdate(o);
		return o.getId();		
	}
	public static String test() throws Exception{
		URL url=nds.control.web.ServletContextManager.class.getResource("/hibernate.hsql.cfg.xml");
		Writer writer=new java.io.StringWriter();
		Connection con= DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521:test" ,"nds3","abc123");
		writer.write("connection autocommit="+con.getAutoCommit()+ StringUtils.LINE_SEPARATOR);
		con.setAutoCommit(false);
		String hsql= "/act/upload/a";
		SchemaImport2 si=new SchemaImport2();
		try{
		si.init(url,hsql , writer, con);
		si.transferTable("groups");
		si.commit();
		}catch(Throwable ex){
			logger.error(ex.toString(), ex);
			writer.write(StringUtils.toString(ex)+  StringUtils.LINE_SEPARATOR);
			try{si.rollback();}catch(Throwable e){
				logger.error("Could not rollback dest session:"+ e);
			}
			/*try{
				con.rollback();
			}catch(Throwable e){
				logger.error("Could not rollback connection:"+ e);
			}*/
		}finally{
			try{si.destroy();}catch(Throwable e){
				logger.error("Error destroy:" ,e);
			}
			try{con.close();}catch(Throwable e){
				logger.error("Error close connection:" +e);
			}
		
		}
		return writer.toString();
		
	}
	
}
