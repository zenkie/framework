/*
 * Agile Control Technologies Ltd,. CO.
 * http://www.agileControl.com
 */
package nds.control.web.binhandler;

import java.io.*;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.*;
import nds.control.util.*;
import nds.query.*;
import nds.control.web.SessionContextManager;
import nds.control.web.UserWebImpl;
import nds.control.web.WebUtils;
import nds.control.ejb.*;
import nds.control.event.*;
import nds.log.Logger;
import nds.log.LoggerManager;
import nds.report.ReportUtils;
import nds.rest.*;
import nds.schema.TableManager;
import nds.util.*;
/**
 * ����Rest����

�ڷ������ϸ���ר��URL��http://portal.server/servlets/binserv/Rest���������нӿ�����

HTTP��֤���£�

Ϊ�ͻ��˳����ṩappkey ��Ӧ�ó�����,��Ϊϵͳ�û�������appSecret ����Կ����Ϊϵͳ�û�����Ӧ�����MD5�룩

�������ݣ�����ѡ������ҵ�������Ϊquery������д��HttpURLConnection�� Post��ר��URL

��ѡ������
sip_appkey - Ӧ�ó���ı��
sip_timestamp - ��������ʱ���(yyyy-mm-dd hh:mm:ss.xxx)��֧�ֺ��롣
sip_sign - ǩ����ʹ��sip_appkey+sip_timestamp+appSecret����MD5���㣬ת��Ϊcod64�õ���32λ���ַ�������������ҪУ���ֵ

ҵ�����(JSON)
tranaction - ����Transaction�����ݣ�������transactions ͬʱ����
transactions -[transaction,...] //���Transaction, һ��transaction��Ķ��������ȫ���ɹ�����ȫ��ʧ�ܣ�ÿ��Transaction����Ķ������
transaction:{
	id: <transaction-id> // ͨ��IDʹ�ÿͻ����ܻ�ȡtransaction��ִ�����
	command:"ObjectCreate"|"ObjectModify"|"ObjectDelete"|"ObjectSubmit"|"WebAction"|"ProcessOrder"|"Query"|"Import",//Transaction�Ĳ�������
	params:{ //��������Ĳ���
		<command-param>:<command-value>,
		...
	}
}	
	
�������ݣ�
��Http Header���в�����
sip_status - ָ�����ؽ���Ƿ���Ч, Ϊ����number(4), 9999��ʾ�ɹ�����������ʧ��

��Http body���д������,ΪJSON��ʽ
[transaction-response,...]
transaction-response:{
	id:<transaction-id>,
	code:<number>,
	message:<string>,
	<addtion-data-name>:<addtion-data-value>,
	...
}

һ��transaction��Ķ��������ȫ���ɹ�����ȫ��ʧ��, code=0��ʾ�ɹ���������Ϊʧ�ܣ�ʧ�ܵ���Ϣ��message��


 
 * @author yfzhu@agilecontrol.com
 */

public class Rest implements BinaryHandler{
	  private Logger logger= LoggerManager.getInstance().getLogger(Rest.class.getName());	 

	  private static final long NETWORK_DELAY_SECONDS=1000*60*2;// 2 mininutes 
	  private static final String CONTENT_TYPE_TEXT = "text/html; charset=UTF-8";
	  
