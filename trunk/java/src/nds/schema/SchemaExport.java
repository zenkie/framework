/*
 * Agile Control Technologies Ltd,. CO.
 * http://www.agileControl.com
 */
package nds.schema;

import java.util.Hashtable;
import java.util.Iterator;
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
import java.io.*;
import java.net.URL;
 
/**
 * Will open a session connect to hsql,transfer tables from oracle to hsql db, 
 * so TableImport can import from hsql to oracle.
 * 
 * Sample usage:
 * 
 * Schematransfer exp=new Schematransfer();
 * try{
 * 		exp.init(url);
 * 		exp.clearupDestDB();
 * 		exp.transferTable(aTable);
 * 		exp.commit();
 * 		exp.saveToFile(file);
 * }catch(Exception e){
 * 		try{exp.rollback();}catch(Exception e2){}
 * }finally{
 * 		try{exp.destroy();}catch(Exception e3){}
 * }
 * 
 * 
 * @author yfzhu@agilecontrol.com
 */

public class SchemaExport {
	private static  Logger logger= LoggerManager.getInstance().getLogger(SchemaExport.class.getName());
	
	private Session destSession, srcSession;
	private SessionFactory sf ; // dest Session factory;
	private Transaction trans; // for dest session
	private java.sql.Connection destConn;
	private long beginTime;
	/**
	 * If false, will not include relate tables
	 */
	private boolean includeRelateTable;
	public void rollback() throws Exception{
		
		trans.rollback();
		logger.debug("Transaction rolled back:"+ trans.hashCode());
		/*ArrayList al =new ArrayList();
		al.add("checkpoint");
		QueryEngine.getInstance().doUpdate(al, destConn);
		*/
	}
	public void commit()throws Exception{
		trans.commit();
		logger.debug("Transaction commited:"+ trans.hashCode());
		/*ArrayList al =new ArrayList();
		al.add("checkpoint");
		QueryEngine.getInstance().doUpdate(al, destConn);
		*/
	}
	public void saveToFile(String file) throws Exception{
		ArrayList al =new ArrayList();
		al.add("script '"+ file+"'");
		QueryEngine.getInstance().doUpdate(al, destConn);
	}
	/**
	 * Clear destination db
	 * @throws Exception
	 */
	private void cleanupDestDB()throws Exception{
		ArrayList al =new ArrayList();
		al.add("delete from ad_table_transfer"); // contains tables to be transferred 
		al.add("delete from ad_limitvalue");
		al.add("delete from ad_limitvalue_group");
		al.add("delete from ad_column");
		al.add("delete from directory");
		al.add("delete from ad_aliastable");
		al.add("delete from AD_REFBYTABLE");
		al.add("delete from ad_table");
		al.add("delete from ad_tablecategory");
		al.add("delete from ad_subsystem");
		QueryEngine.getInstance().doUpdate(al, destConn);
	}
	/**
	 * Destroy hsql session, if transaction not committed, committed here
	 * 
	 */
	public void destroy() throws Exception {
			try{
				if(destSession!=null && destSession.isOpen()) {
					destSession.close();
				}
			}catch(Exception e){}
			if(destConn!=null && destConn.isClosed()==false){
				try{destConn.close();}catch(Exception e){}
			}
			try{
				if(srcSession!=null && srcSession.isOpen()) {
					AdTableDAO.getInstance().closeSession();
				}
			}catch(Exception e){}
			sf.close();
	}
	/**
	 * Init hsql db configurations
	 * @param hsqlConfigXMLURL  
	 * @param saveToFile file to be exported to
	 * @param includeRelateTable if false, will only include table of fk with one level, 
	 * 	else, will incldue fk table with unlimited level
	 *  需要关联至少一层的原因是: 当前表的FK字段至少需要知道对应的表的名称
	 */
	public void init(java.net.URL hsqlConfigXMLURL, boolean includeRelateTable) throws Exception{
		beginTime= System.currentTimeMillis();
		srcSession= AdTableDAO.getInstance().createSession();
		
		sf= new Configuration().configure(hsqlConfigXMLURL).buildSessionFactory();
		
		logger.debug("Session factory loaded according to "+hsqlConfigXMLURL);
		destSession=sf.openSession();
		trans=destSession.beginTransaction();
		destConn= destSession.connection();
		logger.debug("Transaction begin:"+ trans.hashCode());
		this.cleanupDestDB();
		this.includeRelateTable=includeRelateTable;
	}
	
