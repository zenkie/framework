//Source file: f:\\tmp\\mail\\MailRobotSession.java

package nds.mail;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Properties;

import nds.log.Logger;
import nds.log.LoggerManager;
import nds.schema.TableManager;

/**
 * select sqlcondtion, robotparam, ownerid from notifyparams where table=xxx and
 * tableAction=xxx and robotAction='mail'
 *
 * for each fieldrows do
 *   if (isSQLConditionSatisfied( sqlc)==true) then
 *     if FindReciever(robotParam)="$me" then
 *        AddUserToMailingList  getUserName( ownerid)
 *    else
 *      AddUserToMailingList  Reciever
 *  end if
 * next
 * if mailingList.count > 0 then
 *   MailTo mailingList, "Action Subject", "mailBody"
 * end if
 */
public class MailRobotSession //implements RobotSession
{
    private static Logger logger= LoggerManager.getInstance().getLogger(MailRobotSession.class.getName());
    private Properties props ;
    private Connection conn;
    //private ObjectQueue queue;
   /**
    * @roseuid 3E6990110303
    */
   public MailRobotSession()//ObjectQueue queue)
   {
       //this.queue= queue;
   }
   public void init(Properties props){
       this.props= props;
       conn= (Connection)props.get("Connection");
       if ( conn==null){
           logger.error("Could not find Connection in Init()");
       }
   }

   /**
    * @param tableId
    * @param objectId
    * @param tableAction
    * @param briefMsg
    * @param detailMsg
    * @param con the connection to db, if omitted, a new connection will be created
    *      this is used because, the ControllerBean's action may not be stored to db really,
    *      so the modified data may not be visiable to the newly created connection, if they
    *      are not in one connection.
    * @roseuid 3E6990110321
    */
   /*public void handleObject(int tableId, int objectId, String tableAction, StringBuffer briefMsg, StringBuffer detailMsg)
   {
       MailMsg msg=prepareMail(tableId, objectId, tableAction, briefMsg,detailMsg);
       try{
           SendMail(msg);
       }catch(Exception e){
           logger.error("Could not send mail("+ tableId +","+ objectId+ ","+ tableAction+","+ briefMsg+"):"+e);
       }
   }*/
   /**
    * prepare mail for delivery, the mail not not be sent. Such as when deleting objects,
    * mail must be prepared before deleting to check whether to send or not. If errors encount
    * when deleting object, mail should not be sent out then.
    * @return null if errors found
    */
   public MailMsg prepareMail(int tableId, int objectId, String tableAction, StringBuffer briefMsg, StringBuffer detailMsg){
       MailMsg msg= null;
       ArrayList mailingList= new ArrayList();
       try{
           TableManager manager= TableManager.getInstance();
           String tableName= manager.getTable(tableId).getName() ;
           Statement st= conn.createStatement();
           String sql="select sqlcondition, robotparam, ownerid from notifyparams where tablename='"+
                          tableName+"' and tableAction='"+ tableAction+"' and robotAction='mail'";
//           logger.debug(sql);
           ResultSet rs= st.executeQuery( sql);
           while( rs.next()){
               String sqlc= rs.getString(1);
               String param= rs.getString(2);
               int oid= rs.getInt(3);
               if (isSQLConditionSatisfied( sqlc, tableId,objectId )==true) {
                   String reciever=FindReciever( param);
                   if ( "$me".equalsIgnoreCase(reciever)  ){
                       String sMail= getUserMailAddress( oid);
                       if (sMail !=null && ! sMail.trim().equals(""))
                           mailingList.add(sMail);
                       else
                           logger.error("User(id="+ oid+ ")'s email address not set, could not send mail") ;
                   }else{
                       mailingList.add(reciever);
                   }
               }
           }
           try{ rs.close() ;} catch(Exception sqle){}
           try{ st.close() ;} catch(Exception sqle2){}
           msg =new MailMsg(mailingList, briefMsg, detailMsg, tableName+"_"+ objectId);
       }catch(Exception e){
           logger.error("Could not handle object("+ tableId +","+ objectId+ ","+ tableAction+","+ briefMsg+"):"+e);
       }
       return msg;
   }
   /**
    *SQL 条件，即满足此条件的单据或对象将触发通知发布
    * 假设存储的sqlCondition为
    *sqlCondtion= select count(*) from <table> , <table2> where <table>.<id> = $objectid or <table2>.<yyy> = sysdate
    *如果大于0 表示当前操作满足
    */
   private boolean isSQLConditionSatisfied(String sqlc, int tableId, int objectId) throws Exception{
       TableManager manager= TableManager.getInstance();
       String sqlCondition= nds.util.StringUtils.replace(sqlc, "$objectid", objectId+"", 1);
       String tableName= manager.getTable(tableId).getName() ;
       Statement st= conn.createStatement();
       boolean ret=false;
       ResultSet rs= st.executeQuery(sqlCondition);
       if( rs.next() ){
           int count= rs.getInt(1);
           if(count > 0 ){
               ret=true;
           }
       }
       try{ rs.close() ;} catch(Exception e){}
       try{ st.close() ;} catch(Exception sqle2){}
//       logger.debug("isSQLConditionSatisfied=:"+ret +"("+ sqlCondition+")");
       return ret;
   }

   /**
    * Find reciver email according to params item, currently the mail param is just
    * the reciever's email address
    */
   private String FindReciever(String param){
       return param;
   }
   /**
    * @return user's email address according to it's id in "Users" table
    */
   private String getUserMailAddress(int userId) throws Exception{
       Statement st= conn.createStatement();
       ResultSet rs= st.executeQuery("select email from employee where userId="+ userId);
       String mail=null;
       if( rs.next() ){
           mail= rs.getString(1);
       }
       try{ rs.close() ;} catch(Exception e){}
       try{ st.close() ;} catch(Exception sqle2){}
       return mail;
   }
   /*public void SendMail(MailMsg mail)throws Exception{
       //MailTo(mail.getMailingList(), mail.getSubject(),mail.getBody(), mail.getReference() );
       queue.addElement(mail);
   }*/

}
