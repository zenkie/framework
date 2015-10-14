package nds.control.ejb.command;

import java.io.File;
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
	public boolean internalTransaction(DefaultWebEvent event) {
		return true;
	}	
  public ValueHolder execute(DefaultWebEvent event) throws RemoteException, NDSException {
	Configurations conf=(Configurations)nds.control.web.WebUtils.getServletContextManager().getActor(nds.util.WebKeys.CONFIGURATIONS);
	String exportRootPath=conf.getProperty("export.root.nds","/act/home");	  
	Connection conn=null;
  	PreparedStatement pstmt=null;
	long startTime=System.currentTimeMillis();
  	
  	QueryEngine engine= QueryEngine.getInstance();
    
	conn= engine.getConnection();

	try{
		User user=helper.getOperator(event);
		int userId=user.getId().intValue();
		JSONObject jo= event.getJSONObject();
		
		//Table table = TableManager.getInstance().findTable(jo.get("table"));
		//int tableId= table.getId();
		//String dir= table.getSecurityDirectory();
		//event.setParameter("directory",  dir);	
	  	//helper.checkDirectoryReadPermission(event, user);
		
	  	//default file type to html
		logger.debug(jo.toString());
	  	String fileType= jo.getString("filetype");
	  	
	  	List list;
	  	String cxtabName= jo.getString("cxtab");
	  	int cxtabId =Tools.getInt(cxtabName, -1);
	  	if(cxtabId==-1){
	  		list=engine.doQueryList("select c.ad_processqueue_id, q.name , c.isbackground,c.name,c.pre_procedure,c.AD_PI_COLUMN_ID,c.ad_table_id  from ad_cxtab c, ad_processqueue q where q.id= c.ad_processqueue_id and c.name="+ QueryUtils.TO_STRING(cxtabName),conn);
	  		cxtabId=Tools.getInt( engine.doQueryOne("select id from ad_cxtab where ad_client_id="+ user.adClientId +" and c.name="+ QueryUtils.TO_STRING(cxtabName),conn), -1);
	  	}else{
	  		list=engine.doQueryList("select c.ad_processqueue_id, q.name , c.isbackground,c.name,c.pre_procedure,c.AD_PI_COLUMN_ID,c.ad_table_id  from ad_cxtab c, ad_processqueue q where q.id= c.ad_processqueue_id and c.id="+ cxtabName,conn);
	  	}
	  	
	  	if(list.size()==0) throw new NDSException("@parameter-error@:(cxtab="+ cxtabName+")" );		
	  	int queueId= Tools.getInt( ((List)list.get(0)).get(0),-1);
	  	String queueName=(String) ((List)list.get(0)).get(1);
	  	boolean isBg= Tools.getYesNo(((List)list.get(0)).get(2),true);
	  	
	  	cxtabName=(String) ((List)list.get(0)).get(3);
	  	
	  	String preProcedure=(String) ((List)list.get(0)).get(4);
	  	int piColumnId=Tools.getInt( ((List)list.get(0)).get(5), -1);//AD_PI_COLUMN_ID
	  	int Tableid = Tools.getInt(((List)list.get(0)).get(6), -1);//ad_table_id
	  	Table table=TableManager.getInstance().getTable(Tableid);
		String dir= table.getSecurityDirectory();
		event.setParameter("directory",  dir);	
	  	helper.checkDirectoryReadPermission(event, user);	  	
	  	
//	  cxtab name must be name, not pk in process
		JSONObject query=new JSONObject( jo.getString("query"));
		
		/**
		 * ���������
		 * 1��ͨ��cxtab.ad_table_id�������ֶι���Ĳ�ѯ�������ͨ���ӱ�
		 * 2��ͨ��cxtab_jpara �Ķ��幹��Ĳ�ѯ�����Ԥ����洢���̣�
		 * 
		 * ���ڵ�1��������ӽ��������������ѯ����
		 * ��2����������յĽ�����ѯ���������� select xxx from dd where ad_pi_id=143, ���������where���
		 * ���� ad_pi_id��ŵ����ݡ���2������ĳ���������pre_procedure is not null
		 * 
		 * AD_CXTAB.AD_PI_COLUMN_ID number(10)��ad_column.id �����������ʵ������ֶ�����ָ���ĸ��ֶε�ֵ����ʶ��Ϊ
		 * ���α�������ݡ�����ֶν��洢P_PI_ID��ֵ�� ���������Ϊ ad_pi_id�����������AD_PINSTANCE��
		 * �������ֶβ��������ã��ڲ�ѯ��ʵ��ʱ������ȡȫ�����ݡ�
		 * 
		 */
		QueryRequestImpl req=null;
		int piId=-1;

		int pid=Tools.getInt(engine.doQueryOne("select id from ad_process where classname='nds.process.JReportRunner' and isactive='Y'",conn),-1);
		List params= engine.doQueryList("select name, valuetype,nullable,orderno from ad_process_para where ad_process_id="+pid+" order by orderno asc", conn);
		HashMap map=new HashMap();
		String filterDesc=null;
		QuerySession qs;
		if ((qs = event.getQuerySession()) == null)
			qs = QueryUtils.createQuerySession(userId, user.getSecurityGrade(), "EXECUTECXTAB", event.getLocale());

		if(Validator.isNotNull(preProcedure)){
			
			piId = engine.getSequence("ad_pinstance");
			
			//create filter for cxtab data
			Expression expr=null;
			if (piColumnId !=-1) expr=new Expression(new ColumnLink( new int[]{piColumnId}),"="+ piId,null);
			else logger.debug("find ad_pi_column_id=-1 for cxtab id="+ cxtabId);
			req=ExecuteCxtab.constructQuery(table,qs, userId, event.getLocale(),expr);
			
			// ��ad_cxtab_jpara��Ĳ������幹�쵽 params ����������ϴ���Ĳ���ֵ���õ�map ��
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
		    	
		    	filterDesc=ExecuteCxtab.addJparams(params, map, cxtabId,csMap,qs, userId,event.getLocale(), conn);
			}
		}else{
			req=nds.control.util.AjaxUtils.parseQuery(query, qs, userId, event.getLocale());
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
		//String filename="JRP_"+table.getName()+sdf.format(new Date());
		String filename=null;
		if(jo.optString("filename")!=null&&jo.optString("filename")!=""){
			filename=jo.optString("filename");
		}else{
			filename="CXR_"+cxtabId+"_"+sdf.format(new Date());
		}
		map.put("FILENAME", filename);
		map.put("FILETYPE", fileType);

		String folder = jo.optString("folder");
		String loaclpath=exportRootPath + File.separator+user.getClientDomain()+File.separator;
		if(!Validator.isNull(folder)&&folder.indexOf("monitor")>0)loaclpath="";
		folder=Validator.isNull(folder)?loaclpath+user.getName():loaclpath + folder;
		if (Validator.isNotNull(folder)) {
			map.put("FOLDER",folder);
		}
		
	
	    piId=ProcessUtils.createAdProcessInstance(piId,pid,queueName, "",user,params,map,conn);
	    ValueHolder hd=null;
	    JSONObject returnObj=new JSONObject();
	    if((!jo.optBoolean("forcerun", false))&&isBg){
	    	hd=new ValueHolder();
	    	hd.put("code","0");
	    	returnObj.put("message", MessagesHolder.getInstance().translateMessage("@cxtab-task-generated@",event.getLocale()));
	  	}else{
	  		//run now
	  		hd=ProcessUtils.executeImmediateADProcessInstance(piId , userId, false);

	  		/**
	  		 * For htm type, will direct to print page, for xls, direct download
	  		 * add empty .jsp
	  		 */
	  		String url;
	  		//url = "/html/nds/cxtab/viewrpt.jsp?file="+URLEncoder.encode(filename+"."+ fileType,"UTF-8");

	  		String filePath =Validator.isNull(folder)?exportRootPath + File.separator+user.getClientDomain()+File.separator+ user.getName():folder;
			
	 		File f=new File(filePath+File.separator+filename+"."+ fileType);
	 		if(!f.exists()||(f.length() == 0L)) {
	  			url = "/html/nds/cxtab/empty.jsp";
	  		}
	  		else if( "htm".equals(fileType) ){
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
	 	if(!(e instanceof NDSException ))throw new NDSEventException("�쳣", e);
	 	else throw (NDSException)e;
    }finally{
    	if(conn!=null){
    		try{conn.close();}catch(Throwable t){}
    	}
    }	  	  
  }
}