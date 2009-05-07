package nds.control.ejb.command;

import java.rmi.RemoteException;
import java.sql.ResultSet;
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
 *集中解密，为1次解密
 *解密当前处于待评状态的令牌报价, 解密出来的价格 仍然需要客户端的USBKEY进行解密
 *select id, pricecoded from b_prj_token where c_project_id=" + projectId+" and state='W' and isactive='Y' and price is null order by id asc"
 */

public class AHYY_DecryptPrice extends Command {
	/**
	 * @param event
	 */
  public ValueHolder execute(DefaultWebEvent event) throws RemoteException, NDSException {
  	User usr=helper.getOperator(event);
  	
  	event.setParameter("directory", "C_PROJECT_CTRL_LIST");
  	helper.checkDirectoryWritePermission(event, usr);
  	QueryEngine engine=QueryEngine.getInstance();
  	
	int projectCtrlId=Tools.getInt( event.getParameterValue("objectid",true), -1);
	TableManager manager= TableManager.getInstance();
	Connection conn= engine.getConnection();
	int projectId= Tools.getInt( engine.doQueryOne("select c_project_id from c_project_ctrl where id="+ projectCtrlId,conn),-1);
	String prjState=(String) engine.doQueryOne("select t.c_state from c_project t where id ="+ projectId, conn);
    if (!"V".equalsIgnoreCase(prjState)) throw new NDSException("该项目报价未截止!");
		
	StringBuffer message=new StringBuffer();
	boolean hasError=false;
	PreparedStatement stmt=null;
	PreparedStatement stmt2=null;
    ResultSet rs=null;
	ProjectPasswordManager.ProjectPassword pp=null;
	try{
		ProjectPasswordManager ppm= ProjectPasswordManager.getInsatnce();
		if(!ppm.isPasswordSet(projectId, 1) ) throw new NDSException("密码1未设置");
		if(!ppm.isPasswordSet(projectId, 2)) throw new NDSException("密码2未设置");
		
		pp= ppm.getProjectPassword(projectId);
		// LOG
		ArrayList params=new ArrayList();
		params.add(new Integer(projectId));
		engine.executeStoredProcedure("C_PROJECT_DM1_BEGIN", params, false, conn);
		
		stmt2= conn.prepareStatement("update b_prj_token set pricedecode=?, decodedate=sysdate,state_bidprice='H' where id=?");
		
		String sql= "select id, pricecoded from b_prj_token where c_project_id=" + projectId+" and state='W' and isactive='Y' and state_bidprice='Y' order by id asc";
		logger.debug(sql);
        stmt= conn.prepareStatement(sql);
        rs= stmt.executeQuery(sql);
		int tid=-1;
		String pricecoded;
		float price;
		pp.generateCipher();
		while(rs.next()){
			try{
				tid= rs.getInt(1);
				pricecoded= rs.getString(2);
				//price=Float.parseFloat();
				//stmt2.setFloat(1,price);
				if(nds.util.Validator.isNotNull(pricecoded)){
					stmt2.setString(1, pp.decrypt(pricecoded));
					stmt2.setInt(2,tid);
					stmt2.executeUpdate();
				}else{
					logger.debug("pricecoded of b_prj_token(id="+tid+") is not set");
				}
			}catch(Throwable t){
				logger.error("Fail to update price of b_prj_token(id="+tid+")", t);
				message.append(" 品种(ID="+ tid+") 解密失败:"+ t);
				hasError=true;
			}
		}
		// update project status to decoded half
		params=new ArrayList();
		params.add(new Integer(projectId));
		engine.executeStoredProcedure("C_PROJECT_DM1_END", params, false, conn);
		
		if(!hasError) message.append("各品种申报企业需自行解密才能获取当初报价");
		if(pp!=null) pp.clearCipher();
	}catch(Throwable t){
  		if(t instanceof NDSException) throw (NDSException)t;
  		logger.error("exception",t);
  		throw new NDSException(t.getMessage(), t);
  	}finally{
  		
        try{stmt.close();}catch(Exception ea){}
        try{stmt2.close();}catch(Exception ea){}
        try{rs.close();}catch(Exception e){}
        try{conn.close();}catch(Exception e){}
  	} 
	
	//ProjectPasswordManager.ProjectPassword  pp= ppm.getProjectPassword(projectId);
	
	
	/**
	 */

	ValueHolder holder= new ValueHolder();
	holder.put("message","报价集中解密阶段结束, "+ message.toString());
	holder.put("code","1");
	return holder;
  }
}