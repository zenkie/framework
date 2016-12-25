package nds.web.action;

import java.util.*;

import org.json.JSONArray;
import org.json.JSONObject;

import nds.schema.*;
import nds.security.NDSSecurityException;
import nds.control.web.*;
import nds.util.*;
import nds.query.*;

/**
 * For node in ad_tablecategory tree
 * 
 * @author yfzhu
 * 
 */
public class TreeNodeAction extends WebActionImpl {

	public String toHTML(Locale locale, Map env) {
		// <tree icon="/html/nds/images/table.gif"
		// text="<%=StringUtils.escapeForXML(tdesc)%>"
		// action="javascript:pc.navigate('<%=tableId%>')"/>

		StringBuffer sb = new StringBuffer();
		WebAction.ActionTypeEnum ate = this.getActionType();

		// xml in content, anything that concord with xloadtree.js spec, can
		// contain multiple nodes
		if (ate.equals(WebAction.ActionTypeEnum.URL)
				&& this.getScript().startsWith("<")) {
			sb.append(this.getScript());

		} else if (ate.equals(WebAction.ActionTypeEnum.URL)
				&& this.getScript().startsWith("[")) {
            //扩展env 环境变量添加USERWEB request
			if (env == null) {
				throw new NDSRuntimeException(
						"Must have env set for ad_action id =" + this.id);
			}
			UserWebImpl userWeb = ((UserWebImpl) env.get("userweb"));
			if (userWeb == null) {
				throw new NDSRuntimeException("Must have userWeb set in env");
			}
			try {
				sb.append(writeXmlFromJSONArray(
						new JSONArray(this.getScript()), locale, userWeb));
			} catch (Throwable paramLocale) {
				this.logger.error("fail to parse tree script", paramLocale);
			}
		} else {
			// create just one node
			sb.append("<tree icon=\"");
			String s = this.getIconURL();
			if (nds.util.Validator.isNull(s)) {
				s = ((Configurations) WebUtils.getServletContextManager()
						.getActor(nds.util.WebKeys.CONFIGURATIONS))
						.getProperty("webaction.treenode.iconurl",
								"/html/nds/images/table.gif");

			}
			sb.append(s).append("\" text=\"")
					.append(StringUtils.escapeForXML(this.getDescription()))
					.append("\" action=\"");

			String target = this.getUrlTarget();
			switch (ate) {
			case URL:
				// locate to div id = "portal-content" if not set target
				if (nds.util.Validator.isNull(target))
					target = "portal-content";
				if (target.startsWith("_")) {
					sb.append(this.getScript()).append("\" target=\"")
							.append(this.getUrlTarget()).append("\"/>");
				} else {
					sb.append("javascript:pc.navigate('")
							.append(this.getScript()).append("','")
							.append(this.getUrlTarget()).append("')\"/>");
				}
				break;
			case JavaScript:
				sb.append("javascript:").append(this.getScript())
						.append("\"/>");
				break;
			case AdProcess:
				// get ad_process.id according to process name
				try {
					int processId = Tools.getInt(
							QueryEngine.getInstance().doQueryOne(
									"select id from ad_process where name="
											+ QueryUtils.TO_STRING(this
													.getScript())), -1);
					if (processId == -1)
						throw new NDSException("ad_process with name "
								+ this.getScript() + " not found");
					sb.append(
							"javascript:showObject('/html/nds/schedule/addpi.jsp?id=")
							.append(processId).append("')\"/>");
				} catch (Throwable t) {
					sb.append("javascript:alert('Error:")
							.append(t.getMessage()).append("')\"/>");
				}
				break;
			case StoredProcedure:
			case BeanShell:
			case Python:
			case OSShell:
				sb.append("javascript:pc.webaction(").append(this.getId())
						.append(",");
				if (this.getComments() != null)
					sb.append("\"").append(this.getComments()).append("\"");
				else
					sb.append("null,");

				if (nds.util.Validator.isNotNull(target)) {
					;
					sb.append("\"").append(target).append("\"");
				} else {
					sb.append("null");
				}
				sb.append(")\"/>");
				break;

			default:
				sb.append("javascript:").append(this.getScript())
						.append("\"/>");

			}
		}

		return sb.toString();
	}

