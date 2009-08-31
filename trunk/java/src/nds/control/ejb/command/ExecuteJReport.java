package nds.control.ejb.command;

import java.net.URLEncoder;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;

import org.directwebremoting.WebContext;
import org.json.*;

import nds.control.ejb.Command;
import nds.control.event.DefaultWebEvent;
import nds.control.event.NDSEventException;
import nds.control.util.SecurityUtils;
import nds.control.util.ValueHolder;
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
 * Run jasper report using parameters from ui 
 */
public class ExecuteJReport extends Command {
	 /** 
	  * @param event contains JSONObject that has following properties:
	 * 		"query*"    - JSONObject.toString() which should be parsed 
	 * 		"cxtab*"    - id of ad_cxtab
	 *		"table"     - id of table that current cxtab working on, -1 means no table (only jasperreport params)
	 *		"filetype"  - "xls" or "html", by default, html
	 * 
	 * @return  
	 * 		Create instance of nds.process.JReportRunner first, then
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
		
		int tableId= jo.getInt("table"); 
		Table table=TableManager.getInstance().getTable(tableId);
		String dir= table.getSecurityDirectory();
		event.setParameter("directory",  dir);	
	  	helper.checkDirectoryReadPermission(event, user);
		
	  	//default file type to html
	  	String fileType= jo.getString("filetype");
	  	
	  	List list;
	  	String cxtabName= jo.getString("cxtab");
	  	int cxtabId =Tools.getInt(cxtabName, -1);
	  	if(cxtabId==-1){
	  		list=engine.doQueryList("select c.ad_processqueue_id, q.name , c.isbackground,c.name,c.pre_procedure,c.AD_PI_COLUMN_ID from ad_cxtab c, ad_processqueue q where q.id= c.ad_processqueue_id and c.name="+ QueryUtils.TO_STRING(cxtabName),conn);
	  		cxtabId=Tools.getInt( engine.doQueryOne("select id from ad_cxtab where ad_client_id="+ user.adClientId +" and c.name="+ QueryUtils.TO_STRING(cxtabName),conn), -1);
	  	}else{
	  		list=engine.doQueryList("select c.ad_processqueue_id, q.name , c.isbackground,c.name,c.pre_procedure,c.AD_PI_COLUMN_ID from ad_cxtab c, ad_processqueue q where q.id= c.ad_processqueue_id and c.id="+ cxtabName,conn);
	  	}
	  	
	  	if(list.size()==0) throw new NDSException("@parameter-error@:(cxtab="+ cxtabName+")" );		
	  	int queueId= Tools.getInt( ((List)list.get(0)).get(0),-1);
	  	String queueName=(String) ((List)list.get(0)).get(1);
	  	boolean isBg= Tools.getYesNo(((List)list.get(0)).get(2),true);
	  	
	  	cxtabName=(String) ((List)list.get(0)).get(3);
	  	
	  	String preProcedure=(String) ((List)list.get(0)).get(4);
	  	int piColumnId=Tools.getInt( ((List)list.get(0)).get(5), -1);//AD_PI_COLUMN_ID

	  	
//	  cxtab name must be name, not pk in process
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

		int pid=Tools.getInt(engine.doQueryOne("select id from ad_process where classname='nds.process.JReportRunner' and isactive='Y'",conn),-1);
		List params= engine.doQueryList("select name, valuetype,nullable,orderno from ad_process_para where ad_process_id="+pid+" order by orderno asc", conn);
		HashMap map=new HashMap();
		String filterDesc=null;
		if(Validator.isNotNull(preProcedure)){
			
			piId = engine.getSequence("ad_pinstance");
			
			//create filter for cxtab data
			Expression expr=null;
			if (piColumnId !=-1) expr=new Expression(new ColumnLink( new int[]{piColumnId}),"="+ piId,null);
			else logger.debug("find ad_pi_column_id=-1 for cxtab id="+ cxtabId);
			req=ExecuteCxtab.constructQuery(table,event.getQuerySession(), userId, event.getLocale(),expr);
			
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
		    	
		    	filterDesc=ExecuteCxtab.addJparams(params, map, cxtabId,csMap,event.getQuerySession(), userId,event.getLocale(), conn);
			}
		}else{
			req=nds.control.util.AjaxUtils.parseQuery(query, event.getQuerySession(), userId, event.getLocale());
		}	  	
	  	// cxtab name must be name, not pk in process
	  	String queryStr=jo.getString("query");
		
		
		 /* 	"query" - for jasperreport without table, will send json.toString() as query 
		 * 		"filter" - description of the current filter
		 * 		"filter_expr" - nds.query.Expression 
		 * 		"cxtab*"		- name of ad_cxtab(AK)
		 * 		"chk_run_now*"	- run immediate or by schedule
		 */
			
		map.put("CXTAB", cxtabName);
		map.put("FILTER", ((filterDesc==null)? req.getParamDesc(true): filterDesc));
		map.put("QUERY", queryStr); 
		
		if(req.getParamExpression()!=null)map.put("FILTER_EXPR",req.getParamExpression().toString());

		SimpleDateFormat sdf = new SimpleDateFormat("MMddHHmm");
		String filename="JRP_"+table.getName()+sdf.format(new Date());
		
		map.put("FILENAME", filename);
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
	  		hd=ProcessUtils.executeImmediateADProcessInstance(piId , userId, false);
	  		
	  		/**
	  		 * For htm type, will direct to print page, for xls, direct download
	  		 */
	  		String url;
	  		//url = "/html/nds/cxtab/viewrpt.jsp?file="+URLEncoder.encode(filename+"."+ fileType,"UTF-8");
	  		if( "htm".equals(fileType) ){
	  			url = "/html/nds/cxtab/viewrpt.jsp?file="+URLEncoder.encode(filename+"."+ fileType,"UTF-8");
	  		}else{
	  			url= "/html/nds/cxtab/downloadrpt.jsp?file="+URLEncoder.encode(filename+"."+ fileType,"UTF-8");
	  			//"/servlets/binserv/Download/"+ URLEncoder.encode(filename+"."+ fileType,"UTF-8");
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
}