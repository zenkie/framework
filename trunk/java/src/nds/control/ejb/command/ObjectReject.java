package nds.control.ejb.command;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Vector;

import nds.control.ejb.Command;
import nds.control.event.DefaultWebEvent;
import nds.control.event.NDSEventException;
import nds.control.util.ValueHolder;
import nds.control.web.UserWebImpl;
import nds.query.QueryEngine;
import nds.query.SPResult;
import nds.schema.Table;
import nds.schema.TableManager;
import nds.util.JNDINames;
import nds.util.NDSException;
import nds.util.Tools;
/**
 * Reject for acceptance. Single object manipulation
 * audit_state -=2;
 */

public class ObjectReject extends Command{

    /**
     */
    public ValueHolder execute(DefaultWebEvent event) throws NDSException ,RemoteException{
        Integer pid = new Integer(Tools.getInt(event.getParameterValue("id") ,-1));
        int userId=helper.getOperator(event).getId().intValue()  ;
        String spName = (String)event.getParameterValue("spName"); //"TonyTest";
        String tableName = spName.substring(0,spName.indexOf("Reject") ) ;
        Table table=TableManager.getInstance().getTable(tableName);
        tableName=table.getRealTableName();
  
        QueryEngine engine=QueryEngine.getInstance();
    	boolean isCheckStatus= (table.getColumn("status")!=null);
    	int status=-1, audit_state;
    	Connection conn= null;
    	PreparedStatement pstmt=null;
    	ResultSet rs=null;
    	ArrayList al=new ArrayList();
    	ValueHolder v=null;
    	try{
	    	conn=engine.getConnection();
	    	pstmt=conn.prepareStatement("select audit_state" + (isCheckStatus?",status":"") + " from "+
	    			tableName+ " where id=?");
	    	pstmt.setInt(1, pid.intValue());
	    	rs=pstmt.executeQuery();
	    	rs.next();
	    	audit_state= rs.getInt(1);
	    	if(isCheckStatus) status= rs.getInt(2);
	    	if(status==JNDINames.STATUS_SUBMIT ||  audit_state%2!=0||audit_state==0 ) throw new NDSEventException("当前对象非审核状态，请重新装载");
	    	
	    	audit_state --;
	    	
	    	// update
	    	Vector sqls= new Vector();
	    	sqls.addElement("update "+tableName+" set audit_state="+audit_state+",modifierid="+ userId+ ", modifieddate=sysdate where id="+pid);
	    	engine.doUpdate(sqls);
	    	
            if(v==null){
            	v=new ValueHolder();
                v.put("message","请求被驳回." ) ;
                v.put("next-screen", "/html/nds/info.jsp");
            }
            return v;
        }catch(Exception e){
            if(e instanceof NDSEventException )throw (NDSEventException)e;
            else throw new NDSEventException(e.getMessage() );
        }finally{
    		if(pstmt!=null){try{pstmt.close();}catch(Exception e2){}}
    		if(conn!=null){
    			try{conn.close();}catch(Exception e){}
    		}
        	
        }

    }
}