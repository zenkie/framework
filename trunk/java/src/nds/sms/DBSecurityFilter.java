package nds.sms;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Properties;

import nds.connection.FilterChain;
import nds.connection.Message;
import nds.connection.MessageFilter;
import nds.log.Logger;
import nds.log.LoggerManager;
import nds.query.QueryEngine;
import nds.util.NDSRuntimeException;
/**
 * Filter for valid phone number, table name will be specified by "table" property
 * direction set in "direction", "output" for message sender filter, "input" for receiver filter
 *
 * Currently the mechanism does not support rejection, only allowance supported
 * I think if rejection is needed, one more property should be provide: "mechanism" ( "deny"| "permit")
 */
public class DBSecurityFilter implements MessageFilter{
    private static Logger logger=LoggerManager.getInstance().getLogger(DBSecurityFilter.class.getName());
    private String sql; // seelct phoneno from <table> where phoneno=?
    private boolean isInput; // true for input, false for output
    public DBSecurityFilter() {
    }
    /**
     * @param request
     * @param chain
     * @return the new composite message or null if filter decide that
     * this message should be erased and not send/recieve
     * @roseuid 4048C3ED00BF
     */
    public Message doFilter(Message request, FilterChain chain){
        String usr;
        // if isInput set to true, then all incoming message will be checked for valid sender
        if( isInput) usr= request.getSender();
        else usr= request.getReceiver();
        // must exists in users, for permission
        boolean exists= false;
        java.sql.Connection conn=null;
        ResultSet rs=null;
        PreparedStatement pstmt=null;
        try {
            conn= QueryEngine.getInstance().getConnection();
            pstmt= conn.prepareStatement(sql);
            pstmt.setString(1, usr);
            rs= pstmt.executeQuery();
            if ( rs.next()){
                exists=true;
            }
        }
        catch (Exception ex) {
            logger.error("Could not execute sql " + sql + " , no= "+ usr);
        }finally{
            if ( rs!=null) try{ rs.close();}catch(Exception e){}
            if ( pstmt!=null) try{ pstmt.close();}catch(Exception e){}
            if ( conn!=null) try{ conn.close();}catch(Exception e){}
        }

        if (! exists){
            return null;
        }else
            return chain.doFilter(request);

    }

    /**
     * @param props
     sms.filter.A.direction=input
     sms.filter.A.file=e:/aic/sms/allow.txt
     *
     */
    public void init(Properties props){

        String d=  props.getProperty("direction", "input");
        if( "input".equalsIgnoreCase(d)) isInput= true;
        else isInput=false;

        logger.debug("direction= "+ d);
        String table=  props.getProperty("table");
        if ( table ==null) throw new NDSRuntimeException("Not found 'table' property for FileSecurityFilter");
        logger.debug("table= "+ table);

        sql= "select phoneno from "+ table + " where phoneno=?";
    }


    /**
     * Do nothing
     */
    public void destroy(){

    }

    /**
     * OUT_FILTER, IN_FILTER, DUPLEX_FILTER
     * @return int
     * @roseuid 4048D5C500D7
     */
   public int getDirection(){return DUPLEX_FILTER;}
}
