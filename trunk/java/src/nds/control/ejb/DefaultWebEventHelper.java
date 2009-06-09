/******************************************************************
*
*$RCSfile: DefaultWebEventHelper.java,v $ $Revision: 1.13 $ $Author: Administrator $ $Date: 2006/07/12 10:09:15 $
*
********************************************************************/
package nds.control.ejb;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Properties;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONObject;

import nds.control.web.*;
import nds.control.event.DefaultWebEvent;
import nds.control.event.NDSEventException;
import nds.control.event.NDSObjectNotFoundException;
import nds.control.util.AuditUtils;
import nds.control.util.DirectoryCache;
import nds.control.util.EJBUtils;
import nds.control.util.ValueHolder;
import nds.log.Logger;
import nds.log.LoggerManager;
import nds.mail.NotificationManager;
import nds.query.*;
import nds.schema.Column;
import nds.schema.Table;
import nds.schema.TableManager;
import nds.security.Directory;
import nds.security.NDSSecurityException;
import nds.security.Permissions;
import nds.security.User;
import nds.util.JNDINames;
import nds.util.NDSException;
import nds.util.NDSRuntimeException;
import nds.util.PairTable;
import nds.util.StringHashtable;
import nds.util.StringUtils;
import nds.util.Tools;
import nds.util.Validator;
/**
 * Handling DefaultWebEvent information. There's many implicit meaning contained in this event,
 * such as:
 *      "operatorid" contains User Id who triggered this event
 *      "id"  normally means the main object's id to be handled
 *
 * This class wrappers some methods commonly used related to DefaultWebEvent
 */
public class DefaultWebEventHelper {

    private static DirectoryCache dcInstance=null;

    private static Logger logger= LoggerManager.getInstance().getLogger(DefaultWebEventHelper.class.getName());

    private static final String GET_SECURITY_FILTER="select sqlfilter, filterdesc from groupperm where groupid in (select groupid from groupuser where userid=? ) and directoryid in (select id from directory where ad_table_id=?) and bitand(permission,?)+0=?";
    private static final int DEFAULT_CACHE_TIME_OUT= 60 *30;// default to 30 minutes
    private static final int DEFAULT_CACHE_MAXIMUM_SIZE=300;// default to 30 elements cached for one session

    public static final String PARAM_SQL_CONNECTION="java.sql.Connection";
    
    private static final String GET_USER="select u.name, u.passwordhash, u.isenabled, u.isemployee,u.isadmin, u.description, c.domain, u.ad_client_id, u.ad_org_id, u.LANGUAGE from users u, ad_client c where u.id=? and c.id=u.ad_client_id";
    private static String GET_PERMISSION="select GetUserPermission(?,?) from dual";
    private static String GET_DIRECTORY="select id from directory where name=?";
    private StateMachine machine; 
    public final static String PARENT_NOT_FOUND="@parent-record-not-found@";
	private final static String CREATE_ATTRIBUTEDETAIL="insert into m_attributedetail(id,ad_client_id,ad_org_id,ownerid, modifierid, "+
	 "creationdate,modifieddate, isactive, ad_table_id, record_id, m_product_id,M_ATTRIBUTESETINSTANCE_ID,qty,status)"+
	 " select get_sequences('M_ATTRIBUTEDETAIL'), u.ad_client_id, u.ad_org_id, u.id, u.id, sysdate,sysdate,'Y', ?,?,?,?,?,1 from users u where id=? ";
	private final static String DELETE_ATTRIBUTEDETAIL="delete from m_attributedetail where ad_table_id=? and record_id=?";
	private final static String CREATE_ATTRIBUTEDETAIL_BY_COPY="insert into m_attributedetail(id,ad_client_id,ad_org_id,ownerid, modifierid, "+
	 "creationdate,modifieddate, isactive, ad_table_id, record_id, m_product_id,M_ATTRIBUTESETINSTANCE_ID,qty,status)"+
	 " select get_sequences('M_ATTRIBUTEDETAIL'), u.ad_client_id, u.ad_org_id, u.id, u.id, sysdate,sysdate,'Y'"+
	 ",d.ad_table_id,?,d.m_product_id,d.M_ATTRIBUTESETINSTANCE_ID,d.qty,1 from users u, m_attributedetail d where u.id=? and d.ad_table_id=? and d.record_id=? ";
    
    public DefaultWebEventHelper() {}
    /**
     * Used for call other command in one command handling process
     * @param machine
     */
    public void setStateMachine(StateMachine machine){
    	this.machine= machine;
    }    
    /**
     * Initialize table manager
     * @return table manager
     */
    public TableManager getTableManager(){
        TableManager tm=nds.schema.TableManager.getInstance();
        if( !tm.isInitialized()){
            Properties conf= EJBUtils.getApplicationConfigurations();
            String tablePath= conf.getProperty("schema.directory");
            String converter= conf.getProperty("schema.defaultTypeConverter");

            //String tablePath="file:/aic/tables";
            logger.debug("Initializing TableManager, using path "+tablePath);
            Properties props=new Properties();
            props.setProperty("defaultTypeConverter",converter);
            props.setProperty("directory", tablePath);
            tm.init(props);
        }
        return tm;
    }

    /**
     * Check operator as Admin, else throw NDSSecurityException
     */
    public void checkOperatorIsAdmin( DefaultWebEvent event ) throws NDSException, RemoteException {
        User usr= this.getOperator(event);
        if( usr.getIsAdmin() != 1) {
            throw new NDSSecurityException("@must-be-admin-to-execute-this-command@.");
        }
    }

    public void checkDirectoryWritePermission(DefaultWebEvent event, User usr) throws NDSException {
    	if ("root".equals(usr.getName())) return;
    	String dir=(String)event.getParameterValue("directory");
        int perm=0;
        int permission=Directory.WRITE;
        try {
            perm= getPermissions(dir, usr.getId().intValue());
        } catch(Exception e) {
            logger.debug("Errors found", e);
            throw new NDSException("@exception@",e);
        }
        if((perm & permission)!= permission) {
            throw new NDSSecurityException("@no-write-permission@");
        }
    }
    public void checkDirectoryReadPermission(DefaultWebEvent event, User usr) throws NDSException {
    	if ("root".equals(usr.getName())) return;
    	String dir=(String)event.getParameterValue("directory");
        int perm=0;
        int permission=Directory.READ;
        try {
            perm= getPermissions(dir, usr.getId().intValue());
        } catch(Exception e) {
            logger.debug("Errors found", e);
            throw new NDSException("@exception@",e);
        }
        if((perm & permission)!= permission) {
            throw new NDSSecurityException("@no-permission@");
        }
    }
    public int getPermissions(String dirName, int userId)throws NDSException, java.rmi.RemoteException {
        Connection con=QueryEngine.getInstance().getConnection();
        PreparedStatement pstmt= null;
        ResultSet rs=null;
        try {
            pstmt= con.prepareStatement(GET_PERMISSION);
            pstmt.setString(1, dirName);
            pstmt.setInt(2, userId);
            rs= pstmt.executeQuery();
            if( rs.next() ){
                return rs.getInt(1);
            }
        } catch (Exception ce) {
            logger.error("Error getting permission for "+dirName+" of userId="+userId);
            throw new NDSRuntimeException("Error getting SbSecurityManager",ce);
        }finally{
            if( rs !=null){ try{ rs.close();}catch(Exception e){}}
            if( pstmt !=null){ try{ pstmt.close();}catch(Exception e){}}
            if( con !=null){ try{ con.close();}catch(Exception e){}}
        }
        return 0;
    }
    /**
     * Check <code>permission</code> according to event
     * @param event the event triggers this method. The event should has following parameter
     *          "operatorid" - the user who triggered this event
     *          "directoryid" - the directory from which the <code>obj</code> is abtained.
     * @param  permission Directory permission, should be of following values:
     *          Directory.READ, Directory.WRITE
     * @throws NDSException if permission not authorized( NDSSecurityException), or other exceptions occur.
     */

