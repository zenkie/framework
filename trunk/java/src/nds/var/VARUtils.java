package nds.var;
import  nds.var.VAROrder;

import java.util.List;

import com.sun.java.swing.plaf.windows.WindowsBorders.ToolBarBorder;
import nds.query.QueryEngine;
import nds.query.QueryException;
import nds.util.Configurations;
import nds.util.NDSException;
import nds.util.Tools;




public final class VARUtils {
	
	/**
	 * 判断是否是代理商
	 *@param userId users.id
	 *@return true:是代理商,false:不是代理商
	 */
	public static boolean isAgent(int userId) throws NDSException{
		try {
			int count=Tools.getInt(QueryEngine.getInstance().doQueryOne("select count(*) from users u, c_bpartner p where p.isagent='Y' and p.id=u.c_bpartner_id and u.id="+userId),-1);
			if(count!=0){
				return true;
			}
		return false;
		} catch (Throwable t) {
			if(t instanceof NDSException) throw (NDSException)t;
	  		throw new NDSException(t.getMessage(), t);
		}
		
	}
	/**
	 * 
	 * @param bpartnerId c_bpartner.id
	 * @return 代理商面向最终物流客户的零售价
	 * @throws NDSException
	 */
	public static int getProductPriceByAgentId(int bpartnerId) throws NDSException{
		int price=0;		 
		price=Tools.getInt(QueryEngine.getInstance().doQueryOne("select priceso from m_product_price where c_bpartner_id="+bpartnerId),0);
		if(price==0){
			Configurations conf=(Configurations)nds.control.web.WebUtils.getServletContextManager().getActor(nds.util.WebKeys.CONFIGURATIONS);
			int pdtid=Tools.getInt(conf.getProperty("var.default.product"),-1);
			price=Tools.getInt(QueryEngine.getInstance().doQueryOne("select priceretail from m_product where id="+pdtid),0);
			if(price==0){
				throw new NDSException("未设置默认零售单价!");
			}
		}
		return price;
	}
    
	/**
	 * 通过传入参数：userid　如果是代理商就取出代理商的价格
	 * @param userId users.id
	 * @return 产品价格
	 * 通过传入参数：userid 去找对应的c_bpartner_id 在m_product_price里对应的c_bpartner_id取其代理价
	 * 如果价格不存在，就取默认的代理商的id,在m_product取默认的普通代理价
	 */
	public static int getVARPOPrice(int userId) throws NDSException{
	    int price=0;		 
		 try {
			if(isAgent(userId)){
					int c_bpartnerId=Tools.getInt(QueryEngine.getInstance().doQueryOne("select c_bpartner_id from users where id="+userId),-1);
					if(c_bpartnerId!=-1) {
						price=Tools.getInt(QueryEngine.getInstance().doQueryOne("select pricepo from m_product_price where c_bpartner_id="+c_bpartnerId),0);							
							if(price==0){
							Configurations conf=(Configurations)nds.control.web.WebUtils.getServletContextManager().getActor(nds.util.WebKeys.CONFIGURATIONS);
							int pdtid=Tools.getInt(conf.getProperty("var.default.product"),-1);
							price=Tools.getInt(QueryEngine.getInstance().doQueryOne("select pricevar from m_product where id="+pdtid),0);
							if(price==0){
							throw new NDSException("未设置普通代理价!");
						  }
						}
					}
			}else{
				throw new NDSException("页面访问错误，必须代理商才能使用本页面");	
			}
			return price;
		 }catch (Throwable t) {
				if(t instanceof NDSException) throw (NDSException)t;
		  		throw new NDSException(t.getMessage(), t);
		 }
		
	}
	
	/**
	 * @param serialno c_bpartner.serialno
	 * @return 产品价格
	 * 通过传入参数：serialno 去找对应的c_bpartner_id 在m_product_price里对应的c_bpartner_id取其零售价格
	 * 如果价格不存在，就取默认的代理商的id,在m_product取默认的零售价格
	 * 如果c_bpartner_id不存在，就取默认的代理商的id,在m_product取默认的零售价
	 */
	public static int getRetailPrice(String serialno) throws NDSException{
		 int price=0;
		 try {
		  int varid= Tools.getInt(QueryEngine.getInstance().doQueryOne("select id from c_bpartner where serialno='"+serialno+"'"),-1);
		  Configurations conf=(Configurations)nds.control.web.WebUtils.getServletContextManager().getActor(nds.util.WebKeys.CONFIGURATIONS);
		  int pdtid=Tools.getInt(conf.getProperty("var.default.product"),-1);
		  if(varid==-1){
			 price=Tools.getInt(QueryEngine.getInstance().doQueryOne("select priceretail from m_product where id="+pdtid),0);	
			 if(price==0){
				 throw new NDSException("未设置默认零售单价!");	  
			 }
		   }else{
				 price=Tools.getInt(QueryEngine.getInstance().doQueryOne("select priceso from m_product_price where c_bpartner_id="+varid),0);
				 if(price==0){
				 price=Tools.getInt(QueryEngine.getInstance().doQueryOne("select priceretail from m_product where id="+pdtid),0);
				 if(price==0){
				 throw new NDSException("未设置零售单价!");
			   }
			 }
		   }
		   return price;
		 }catch (Throwable t) {
				if(t instanceof NDSException) throw (NDSException)t;
		  		throw new NDSException(t.getMessage(), t);
		 }
		 
	}
	
