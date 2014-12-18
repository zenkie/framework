package nds.weixin.ext;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import nds.control.event.DefaultWebEvent;
import nds.control.util.ValueHolder;
import nds.control.web.ClientControllerWebImpl;
import nds.control.web.WebUtils;
import nds.log.Logger;
import nds.log.LoggerManager;
import nds.query.QueryEngine;
import nds.query.QueryException;
import nds.rest.RestUtils;
import nds.util.Tools;

public class RestControl {
	private Logger logger= LoggerManager.getInstance().getLogger(RestControl.class.getName());	 
	private String serverUrl;
	private String SKEY;
	private String Sign;
	private String careid;
	private String ts;
	
	//restserver param =>{"BIRTHDAY":"19340202","NAME":"jackrain","PHONENUM":"18005695669","partial_update":true,"table":16003,"CONTACTADDRESS":"天津市辖区和平区中国上海徐汇","parsejson":"Y","PROVINCE":"天津","GENDER":"1","javax.servlet.http.HttpServletRequest":"+/servlets/binserv+/Rest\nPOST /servlets/binserv/Rest HTTP/1.1\r\nCache-Control: no-cache\r\nPragma: no-cache\r\nUser-Agent: Java/1.6.0_07\r\nHost: localhost\r\nAccept: text/html, image/gif, image/jpeg, *; q=.2, */*; q=.2\r\nConnection: keep-alive\r\nContent-Type: application/x-www-form-urlencoded\r\nContent-Length: 682\r\n\r\n","ID":69,"command":"ObjectModify","CITY":"市辖区","REGIONID":"和平区"}
	
	public void disposeCadeControll(JSONObject jo) throws Exception {
		//{"BIRTHDAY":"19340202","NAME":"jackrain","PHONENUM":"13705519364","partial_update":true,"table":16003,"vipid":79,"CONTACTADDRESS":"上海市辖区黄浦区中国上海徐汇","parsejson":"Y","ad_client_id":55,"javax.servlet.http.HttpServletRequest":"+/servlets/binserv+/Rest\nPOST /servlets/binserv/Rest HTTP/1.1\r\nCache-Control: no-cache\r\nPragma: no-cache\r\nUser-Agent: Java/1.6.0_07\r\nHost: localhost\r\nAccept: text/html, image/gif, image/jpeg, *; q=.2, */*; q=.2\r\nConnection: keep-alive\r\nContent-Type: application/x-www-form-urlencoded\r\nContent-Length: 733\r\n\r\n","GENDER":"1","PROVINCE":"上海","command":"ObjectModify","ID":74,"CITY":"市辖区","REGIONID":"黄浦区"}
		String method=jo.optString("method");
		
		logger.debug("dispose cade method->"+method);
		if("opencard".equalsIgnoreCase(method)) {
			openCard(jo);
		}else if("bindCard".equalsIgnoreCase(method)) {
			bindCard(jo);
		}else if("updateCard".equalsIgnoreCase(method)) {
			updateCard(jo);
		}else if("integralExchange".equalsIgnoreCase(method)) {
			integralExchange(jo);
		}
	}
	
