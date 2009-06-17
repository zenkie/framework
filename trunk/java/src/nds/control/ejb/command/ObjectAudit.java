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
 * Title:        NDS Project
 * Description:  San gao shui yuan, mu xiang ren jia
 * Copyright:    Copyright (c) 2001
 * Company:      aic
 * @author yfzhu
 * @version 1.0
 * @deprecated
 */

public class ObjectAudit extends Command{
    public ObjectAudit() {

    }
    /**
     * 修改单据以下内容：
     *  status= JNDINames.STATUS_AUDITING
     *  auditorId=null,
     *  auditNote=null,
     *  modifierid=Operator.UserId()
     *  modifieddate=sysdate
     * 
     */
    public ValueHolder execute(DefaultWebEvent event) throws NDSException ,RemoteException{
        Integer pid = new Integer(Tools.getInt(event.getParameterValue("id") ,-1));
        int userId=helper.getOperator(event).getId().intValue()  ;
        String spName = (String)event.getParameterValue("spName");
        String tableName = spName.substring(0,spName.indexOf("Audit") ) ;
        QueryEngine engine = QueryEngine.getInstance() ;
        int status = engine.getSheetStatus(tableName,pid.intValue() );
        if(status==JNDINames.STATUS_AUDITING || status==JNDINames.STATUS_PERMIT){
            throw new NDSEventException("@req-commited@" );
        }
        Vector sqls= new Vector();
        sqls.addElement("update "+tableName+" set status="+JNDINames.STATUS_AUDITING+",auditorId=null,auditNote=null,modifierid="+ userId+ ", modifieddate=sysdate where id="+pid);
        try{
            engine.doUpdate(sqls);
            // Notify
            java.sql.Connection con=null;
            try{
            con=engine.getConnection();
            helper.Notify(TableManager.getInstance().getTable(tableName),
                          pid.intValue(),helper.getOperator(event).getDescription(),JNDINames.STATUS_AUDITING ,con);
            }catch(Exception eee){}finally{
                 try{if(con !=null) con.close(); }catch(Exception eee){}
            }
            ValueHolder v=new ValueHolder();
                v.put("message","@req-audit@" ) ;
            return v;
        }catch(Exception e){
            throw new NDSEventException(e.getMessage() );
        }

    }
}