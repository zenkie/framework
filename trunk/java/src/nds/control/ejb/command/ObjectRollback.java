package nds.control.ejb.command;
import java.rmi.RemoteException;
import java.util.Vector;

import nds.control.ejb.Command;
import nds.control.event.DefaultWebEvent;
import nds.control.event.NDSEventException;
import nds.control.util.ValueHolder;
import nds.query.QueryEngine;
import nds.schema.TableManager;
import nds.util.JNDINames;
import nds.util.NDSException;
import nds.util.Tools;
/**
 * When user execute "Permit" commnad ( commands which ended with "Rollback" string will be
 * replace as "ObjectRollback" in nds.control.web.requesthandler.DefaultRequestHandler), this
 * Command will be executed.
 *
 * We will first store all modifications on page before execution, just like command ObjectModify,
 * then do permission actions
 * @deprecated 

 */
public class ObjectRollback extends Command{
    public ObjectRollback() {

    }
    /**
     * Check for object status, must be JNDINames.STATUS_AUDITING
     * Do object modification (User may permit in sheet_title page, who will add some comments)
     *              (we check this using "objectid" param, if "objectid" exist in event,
     *               then it must be in sheet_item page, so we will not do modification)
     * Change object status to JNDINames.STATUS_PERMIT,
     * Send notification to the user box (undo)
     
     */
    public ValueHolder execute(DefaultWebEvent event) throws NDSException ,RemoteException{

        Integer pid = new Integer(Tools.getInt(event.getParameterValue("id") ,-1));
//        logger.debug("the vlaue of pid is:"+pid.intValue() ) ;
//        logger.debug(event.toDetailString() ) ;
        String spName = (String)event.getParameterValue("spName");
//        logger.debug("The value of spName is:"+spName) ;
        String tableName = spName.substring(0,spName.indexOf("Rollback") ) ;
        QueryEngine engine2 = QueryEngine.getInstance() ;
        int status = engine2.getSheetStatus(tableName,pid.intValue() );
        if(status!=JNDINames.STATUS_AUDITING){// status=2 meaning
            throw new NDSEventException("该请求已经不在等待审核状态，请刷新数据！" );
        }

        // if page not in sheet_item.jsp ( which must has a parameter name "objectid" in event)
        // we will do object modification.
        int fkValue =Tools.getInt(event.getParameterValue("objectid"),-1);
        if(fkValue==-1){
            // Do object modification
            ObjectModify om=new ObjectModify();
            om.execute(event);
        }
        int userId=helper.getOperator(event).getId().intValue()  ;
        int employeeid = engine2.getEmployeeId(userId) ;
        Vector sqls= new Vector();
        sqls.addElement("update "+tableName+" set status="+JNDINames.STATUS_ROLLBACK +",auditorId="+ employeeid +",modifierid="+ userId+ ", modifieddate=sysdate where id="+pid);
        try{
            QueryEngine engine = QueryEngine.getInstance() ;
            engine.doUpdate(sqls);
            //Notify
            java.sql.Connection con=null;
            try{
            con= engine.getConnection();
            helper.Notify(TableManager.getInstance().getTable(tableName),
                          pid.intValue(),helper.getOperator(event).getDescription(),JNDINames.STATUS_ROLLBACK,con);
             }catch(Exception e233){}finally{
                 try{if(con !=null) con.close(); }catch(Exception eee){}
             }

            ValueHolder v=new ValueHolder();
             v.put("message","单据被驳回." ) ;
            return v;
        }catch(Exception e){
            throw new NDSEventException(e.getMessage() );
        }
    }
}