	private  boolean isObjectExistsInDestDB(String objectClass, Integer id) throws Exception{
		boolean b=false;
		try{
			b=(destSession.get("nds.model."+objectClass, id)!=null);
		}catch(org.hibernate.ObjectNotFoundException o){}
		//boolean b=Tools.getInt(destSession.createSQLQuery("select count(*) from "+ objectClass +" a where a.Id="+ id).list().get(0),0)>0; 
		//logger.debug("isObjectExistsInDestDB("+objectClass+","+id+")="+ b);
		//return ( Tools.getInt( destSession.find("select count(a) from "+ objectClass +" a where a.Id="+ id).get(0), 0)>0);
		return b;
	}
	/**
	 * 
	 * @throws Exception
	 */
	private void replicate(Object obj) throws Exception{
		srcSession.evict(obj);
		destSession.replicate(obj, ReplicationMode.IGNORE);
	}
	public void transferTable(String tableName )throws Exception{
		TableManager manager= TableManager.getInstance();
		Table tb=manager.getTable(tableName);
		if(tb==null) throw new NDSException("Could not found table for "+ tableName);
		transferTable(new Integer(tb.getId()), 0);
		// record information to ad_table_transfer
		AdTable tb2=AdTableDAO.getInstance().load(new Integer(tb.getId()),srcSession);
		recordTable(tb.getId(), tb.getName(), tb.getDescription(manager.getDefaultLocale()),  tb2.getComments());
	}
	/**
	 * 
	 * @param tableId
	 * @param isRecord if true, will let user select whether to import or not. 
	 * 	This table information will be stored to table "ad_table_transfer" in migration db
	 * @param level 0 to unlimited, 0 means the table specified by caller, 1 for fk table of main table
	 *  2 for fk table of level 1 table, and so on.
	 * Only level 0 table will be stored to table "ad_table_transfer" in migration db
	 * @throws Exception
	 * @return table id in dest db, or null if table has not been transfered.
	 */
	private Integer transferTable(Integer tableId,  int level)throws Exception{
		if( tableId ==null) return null;
		// won't include relate table if not record
		if( !this.includeRelateTable && level > 1){
			return null;
		}
		AdTableDAO adTableDAO=AdTableDAO.getInstance();
		//check existance in dest table
		if(isObjectExistsInDestDB("AdTable", tableId)) return tableId;
		
		//logger.debug("transfer table "+ tableId+", level="+level);
		AdTable tb=adTableDAO.load(tableId,srcSession);
		//tb.getAdAliastableSet().size(); // force load set to memory
		tb.getAdRefbytableSet().size();
		tb.getAdColumnSet().size();
		
		replicate(tb);
		// transfer category
		transferTableCategory( tb.getAdTableCategoryId());
		//alias table
		/*for(Iterator it=tb.getAdAliastableSet().iterator();it.hasNext();){
			transferAliasTable(((AdAliastable)it.next()).getId(), level);
		}*/
		//refby table
		for(Iterator it=tb.getAdRefbytableSet().iterator();it.hasNext();){
			transferRefByTable(((AdRefbytable)it.next()).getId(), level);
		}
		//columns
		for(Iterator it= tb.getAdColumnSet().iterator();it.hasNext();){
			transferColumn( ((AdColumn)it.next()).getId(), level);
		}
		//real table
		transferTable(tb.getRealtableId(), level+1);
		transferDirectory(tb.getDirectoryId(), level);
		//logger.debug("Table definition "+ tb.getName() + " transfered.");
		return tableId;
	}
	private void recordTable(int tableId, String tableName, String tableDescription, String comments) throws Exception{
		PreparedStatement pstmt=null;
		try{
			pstmt=destConn.prepareStatement("insert into ad_table_transfer(id,  name, description,comments,ad_table_id) values (?,?,?,?,?)");
			pstmt.setInt(1, tableId);
			pstmt.setString(2, tableName);
			pstmt.setString(3, tableDescription);
			pstmt.setString(4, comments);
			pstmt.setInt(5, tableId);
			pstmt.executeUpdate();
		}finally{
			try{if(pstmt!=null)pstmt.close();}catch(Exception e){}
		}
	}
	/**
	 * 
	 * @param columnId
	 * @param level level of the column's table to be imported
	 * @throws Exception
	 */
	private void transferColumn(Integer columnId, int level) throws Exception{
		//table
		if( columnId ==null) return;
		AdColumnDAO dao=AdColumnDAO.getInstance();
		//check existance in dest
		if(isObjectExistsInDestDB("AdColumn", columnId)||
				(level>1 && ! this.includeRelateTable)){
			//在以下情况下，字段所在的表因为级数过高，而被禁止传输：
			//字段（level=2）被主表(level=0)对应的关联表(level=1)引用，字段被成功装入，但对应的表未被装入，从而造成
			//部分数据无法恢复
			//这里需要进行表的存在性的判断，如果table未被装载，则字段也不被装载，或者说，要装载字段，必先装载字段所在表
			return;
		}
		AdColumn col=dao.load(columnId,srcSession);
		replicate(col);
		//table
		transferTable( col.getAdTableId(), level);
		//fk
		transferColumn(col.getRefColumnId(), level+1);
		// limit value group
		transferLimitValueGroup(col.getAdLimitValueGroupId());
		
		
	}
	private void transferRefByTable(Integer refByTableId, int level)throws Exception{
		if(refByTableId ==null) return;
		AdRefbytableDAO dao= AdRefbytableDAO.getInstance();
		
		//check existance in dest
		if(isObjectExistsInDestDB("AdRefbytable", refByTableId)) return;
		AdRefbytable rf= dao.load(refByTableId, srcSession);
		replicate(rf);
		
		transferTable(rf.getAdRefbyTableId(),  level+1);
		transferColumn(rf.getAdRefbyColumnId(), level+1);
		
	}

