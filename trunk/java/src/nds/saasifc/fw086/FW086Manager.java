package nds.saasifc.fw086;

import nds.control.util.Result;
import nds.control.web.WebUtils;


import java.security.*;
import java.text.SimpleDateFormat;

import javax.crypto.*;
import javax.crypto.spec.*;

import com.microsoft.sispark.saasinterface.*;

import org.codehaus.xfire.service.Service;
import org.codehaus.xfire.service.binding.*;

import java.util.*;
import org.json.*;

import nds.control.web.*;
import nds.control.event.*;
import nds.log.Logger;
import nds.log.LoggerManager;
import nds.util.*;

public class FW086Manager {
	private static Logger logger= LoggerManager.getInstance().getLogger(FW086Manager.class.getName());
	private  DesEncrypter desEncrypter=null;
	private  String iSVId =null;
	private  String appId =null;
	private String saaSWSInterface=null;
	private String saaSLoginUrl=null;
	
	private SaaS2ISVInterfaceClient client;
	private FW086Manager() throws Exception{
    	Configurations conf= (Configurations)WebUtils.getServletContextManager().getActor(WebKeys.CONFIGURATIONS);
    	String key= conf.getProperty("saas.fw086.CryptogramKey","ABCD1234");         
		desEncrypter=new DesEncrypter(key);
		
		iSVId = conf.getProperty("saas.fw086.ISVID","ISV200806050001");
		appId = conf.getProperty("saas.fw086.AppID","PRO200806050001");
		saaSWSInterface = conf.getProperty("saas.fw086.SaaSWSInterface", "http://222.92.117.85:7001/SaaS2ISVInterface.asmx");
		saaSLoginUrl = conf.getProperty("saas.fw086.SaaSLoginUrl","http://222.92.117.85:7002/SSOLogin.aspx");
		
		client= new SaaS2ISVInterfaceClient();
	}
	
	private	static FW086Manager instance=null;
	public static FW086Manager getInstance() {
		
		if(instance==null){
			try{
			instance = new FW086Manager();
			}catch(Throwable t){
				logger.error("Fail", t);
				throw new NDSRuntimeException(t.getMessage());
			}
		}
		return instance;
	}
	/**
	 * 
	 * @param token user guid
	 * @return  "code" 0 - ok, 1 - user not found, 2 - user not subscribe this product
	 * 			"saasuser" - saas user id
	 * @throws Exception
	 */
	public JSONObject checkUserToken(String token)throws Exception{
		int code=1;
		String saasUserId="";
		if(nds.util.Validator.isNull(token)){
			code= 1;
		}else{
			RequestInfo ri=new RequestInfo();
			
			RequestHeadInfo head = new RequestHeadInfo();
			head.setAppID(this.appId);
			head.setCode("SYS10101");
			head.setCTID(this.nextCTID());
			head.setSubmitTime(this.now());
			
			String body= "<ISVID>"+ this.iSVId+"</ISVID><AppID>"+this.appId+"</AppID><Token>"+token+"</Token>";
			
			logger.debug(body);
			ri.setBody(this.encodeBodyXML(body));
			
			ri.setHead(head);
			
			ResponseInfo res=executeService(ri);
			logger.debug(res.getBody());
			String xml=decodeBodyXML( res.getBody());
			logger.debug(xml);
			
			JSONObject jo=org.json.XML.toJSONObject(xml);
			if(Tools.getInt(jo.get("ResultCode"),-1)==0){
				String corpId= jo.getString("CorpID");
				String userId= jo.getString("UserID");
				saasUserId= userId;
				// SYS10202
				ri=new RequestInfo();
				
				head = new RequestHeadInfo();
				head.setAppID(this.appId);
				head.setCode("SYS10202");
				head.setCTID(this.nextCTID());
				head.setSubmitTime(this.now());
				
				body= "<AppID>"+this.appId+"</AppID><Token>"+token+"</Token>";
				
				logger.debug(body);
				ri.setBody(this.encodeBodyXML(body));
				
				ri.setHead(head);
				
				res=executeService(ri);
				logger.debug(res.getBody());
				xml=decodeBodyXML( res.getBody());
				logger.debug(xml);
				
				jo=org.json.XML.toJSONObject(xml);
				
				if(Tools.getInt(jo.get("ResultCode"),-1)==0){
					code=0;
				}else{
					code=2;// no permission to this product
				}
					
			}else{
				code= 1;// not found
			}
			
		}
		JSONObject j=new JSONObject();
		j.put("code",code);
		j.put("saasuser",saasUserId);
		
		return j;
	}
	 
	
	private synchronized String nextCTID() {
    	SimpleDateFormat a=new SimpleDateFormat("yyyyMMddHHmmssSSS");
    	return a.format(new Date()).substring(0,16);
	}
	public String now(){
    	SimpleDateFormat a=new SimpleDateFormat("yyyyMMddHHmmssSSS");
    	return a.format(new Date());
	}
	
	/**
	 * Base64(DES(MD5(xml)+xml))
	 * @param xml
	 * @return
	 */
	public String encodeBodyXML(String xml) throws Exception{
		logger.debug("encoding:"+xml);
		String s= MD5Sum.toCheckSumStr(xml)+ xml;
		return this.desEncrypter.encrypt(s);
	}
	/**
	 * Debase64->DeDES-> first 32bit as md5, last as data
	 * @param encryptStr message
	 * @return
	 */
	public String decodeBodyXML(String encryptStr) throws Exception{
		String s= this.desEncrypter.decrypt(encryptStr);
		String md5= s.substring(0,32);
		String xml= s.substring(32);
		String xmlMD5=MD5Sum.toCheckSumStr(xml);
		if(! md5.equals(xmlMD5)){
			logger.error("md5:"+ md5 + "not queals to xml md5:"+xmlMD5+", xml:"+ xml);
			throw new NDSException("@exception@: md5 check error");
		}
		logger.debug("decoded to:"+xml);
		return xml;
	}
	private ResponseHeadInfo createResponseHeadInfo(RequestHeadInfo qi){
		ResponseHeadInfo h= new ResponseHeadInfo();
		
		h.setAppID(qi.getAppID());
		h.setCTID(qi.getCTID());
		h.setCode( qi.getCode());
		h.setRequestTime(qi.getSubmitTime());
		h.setResponseTime(this.now());
		h.setSTID(qi.getAppID()+"_"+ this.nextCTID());
		h.setStatus("0");
		h.setPriority("10");
		h.setVersion("1");
		
		return h; 
	}
	