    private void checkDirectoryPermission(DefaultWebEvent event, int permission) throws NDSException {
        int operatorId=getInt((String)event.getParameterValue("operatorid"), -1);
        String dir=(String)event.getParameterValue("directory");
        int perm=0;
        try {
            perm= getPermissions(dir, operatorId);
        } catch(Exception e) {
            logger.debug("Errors found", e);
            throw new NDSException("@exception@",e);
        }
        if((perm & permission)!= permission) {
            String s=null;
            switch(permission) {
                    case Directory.READ:
                    s="@no-read-perission@";
                    break;
                    case Directory.WRITE:
                    s="@no-write-permission@";
                    break;
                    default:
                    s="@no-permssion@";
                    break;
            }
            throw new NDSSecurityException(s);
        }

    }
    /**
     * Return the directory on which this event is triggered from.
     * The directory id must be a parameter in event named "directoryid"
     * @return a valid directory, which must not be null
     * @throws NDSEventException if any exception occurs, or the directory not found
     */
/*    public Directory getDirectory(DefaultWebEvent event) throws NDSEventException {
        Directory dir=null;
        int dirId=-1;
        try {
            dirId=getInt((String)event.getParameterValue("directoryid"), -1);
            DirectoryHome dirHome= EJBUtils.getDirectoryHome();
            try {
                dir= dirHome.findByPrimaryKey(new Integer(dirId));
            } catch(ObjectNotFoundException er) {}
        }
        catch(Exception e) {
            logger.debug("Errors found when trying to get directory from event.",e);
            throw new NDSEventException("异常", e);
        }
        if( dir==null)
            throw new NDSEventException("内部错误，未指明操作所在目录, 或目录未找到.( event name="+ event.getEventName()+
                                        ", command="+ event.getParameterValue("command")+", directory id="+ dirId+")");
        return dir;

    }*/
    public int getDirectoryId(DefaultWebEvent event) throws NDSEventException {
        String dirName="";
        dirName=(String)event.getParameterValue("directory");
        int dirId=-1;
        PreparedStatement pstmt= null;
        Connection con=null;
        ResultSet rs=null;
        try {
            con=QueryEngine.getInstance().getConnection();
            pstmt= con.prepareStatement(GET_DIRECTORY);
            pstmt.setString(1, dirName);
            rs= pstmt.executeQuery();
            if( rs.next() ){
                return rs.getInt(1);
            }
        } catch (Exception ce) {
            logger.error("Error getting permission for "+dirName, ce);
            throw new NDSEventException("@exception@",ce);
        }finally{
            if( rs !=null){ try{ rs.close();}catch(Exception e){}}
            if( pstmt !=null){ try{ pstmt.close();}catch(Exception e){}}
            if( con !=null){ try{ con.close();}catch(Exception e){}}
        }
        throw new NDSEventException("@directory-not-found@.( event name="+ event.getEventName()+
                                        ", command="+ event.getParameterValue("command")+", directory name="+ dirName+")");
    }
    /**
     * Create m_attributedetail records according to existing one
     * @param table the table_id column of the existing record and new one
     * @param userId operator
     * @param copyFromObjectId record_id column of the existing record
     * @param copyToObjectId new record_id 
     * @param con
     * @throws Exception
     */
    public void createAttributeDetailRecordsByCopy(Table table, int userId,  int copyFromObjectId,
    		int copyToObjectId, Connection con) throws Exception{
    	
    	PreparedStatement pstmt= null;
        TableManager manager=TableManager.getInstance();
        int realTableId=  manager.getTable(table.getRealTableName()).getId();
        try {
        	pstmt= con.prepareStatement(CREATE_ATTRIBUTEDETAIL_BY_COPY);
            pstmt.setInt(1,copyToObjectId );
            pstmt.setInt(2,userId );
            pstmt.setInt(3,realTableId );
            pstmt.setInt(4,copyFromObjectId );
            pstmt.executeUpdate();
        } catch (Exception ce) {
            logger.error("Error createAttributeDetailRecords table="+ table+", user="+ userId+" copyFromObjectId="+ copyFromObjectId+" copyToObjectId="+copyToObjectId , ce);
            throw new NDSEventException("@exception@",ce);
        }finally{
            if( pstmt !=null){ try{ pstmt.close();}catch(Exception e){}}
        }    	
    }
    /**
     * Create records in m_attributedetail table 
     * @param table
     * @param jsonobj JSONArray
     * @param masterEvent event that will be duplicated 
     * @param objId item table record id
     * @param asiRelateColumnsPosInStatement array with 3 elements, 
     * 0 - ID column position in PreparedStatement of getPreparedStatementSQL
     * 1 - M_ATTRBIUTESETINSTANCE_ID column
     * 2 - *QTY* column
     * @param acamTrigger "AC" or "AM" for trigger on each newly created record
     * @throws Exception
     */
    public void execStmtOfAttributeDetailRecordsByJSON(Table table, Object jsonobj, 
    		int objId, Connection con,PreparedStatement pstmt,
    		int[] asiRelateColumnsPosInStatement, String acamTrigger) throws Exception{
    	if(jsonobj==null) return;
    	JSONArray ja=null;
    	if(jsonobj instanceof JSONArray) ja= (JSONArray)jsonobj;
    	else if(jsonobj.equals(JSONObject.NULL) ){
    		ja=new JSONArray();
    	}else if(jsonobj instanceof String){
    		if(Validator.isNull((String)jsonobj) )
    			ja= new JSONArray();
    		else
    			ja= new JSONArray((String)jsonobj);
    	}else{
    		throw new IllegalArgumentException("jsonobj type not supported:"+ jsonobj.getClass());
    	}
         try {
             if(ja.length()>0){
	             int pasiId, qty, pkid;
	             JSONArray attributePair;
	             for(int i=0;i< ja.length();i++){
	             	attributePair= ja.getJSONArray(i);
	             	pasiId=Tools.getInt( attributePair.getString(0).substring(1), -1); // first char is "P", should be removed
	             	qty= attributePair.getInt(1);
	             	pkid= (i==0? objId: QueryEngine.getInstance().getSequence(table.getName(), con));
	             	pstmt.setInt(asiRelateColumnsPosInStatement[0]/*id*/,pkid);
	             	pstmt.setInt(asiRelateColumnsPosInStatement[1]/*asiid*/,pasiId );
	             	pstmt.setInt(asiRelateColumnsPosInStatement[2]/*qty*/,qty );
	             	pstmt.executeUpdate();
	             	doTrigger(acamTrigger, table, pkid, con);
	             }
             }
         } catch (Exception ce) {
         	logger.error("Error createAttributeDetailRecords table="+ table+", jsonobj="+ jsonobj+", objid="+objId , ce);
             throw ce;
         }
    }  
    /**
     * Create records in m_attributedetail table 
     * @param table
     * @param jsonobj JSONArray
     * @param masterEvent event that will be duplicated 
     * @param objId item table record id
     * @throws Exception
     */
    public void createAttributeDetailRecordsByJSON(Table table, Object jsonobj, 
    		DefaultWebEvent masterEvent, int objId, Connection con) throws Exception{
    	if(jsonobj==null) return;
    	JSONArray ja=null;
    	if(jsonobj instanceof JSONArray) ja= (JSONArray)jsonobj;
    	else if(jsonobj.equals(JSONObject.NULL) ){
    		ja=new JSONArray();
    	}else if(jsonobj instanceof String){
    		if(Validator.isNull((String)jsonobj) )
    			ja= new JSONArray();
    		else
    			ja= new JSONArray((String)jsonobj);
    	}else{
    		throw new IllegalArgumentException("jsonobj type not supported:"+ jsonobj.getClass());
    	}
         TableManager manager=TableManager.getInstance();
         ArrayList columns=table.getAllColumns();
         Column qtyColumn=null;
         Table realTable = manager.getTable(table.getRealTableName());
         for(int i=0;i<columns.size();i++){
         	Column c= (Column) columns.get(i);
         	/**
         	 * todo here's a big de facto, column must have "qty" in name and set before other columns
         	 */
         	if(c.getType()== Column.NUMBER &&  c.isMaskSet(Column.MASK_CREATE_EDIT) && c.getName().indexOf("QTY")>-1){
         		qtyColumn = c;
         		break;
         	}
         }
         DefaultWebEvent template= (DefaultWebEvent)masterEvent.clone();
         template.getData().remove("jsonobj".toUpperCase());
         try {
             if(ja.length()>0){
	             int pasiId, qty;
	             JSONArray attributePair;
	             for(int i=0;i< ja.length();i++){
	             	attributePair= ja.getJSONArray(i);
	             	pasiId=Tools.getInt( attributePair.getString(0).substring(1), -1); // first char is "P", should be removed
	             	qty= attributePair.getInt(1);
	             	if(i==0){
	             		// master record update
	             		con.createStatement().executeUpdate("update "+ realTable.getName()+
	             				" set m_attributesetinstance_id="+  pasiId+", "+ qtyColumn.getName()+"="+qty
								+" where id="+objId);
	             		continue;
	             	}
	             	DefaultWebEvent e= (DefaultWebEvent)template.clone();
	             	e.setParameter("M_ATTRIBUTESETINSTANCE_ID", String.valueOf(pasiId));
	             	e.setParameter(qtyColumn.getName(), String.valueOf(qty));
	             	ValueHolder v=this.handleEvent(e);
	             	if( Tools.getInt(v.get("code"), 0) !=0) throw new NDSException((String)v.get("message"));
	             }
             }
         } catch (Exception ce) {
         	logger.error("Error createAttributeDetailRecords table="+ table+", jsonobj="+ jsonobj+", objid="+objId , ce);
             throw ce;
         }
    }        
    /**
     * Create records in m_attributedetail table 
     * @param table
     * @param jsonobj JSONArray
     * @param user
     * @param objId item table record id
     * @param productId m_product_id value
     * @param deletePrevious delete previous record or not, when item is newly created, set this to false
     * else, set to true
     * @throws Exception
     * @deprecated
     */
    private void createAttributeDetailRecordsByJSON_old(Table table, Object jsonobj, int userId, 
    		int productId, int objId, Connection con, boolean deletePrevious) throws Exception{
    	if(jsonobj==null) return;
    	JSONArray ja=null;
    	if(jsonobj instanceof JSONArray) ja= (JSONArray)jsonobj;
    	else if(jsonobj.equals(JSONObject.NULL) ){
    		ja=new JSONArray();
    	}else if(jsonobj instanceof String){
    		if(Validator.isNull((String)jsonobj) )
    			ja= new JSONArray();
    		else
    			ja= new JSONArray((String)jsonobj);
    	}else{
    		throw new IllegalArgumentException("jsonobj type not supported:"+ jsonobj.getClass());
    	}
    	PreparedStatement pstmt= null;
         TableManager manager=TableManager.getInstance();
         int realTableId=  manager.getTable(table.getRealTableName()).getId();
         try {
         	 pstmt= con.prepareStatement(DELETE_ATTRIBUTEDETAIL);
             pstmt.setInt(1,realTableId );
             pstmt.setInt(2,objId );
             pstmt.executeUpdate();
             pstmt.close();
             
             if(ja.length()>0){
         	 pstmt= con.prepareStatement(CREATE_ATTRIBUTEDETAIL);
             pstmt.setInt(1,realTableId );
             pstmt.setInt(2,objId );
             pstmt.setInt(3,productId );
             pstmt.setInt(6,userId );
             int pasiId, qty;
             JSONArray attributePair;
             for(int i=0;i< ja.length();i++){
             	attributePair= ja.getJSONArray(i);
             	pasiId=Tools.getInt( attributePair.getString(0).substring(1), -1); // first char is "P", should be removed
             	qty= attributePair.getInt(1);
             	pstmt.setInt(4, pasiId);
             	pstmt.setInt(5, qty);
                pstmt.executeUpdate();
             }
             }
         } catch (Exception ce) {
             logger.error("Error createAttributeDetailRecords table="+ table+", jsonobj="+ jsonobj+" user="+ userId+" pdtid="+ productId+" objid="+objId , ce);
             throw new NDSEventException("@exception@",ce);
         }finally{
             if( pstmt !=null){ try{ pstmt.close();}catch(Exception e){}}
         }
    }    
    
    
    /**
     * Return operator who trigger this event, normally the operator's user id is set in
     * event.getParameterValue("operatorid"), which is set by RequestProcessor( the one who
     * generate the event).
     * @return a valid user, which will not be null definitely
     * @throws NDSEventException if any exception occurs, or the user not found
     */
    public User getOperator(DefaultWebEvent event) throws NDSEventException {
        User usr=null;
        Connection con=null;
        PreparedStatement pstmt= null;
        ResultSet rs= null;
        try {
            con= QueryEngine.getInstance().getConnection();
            int operatorId=getInt((String)event.getParameterValue("operatorid"), -1);
//            logger.debug("operator id="+ operatorId);
            pstmt= con.prepareStatement(GET_USER);
            pstmt.setInt(1,operatorId);
            rs= pstmt.executeQuery();
            //"select name, passwordhash, isenabled, isemployee,isadmin, description from users where id=?";
            if( rs.next()){
                usr= new User();
                String name= rs.getString(1);
                String passwd= rs.getString(2);
                int isEnabled= rs.getInt(3);
                int isEmployee= rs.getInt(4);
                int isAdmin= rs.getInt(5);
                String desc= rs.getString(6);
                usr.setClientDomain(rs.getString(7));
                usr.adClientId = rs.getInt(8);
                usr.adOrgId = rs.getInt(9);
                usr.locale= StringUtils.fromLanguageId(rs.getString(10));
                usr.setId(new Integer(operatorId));
                
                usr.setName(name);
                usr.setPasswordHash(passwd);
                usr.setIsEnabled(isEnabled);
                usr.setIsEmployee(isEmployee);
                usr.setIsAdmin(isAdmin);
                usr.setDescription(desc);
            }
        }
        catch(Exception e) {
            logger.debug("Errors found when trying to get operator from event.",e);
            throw new NDSEventException("@exception@", e);
        }finally{
            if( rs!=null){try{rs.close();}catch(Exception e){}}
            if( pstmt!=null){try{pstmt.close();}catch(Exception e){}}
            if( con!=null){try{con.close();}catch(Exception e){}}
        }
        if( usr==null)
            throw new NDSEventException("@operator-not-found@.( event name="+ event.getEventName()+
                                        ", command="+ event.getParameterValue("command")+")");
        return usr;

    }
    /**
     * Get main object to be handled according to event's parameters: "id".
     * Note this method is limited to only object of those table whose PK is "id".
     * 由于以上局限性，请考虑必要时自行实现有关对象的获得。
     *
     * @param event - the event which should have following parameters set:
     *              "id" the object id to be retrieved.
     * @param ejbObjectName - name of ejb object, note this object name must be one
     *              that declared in EJBUitls's method hashtable
     *              注意名称必须在EJBUtils的method hashtable中声明

    public ValueHolder createSuccessInfo(String message) {
        ValueHolder v=new ValueHolder();
        v.put("code", "0");// means successfully
        v.put("message", message);
        return v;
    }
    /**
     * Find id of specified object by it's alternate key's value, throw exception if object not found or errors occur.
     * <p>
     * Not all ejb can be found using thid method, following requirments are needed:<br>
     *      1. ejb relate database table must has primary key named "id"
     *      2. ejb name is identical to table name
     *
     * @param ejbObjectName name of ejb object. <BR>
     *      <B>PLEASE INFORM THE PROJECT MANAGER IF YOU FOUND THE OBJECT NAME IS NOT IDENTICAL TO DATABASE TABLE NAME.<B>
     * @param keyName alternate key's name, normally it's a String type and act as displayer that object
     * @param keyValue the value of keyName, used for identify the object.
     * @return object id
     * @throws NDSObjectNotFoundException if object not found, or errors occur
     */
    public int findObjectIdByAlternateKey(String ejbObjectName, String keyName, String keyValue) throws NDSObjectNotFoundException {
        Exception ex=null;
        String sql="select id from "+ ejbObjectName+" where "+keyName+" = '"+ keyValue+"'";
        try {
            nds.query.QueryEngine engine= nds.query.QueryEngine.getInstance();
            ResultSet rs=engine.doQuery(sql);
            if( rs.next()) {
                return rs.getInt(1);
            }
            try {
                rs.close();
            } catch(Exception ee) {}
        }
        catch(Exception e) {
            logger.debug("Error when doing "+sql);
            ex=e;
        }
        NDSObjectNotFoundException onfe=new NDSObjectNotFoundException("@object-not-found@:"+ejbObjectName+"("+ keyName+"="+ keyValue+")");
        if( ex !=null)
            onfe.setNextException(ex);
        throw onfe;

    }
    /**
     * This call stored procedure synchronously, for long execution time procedures, using
     *  #executeStoredProcedure(String,Collection,boolean, boolean) instead
     */
    public SPResult executeStoredProcedure(String procedureName, Collection params, boolean hasReturnValue)throws QueryException{
        return QueryEngine.getInstance().executeStoredProcedure(procedureName,params,hasReturnValue);
    }
    /**
     * 本方法禁止同名、同参数的存储过程在内存中有两个线程同时执行，如果要允许这种操作，请调用
     *  #executeStoredProcedureBackground(String,Collection,boolean, boolean，boolean)
     *  @see #executeStoredProcedureBackground(String,Collection,boolean, boolean，boolean)
     */
    public SPResult executeStoredProcedureBackground(String procedureName, Collection params, boolean hasReturnValue, User usr)throws QueryException{
        return executeStoredProcedureBackground(procedureName,params,hasReturnValue,usr,true/*disallow simutaneous call*/);
    }
    /**
     * Doing stored procedure in background, in this case, normally the stored procedure
     *        will return a message in <code>SPResult</code>, the caller will store the returned message to
     *        a file so as to let user know about the result, if <code>hasReturnValue</code> is set to false,
     *        the return message will be automatically generated with execution duration logged. <p>
     *
     *        The realization of asynchronous call is using  Message Driven Bean, AsyncControllerBean,
     *        first make all parameters of this method to DefaultWebEvent, with names as: <br>
     *        <ul>
     *        <li>EventName is "ProcedureEvent" </li>
     *        <li>"procedureName" - procedureName   </li>
     *        <li>"params"        - params ( if <code>params</code> is not serializable, the interal process will convert it
     *                          to a serializable one</li>
     *        <li>"hasReturnValue"- hasReturnValue(Boolean) </li>
     *        <li>"resultFile"   - absolute result file name(include path)that will store result information</li>
     *        <li>"operator"     - the commander of this calle
     *        <li>"singleton"    - whether allow simutaneous call or not</li>
     *        </ul>
     *        The final executer will be nds.control.ejb.ProcedureHandler
     * @param procedureName Stored procedure name
     * @param params procedure parameters, elements can be String, Integer, or Float, others are not supported
     *        in this release.
     * @param hasReturnValue True if procedure has return value, in this case, one more parameter( OUT ) will be
     *        appended to <code>params</code> as type of String.
     * @param usr the operator of this execution, output file name will be generated according to this
     * @param isSingleton If true, the stored procedure will not be allowed to be executed simutaneously
     *        when parameters are all same. 如果存储过程的所有参数都相同，则禁止在内存中有两个操作同时进行
     * @return SPResult which will nerver be null
     * @throws QueryException
     * @see nds.control.ejb.AsyncControllerBean#onMessage
     */
    public SPResult executeStoredProcedureBackground(String procedureName, Collection params, boolean hasReturnValue, User usr, boolean isSingleton)throws QueryException{
        try{
        SPResult res=null;
            DefaultWebEvent event=new DefaultWebEvent("ProcedureEvent");
            event.put("procedureName", procedureName);
            event.put("params", ((params instanceof Serializable)?params:(new ArrayList(params))));
            event.put("hasReturnValue",new Boolean(hasReturnValue) );
            event.put("singleton", new Boolean(isSingleton));
            String rootPath=EJBUtils.getApplicationConfigurations().getProperty("export.root.nds","/aic/home");
            String resultFileName= rootPath+"/"+usr.getClientDomain()+"/"+ usr.getName() +"/"+ procedureName+".log";
            event.put("resultFile", resultFileName);
            event.put("operator", usr.getName());
            logger.debug("Procedure "+ procedureName+ " called by "+ usr.getName()+", will report log to "+ resultFileName);
            // create mdb
            AsyncControllerBean asyncControllerBean = new AsyncControllerBean();
            //asyncControllerBean.setEnvironment(hashtable);  //Specify any vendor-specific JNDI settings here
            asyncControllerBean.sendEvent(event);
            res= new SPResult("@this-command-will-run-in-background-with-log-file@:"+
            procedureName+".log");
        return res;
        }catch(Exception e){
            logger.debug("Errors found when calling Stored Procedure:"+ procedureName,e);
            throw new QueryException("Error calling "+ procedureName, e);
        }
    }
    public ValueHolder handleEventBackground(DefaultWebEvent event, String outputFileName) throws Exception{
    	
    	ClientControllerWebImpl controller=(ClientControllerWebImpl)WebUtils.getServletContextManager().getActor(nds.util.WebKeys.WEB_CONTROLLER);
    	controller.handleEventBackground(event);
    	
//        try{
        ValueHolder res=null;
/*            // create mdb
            AsyncControllerBean asyncControllerBean = new AsyncControllerBean();
            //asyncControllerBean.setEnvironment(hashtable);  //Specify any vendor-specific JNDI settings here
            asyncControllerBean.sendEvent(event);*/
            res= new ValueHolder();
            res.put("message", "@this-command-will-run-in-background-with-log-file@:"+
                    outputFileName);
        return res;
/*        }catch(Exception e){
            logger.debug("Errors found when handleEventBackground:" + event.getEventName() ,e);
            throw new NDSException("Error calling "+ procedureName, e);
        }*/

    }
    /**
     * Try get connection from event param "java.sql.Connection". The param will be set
     * when one event handler calls for another event handler, the caller may set this 
     * param so the callee can use it directly.
     * <p>
     * If that param not found in event, a new connection will be created.
     * <p>
     * Use #closeConnection(DefaultWebEvent) to try close connection or leave it alone 
     * if you use this method to gain connection. The caller will maintain the life-cycle
     * of the created connection
     * @param event
     * @return Connection either create from scratch or from event parameter.
     */
    public Connection getConnection(DefaultWebEvent event) throws QueryException{
    	Object o= event.getParameterValue(PARAM_SQL_CONNECTION);
    	if(o!=null && o instanceof Connection) return (Connection)o;
    	return QueryEngine.getInstance().getConnection();
    }
    /**
     * Close connection if <param>conn</param> is not retrieved from event
     * @param conn if is event param "java.sql.Connection", then that connection 
     * will not be closed
     * @param event may contain param "java.sql.Connection"
     */
    public void closeConnection(Connection conn, DefaultWebEvent event){
    	Object o= event.getParameterValue(PARAM_SQL_CONNECTION);
    	if(conn==o) return ;
    	try{
    		if(conn!=null)conn.close();
    	}catch(Throwable e){
    		logger.error("Can not close connection ", e);
    	}
    }
    /**
     * Create new transaction to handle event, for same transaction with parent method, call #handleEvent instead
     * for asynchronized transaction, call #handleEventBackground
     * Note caller event must have parameter named "nds.control.ejb.UserTransaction" set to "N", to avoid nested
     * transaction
     * @param event 
     * @return
     * @throws NDSException
     * @throws RemoteException
     */
    public ValueHolder handleEventWithNewTransaction(DefaultWebEvent event) throws NDSException ,RemoteException{
    	ClientControllerWebImpl controller= (ClientControllerWebImpl)WebUtils.getServletContextManager().getActor( nds.util.WebKeys.WEB_CONTROLLER );
    	return controller.handleEvent(event);
    }
    /**
     * Will call local statemachine's method, so in the same thread and transaction,
     * if want to start another transaction, call #handleEventBackground
     * @param event
     * @return
     * @throws NDSException
     * @throws RemoteException
     */
    public ValueHolder handleEvent(DefaultWebEvent event) throws NDSException ,RemoteException{
        return machine.handleEvent(event);
    	/**
         * Following process is used for EJB environment
         * deprecated since 2.0
         * 
         try{
        ValueHolder res=null;
        ClientControllerHome cch=EJBUtils.getClientControllerHome();
        return cch.create().handleEvent(event);
        }catch(javax.naming.NamingException e){
            throw new NDSEventException("无法找到模块."+e, e);
        }catch(javax.ejb.CreateException e2){
            throw new NDSEventException("无法创建模块."+e2, e2);
        }
        */
    }
    /**
     * parse int value from <code>str</code>
     * @param str normally it should be a string
     * @param defaultValue if errors found when parsing, this value will be returned
     */
    public int getInt(Object str, int defaultValue) {
        return Tools.getInt(str, defaultValue);
    }
    /**
     * A very simple permission setter, which return permission that allows every one in the
     * directory to modify and delete the object, that is "-rwdrwd"
     */
    private int getDefaultObjectPermission() {
        return defaultPerm.intValue();
    }
    private static Permissions defaultPerm= new Permissions(false,true,true,true,true,true,true);


