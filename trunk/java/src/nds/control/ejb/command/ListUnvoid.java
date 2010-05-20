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
*  Valid all records selected, if condition ok
*/
public class ListUnvoid extends Command{
  private TableManager manager;
  public ValueHolder execute(DefaultWebEvent event) throws NDSException ,RemoteException{

       manager = helper.getTableManager() ;
       
       int tableId = Tools.getInt(event.getParameterValue("table", true),-1 ) ;
       String[] itemidStr = event.getParameterValues("itemid", true);

       Table table = manager.getTable(tableId) ;
       User user= helper.getOperator(event);
       String operatorDesc=user.getDescription();
       String tableName = table.getRealTableName() ;
       String tableDesc  = table.getDescription(event.getLocale());
       
       if (itemidStr==null) itemidStr= new String[0];
       java.sql.Connection con=null;
       QueryEngine engine =QueryEngine.getInstance();
       con= engine.getConnection();
       try{
       //logger.debug(" parent table of " + table + " is " + parent);
       int[] oids=new int[itemidStr.length];
       for(int i=0;i<oids.length;i++) oids[i]= Integer.parseInt(itemidStr[i]);
       
       ValueHolder v = new ValueHolder();
       
       // 由于界面上无法控制所有的对象都具有相同层次的权限，故需要在此进行权限认证
   		// check permissions on all objects
   		if (!SecurityUtils.hasPermissionOnAll(user.getId().intValue(), user.getName(), 
   				table.getName(), itemidStr, Directory.WRITE, event.getQuerySession())){
   			v.put("message", "@no-void-permission-on-all-pls-do-one-by-one@");
   			return v;
   		}
       
        String res = "", s; int errCount=0;
        for(int i = 0;i<itemidStr.length ;i++){
            int itemid = Tools.getInt(itemidStr[i],-1) ;
            s= unvoidOne(table, itemid, user.getId().intValue(),con );
            if (s !=null) {
                res += s+ "<br>";
                errCount ++;
            }else{
                logger.info("Invalidate table="+ table+", id="+itemid+" by "+ user.name+" of id "+ user.id);
            }
        }
        
        String message = null;

        message =itemidStr.length + "@line@ @request-to-void@";
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
   * @return error message, and null if ok
   * 
   */
  private String unvoidOne(Table table,int itemid,int userId,Connection con){
      try{
          
          QueryUtils.lockRecord(table,itemid,con);
          QueryUtils.checkVoid(table, itemid,"N", con);
          
          String sql= ObjectUnvoid.getSql(table, itemid, userId);
          logger.debug(sql);
          int cnt=con.createStatement().executeUpdate(sql);
          return null;
          
      }catch(Exception e){
          logger.error("Could not void record(table="+table.getName()+",id="+ itemid+")", e );
          return e.getMessage();
      }
  }

 
}