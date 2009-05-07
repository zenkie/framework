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
		
	  	List list;
	  	int cxtabId= jo.getInt("cxtab");
	  	String cxtabName;
  		list=engine.doQueryList("select c.ad_processqueue_id, q.name , c.isbackground,c.name from ad_cxtab c, ad_processqueue q where q.id= c.ad_processqueue_id and c.id="+ cxtabId,conn);
	  	
	  	if(list.size()==0) throw new NDSException("@parameter-error@:(cxtab="+ cxtabId+")" );
	  	int queueId= Tools.getInt( ((List)list.get(0)).get(0),-1);
	  	String queueName=(String) ((List)list.get(0)).get(1);
	  	boolean isBg= Tools.getYesNo(((List)list.get(0)).get(2),true);
	  	cxtabName=(String) ((List)list.get(0)).get(3);

	  	int tableId= jo.optInt("table",-1); 
		
		Table table=TableManager.getInstance().getTable(tableId);
		if(table!=null){
			String dir= table.getSecurityDirectory();
			event.setParameter("directory",  dir);	
		  	helper.checkDirectoryReadPermission(event, user);
		}else{
			// check read permission on cxtab
			if (!nds.control.util.SecurityUtils.hasObjectPermission(userId,user.name,"AD_CXTAB",cxtabId, 1,event.getQuerySession())){
				throw new NDSException("@no-permission@");
			}
		}
	  	//default file type to html
	  	String fileType= jo.getString("filetype");
	  	
	  	// cxtab name must be name, not pk in process
	  	String queryStr=jo.getString("query");
		
		QueryRequestImpl req=null;
		if( table!=null){
			JSONObject query=new JSONObject( queryStr);
			req=nds.control.util.AjaxUtils.parseQuery(query, event.getQuerySession(), userId, event.getLocale());
		}else{
			//no table, that's only for jasperreport
			//will set queryStr to queyr directly
		}
		
		 /* 	"query" - for jasperreport without table, will send json.toString() as query 
		 * 		"filter" - description of the current filter
		 * 		"filter_expr" - nds.query.Expression 
		 * 		"cxtab*"		- name of ad_cxtab(AK)
		 * 		"chk_run_now*"	- run immediate or by schedule
		 */
			
	  	int pid=Tools.getInt(engine.doQueryOne("select id from ad_process where classname='nds.process.JReportRunner' and isactive='Y'",conn),-1);
		List params= engine.doQueryList("select name, valuetype,nullable,orderno from ad_process_para where ad_process_id="+pid+" order by orderno asc", conn);
		
		HashMap map=new HashMap();
		map.put("CXTAB", cxtabName);
		if(req!=null){
			map.put("FILTER", req.getParamDesc(true));
			if(req.getParamExpression()!=null)map.put("FILTER_EXPR",req.getParamExpression().toString());
		}else{
			map.put("QUERY",queryStr);
		}
		SimpleDateFormat sdf = new SimpleDateFormat("MMddHHmm");
		String filename="JRP_"+table.getName()+sdf.format(new Date());
		
		map.put("FILENAME", filename);
		map.put("FILETYPE", fileType);
		
	    int piId=ProcessUtils.createAdProcessInstance(pid,queueName, "",user,params,map,conn);
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
	 	if(!(e instanceof NDSException ))throw new NDSEventException("“Ï≥£", e);
	 	else throw (NDSException)e;
    }finally{
    	if(conn!=null){
    		try{conn.close();}catch(Throwable t){}
    	}
    }	  	  
  }
}