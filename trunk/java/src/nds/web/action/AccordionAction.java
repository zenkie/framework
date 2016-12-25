package nds.web.action;

import java.util.*;

import org.json.JSONArray;
import org.json.JSONObject;

import nds.schema.*;
import nds.security.NDSSecurityException;
import nds.control.web.*;
import nds.util.*;
import nds.log.Logger;
import nds.query.*;
//����JSON ����Accordion �۵��˵�
public class AccordionAction extends WebActionImpl {
	private static int Accordnum=0;  
	
	public String toHTML(Locale locale,Map env) {
        /**
         * <div>
         * <h3><a>head name</a></h3>
         * <div style="height:300px;max-height:269px"> max 12 div
         * tabimg="<img src=\""+StringUtils.escapeForXML(table.getAccordico())+"\" style=\"height:16px;width:20px;\"></img>";
         * <div class="accordion_headings" onclick="javascript:pc.navigate('15000')">tabimg<a>�ɹ��˼۵�</a></div>
         * </div>
         * </div>
         * one action simaple
         */
		StringBuffer sb = new StringBuffer();
		WebAction.ActionTypeEnum ate = this.getActionType();

		// xml in content, anything that concord with xloadtree.js spec, can
		// contain multiple nodes
			if (ate.equals(WebAction.ActionTypeEnum.URL)
				&& this.getScript().startsWith("[")) {
            //��չenv �����������USERWEB request
			
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
		}else{
			throw new NDSRuntimeException("Must have first char is [");
		}
			//TODO ������������洫��ķ�JSON�����ʱ��
			//����ACTION ������ ����һ��JSONOBJECT ��������Ӧ�Ľڵ�
		return sb.toString();
	}
	