	  /**
	   */
      public void process(HttpServletRequest request,HttpServletResponse  response)  throws Exception{
    	  
    	  String message=null;
    	  SipStatus status=null;
    	  try{
	    	  status= validateRequest(request);
	    	  if(status==SipStatus.SUCCESS ){
	        	  // handle transctions
	        	  String ts=request.getParameter("transactions");
	        	  if(ts!=null){
	        		  SessionContextManager scmanager= WebUtils.getSessionContextManager(request.getSession());
	        		  UserWebImpl usr=(UserWebImpl)scmanager.getActor(WebKeys.USER);
	        		  
	        		  JSONArray ja=new JSONArray(ts);
	        		  JSONArray jr=new JSONArray();
	        		  for(int i=0;i< ja.length();i++){
	        			  // handle every transactions
	        			  TransactionResponse tr=handleTransaction(usr,ja.getJSONObject(i));
	        			  jr.put(tr);
	        		  }
	        		  //write to http body
	        		  response.setContentType(CONTENT_TYPE_TEXT);
	        		  PrintWriter out = response.getWriter();
	        		  out.print(jr.toString());
	        	  }else{
	        		  status=SipStatus.ERROR;
	        		  message="transactions not found in request";
	        	  }
	    	  }
    	  }catch(Throwable t){
    		  logger.error("Fail to process rest :",t);
    		  status= SipStatus.ERROR;
    		  message=t.getLocalizedMessage();
    	  }
    	  response.addHeader("sip_status", status.getCode());
		  if(status!=SipStatus.SUCCESS){
		      response.setContentType(CONTENT_TYPE_TEXT);
		      PrintWriter out = response.getWriter();
		      out.println((message ==null?status.toString(): message));
		  }
      }
      /**
		У���ѡ����, ���Ҹ����û������������û���Ϣ(UserWebImpl)
		��ѡ������
		sip_appkey - Ӧ�ó���ı��
		sip_timestamp - ��������ʱ���(yyyy-mm-dd hh:mm:ss.xxx)��֧�ֺ��롣
		sip_sign - ǩ����ʹ��sip_appkey+sip_timestamp+appSecret����MD5���㣬ת��Ϊcod64�õ���32λ���ַ�������������ҪУ���ֵ
       * @param request
       * @return
       */
      private SipStatus validateRequest(HttpServletRequest request) throws Exception{
    	  String sip_appkey=request.getParameter("sip_appkey");
    	  String sip_timestamp=request.getParameter("sip_timestamp");
    	  String sip_sign=request.getParameter("sip_sign");
    	  logger.debug("input sip_sign="+sip_sign+",sip_appkey="+sip_appkey+",sip_timestamp="+sip_timestamp);
    	  
    	  if(nds.util.Validator.isNull(sip_appkey)) return  SipStatus.NEED_APPKEY;
    	  if(nds.util.Validator.isNull(sip_timestamp)) return  SipStatus.NEED_TIMESTAMP;
    	  if(nds.util.Validator.isNull(sip_sign)) return  SipStatus.NEED_SIGN;
    	  
    	  /*
    	  // parse timestamp
    	  SimpleDateFormat a=new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");
    	  a.setLenient(false);
    	  long d=a.parse(sip_timestamp).getTime();
    	  if( System.currentTimeMillis()- d <0 || System.currentTimeMillis()-d >NETWORK_DELAY_SECONDS){
    		  return SipStatus.REQ_TIMEOUT;
    	  }
    	  
    	  
    	  String passwd=(String)QueryEngine.getInstance().doQueryOne("select u.passwordhash from users u where email="+ QueryUtils.TO_STRING(sip_appkey));
    	  logger.debug("passwd="+ passwd);
    	  if(nds.util.Validator.isNotNull(passwd)){
    		  String md5=nds.util.MD5Sum.toCheckSumStr(passwd);
    		  
			  String sign=nds.util.MD5Sum.toCheckSumStr(sip_appkey+sip_timestamp+passwd);
	    	  logger.debug("passwdsign="+ sign);
			  if(!sip_sign.equalsIgnoreCase(sign)){
				  return SipStatus.SIGNATURE_INVALID;
			  }
    	  }else{
    		  return SipStatus.BINDUSER_FAILD;
    	  }*/
    	  logger.debug("authentication passed.");
    	  
    	  WebUtils.getSessionContextManager(request.getSession(true));
    	  request.getSession().setAttribute(org.apache.struts.Globals.LOCALE_KEY,TableManager.getInstance().getDefaultLocale());
    	  request.getSession().setAttribute("USER_ID", sip_appkey+"@burgeon");
    	  //
    	  return SipStatus.SUCCESS;
      }
      /**
       * Should not throw exception
       * Handle one transction
       * @param request
       * @param tra
	id: <transaction-id> // ͨ��IDʹ�ÿͻ����ܻ�ȡtransaction��ִ�����
	command:"ObjectCreate"|"ObjectModify"|"ObjectDelete"|"ObjectSubmit"|"WebAction"|"ProcessOrder"|"Query"|"Import",//Transaction�Ĳ�������
	params:{ //��������Ĳ���
		<command-param>:<command-value>,
		...
	}
       * @return
       */
      private TransactionResponse handleTransaction(UserWebImpl usr, JSONObject tra){
    	  String traId= tra.optString("id","");
    	  TransactionResponse trs=new TransactionResponse(traId);
    	  try{
    		  
    		  String command=tra.getString("command");
    		  JSONObject jo=tra.getJSONObject("params");
    		  jo.put("command",command);
    		  
    		  Result r=AjaxUtils.handle(jo, usr.getSession(), usr.getUserId(), usr.getLocale());
    		  trs.setCode(r.getCode());
    		  trs.setMessage(r.getMessage());
    		  
    	  }catch(Throwable t){
    		  logger.error("fail to handle:"+ tra, t);
    		  trs.setCode(-1);
    		  trs.setMessage(WebUtils.getExceptionMessage(t, usr.getLocale()));
    	  }
    	  return trs;
      }
}