	/*开卡
	 * 
	 */
	private void openCard(JSONObject restjo) throws Exception{
		if(restjo==null||!restjo.has("vipid")||!restjo.has("ad_client_id")){
			logger.debug("openCard params error->"+restjo.toString());
			return;
		}
		

		List all=null;
		ValueHolder vh=null;
		int vipid=restjo.optInt("vipid");
		int ad_client_id=restjo.optInt("ad_client_id");
		boolean isErp=false;
		careid=String.valueOf(ad_client_id);
		String isVerifyCode="N";
		String vipno="";
		
		//查询接口相关信息 url,skey
		all=QueryEngine.getInstance().doQueryList("select ifs.erpurl,ifs.username,ifs.iserp,ifs.wxparam,nvl(wc.ismesauth,'N') from WX_INTERFACESET ifs join web_client wc on ifs.ad_client_id=wc.ad_client_id WHERE ifs.ad_client_id="+ad_client_id);
		
		
		if(all!=null&&all.size()>0) {
			logger.debug("WX_INTERFACESET size->"+all.size());
			serverUrl=(String)((List)all.get(0)).get(0);
			isErp="Y".equalsIgnoreCase((String)((List)all.get(0)).get(2));
			SKEY=(String)((List)all.get(0)).get(3);
			isVerifyCode=String.valueOf(((List)all.get(0)).get(4));
			if(isErp&&(nds.util.Validator.isNull(serverUrl)||nds.util.Validator.isNull(SKEY))) {
				logger.debug("SERVERuRL OR SKEY IS NULL");
				throw new Exception("数据维护异常，请联系商家");
			}
		}else {
			System.out.println("not find WX_INTERFACESET ad_client_id->"+ad_client_id);
			throw new Exception("数据维护异常，请联系商家");
		}
			
		
		//判断是否需要验证
		if("Y".equalsIgnoreCase(isVerifyCode)) {
			//判断验证码
			JSONObject vjo=new JSONObject();
			try {
				JSONObject pjo=new JSONObject();
				pjo.put("vipid", restjo.optInt("vipid",0));
				pjo.put("verifycode", restjo.optString("verifycode"));
				vjo.put("params", pjo);
			} catch (Exception e) {
				
			}
			
			try {
				ClientControllerWebImpl controller=(ClientControllerWebImpl)WebUtils.getServletContextManager().getActor(nds.util.WebKeys.WEB_CONTROLLER);
				DefaultWebEvent event=new DefaultWebEvent("CommandEvent");
				event.put("jsonObject", vjo);
				event.setParameter("command", "nds.weixin.ext.ValidationVipVerifycodeCommand");
				vh=controller.handleEvent(event);
			}catch(Exception e) {
				logger.debug("open card verify code error->"+e.getLocalizedMessage());
				throw new Exception("验证异常");
			}
			
			if(vh==null||!"0".equalsIgnoreCase(String.valueOf(vh.get("code")))) {
				logger.debug("open card verify code result error->");
				if(vh==null) {throw new Exception("验证异常");}
				else{throw new Exception(String.valueOf(vh.get("message")));}
			}
		}
		
		if(!isErp) {
			logger.debug("未接通ERP");
			//线上发券
			ArrayList params=new ArrayList();
			params.add(vipid);
			ArrayList para=new ArrayList();
			para.add( java.sql.Clob.class);
			
			try {
				Collection list=QueryEngine.getInstance().executeFunction("wx_coupon_onlinecoupon",params,para);
				String res=(String)list.iterator().next();
				logger.debug("online send coupon result->"+res);
			}catch (QueryException e) {
				logger.debug("online send coupon erroe->"+e.getMessage());
				e.printStackTrace();
			}
			return;
		}
		SipStatus sp=validateSing();
		HashMap<String, String> params =new HashMap<String, String>();
		Connection conn = QueryEngine.getInstance().getConnection();
		String isSendCoupon="N";
		String couponUseType="0";
		String couponCode="";
		conn.setAutoCommit(false);
		try{
			if(sp.getCode().equals("0")){

				List al = QueryEngine.getInstance().doQueryList("select vp.wechatno,vp.vipcardno,vp.store_id,NVL(vt.ISSEND,'N'),cp.num,cp.usetype1,vt.code,nvl(cp.value,'0'),nvl(vp.integral,0),to_char(decode(nvl(cp.validay,0),0,nvl(cp.starttime,sysdate), sysdate), 'YYYYMMDD'),to_char(decode(nvl(cp.validay,0),0, nvl(cp.endtime, add_months(sysdate, 1)),sysdate+cp.validay), 'YYYYMMDD')"+
						" from wx_vip vp LEFT JOIN wx_vipbaseset vt ON vp.viptype=vt.id LEFT JOIN WX_COUPON CP ON NVL(vt.LQTYPE,-1)=cp.id"+
						" WHERE vp.id=?",new Object[] {vipid});
				
				if(al!=null&&al.size()>0) {
					logger.debug("vip size->"+al.size());
					params.put("args[openid]", (String) ((List)al.get(0)).get(0));
					params.put("args[cardid]",String.valueOf(ad_client_id));
					vipno=(String) ((List)al.get(0)).get(1);
					params.put("args[wshno]",vipno);
					params.put("args[shopid]",String.valueOf(((List)al.get(0)).get(2)));
					params.put("args[viptype]",String.valueOf(((List)al.get(0)).get(6)));
					params.put("args[couponval]",String.valueOf(((List)al.get(0)).get(7)));
					params.put("args[credit]",String.valueOf(((List)al.get(0)).get(8)));
					params.put("args[begintime]",String.valueOf(((List)al.get(0)).get(9)));
					params.put("args[endtime]",String.valueOf(((List)al.get(0)).get(10)));
					isSendCoupon=(String)((List)al.get(0)).get(3);
					couponCode=(String)((List)al.get(0)).get(4);
					couponUseType= String.valueOf(((List)al.get(0)).get(5));
					logger.debug("isSendCoupon->"+isSendCoupon+",couponCode->"+couponCode+",couponUseType->"+couponUseType);
					if("N".equalsIgnoreCase(isSendCoupon)||"1".equalsIgnoreCase(couponUseType)) {couponCode="";}
					params.put("args[coupon]",couponCode);
					
				}else {
					logger.debug("not find vipid->"+vipid);
					throw new Exception("会员数据异常，请刷新重试。");
				}
				
				//params.put("args[name]",(nds.util.Validator.isNull(restjo.optString("RELNAME",null))?restjo.optString("NAME",null):restjo.optString("RELNAME",null)));
				params.put("args[name]",restjo.optString("RELNAME",null));
				params.put("args[gender]",restjo.optString("GENDER"));
				params.put("args[birthday]",restjo.optString("BIRTHDAY"));
				params.put("args[contactaddress]",restjo.optString("CONTACTADDRESS"));				
				params.put("args[phonenum]",restjo.optString("PHONENUM"));
				
				params.put("format","JSON");
				params.put("client","");
				params.put("ver","1.0");
				params.put("ts",ts);
				params.put("sig",Sign);
				params.put("method","openCard");
			}else {
				logger.debug("code->"+sp.getCode());
				throw new Exception("validateSing error->"+sp.toString());
			}

			try{
				vh=RestUtils.sendRequest(serverUrl,params,"POST");
			} catch (Throwable tx) {
				logger.debug("ERP网络通信障碍!");
				tx.printStackTrace();
				throw new Exception("ERP网络通信障碍!->"+tx.getMessage());
				//return false;
			}
			
			//int vvid=QueryEngine.getInstance().getSequence("WX_COUPONEMPLOY", conn);
			String result=(String) vh.get("message");
			logger.debug("open offline code result->"+result);
			JSONObject jo=null;
			try {
				jo= new JSONObject(result);
			}catch(Exception e) {
				throw new Exception("线下会员开卡异常，请联系商家");
			}

			//{"result":{"data":{"code":"26f5lb99fr0-0","couponId":"6F5Lb99Fr"},"card":{"balance":0,"level":215,"no":"WX140515000000002","credit":0},"openid":"owAZBuEBBLn-LQ_5ebcbkSh_wFDk","cardid":"37"},"errMessage":"微生活会员开卡成功！","errCode":0}
			
			int insertcount=1;
			if(jo.optInt("errCode",-1)==0) {
				//如果券类型是线上线下，则线下线下同时发券
				if("Y".equalsIgnoreCase(isSendCoupon)) {
					if(!"1".equalsIgnoreCase(couponUseType)) {
						JSONObject vipmessage=jo.optJSONObject("result").optJSONObject("data");
						if(nds.util.Validator.isNotNull(vipmessage.optString("code"))&&nds.util.Validator.isNotNull(vipmessage.optString("couponId"))) {
							String sql="SELECT COUNT(1) FROM wx_couponemploy cm WHERE cm.wx_vip_id=? AND cm.sncode=? AND cm.ad_client_id=?";
							int count=QueryEngine.getInstance().doQueryInt(sql,new Object[] {vipid,vipmessage.optString("code"),ad_client_id},conn);
							
							if(count<=0) {
								String createcopu="INSERT INTO WX_COUPONEMPLOY(ID,AD_CLIENT_ID,AD_ORG_ID,SNCODE,STATE,WX_VIP_ID,ISSUETYPE,WX_COUPON_ID,OWNERID,MODIFIERID,CREATIONDATE,MODIFIEDDATE,USENUM)"+
											   		" SELECT get_Sequences('WX_COUPONEMPLOY'),WC.AD_CLIENT_ID,WC.AD_ORG_ID,?,'N',?,1,WC.ID,WC.OWNERID,WC.MODIFIERID,SYSDATE,SYSDATE,1 FROM WX_COUPON WC"+
											   	" WHERE WC.NUM=? AND WC.AD_CLIENT_ID=?";
								insertcount=QueryEngine.getInstance().executeUpdate(createcopu, new Object[] {vipmessage.optString("code"),vipid,vipmessage.optString("couponId"),ad_client_id},conn);
							}else {
								insertcount=count;
							}
						}
					}else {
						//如果券类型是线上，则只在线上发券
						ArrayList paramss=new ArrayList();
						paramss.add(vipid);
						ArrayList para=new ArrayList();
						para.add(java.sql.Clob.class);
						
						try {
							Collection list=QueryEngine.getInstance().executeFunction("wx_coupon_onlinecoupon",paramss,para);
							String res=(String)list.iterator().next();
							logger.debug("online send coupon result->"+res);
							JSONObject tempjo=new JSONObject(res);
							if(tempjo!=null&&"0".equals(tempjo.optString("code","-1"))){insertcount=1;}
						}catch (QueryException e) {
							logger.debug("online send coupon erroe->"+e.getMessage());
							e.printStackTrace();
							insertcount=0;
						}
					}
				}

				if(insertcount>0&&jo.has("result")&&jo.optJSONObject("result").has("card")&&jo.optJSONObject("result").optJSONObject("card").has("no")) {
					String updatevip="UPDATE wx_vip SET docno=?,vipcardno=?,opencard_status=2 WHERE ID=?";
					insertcount=QueryEngine.getInstance().executeUpdate(updatevip,new Object[] {jo.optJSONObject("result").optJSONObject("card").optString("no"),jo.optJSONObject("result").optJSONObject("card").optString("wshno",vipno),vipid},conn);
					if(insertcount<=0) {
						logger.debug("open offline code update WX_VIP error count->"+insertcount);
						throw new Exception("开卡异常，请重试");
					}else {
						try {conn.commit();}
						catch(Exception e) {
							logger.debug("open offline code commit error->"+e.getLocalizedMessage());
							e.printStackTrace();
							throw new Exception("开卡异常，请重试");
						}
					}
				}else {
					if(insertcount<=0) {
						logger.debug("open offline code insert WX_COUPONEMPLOY error->"+jo.optString("errMessage"));
						throw new Exception("开卡异常，请重试");
					}else {
						logger.debug("open offline code result not find no[线下卡号] error->"+jo.optString("errMessage"));
						throw new Exception("开卡异常，请重试");
					}
				}
				
			}else {
				logger.debug("open offline code error->"+jo.optString("errMessage"));
				throw new Exception("开卡异常，请重试");
			}
				
		}catch(Exception e) {
			e.printStackTrace();
			try {conn.rollback();}
			catch(Exception e1) {e1.printStackTrace();}
			throw new Exception("开卡异常，请重试");
		}finally{
			try {
				conn.setAutoCommit(true);
				conn.close();
			}
			catch(Exception e) {
				logger.debug("开卡FINALLY 异常");
				e.printStackTrace();
				throw new Exception("开卡异常，请重试");
			}
		}
	}
	
