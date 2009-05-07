/*******************************************************************************
 * 
 * $RCSfile: SaveAttributeSetInstance.java,v $ $Revision: 1.1 $ $Author:
 * Administrator $ $Date: 2006/03/28 02:27:11 $
 * 
 *  
 ******************************************************************************/
package nds.control.ejb.command;

import java.math.BigDecimal;
import java.rmi.RemoteException;
import java.util.*;
import java.sql.*;
import nds.schema.*;

import nds.control.ejb.Command;
import nds.control.event.DefaultWebEvent;
import nds.control.util.ValueHolder;
import nds.query.QueryEngine;
import nds.query.QueryUtils;
import nds.util.*;
import nds.control.util.*;
import nds.security.*;
import nds.util.Tools;
/**
 * Including both create and modify action
 * 
 * @author yfzhu@agilecontrol.com
 */
public class SaveAttributeSetInstance extends Command {
	private final static String INSERT_SETINSTANCE = "insert into m_attributesetinstance(id,ad_client_id,ad_org_id,isactive,modifieddate,creationdate,modifierid, ownerid,m_attributeset_id,GUARANTEEDATE,M_LOT_ID,SERNO,LOT,description)values(?,?,?,'Y',sysdate,sysdate,?,?,?,?,?,?,?,?)";
	private final static String UPDATE_SETINSTANCE = "update m_attributesetinstance set modifieddate=sysdate,modifierid=?,GUARANTEEDATE=?,M_LOT_ID=?,SERNO=?,LOT=?,description=? where id=?";