	private StringBuffer writeXmlFromJSONArray(JSONArray jsonArray,
			Locale locale, UserWebImpl userweb) throws Exception {
		// TODO Auto-generated method stub
		// 根据界面的JSON 数组 生成对应的JSONOBJEC
		StringBuffer stringbuffer = new StringBuffer();
		for (int i = 0; i < jsonArray.length(); i++) {
			JSONObject jsonobject = jsonArray.getJSONObject(i);
			stringbuffer.append(writeXmlFromJSON(jsonobject, locale, userweb));
		}
		return stringbuffer;
	}

	private StringBuffer writeXmlFromJSON(JSONObject jo, Locale local,
			UserWebImpl userweb) throws Exception {
		// TODO Auto-generated method stub
		// 在JSONOBJECT 中解析XTREE XML 结构体
		StringBuffer xtree_xml = new StringBuffer();
		Object text_lable = MessagesHolder.getInstance().getMessage4(local,jo.optString("text"));
		String ico_png;// 定义标签图片
		if ((Validator.isNotNull(ico_png = jo.optString("icon")))
				&& (!ico_png.startsWith("http:")) && (!ico_png.startsWith("/"))) {
			ico_png = "/html/nds/images/" + ico_png;
		}
		// 判断权限 对应的权限规则
		if (!hasPermission(jo.optJSONObject("perm"), userweb))
			return xtree_xml;
		Object folder;// 所说下拉类别菜单分类
		// Object table;
		//
		if ((folder = jo.optJSONArray("folder")) != null) {
			if (Validator.isNull(ico_png))
				ico_png = "/html/nds/js/xloadtree111/images/xp/folder.png";
			if (Validator.isNull((String) text_lable))
				text_lable = "Folder";
			folder = writeXmlFromJSONArray((JSONArray) folder,
					local, userweb).toString();
			if (folder != null&&(folder.toString().length() != 0)&&(folder.toString()
					.trim().length() != 0))

			{
				xtree_xml.append("<tree icon=\"").append(ico_png)
						.append("\" text=\"").append((String) text_lable)
						.append("\">");
				xtree_xml.append((String) folder);
				xtree_xml.append("</tree>");
			}

		} else if (Validator.isNotNull(jo.optString("table")))
		// 判断table 标签
		{

			Table table = TableManager.getInstance().findTable(
					jo.optString("table"));
			if (table != null) {
				String action = jo.optString("action");
				int i = 1;
				if ("A".equals(action))
					i = 3;
				try {
					userweb.checkPermission(table.getSecurityDirectory(), i);
					if (Validator.isNull((String) text_lable))
						text_lable = table.getDescription(local);
					if (Validator.isNull(ico_png))
						ico_png = "/html/nds/images/table.gif";
					if ("A".equals(action)) {
						action = "dlgo("
								+ (String.valueOf(table.getId()) + ",-1)");
					} else
						action = "pc.navigate('"
								+ (String.valueOf(table.getId()) + "')");

					xtree_xml.append("<tree icon=\"").append(ico_png)
							.append("\" text=\"").append((String) text_lable)
							.append("\" action=\"javascript:").append(action)
							.append("\"/>");
				} catch (NDSSecurityException e) {
				}
			}
		} else if (Validator.isNotNull(jo.optString("rpt")))
		// 判断报表标签RPT
		{
			String rpt_json = jo.optString("rpt");
			List rpt_list = QueryEngine.getInstance().doQueryList(
					"select ad_table_id,name from ad_cxtab where name="
							+ rpt_json + " and ad_client_id="
							+ Integer.valueOf(userweb.getAdClientId())
							+ " and isactive='Y'");
			if ((rpt_list != null) && (rpt_list.size() > 0)) {
				try {
					Table table = TableManager.getInstance().findTable(
							((List) rpt_list.get(0)).get(0));
					String cxtab_name = (String) ((List) rpt_list.get(0))
							.get(1);
					userweb.checkPermission(table.getSecurityDirectory(), 1);
					if (Validator.isNull((String) text_lable))
						text_lable = cxtab_name;
					if (Validator.isNull(ico_png))
						ico_png = "/html/nds/images/jrpt.gif";
					String action = "pc.qrpt('" + cxtab_name + "')";
					xtree_xml.append("<tree icon=\"").append(ico_png)
							.append("\" text=\"").append((String) text_lable)
							.append("\" action=\"javascript:").append(action)
							.append("\"/>");
				} catch (NDSSecurityException e) {

				} catch (Throwable e) {
					this.logger.error("Found error for rpt check:" + rpt_json,
							e);
				}
			}

		} else if (Validator.isNotNull(jo.optString("script"))) {
			// 判断标间是否为JS 脚本

			if (Validator.isNull((String) text_lable))
				text_lable = "Script";
			if (Validator.isNull(ico_png))
				ico_png = "/html/nds/images/table.gif";
			xtree_xml.append("<tree icon=\"").append(ico_png)
					.append("\" text=\"").append((String) text_lable)
					.append("\" action=\"javascript:")
					.append(jo.optString("script")).append("\"/>");
		}

		return xtree_xml;

	}