    /**
     * Parse <code>str</code> to Date, if parse error found, will
     * @param str the object ( is a String) which has date format as defined in
     *          DefaultWebEventHelper.InputDateFormatter
     */
    /*public java.sql.Date getDate(Object str) throws NDSEventException {
        try {
            return   new java.sql.Date( inputDateFormatter.parse(""+str).getTime());
        } catch(Exception e) {
            logger.debug("Not a valid date format:"+ str);
            throw new NDSEventException("日期 "+str+
                                        " 格式不对，请用‘"+ inputDateFormatter.toPattern()+" ’形");
        }
    }*/
    /**
     * DateFormatter for display in input field, normally it's very easy to write
     */
    //private final static SimpleDateFormat inputDateFormatter=new SimpleDateFormat("yyyy/MM/dd");
    /**
    * Notify of sheet table modification
    * @param action can be JNDINames.STATUS_AUDITING,SUBMIT,PERMIT,ROLLBACK
    */
    public void Notify(  Table table, int objectId, String operatorDesc, int action, Connection con){
        String actionDesc="???", actionName="";
        switch(action){
            case JNDINames.STATUS_AUDITING:
                actionDesc="申报核准";actionName="audit";break;
            case JNDINames.STATUS_PERMIT :
                actionDesc="审核通过";actionName="permit";break;
            case JNDINames.STATUS_ROLLBACK :
                actionDesc="驳回";actionName="rollback";break;
            case JNDINames.STATUS_SUBMIT:
                actionDesc="提交";actionName="submit";break;
            default:
                logger.error("Unknow action(" + action+" ) when notify on table "+ table.getName());
        }
        try{
            String no=null;
            StringBuffer briefMsg=new StringBuffer(), detailMsg=new StringBuffer();
//            String operatorDesc= helper.getOperator(event).getDescription();
            NotificationManager nm=nds.mail.NotificationManager.getInstance();
            ResultSet rs= QueryEngine.getInstance().doQuery("select "+ table.getAlternateKey().getName() +
                    " from "+ table.getName()+ " where id=" + objectId);
            if( rs.next() ){
                no= rs.getString(1);
            }
            try{ rs.close();} catch(Exception es){}
            if(no !=null) briefMsg.append( table.getDescription(Locale.CHINA) + "("+ no + ") 被"+ operatorDesc+ actionDesc);
            else briefMsg.append( table.getDescription(Locale.CHINA) + "(id=" + objectId + ") 被"+ operatorDesc+ actionDesc);
            String webroot=nm.getProperty("weburl", "http://mit:8001/nds");
            detailMsg.append(briefMsg +" 请访问网页:" );
            detailMsg.append("\n\r");
            detailMsg.append( webroot+ "/objext/sheet_title.jsp?table="+table.getId() +"&id="+ objectId);
            nm.handleObject(table.getId(), objectId, actionName, briefMsg, detailMsg,con);

        }catch(Exception e){
            logger.error("Could not notify modification of " + table.getName()+ ", id=" +objectId, e);
        }

    }
    /**
     * 
     * @param action "AM", "BD","BM"
     * @param table
     * @param id
     * @param con
     * @since 2.0 will throw exception if found error, can it's outsider to consider whether handle it or 
     *  rollback whole process
     */
    public void doTrigger(String action, Table table, int id, Connection con) throws NDSException {
        //try{ marked after 2.0
            if( table== null || id==-1) return;
            String spName= table.getTriggerName(action);
            /**
             * If not exists, call real-table's trigger
             */
            if( spName ==null) spName= TableManager.getInstance().getTable(table.getRealTableName()).getTriggerName(action);
//            logger.info("spName =" + spName);
            if( spName ==null) return;
            // yfzhu modified 2003-12-24 for crc re-calculate, so we will find a java class to handle first
            if( execTriggerClass(spName, id,con) ==true) {
//                logger.info("exec class ok  for spName =" + spName);
                return;
            }
            ArrayList al=new ArrayList();
            al.add(new Integer(id));
            QueryEngine.getInstance().executeStoredProcedure(spName,al, false,con);
        /*}catch(Exception e){
            logger.error("Error do trigger('"+action+"')" +table+ "(id=" + id+ ")" ,e);
        }  */  	
    }
    /**
     * Get root message of exception
     * @param t
     * @return
     */
    public String getRootCauseMessage(Throwable t, Locale locale){
    	return nds.control.web.WebUtils.getExceptionMessage(t, locale);
        /*Throwable e = t;
        String s=t.getMessage();
        while (e != null) {
          if(Validator.isNotNull(e.getMessage())) s= e.getMessage();
          Throwable prev = e;
          e = e.getCause();
          if (e == prev)
            break;
        }
        return s;*/
    }
    /**
     * Trigger after records list changed, will check each records's trigger 
     * first, then trying to got "sheet" table's records. and notify it.
     * 
     * The table's parent "sheet" id will obtained first before doing trigger
     * , this is a must when doing on "bd" 
     * @param action "AM", "BD","BM"
     * @param table  
     * @param ids  id array 
     * @param con
     */
    public void doTrigger( String action, Table table, int[] ids, Connection con) throws NDSException{
    	if(table==null || ids==null || ids.length==0 ) return;
    	TableManager manager=TableManager.getInstance();
    	if( table.getTriggerName(action)!=null || manager.getTable(table.getRealTableName()).getTriggerName(action)!=null){
    		for(int i=0;i<ids.length;i++) doTrigger(action, table, ids[i], con);
    	}
    }
    /* 检查父表的存在性，如果不存在或未找到，抛出错误，这种情况发生在以下情况：
    父表的状态被改变了，而子表的界面仍然保留在那里，故用户可以对子表进行操作，导致
     父表出现错误
    举例：  m_v_inout 被提交生成了 m_v_2_inout, 而用户仍可以对m_v_inoutitem表
    中的内容进行修改。这是不允许的。
    这里存在一个bug,没有进行客户端的读权限判断
     */
    public void checkTableRows(Table table,int[] ids ,Connection con, String errMsg) throws NDSException{
    	if( table ==null || ids==null || ids.length==0) return;
    	String s=""+ids[0];
    	for(int i=0;i< ids.length;i++) {
            s +=","+ids[i];
        }
    	int cnt=-1;
    	ResultSet rs=null;
    	String sql=null;
    	try{
	    	QueryEngine engine=QueryEngine.getInstance();
	    	QueryRequestImpl query= engine.createRequest(null);
	    	query.setMainTable(table.getId());
	    	query.addSelection( table.getPrimaryKey().getId());
	    	query.addParam(table.getPrimaryKey().getId(), " in ("+ s+")");
	    	sql=query.toCountSQL();
	    	rs= con.createStatement().executeQuery( sql);
	    	rs.next();
	    	cnt=rs.getInt(1);
	    	logger.debug(sql+":"+ cnt+", ids.length="+ ids.length);
    	}catch(Exception e){
    		logger.error("found error:",e);
    		throw new NDSException("@can-not-check-records@:"+ e.getMessage());
    	}
    	finally{
    		if(rs!=null) try{rs.close();}catch(Exception ee){}
    	}
    	if (cnt!= ids.length){
    		logger.error(errMsg+":("+sql+"):"+ cnt+", ids.length="+ ids.length);
    		throw new NDSException(errMsg);
    	}
    }
    /**
     * 如果event中存在 "mainobjecttableid"将作为parent的首选
     * @param child 要寻找的父亲的子table
     * @param event
     * @return
     */
    public Table getParentTable(Table child,  DefaultWebEvent event){
    	/*int mtid=Tools.getInt(event.getParameterValue("mainobjecttableid"),-1);
    	Table supposedParent =null;
    	TableManager manager=TableManager.getInstance();
    	if (mtid !=-1) supposedParent= manager.getTable(mtid);
    	return manager.getParentTable(child,supposedParent );
    	*/
    	return child.getParentTable();
    }
    /**
     * Get parent table's ids, this method is solely for parent table am, so checked it here first
     * 
     * @param childTable the child table
     * @param ids
     * @return null if not found parent table.
     */
    public int[] getParentTablePKIDs(Table childTable,  int[] ids, Connection con){
    	TableManager manager=TableManager.getInstance();
    	Table parent=childTable.getParentTable();
    	if(parent ==null)return null;
    	int[] rids=null;
    	// this method is solely for parent table am, so checked it here first
    	if( parent.getTriggerName("AM")!=null){
			// get pk ids of tb
			try{
				rids= getPKIDs( childTable, manager.getParentFKColumn(childTable), ids, con );
			}catch(Exception e){
				logger.error("Fail to get pkids of parent:", e);
			}
					
		}
    	
    	return rids;
    }
    /**
     * Oracle has a limitation:
     *  ORA-01795: maximum number of expressions in a list is 1000
     * @param itemTable
     * @param itemColumn
     * @param itemIds
     * @param con
     * @return
     * @throws SQLException
     */
    private int[] getPKIDs(Table itemTable,Column itemColumn, int[] itemIds, Connection con) throws SQLException{
    	if (itemIds==null || itemIds.length==0) return new int[0];
    	java.util.HashMap pkIds=new java.util.HashMap();
    	int i=0;
    	int m=itemIds.length/1000+ ( itemIds.length%1000==0? 0:1);
    	for( int j=0;j< m;j++ ){
			StringBuffer query=new StringBuffer("select distinct " + itemColumn.getName() + " from " + 
				itemTable.getRealTableName()+ " where " + itemTable.getPrimaryKey().getName()+
				" in (");
	    	for(i=j*1000;i<j*1000+1000-1 && i<itemIds.length-1 ;i++ ){
	    		query.append(itemIds[i]+",");
	    	}
	    	query.append(itemIds[i]+")");
	    	logger.debug("get pkids of parent:" + query);
	    	ResultSet rs= con.createStatement().executeQuery(query.toString());
	    	while(rs.next()){
	    		pkIds.put(new Integer(rs.getInt(1)), null);
	    	}
	    	rs.close();
    	}
    	java.util.Set al=pkIds.keySet();
    	int[] pkids= new int[al.size()]; 
    	i=0;
		for(java.util.Iterator it= al.iterator();it.hasNext();){
			pkids[i++]=((Integer)it.next()).intValue();
		}
		return pkids;
    }
    /**
     *  Find class for that name and execute, the class must belong to some package, that is, contains
     * dot in name, so we can figure out whether its class or not (stored procedure)
     * @param className full class name to be executed, if not contains ".", will not take as class object
     * @return true if class found and executed, false if class not found or not trigger interface
     */
    private boolean execTriggerClass(String className, int id, Connection con)  {
        if (Validator.isNull(className) || className.indexOf('.')<0 ) return false;
    	Trigger t=null;
        try{
            //yfzhu changed following class finding mechanism, from specified package to any package acceptable
        	//2005-10-26
        	//Class c=Class.forName("nds.control.ejb.command."+ className);
        	Class c=Class.forName(className);
            t= (Trigger)c.newInstance();
        }catch(Exception e){
            logger.debug("Could not load class:"+className+":"+e);
            return false;
        }
        t.execute(id, con);
        return true;

    }
    /**
     * This function is cache enabled
     * The returned expression will be combined to user preference filter and ui status filter
     *   with "AND" relationship
     * @param tableName the directory name who many had specific sqlfiler in groupperm, this is case insensitive, since
     *        all will be convert to upper case.
     * @return null if no security filter found, Not the returned Expression Object may also be empty
     *  ( isLeaft() && getColumnLink()==null)
     * @param permission, 1 for read, 3 for write, 5 for submit, 9 for audit, combine, so 7 for read/write/submit
     * @param userId the user's id
     */
    public Expression getSecurityFilter(String tableName, int permission, int userId)throws QueryException{
        tableName= tableName.toUpperCase();
        /*if( userId == 0) {
            // root
            return null;
        }*/ // marked at 2005-04-19 for multiple ad_client 
        String key=userId+tableName+permission; // note!!!! this key is different from web directory cache
        Expression sf=null;
        DirectoryCache directoryCache= getDirectoryCache();
            // load form cache
        sf =(Expression) directoryCache.getCachedObject(key);
        if( sf == null) {
            try {
                sf = getSecurityFilter0(tableName, permission, userId);
                // save to cache for future faster load
                directoryCache.addCachedObject(key, sf);
            } catch(Exception e) {
                logger.error("Errors found when check permission (Directory:"+tableName+", "+ permission+").", e);
                throw new QueryException("@security-filter-exception@:"+ e, e);
            }finally{
            }
        }
        return sf;

    }
    /**
     * Create only one instance of directory cache for one container
     */
    public DirectoryCache getDirectoryCache(){
        if(dcInstance!=null) return dcInstance;
        Properties conf= EJBUtils.getApplicationConfigurations();

        long timeOut=DEFAULT_CACHE_TIME_OUT;
       try{
          timeOut=( new Integer(conf.getProperty("ejb.cache.timeout"))).longValue()  ; // seconds
       }catch(Exception e){}
        int size=DEFAULT_CACHE_MAXIMUM_SIZE;
       try{
          size=( new Integer( conf.getProperty("ejb.cache.size"))).intValue() ; // size
       }catch(Exception e2){}

        dcInstance=new DirectoryCache(timeOut*1000, size);
        logger.debug("Create Directory Cache, timeout="+ timeOut+" seconds, size="+ size);
        return dcInstance;
    }
    /**
     * @param permission, 1 for read, 3 for write, 5 for submit, 9 for audit, combine, so 7 for read/write/submit
     * @param userId the user's id
     * @return empty exprssion if not found, nerver return null!
     *
     */
    private Expression getSecurityFilter0(String tableName, int permission, int userId) throws Exception{
        Connection con= null;
        ResultSet rs=null;
        PreparedStatement pstmt=null;
        try{
            con=QueryEngine.getInstance().getConnection();
            pstmt= con.prepareStatement(GET_SECURITY_FILTER);
///"select sqlfilter from groupperm where groupid in (select groupid from groupuser where userid=? ) and directoryid in (select id from directory where tablename=?) and bitand(permission,?)=?";
            pstmt.setInt(1, userId);
            pstmt.setInt(2, getTableManager().getTable(tableName).getId());
            pstmt.setInt(3, permission);
            pstmt.setInt(4, permission);
            rs= pstmt.executeQuery();
            StringHashtable st=new StringHashtable(10, 2000); // the maximum sql length is 2000, limited by db
            String sql,desc;
            while( rs.next()){
                sql= rs.getString(1);
                desc= rs.getString(2);
                if ( sql !=null && sql.length() > 1){
                    st.put(sql, desc);
                }
            }
            if( st.size() > 0){
                TableManager tm=getTableManager();
                Table table= tm.getTable(tableName);
                int[] cids= new int[1]; // contains only pk column
                cids[0]= table.getPrimaryKey().getId() ;
                ColumnLink cl= new ColumnLink( cids);

                // or them
                Enumeration enu= st.keys();// key is just value
                Expression expr1, expr=null;
                while( enu.hasMoreElements() ){
                    sql=(String) enu.nextElement(); // the sqlfilter is xml=Expression.toString() with CDATA indeed.
                    desc=(String) st.get(sql);
                    //expr1= new Expression(cl, sql,desc);
                    expr1=new Expression(sql); // xml
                    if( expr==null) expr= expr1;
                    else expr= expr.combine(expr1,SQLCombination.SQL_OR, null); // or relationship
                }
                return expr;
            }else{
                return new Expression();
            }
        }finally{
            if( rs !=null) try{rs.close();}catch(Exception e){}
            if( pstmt !=null) try{pstmt.close();}catch(Exception e2){}
            if( con!=null) try{con.close();}catch(Exception e3){}
        }
    }
    public static PairTable getFixedColumns(DefaultWebEvent event){
     	 String f=(String) event.getParameterValue("fixedcolumns");
     	 return PairTable.parseIntTable( f,null);
     }  
    /**
     * When server run in single company mode, return email+"@"+domain, else return username+"@"+ domain
     * @param uid
     * @return 
     * @throws NDSException
     */
    public  String getUserDomainName(int uid) throws NDSException{
    	ResultSet rs=null;
    	String d=null;
    	try{
    		StringBuffer sql=new StringBuffer("select ");
    		if(nds.control.web.WebUtils.isServerRunInSingleCompanyMode()){
    			sql.append("users.email");
    		}else{
    			sql.append("users.name");
    		}
    		sql.append(" || '@' || ad_client.domain from ad_client, users  where users.ad_client_id =ad_client.id and users.id=").append(uid);
    		rs=QueryEngine.getInstance().doQuery(sql.toString());
    	rs.next();
    	d= rs.getString(1);
    	}catch(Exception e){
    		logger.error("Could not fetch ad_client domain according to uid:"+uid, e);
    		throw new NDSException("@can-not-find-client-domain-for-user@:"+ e);
    	}finally{
    		if(rs!=null) try{rs.close();}catch(Exception e2){}
    	}
    	
    	return d;
    }
    /**
     * Get $AD_CLIENT_ID$ in <param>qs</param>, and throw exception according to bThrowExceptionIfNotFound 
     * @param qs
     * @param bThrowExceptionIfNotFound if true, will throw exception when ad_client_id not found in QuerySession
     * @return ad client id 
     * @throws NDSException
     */
    public int getAdClientId(QuerySession qs, boolean bThrowExceptionIfNotFound) throws NDSException{
		int ad_client_id= Tools.getInt(qs.getAttribute("$AD_CLIENT_ID$"),-1);
		if (ad_client_id ==-1 && bThrowExceptionIfNotFound ) throw new QueryException("@not-specify-client@");
		return ad_client_id;
    }

