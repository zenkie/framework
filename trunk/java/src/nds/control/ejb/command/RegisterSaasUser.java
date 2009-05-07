package nds.control.ejb.command;
import java.io.FileWriter;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Vector;
import java.sql.*;

import javax.servlet.http.HttpServletRequest;

import nds.control.ejb.Command;
import nds.control.ejb.command.pub.Pub;
import nds.control.event.DefaultWebEvent;
import nds.control.event.NDSEventException;
import nds.control.web.*;
import nds.control.util.*;
import nds.query.*;
import nds.schema.*;
import nds.security.Directory;
import nds.security.User;
import nds.util.*;

import org.directwebremoting.WebContext;
import org.json.*;
/**
处理来自 /saas/reg.jsp 的请求，注册saas 供应商的付费用户，saas供应商的名称和用户id 保存在session的attribute里
分别为 "saasvendor" 和 "saasuser" (参见 alisoft.jsp) 
根据用户选择，决定是否创建ad_client，还是加入已存在ad_client, 对于加入请求，将设置创建用户的isactive='N', 以等待
对应公司的root开放对应用户	  
*/
public class RegisterSaasUser  extends Command{
	private final static String UPDATE_ROOT="update users set truename=?, phone2=? where name='root' and ad_client_id=(select id from ad_client where domain=?";

    /**
     */
    public ValueHolder execute(DefaultWebEvent event) throws NDSException ,RemoteException{
    	ValueHolder vh=new ValueHolder();
    	JSONObject data=new JSONObject();
    	try{
	    	long beginTime= System.currentTimeMillis();
	        logger.debug(event.toDetailString());
			
	        JSONObject jo= event.getJSONObject();
        	// login session 
			WebContext ctx = (WebContext)jo.get("org.directwebremoting.WebContext");
			String saasVendor= (String)ctx.getSession().getAttribute("saasvendor");
			String saasUser=  (String)ctx.getSession().getAttribute("saasuser");
			if(saasVendor==null || saasUser==null){
				throw new NDSException("必须首先在SAAS供应商页面完成登录");
			}
        	int userId=Tools.getInt(QueryEngine.getInstance().doQueryOne("select id from users where email="+ QueryUtils.TO_STRING(jo.getString("email"))),-1);
        	if(userId!=-1) throw new NDSException(jo.getString("email")+"对应的用户已存在");
			
/*
create or replace procedure AD_CLIENT_Clone(v_copydomain in varchar2,
                                            v_domain in varchar2,
                                            v_clientname in varchar2,
                                            v_memorysize in varchar2,
                                            v_description in varchar2,
                                            v_email in varchar2,
                                            v_root_password in varchar2 := 'test') is */	        
	        if(jo.getBoolean("newCompany")){
	        	//new company
	        	String domain=jo.getString("domain");
	        	String companyName= jo.getString("companyname");
	        	String location=jo.optString("location");
	        	String phone= jo.optString("companyphone");

	        	int adClientId=Tools.getInt(QueryEngine.getInstance().doQueryOne("select id from ad_client where domain="+ QueryUtils.TO_STRING(domain)),-1);
	        	if(adClientId!=-1) throw new NDSException(domain+"对应的公司已存在");
	        	
				Configurations conf=(Configurations)nds.control.web.WebUtils.getServletContextManager().getActor(nds.util.WebKeys.CONFIGURATIONS);
				String templateDomain=conf.getProperty("newclient.template","demo");
				String fileSize= conf.getProperty("newclient.size", "50M");
	        	
	        	ArrayList params=new ArrayList();
	        	params.add(templateDomain);
	        	params.add(domain);
	        	params.add(companyName);
	        	params.add(fileSize);
	        	params.add(location+","+ phone);
	        	
	        	params.add(jo.getString("email"));
	        	params.add(jo.getString("password"));
	        	
	        	helper.executeStoredProcedure("AD_CLIENT_CLONE", params, false);
	        	// retrieve that new client' root and update user information
	        	String sql="update users set saasvendor="+QueryUtils.TO_STRING( saasVendor)+ 
	        		",saasuser="+QueryUtils.TO_STRING( saasUser)+ 
	        		",truename="+ QueryUtils.TO_STRING(jo.getString("truename"))+
	        		", phone2="+ QueryUtils.TO_STRING(jo.optString("mobile"))+
	        		" where name='root' and ad_client_id=(select id from ad_client where domain="+
	        		QueryUtils.TO_STRING(jo.optString("domain"))+")";
	        	logger.debug(sql);
   	        	
	        	int cnt=QueryEngine.getInstance().executeUpdate(sql);
	        	
   	        	
    			User user=nds.saasifc.UserUtils.getUser(saasVendor,saasUser);
    			WebUtils.loginSSOUser(user,ctx.getHttpServletRequest() , ctx.getHttpServletResponse());
    			
    			data.put("nextscreen", "/html/nds/portal/index.jsp");
    			data.put("message", "注册成功");
	        }else{
	        	//enter existing company
	        	String compDomain=jo.getString("compdomain");
	        	int adClientId=Tools.getInt(QueryEngine.getInstance().doQueryOne("select id from ad_client where domain="+ QueryUtils.TO_STRING(compDomain)),-1);
	        	if(adClientId==-1) throw new NDSException(compDomain+"对应的公司不存在");
	        	
	        	userId=QueryEngine.getInstance().getSequence("USERS");
	        	
	        	String sql="insert into users(id, ad_client_id,saasuser,saasvendor,passwordhash,creationdate, modifieddate,isactive,name,truename,email,phone2) values("+userId+","+adClientId+
	        	","+QueryUtils.TO_STRING(saasUser)+","+QueryUtils.TO_STRING( saasVendor)+
	        	","+QueryUtils.TO_STRING(jo.getString("password"))+","+"sysdate,sysdate,'N',"+QueryUtils.TO_STRING( jo.getString("name"))+","+QueryUtils.TO_STRING( jo.getString("truename"))+
	        	","+QueryUtils.TO_STRING( jo.getString("email"))+","+QueryUtils.TO_STRING( jo.optString("mobile"))+")";
	        	
	        	QueryEngine.getInstance().executeUpdate(sql);
	        	ArrayList params=new ArrayList();
	        	params.add(new Integer(userId));
	        	helper.executeStoredProcedure("USERS_AC", params, false);
	        	data.put("message", "注册成功，请等待公司管理员激活账户后即可登录系统");
	        	data.put("nextscreen","/");
	        }
    	}catch(Exception e){
            logger.error("Found error:", e);
            if(e instanceof NDSException)throw (NDSException)e;
            else throw new NDSEventException("异常",e);
        }
    	vh.put("data", data);
    	return vh;
    }
   
}