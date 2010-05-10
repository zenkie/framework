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
 * Title:        NDS Project
 * Description:  San gao shui yuan, mu xiang ren jia
 * Copyright:    Copyright (c) 2001
 * Company:      aic
 * @author yfzhu
 * @version 1.0
 */
/**
*   ObjectDelete is a module to delete the record from the web
*
*/
public class ObjectDelete extends Command{
    private TableManager manager ;
    public ValueHolder execute(DefaultWebEvent event) throws NDSException ,RemoteException{
      	/**
      	 * 2005-11-15 增加了对status 字段的判断，如果status字段为2 则当前记录不允许删除。这种情况主要发生
      	 * 在支持审计的单视图模式下。此模式下所有的单据都在一个界面，对于已审计通过的单据应禁止删除。系统会在通过
      	 * 审计的单据上修改status=2，作为判断条件。
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
       boolean bCheckStatus= (table.getColumn("status") !=null);
       // check status
       if ( bCheckStatus){
      	int status=Tools.getInt(engine.doQueryOne("select status from "+ table.getRealTableName()+ " where id="+ objectid, con),-1);
      	logger.error("Internal Error: status not valid(id="+objectid+")" );
      	if(status==-1)  throw new NDSException("Internal Error: status not valid");
      	if(status==JNDINames.STATUS_SUBMIT|| status==JNDINames.STATUS_AUDITING){
      		// already submmited, so will not allow delete
      		throw new NDSException("@object-already-submitted-no-delete@");
      	}
      }
       
       sql = getSql(tableName,objectid);
       /*vec.addElement(sql) ;
       //##################### added by yfzhu for dispatching to shop
       if ( table.getDispatchType() != table.DISPATCH_NONE &&
            table.isActionEnabled(Table.SUBMIT)==false && table.isActionEnabled(Table.AUDIT)==false){
           // all client will recieve this sql, but only those has there records
           // will be deleted
           vec.addElement( Pub.getExpDataRecord(-1, sql+";"));
       }
       realCount=1;*/
       
       /**
        * Lock first
        */
       QueryUtils.lockRecord(table, objectid, con);
       helper.doTrigger("BD", table, objectid, con);
       Table parent= helper.getParentTable(table,event);
       int[] poids= helper.getParentTablePKIDs( table, new int[]{objectid}, con);
       
       //helper.checkTableRows(parent, poids, con, helper.PARENT_NOT_FOUND);
       
       //check parent table records exist and modifiable
 	   helper.checkTableRowsModifiable(parent, poids, con);
       
       int count = con.createStatement().executeUpdate(sql);
       // call parent table's after modify method
       helper.doTrigger("AM", parent, poids, con);

       //SendMail(mail);
       ValueHolder v = new ValueHolder();
       String message ="@total-records-deleted@:" + count ;
       v.put("message",message) ;

       logger.info("deleted table="+ table+", id="+objectid+" by "+ usr.name+" of id"+ usr.id);
       return v;
       }catch(Exception e){
           if(e instanceof NDSException) throw (NDSException)e;
           else throw new NDSException(e.getMessage(),e);
       }finally{
           try{if(con !=null) con.close(); }catch(Exception eee){}
       }

  }

 /*  form the String sql
  *  tablename: the name of the table
  *  id : the primary key
  */
   private String getSql(String tableName,int id){
     String sql = "delete from "+manager.getTable(tableName).getRealTableName()+" where id = "+id;
     return sql;
    }
    private String getDispatchDeleteSQL(String tableName,int id){
     String sql = "delete from "+manager.getTable(tableName).getDispatchTableName() +" where id = "+id;
     return sql;
    }

    /**
     * Get AK data accordint to PK data
     * @return null if any error occurs.
     */
    private String[] getAKData(Table table, Integer[] Ids, Connection con){
        try{
        ArrayList al=new ArrayList();
        String q="select "+ table.getAlternateKey().getName() + " from "+ table.getRealTableName() +
                 " where " + table.getPrimaryKey().getName() + "=?";
        PreparedStatement pstmt= con.prepareStatement(q);
        for (int i=0;i< Ids.length;i++){
            pstmt.setInt(1, Ids[i].intValue() );
            ResultSet rs= pstmt.executeQuery();
            if( rs.next()){
                al.add(rs.getString(1));
            }else{
                al.add("id="+Ids[i] );
            }
            try{rs.close();}catch(Exception e2){}
        }

        String[] s= new String[al.size()];
        for (int i=0;i< s.length;i++){
            s[i]=(String) al.get(i);
        }
        return s;
        }catch(Exception e){
            logger.error("Error in getAKData():" + e);
            return null;
        }
    }

    /**
     * Notify of sheet table modification
     */
     private MailMsg prepareMail(Table sheetTable, int sheetObjectId, String creatorDesc, Connection con, boolean isModify){
         // first get the table record description
         try{
             String no=null;
             StringBuffer briefMsg=new StringBuffer(), detailMsg=new StringBuffer();
             NotificationManager nm=nds.mail.NotificationManager.getInstance();
             ResultSet rs= QueryEngine.getInstance().doQuery("select "+ sheetTable.getAlternateKey().getName() +
                     " from "+ sheetTable.getRealTableName()+ " where id=" + sheetObjectId);
             if( rs.next() ){
                 no= rs.getString(1);
             }
             try{ rs.close();} catch(Exception es){}
             if(no !=null) briefMsg.append( sheetTable.getDescription(Locale.CHINA) + "("+ no + ") 被"+ creatorDesc);
             else briefMsg.append( sheetTable.getDescription(Locale.CHINA) + "(id=" + sheetObjectId + ") 被"+ creatorDesc);
             if( isModify) briefMsg.append("修改.");
             else briefMsg.append("删除.");
             String webroot=nm.getProperty("weburl", "http://mit:8001/nds");
             detailMsg.append(briefMsg );
             if(isModify){
                 detailMsg.append("\n\r");
                 detailMsg.append("请访问网页"+ webroot+ "/objext/sheet_title.jsp?table="+sheetTable.getId() +"&id="+ sheetObjectId);
             }
             return nm.prepareMail(sheetTable.getId(), sheetObjectId,(isModify? "modify":"delete"), briefMsg, detailMsg,con);

         }catch(Exception e){
             logger.error("Could not notify modification of " + sheetTable.getName()+ ", id=" +sheetObjectId, e);
         }
         return null;
     }
     private void SendMail(MailMsg mail){
         NotificationManager nm=nds.mail.NotificationManager.getInstance();
         nm.SendMail(mail);
     }
}