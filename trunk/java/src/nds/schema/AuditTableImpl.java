/*
 * Agile Control Technologies Ltd,. CO.
 * http://www.agileControl.com
 */
package nds.schema;

import java.util.*;
import java.util.Collections;
import nds.web.bean.*;
import nds.query.*;
import nds.security.Directory;
import nds.control.web.UserWebImpl;
import java.sql.*;
import nds.log.*;
import nds.util.*;
/**
 * 
 * Table which supports audit manipuplation
 * This kind of table must have following columns:
 * audit_state
 * @deprecated
 */

public class AuditTableImpl extends TableImpl {
    private static Logger logger=LoggerManager.getInstance().getLogger(AuditTableImpl.class.getName());
    /**
     * Elements are nds.web.bean.Button or String(must exists in ButtonFactory)
     * status 如果存在且=2 将无任何按钮
     * status !=2 ，将显示按钮，当
     *        audit_state 为奇数，且用户有当前对象的写权限，显示 request 
     *        audit_state 为偶数且不为0，且用户有当前对象的审核权限，显示 accept, reject
     * @return 
     */
    public Collection getExtendButtons(int objectId, Object usr){
    	int status=-1, audit_state;
    	boolean isCheckStatus= (this.getColumn("status")!=null);
    	Connection conn= null;
    	PreparedStatement pstmt=null;
    	ResultSet rs=null;
    	ArrayList al=new ArrayList();
    	try{
        	int perm=((UserWebImpl)usr).getObjectPermission(this.getName(), objectId);
	    	conn=QueryEngine.getInstance().getConnection();
	    	pstmt=conn.prepareStatement("select audit_state" + (isCheckStatus?",status":"") + " from "+
	    			this.getRealTableName()+ " where id=?");
	    	pstmt.setInt(1, objectId);
	    	rs=pstmt.executeQuery();
	    	rs.next();
	    	audit_state= rs.getInt(1);
	    	if(isCheckStatus) status= rs.getInt(2);
	    	if(status!=JNDINames.STATUS_SUBMIT){
	    		if(audit_state%2==1 && (perm&Directory.WRITE)==Directory.WRITE) al.add("Request");
	    		if(audit_state%2==0 && audit_state!=0 &&  ((perm&Directory.AUDIT)==Directory.AUDIT)){
	    			al.add("Accept");
	    			al.add("Reject");
	    		}
	    	}
	    	
	    	return al;
    	}catch(Throwable t){
    		logger.error("Could not get extended buttons for table "+ this.getName()+" records:"+ objectId+" of usr:"+ ((UserWebImpl)usr).getUserName(), t);
    		return Collections.EMPTY_LIST;
    	}finally{
    		if(pstmt!=null){try{pstmt.close();}catch(Exception e2){}}
    		if(conn!=null){
    			try{conn.close();}catch(Exception e){}
    		}
    	}
    	
    }	

}
