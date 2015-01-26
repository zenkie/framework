package nds.control.ejb.command;

import java.rmi.RemoteException;
import java.util.ArrayList;

import org.directwebremoting.WebContext;
import org.json.*;

import nds.control.ejb.Command;
import nds.control.event.DefaultWebEvent;
import nds.control.event.NDSEventException;
import nds.control.util.ValueHolder;
import nds.control.web.FlowProcessor;
import nds.log.Logger;
import nds.log.LoggerManager;
import nds.query.*;
import nds.schema.*;
import nds.util.*;

import java.util.*;

import nds.security.User;
import java.sql.*;
/**
 * check attribute setting according to product input specified. There are 3 conditions:
 * 1) product input is not valid,
 * 2) product has no attribute that should be configured.
 * 3) product has attribute to configure.
 * This is for ajax request.
 *
 *  ����
 */
public class CheckProductAttribute extends Command {
	private Logger logger= LoggerManager.getInstance().getLogger(CheckProductAttribute.class.getName());
	//for burgeon product, size and color will also try to be loaded
	private boolean loadingBurgeonProduct=false; //will loading M_ATTRIBUTESETINSTANCE_ID;value1 as color, and M_ATTRIBUTESETINSTANCE_ID;value2 as size
	private boolean loadingBurgeonProduct2=false;//load M_ATTRIBUTESETINSTANCE_ID.VALUE1_CODE, and VALUE2_CODE

