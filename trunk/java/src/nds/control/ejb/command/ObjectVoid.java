package nds.control.ejb.command;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Vector;

import nds.control.ejb.Command;
import nds.control.ejb.command.pub.Pub;
import nds.control.event.DefaultWebEvent;
import nds.control.event.NDSEventException;
import nds.control.util.ValueHolder;
import nds.mail.MailMsg;
import nds.mail.NotificationManager;
import nds.query.QueryEngine;
import nds.query.QueryUtils;
import nds.schema.Table;
import nds.schema.TableManager;
import nds.security.User;
import nds.util.JNDINames;
import nds.util.NDSException;
import nds.util.Tools;

/**
*   Invalidate one object
*
*/
public class ObjectVoid extends Command{
    private TableManager manager ;
    public ValueHolder execute(DefaultWebEvent event) throws NDSException ,RemoteException{
      	/**
      	 * 增加了对status 字段的判断，如果status字段不为1 则当前记录不允许作废
      	 */ 
    	java.sql.Connection con=null;
       try{
       manager = helper.getTableManager() ;
       int tableId = Tools.getInt(event.getParameterValue("table"),-1 ) ;
       Table table = manager.getTable(tableId) ;

       String tableName = table.getName() ;
       String tableDesc  = table.getDescription(Locale.CHINA);

       User usr= helper.getOperator(event);	
       QueryEngine engine =QueryEngine.getInstance();
       con= engine.getConnection();

       //int objectid = Tools.getInt(event.getParameterValue("id"),-1 ) ;
       int objectid =event.getObjectId(table, usr.adClientId, con);
       
       if(objectid==-1){
    	   throw new NDSException("@object-not-found@");
       }
       
       //String[] itemidStr = event.getParameterValues("itemid");

       Vector vec = new Vector();
       String sql = "";
       MailMsg mail=null; // elements of MailMsg
       String operatorDesc=usr.getDescription() ;
       
       /**
        * Unsubmit status check
        */
       QueryUtils.checkStatus(table, objectid, con);
       
       /**
        * Lock first
        */
       QueryUtils.lockRecord(table, objectid, con);

       sql = getSql(table,objectid,usr.getId().intValue());
       logger.debug(sql);
       
       
       int count = con.createStatement().executeUpdate(sql);

       //SendMail(mail);
       ValueHolder v = new ValueHolder();
       String message ="@total-records-void@:" + count ;
       v.put("message",message) ;

       logger.info("invalidate table="+ table+", id="+objectid+" by "+ usr.name+" of id"+ usr.id);
       return v;
       }catch(Exception e){
           if(e instanceof NDSException) throw (NDSException)e;
           else throw new NDSException(e.getMessage(),e);
       }finally{
           try{if(con !=null) con.close(); }catch(Exception eee){}
       }

  }

 /*  form the String sql to void one record
  *  tablename: the name of the table
  *  id : the primary key
  *  
  */
   static String getSql(Table table,int id, int userid){
		
		StringBuffer  sql =new StringBuffer();
		sql.append("update ").append(table.getRealTableName()).append(" set ");
		if( table.getColumn("modifierid")!=null) sql.append("modifierid=").append(userid).append(", ");
		if( table.getColumn("modifieddate")!=null) sql.append("modifieddate=sysdate, ");
		sql.append("isactive='N' where id = ").append(id);
		return sql.toString();
    }

}