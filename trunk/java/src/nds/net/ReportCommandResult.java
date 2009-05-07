package nds.net;
import java.sql.Connection;
import java.sql.PreparedStatement;

import nds.query.QueryEngine;

/**
 * Report Command Status from POS <p>
 * "CommandType"= "ReportCommandResult"<br>
 * "Client"=m_controller.getAttribute("username")<br>
 * "ResultCode" command result <br>
 * "ResultString" command result string <br>
 * "OnCommand" command  <br>
 * "OnCommandParam" command params  <br>
 * <p>
 * No need for return anything back
 */
public class ReportCommandResult extends AbstractSessionListener{
    private String lastestVersion, downloadURL ;
    private boolean logToDB= true;
    Connection conn=null;
    PreparedStatement stmt=null;

    public ReportCommandResult() {
    }

    public void setController( SessionController controller){
        super.setController(controller);
        try{
            logToDB =(new Boolean(controller.getAttribute("ReportCommandResult.logdb", "true"))).booleanValue() ;
        }catch(Exception e){
            logToDB=true;
        }

        if( logToDB){
        try{
            conn= QueryEngine.getInstance().getConnection();
            stmt= conn.prepareStatement("insert into poscmdresult (id, clientname, resultcode, resultstring,oncmd,oncmdparam,creationDate) values ( seq_poscmdresult.nextval,?,?,?,?,?,?)");

        }catch(Exception e){
            logger.error("Error preparing connection to poscmdresult table.", e);
            try{if( stmt !=null) stmt.close();}catch(Exception e2){}
            try{if( conn!=null)conn.close();}catch(Exception  ee){}
        }finally{
        }
        }
    }
    public String getID(){
        return getType();
    }

    public void onMessage(SessionMsg msg){
        String client= msg.getParam("ClientName");
        String ResultCode= msg.getParam("ResultCode");
        String ResultString=msg.getParam("ResultString");
        String OnCommand= msg.getParam("OnCommand");
        String OnCommandParam= msg.getParam("OnCommandParam");

        // insert int db
        int code= -1;
        try{
            code= (new Integer(ResultCode)).intValue() ;
        }catch(Exception e){
//            logger.error("Could not parse " + ResultCode + " to int.");
        }
        try{
        if( stmt !=null){
            stmt.setString(1, client);
            stmt.setInt(2, code);
            stmt.setString(3, ResultString);
            stmt.setString(4, OnCommand);
            stmt.setString(5, OnCommandParam);
            stmt.setDate(6, new java.sql.Date(System.currentTimeMillis() ));
            stmt.executeUpdate() ;
        }else{
            logger.debug("ReportCommandResult request from " +client+", ResultCode:" + ResultCode+ ", ResultString=" +ResultString +
                         "OnCommand:"+ OnCommand+ ", OnCommandParam:" +OnCommandParam );

        }
        }catch(Exception ee){
            logger.debug("Could not record cmd", ee);
        }

    }
    public void kill(){
        super.kill();
        try{if( stmt !=null) stmt.close();}catch(Exception e2){}
        try{if( conn!=null)conn.close();}catch(Exception  ee){}
    }


}