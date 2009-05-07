/******************************************************************
*
*$RCSfile: CreateAttributeSetInstances.java,v $ $Revision: 1.1 $ $Author: Administrator $ $Date: 2006/03/28 02:27:11 $
*

********************************************************************/
package nds.control.ejb.command;

import java.rmi.RemoteException;
import java.util.*;
import java.sql.*;
import nds.schema.*;

import nds.control.ejb.Command;
import nds.control.event.DefaultWebEvent;
import nds.control.util.ValueHolder;
import nds.query.QueryEngine;
import nds.util.NDSException;
import nds.control.util.*;
import nds.security.*;
import nds.util.Tools;
/**
 * Create alias records for specfied product.
 * 
 * 
 * @author yfzhu@agilecontrol.com
 */
public class CreateAlias extends Command{
	private final static String  DELETE_ALIAS="delete from m_product_alias where m_product_id=? and m_attributesetinstance_id=?";

	//private final static String  DEACTIVE_ALIAS="update m_product_alias set isactive='N' where m_product_id=? and m_attributesetinstance_id=?";
	private final static String  UPDATE_ALIAS="update m_product_alias set isactive='Y', no=? where m_product_id=? and m_attributesetinstance_id=?";
	private final static String  INSERT_ALIAS="insert into m_product_alias(id,ad_client_id,ad_org_id,ownerid,modifierid,creationdate, modifieddate,isactive, no, m_product_id, m_attributesetinstance_id)"+
	" select get_sequences('m_product_alias'), ad_client_id, ad_org_id, id, id, sysdate,sysdate,'Y',?,?,? from users where id=?";
	private final static String SELECT_INSTANCE_VALUE="select ai.value from M_ATTRIBUTEINSTANCE ai, m_attributeuse au where ai.m_attributesetinstance_id=? and au.M_ATTRIBUTE_ID=ai.M_ATTRIBUTE_ID and au.M_ATTRIBUTESET_ID=? order by au.orderno";

