package nds.control.ejb.command;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Vector;

import org.directwebremoting.WebContext;
import org.json.*;

import nds.control.check.ColumnCheckImpl;
import nds.control.ejb.Command;
import nds.control.event.DefaultWebEvent;
import nds.control.event.NDSEventException;
import nds.control.util.SecurityUtils;
import nds.control.util.ValueHolder;
import nds.control.web.UserWebImpl;
import nds.control.web.WebUtils;
import nds.process.ProcessUtils;
import nds.query.*;
import nds.schema.*;
import nds.util.*;

import java.util.*;

import nds.security.Directory;
import nds.security.User;
import java.text.*;
import org.json.*;
import com.Ostermiller.util.CGIParser;
import com.Ostermiller.util.NameValuePair;;

/**
 * Create crosstab runner process instance, including following parameters:
 * 	select filter (where clause)
 *  ad_cxtab template
 *  
 *  Use portal.properties#cxtab.concurrent.max to avoid too many immediate running cxtab jobs 
 * 
 * @author yfzhu@agilecontrol.com 
 */
public class ExecuteCxtab extends Command {
	/**
	 * Record current running job count
	 */
	private static int runningJobCount=0;
	/**
	 * Last time runningJobCount is equal to max threshold
	 */
	private static long lastMaxJobCountReachedTime=-1;
	public static int getRunningJobCount(){
		return runningJobCount;
	}
	public static void resetRunningJobCount(){
		lastMaxJobCountReachedTime=-1;
		runningJobCount=0;
	}
	 /** 
	  * @param event contains JSONObject that has following properties:
	 * 		"query*" - JSONObject.toString() which should be parsed
	 * 		"cxtab*"		- name of ad_cxtab (AK,String) or id (PK, Integer)
	 *		"filetype"  - "xls" or "html", by default, html
	 * 		"isrest" - if true, will take query as REST interface format 
	 *		--- some parameters prefixed with "preps_" will set to pre-process of ad_cxtab
	 *
	 * @return  
	 * 		Create instance of nds.process.CxtabRunner first, then
	 * 		if by immediately, execute that pi and create that report as html and save to user report folder
	 *      if by schedule, return infor.jsp
	 */
  public ValueHolder execute(DefaultWebEvent event) throws RemoteException, NDSException {
	Connection conn=null;
  	PreparedStatement pstmt=null;
  	
  	QueryEngine engine= QueryEngine.getInstance();
    
	conn= engine.getConnection();
	try{
		User user=helper.getOperator(event);
		int userId=user.getId().intValue();
		JSONObject jo= event.getJSONObject();
		
	  	//default file type to html
	  	String fileType= jo.getString("filetype");
	  	//if(!"xls".equals(fileType)) fileType="htm";
	  	boolean isRest= jo.optBoolean("isrest", false);
	  	
	  	List list;
	  	String cxtabName= jo.getString("cxtab");
	  	int cxtabId =Tools.getInt(cxtabName, -1);
	  	if(cxtabId==-1){
	  		list=engine.doQueryList("select c.ad_processqueue_id, q.name , c.isbackground,c.name,c.pre_procedure,c.AD_PI_COLUMN_ID, ad_table_id from ad_cxtab c, ad_processqueue q where q.id= c.ad_processqueue_id and c.name="+ QueryUtils.TO_STRING(cxtabName),conn);
	  		cxtabId=Tools.getInt( engine.doQueryOne("select id from ad_cxtab where ad_client_id="+ user.adClientId +" and c.name="+ QueryUtils.TO_STRING(cxtabName),conn), -1);
	  	}else{
	  		list=engine.doQueryList("select c.ad_processqueue_id, q.name , c.isbackground,c.name,c.pre_procedure,c.AD_PI_COLUMN_ID, ad_table_id from ad_cxtab c, ad_processqueue q where q.id= c.ad_processqueue_id and c.id="+ cxtabName,conn);
	  	}
	  	if(list.size()==0) throw new NDSException("@parameter-error@:(cxtab="+ cxtabName+")" );
	  	
	  	// save user cxtab preference on the root cxtab, so next time load, will show directly
	  	String cxtabRootId=String.valueOf( engine.doQueryOne("select nvl(parent_id, id) from ad_cxtab where id="+ cxtabId, conn));
	  	Properties props=new Properties();
	  	props.setProperty( cxtabRootId,String.valueOf( cxtabId));
	  	SavePreference.setPreferenceValues(user.id.intValue(), "cxtab"+cxtabRootId, props);
		//clear user web cache 
	  	// no need, we do not cache this one
		/*WebContext wc=(WebContext) jo.opt("org.directwebremoting.WebContext");
		if(wc!=null){
			UserWebImpl userWeb= ((UserWebImpl)WebUtils.getSessionContextManager(wc.getSession()).getActor(nds.util.WebKeys.USER));
			if(userWeb!=null)userWeb.invalidatePreference("cxtab"+cxtabRootId, cxtabRootId);
		}*/
		
		
		
	  	int queueId= Tools.getInt( ((List)list.get(0)).get(0),-1);
	  	String queueName=(String) ((List)list.get(0)).get(1);
	  	boolean isBg= Tools.getYesNo(((List)list.get(0)).get(2),true);
	  	
	  	// At most portal.properties#cxtab.concurrent.max pinstance can run, -1 means no limitation
	  	int maxJobCount=-1;
  		if(!isBg){
		  	Configurations conf=(Configurations)nds.control.web.WebUtils.getServletContextManager().getActor(nds.util.WebKeys.CONFIGURATIONS);
	  		maxJobCount=Tools.getInt( conf.getProperty("cxtab.concurrent.max"),-1);
	  		if(maxJobCount >0 && runningJobCount>=maxJobCount){
	  			throw new NDSException( MessagesHolder.getInstance().
	  					translateMessage("@cxtab-too-many-jobs-running@",event.getLocale()).replaceAll("0",
	  							((SimpleDateFormat)QueryUtils.smallTimeFormatter.get()).format(
	  									new java.util.Date(lastMaxJobCountReachedTime))
	  							));
	  		}
  		}
	  	cxtabName=(String) ((List)list.get(0)).get(3);
	  	
	  	String preProcedure=(String) ((List)list.get(0)).get(4);
	  	int piColumnId=Tools.getInt( ((List)list.get(0)).get(5), -1);//AD_PI_COLUMN_ID

		int tableId= Tools.getInt( ((List)list.get(0)).get(6), -1);//AD_TABLE_ID, ALWAYS NOT NULL
		Table table=TableManager.getInstance().getTable(tableId);
		String dir= table.getSecurityDirectory();
		event.setParameter("directory",  dir);	
	  	helper.checkDirectoryReadPermission(event, user);
		
	  	
	  	// cxtab name must be name, not pk in process
		JSONObject query=new JSONObject( jo.getString("query"));
		
		/**
		 * 两种情况：
		 * 1）通过cxtab.ad_table_id的索引字段构造的查询（针对普通交接表）
		 * 2）通过cxtab_jpara 的定义构造的查询（针对预运算存储过程）
		 * 
		 * 对于第1种情况，从界面输入来构造查询条件
		 * 第2种情况，最终的交叉表查询条件类似于 select xxx from dd where ad_pi_id=143, 即结果集的where语句
		 * 就是 ad_pi_id存放的内容。第2种情况的出现条件是pre_procedure is not null
		 * 
		 * AD_CXTAB.AD_PI_COLUMN_ID number(10)，ad_column.id 必须关联到事实表，这个字段用于指明哪个字段的值将被识别为
		 * 本次报表的数据。这个字段将存储P_PI_ID的值。 建议就设置为 ad_pi_id，无需关联到AD_PINSTANCE表。
		 * 如果这个字段不进行设置，在查询事实表时，将获取全部数据。
		 * 
		 */
		QueryRequestImpl req=null;
		int piId=-1;

		int pid=Tools.getInt(engine.doQueryOne("select id from ad_process where classname='nds.process.CxtabRunner' and isactive='Y'",conn),-1);
		List params= engine.doQueryList("select name, valuetype,nullable,orderno from ad_process_para where ad_process_id="+pid+" order by orderno asc", conn);
		HashMap map=new HashMap();
		
		String filterDesc=null;
		if(Validator.isNotNull(preProcedure)){
			
			piId = engine.getSequence("ad_pinstance");
			
			//create filter for cxtab data
			Expression expr=null;
			if (piColumnId !=-1) expr=new Expression(new ColumnLink( new int[]{piColumnId}),"="+ piId,null);
			else logger.debug("find ad_pi_column_id=-1 for cxtab id="+ cxtabId);
			req=constructQuery(table,event.getQuerySession(), userId, event.getLocale(),expr);
			
			// 将ad_cxtab_jpara里的参数定义构造到 params 里，并将界面上传入的参数值放置到map 里
			String cs=query.optString("param_str");
			if ( Validator.isNotNull(cs)){
				CGIParser parser= new CGIParser(cs,"UTF-8");
		    	java.util.Enumeration e=parser.getParameterNames();
		    	HashMap csMap=new HashMap();
		    	while(e.hasMoreElements()){
		    		String key= (String)e.nextElement();
		    		csMap.put(key, parser.getParameter(key));
		    	}

		    	//logger.debug( Tools.toString(csMap));
		    	
		    	filterDesc=addJparams(params, map, cxtabId,csMap,event.getQuerySession(), userId,event.getLocale(), conn);
			}
		}else{
			if(isRest){
				logger.debug("parse as rest query");
				req=nds.control.util.AjaxUtils.parseRestQuery(query, event.getQuerySession(), userId, event.getLocale());
			}else
				req=nds.control.util.AjaxUtils.parseQuery(query, event.getQuerySession(), userId, event.getLocale());
		}
	
		
		 /* 	"filter" - description of the current filter
		 * 		"filter_expr" - nds.query.Expression 
		 * 		"filter_sql"  - string like 'in (13,33)'
		 * 		"cxtab*"		- name of ad_cxtab (AK)
		 * 		"chk_run_now*"	- run immediate or by schedule
		 *		
		 *		--- some parameters prefixed with "preps_" will set to pre-process of ad_cxtab
		 *
		 * 		"queue"  - process queue
		 * 		"recordno"		- process instance record no
		 * 		"filename"		- file name to save the report (xls)	
		 * 		"filetype"		- type of file, "xls"|"html"|"cub"
		 */
			
		map.put("CXTAB", cxtabName);
		map.put("FILTER", ((filterDesc==null)? req.getParamDesc(true): filterDesc));
		if(req.getParamExpression()!=null)map.put("FILTER_EXPR",req.getParamExpression().toString());
		
		
		SimpleDateFormat sdf = new SimpleDateFormat("MMddHHmm");
		//String filename="CXR_"+table.getName()+sdf.format(new Date());
		String filename="CXR_"+cxtabId+"_"+sdf.format(new Date());
		
		map.put("FILENAME", filename);
		//map.put("FILETYPE", isBg?"xls":"htm");
		map.put("FILETYPE", fileType);
		
	    piId=ProcessUtils.createAdProcessInstance(piId,pid,queueName, "",user,params,map,conn);
	    ValueHolder hd=null;
	    JSONObject returnObj=new JSONObject();
	    if(isBg){
	    	hd=new ValueHolder();
	    	hd.put("code","0");
	    	returnObj.put("message", MessagesHolder.getInstance().translateMessage("@cxtab-task-generated@",event.getLocale()));
	  	}else{
	  		
	  		//run now
	  		try{
	  			
		  		runningJobCount++;
		  		if(maxJobCount>-1 && maxJobCount==runningJobCount){
		  			lastMaxJobCountReachedTime=System.currentTimeMillis();//record, so when time is too long, admin should get in to check 
		  		}
	  			hd=ProcessUtils.executeImmediateADProcessInstance(piId , userId, false);
	  			
	  		}finally{
	  			runningJobCount--;
	  			lastMaxJobCountReachedTime=-1;//reset
	  		}
	  		
	  		//and tell client to fetch file from 
	  		//returnObj.put("url", "/servlets/binserv/GetFile?show=Y&filename="+ URLEncoder.encode(filename+"."+ fileType,"UTF-8"));
	  		
	  		/**
	  		 * For htm type, will direct to print page, for xls, direct download
	  		 */
	  		String url;
	  		//url = "/html/nds/cxtab/viewrpt.jsp?file="+URLEncoder.encode(filename+"."+ fileType,"UTF-8");
	  		if( "htm".equals(fileType) ){
	  			url = "/html/nds/cxtab/viewrpt.jsp?file="+URLEncoder.encode(filename+"."+ fileType,"UTF-8");
	  		}else if("xls".equals(fileType) || "csv".equals(fileType)){
	  			url= "/html/nds/cxtab/downloadrpt.jsp?file="+URLEncoder.encode(filename+"."+ fileType,"UTF-8");
	  			//"/servlets/binserv/Download/"+ URLEncoder.encode(filename+"."+ fileType,"UTF-8");
	  		}else{
	  			// cube
	  			url = "/html/nds/cxtab/viewcube.jsp?pi="+piId  +"&file="+URLEncoder.encode(filename+"."+ fileType,"UTF-8");
	  		}
	  		returnObj.put("url", url);
	  	}	
	    hd.put("data",returnObj );
		
		return hd;
	 }catch(Throwable e){
	 	logger.error("", e);
	 	if(!(e instanceof NDSException ))throw new NDSEventException("异常", e);
	 	else throw (NDSException)e;
    }finally{
    	if(conn!=null){
    		try{conn.close();}catch(Throwable t){}
    	}
    }	  	  
  }
  
