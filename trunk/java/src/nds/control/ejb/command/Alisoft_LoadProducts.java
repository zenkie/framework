package nds.control.ejb.command;

import java.rmi.RemoteException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import org.directwebremoting.WebContext;
import org.json.*;

import nds.saasifc.*;
import nds.control.ejb.Command;
import nds.control.event.DefaultWebEvent;
import nds.control.util.ValueHolder;
import nds.control.web.WebUtils;
import nds.process.ProcessUtils;
import nds.query.QueryEngine;
import nds.query.SPResult;
import nds.schema.TableManager;
import nds.security.User;
import nds.util.Configurations;
import nds.util.NDSException;
import nds.util.Tools;

/**
 * 从淘宝获取正在出售的商品，导入到系统中，写入ali_product 表，含transid 表示一次交易，
 * 导入完成后执行 ali_product_sync 方法, 此方法将产品信息写入产品表（update/insert)，
 * 并发送导入完成的通知给调用者（或者在线等待）
 * 
 * 用户必须具有产品写权限
 * 
 * 此命令需要以json 方式调用
 * @author yfzhu
 *
 */
public class Alisoft_LoadProducts  extends Command {
	/**
	 * How many items will be fetched per request, max to 200
	 */
	private static int NUM_PER_PAGE=100;
	private static String API_METHOD_ONSALE="taobao.items.onsale.get";
	private static String API_METHOD_INSTOCK="taobao.items.instock.get";
	
