package nds.control.ejb.command;

import java.rmi.RemoteException;
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
 * 设置项目密码
 * 系统管理员分两人，每个人都需要在设置项目密码界面填写密码。密码在浏览器本地使用CA卡进行加密，加密后的密文和密码一道上传服务器。密码以对称算法加密后保存到服务器，密文驻留内存。两人的密文合并后作为AES算法的加密因子，对客户上传的加密报价进行二次加密。
CA 加密密码无需可逆，使用CFCA_HashMessage 方法
修改管理员密码设置界面。用户设置密码，点击提交时，使用密钥对密码进行加密，方法为CFCA_HashMessage，该密文将作为ASE算法因子，不保留在数据库中。另外使用CFCA_EnvelopeMessage方法对密码进行对称算法加密，该密文保存在数据库中。
后台c_project_ctrl 表无需修改

 *
 */

public class AHYY_SetProjectPassword extends Command {
	/**
	 * @param event "password1" 此密码将采用对称算法加密保存在后台，以防管理员遗忘,
	 *  "objectid" "columnid","keycode" - 管理员的CA标识
	 * "pwdhash" - 经CA哈希过的密码，此密码将驻留内存作为加密因子
	 * 
	 */
  public ValueHolder execute(DefaultWebEvent event) throws RemoteException, NDSException {
  	User usr=helper.getOperator(event);
  	
  	event.setParameter("directory", "C_PROJECT_CTRL_LIST");
  	helper.checkDirectoryWritePermission(event, usr);
  	QueryEngine engine=QueryEngine.getInstance();
  	
	int projectCtrlId=Tools.getInt( event.getParameterValue("objectid",true), -1);
	int columnId=Tools.getInt( event.getParameterValue("columnid",true), -1);
	TableManager manager= TableManager.getInstance();
	int pos= manager.getColumn(columnId).getDescription(event.getLocale()).indexOf('1')>0? 1:2;
	int projectId= Tools.getInt( engine.doQueryOne("select c_project_id from c_project_ctrl where id="+ projectCtrlId),-1);
	
	java.lang.String password1= (String)event.getParameterValue("password1",true);
	java.lang.String password2= (String)event.getParameterValue("password2",true);
	String message=null;

	try{
		ProjectPasswordManager ppm= ProjectPasswordManager.getInsatnce();
		if(ppm.isPasswordSet(projectId, pos)) throw new NDSException("密码已设置");
		
	  	if(!nds.ahyy.Utils.isKeycodeValid(usr.id.intValue(),(String) event.getParameterValue("keycode",true))){
	  		throw new NDSException("@usbkey-error@");
	  	}
	  	String pwdhash=(String) event.getParameterValue("keycode",true);
	  	if(nds.util.Validator.isNull(pwdhash)){
	  		throw new NDSException("加密错误，请检查usbkey");
	  	}
		
		if(pos==1) {
			message=ppm.setPassword1(projectId, password1,password2,pwdhash);
		}
		else message=ppm.setPassword2(projectId, password1,password2,pwdhash);
		
	}catch(Throwable t){
  		if(t instanceof NDSException) throw (NDSException)t;
  		logger.error("exception",t);
  		throw new NDSException(t.getMessage(), t);
  	}
	
	//ProjectPasswordManager.ProjectPassword  pp= ppm.getProjectPassword(projectId);
	
	
	/**
	 */

	ValueHolder holder= new ValueHolder();
	holder.put("message", message);
	holder.put("code","0");
	return holder;
  }
}