	/*绑定
	 * 
	 */
	private void bindCard(JSONObject restjo) throws Exception{
		if(restjo==null||!restjo.has("vipid")||!restjo.has("ad_client_id")||!restjo.has("PHONENUM")) {
			logger.debug("bindCard params error->"+restjo.toString());
			return;
		}
		
		ValueHolder vh=null;
		int vipid=restjo.optInt("vipid");
		int ad_client_id=restjo.optInt("ad_client_id");
		List all=null;
		boolean isErp=false;
		//boolean isSuccessfull=false;
		careid=String.valueOf(ad_client_id);
		String isVerifyCode="N";
		
		
		all=QueryEngine.getInstance().doQueryList("select ifs.erpurl,ifs.username,ifs.iserp,ifs.wxparam,nvl(wc.ismesauth,'N') from WX_INTERFACESET ifs join web_client wc on ifs.ad_client_id=wc.ad_client_id WHERE ifs.ad_client_id="+ad_client_id);
		
		if(all!=null&&all.size()>0) {
			System.out.println("WX_INTERFACESET size->"+all.size());
			serverUrl=(String)((List)all.get(0)).get(0);
			SKEY=(String)((List)all.get(0)).get(3);
			isVerifyCode=String.valueOf(((List)all.get(0)).get(4));
			isErp="Y".equalsIgnoreCase((String)((List)all.get(0)).get(2));
			if(nds.util.Validator.isNull(serverUrl)||nds.util.Validator.isNull(SKEY)) {
				logger.debug("SERVERuRL OR SKEY IS NULL");
				throw new Exception("数据维护异常，请联系商家");
				//return isSuccessfull;
			}
		}else {
			logger.debug("not find WX_INTERFACESET ad_client_id->"+ad_client_id);
			throw new Exception("数据维护异常，请联系商家");
			//return isSuccessfull;
		}
		
		//判断是否需要验证码
		if("Y".equalsIgnoreCase(isVerifyCode)) {
			//判断验证码
			JSONObject vjo=new JSONObject();
			try {
				JSONObject pjo=new JSONObject();
				pjo.put("vipid", restjo.optInt("vipid",0));
				pjo.put("verifycode", restjo.optString("verifycode"));
				vjo.put("params", pjo);
			} catch (Exception e) {
				
			}
			
			try {
				ClientControllerWebImpl controller=(ClientControllerWebImpl)WebUtils.getServletContextManager().getActor(nds.util.WebKeys.WEB_CONTROLLER);
				DefaultWebEvent event=new DefaultWebEvent("CommandEvent");
				event.put("jsonObject", vjo);
				event.setParameter("command", "nds.weixin.ext.ValidationVipVerifycodeCommand");
				vh=controller.handleEvent(event);
			}catch(Exception e) {
				logger.debug("open card verify code error->"+e.getLocalizedMessage());
				throw new Exception("验证异常");
			}
			
			if(vh==null||!"0".equalsIgnoreCase(String.valueOf(vh.get("code")))) {
				logger.debug("open card verify code result error->");
				if(vh==null) {throw new Exception("验证异常");}
				else{throw new Exception(String.valueOf(vh.get("message")));}
			}
		}
		
		
		if(!isErp) {
			logger.debug("未接通ERP");
			return;
		}
		
		List al=null;
		SipStatus sp=validateSing();
		HashMap<String, String> params =new HashMap<String, String>();
		
		if(sp.getCode().equals("0")){
			al = QueryEngine.getInstance().doQueryList("select vp.wechatno,vp.vipcardno,vb.code from wx_vip vp,wx_vipbaseset vb WHERE vp.id=? and vp.viptype=vb.id",new Object[] {vipid});
			
			if(al!=null&&al.size()>0) {
				logger.debug("vip size->"+al.size());
				
				params.put("args[openid]", (String) ((List)al.get(0)).get(0));
				params.put("args[cardid]",String.valueOf(ad_client_id));
				params.put("args[wshno]","");
				params.put("args[name]",restjo.optString("RELNAME"));
				params.put("args[phonenum]",restjo.optString("PHONENUM"));
				params.put("args[email]","");
				params.put("args[idno]","");
				params.put("args[cardno]",restjo.optString("DOCNO"));
				params.put("args[cardpwd]",restjo.optString("VIPPASSWORD"));
				params.put("args[viptype]",(String)((List)al.get(0)).get(2));
				params.put("args[cardpin]","");
				//params.put("args[isCodeRepeat]",String.valueOf(Tools.getInt(((List)al.get(0)).get(5), 0)));
			}else {
				logger.debug("bind offline code error not find vip->"+vipid);
				throw new Exception("会员异常，请重试。");
				//return isSuccessfull;
			}
			params.put("format","JSON");
			params.put("client","");
			params.put("ver","1.0");
			params.put("ts",ts);
			params.put("sig",Sign);
			params.put("method","bindCard");
		}

		try{
		 vh=RestUtils.sendRequest(serverUrl,params,"POST");
		} catch (Throwable tx) {
			logger.debug("ERP网络通信障碍!");
			tx.printStackTrace();
			throw new Exception("ERP网络通信障碍");
		}
		String result=(String) vh.get("message");
		logger.debug("bind offline code result->"+result);
		JSONObject jo=null;
		try {
			jo= new JSONObject(result);
		}catch(Exception e) {
			throw new Exception("线下会员绑卡异常，请联系商家");
		}
		int recode=jo.optInt("errCode");
		if(recode!=0) {
			throw new Exception("绑卡异常，请重试");
		}else {
			String sql="";
			int viptypeid=0;
			jo=jo.optJSONObject("result");
			JSONObject cardjo=jo.optJSONObject("card");
			//{"errCode":0,"errMessage":"\u5FAE\u751F\u6D3B\u4F1A\u5458\u7ED1\u5361\u6210\u529F\uFF01","result":{"openid":"o8rO7t2F5lwDQWAN8m0hUHEnRPWY","cardid":"91","card":{"level":"800000000002","no":"WX1400000003","credit":11417,"balance":5555}}}
			sql="select vb.id from wx_vipbaseset vb WHERE vb.code=? AND vb.ad_client_id=?";
			   try {
			   		viptypeid=QueryEngine.getInstance().doQueryInt(sql, new Object[] {cardjo.optString("level"),ad_client_id});
			   		if(viptypeid<=0) {
			   			logger.debug("bind offline code error not find viptype->"+cardjo.optString("level"));
						throw new Exception("绑卡异常，请重试");
			   		}
			   }catch(Exception e) {
				   logger.debug("bind offline code error not find viptype->"+cardjo.optString("level"));
				   e.printStackTrace();
				   throw new Exception("绑卡异常，请重试");
			   }
			//sql="UPDATE wx_vip v SET integral=?,lastamt=?,viptype=? WHERE v.id=?";
			  sql="UPDATE WX_VIP V SET V.INTEGRAL=?,V.LASTAMT=?,V.VIPTYPE=?,V.DOCNO=?,V.RELNAME=?,V.EMAIL=?,V.PHONENUM=?,V.STORE_ID=?,V.CONTACTADDRESS=?,V.BIRTHDAY=?,V.GENDER=?,V.PROVINCE=?,V.CITY=?,V.AREA=?"+
				  " WHERE V.ID=?";
			int count=0;
			count=QueryEngine.getInstance().executeUpdate(sql,new Object[] {cardjo.optInt("credit"),cardjo.optDouble("balance"),viptypeid,cardjo.optString("no"),cardjo.optString("name",""),cardjo.optString("email",null),cardjo.optString("phonenum",null),cardjo.optInt("store_id",0),cardjo.optString("address",null),cardjo.optString("birthday"),cardjo.optString("gender"),cardjo.optString("province",null),cardjo.optString("city",null),cardjo.optString("depart",null),vipid});
			if(count<=0) {
				logger.debug("bind offline code update vip error->"+count);
				throw new Exception("绑卡异常，请重试");
			}
			/*sql="UPDATE WX_VIPINFO V SET V.isopencard=2,V.VIPTYPE_ID=?,v.relname=?,v.email=?,v.phonenum=?,v.otheraddress=?,v.birthday=?,v.gender=?,v.province=?,v.city=?,v.regionid=?"+
				  " WHERE v.wx_vip_id=?";
			count=QueryEngine.getInstance().executeUpdate(sql,new Object[] {viptypeid,cardjo.optString("name",null),cardjo.optString("email",null),cardjo.optString("phonenum",null),cardjo.optString("address",null),cardjo.optString("birthday"),cardjo.optString("gender"),cardjo.optString("province",null),cardjo.optString("city",null),cardjo.optString("depart",null),vipid});
			if(count<=0) {
				logger.debug("bind offline code update vipinfo error->"+count);
				throw new Exception("绑卡异常，请重试");
			}*/
			restjo.put("DOCNO", cardjo.optString("no",null));
			restjo.put("PHONENUM",cardjo.optString("phonenum",null));
		}

		//return isSuccessfull;
	}
	