	//
	// ���ݴ����JSON �����ж����Ӧ��Ȩ��
	private boolean hasPermission(JSONObject jo, UserWebImpl userweb)
			throws Exception {
		if (jo == null)
			return true;

		int i = 0; // ���Ȩ���ж�
		int j = 0; // ��MASK Ȩ���ж�
		int k = 0;// ����SQL ����ж�
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
					// �Դ���Ķ��MASK�������ж� �Ƿ���Ȩ��
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
				// �������
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

	
	private StringBuffer writeXmlFromJSONArray(JSONArray jsonArray,
			Locale locale, UserWebImpl userweb) throws Exception {
		// TODO Auto-generated method stub
		// ���ݽ����JSON ���� ���ɶ�Ӧ��JSONOBJEC
		StringBuffer stringbuffer = new StringBuffer();
		Accordnum= jsonArray.length();
		for (int i = 0; i < jsonArray.length(); i++) {
			JSONObject jsonobject = jsonArray.getJSONObject(i);
			stringbuffer.append(writeXmlFromJSON(jsonobject, locale, userweb));
		}
		return stringbuffer;
	}

	private Object writeXmlFromJSON(JSONObject jo, Locale local,
			UserWebImpl userweb)throws Exception  {
		// TODO Auto-generated method stub
		
		// ��JSONOBJECT �н���Accordion XML �ṹ��
		StringBuffer accord_xml = new StringBuffer();
		//Object text_lable = jo.optString("text");
		Object text_lable = MessagesHolder.getInstance().getMessage4(local,jo.optString("text"));
		String ico_png;// �����ǩͼƬ
		boolean showico;
		//tabimg="<img src=\""+StringUtils.escapeForXML(table.getAccordico())+"\" style=\"height:16px;width:20px;\"></img>";
		//���Ӳ����ж��Ƿ���ʾICO ���ӽ���װ���ٶ�
		showico=Tools.getYesNo(userweb.getUserOption("showico","Y"),false);
		logger.debug("showico ->"+userweb.getUserOption("showico","Y"));
		logger.debug("showico ->"+showico);
		if ((Validator.isNotNull(ico_png = jo.optString("icon")))
				&& (!ico_png.startsWith("http:")) && (!ico_png.startsWith("/"))) {
			ico_png = "<img src=\"/html/nds/images/" + ico_png+"\" style=\"height:16px;width:16px;vertical-align:bottom;padding-right: 4px;\"></img>";
		}
		//System.out.println(showico);
		if(!showico){
			ico_png=" ";
		}
		// �ж�Ȩ�� ��Ӧ��Ȩ�޹���
		if (!hasPermission(jo.optJSONObject("perm"), userweb))
			return accord_xml;
		Object folder;// ��˵�������˵�����
		// Object table;
		//
		if ((folder = jo.optJSONArray("folder")) != null) {
			if (Validator.isNull(ico_png))
				ico_png = " ";
			if (Validator.isNull((String) text_lable))
				text_lable = MessagesHolder.getInstance().getMessage4(local,"�Զ���˵�");
			//System.out.println(((JSONArray) folder).length());  
			int folder_size= ((JSONArray) folder).length();
			folder = writeXmlFromJSONArray((JSONArray) folder,
					local, userweb).toString();
			if (folder != null&&(folder.toString().length() != 0)&&(folder.toString()
					.trim().length() != 0))
			{
				//<h3><a>head name</a></h3><div>
				//</div>
				if(folder_size+Accordnum>=12){
				//	tabout="<div><h3><a>"+ACCORDION_name+"</a></h3><div style=\"height:300px;max-height:269px\">"+Inable+"</div></div>";
					//����������ϸ�Ĵ�С���Ƹ߶�
				accord_xml.append("<div><h3><a>").append((String) text_lable)
					  .append("</a></h3><div style=\"max-height:290px;\">");
				accord_xml.append((String) folder);
				accord_xml.append("</div></div>");
					}else{
				accord_xml.append("<div><h3><a>").append((String) text_lable)
						  .append("</a></h3><div>");
				accord_xml.append((String) folder);
				accord_xml.append("</div></div>");
					}
			}

		} else if (Validator.isNotNull(jo.optString("table")))
		// �ж�table ��ǩ
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
						ico_png = " ";
					if ("A".equals(action)) {
						action = "dlgo("
								+ (String.valueOf(table.getId()) + ",-1)");
					} else
						action = "pc.navigate('"
								+ (String.valueOf(table.getId()) + "')");
					//accord item
				//<div class="accordion_headings" onclick="javascript:pc.navigate('15000')">tabimg<a>�ɹ��˼۵�</a></div>
					accord_xml.append("<div class=\"accordion_headings")//.append(ico_png)
							.append("\" onclick=\"javascript:").append(action)
							.append("\">").append(ico_png)
							.append("<a>").append((String) text_lable)
							.append("</a></div>");
				} catch (NDSSecurityException e) {
				}
			}
		} else if (Validator.isNotNull(jo.optString("rpt")))
		// �жϱ����ǩRPT
		{
			String rpt_json = jo.optString("rpt");
			List rpt_list = QueryEngine.getInstance().doQueryList(
					"select ad_table_id,name from ad_cxtab where name='"
							+ rpt_json + "' and ad_client_id="
							+ Integer.valueOf(userweb.getAdClientId())
							+ " and isactive='Y'");
			if ((rpt_list != null) && (rpt_list.size() > 0)) {
				try {
					Table table = TableManager.getInstance().findTable(
							((List) rpt_list.get(0)).get(0));
					String cxtab_name = (String) ((List) rpt_list.get(0)).get(1);
					userweb.checkPermission(table.getSecurityDirectory(), 1);
					if (Validator.isNull((String) text_lable))
						text_lable = cxtab_name;
					if (Validator.isNull(ico_png))
						ico_png = "<img src=\"/html/nds/images/jrpt.gif\" style=\"height:16px;width:16px;vertical-align:bottom;padding-right: 4px;\"></img>";
					String action = "pc.qrpt('" + cxtab_name + "')";
					accord_xml.append("<div class=\"accordion_headings")//.append(ico_png)
					.append("\" onclick=\"javascript:").append(action)
					.append("\">").append(ico_png)
					.append("<a>").append((String) text_lable)
					.append("</a></div>");
					/*
					accord_xml.append("<tree icon=\"").append(ico_png)
							.append("\" text=\"").append((String) text_lable)
							.append("\" action=\"javascript:").append(action)
							.append("\"/>");
					*/
				} catch (NDSSecurityException e) {

				} catch (Throwable e) {
					this.logger.error("Found error for rpt check:" + rpt_json,
							e);
				}
			}

		} else if (Validator.isNotNull(jo.optString("script"))) {
			// �жϱ���Ƿ�ΪJS �ű�

			if (Validator.isNull((String) text_lable))
				text_lable = "Script";
			if (Validator.isNull(ico_png))
				ico_png = " ";
			accord_xml.append("<div class=\"accordion_headings")//.append(ico_png)
			.append("\" onclick=\"javascript:")
			.append(jo.optString("script"))
			.append("\">").append(ico_png)
			.append("<a>").append((String) text_lable)
			.append("</a></div>");
			/*
			accord_xml.append("<tree icon=\"").append(ico_png)
					.append("\" text=\"").append((String) text_lable)
					.append("\" action=\"javascript:")
					.append(jo.optString("script")).append("\"/>");
					*/
		}

		return accord_xml;

	}

	@Override
	public String toHREF(Locale locale, Map env) {
		// TODO Auto-generated method stub
		return null;
	}

}
