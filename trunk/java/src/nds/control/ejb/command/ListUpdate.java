package nds.control.ejb.command;

import java.rmi.RemoteException;
import java.util.*;

import javax.servlet.http.HttpServletRequest;

import org.directwebremoting.WebContext;
import org.json.*;

import nds.control.ejb.Command;
import nds.control.event.DefaultWebEvent;
import nds.control.event.NDSEventException;
import nds.control.util.AjaxUtils;
import nds.control.util.EditableGridMetadata;
import nds.control.util.ValueHolder;
import nds.query.*;
import nds.schema.*;
import nds.util.*;




/*import com.liferay.portal.ejb.UserManagerUtil;
import com.liferay.portal.ejb.UserManager;
import com.liferay.portal.ejb.UserManagerFactory;

*/
import nds.security.User;

/**
 * 界面导入文件做部分字段更新。由ImportExcel创建Event ListUpdate， 然后分别调用ObjectModify操作
 */
public class ListUpdate extends Command {
	/**
     * Whether this command use internal transaction control. For normal command, transaction is controled by
     * caller, yet for some special ones, the command will control transaction seperativly, that is, the command
     * will new transaction and commit that one explicitly
     * @return false if use transaction from caller
     */
    public boolean internalTransaction(){
    	return true;
    }
	/**
	 * @param event contains 
	 * 		rows - ArrayList elements are DefaultWebEvent with only data from file
	 	
	 * 	
	 */
  public ValueHolder execute(DefaultWebEvent event) throws RemoteException, NDSException {
	  TableManager manager=TableManager.getInstance();
      boolean isOutputJSONError=Tools.getYesNo(
    		  event.getParameterValue("output_json", true), false);
	  
      int tableId = Tools.getInt(event.getParameterValue("table"),-1 ) ;
      Table table = manager.getTable(tableId) ;
      String tableName = table.getName();
      String akName= table.getAlternateKey().getName();
	  String operatorId=(String)event.getParameterValue("operatorid");
	  
	  ArrayList rows=(ArrayList) event.getParameterValue("rows");
	  PairTable pt=new PairTable();
	  int successCount=0;
	  int recordLen= rows.size();
	  Object qs=event.getParameterValue("nds.query.querysession");
	  Object ls= event.getParameterValue("JAVA.UTIL.LOCALE");
	  for(int i=0;i< rows.size();i++){
		  DefaultWebEvent dwe= (DefaultWebEvent)rows.get(i);
		  Object rowIndex= dwe.getParameterValue("nds.row.index");
		  dwe.setParameter("command","ObjectModify");
		  dwe.setParameter("table", String.valueOf(tableId));
		  dwe.setParameter("operatorid",operatorId );
		  dwe.setParameter("partial_update", "true");
		  dwe.setParameter("ak", (String)dwe.getParameterValue(akName));
		  dwe.put("nds.query.querysession",qs);
		  dwe.put("JAVA.UTIL.LOCALE",ls);
		  
		  try{
			  ValueHolder vh=helper.handleEventWithNewTransaction(dwe);
			  if(Tools.getInt(vh.get("code"), 0)!=0){
				  pt.put(rowIndex, vh.get("message"));
			  }else
				  successCount++;
		  }catch(Throwable t){
			  logger.error("fail to do update:"+t.toString());
			  pt.put(rowIndex, t.getMessage());
		  }
	  }
      ValueHolder v = new ValueHolder();
      try{
	  
	  StringBuffer sb=new StringBuffer();
	  long beginTime= Long.parseLong( (String)event.getParameterValue("nds.start_time"));
	  sb.append("<pre>### "+ (new java.util.Date())+ ":@finished-import@(@consumed-to@ "+ (System.currentTimeMillis() -beginTime)/1000 + " @seconds@) ###");
      sb.append("\r\n");
      sb.append("@operate-table@："+table.getDescription(event.getLocale())+"\r\n");
      sb.append("@total-lines@:"+ recordLen +", @success-import@:"+ successCount +" ,@fail-import@:"+(recordLen-successCount)+"\r\n");

      JSONObject restResult=new JSONObject();// for rest call
      v.put("restResult", restResult);
      if(pt.size()>0){
          JSONArray ja=new JSONArray();
          JSONObject job;
          for( int i=0;i< pt.size();i++){
        	  
           	   if(isOutputJSONError){
           		   job=new JSONObject();
           		   job.put("lineno",Tools.getInt( pt.getKey(i),-1) );
           		   job.put("errmsg",pt.getValue(i));
           		   ja.put(job);
           	   }else{
               	   sb.append("@line@:"+ pt.getKey(i)+ ": "+ pt.getValue(i)+ "\r\n");
           	   }
              
          }
          restResult.put("errors", ja);
      }
      sb.append("</pre>");	  
      if(isOutputJSONError){
		   v.put("message","complete");
	   }else
		   v.put("message", sb.toString()) ;
      }catch(Throwable t){
    	  throw new NDSEventException(nds.util.StringUtils.getRootCause(t).getMessage(),t);
      }
      return v;
  }
  
}