	private void transferLimitValueGroup(Integer lmgId) throws Exception{
		if(lmgId ==null) return;
		AdLimitValueGroupDAO dao= AdLimitValueGroupDAO.getInstance();
		
		//check existance in dest
		if(isObjectExistsInDestDB("AdLimitValueGroup", lmgId)) return;
		AdLimitValueGroup o= dao.load(lmgId, srcSession);
		o.getAdLimitValueSet().size();
		replicate(o);
		
		// value
		for(Iterator it= o.getAdLimitValueSet().iterator();it.hasNext();){
			transferLimitValue( ((AdLimitValue)it.next()).getId());
		}
		
	}
	private void transferLimitValue(Integer lmId) throws Exception{
		if(lmId ==null) return;
		AdLimitValueDAO dao=AdLimitValueDAO.getInstance();
		
		//check existance in dest
		if(isObjectExistsInDestDB("AdLimitValue", lmId)) return;
		AdLimitValue o= dao.load(lmId, srcSession);
		replicate(o);	
	}
	private void transferDirectory(Integer dirId, int level)throws Exception{
		if(dirId ==null) return;
		DirectoryDAO dao= DirectoryDAO.getInstance();
		
		//check existance in dest
		if(isObjectExistsInDestDB("Directory", dirId)) return;
		Directory o= dao.load(dirId, srcSession);
		replicate(o);
		
		transferTableCategory(o.getAdTableCategoryId());
		transferTable(o.getAdTableId(), level+1);
		
	}
	private void transferTableCategory(Integer categoryId )throws Exception{
		if(categoryId ==null) return;
		AdTableCategoryDAO dao= AdTableCategoryDAO.getInstance();
		
		//check existance in dest
		if(isObjectExistsInDestDB("AdTableCategory", categoryId)) return;
		AdTableCategory o= dao.load(categoryId, srcSession);
		replicate(o);
		transferSubSystem(o.getAdSubSystemId());
	}
	private void transferSubSystem(Integer systemId )throws Exception{
		if(systemId ==null) return;
		AdSubSystemDAO dao= AdSubSystemDAO.getInstance();
		
		//check existance in dest
		if(isObjectExistsInDestDB("AdSubSystem", systemId)) return;
		AdSubSystem o= dao.load(systemId, srcSession);
		replicate(o);
		
	}
	private void transferAliasTable(Integer aliasTableId, int level)throws Exception{
		if(aliasTableId ==null) return;
		AdAliastableDAO dao= AdAliastableDAO.getInstance();
		
		//check existance in dest
		if(isObjectExistsInDestDB("AdAliastable", aliasTableId)) return;
		AdAliastable o= dao.load(aliasTableId, srcSession);
		replicate(o);
		// real table
		transferTable(o.getRealtableId(), level+1);
	}
	
	public static void test() throws Exception{
		URL url=nds.control.web.ServletContextManager.class.getResource("/hibernate.hsql.cfg.xml");
		String saveFile="/a.script";
		SchemaExport e=new nds.schema.SchemaExport();
		try{
		e.init(url, true);
		e.transferTable("groups");
		e.commit();
		  java.io.File file=new File(saveFile);
		  if(file.exists()) file.delete();
		  e.saveToFile(saveFile);
		  logger.debug("Saved file to "+ saveFile);
		}catch(Exception ex){
			logger.error("Fail", ex);
			try{ e.rollback();}catch(Exception ee){
				logger.error("Could not rollback:"+ee);
			}
		}finally{
			try{
				e.destroy();
			}catch(Exception ee){
				logger.error("Could not destroy:"+ee);
			}
		}
	}
}
