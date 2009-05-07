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
public class CreateAttributeSetInstances extends Command{
	private final static String INSERT_SETINSTANCE="insert into m_attributesetinstance(id,ad_client_id,ad_org_id,isactive,modifieddate,creationdate,modifierid, ownerid,m_attributeset_id,description)values(?,?,?,'Y',sysdate,sysdate,?,?,?,?)";
	private final static String INSERT_INSTANCE="insert into M_ATTRIBUTEINSTANCE(id,ad_client_id,ad_org_id,isactive,modifieddate,creationdate,modifierid, ownerid,M_ATTRIBUTESETINSTANCE_ID,M_ATTRIBUTE_ID,M_ATTRIBUTEVALUE_ID,value)values(get_sequences('M_ATTRIBUTEINSTANCE'),?,?,'Y',sysdate,sysdate,?,?,?,?,?,?)";

    /**
     * @param event - special parameters:
     *  objectid - m_attributeset.id
     *  r0-rn    - naming rule seperator, n is the count+1 of m_attributeset.attributes
     *  instance - ids of attributes to be created, id in format 
     * 			"_attributevalue.id1_attributevalue.id2_attributevalue.id3"
     */
    public ValueHolder execute(DefaultWebEvent event) throws NDSException, RemoteException{
    	//logger.debug(event.toDetailString());
    	String dir= TableManager.getInstance().getTable("M_ATTRIBUTESETINSTANCE").getSecurityDirectory();
    	event.setParameter("directory",  dir);
		User usr =helper.getOperator(event);
		int clientId= usr.adClientId;
		int orgId= usr.adOrgId;
		int uId= usr.id.intValue();
		
    	helper.checkDirectoryWritePermission(event, usr);
    	
    	int objectId= Tools.getInt(event.getParameterValue("objectid",true), -1);
    	String[] instances= event.getParameterValues("instance");
    	if(instances==null || instances.length==0) throw new NDSException("@select-at-least-one@");
    	
    	QueryEngine engine=QueryEngine.getInstance();
    	
    	List attributes=engine.doQueryList("select a.id from m_attribute a, m_attributeuse u where a.ATTRIBUTEVALUETYPE='L' and a.id=u.m_attribute_id and u.m_attributeset_id="+objectId+" order by u.orderno asc");
    	if(attributes.size()<1) throw new NDSException("Not find List type attribute in set id="+ objectId);
    	HashMap attributeValues=new HashMap(); //key vid (Integer), value: value (String)
    	for(int i=0;i< attributes.size();i++){
    		List v= engine.doQueryList("select v.id, v.value from m_attributevalue v where v.m_attribute_id="+attributes.get(i));
    		for(int j=0;j<v.size();j++){
    			attributeValues.put(new Integer( Tools.getInt(((List)v.get(j)).get(0),-1)),((List)v.get(j)).get(1));
    		}
    	}
    	String[] seps= new String[attributes.size()+1];
    	for(int i=0;i<attributes.size()+1;i++){
    		seps[i]=(String) event.getParameterValue("r"+i,true);
    	}
    	int pos=0;
    	
    	Connection conn=null;
    	PreparedStatement pstmt=null;
    	PreparedStatement pstmt2=null;
    	
    	int  valueId;
    	String value;
    	int failCount=0,successCount=0;
    	boolean bOK;
    	try{
    	conn= engine.getConnection();
    	pstmt=conn.prepareStatement(INSERT_SETINSTANCE);
    	pstmt2=conn.prepareStatement(INSERT_INSTANCE);
    	for(int i=0;i<instances.length;i++){
    		pos=0;
    		StringBuffer setInstanceName=new StringBuffer( seps[0]);
    		
    		StringTokenizer st=new StringTokenizer(instances[i],"_");
    		while(st.hasMoreTokens()){
    			// every value will construct one instance value
    			valueId=Tools.getInt( st.nextToken(),-1);
    			value= (String) attributeValues.get(new Integer(valueId));
    			setInstanceName.append(value).append(seps[++pos]);
    		}
    		// setinstance
    		int instanceId= engine.getSequence("M_AttributeSetInstance", conn);
    		pstmt.setInt(1, instanceId);
    		pstmt.setInt(2, clientId);
    		pstmt.setInt(3,orgId);
    		pstmt.setInt(4,uId);
    		pstmt.setInt(5,uId);
    		pstmt.setInt(6,objectId);
    		pstmt.setString(7, setInstanceName.toString());
    		try{
    			pstmt.executeUpdate();
    			bOK=true;
    		}catch(Throwable t){
    			logger.error("Fail to craete one m_attributesetinstance with name="+ setInstanceName.toString()+":"+ t.getMessage());
    			failCount++;
    			bOK=false;
    		}
    		if(bOK){
    			pos=0;
	    		st=new StringTokenizer(instances[i],"_");
	    		while(st.hasMoreTokens()){
	    			// every value will construct one instance value
	    			valueId=Tools.getInt( st.nextToken(),-1);
	    			value= (String) attributeValues.get(new Integer(valueId));
	        		pstmt2.setInt(1, clientId);
	        		pstmt2.setInt(2,orgId);
	        		pstmt2.setInt(3,uId);
	        		pstmt2.setInt(4,uId);
	        		pstmt2.setInt(5,instanceId);
	        		pstmt2.setInt(6, Tools.getInt(attributes.get(pos),-1) );
	        		pstmt2.setInt(7,valueId);
	        		pstmt2.setString(8,value);
	        		pstmt2.executeUpdate();
	    			pos++;
	    		}    
	    		// update attribute set instance values
	    		ArrayList params= new ArrayList();
	    		params.add(new Integer(instanceId));
				engine.executeStoredProcedure("M_ATTRIBUTESETINSTANCE_SYNC",params, false,conn);
    		}
    		successCount++;
    		
    	}
    	}catch(Throwable t){
    		logger.error("failed when attributesetid="+objectId, t);
			throw new NDSException("@exception@", t);
    	}finally{
    		if(pstmt2!=null) {try{pstmt2.close();}catch(Throwable t2){}}
    		if(pstmt!=null) {try{pstmt.close();}catch(Throwable t2){}}
    		if(conn!=null) {try{conn.close();}catch(Throwable t2){}}
    	}
        ValueHolder v = new ValueHolder();
        

        v.put("message","Finished with success "+successCount+", and failed " + failCount+".") ;
         return v;

    }
}