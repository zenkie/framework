package nds.control.ejb.command;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import nds.control.ejb.Command;
import nds.control.event.DefaultWebEvent;
import nds.control.event.NDSEventException;
import nds.control.util.ValueHolder;
import nds.query.QueryEngine;
import nds.schema.TableManager;
import nds.util.MessagesHolder;
import nds.util.NDSException;
import nds.util.Tools;

import org.directwebremoting.WebContext;
import org.json.JSONObject;
import nds.security.User;

public class VAR_CLIENT_CHARGE extends Command {

	public ValueHolder execute(DefaultWebEvent event) throws RemoteException,
			NDSException {

		logger.debug(event.toDetailString());

		TableManager manager = TableManager.getInstance();
 
		JSONObject jo = (JSONObject) event.getParameterValue("jsonObject");
		JSONObject params = null, params2 = null;
		java.util.Locale locale = event.getLocale();
		QueryEngine engine = QueryEngine.getInstance();
		Connection conn = engine.getConnection();
		PreparedStatement pstmt = null;
		String email;
		//Boolean flag = false;
		String docno = "";
		String name = "";
		HttpServletRequest request = null;
		List exist_client = null;
		int e_orderid = 0;
		User usr = helper.getOperator(event);
		int userid = usr.getId();
		String state = "D";
		int agentId=-1;
		try {
			params2 = jo.getJSONObject("params");
			email = params2.getString("email");
			email = email.replaceAll(" ", "");
			name = params2.getString("companyname");
			name = name.replaceAll(" ", "");
			String userValidCode = params2.getString("verifyCode");
			WebContext ctx = (WebContext) jo
					.opt("org.directwebremoting.WebContext");
			if (ctx != null) {
				request = ctx.getHttpServletRequest();
				String serverValidCode = (String) request.getSession()
						.getAttribute("nds.control.web.ValidateMServlet");
				if (serverValidCode.equalsIgnoreCase(userValidCode)) {

				} else {
					throw new NDSEventException("@error-verify-code@");
				}
			}
			exist_client = QueryEngine.getInstance().doQueryList("select ad_client_id, ad_org_id,agent_id,c_bpartner_id,area,phone,mobile,truename from e_client where email='"+ email + "'and name='" + name + "'");
			if (exist_client.size() == 0) {
				throw new NDSEventException("公司与邮箱不匹配！");
			}
		} catch (Throwable th) {
			try {
				conn.close();
			} catch (Exception e) {
			}
			if (th instanceof NDSException)
				throw (NDSException) th;
			logger.error("exception", th);
			throw new NDSException(th.getMessage(), th);
		}

		try {
			params = jo.getJSONObject("params");
			//int peaple_fee= params.getInt("peaple_fee");// 购买人月
			
			// 根据
			
			// email=params.getString("email");
			// name=params.getString("companyname");
			// exist_client= QueryEngine.getInstance().doQueryList("select
			// ad_client_id,
			// ad_org_id,agent_id,c_bpartner_id,area,phone,mobile,truename from
			// e_client where email='"+email+"'and name='"+name+"'" );
			if (userid != nds.control.web.UserWebImpl.GUEST_ID) {
				state = "M";
			}
			agentId= Tools.getInt(((List) exist_client.get(0)).get(2), -1);
			int price = nds.var.VARUtils.getProductPriceByAgentId(agentId);//代理商的零售价
			int amt= price * params.getInt("peaple_fee");// 客户应该支付的总价
			
				e_orderid = QueryEngine.getInstance().getSequence("e_order",conn);
				pstmt = conn.prepareStatement("insert into e_order(id,ad_client_id, ad_org_id,docno,doctype,amt,c_bpartner_id,payer_id,pname,pemail,parea,pphone,pmobile,ptruename,state, ownerid, modifierid, creationdate,modifieddate,isactive) values(?,?,?,Get_SequenceNo('VAR',?),?,?,?,?,?,?,?,?,?,?,'N',?,?,sysdate,sysdate,'Y')");
				pstmt.setInt(1, e_orderid);
				pstmt.setInt(2, Tools.getInt(((List) exist_client.get(0)).get(0), -1));
				pstmt.setInt(3, Tools.getInt(((List) exist_client.get(0)).get(1), -1));
				pstmt.setInt(4, Tools.getInt(((List) exist_client.get(0)).get(0), -1));
				pstmt.setString(5, state);
				
				
				pstmt.setInt(6, Tools.getInt(amt, -1));
				pstmt.setInt(7, Tools.getInt(((List) exist_client.get(0)).get(2), -1));
				pstmt.setInt(8, Tools.getInt(((List) exist_client.get(0)).get(3), -1));
				pstmt.setString(9, name);
				pstmt.setString(10, email);
				pstmt.setString(11, (String) ((List) exist_client.get(0)).get(4));
				pstmt.setString(12, (String) ((List) exist_client.get(0)).get(5));
				pstmt.setString(13, (String) ((List) exist_client.get(0)).get(6));
				pstmt.setString(14, (String) ((List) exist_client.get(0)).get(7));
				pstmt.setInt(15,userid);
				pstmt.setInt(16,userid);
				pstmt.executeUpdate();
			
			//docno = (String) QueryEngine.getInstance().doQueryOne("select docno from e_order where id=" + e_orderid);
			/*if (userid != nds.control.web.UserWebImpl.GUEST_ID) {
				int c_bpartner_id = Tools.getInt(QueryEngine.getInstance()
						.doQueryOne(
								"select c_bpartner_id from users where id="
										+ userid), -1);
				ArrayList params1 = new ArrayList();
				params1.add(docno);

				QueryEngine.getInstance().executeStoredProcedure(
						"E_ORDER_CHECK_BALANCE", params1, false, conn);
			}*/
			ValueHolder holder = new ValueHolder();
			holder.put("message", nds.util.MessagesHolder.getInstance().translateMessage("@complete@", locale));
			holder.put("code", "0");
			JSONObject returnObj = new JSONObject();
			returnObj.put("url", "/var/order_pay.jsp?id=" + e_orderid+ "&isvar=" + (params.optString("isvar")));
			holder.put("data", returnObj);
			return holder;

		} catch (Throwable th) {
			if (th instanceof NDSException)
				throw (NDSException) th;
			logger.error("exception", th);
			throw new NDSException(th.getMessage(), th);
		} finally {
			try {
				pstmt.close();
			} catch (Exception ea) {
			}
			try {
				conn.close();
			} catch (Exception e) {
			}
		}
	}
}