	/**
	 * 通过代理商序列号e_orderid来设置varorder相关值．
	 *
	 */
	public static VAROrder getVAROrder(int id) throws NDSException{
		try {
			List order= QueryEngine.getInstance().doQueryList("select id,docno,doctype,pemail,pname,c_bpartner_id,amt,ptruename,payer_id,status,state from e_order where id="+id);
			VAROrder varorder=new VAROrder();
			if(order.size()!=0){
				varorder.setId(Tools.getInt(((List)order.get(0)).get(0),-1));
            	varorder.setDocno((String)((List)order.get(0)).get(1));
            	varorder.setDoctype((String)((List)order.get(0)).get(2));
            	varorder.setPemail((String)((List)order.get(0)).get(3));
            	varorder.setPname((String)((List)order.get(0)).get(4));
            	varorder.setC_bpartner_id(Tools.getInt(((List)order.get(0)).get(5),-1));
            	varorder.setAmt(Tools.getBigDecimal(((List)order.get(0)).get(6).toString(),false).doubleValue());
            	varorder.setPtruename((String)((List)order.get(0)).get(7));
             	varorder.setPayer_id(Tools.getInt(((List)order.get(0)).get(8),-1));
            	varorder.setStatus(Tools.getInt(((List)order.get(0)).get(9),-1));
            	varorder.setState((String)((List)order.get(0)).get(10));	
            }else {
            	throw new NDSException("订单未找到，或不能支付，请重新创建");
            }
            if(varorder.getAmt()<=0){
            	throw new NDSException("订单金额不正确，请重新创建");
            }
            
            if(varorder.getC_bpartner_id()!=-1){
            	String c_bpartnername=(String)QueryEngine.getInstance().doQueryOne("select name from c_bpartner where id="+varorder.getC_bpartner_id());
            	varorder.setC_bpartner_name(c_bpartnername);
            }
     	 return varorder;
 		} catch (Throwable t) {
			if(t instanceof NDSException) throw (NDSException)t;
	  		throw new NDSException(t.getMessage(), t);
		}

	}
	
	/**
	 * 通过代理商序列号e_order的单据编号docno来设置varorder相关值．
	 *
	 */
     public static VAROrder getVAROrder(String  docno) throws NDSException{
    	 try {
 			List order= QueryEngine.getInstance().doQueryList("select id,docno,doctype,pemail,pname,c_bpartner_id,amt,ptruename,payer_id,status,state from e_order where  docno='"+docno+"'");
 			VAROrder varorder=new VAROrder();
 			if(order.size()!=0){
             	varorder.setId(Tools.getInt(((List)order.get(0)).get(0),-1));
             	varorder.setDocno((String)((List)order.get(0)).get(1));
             	varorder.setDoctype((String)((List)order.get(0)).get(2));
             	varorder.setPemail((String)((List)order.get(0)).get(3));
             	varorder.setPname((String)((List)order.get(0)).get(4));
             	varorder.setC_bpartner_id(Tools.getInt(((List)order.get(0)).get(5),-1));
            	varorder.setAmt(Tools.getBigDecimal(((List)order.get(0)).get(6).toString(),false).doubleValue());
             	varorder.setPtruename((String)((List)order.get(0)).get(7));
             	varorder.setPayer_id(Tools.getInt(((List)order.get(0)).get(8),-1));
             	varorder.setStatus(Tools.getInt(((List)order.get(0)).get(9),-1));
             	varorder.setState((String)((List)order.get(0)).get(10));	
             }else {
             	throw new NDSException("订单未找到，或不能支付，请重新创建");
             }
             if(varorder.getAmt()<=0){
             	throw new NDSException("订单金额不正确，请重新创建");
             }
             if(varorder.getC_bpartner_id()!=-1){
             	String c_bpartnername=(String)QueryEngine.getInstance().doQueryOne("select name from c_bpartner where id="+varorder.getC_bpartner_id());
             	varorder.setC_bpartner_name(c_bpartnername);
             }
      		return varorder;
  		} catch (Throwable t) {
			if(t instanceof NDSException) throw (NDSException)t;
	  		throw new NDSException(t.getMessage(), t);
 		}
	}
     
    
}
