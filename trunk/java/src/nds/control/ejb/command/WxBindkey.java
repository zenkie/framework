package nds.control.ejb.command;

import java.io.File;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;
import java.util.Vector;

import org.json.JSONObject;

import com.kin.weixin.WeixinBind;

import nds.control.ejb.Command;
import nds.control.event.DefaultWebEvent;
import nds.control.event.NDSEventException;
import nds.control.util.ValueHolder;
import nds.control.web.UserWebImpl;
import nds.control.web.WebUtils;
import nds.control.web.binhandler.Wxvcode;
import nds.query.QueryEngine;
import nds.schema.Column;
import nds.schema.Table;
import nds.schema.TableManager;
import nds.security.User;
import nds.util.Configurations;
import nds.util.NDSException;
import nds.util.Tools;
import nds.util.WebKeys;
import nds.weixin.ext.WeUtils;
import nds.weixin.ext.WeUtilsManager;

public class WxBindkey extends Command {

	public static final String PROP_STORAGEDIR = "webclient.upload";
	private String m_storageDir;
	
	@Override
	public ValueHolder execute(DefaultWebEvent event) throws NDSException,
			RemoteException {
		ValueHolder vh= new ValueHolder();
		JSONObject rs=new JSONObject();
	  	JSONObject jo=(JSONObject)event.getParameterValue("jsonObject");
	  	JSONObject params=null;
  	    QueryEngine engine=QueryEngine.getInstance();
        Connection conn= engine.getConnection();
        PreparedStatement pstmt=null;
    	try{
   		 Configurations conf=(Configurations)WebUtils.getServletContextManager().getActor(WebKeys.CONFIGURATIONS);
	     m_storageDir = conf.getProperty(PROP_STORAGEDIR, "/act.net/webhome");
	     User usr=helper.getOperator(event);
	     int clientId= usr.adClientId; 
	     	params=jo.getJSONObject("params");
	     	String login_user=params.getString("user");
	   		String login_pwd=params.getString("pwd");
	   		String vcode=params.optString("vcode","");
	   		String userid=params.getString("userid");
    		String url = null,token = null;
   
    		//vcode
    		Wxvcode wxv=Wxvcode.getInstance();
    		WeixinBind wx = new WeixinBind(login_user, login_pwd,wxv.getcookei(userid));
    		wx.login(vcode);
    		logger.debug("get token ->"+wx.getToken());
    		if(wx.getLoginErrCode()==0&&wx.getToken()!=null){
    		

    		wx.editDevInteface(wx.getToken());//编辑开发模式
    		
    		
    		List li=engine.doQueryList("select url,wxtoken from WX_INTERFACESET t where t.ad_client_id="+clientId,conn);
	        if (li.size() > 0) {
	        	url = String.valueOf(((List)li.get(0)).get(0));
	        	token = String.valueOf(((List)li.get(0)).get(1));
	         }
    		wx.editCommonInteface(wx.getToken(),url,token);
    		
    		if(!wx.isDevUser(wx.getToken())){
    			vh.put("message", "尚未成为开发者，请在公众平台中检查相应的注册信息！");
    			vh.put("code","-1");
    			return vh;
    		}
    		
    		
    		WeUtilsManager Wemanage =WeUtilsManager.getInstance();
			WeUtils wu=Wemanage.getByAdClientId(usr.adClientId);
    		String svrPath = m_storageDir+ File.separator  + wu.getDoMain();
    		
    		wx.editServiceOAuth(wx.getToken(), wu.getDoMain());//oauth2.0
    		
    		
    		
    		File uploadPath = new File(svrPath);//上传文件目录
    		logger.debug("uploadPath ->"+svrPath);
    	    if (!uploadPath.exists()) {
    	       uploadPath.mkdirs();
    	    }
    		svrPath+=File.separator+"wxappcode.jpg";
    		wx.getQrcode(wx.getToken(),svrPath);
    		
    		//QueryEngine.getInstance().executeUpdate(sql)
    		pstmt= conn.prepareStatement("update WEB_CLIENT set qrcode=? where ad_client_id=?");		
   			pstmt.setString(1,"/servlets/userfolder/wxappcode.jpg");
   			pstmt.setInt(2,clientId);
		    pstmt.executeUpdate();
		    //set OriginalID
		    //set appid
		    //set appKey
		    //set ServiceType
    		pstmt= conn.prepareStatement("update WX_INTERFACESET set originalid=?,appid=?,appsecret=?,PUBLICTYPE=? where ad_client_id=?");		
   			pstmt.setString(1,wx.getOriginalID(wx.getToken()));
   			pstmt.setString(2,wx.getAppId(wx.getToken()));
   			pstmt.setString(3,wx.getAppKey(wx.getToken()));
   			pstmt.setInt(4,wx.getServiceType(wx.getToken()));
   			pstmt.setInt(5,clientId);
		    pstmt.executeUpdate();
		    
    		vh.put("message", wx.getLoginErrMsg());
			vh.put("code","0");
    		}else if(wx.getLoginErrCode()==-8||wx.getLoginErrCode()==-27){
    			vh.put("message", wx.getLoginErrMsg());
    			vh.put("code","-8");
    		}else{
    			System.out.print("getLoginErrCode->"+wx.getLoginErrCode());
    			vh.put("message", wx.getLoginErrMsg());
    			vh.put("code","-1");
    		}
    		
   		}catch(Throwable t){
   	  		if(t instanceof NDSException) throw (NDSException)t;
   	  		logger.error("exception",t);
   	  		throw new NDSException(t.getMessage(), t);	
   	 	}finally{
   	        try{pstmt.close();}catch(Exception ea){}
   	        try{conn.close();}catch(Exception e){}
   	  	} 
    	return vh;
	}
}