	//
	// 跟据传入的JSON 对象判断其对应的权限
	private boolean hasPermission(JSONObject jo, UserWebImpl userweb)
			throws Exception {
		if (jo == null)
			return true;

		int i = 0; // 组别权限判断
		int j = 0; // 表MASK 权限判断
		int k = 0;// 定义SQL 语句判断
		String v_group = jo.optString("group");
		String v_table = jo.optString("table");
		String v_sql = jo.optString("sql");
		int n;
		if (Validator.isNotNull(v_group)) {
			String[] groups = v_group.split(";");
			for (int m = 0; m < groups.length; m++) {
				if (Tools
						.getInt(QueryEngine
								.getInstance()
								.doQueryOne(
										"select gu.id from groupuser gu, groups g where g.name="
												+ groups[m]
												+ " and gu.groupid=g.id and gu.userid=?"
												+ Integer.valueOf(userweb
														.getUserId())), -1) != -1) {
					i = 1;
					break;
				}
			}
		} else {
			i = 1;
		}
		if (i != 0) {
			if (Validator.isNotNull(v_table)) {
				Table table = TableManager.getInstance().findTable(v_table);
				if (table == null) {
					this.logger.error("Fail to load table from perm of obj:"
							+ jo);
					throw new Error("Fail to load table from perm of obj:" + jo);
				}
				String str3 = jo.optString("action", "Q");
				n = 1;
				for (int index = 0; index < str3.length(); index++) {
					// 对传入的多个MASK的属性判断 是否有权限
					int mask = TableImpl.getDirectoryPermissionByMask(str3
							.charAt(index));
					if (!userweb.isPermissionEnabled(
							table.getSecurityDirectory(), mask)) {
						n = 0;
						break;
					}
				}
				j = n;
			} else {
				j = 1;
			}
		}
		if ((i != 0) && (j != 0)) {
			if (Validator.isNotNull(v_sql)) {
				// 替代变量
				String psql = QueryUtils.replaceVariables(v_sql,
						userweb.getSession());
				this.logger.debug("webaction jsonobject psql   " + psql);
				k = QueryEngine.getInstance().doQueryOne(psql) != null ? 1 : 0;
			} else {
				k = 1;
			}
		}
		return (i != 0) && (j != 0) && (k != 0);
	}

	@Override
	public String toHREF(Locale locale, Map env) {
		// TODO Auto-generated method stub
		return null;
	}

}
