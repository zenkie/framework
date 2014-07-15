package nds.control.ejb.command;

import java.rmi.RemoteException;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import nds.control.ejb.Command;
import nds.control.event.DefaultWebEvent;
import nds.control.util.ValueHolder;
import nds.query.QueryEngine;
import nds.schema.Table;
import nds.schema.TableManager;
import nds.security.User;
import nds.util.NDSException;
import nds.util.Tools;
import nds.weixin.ext.WeUtils;
import nds.weixin.ext.WeUtilsManager;

public class wx_refshkey extends Command {

	@Override
	public ValueHolder execute(DefaultWebEvent event) throws NDSException,
			RemoteException {
		// TODO Auto-generated method stub
		
		try{
		ValueHolder vh= new ValueHolder();
		nds.security.User  usr= helper.getOperator(event);
	     int clientId= usr.adClientId; 
    	JSONObject jo=event.getJSONObject();
    	JSONObject params=new JSONObject( jo.getString("params"));
    	logger.debug("params!!!!!!!"+params.toString());

   
    	int objectId = -1;
     	objectId=params.optInt("objid");
    	
    	//String appid = null,appSecret = null;
    	//List li=QueryEngine.getInstance().doQueryList("select t.APPID,t.APPSECRET from WX_INTERFACESET t where t.id="+objectId);
        //if (li.size() > 0) {
        //	appid = String.valueOf(((List)li.get(0)).get(0));
        //	appSecret = String.valueOf(((List)li.get(0)).get(1));
        // }
 
		WeUtilsManager Wemanage =WeUtilsManager.getInstance();
    	Wemanage.unloadAdClientId(clientId);
    	WeUtils wu=Wemanage.loadAdClientbyid(clientId);
	   	if(wu!=null){
	//    		wu.setAppId(appid);
	//    		wu.setAppSecret(appSecret);
	   		logger.debug("setRefshmem true!");
	  		wu.setRefshmem(true);//接口重新获取新的token
	 	}
    	
    	vh.put("message","appid,appsecret 刷新成功！");
		vh.put("code","0");
		vh.put("data","appid,appsecret 刷新成功！");
		return vh;
		}catch(Throwable e){
    		logger.error("Fail to wx_refshkey background:", e);
    		if( e instanceof NDSException) throw (NDSException)e;
    		else throw new NDSException(e.getMessage(), e);
    		//holder.put("code", "-1");
    	}
	
	}

}