	private int cutOffTailLength=-1; //�ضϵ��������룬һ�����Ψһ�����ˮ��
	public CheckProductAttribute(){
		loadingBurgeonProduct=( TableManager.getInstance().getColumn("M_ATTRIBUTESETINSTANCE","VALUE1")!=null);
		loadingBurgeonProduct2=( TableManager.getInstance().getColumn("M_ATTRIBUTESETINSTANCE","VALUE1_CODE")!=null);
		try{
		cutOffTailLength=Tools.getInt( QueryEngine.getInstance().doQueryOne("select value from ad_param where name='portal.6001'"),-1);
		}catch(Throwable t){
			logger.error("fail", t);
		}
	}
	/**
     * Whether this command use internal transaction control. For normal command, transaction is controled by
     * caller, yet for some special ones, the command will control transaction seperativly, that is, the command
     * will new transaction and commit that one explicitly
     * @return false if use transaction from caller
     */
    public boolean internalTransaction(DefaultWebEvent event){
    	return true;
    }
	/**
	 * If we can find product by name, attribute set will be checked, if found, will direct
	 * front to attribute set filling page.
	 *
	 * If we find product alias by name as barcode, attribute set will not be checked if attribute
	 * set instance id is set in m_product_alias table.
	 *
	 * ���m_product_alias �����Ӧ����δ���� ��������ʵ��id�����������ϵ��������Լ���ȻҪ�����������á�
	 *
	 * @param event contains
	 * 	jsonObject -
			product* - product name or barcode
			fixedColumns - fixed columns (PairTable) for current check table, by which we can get master table's
			 				record id
			check_alias - default to true
			trymatrix - default to true, whether to try loading matrix or not, for modify page, should set to false
			uk.ltd.getahead.dwr.WebContext - this is for convenience to request jsp result
			tag - this is used by client to remember locale status, such as for row information,
				  it will be sent back unchanged.
	 * @return "data" will be jsonObject with following format:
	 * { 	code:0|!=0,
	 * 		message: message for error,
	 * 		dom: DOM created by special we page, may contain javascript segments
	 * 		showDialog: true|false
	 * 		product_id: id of the product, set when code!=0
	 * 		product_name: name of the product, set when code!=0
	 * 		product_value: value of the product
	 * 		product_asi_id: attribute set instance id of the product
	 * 		product_asi_name: attribute set instance name of the product
	 * 		product_asi_value1: this is optional, for burgeon product only, it's color
	 * 		product_asi_value2: this is optional, for burgeon product only, it's size
	 * }
	 *
	 */
  public ValueHolder execute(DefaultWebEvent event) throws RemoteException, NDSException {
  	TableManager manager=TableManager.getInstance();
  	User usr=helper.getOperator(event);
  	QueryEngine engine=QueryEngine.getInstance();

  	boolean productUCase= manager.getColumn("m_product", "name").isUpperCase();
  	MessagesHolder mh= MessagesHolder.getInstance();
  	Connection conn= QueryEngine.getInstance().getConnection();
  	PreparedStatement pstmt=null;
  	ResultSet rs=null;
  	try{
	  	JSONObject jo=(JSONObject)event.getParameterValue("jsonObject");
	  	Object tag= jo.opt("tag");
	  	String product= jo.getString("product");
	  	if(productUCase) product=product.toUpperCase();
	  	int tableId= jo.getInt("tableId");
	  	Table table= manager.getTable(tableId);
	  	//if(uCase) product= product.toUpperCase();
	  	boolean checkAlias = jo.optBoolean("check_alias", true);
	  	boolean tryMatrix=  jo.optBoolean("trymatrix", true);
	  	JSONObject fixedColumns=jo.optJSONObject("fixedColumns");
	  	int masterId=-1;
	  	Table masterTable=null;
	  	if(fixedColumns!=null && fixedColumns.length()==1){
	  		//��������ֱ�ӵ��ж����������ļ�¼��ID�������Ĳ��������൱�ľ�����
	  		String key= (String)fixedColumns.keys().next();
	  		int fkColumnId= Tools.getInt(key,-1);
	  		Column fkColumn=manager.getColumn(fkColumnId);
	  		if(fkColumn!=null){
	  			masterTable= fkColumn.getReferenceTable();
		  		masterId=Tools.getInt( fixedColumns.get(key),-1);
	  		}
	  	}

	  	ValueHolder vh;
	  	int productId=-1,  asId=-1 ,asiId=-1 ;
	  	String productName=null, productValue=null, asiName=null;
	  	String pricelist=null;// take it as string, so we can handle null
	  	String value1=null,value2=null,value1_code=null,value2_code=null;

	  	Column aliasAk2= manager.getTable("M_PRODUCT_ALIAS").getAlternateKey2();
	  	Column pdtAk2= manager.getTable("M_PRODUCT").getAlternateKey2();

	  	// ������˿���Ҫ����������
	  	// ����LILYҪ������Ҳ�п����(AK2)
  		if(checkAlias){

  			String sql="select m.id, m.name, m.value, m.m_attributeset_id, a.id,a.description, m.pricelist"+
  			(this.loadingBurgeonProduct?",a.value1,a.value2":"")+
  			(this.loadingBurgeonProduct2?",a.value1_code,a.value2_code":"")
  			+" from m_product_alias p, "+
				"m_product m, m_attributesetinstance a where (p.no=? "+
				(aliasAk2==null?"":" or p."+aliasAk2.getName()+"=?" )
				+" ) and p.isactive='Y' and m.isactive='Y' and p.ad_client_id=? and m.id=p.m_product_id and a.id(+)=p.M_ATTRIBUTESETINSTANCE_ID";

  			pstmt= conn.prepareStatement(sql);
  			int i=1;
  		  	pstmt.setString(i++, (manager.getColumn("m_product_alias", "no").isUpperCase()? product.toUpperCase():product));
  		  	if(aliasAk2!=null)
  		  		pstmt.setString(i++, (aliasAk2.isUpperCase()? product.toUpperCase():product));

  		  	pstmt.setInt(i++, usr.adClientId);
  		  	rs=pstmt.executeQuery();
  			if(rs.next()){
  		  		productId= rs.getInt(1);// Tools.getInt( al.get(0), -1);
  		  		productName= rs.getString(2);//(String) al.get(1);
  		  		productValue=rs.getString(3);// (String) al.get(2);
  		  		asId=rs.getInt(4);// Tools.getInt( al.get(3), -1);
  		  		if(rs.wasNull()) asId=-1;
  		  		asiId= rs.getInt(5);
  		  		if(rs.wasNull()) asiId=-1;
  		  		asiName= rs.getString(6);

  		  		pricelist=String.valueOf( rs.getDouble(7));
  		  		if(rs.wasNull()) pricelist=null;
  		  		if(loadingBurgeonProduct){
  		  			value1= rs.getString(8);
  		  			value2=rs.getString(9);
  		  		}
  		  		if(loadingBurgeonProduct2){
  		  			value1_code= rs.getString(10);
  		  			value2_code=rs.getString(11);
  		  		}

  			}
  			rs.close();
  			pstmt.close();

  		}
  		if(productId ==-1){
  		  	pstmt= conn.prepareStatement("select id, name, value, m_attributeset_id, pricelist from m_product where (name=? "+
				(pdtAk2==null?"":" or "+pdtAk2.getName()+"=?" )
				+") and isactive='Y' and ad_client_id=?");
  		  	int i=1;
  		  	pstmt.setString(i++, (productUCase? product.toUpperCase():product));
  		  	if(pdtAk2!=null)pstmt.setString(i++, (pdtAk2.isUpperCase()? product.toUpperCase():product));
  		  	pstmt.setInt(i++, usr.adClientId);
  		  	rs= pstmt.executeQuery();
  		  	if(rs.next()){
  		  		productId= rs.getInt(1);// Tools.getInt( al.get(0), -1);
  		  		productName= rs.getString(2);//(String) al.get(1);
  		  		productValue=rs.getString(3);// (String) al.get(2);
  		  		asId=rs.getInt(4);// Tools.getInt( al.get(3), -1);
  		  		if(rs.wasNull()) asId=-1;
  		  		pricelist=String.valueOf( rs.getDouble(5));
  		  		if(rs.wasNull()) pricelist=null;
  		  	}
  		  	rs.close();
			pstmt.close();
  		}

  		// ���ս��ϲ��µ����������Ȼû�ҵ���Ʒ������ͨ���ض�����λ�����ж�λ����������λ��Ψһ�����ˮ��
  		// 2010-7-7 added
  		if(productId==-1){
  			if(checkAlias && cutOffTailLength>0 && product.length()>cutOffTailLength){
  				product = product.substring(0,product.length()-cutOffTailLength );

  	  			String sql="select m.id, m.name, m.value, m.m_attributeset_id, a.id,a.description, m.pricelist"+
  	  			(this.loadingBurgeonProduct?",a.value1,a.value2":"")+
  	  			(this.loadingBurgeonProduct2?",a.value1_code,a.value2_code":"")
  	  			+" from m_product_alias p, "+
  					"m_product m, m_attributesetinstance a where (p.no=? "+
  					(aliasAk2==null?"":" or p."+aliasAk2.getName()+"=?" )
  					+" ) and p.isactive='Y' and m.isactive='Y' and p.ad_client_id=? and m.id=p.m_product_id and a.id(+)=p.M_ATTRIBUTESETINSTANCE_ID";

  	  			pstmt= conn.prepareStatement(sql);
  	  			int i=1;
  	  		  	pstmt.setString(i++, (manager.getColumn("m_product_alias", "no").isUpperCase()? product.toUpperCase():product));
  	  		  	if(aliasAk2!=null)
  	  		  		pstmt.setString(i++, (aliasAk2.isUpperCase()? product.toUpperCase():product));

  	  		  	pstmt.setInt(i++, usr.adClientId);
  	  		  	rs=pstmt.executeQuery();
  	  			if(rs.next()){
  	  		  		productId= rs.getInt(1);// Tools.getInt( al.get(0), -1);
  	  		  		productName= rs.getString(2);//(String) al.get(1);
  	  		  		productValue=rs.getString(3);// (String) al.get(2);
  	  		  		asId=rs.getInt(4);// Tools.getInt( al.get(3), -1);
  	  		  		if(rs.wasNull()) asId=-1;
  	  		  		asiId= rs.getInt(5);
  	  		  		if(rs.wasNull()) asiId=-1;
  	  		  		asiName= rs.getString(6);

  	  		  		pricelist=String.valueOf( rs.getDouble(7));
  	  		  		if(rs.wasNull()) pricelist=null;
  	  		  		if(loadingBurgeonProduct){
  	  		  			value1= rs.getString(8);
  	  		  			value2=rs.getString(9);
  	  		  		}
  	  		  		if(loadingBurgeonProduct2){
  	  		  			value1_code= rs.getString(10);
  	  		  			value2_code=rs.getString(11);
  	  		  		}

  	  			}
  	  			rs.close();
  	  			pstmt.close();

  	  		}
  		}

	  	/**
	  	 * �������˴˱���Ϊʵ����ı��壬����ϸ������nds.control.ejb.command.CheckProductAttributeʱ��
		 * ����ϵͳ�Զ����ö�Ӧ����Ķ�Ӧ�洢������������"_CHKPDT"(�����¼id, ����id)�������Ƿ��������ָ����Ʒ�ļ��顣
		 *
		 * ���磬����ⵥ��ϸɨ������ʱ����������з���ⵥ���Ѿ����õĲ�Ʒ��Ӧ���̽�ֹ����
	  	 */
	  	String errorMsg=null;
	  	/*if(productId!=-1){
		  	if(masterTable!=null && masterId!=-1 && (masterTable instanceof ProductCheckTableImpl)){
		  		// check if master table has special test on validity of product
		  		boolean isValid=Tools.getYesNo(engine.doQueryOne("select "+ masterTable.getRealTableName()+
		  				"_CHKPDT("+masterId+","+ productId+") from dual",conn ), true);
		  		if(!isValid){
		  			productId=-1;
		  			errorMsg="product-not-valid";
		  		}
		  	}
	  	}*/

	  	JSONObject ro=new JSONObject();
	  	ro.put("tag", tag); //  return back unchanged.
	  	logger.debug("productId        "+String.valueOf(productId));
	  	logger.debug("asiId        "+String.valueOf(asiId));
	  	//֩����ģʽ��������ģʽ
	  	//String pstr=(String)QueryEngine.getInstance().doQueryOne("select t.value from ad_param t where t.name = 'portal.pdtMatrix.tables'");
	  	//if(pstr.indexOf(table.getRealTableName())>=0){
	  	//	asiId=-1;
	  	//}
	  	if(productId==-1 ){
	  		// not found the product, error returns
	  		ro.put("code", 1);
	  		ro.put("message", mh.getMessage(event.getLocale(), (errorMsg==null?"product-not-found":errorMsg)));
	  		ro.put("showDialog",false);
	  	}else{
	  	/* 		product_id: id of the product, set when code!=0
		 * 		product_name: name of the product, set when code!=0
		 * 		product_value: value of the product
		 * 		product_asi_id: attribute set instance id of the product
		 * 		product_asi_name: attribute set instance name of the product
		 */
	  		ro.put("code", 0);
	  		ro.put("product_id",productId );
	  		ro.put("product_name",productName);
	  		ro.put("product_value",productValue);
	  		ro.put("product_pricelist",pricelist);
	  		if(asiId!=-1 ||!table.supportAttributeDetail() ){
	  			// no need to show page
	  	  		ro.put("showDialog",false);
	  	  		ro.put("product_asi_id",asiId );
	  	  		ro.put("product_asi_name",asiName);
	  	  		ro.put("product_value1",value1);
	  	  		ro.put("product_value2",value2);
	  	  		ro.put("product_value1_code",value1_code);
	  	  		ro.put("product_value2_code",value2_code);
	  		}else{
	  			if(asId!=-1 && tryMatrix ){
	  				logger.debug("now matrix");
	  				
	  				// does the attribute set support matrix?
	  				//logger.info("select count(*) from m_attributeuse u where u.isactive='Y' and u.M_ATTRIBUTESET_ID="+ asId+" and exists(select 1 from M_ATTRIBUTE a where a.ATTRIBUTEVALUETYPE='L' and a.id=u.M_ATTRIBUTE_ID)");
	  				int cnt=Tools.getInt( engine.doQueryOne("select count(*) from M_PRODUCT t WHERE t.isactive='Y' AND t.id="+productId,conn), -1);
	  				logger.info("now cnt:"+cnt);
	  				if(cnt>0){
		  				WebContext wc=(WebContext) jo.get("org.directwebremoting.WebContext");
		  				/**
		  				 * Please note param "compress=false" is to prohibit  com.liferay.filters.compression.CompressionFilter from compressing file content
		  				 */
		  				int store_colId=Tools.getInt((String)jo.getString("store_colId"),-1);
		  				String storedata=(String)jo.getString("storedata");
		  				int dest_colId=Tools.getInt((String)jo.getString("dest_colId"),-1);
		  				String destdata=(String)jo.getString("destdata");
		  				String page="";
		  				//System.out.print(" fixedColumns is"+fixedColumns.toString());
		  				String url=WebKeys.NDS_URI+"/pdt/itemdetail.jsp?compress=f&table="+tableId+"&pdtid="+productId+"&asid="+asId+"&storedata="+storedata+"&store_colId="+store_colId+"&dest_colId="+dest_colId+"&destdata="+destdata+"&fixedColumns="
		  				+(fixedColumns==null?"":fixedColumns.toString());
		  				//System.out.print(" url is"+url);
			  			  
		  				//Edit by Robin 20101207 �����ж�ϵͳ�������������п�������Ƿ���Ҫ���ƻ����桱���Ϊtrue �Ҳ��붨�ƻ��ġ������������˱� �򵯳�����Ϊ���ƽ��棨������portal.pdtMatrix.CustomJSP.JSPURL���磺�¿��ķ������������빦��
                        boolean customJSP=Tools.getBoolean(engine.doQueryOne("select value from ad_param where name='portal.pdtMatrix.CustomJSP'"),false);
                        if(customJSP){
                             String tables=String.valueOf(engine.doQueryOne("select value from ad_param where name='portal.pdtMatrix.tables'"));
                            if(tables.contains(table.getName())){

                               String jspURL=String.valueOf(engine.doQueryOne("select value from ad_param where name='portal.pdtMatrix.CustomJSP.JSPURL'"));                                 
                               url=jspURL+"?compress=f&table="+tableId+"&pdtid="+productId+"&asid="+asId+"&storedata="+storedata+"&store_colId="+store_colId+"&dest_colId="+dest_colId+"&destdata="+destdata+"&productName="+productName+"&objId="+masterId;                                
                               page=url;
                               logger.info(page);
                               ro.put("customJSP",true);
                            }else{
                            	page=wc.forwardToString(url);
                           	 	ro.put("customJSP",false);
                            }
                         }
		  				ro.put("pagecontent", page);
		  				ro.put("showDialog",true);

		  				logger.info("now cnt-------��:"+cnt);
	  				}else{
	  					ro.put("showDialog",false);
	  				}
	  			}else
	  				ro.put("showDialog",false);
	  		}
	  	}
	  	
	  	ValueHolder holder= new ValueHolder();  
		holder.put("message", mh.getMessage(event.getLocale(), "complete"));
		holder.put("code","0");
		holder.put("data",ro );
		logger.debug(ro.toString());
		return holder;
  	}catch(Throwable t){
  		logger.error("exception",t);
  		throw new NDSException(t.getMessage(), t);
  	}finally{
  		if(rs!=null )try{rs.close();}catch(Throwable t){}
  		if(pstmt!=null)try{pstmt.close();}catch(Throwable t){}
  		if(conn!=null)try{conn.close();}catch(Throwable t){}
  	}
  }

  private DefaultWebEvent createEvent(JSONArray row, ArrayList colNames, DefaultWebEvent template ) throws JSONException{
  	DefaultWebEvent e=(DefaultWebEvent)template.clone();
  	for(int i=0;i< colNames.size();i++){
  		e.put( (String)colNames.get(i), row.get(i));
  	}
  	return e;
  }
}