	/* 用户卡信息更新接口可以被商家调用来更新用户微生活会员卡的信息。
	 * 当用户在商家的 CRM 系统里的 用户等级、积分或余额发生变化时,
	 * 商家通过调用该接口可以将变更的用户数据信息和 openid(微信用户 标识)传递给微生活,微生活展示在会员卡 Html5 页面,方便查看。
	 * @param objectid 调整单id
	 * @return 
	 * @throws Exception
	 */
	private void updateCard(JSONObject restjo) throws Exception{
		ValueHolder vh=null;
		if(restjo==null||!restjo.has("vipid")||!restjo.has("ad_client_id")||!restjo.has("RELNAME")||!restjo.has("GENDER")||!restjo.has("BIRTHDAY")||!restjo.has("PHONENUM")) {
			logger.debug("updateCard params error->"+restjo.toString());
			return;
		}
		
		String isVerifyCode="N";
		List all=null;
		boolean isErp=false;
		int vipid=restjo.optInt("vipid");
		int ad_client_id=restjo.optInt("ad_client_id");
		careid=String.valueOf(ad_client_id);
		
		all=QueryEngine.getInstance().doQueryList("select ifs.erpurl,ifs.username,ifs.iserp,ifs.wxparam,nvl(wc.ismesauth,'N') from WX_INTERFACESET ifs join web_client wc on ifs.ad_client_id=wc.ad_client_id WHERE ifs.ad_client_id="+ad_client_id);
		
		if(all!=null&&all.size()>0) {
			System.out.println("WX_INTERFACESET size->"+all.size());
			serverUrl=(String)((List)all.get(0)).get(0);
			SKEY=(String)((List)all.get(0)).get(3);
			isVerifyCode=String.valueOf(((List)all.get(0)).get(4));
			isErp="Y".equalsIgnoreCase((String)((List)all.get(0)).get(2));
			if(isErp&&nds.util.Validator.isNull(serverUrl)||nds.util.Validator.isNull(SKEY)) {
				logger.debug("SERVERuRL OR SKEY IS NULL");
				throw new Exception("数据维护异常，请联系商家");
				//return isSuccessfull;
			}
		}else {
			logger.debug("not find WX_INTERFACESET ad_client_id->"+ad_client_id);
			throw new Exception("数据维护异常，请联系商家");
			//return isSuccessfull;
		}
		
		//判断是否需要验证码
		if("Y".equalsIgnoreCase(isVerifyCode)) {
			//判断验证码
			JSONObject vjo=new JSONObject();
			try {
				JSONObject pjo=new JSONObject();
				pjo.put("vipid", restjo.optInt("vipid",0));
				pjo.put("verifycode", restjo.optString("verifycode"));
				vjo.put("params", pjo);
			} catch (Exception e) {
				
			}
			
			try {
				ClientControllerWebImpl controller=(ClientControllerWebImpl)WebUtils.getServletContextManager().getActor(nds.util.WebKeys.WEB_CONTROLLER);
				DefaultWebEvent event=new DefaultWebEvent("CommandEvent");
				event.put("jsonObject", vjo);
				event.setParameter("command", "nds.weixin.ext.ValidationVipVerifycodeCommand");
				vh=controller.handleEvent(event);
			}catch(Exception e) {
				logger.debug("open card verify code error->"+e.getLocalizedMessage());
				throw new Exception("验证异常");
			}
			
			if(vh==null||!"0".equalsIgnoreCase(String.valueOf(vh.get("code")))) {
				logger.debug("open card verify code result error->");
				if(vh==null) {throw new Exception("验证异常");}
				else{throw new Exception(String.valueOf(vh.get("message")));}
			}
		}
		
		if(!isErp) {
			logger.debug("未接通ERP");
			return;
		}
		
		SipStatus sp=validateSing();
		HashMap<String, String> params =new HashMap<String, String>();

		List al = QueryEngine.getInstance().doQueryList("select vp.wechatno,vs.code,vp.vipcardno from wx_vip vp,wx_vipbaseset vs WHERE vp.id=? AND vp.viptype=vs.id",new Object[] {vipid});
		if(al!=null&&al.size()>0) {
			//"BIRTHDAY":"19340202","NAME":"jackrain","PHONENUM":"18005695669","CONTACTADDRESS":"天津市辖区和平区中国上海徐汇","GENDER":"1"
			params.put("args[openid]", (String) ((List)al.get(0)).get(0));
			params.put("args[cardid]",String.valueOf(ad_client_id));
			params.put("args[wshno]","");
			params.put("args[name]",restjo.optString("RELNAME"));
			params.put("args[gender]",restjo.optString("GENDER"));
			params.put("args[birthday]",restjo.optString("BIRTHDAY"));
			params.put("args[contactaddress]",restjo.optString("CONTACTADDRESS"));				
			params.put("args[phonenum]",restjo.optString("PHONENUM"));
			params.put("args[viptype]", (String) ((List)al.get(0)).get(1));
			params.put("args[email]","");
			params.put("args[idno]","");
		}
		params.put("format","JSON");
		params.put("client","");
		params.put("ver","1.0");
		params.put("sig",Sign);
		params.put("ts",ts);
		params.put("method","updateUserInfo");

		
		try{
			vh=RestUtils.sendRequest(serverUrl,params,"POST");
		} catch (Throwable tx) {
			logger.debug("微生活网络通信障碍! "+tx.getMessage());
			tx.printStackTrace();
			throw new Exception("数据维护异常，请联系商家");
			//return;
		}
		String result=(String) vh.get("message");
		logger.debug("update offline result->"+result);
		
		JSONObject jo=null;
		try {
			jo= new JSONObject(result);
		}catch(Exception e) {
			throw new Exception("线下同步会员信息异常，请联系商家");
		}
		int recode=jo.optInt("errCode");
		if(recode!=0) {
			throw new Exception("数据维护异常，请联系商家");
		}
	}
	