    /**
     * @param event - special parameters:
     *  pdtid - m_product.id
     *  setid - m_attributeset.id of that product
     *  r0-rn    - naming rule seperator, n is the count of m_attributeset.attributes +2
     * 	pdtcolumn - column of m_product that will be used as part in alias name. pdtcolumn format:
     * 				"1233" or "1233,314" the second one is for fk column, refer QueryUtils.getQuickSearchComboBox for detail
     *  all instances that will have alias set will be uploaded in format "A"+ m_attributesetinstance_id			
     */
    public ValueHolder execute(DefaultWebEvent event) throws NDSException, RemoteException{
    	//logger.debug(event.toDetailString());
    	String dir= TableManager.getInstance().getTable("M_PRODUCT_ALIAS").getSecurityDirectory();
    	event.setParameter("directory",  dir);
		User usr =helper.getOperator(event);
		int clientId= usr.adClientId;
		int orgId= usr.adOrgId;
		int uId= usr.id.intValue();
		
    	helper.checkDirectoryWritePermission(event, usr);
    	QueryEngine engine=QueryEngine.getInstance();
    	
    	int setId= Tools.getInt(event.getParameterValue("setid",true), -1);
    	int pdtId=  Tools.getInt(event.getParameterValue("pdtid",true), -1);
    	
    	String pdtColumn=(String) event.getParameterValue("pdtcolumn",true);
    	int idx=pdtColumn.indexOf(",");
    	if(idx>0) pdtColumn= pdtColumn.substring(0, idx);
    	TableManager manager= TableManager.getInstance();
    	Column col= manager.getColumn(Tools.getInt(pdtColumn,-1));
    	if(col==null) throw new NDSException("Could not find column in m_product:"+ pdtColumn);
    	String pdtColumnValue= (String)engine.doQueryOne("select "+ col.getName() + " from m_product where id="+ pdtId);
    	
    	int attributesSize=Tools.getInt(engine.doQueryOne("select count(*) from m_attribute a, m_attributeuse u where a.ATTRIBUTEVALUETYPE='L' and a.id=u.m_attribute_id and u.m_attributeset_id="+setId),-1);
    	String[] seps= new String[attributesSize+2];
    	for(int i=0;i<attributesSize+2;i++){
    		seps[i]=(String) event.getParameterValue("r"+i,true);
    	}

    	/**
    	 * Process:
    	 *   for each instance in set:
    	 * 		if find in event(Axxx) then
    	 * 			if find in m_product_alias, then update it's name accordingly, and set active
    	 * 			else insert one
    	 * 		else
    	 * 			if find in m_product_alias, then set inactive
    	 */
    	
    	Connection conn=null;
    	PreparedStatement pstmtUpdate=null;
    	PreparedStatement pstmtDeleteAlias=null;
    	PreparedStatement pstmtInsert=null;
    	PreparedStatement pstmtSelect=null;
    	ResultSet rs=null;
    	try{
        	conn= engine.getConnection();
        	pstmtDeleteAlias= conn.prepareStatement(DELETE_ALIAS);
        	pstmtUpdate=conn.prepareStatement(UPDATE_ALIAS);
        	pstmtInsert=conn.prepareStatement(INSERT_ALIAS);
        	pstmtSelect=conn.prepareStatement(SELECT_INSTANCE_VALUE);
	    	List instances=engine.doQueryList("select id from m_attributesetinstance where m_attributeset_id="+setId, conn);
			String no;
			StringBuffer aliasNO;
			//logger.debug("instance count:"+ instances.size());
	    	for(int i=0;i< instances.size();i++){
	    		int asiId= Tools.getInt( instances.get(i),-1 );
	    		String name="A"+ asiId; 
	    		if(event.getParameterValue(name,true)!=null){
	    			pstmtSelect.setInt(1, asiId);
	    			pstmtSelect.setInt(2, setId);
	    			rs = pstmtSelect.executeQuery();
	    			aliasNO=new StringBuffer( seps[0]);
	        		aliasNO.append(pdtColumnValue);
	        		for(int j=1;j< seps.length-1;j++){
	        			if(!rs.next()){
	        				// must find seps.length-2 records in instance value table of that set instance
	        				logger.error("Fail to retrieve records for asiId="+asiId+", setId="+setId);
	        			}
	        			aliasNO.append(seps[j]).append( rs.getString(1));
	        			
	        		}
	        		aliasNO.append(seps[seps.length-1]);
	        		rs.close();
	        		no= aliasNO.toString();
	        		//logger.debug("alias no:"+ no);
	    			pstmtUpdate.setString(1,no);
	    			pstmtUpdate.setInt(2,pdtId);
	    			pstmtUpdate.setInt(3,asiId);
	    			int cnt=pstmtUpdate.executeUpdate();
	    			if(cnt ==0){
	    				//logger.debug("insert one for "+ no+", pdtid="+ pdtId+", asiId="+ asiId);
	    				// insert one
	    				pstmtInsert.setString(1, no);
	    				pstmtInsert.setInt(2,pdtId);
	    				pstmtInsert.setInt(3,asiId);
	    				pstmtInsert.setInt(4,uId);
	    				pstmtInsert.executeUpdate();
	    			}
	    		}else{
	    			//if find in m_product_alias, then set inactive
	    			pstmtDeleteAlias.setInt(1,pdtId);
	    			pstmtDeleteAlias.setInt(2,asiId);
	    			pstmtDeleteAlias.executeUpdate();
	    			//logger.debug("deactive one for  pdtid="+ pdtId+", asiId="+ asiId);
	    		}
	    	}
    	}catch(Throwable t){
    		logger.error("failed when create alias for ="+pdtId, t);
			throw new NDSException("@exception@", t);
    	}finally{
    		if(pstmtInsert!=null) {try{pstmtInsert.close();}catch(Throwable t2){}}
    		if(pstmtUpdate!=null) {try{pstmtUpdate.close();}catch(Throwable t2){}}
    		if(pstmtDeleteAlias!=null) {try{pstmtDeleteAlias.close();}catch(Throwable t2){}}
    		if(conn!=null) {try{conn.close();}catch(Throwable t2){}}
    	}
        ValueHolder v = new ValueHolder();
        v.put("message","@complete@.") ;
         return v;

    }
}