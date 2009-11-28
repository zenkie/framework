package nds.web.button;

import java.sql.Connection;
import java.util.*;

import javax.servlet.http.HttpServletRequest;

import nds.control.util.SecurityUtils;
import nds.control.web.UserWebImpl;
import nds.control.web.WebUtils;
import nds.query.QueryEngine;
import nds.schema.Column;
import nds.schema.*;
import nds.security.Directory;
import nds.util.*;
import nds.util.WebKeys;

/**
 * 
销售退货单，在商品明细行上增加一个按钮，能够查询该商品，该经销商的所有销售记录。
 * @author yfzhu
 *
 */
public class ButtonQuerySaleHistory extends ButtonCommandUI_Impl{
	
	/**
 format:
 oq.toggle('/html/nds/query/search.jsp?table=14305&return_type=s&queryindex='+encodeURIComponent(document.getElementById('queryindex_-1').value),'noacc')
	 */
	protected String getHandleURL(HttpServletRequest request, Column column,int objectId){
		//get customerid and pdt,asiid
		String sql= "select c.c_customer_id, b.m_product_id, b.m_attributesetinstance_id from 	M_RET_SALE a, M_RET_SALEITEM b,C_STORE c where b.id="+
			objectId+" and a.id=b.M_RET_SALE_ID and c.id=a.C_ORIG_ID";
		int rtpTableId= TableManager.getInstance().getTable("RP_SALE001").getId();

		StringBuffer sb=new StringBuffer();
		try{
			List al=QueryEngine.getInstance().doQueryList(sql);
			String pt;
			if(al.size()>0){
				//= %3D
				//& %26
				int cid=Tools.getInt( ((ArrayList)al.get(0)).get(0),-1);
				int pid=Tools.getInt( ((ArrayList)al.get(0)).get(1),-1);
				int aid=Tools.getInt( ((ArrayList)al.get(0)).get(2),-1);
				
				pt="RP_SALE001.C_CUSTOMER_ID%3D"+cid+"%26RP_SALE001.M_PRODUCT_ID%3D"+pid+"%26RP_SALE001.M_ATTRIBUTESETINSTANCE_ID%3D"+aid;
	//			int RP_SALE001_C_CUSTOMER_ID= 	TableManager.getInstance().getColumn("RP_SALE001","C_CUSTOMER_ID").getId();
		//		int RP_SALE001_M_PRODUCT_ID= 	TableManager.getInstance().getColumn("RP_SALE001","M_PRODUCT_ID").getId();
			//	int RP_SALE001_ASI= 	TableManager.getInstance().getColumn("RP_SALE001","M_ATTRIBUTESETINSTANCE_ID").getId();
				
				sb.append("/html/nds/query/search.jsp?immediate=Y&table=");
				sb.append(rtpTableId).append("&return_type=n&queryindex=-1&fixedcolumns=")
				.append(pt).append("\",\"nonexist");
				
			}else{
				sb.append("/html/nds/query/search.jsp?immediate=Y&table=");
				sb.append(rtpTableId).append("&return_type=n&queryindex=-1\",\"nonexist");
			}
		}catch(Throwable t){
			logger.error("error", t);
			sb.append("/html/nds/query/search.jsp?immediate=Y&table=");
			sb.append(rtpTableId).append("&return_type=n&queryindex=-1\",\"nonexist");
			
		}
		return sb.toString();
	}
	/**
	 * Popup type
	 * @return
	 */
	protected String getPopupType( HttpServletRequest request, Column column, int objectId){
		return "oq.toggle";
	}

}
