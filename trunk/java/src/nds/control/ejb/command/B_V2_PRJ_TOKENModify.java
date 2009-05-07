package nds.control.ejb.command;
import org.json.*;
import java.rmi.RemoteException;
import java.sql.*;
import java.util.ArrayList;

import nds.control.ejb.Command;
import nds.control.event.DefaultWebEvent;
import nds.control.event.NDSEventException;
import nds.control.util.ValueHolder;
import nds.query.*;
import nds.schema.*;
import nds.util.*;

import com.liferay.portal.service.UserLocalServiceUtil;
import com.liferay.portal.*;
/*import com.liferay.portal.ejb.UserManagerUtil;
import com.liferay.portal.ejb.UserManager;
import com.liferay.portal.ejb.UserManagerFactory;

*/
import nds.security.User;
import nds.ahyy.*;


/**
 * 
 * 加密报价
 * 界面上允许设置放弃，当放弃时，需要设置价格为0，并且将是否放弃设置为是
 * 价格必须设置为不高于上次报价，如果上次报价不为空
 *  含有参数 "keycode" 是加密时候的必须要求与当前用户的keycode 一致

 **/

public class B_V2_PRJ_TOKENModify extends Command {
	
	private double getDouble(Object str, double defaultValue) {
    	if(str==null) return defaultValue;
        try {
            return Double.parseDouble(str.toString());
        } catch(Exception e) {
        }
        return defaultValue;
    }
	/**
	 * @param event
	 * 
	 */
  public ValueHolder execute(DefaultWebEvent event) throws RemoteException, NDSException {
	  logger.debug(event.toDetailString());
  	User usr=helper.getOperator(event);
  	int objectId =Tools.getInt(event.getParameterValue("id",true),-1);//B_PRJ_TOKEN
  	
  	event.setParameter("directory", "B_V2_PRJ_TOKEN_LIST");
  	if(!nds.control.util.SecurityUtils.hasObjectPermission(usr.id.intValue(), usr.name, 
  			"B_V2_PRJ_TOKEN", objectId, nds.security.Directory.WRITE, event.getQuerySession())){
  		throw new NDSException("@no-permission@");
  	}
  	JSONArray row= (JSONArray)event.getParameterValue("JSONROW");
  	if(row==null)throw new NDSException("请在列表界面里切换到修改模式进行价格填报");
  	
  	QueryEngine engine=QueryEngine.getInstance();
/*  	Object po= event.getParameterValue("price"); // 加密的价格
  	String price=null;
  	if(po !=null ) price= po.toString();
  	double currentPrice=0;
  	try{
  		if(price!=null) currentPrice=Double.parseDouble(price);
  		else throw new NDSException("必须输入价格");
  	}catch(NumberFormatException nfe){
  		throw new NDSException("价格必须为数字型:"+ price);
  	}
  	if(currentPrice<0) throw new NDSException("价格不能小于0");
  	else if(currentPrice > 100000000) throw new NDSException("价格不能大于1亿");
*/
  	
  	TableManager manager= TableManager.getInstance();
		
	PreparedStatement stmt2=null;
	Connection conn= engine.getConnection();
	ProjectPasswordManager.ProjectPassword pp=null;
//	double lastPrice =getDouble( engine.doQueryOne("select PRICELAST from B_PRJ_TOKEN where id="+ objectId, conn),Double.MAX_VALUE);

//	if(currentPrice >lastPrice) throw new NDSException("报价不得高于上次报价("+lastPrice+")");
	
	try{
	  	Object pomd5= row.get(row.length()-1); 

		int projectId= Tools.getInt( engine.doQueryOne("select c_project_id from B_PRJ_TOKEN where id="+ objectId, conn),-1);
		ProjectPasswordManager ppm= ProjectPasswordManager.getInsatnce();
		if(!ppm.isPasswordSet(projectId, 1 ) || !ppm.isPasswordSet(projectId, 2) ) 
			throw new NDSException("项目统一加密密码未设置，请立即联络系统管理员.");
		
		/**
		 * 在js/portalcontrol.js上进行了特殊的设置,  每加密行的倒数第二个为用户的keycode,防止用错了key
		 */
		String keyCode=( row.optString(row.length()-2, "") );
		String userKey=(String) engine.doQueryOne("select EMAILVERIFY from users where id="+ usr.id);
		if(nds.util.Validator.isNull(userKey) ){
			throw new NDSException("当前用户("+ usr.name+")未定义CA标识，无法接受报价");
		}
		if(nds.util.Validator.isNull(keyCode) ){
			throw new NDSException("未在报价时使用USBKEY，无法接受报价");
		}
		if(!keyCode.equals(userKey)){
			throw new NDSException("当前报价采用的USBKEY不是用户法定的KEY，请使用正确的USBKEY");
		}
		pp= ppm.getProjectPassword(projectId);
		pp.generateCipher(); // make sure cipher exists
		String encodedPrice=pp.encrypt( (String)event.getParameterValue("price"));
		/**
		 * 在 js/portalcontrol.js上进行了特殊的设置，将在浏览器上完成加密，加密前对价格进行md5，就是pricehash，
		 * pricehash =md5("M"+ b_prj_token.id + b_prj_token.price), 这样价格一致的行生成出来的pricehash也是不一致的。 
		 * 在解密后将对价格进行比较，如果不一致，将报错。
		 */
		String pricehash=( row.optString(row.length()-1, "") );
		
		String sql="update b_prj_token set pricecoded=?, pricehash=?,price=?, abort_flag=?, gupinfo=?,state_bidprice='Y', modifieddate=sysdate, modifierid=? where id=?";
		logger.debug(sql+"(pricecoded="+ encodedPrice+", id="+ objectId+")");
		
		stmt2= conn.prepareStatement(sql);
		int c=1;
		stmt2.setString(c++,encodedPrice);
		stmt2.setString(c++,pricehash);
		stmt2.setNull(c++,java.sql.Types.FLOAT );
		String abort_flag= (String) event.getParameterValue("abort_flag");
		String giveupInfo= (String) event.getParameterValue("GUPINFO");
		logger.debug("abort_flag="+abort_flag+", giveupInfo="+giveupInfo);
		if(abort_flag ==null) stmt2.setNull(c++, java.sql.Types.VARCHAR);
		else stmt2.setString(c++, abort_flag);

		if(giveupInfo ==null) stmt2.setNull(c++, java.sql.Types.VARCHAR);
		else stmt2.setString(c++, giveupInfo);
		
		stmt2.setInt(c++,usr.id.intValue());
		stmt2.setInt(c++,objectId);
		
		stmt2.executeUpdate();

		// CALL DB PROC
		ArrayList params=new ArrayList();
		params.add(new Integer(objectId));
		engine.executeStoredProcedure("B_V2_PRJ_TOKEN_AM", params, false, conn);
		
	}catch(Throwable t){
  		if(t instanceof NDSException) throw (NDSException)t;
  		logger.error("exception",t);
  		throw new NDSException(t.getMessage(), t);
  	}finally{
        try{stmt2.close();}catch(Exception ea){}
        try{conn.close();}catch(Exception e){}
  	} 
	
	ValueHolder holder= new ValueHolder();
	holder.put("message", "设置的价格已被加密保存在系统中");
	holder.put("code","0");
	return holder;
  }
}