    /**
     * Check is the specified object in the status that table specified
     * @param table
     * @param objectId
     * @return false if not in the table
     * @throws NDSException if could not query from db
     */
    public boolean isObjectInTable(Table table, Integer objectId) throws NDSException{
    	
    	String sql= "select id from " + table.getRealTableName() + " " + table.getName()+" where id="+ objectId ;
    	if (table.getFilter()!=null ) sql+= " and " + table.getFilter();
    	return Tools.getInt(QueryEngine.getInstance().doQueryOne(sql), -1)== objectId.intValue();
    	 
    }
    /**
     * 
     * @param table
     * @param objectIds  in format like "id1, id2, id3" 
     * @param cnt count of the object ids
     * @return
     * @throws NDSException
     */
    public boolean isObjectsInTable(Table table, String objectIds, int cnt) throws NDSException{
    	
    	String sql= "select count(*) from " + table.getRealTableName() + " " + table.getName()+" where id in ("+ objectIds+ ")" ;
    	if (table.getFilter()!=null ) sql+= " and " + table.getFilter();
    	return Tools.getInt(QueryEngine.getInstance().doQueryOne(sql), -1)== cnt;
    	
    }
    /**
     * If table record shall do audit, then setup audit process and execute it.
     * If audit is accpeted, or record do not need auditing, then submit directly
     * @param table
     * @param id
     * @param operatorId
     * @param event
     * @return
     */
    public SPResult auditOrSubmitObject(Table table, int id, int operatorId, DefaultWebEvent event)  {
    	String state=null;
    	SPResult result=null;
    	try{
   	 	/**
         * Check is this object shall do audit process
         */
        int processId= AuditUtils.getProcess(table.getId(),id );
        if (processId!=-1){
        	// shall do audit process
        	ValueHolder v= AuditUtils.executeProcess(table.getId(),id, processId, operatorId,-1,null);
        	state= (String)v.get("state");
        	logger.debug(v.toDebugString());
       		result =new SPResult(Tools.getInt(v.get("code"),-1), (String)v.get("message"));
        }
        if (processId==-1 || "A".equals(state)){
        	// do submit 
        	result = submitObject(table, id, operatorId, event);
        }
        if( result.getCode()!=0 && result.getCode()!=2/*set by #submitOne*/){
        	// set status of object to 1 if that column exists and status =3
        	if(table.getColumn("status")!=null)
        		QueryEngine.getInstance().executeUpdate("update "+ table.getRealTableName()+" set status=1 where id="+ id + " and status=3");
        }
    	}catch(Throwable t){
    		logger.error("Fail to do submt object "+ table+ ",id="+ id, t);
    		result=new SPResult(-1, t.getMessage());
    	}
    	logger.debug("spresult:code="+ result.getCode()+", message="+ result.getMessage());
        return result;
    }
    /**
     * 
     * @param table
     * @param id
     * @param operatorId
     * @param event
     * @return if SPResult.getCode()!=0, it's failed, else, it's ok
     */
    public SPResult submitObject( Table table, int id, int operatorId, DefaultWebEvent event) {
    	String spName=  table.getSubmitProcName();
    	String tableName = table.getRealTableName() ;
       	if( nds.util.Validator.isNull(spName))spName=tableName+ "Submit";
       	boolean isJavaClass= spName.indexOf('.')>0;
       	return isJavaClass?submitJavaOne(spName, table,id,operatorId,event):
           	submitOne(spName ,table,id, operatorId);
       	
    }
    /**
     * 
     * @param className the class name of the command to handle submit request
     * @param table
     * @param id
     * @param operatorID
     * @return if SPResult.getCode()!=0, it's failed, else, it's ok
     */
    private SPResult submitJavaOne(String className, Table table, int id, int operatorID, DefaultWebEvent event){
    		DefaultWebEvent e= (DefaultWebEvent)event.clone();
    		e.setParameter("command", className);
    		e.setParameter("spName", table.getName()+"Submit" );
    		e.setParameter("id", String.valueOf(id));
    		SPResult s=null;
    		try{
    			ValueHolder v=handleEvent(e);
    			s= new SPResult(Tools.getInt(v.get("code"), 0),(String)v.get("message"));
    		}catch(Throwable t){
    			logger.error("Could not handle one using java class for "+ table + " class="+ className+", id="+ id, t);
    			s=new SPResult(-1, t.getMessage());
    		}
    		return s;
    }
    