	/**
	 * @param event 
	 * 	"type" - "onsale"(default), "instock"
	 *  
	 *  
	 */
	public ValueHolder execute(DefaultWebEvent event) throws RemoteException, NDSException {
	  	QueryEngine engine=QueryEngine.getInstance();
	  	User usr=helper.getOperator(event);
	  	event.setParameter("directory", "M_PRODUCT_LIST");
	  	helper.checkDirectoryWritePermission(event, usr);
	  	
	  	
	  	// sale first, instock second
	  	String  apiMethod=API_METHOD_ONSALE;//"taobao.items.onsale.get";// "instock".equals(event.getParameterValue("type",true))? "taobao.items.onsale.get":"taobao.items.instock.get";
	  	
	  	
	  	
		TableManager manager= TableManager.getInstance();
		String message="@complete@";	
		Connection conn= engine.getConnection();
		PreparedStatement pstmt= null; 
		
		int transactionId=Integer.parseInt( (new java.text.SimpleDateFormat("MMdd")).format(new java.util.Date())+  
				(new java.text.DecimalFormat("000000")).format( (nds.util.Sequences.getNextID("alitrans"))));
		
		/*
		 * format alisoft datetime 
		 * 
		 */
		java.text.SimpleDateFormat sd=new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    	sd.setLenient(false);
		ValueHolder holder= new ValueHolder();
		
		String sql="insert into ali_product(ad_client_id,transid,iid,title,type,cid,seller_cids,props,pic_path,num," +
		"valid_thru,list_time,delist_time,stuff_status,location,price,post_fee,express_fee,ems_fee,has_discount," +
		"freight_payer,has_invoice,has_warranty,has_showcase,bulk_base_num,modified,increment_,auto_repost,approve_status)" +
		"values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		logger.debug(sql);
		VendorAPIManager vm=VendorAPIManagerFactory.getInstance().getManager(VendorAPIManagerFactory.VENDOR_ALISOFT);
		try{
			 
			pstmt= conn.prepareStatement(sql);
			java.util.HashMap parameters=new java.util.HashMap();
			//系统级参数

			parameters.put("sip_format","json");
			parameters.put("format","json");
		  	JSONObject jo=(JSONObject)event.getParameterValue("jsonObject");	  	
		  	WebContext wc=(WebContext) jo.get("org.directwebremoting.WebContext");
		  	String sid=wc.getHttpServletRequest().getSession().getId();
			parameters.put("sip_sessionid", sid);
			
			//  应用级参数
			parameters.put("fields","transid,nick,iid,title,type,cid,seller_cids,props," +
					"pic_path,num,valid_thru,list_time,delist_time,stuff_status,location,price,post_fee,express_fee," +
					"ems_fee,has_discount,freight_payer,has_invoice,has_warranty,has_showcase,bulk_base_num,modified," +
					"increment,auto_repost,approve_status" );//,不支持desc这个字段，如果需要获取该字段数据，调用taobao.item.get
			parameters.put("v", "1.0");
			parameters.put("q", "");
			
			int pageNo=0;
			int cnt=0;
 
			while(true){
				pageNo++;
				parameters.put("page_no", pageNo);
				parameters.put("page_size", NUM_PER_PAGE); // maximum
				//parameters.put("format", "json");
				ValueHolder vh =vm.invokeAPI(apiMethod, parameters);
				int code= Tools.getInt(vh.get("code"), -1);
				String msg= (String)vh.get("message");
				logger.debug("page_no:"+ pageNo+", page_size:"+ NUM_PER_PAGE+ ", code:"+ code+",msg:"+ msg);
				if(code==0){
					JSONObject omsg=new JSONObject(msg);
					JSONObject items= omsg.optJSONObject("rsp");
					if(items==null) items=new JSONObject();
					JSONArray ja= items.optJSONArray("items");
					if(ja==null)ja=new JSONArray();
					for(int i=0;i<ja.length();i++){
						cnt++;
						JSONObject item=ja.getJSONObject(i);

						int c=1;
						//ad_client_id,transid,iid,title,type,cid,seller_cids,props,pic_path,num
						pstmt.setInt(c++, usr.adClientId);
						pstmt.setInt(c++,transactionId );
						pstmt.setString(c++, item.getString("iid"));
						pstmt.setString(c++, item.getString("title"));
						pstmt.setString(c++, item.optString("type",""));
						pstmt.setString(c++, item.optString("cid",""));
						pstmt.setString(c++, item.optString("seller_cids",""));
						pstmt.setString(c++, item.optString("props",""));
						pstmt.setString(c++, item.optString("pic_path",""));
						pstmt.setInt(c++, item.optInt("num",0));
						
						//valid_thru,list_time,delist_time,stuff_status,location,price,post_fee,express_fee,ems_fee,has_discount
						pstmt.setInt(c++, item.optInt("valid_thru",0));
						pstmt.setDate(c++, getDate(item.optString("list_time"), sd));
						pstmt.setDate(c++, getDate(item.optString("delist_time"), sd));
						pstmt.setString(c++, item.optString("stuff_status",""));
							JSONObject loc= item.optJSONObject("location");
							if(loc==null)loc=new JSONObject();
						pstmt.setString(c++, loc.optString("city",""));
						pstmt.setString(c++, item.optString("price",""));
						pstmt.setString(c++, item.optString("post_fee",""));
						pstmt.setString(c++, item.optString("express_fee",""));
						pstmt.setString(c++, item.optString("ems_fee",""));
						pstmt.setString(c++, item.optBoolean("has_discount",false)?"Y":"N");
						
						//freight_payer,has_invoice,has_warranty,has_showcase,bulk_base_num,modified,increment,auto_repost,approve_status
						pstmt.setString(c++, item.optString("freight_payer",""));
						pstmt.setString(c++, item.optBoolean("has_invoice",false)?"Y":"N");
						pstmt.setString(c++, item.optBoolean("has_warranty",false)?"Y":"N");
						pstmt.setString(c++, item.optBoolean("has_showcase",false)?"Y":"N");
						pstmt.setInt(c++, item.optInt("bulk_base_num",0));
						pstmt.setDate(c++, getDate(item.optString("modified"), sd));
						pstmt.setString(c++, item.optString("increment",""));
						pstmt.setString(c++, item.optBoolean("auto_repost",false)?"Y":"N");
						pstmt.setString(c++, item.optString("approve_status",""));
						
						pstmt.executeUpdate();
					}
					if(ja.length()< NUM_PER_PAGE){
						if(API_METHOD_ONSALE.equals(apiMethod)){
							apiMethod = API_METHOD_INSTOCK;
							pageNo=0;
						}
						else break;
					}
				}else{
					if(code==-1004){
						Configurations conf= (Configurations)WebUtils.getServletContextManager().getActor( nds.util.WebKeys.CONFIGURATIONS);
						String url="http://sip.alisoft.com/sip/login?sip_apiname=" + apiMethod +"&sip_appkey="+ 
							conf.getProperty("saas.alisoft.appkey")+"&sip_sessionid="+ sid+ "&sip_apptype=1";

						String serverURL= conf.getProperty("server.url");
						url=url+"&sip_redirecturl="+ java.net.URLEncoder.encode(serverURL+"/html/nds/alisoft/loadpdt.jsp", "UTF-8");
						logger.debug("redirect to "+ url);
						holder.put("data", url);
						break;
					}else
						throw new NDSException("从淘宝导入商品时遇到错误:"+ msg);
				}
			}
			if(cnt >0){
				  ArrayList params=new ArrayList();
				  params.add(new Integer(transactionId));//
				  params.add(usr.getId());//
				  SPResult result=engine.executeStoredProcedure("ali_product_sync", params, false, conn);
				
			}
		  	message="共导入"+ cnt+"条商品信息";
		  	// loading product category defined by user
		}catch(Throwable t){
	  		logger.error("exception",t);
	  		if(t instanceof NDSException) throw (NDSException)t;
	  		throw new NDSException(t.getMessage(), t);
	  	}finally{
	  		try{pstmt.close();}catch(Exception e){}
	        try{conn.close();}catch(Exception e){}
	  	} 
		
		holder.put("message", message);
		holder.put("code","0");
		return holder;	  
  }
	/**
	 * Default to current date
	 * @param d
	 * @param sf
	 * @return
	 */
	private java.sql.Date getDate(String d, java.text.SimpleDateFormat sd){
		try{
			return new java.sql.Date(sd.parse(d).getTime());
		}catch(Throwable t){
			return new java.sql.Date(System.currentTimeMillis());
		}
	}
}
