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
import org.json.*;

/**
 * 解密已经由管理员解密的加密价格
 	加密算法设计：单一管理员密码以可逆加密方式保存在系统中，该密码通过usb-key硬件加密，
 	加密数据作为AES算法的密钥，对生产商的加密产品报价进行二次加密。密码和USB-KEY分别保管在两个人手上。
 	由于密码可以由开发商反向获取，key可以由CA复制，保证了管理员出现意外的情况下（忘记密码，遗失key），开标工作正常进行*
 */

public class AHYY_LoadPriceCoded2 extends Command {
  /**
   * How many records for each page at most
   */
  private static int COUNT_PER_PAGE=10; 
	/**
	 * 本程序为根据项目和当前用户获取未解密的数据，传回到客户端完成基于CA的解密。如果客户端传递了已经解密的数据，则需要
	 * 写入到数据库里
	 * @param event
	 *    "projectid*" - 对应项目id
	 *    "prices" 数组，内部为JSONObject 对象，含有以下属性：id, price, 当为空的时候，表示客户端没有解密数据
	 * @return      
	 *    
	 */
  public ValueHolder execute(DefaultWebEvent event) throws RemoteException, NDSException {
  	User usr=helper.getOperator(event);
  	int userId= usr.id.intValue();
  	QueryEngine engine=QueryEngine.getInstance();
	TableManager manager= TableManager.getInstance();
  	java.util.Locale locale= event.getLocale();
  	JSONObject jo= event.getJSONObject();
	StringBuffer message=new StringBuffer();
	boolean hasError=false;
	PreparedStatement stmt=null;
	PreparedStatement stmt2=null;
    ResultSet rs=null;
	Connection conn= engine.getConnection();
	String sql;
	JSONObject returnObj=new JSONObject();
	JSONArray  returnPrices=new JSONArray();
	String msg=nds.util.MessagesHolder.getInstance().translateMessage("@complete@",locale);
	try{

	  	int applierId= userId;// Tools.getInt(engine.doQueryOne("select C_BPARTNER_ID from users where id="+ userId,conn), -1);
	  	//if(applierId==-1) throw new NDSException("当前用户("+ usr.name+")未配置所属机构");
	  	int projectId=jo.getInt("projectid");
	  	
	  	// Get band
	  	//int band= Tools.getInt(engine.doQueryOne("select bank from c_project_ctrl where c_project_id="+ projectId, conn), -1);
	  	//if(band ==-1) throw new NDSException("项目轮次未设定，请通知项目管理员");
	  	
		JSONArray prices= jo.optJSONArray("prices");
		if(prices==null || JSONObject.NULL.equals(prices)){
		}else{
			// 解密价格的hash值必须与初始的pricehash 值一致
			sql= "update b_prj_token set price=?, state_bidprice='P',DECODEDATE=sysdate, modifieddate=sysdate, modifierid="+ userId+" where id=? AND state_bidprice='H' and pricehash=?";
			
			stmt=conn.prepareStatement(sql);
			for(int i=0;i< prices.length();i++){
				JSONObject po= prices.getJSONObject(i);
				double d= po.getLong("price")/100.0;  // 初始加密的时候，为了防止小数点位数错误，统一以 Int(price *100) 保存，所以最终价格也是按price/100获得
				int tokenid=po.getInt("id");
				String hash= StringUtils.hash("M"+ tokenid+po.getLong("price") );
				stmt.setDouble(1,d);
				stmt.setInt(2, tokenid);
				stmt.setString(3,  hash);
				if(stmt.executeUpdate()==0){
					conn.createStatement().execute("update b_prj_token set state_bidprice='F',DECODEDATE=sysdate, modifieddate=sysdate, modifierid="+ userId+" where id="+tokenid+" AND state_bidprice='H'");
					logger.error("b_prj_token (id="+ tokenid+") update price failed: from web: price="+ d+", hash="+hash);
					
				}
			}
		}
		int totalCount= Tools.getInt(engine.doQueryOne("select count(*) from b_prj_token where c_project_id=" + projectId+" and ownerid="+ applierId+ " and isactive='Y' and state_bidprice='H'",conn), 0);
		if(totalCount>0){
			// fetch next lines, at most 10 lines one time
			sql= "select id, PRICEDECODE from b_prj_token where c_project_id=" + projectId+" and ownerid="+ applierId+ " and isactive='Y' and state_bidprice='H' and rownum< 11 order by id asc";
			logger.debug(sql);
	        stmt2= conn.prepareStatement(sql);
	        rs= stmt2.executeQuery(sql);
			int tid=-1;
			String pricecoded;
			float price;
			while(rs.next()){
				try{
					tid= rs.getInt(1);
					pricecoded= rs.getString(2);
					JSONObject po=new JSONObject();
					po.put("id", tid);
					po.put("pricecode", pricecoded);
					returnPrices.put(po);
				}catch(Throwable t){
					logger.error("Fail to get PRICEDECODE of b_prj_toke(id="+tid+")", t);
				}
			}
		}
		if(returnPrices.length()==0){
			int fcnt= Tools.getInt(engine.doQueryOne("select count(*) from b_prj_token where c_project_id=" + projectId+" and ownerid="+ applierId+ " and isactive='Y' and state_bidprice='F'",conn), 0);
			if(fcnt>0)msg="解密结束，但发现您有"+ fcnt+"个品种的申报价格和解密价格不一致，若不处理，这些品种将还原为上轮报价，或作废，请速联系管理员解决。";
		}
		returnObj.put("tokenAllCount", totalCount);
		returnObj.put("priceObj", returnPrices);
		returnObj.put("message", msg);
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
  	ValueHolder holder= new ValueHolder();
	holder.put("message", msg);
	holder.put("code","0");
	holder.put("data",returnObj );
	return holder;
  }
}