    /**
     * @return if SPResult.getCode()!=0, it's failed, else, it's ok
     */
      private SPResult submitOne(String spName, Table table, int pid,  int operaorID) {
      	String tableName= table.getRealTableName();
      	try{

             if(!isObjectInTable(table, new Integer(pid))){
              	//object no longer in table , so abort
              	return new SPResult(2,"(id="+pid+")@object-status-error-may-submitted-or-deleted@!" );
              }
          QueryEngine engine = QueryEngine.getInstance() ;
          
          int status = engine.getSheetStatus(tableName,pid );
          if(status==JNDINames.STATUS_SUBMIT ){

              return new SPResult(2,"(id=" + pid + ")@object-already-submitted@"); // this code is very important, used by 
              //throw new NDSEventException("该请求已经被提交过了！" );
          }
          Vector sqls= new Vector();
          sqls.addElement("update "+tableName+" set modifierid="+ operaorID+ ", modifieddate=sysdate where id="+pid);
          try{
              engine.doUpdate(sqls);
          }catch(Exception e){
              throw new NDSEventException(e.getMessage() );
          }

          ArrayList list = new ArrayList();

              list.add(new Integer(pid));
              SPResult sp=engine.executeStoredProcedure(spName,list,true);
              //logger.debug("submitOne:code="+ sp.getCode()+", message="+ sp.getMessage());
              return sp;
          }catch(Exception e){
              logger.error("Could not submit record(table="+tableName+",id="+ pid+")", e );
              return new SPResult(-1,e.getMessage());
          }
      }    
}

