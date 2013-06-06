package nds.control.ejb.command;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Vector;

import nds.control.ejb.Command;
import nds.control.event.DefaultWebEvent;
import nds.control.event.NDSEventException;
import nds.control.util.SecurityUtils;
import nds.control.util.ValueHolder;
import nds.query.QueryEngine;
import nds.query.SPResult;
import nds.schema.Table;
import nds.schema.TableManager;
import nds.security.Directory;
import nds.security.User;
import nds.util.JNDINames;
import nds.util.NDSException;
import nds.util.Tools;
import nds.util.Validator;

/**
 * 当表的提交处理动作设置为java 类时，需要将处理过程移交给此类（目前不支持java类的groupsubmit)。
 */
public class ListSubmit extends Command{
    public ListSubmit() {

    }
    /**
     * Whether this command use internal transaction control. For normal command, transaction is controled by
     * caller, yet for some special ones, the command will control transaction seperativly, that is, the command
     * will new transaction and commit that one explicitly
     * @return false if use transaction from caller
     */
	public boolean internalTransaction(DefaultWebEvent event) {
		return true;
	}
    /**
     * Will check all objects' status before submit
     * @param event:
     * 		"table"
     * 		"itemid"
     * 		
     */
    public ValueHolder execute(DefaultWebEvent event) throws NDSException ,RemoteException{
		TableManager manager = helper.getTableManager();
		Table table = manager.findTable(event.getParameterValue("table", true));
		int tableId = table.getId();
		String prop = null;
		if (table.getJSONProps() != null)
			prop = table.getJSONProps().optString("check_submit");
		if (Validator.isNotNull(prop)) {
			throw new NDSException("@pls-submit-one-by-one@");
		}
         String[] itemidStr = event.getParameterValues("itemid",true);
         User user= helper.getOperator(event);
         String operatorDesc=user.getDescription();
         Integer operaorID= user.getId() ;
         String tableName = table.getRealTableName() ;
         String tableDesc  = table.getDescription(event.getLocale());

         ValueHolder v = new ValueHolder();
         // 由于界面上无法控制所有的对象都具有相同层次的权限，故需要在此进行权限认证
 		// check permissions on all objects
         boolean b= false;
         try{
        	 boolean statusExists=(table.getColumn("status")!=null);
        	 b=SecurityUtils.hasPermissionOnAll(user.getId().intValue(), user.getName(), 
  				table.getName(), itemidStr, Directory.SUBMIT, event.getQuerySession(), 
  				(statusExists? table.getName()+".status=1":null));
        	 	// bugs found: status must be 1 when submit
  				//(statusExists? table.getName()+".status<>2":null));
         }catch(Exception e){
         	logger.error("Fail",e);
         	throw new NDSException("@error-in-permission-check@:"+ e.getMessage(), e);
         }
  		if (!b){
  			v.put("message","@no-submit-permission-on-all-pls-submit-one-by-one@");
  		}else{
  	         // has permssion
  	         if (itemidStr==null) itemidStr= new String[0];
  	         String res = "", s; int errCount=0;
  	         // so if only one item selected, it will call Submit instead of GroupSubmit
  	         if (itemidStr.length > 1 && table.isActionEnabled(Table.GROUPSUBMIT)){
  	             // group submit, added by yfzhu 20040531
  	             s=groupSubmit(tableName+"GroupSubmit", table, itemidStr, operatorDesc, operaorID);
  	             if( s !=null) throw new NDSException(s);
  	         }else{
  	         	 // submit one by one
	  	         for(int i = 0;i<itemidStr.length ;i++){
	  	             int itemid = Tools.getInt(itemidStr[i],-1) ;
	  	             //
	  	             SPResult r=helper.auditOrSubmitObject(table,itemid,operaorID.intValue(),event);
	  	            /**
	  	             * 修改submit 方法支持rcode 返回
	  	             * 101 刷新不关闭
	  	             */
	  	             if (r.getCode() !=0&& r.getCode()!=101) {
	  	                 res += r.getMessage()+ "<br>";
	  	                 errCount ++;
	  	             }else{
	  	            	 if(nds.util.Validator.isNotNull(r.getMessage())){
	  	            		 res+=r.getMessage()+ "<br>";
	  	            	 }
	  	             }
	  	         }
  	         }
  	         String message = null;

  	         message ="@total-lines@:"+itemidStr.length;
  	         message +=",@failed-count@:"+ errCount +",@detail-msg@:<br>" + res;
  	         v.put("message",message) ;
  		}
         return v;

  }
	
  private String groupSubmit(String spName,Table table,String[] ids, String operatorDesc, Integer operaorID) {
    String tableName= table.getRealTableName();  
  	String s=ids[0];
      for(int i=1;i< ids.length;i++){
          s += ","+ ids[i];
      }
      // check all objects' status
      
      try{
      	  if(!helper.isObjectsInTable(table, s, ids.length)){
      	  	return "@some-objects-changed@";
      	  }
          QueryEngine engine = QueryEngine.getInstance() ;
          int cnt=Tools.getInt(engine.doQueryOne("select count(*) from (select distinct status from "+tableName+" where id in ("+s+"))"), 0);
          if (cnt!=1) return "@some-objects-status-changed@";
          Vector sqls= new Vector();
          sqls.addElement("update "+tableName+" set modifierid="+ operaorID+ ", modifieddate=sysdate where id in("+s+")");
          engine.doUpdate(sqls);
          
          ArrayList list = new ArrayList();
          list.add(s);
          SPResult result = engine.executeStoredProcedure(spName,list,true);
          if(result.isSuccessful() ){
              // notify
              return null  ;
          }else{
              return (result.getDebugMessage());
          }
      }catch(Exception e){
          logger.error("Could not submit record(table="+tableName+",id="+ s+")", e );
          return "@exception@:"+e.getMessage();
      }

  }
 
}