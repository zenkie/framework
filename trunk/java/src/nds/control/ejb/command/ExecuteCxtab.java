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
 * Create crosstab runner process instance, including following parameters:
 * 	select filter (where clause)
 *  ad_cxtab template
 * 
 * @author yfzhu@agilecontrol.com 
 */
public class ExecuteCxtab extends Command {
	 /** 
	  * @param event contains JSONObject that has following properties:
	 * 		"query*" - JSONObject.toString() which should be parsed 
	 * 		"cxtab*"		- name of ad_cxtab (AK,String) or id (PK, Integer)
	 *		"table*"  - id of table that current cxtab working on
	 *		"filetype"  - "xls" or "html", by default, html
	 * 
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
		
		int tableId= jo.getInt("table"); 
		Table table=TableManager.getInstance().getTable(tableId);
		String dir= table.getSecurityDirectory();
		event.setParameter("directory",  dir);	
	  	helper.checkDirectoryReadPermission(event, user);
		
	  	//default file type to html
	  	String fileType= jo.getString("filetype");
	  	//if(!"xls".equals(fileType)) fileType="htm";
	  	
	  	List list;
	  	String cxtabName= jo.getString("cxtab");
	  	int cxtabId =Tools.getInt(cxtabName, -1);
	  	if(cxtabId==-1){
	  		list=engine.doQueryList("select c.ad_processqueue_id, q.name , c.isbackground,c.name from ad_cxtab c, ad_processqueue q where q.id= c.ad_processqueue_id and c.name="+ QueryUtils.TO_STRING(cxtabName),conn);
	  		cxtabId=Tools.getInt( engine.doQueryOne("select id from ad_cxtab where ad_client_id="+ user.adClientId +" and c.name="+ QueryUtils.TO_STRING(cxtabName),conn), -1);
	  	}else{
	  		list=engine.doQueryList("select c.ad_processqueue_id, q.name , c.isbackground,c.name from ad_cxtab c, ad_processqueue q where q.id= c.ad_processqueue_id and c.id="+ cxtabName,conn);
	  	}
	  	
	  	if(list.size()==0) throw new NDSException("@parameter-error@:(cxtab="+ cxtabName+")" );
	  	int queueId= Tools.getInt( ((List)list.get(0)).get(0),-1);
	  	String queueName=(String) ((List)list.get(0)).get(1);
	  	boolean isBg= Tools.getYesNo(((List)list.get(0)).get(2),true);
	  	
	  	cxtabName=(String) ((List)list.get(0)).get(3);
	  	// cxtab name must be name, not pk in process
		JSONObject query=new JSONObject( jo.getString("query"));
		QueryRequestImpl req= nds.control.util.AjaxUtils.parseQuery(query, event.getQuerySession(), userId, event.getLocale());
	
		
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
			
	  	int pid=Tools.getInt(engine.doQueryOne("select id from ad_process where classname='nds.process.CxtabRunner' and isactive='Y'",conn),-1);
		List params= engine.doQueryList("select name, valuetype,nullable,orderno from ad_process_para where ad_process_id="+pid+" order by orderno asc", conn);
		
		HashMap map=new HashMap();
		map.put("CXTAB", cxtabName);
		map.put("FILTER", req.getParamDesc(true));
		if(req.getParamExpression()!=null)map.put("FILTER_EXPR",req.getParamExpression().toString());
		
		
		SimpleDateFormat sdf = new SimpleDateFormat("MMddHHmm");
		//String filename="CXR_"+table.getName()+sdf.format(new Date());
		String filename="CXR_"+cxtabId+sdf.format(new Date());
		
		map.put("FILENAME", filename);
		//map.put("FILETYPE", isBg?"xls":"htm");
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
	  		//and tell client to fetch file from 
	  		//returnObj.put("url", "/servlets/binserv/GetFile?show=Y&filename="+ URLEncoder.encode(filename+"."+ fileType,"UTF-8"));
	  		
	  		/**
	  		 * For htm type, will direct to print page, for xls, direct download
	  		 */
	  		String url;
	  		//url = "/html/nds/cxtab/viewrpt.jsp?file="+URLEncoder.encode(filename+"."+ fileType,"UTF-8");
	  		if( "htm".equals(fileType) ){
	  			url = "/html/nds/cxtab/viewrpt.jsp?file="+URLEncoder.encode(filename+"."+ fileType,"UTF-8");
	  		}else if("xls".equals(fileType) ){
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
	 	if(!(e instanceof NDSException ))throw new NDSEventException("“Ï≥£", e);
	 	else throw (NDSException)e;
    }finally{
    	if(conn!=null){
    		try{conn.close();}catch(Throwable t){}
    	}
    }	  	  
  }
}