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
 * Request for acceptance. Single object manipulation
 * 当修正audit_state为0时，应触发Table.getSubmitProc 对应的方法。
 */

public class ObjectRequest extends Command{

    /**
     * 特殊情况下，允许在记录上设置初始值，比如货运单，有需要审核的货运单，也有不需要审核的货运单，将根据单据
     * 类型由系统自动创建。这时在表中可以设置init_audit_state字段。此字段可以不必显示，但需要在表定义中指明。
     * 提交审核时，本方法将根据表是否存在此字段进行判断
     */
    public ValueHolder execute(DefaultWebEvent event) throws NDSException ,RemoteException{
        Integer pid = new Integer(Tools.getInt(event.getParameterValue("id") ,-1));
        int userId=helper.getOperator(event).getId().intValue()  ;
        String spName = (String)event.getParameterValue("spName"); //"TonyTest";
        String tableName = spName.substring(0,spName.indexOf("Request") ) ;
        Table table=TableManager.getInstance().getTable(tableName);
        tableName=table.getRealTableName();
  
        QueryEngine engine=QueryEngine.getInstance();
    	boolean isCheckStatus= (table.getColumn("status")!=null);
    	boolean isCheckInitAuditState=  (table.getColumn("init_audit_state")!=null);
    	int status=-1, audit_state;
    	Connection conn= null;
    	PreparedStatement pstmt=null;
    	ResultSet rs=null;
    	ArrayList al=new ArrayList();
    	ValueHolder v=null;
    	int db_init_audit_state=3;
    	try{
	    	conn=engine.getConnection();
	    	pstmt=conn.prepareStatement("select audit_state" + (isCheckStatus?",status":"") +
	    			 (isCheckInitAuditState?",init_audit_state":"") +
	    			" from "+
	    			tableName+ " where id=?");
	    	pstmt.setInt(1, pid.intValue());
	    	rs=pstmt.executeQuery();
	    	rs.next();
	    	audit_state= rs.getInt(1);
	    	if(isCheckStatus) status= rs.getInt(2);
	    	if(isCheckInitAuditState) db_init_audit_state= rs.getInt( (isCheckStatus?3:2));
	    	
	    	if(status==JNDINames.STATUS_SUBMIT ||  audit_state%2!=1) throw new NDSEventException("当前对象非请求状态，请重新装载");
	    	
	    	// reqeust from start state is --, else is ++ for rejected state
	    	int initAuditState= Tools.getInt(table.getColumn("audit_state").getDefaultValue(), -1);
	    	if(initAuditState==-1){
	    		// load from init_audit_state data in db, if exists
	    		initAuditState =db_init_audit_state;
	    	}
	    	if(initAuditState==audit_state)
	    		audit_state --;
	    	else audit_state ++;
	    	
	    	// update
	    	ArrayList sqls= new ArrayList();
	    	sqls.add("update "+tableName+" set audit_state="+audit_state+",modifierid="+ userId+ ", modifieddate=sysdate where id="+pid);
	    	engine.doUpdate(sqls);
	    	String addionalMessage="";
	    	// end?
	    	if(audit_state==0){
	    		if( table.getSubmitProcName()!=null){
	    			// java class?
	    			if( table.getSubmitProcName().indexOf('.')>0){
	    				// call as Command
	    				DefaultWebEvent ev= (DefaultWebEvent)event.clone();
	    				ev.setParameter("command", table.getSubmitProcName());
	    				ev.setParameter("spName", table.getName()+"Submit" );
	    				v=helper.handleEvent(ev);
	    			}else{
	    				// call as SP
	    				ArrayList list = new ArrayList();
	    				list.add(pid);
	    				SPResult result = engine.executeStoredProcedure(table.getSubmitProcName(),list,true);
	    				if(!result.isSuccessful()) throw new NDSEventException(result.getDebugMessage());
	    				else{
	    					addionalMessage=","+result.getMessage();
	    				}
	    				
	    			}
	    		}
	    	}
            // Notify
/*            java.sql.Connection con=null;
            try{
            con=engine.getConnection();
            helper.Notify(TableManager.getInstance().getTable(tableName),
                          pid.intValue(),helper.getOperator(event).getDescription(),JNDINames.STATUS_AUDITING ,con);
            }catch(Exception eee){}finally{
                 try{if(con !=null) con.close(); }catch(Exception eee){}
            }
*/            
            if(v==null){
            	v=new ValueHolder();
                v.put("message", (audit_state==0?"请求被接受"+addionalMessage:"请求进入审核.") ) ;
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