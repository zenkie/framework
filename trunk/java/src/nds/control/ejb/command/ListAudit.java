package nds.control.ejb.command;
import java.rmi.RemoteException;
import java.util.Locale;
import java.util.Vector;

import nds.control.ejb.Command;
import nds.control.event.DefaultWebEvent;
import nds.control.util.ValueHolder;
import nds.query.QueryEngine;
import nds.schema.Table;
import nds.schema.TableManager;
import nds.util.JNDINames;
import nds.util.NDSException;
import nds.util.Tools;


public class ListAudit extends Command{
    public ListAudit() {

    }
    public ValueHolder execute(DefaultWebEvent event) throws NDSException ,RemoteException{
         TableManager manager = helper.getTableManager() ;
         Table table = manager.findTable(event.getParameterValue("table"));
         int tableId = table.getId();
         nds.security.User usr=helper.getOperator(event);
         String operatorDesc=usr.getDescription();
         int operatorId= usr.getId().intValue();
         String tableName = table.getName() ;
         String tableDesc  = table.getDescription(Locale.CHINA);
         String[] itemidStr = event.getParameterValues("itemid");
         if (itemidStr==null) itemidStr= new String[0];
//         Vector vec = new Vector();
         String res = "", s; int errCount=0;
             for(int i = 0;i<itemidStr.length ;i++){
                 int itemid = Tools.getInt(itemidStr[i],-1) ;
                 s =auditOne( tableName,itemid,operatorDesc,operatorId);
                 if (s !=null) {
                     res += s+ "<br>";
                     errCount ++;
                 }
             }
         ValueHolder v = new ValueHolder();
         String message = null;

         message =itemidStr.length + "条记录在"+tableDesc+"中被请求申报核准";
         if ( errCount > 0) message +=", 其中"+ errCount +"条有错误，详细信息：<br>" + res;
         v.put("message",message) ;
         return v;

  }
  /**
   * @return null if successful, else return error message
   */
    private String auditOne( String tableName, int pid, String operatorDesc, int operatorId) {
        try{

        QueryEngine engine = QueryEngine.getInstance() ;
        int status = engine.getSheetStatus(tableName,pid );
        if(status==JNDINames.STATUS_AUDITING || status==JNDINames.STATUS_PERMIT){
            return "id=" + pid + " 的单据已经提交过了";
        }
        Vector sqls= new Vector();
        sqls.addElement("update "+tableName+" set status="+JNDINames.STATUS_AUDITING+",auditorId=null,auditNote=null,modifierid="+ operatorId+ ", modifieddate=sysdate where id="+pid);
            engine.doUpdate(sqls);
            // Notify
            java.sql.Connection con=null;
            try{
            con=engine.getConnection();
            helper.Notify(TableManager.getInstance().getTable(tableName),
                          pid,operatorDesc,JNDINames.STATUS_AUDITING ,con);
            }catch(Exception eee){}finally{
                 try{if(con !=null) con.close(); }catch(Exception eee){}
            }
            return null;
        }catch(Exception e){
            logger.error("Could not submit record(table="+tableName+",id="+ pid+")", e );
            return e.getMessage();
        }
    }
}