  /**
   * Shared with nds.control.ejb.command.ExecuteJReport
   * 
   * Add jpara to params list and data map, if jpara name conflicts with nds.process.CxtabRunner,
   * it's value will be replaced. SO NEVER GIVE JPARA THE RESERVED NAMES 
   * @param params elements like "select name, valuetype,nullable,orderno from ad_process_para" 
   * @param map 
   * @param cxtabId
   * @param jo from event
   * @param conn
   * @return filter description for all parameters
   * @throws Exception
   */
  static String addJparams(List params, HashMap map, int cxtabId, HashMap jo,QuerySession qession,int userId, Locale locale,Connection conn) throws Exception{
	  	StringBuffer descs=new StringBuffer();
	  	// always set param value in ad_pinstance_para.INFO_TO, so set ad_process_para.VALUETYPE is set to 'B' (Blob Data)
	  	// first 4 selection must be in accordance with 
	  	// select name, valuetype,nullable,orderno from ad_process_para where ad_process_id="+pid+" order by orderno asc
	  	List jparas= QueryEngine.getInstance().doQueryList("select name, 'B',nullable,orderno+1000, ad_column_id,SELECTIONTYPE, PARATYPE, description from ad_cxtab_jpara where isactive='Y' and ad_cxtab_id="+ cxtabId+" order by orderno asc",conn);
	  	params.addAll(jparas);
	  	TableManager manager= TableManager.getInstance();
        String desc;
	    for(int i=0;i<jparas.size();i++){
          List para= (List) jparas.get(i);
		  Column column=manager.getColumn( Tools.getInt( para.get(4),-1));
		  nds.util.PairTable values =null;
          String inputName=(String)para.get(0);
          String type=(String)para.get(6);//paratype
          int colType;
          if("n".equalsIgnoreCase(type)) colType=Column.NUMBER;
          else if("d".equalsIgnoreCase(type)) colType= Column.DATENUMBER;
          else if("s".equalsIgnoreCase(type) ) colType= Column.STRING;
          else throw new NDSException("Illegal type for ad_cxtab_jpara.type:"+ type);
          boolean isMultipleSelection=   "M".equalsIgnoreCase( (String)para.get(5));
          boolean isNullable="Y".equalsIgnoreCase( (String)para.get(2));
          String description= (String)para.get(7);
          String value= (String)jo.get(inputName);
          if(!isNullable && nds.util.Validator.isNull(value)){
        	  throw new NDSException("@pls-input@'"+description+"'");
          }
          String storeValue="";
          if(column!=null && column.getReferenceTable()!=null){ 
        	  if(isMultipleSelection){
        		  // multiple selection
	        	  String sql= (String)jo.get(inputName+"/sql");
	        	  if(nds.util.Validator.isNotNull(sql)){
	        		  storeValue=constructSQLByIDSQL(column, sql, qession,userId,locale);
	        		  
	        	  }else{
	        		  storeValue=constructSQLByAKInput(column, value, qession,userId,locale);
	        	  }
        	  }else{
        		  // single selection
        		  if(nds.util.Validator.isNotNull( value)){
        			  String sql=constructSQLByAKInput(column, value, qession,userId,locale);
        			  int id= Tools.getInt(QueryEngine.getInstance().doQueryOne(sql,conn) , -1);
        			  if(id==-1){
        				  throw new NDSException(value+" ("+
        						  column.getReferenceTable().getDescription(locale)+")@not-exists-or-invalid@");
        			  }
        			  storeValue=String.valueOf(id);
        		  }// else will set storeValue to empty
        	  }
		  }else{
			  if(nds.util.Validator.isNotNull(value))
				  storeValue=QueryUtils.toSQLClause(inputName,value, colType);
//			 else will set storeValue to empty
		  }
//          logger.debug(inputName+":="+ storeValue);
          
          map.put(inputName,storeValue );
          if(i>0) 
        	  descs.append(" "+MessagesHolder.getInstance().getMessage(locale,"sql-and")+" " );
          descs.append(" (").append(description ).append(" ").append(value).append(")");
	    }
	    return StringUtils.shortenInBytes(descs.toString(), 4000); // db can only store 4000 bytes
  }
  /**
   * 
   * @param col ad_cxtab_jpara.ad_column_id, which must be FK type
   * @param idSQL id sql for FK table selection
   * @param qsession 
   * @param userId
   * @param locale
   * @return sql in format like "select id from fk_table where xxxx"
   * @throws Exception
   */
  static String constructSQLByIDSQL(Column col, String idSQL,QuerySession qsession, int userId, Locale locale )throws Exception{
	  Table table= col.getReferenceTable();
	  Expression expr=new Expression(new ColumnLink(new int[]{table.getPrimaryKey().getId()}), idSQL,null);
      if( col.getFilter()!=null){
    	  if(!col.isFilteredByWildcard()){
      		  Expression expr1= new Expression(null, col.getFilter(),null);	
      		  expr= expr1.combine(expr,SQLCombination.SQL_AND,null);
    	  }else{
    		  throw new NDSException("wildcard filter is not supported in cxtab jpara definition");
    	  }
      }      

	  QueryRequestImpl query= constructQuery(table, qsession, userId, locale, expr);
	  
	  return query.toPKIDSQL(true);
	  
  }
  /**
   * 
   * @param col ad_cxtab_jpara.ad_column_id, which must be FK type
   * @param akData input from ui
   * @param qsession 
   * @param userId
   * @param locale
   * @return sql in format like "select id from fk_table where xxxx"
   * @throws Exception
   */
  static String constructSQLByAKInput(Column col, String akData,QuerySession qsession, int userId, Locale locale )throws Exception{
	  TableManager tm=TableManager.getInstance();
	  QueryEngine engine=QueryEngine.getInstance();
	  
	  Table refTable= col.getReferenceTable();
	  QueryRequestImpl query= constructQuery(refTable, qsession, userId, locale, null);
	  
	  String refTableName= refTable.getName();
	  
      query.addParam(refTable.getAlternateKey().getId(), akData);
      

      if( col.getFilter()!=null){
    	  if(!col.isFilteredByWildcard()){
      		  Expression expr= new Expression(null, col.getFilter(),null);	
    		  query.addParam(expr);
    	  }else{
    		  throw new NDSException("wildcard filter is not supported in cxtab jpara definition");
    	  }
      }      
      
	  return query.toPKIDSQL(true);
	  
  }
  /**
   * Create a query object for table, current user will have read permission checked, 
   * the query will in format like "select id from table where xxxx"
   * @param table
   * @param qsession
   * @param userId
   * @param locale
   * @param expr will be added to where clause in addtion to users read permission on that table
   * @return
   * @throws Exception
   */
  static QueryRequestImpl constructQuery(Table table, QuerySession qsession, int userId, Locale locale,Expression expr) throws Exception{
		QueryEngine engine =QueryEngine.getInstance();
		QueryRequestImpl query = engine.createRequest(qsession);
		
		TableManager manager =TableManager.getInstance();
		query.setMainTable(table.getId());
		
		int[] cmasks= new int[]{Column.MASK_QUERY_LIST};	

		//Select
		query.addSelection(table.getPrimaryKey().getId());
		//query.addColumnsToSelection(cmasks, false);
		
		// Where
		Expression expr2;
		String cs;
		//if( jo.optBoolean("init_query", true)){
			// directory perm
			int dirPerm=  nds.security.Directory.READ;
			expr2 =SecurityUtils.getSecurityFilter(table.getName(), dirPerm, userId, qsession);
			
			if(expr2!=null && !expr2.isEmpty()){
	        	expr=expr2.combine(expr, SQLCombination.SQL_AND,null);
	        }
		//}
			
		
      
		if(expr!=null){
			//logger.debug("query expr"+expr.toString());
			query.addParam(expr);
		}
		
/*		// range
      int startIdx=0;
      int range=  QueryUtils.DEFAULT_RANGE;
      query.setRange(startIdx, range);
*/      
      return query;
	}  
}