	private final static String INSERT_INSTANCE = "insert into M_ATTRIBUTEINSTANCE(id,ad_client_id,ad_org_id,isactive,modifieddate,creationdate,modifierid, ownerid,M_ATTRIBUTESETINSTANCE_ID,M_ATTRIBUTE_ID,M_ATTRIBUTEVALUE_ID,value, VALUENUMBER)values(get_sequences('M_ATTRIBUTEINSTANCE'),?,?,'Y',sysdate,sysdate,?,?,?,?,?,?,?)";
	private final static String UPDATE_INSTANCE = "UPDATE M_ATTRIBUTEINSTANCE SET modifieddate=SYSDATE,modifierid=?, M_ATTRIBUTEVALUE_ID=?,value=?, VALUENUMBER=? WHERE M_ATTRIBUTESETINSTANCE_ID=? AND M_ATTRIBUTE_ID=?";
	private final static String DELETE_INSTANCE = "DELETE FROM  M_ATTRIBUTEINSTANCE WHERE M_ATTRIBUTESETINSTANCE_ID=? AND M_ATTRIBUTE_ID=?";
	/**
	 * @param event -
	 *    special parameters: 
	 * 			setid    - m_attributeset.id
	 *          id       - m_atrributesetinstance.id if not -1, modify this object, else create one
	 			Axxx     - attribute instance content, xxx is for m_attribute.id
	 			name     - instance description
	 			serno    - serial no
	 			lot      - lot no
	 			guaranteedate - date 
	 *            
	 */
	public ValueHolder execute(DefaultWebEvent event) throws NDSException,
			RemoteException {
		int instanceId = Tools.getInt(event.getParameterValue("id"),-1);
		ValueHolder vh ;
		if (instanceId!=-1){
			vh=doModify(event);
		}else vh=doCreate(event);
		return vh;
	}
	/**
	 * Modify an existing set instance, and all its instance values
	 * @param event
	 * @return
	 * @throws NDSException
	 * @throws RemoteException
	 */
	private ValueHolder doModify(DefaultWebEvent event) throws NDSException,
			RemoteException {
		int setId = Tools.getInt(event.getParameterValue("setid"), -1);
		int instanceId = Tools.getInt(event.getParameterValue("id"),-1);
		
		String dir = TableManager.getInstance().getTable(
				"M_ATTRIBUTESETINSTANCE").getSecurityDirectory();
		event.setParameter("directory", dir);
		User usr = helper.getOperator(event);
		int clientId = usr.adClientId;
		int orgId = usr.adOrgId;
		int uId = usr.id.intValue();

		if(!SecurityUtils.hasObjectPermission(uId, usr.getName(), 
				"M_ATTRIBUTESETINSTANCE",  instanceId,Directory.WRITE,  event.getQuerySession() ))
			throw new NDSException("@no-permission@");
		

		QueryEngine engine = QueryEngine.getInstance();

		List attributes = engine
				.doQueryList("select a.id , a.ATTRIBUTEVALUETYPE from m_attribute a, m_attributeuse u where a.id=u.m_attribute_id and u.m_attributeset_id="
						+ setId + " order by u.orderno asc");
		HashMap attributeValues = new HashMap(); //key vid (Integer), value:
												 // value (String)
		for (int i = 0; i < attributes.size(); i++) {
			List v = engine
					.doQueryList("select v.id, v.value from m_attributevalue v where v.m_attribute_id="
							+ ((List)attributes.get(i)).get(0));
			for (int j = 0; j < v.size(); j++) {
				attributeValues.put(new Integer(Tools.getInt(((List) v.get(j))
						.get(0), -1)), ((List) v.get(j)).get(1));
			}
		}

		Connection conn = null;
		PreparedStatement pstmt = null;
		PreparedStatement pstmt2 = null;
		PreparedStatement pstmtDelete = null;
		int valueId;
		String value;
		
		try {
			conn = engine.getConnection();
			String lot=(String) event.getParameterValue("lot");
			int m_lot_id=-1;
			if(Validator.isNotNull(lot)){
				try{
					m_lot_id= Tools.getInt(engine.doQueryOne("select id from m_lot where name=" + QueryUtils.TO_STRING(lot)+" and ad_client_id="+clientId, conn ),-1);
				}catch(Throwable ttt){
					logger.error("Fail to load m_lot id according to name="+ lot, ttt);
				}
			}
			pstmt = conn.prepareStatement(UPDATE_SETINSTANCE);
			pstmt2 = conn.prepareStatement(INSERT_INSTANCE);
			pstmtDelete =conn.prepareStatement(DELETE_INSTANCE);
			
			String setInstanceName = (String) event.getParameterValue("name");

			//modifierid=?,GUARANTEEDATE=?,M_LOT_ID=?,SERNO=?,LOT=?,description=? where id=?";
			
			pstmt.setInt(1, uId);
			String gDate =   (String) event.getParameterValue("guaranteedate");
			java.sql.Date gD=null;
			try{
				if(Validator.isNotNull(gDate))gD=QueryUtils.parseInputDate(gDate, true, SQLTypes.DATE);
			}catch(Throwable t2){
				logger.error("Fail to get guaranteedate according to "+ gDate, t2);
			}
			if(gD!=null)pstmt.setDate(2, gD); //GUARANTEEDATE
			else
				pstmt.setNull(2, Types.DATE);
			if( m_lot_id!=-1){
				pstmt.setInt(3, m_lot_id);//M_LOT_ID
				pstmt.setString(5, (String) event.getParameterValue("lot"));//LOT
			}	
			else{
				pstmt.setNull(3, Types.NUMERIC);
				pstmt.setNull(5, Types.VARCHAR);//LOT
			}
			String serno=(String) event.getParameterValue("serno");
			if(Validator.isNotNull(serno))
				pstmt.setString(4,serno );//SERNO
			else
				pstmt.setNull(4, Types.VARCHAR);

			pstmt.setString(6, setInstanceName);
			pstmt.setInt(7, instanceId);
			
			pstmt.executeUpdate();
			pstmt.close();
			pstmt = conn.prepareStatement(UPDATE_INSTANCE);
			for (int i = 0; i < attributes.size(); i++) {
				int  attributeId= Tools.getInt(((List) attributes.get(i)).get(0), -1);
				String aId = "A" +attributeId;
				
				String instanceValue = (String) event.getParameterValue(aId);
				String type = (String) ((List) attributes.get(i)).get(1);
				if ("L".equals(type)) {
					// so instanceValue should be id of m_attributevalue
					valueId = Tools.getInt(instanceValue, -1);
					if (valueId != 0) {
						instanceValue = (String) attributeValues
								.get(new Integer(valueId));
					}else instanceValue=null; // so will not insert this one
				} else {
					valueId = -1;

				}
				if(Validator.isNull(instanceValue)){
					pstmtDelete.setInt(1, instanceId);
					pstmtDelete.setInt(2, attributeId);
					pstmtDelete.executeUpdate();
				}else{
				//modifieddate=SYSDATE,modifierid=?, M_ATTRIBUTEVALUE_ID=?,value=?, VALUENUMBER=? WHERE M_ATTRIBUTESETINSTANCE_ID=? AND M_ATTRIBUTE_ID=?";
				pstmt.setInt(1, uId);
				if (valueId != -1)
					pstmt.setInt(2, valueId);
				else
					pstmt.setNull(2,Types.NUMERIC);
				pstmt.setString(3, instanceValue);
				try{
					pstmt.setBigDecimal(4,  new BigDecimal(instanceValue));
				}catch(Throwable b){
					pstmt.setNull(4,Types.NUMERIC );
				}
				pstmt.setInt(5, instanceId);
				pstmt.setInt(6, attributeId);
				
				int cnt= pstmt.executeUpdate();
				if(cnt==0){
					// do create 
					pstmt2.setInt(1, clientId);
					pstmt2.setInt(2, orgId);
					pstmt2.setInt(3, uId);
					pstmt2.setInt(4, uId);
					pstmt2.setInt(5, instanceId);
					pstmt2.setInt(6, Tools.getInt(
							((List) attributes.get(i)).get(0), -1));// attributeId
					if (valueId != -1)
						pstmt2.setInt(7, valueId);
					else
						pstmt2.setNull(7,Types.NUMERIC);
					pstmt2.setString(8, instanceValue);
					try{
						pstmt2.setBigDecimal(9,  new BigDecimal(instanceValue));
					}catch(Throwable b){
						pstmt2.setNull(9,Types.NUMERIC );
					}
					pstmt2.executeUpdate();					
				}
				}// end instanceValue is not null
			}
		} catch (Throwable t) {
			logger.error("failed when attributesetid=" + setId, t);
			throw new NDSException("@exception@", t);
		} finally {
			if (pstmt2 != null) {
				try {
					pstmt2.close();
				} catch (Throwable t2) {
				}
			}
			if (pstmt != null) {
				try {
					pstmt.close();
				} catch (Throwable t2) {
				}
			}
			if (conn != null) {
				try {
					conn.close();
				} catch (Throwable t2) {
				}
			}
		}
		ValueHolder v = new ValueHolder();
		v.put("next-screen",WebKeys.NDS_URI+"/pdt/attributesetinstance.jsp?id="+instanceId );
		v.put("message", "Updated.");
		return v;

	}	
	/**
	 * Create a new set instance
	 * @param event
	 * @return
	 * @throws NDSException
	 * @throws RemoteException
	 */
	private ValueHolder doCreate(DefaultWebEvent event) throws NDSException,
			RemoteException {
		int setId = Tools.getInt(event.getParameterValue("setid"), -1);
		int instanceId ;
		
		String dir = TableManager.getInstance().getTable(
				"M_ATTRIBUTESETINSTANCE").getSecurityDirectory();
		event.setParameter("directory", dir);
		User usr = helper.getOperator(event);
		int clientId = usr.adClientId;
		int orgId = usr.adOrgId;
		int uId = usr.id.intValue();

		helper.checkDirectoryWritePermission(event, usr);


		QueryEngine engine = QueryEngine.getInstance();

		List attributes = engine
				.doQueryList("select a.id , a.ATTRIBUTEVALUETYPE from m_attribute a, m_attributeuse u where a.id=u.m_attribute_id and u.m_attributeset_id="
						+ setId + " order by u.orderno asc");
		HashMap attributeValues = new HashMap(); //key vid (Integer), value:
												 // value (String)
		for (int i = 0; i < attributes.size(); i++) {
			List v = engine
					.doQueryList("select v.id, v.value from m_attributevalue v where v.m_attribute_id="
							+ ((List)attributes.get(i)).get(0));
			for (int j = 0; j < v.size(); j++) {
				attributeValues.put(new Integer(Tools.getInt(((List) v.get(j))
						.get(0), -1)), ((List) v.get(j)).get(1));
			}
		}

		Connection conn = null;
		PreparedStatement pstmt = null;
		PreparedStatement pstmt2 = null;

		int valueId;
		String value;
		
		try {
			conn = engine.getConnection();
			String lot=(String) event.getParameterValue("lot");
			int m_lot_id=-1;
			if(Validator.isNotNull(lot)){
				try{
					m_lot_id= Tools.getInt(engine.doQueryOne("select id from m_lot where name=" + QueryUtils.TO_STRING(lot)+" and ad_client_id="+clientId, conn ),-1);
				}catch(Throwable ttt){
					logger.error("Fail to load m_lot id according to name="+ lot, ttt);
				}
			}
			pstmt = conn.prepareStatement(INSERT_SETINSTANCE);
			pstmt2 = conn.prepareStatement(INSERT_INSTANCE);

			String setInstanceName = (String) event.getParameterValue("name");

			// setinstance
			instanceId = engine.getSequence("M_AttributeSetInstance", conn);
			pstmt.setInt(1, instanceId);
			pstmt.setInt(2, clientId);
			pstmt.setInt(3, orgId);
			pstmt.setInt(4, uId);
			pstmt.setInt(5, uId);
			pstmt.setInt(6, setId);
			String gDate =   (String) event.getParameterValue("guaranteedate");
			java.sql.Date gD=null;
			try{
				if(Validator.isNotNull(gDate))gD=QueryUtils.parseInputDate(gDate, true, SQLTypes.DATE);
			}catch(Throwable t2){
				logger.error("Fail to get guaranteedate according to "+ gDate, t2);
			}
			if(gD!=null)pstmt.setDate(7, gD); //GUARANTEEDATE
			else
				pstmt.setNull(7, Types.DATE);
			if( m_lot_id!=-1){
				pstmt.setInt(8, m_lot_id);//M_LOT_ID
				pstmt.setString(10, (String) event.getParameterValue("lot"));//LOT
			}	
			else{
				pstmt.setNull(8, Types.NUMERIC);
				pstmt.setNull(10, Types.VARCHAR);//LOT
			}
			String serno=(String) event.getParameterValue("serno");
			if(Validator.isNotNull(serno))
				pstmt.setString(9,serno );//SERNO
			else
				pstmt.setNull(9, Types.VARCHAR);

			pstmt.setString(11, setInstanceName);
			//m_attributeset_id 6,description, GUARANTEEDATE,M_LOT_ID,SERNO,LOT			
			pstmt.executeUpdate();
			for (int i = 0; i < attributes.size(); i++) {
				String aId = "A" + ((List) attributes.get(i)).get(0);
				String instanceValue = (String) event.getParameterValue(aId);
				String type = (String) ((List) attributes.get(i)).get(1);
				if ("L".equals(type)) {
					// so instanceValue should be id of m_attributevalue
					valueId = Tools.getInt(instanceValue, -1);
					if (valueId != 0) {
						instanceValue = (String) attributeValues
								.get(new Integer(valueId));
					}else instanceValue=null; // so will not insert this one
				} else {
					valueId = -1;

				}
				if(Validator.isNull(instanceValue)) continue;
				pstmt2.setInt(1, clientId);
				pstmt2.setInt(2, orgId);
				pstmt2.setInt(3, uId);
				pstmt2.setInt(4, uId);
				pstmt2.setInt(5, instanceId);
				pstmt2.setInt(6, Tools.getInt(
						((List) attributes.get(i)).get(0), -1));// attributeId
				if (valueId != -1)
					pstmt2.setInt(7, valueId);
				else
					pstmt2.setNull(7,Types.NUMERIC);
				pstmt2.setString(8, instanceValue);
				try{
					pstmt2.setBigDecimal(9,  new BigDecimal(instanceValue));
				}catch(Throwable b){
					pstmt2.setNull(9,Types.NUMERIC );
				}
				pstmt2.executeUpdate();

			}
		} catch (Throwable t) {
			logger.error("failed when attributesetid=" + setId, t);
			throw new NDSException("@exception@", t);
		} finally {
			if (pstmt2 != null) {
				try {
					pstmt2.close();
				} catch (Throwable t2) {
				}
			}
			if (pstmt != null) {
				try {
					pstmt.close();
				} catch (Throwable t2) {
				}
			}
			if (conn != null) {
				try {
					conn.close();
				} catch (Throwable t2) {
				}
			}
		}
		ValueHolder v = new ValueHolder();
		v.put("next-screen",WebKeys.NDS_URI+"/pdt/attributesetinstance.jsp?id="+instanceId );
		v.put("message", "Created.");
		return v;

	}
}