	public ResponseInfo createResponse(RequestHeadInfo qi, int code, String okMsg) throws Exception{
		return createResponse(qi,code,okMsg,null);
	}
	public ResponseInfo createResponse(RequestHeadInfo qi, int code, String okMsg, JSONObject jo) throws Exception{
		ResponseInfo ri=new ResponseInfo();
		ri.setHead(createResponseHeadInfo(qi));
		String bodyNotEncrypted;
		if(jo!=null){
			jo.put("ResultCode", code);
			jo.put("ResultDesc", okMsg);
			bodyNotEncrypted = org.json.XML.toString(jo);
		}else{
			bodyNotEncrypted= "<ResultCode>"+ code+"</ResultCode><ResultDesc>"+ okMsg+"</ResultDesc>";
		}
		ri.setBody(encodeBodyXML(bodyNotEncrypted));
		return ri;
	}

	public String getAppId() {
		return appId;
	}



	public String getISVId() {
		return iSVId;
	}



	public String getSaaSLoginUrl() {
		return saaSLoginUrl;
	}



	public String getSaaSWSInterface() {
		return saaSWSInterface;
	}	
	
	/**
	 * sys10302
	 * @param userIds
	 * @return
	 * @throws Exception
	 */
	public JSONObject getUsersInfo(String corpId, String userIds) throws Exception{
		
		RequestInfo ri=new RequestInfo();
		
		RequestHeadInfo head = new RequestHeadInfo();
		head.setAppID(this.appId);
		head.setCode("SYS10302");
		head.setCTID(this.nextCTID());
		head.setSubmitTime(this.now());
		
		String body= "<CorpID>"+ corpId+"</CorpID><UserIDs>"+ userIds+"</UserIDs>";
		
		logger.debug(body);
		ri.setBody(this.encodeBodyXML(body));
		
		ri.setHead(head);
		
		ResponseInfo res=executeService(ri);
		String xml=decodeBodyXML( res.getBody());
		
		JSONObject jo=org.json.XML.toJSONObject(xml);
		
		
		return jo ;
		
	}
	/**
	 * sys10301
	 * @param corpId
	 * @return
	 * @throws Exception
	 * @deprecated
	 */
	public JSONObject getCorpInfo(String corpId) throws Exception{
		
		RequestInfo ri=new RequestInfo();
		
		RequestHeadInfo head = new RequestHeadInfo();
		head.setAppID(this.appId);
		head.setCode("SYS10301");
		head.setCTID(this.nextCTID());
		head.setSubmitTime(this.now());
		
		String body= "<CorpID>"+ corpId+"</CorpID>";
		logger.debug(body);
		ri.setBody(this.encodeBodyXML(body));
		
		ri.setHead(head);
		
		ResponseInfo res=executeService(ri);
		String xml=decodeBodyXML( res.getBody());
		 
		JSONObject jo=org.json.XML.toJSONObject(xml);
		
		
		return jo ;
		
	}
	public boolean isAlive(String token) throws Exception{
		RequestInfo ri=new RequestInfo();
		
		RequestHeadInfo head = new RequestHeadInfo();
		head.setAppID(this.appId);
		head.setCode("SYS10102");
		head.setCTID(this.nextCTID());
		head.setSubmitTime(this.now());
		
		String body= "<Token>"+ token+"</Token>";
		
		ri.setBody(this.encodeBodyXML(body));
		
		ri.setHead(head);
		
		ResponseInfo res=executeService(ri);
		String xml=decodeBodyXML( res.getBody());
		 
		JSONObject jo=org.json.XML.toJSONObject(xml);
		
		
		return jo.getInt("ResultCode")==0 ;
		
	}
	private ResponseInfo executeService(RequestInfo ri) throws Exception{
		logger.debug("executeService "+ ri.getHead().getCode());
		ResponseInfo res=client.getSaaS2ISVInterfaceSoap(this.saaSWSInterface).saaSISVInterface(ri);
		//logger.debug("executeService return "+ ri.getHead().getCode()+" with status="+ ri.getHead().get);
		return res;
	}
	/**
	 * 
	 * @param jo from #getCorpInfo
	 * @throws Exception
	 */
	public ResponseInfo executeCommand(String cmd, JSONObject jo,RequestInfo req) throws Exception{
	    if(jo==null ) throw new Exception(" jo is null");
	    int code=(jo.optInt("ResultCode",0));
	    if(code!=0) throw new Exception("´íÎó:"+ jo.optString("ResultDesc","")+"("+ code+")" );
	    jo.put("command",cmd);
	    jo.put("nds.control.ejb.UserTransaction" , "Y");
	    Result r=nds.control.util.AjaxUtils.handle(jo, null, 0, nds.schema.TableManager.getInstance().getDefaultLocale());
	    logger.debug(r.getMessage());
	    ResponseInfo ri;
    	ri= this.createResponse(req.getHead(), r.getCode(), r.getMessage(), (JSONObject)r.getData());
    	return ri;
	}
	
}
