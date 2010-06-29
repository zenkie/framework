package nds.control.ejb.command;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.util.Locale;
import java.util.Vector;

import nds.control.ejb.Command;
import nds.control.ejb.command.pub.Pub;
import nds.control.event.DefaultWebEvent;
import nds.control.util.SecurityUtils;
import nds.control.util.ValueHolder;
import nds.query.QueryEngine;
import nds.query.QueryUtils;
import nds.schema.Table;
import nds.schema.TableManager;
import nds.security.Directory;
import nds.security.User;
import nds.util.JNDINames;
import nds.util.NDSException;
import nds.util.Tools;
/**
*   ListDelete is a module to delete the record from the web
*   delete in batch

*   yfzhu 2010-5-20 record must be void before delete if table has void action set
*/
public class ListDelete extends Command{
  private TableManager manager;
  public ValueHolder execute(DefaultWebEvent event) throws NDSException ,RemoteException{
  	/**
  	 * 2005-11-15 增加了对status 字段的判断，如果status字段为2 则当前记录不允许删除。这种情况主要发生
  	 * 在支持审计的单视图模式下。此模式下所有的单据都在一个界面，对于已审计通过的单据应禁止删除。系统会在通过
  	 * 审计的单据上修改status=2，作为判断条件。
  	 */
       manager = helper.getTableManager() ;
       
       Table table = manager.findTable(event.getParameterValue("table",true));
       int tableId = table.getId();

       String[] itemidStr = event.getParameterValues("itemid", true);

       User user= helper.getOperator(event);
       String operatorDesc=user.getDescription();
       String tableName = table.getRealTableName() ;
       String tableDesc  = table.getDescription(event.getLocale());
       
       if (itemidStr==null) itemidStr= new String[0];
       java.sql.Connection con=null;
       QueryEngine engine =QueryEngine.getInstance();
       con= engine.getConnection();
       try{
       // get parent table ids, for later after-modify trigger
       Table parent= helper.getParentTable(table,event);
       //logger.debug(" parent table of " + table + " is " + parent);
       int[] oids=new int[itemidStr.length];
       for(int i=0;i<oids.length;i++) oids[i]= Integer.parseInt(itemidStr[i]);
       int[] poids= helper.getParentTablePKIDs(table, oids, con);
       logger.debug(" parent id of " + table + " is " + Tools.toString(poids));
       // 检查父表的存在性，如果不存在或未找到，抛出错误，这种情况发生在以下情况：
       // 父表的状态被改变了，而子表的界面仍然保留在那里，故用户可以对子表进行操作，导致
       // 父表出现错误
       //举例：  m_v_inout 被提交生成了 m_v_2_inout, 而用户仍可以对m_v_inoutitem表
       //中的内容进行修改。这是不允许的。
       helper.checkTableRows(parent, poids, con, helper.PARENT_NOT_FOUND);
       
       ValueHolder v = new ValueHolder();
       
       // 由于界面上无法控制所有的对象都具有相同层次的权限，故需要在此进行权限认证
       if(parent==null){
       		// check permissions on all objects
       		if (!SecurityUtils.hasPermissionOnAll(user.getId().intValue(), user.getName(), 
       				table.getName(), itemidStr, Directory.WRITE, event.getQuerySession())){
       			v.put("message", "@no-delete-permission-on-all-pls-delete-one-by-one@");
       			return v;
       		}
       }
       
        // check status 2005-11-15
       boolean bCheckStatus= (table.getColumn("status") !=null);
       boolean bCheckVoid= table.isActionEnabled(Table.VOID);
        String res = "", s; int errCount=0;
        for(int i = 0;i<itemidStr.length ;i++){
            int itemid = Tools.getInt(itemidStr[i],-1) ;
            s =deleteOne( table,itemid,operatorDesc,con,bCheckStatus,bCheckVoid );
            if (s !=null) {
                res += s+ "<br>";
                errCount ++;
            }else{
                logger.info("deleted table="+ table+", id="+itemid+" by "+ user.name+" of id "+ user.id);
            }
        }
        if(parent !=null)
        	helper.doTrigger("AM", parent, poids, con);
        
        String message = null;

        message =itemidStr.length + "@line@ @request-to-delete@";
        if ( errCount > 0) message +=", @failed-count@:"+ errCount +", @detail-msg@:" + res;
        else message +=",@complete@";
        v.put("message",message) ;
        
        
        return v;
       }catch(Exception t){
       		if( t instanceof NDSException ) throw (NDSException)t;
       		else{
       			logger.error("Failed", t);
       			throw new NDSException(t.getMessage(), t);
       		}
       }finally{
       		try{ con.close();}catch(Exception e){}
       }

  }
  /**
   * 
   * @param table
   * @param itemid
   * @param isDispatch
   * @param operatorDesc
   * @param con
   * @param checkStatus if true, will not delete when column "status" is 2
   * @param checkVoid if true, will check isactive column, should be N when delete
   * @return
   */
  private String deleteOne(Table table,int itemid, 
  		String operatorDesc,Connection con, boolean checkStatus, boolean checkVoid){
      try{
          Vector vec = new Vector();
          String sql = "";
          QueryEngine engine =QueryEngine.getInstance();
          
          QueryUtils.lockRecord(table,itemid,con);
          
          QueryUtils.checkStatus(table,itemid,con);
          /**
           * should be void
           */
          QueryUtils.checkVoid(table,itemid,"N",con);
          
          sql = getSql(table.getRealTableName(),itemid);
          vec.addElement(sql);
          
          helper.doTrigger("BD", table, itemid, con);
          int count = engine.doUpdate(vec,con);
          // notify
          /*try{
              helper.Notify(TableManager.getInstance().getTable(tableName),
                            itemid,operatorDesc,JNDINames.STATUS_DELETE ,con);
          }catch(Exception ee){}finally{
              
          }*/

          return null;
      }catch(Exception e){
          logger.error("Could not delete record(table="+table.getName()+",id="+ itemid+")", e );
          return e.getMessage();
      }
  }

 /*  form the String sql
  *  tablename: the name of the table
  *  id : the primary key
  */
   private String getSql(String tableName,int id){
     String sql = "delete from "+tableName+" where id = "+id;
     return sql;
    }
    private String getDispatchDeleteSQL(String tableName,int id){
     String sql = "delete from "+manager.getTable(tableName).getDispatchTableName() +" where id = "+id;
     return sql;
    }
}