	/*
	 * 积分兑换
	 */
	private void integralExchange(JSONObject restjo) throws Exception{
		if(restjo!=null&&!restjo.has("query")) {
			logger.debug("integralExchange params error->"+restjo.toString());
			return;
		}
		JSONObject tjo=restjo.optJSONObject("query");
		if(tjo==null||!tjo.has("cardno")||!tjo.has("docno")||!tjo.has("integral")) {
			logger.debug("integralExchange params error->"+tjo.toString());
			return;
		}
		
		List all=null;
		int ad_client_id=tjo.optInt("ad_client_id");
		careid=String.valueOf(ad_client_id);
		boolean isErp=false;
		
		all=QueryEngine.getInstance().doQueryList("select ifs.erpurl,ifs.username,ifs.iserp,ifs.wxparam from WX_INTERFACESET ifs WHERE ifs.ad_client_id="+ad_client_id);
		
		if(all!=null&&all.size()>0) {
			System.out.println("WX_INTERFACESET size->"+all.size());
			serverUrl=(String)((List)all.get(0)).get(0);
			SKEY=(String)((List)all.get(0)).get(3);
			isErp="Y".equalsIgnoreCase((String)((List)all.get(0)).get(2));
			if(isErp && nds.util.Validator.isNull(serverUrl)||nds.util.Validator.isNull(SKEY)) {
				logger.debug("SERVERuRL OR SKEY IS NULL");
				throw new Exception("数据维护异常，请联系商家");
				//return isSuccessfull;
			}
		}else {
			logger.debug("not find WX_INTERFACESET");
			throw new Exception("数据维护异常，请联系商家");
			//return isSuccessfull;
		}
		
		if(!isErp) {
			logger.debug("未接通ERP");
			return;
		}
		
		SipStatus sp=validateSing();
		HashMap<String, String> params =new HashMap<String, String>();
		params.put("args[cardno]", tjo.optString("cardno"));
		params.put("args[docno]", tjo.optString("docno"));
		params.put("args[integral]", tjo.optString("integral"));
		
		params.put("format","JSON");
		params.put("client","");
		params.put("ver","1.0");
		params.put("sig",Sign);
		params.put("ts",ts);
		params.put("method","integralExchange");
		
		ValueHolder vh=null;
		try{
			vh=RestUtils.sendRequest(serverUrl,params,"POST");
		} catch (Throwable tx) {
			logger.debug("微生活网络通信障碍! "+tx.getMessage());
			tx.printStackTrace();
			throw new Exception("数据维护异常，请联系商家");
			//return;
		}
		String result=(String) vh.get("message");
		logger.debug("integralExchange offline result->"+result);
		JSONObject jo= new JSONObject(result);
		int recode=jo.optInt("errCode");
		if(recode!=0) {
			logger.debug("integralExchange offline error->"+jo.optString("errMessage"));
			throw new Exception("积分兑换异常，请联系商家");
		}else {
			tjo.put("availableintegral", jo.optInt("integral",0));
			//restjo.optJSONObject("query").put("availableintegral", jo.optInt("integral",0));
		}
	}
	
	private SipStatus validateSing() throws Exception{
		ts=String.valueOf(System.currentTimeMillis());
    	
    	
    	logger.debug("cardid=" + careid + ",ts=" + ts + ",skey="+ SKEY);
    	if (nds.util.Validator.isNull(careid)) {return SipStatus.PARAM_ERROR;}
		if (nds.util.Validator.isNull(ts)) {return SipStatus.PARAM_ERROR;}
		if (nds.util.Validator.isNull(SKEY)) {return SipStatus.PARAM_ERROR;}
		Sign = nds.util.MD5Sum.toCheckSumStr(careid + ts+ SKEY);
		logger.debug("passwdsign=" + Sign);			
		return SipStatus.SUCCESS;
	}
}
