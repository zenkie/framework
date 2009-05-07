package nds.sms;

import java.util.Hashtable;

import nds.log.Logger;
import nds.log.LoggerManager;
/**
 * Messages recieved will be written to db first to avoid lost
 * 
 * create a table sms_inmsg when using this function
 * create table shortmessage ( id not null, msgid ....)
 * Note id should be the primary key, msgid should be the unique index key
 * 
 *  
 */
public final class SMDBUtils {
    private static Logger logger=LoggerManager.getInstance().getLogger(SMDBUtils.class.getName());
    // this is used for db store
    private static String[] propertyNames=new String[]{
        "msgid", "receiver","duration", "content", "sender","state", "creationdate","parent","msgtype"
        };

    private static String SELECT_SQL,INSERT_SQL;
    private static String FIRST_MSG="select min(id) from sms_inmsg where receiver=?";
    private static String ALL_CLIENTS="select userid,usercode from sms_client";
    private static String DELETE_MSG="delete from sms_inmsg where id=?";
    private static String DELETE_MSG_BY_MSGID="delete from sms_inmsg where msgid=?";
    /**
     * cached usercode - userId here, if clients are too many, should discard this machinism
     */
    private static Hashtable htIdCode;// key userid (Integer), value userCode (String)
    private static Hashtable htCodeId;// key userCode(String), value userId(integer)

    static {
        SELECT_SQL="select id " ;
        String ask="";
        INSERT_SQL = "insert into sms_inmsg(id ";
        for (int i=0;i< propertyNames.length;i++){
             SELECT_SQL += "," + propertyNames[i];
             INSERT_SQL += "," + propertyNames[i];
             ask += ",?";
        }
        SELECT_SQL +=" from sms_inmsg where id=?";
        INSERT_SQL +=" )values(get_sequences('sms_inmsg') "+ ask+")";

    }
    public static String[] getSMProperyNames(){
        return propertyNames;
    }
    /**
     * Delete message in queue 
     * @param dbcon
     * @param messageId msgid of message property
     * @return true if at least one message deleted.
     * @throws Exception
     */
    public static boolean deleteMessage(java.sql.Connection dbcon, String messageId) throws Exception{
    	java.sql.PreparedStatement pstmt=null;
        try {
            pstmt=dbcon.prepareStatement(DELETE_MSG_BY_MSGID);
            pstmt.setString(1, messageId);
            return (pstmt.executeUpdate()>0);
        }
        finally {
            if(pstmt !=null){ try{ pstmt.close();}catch(Exception e){}}
        }
    }
    /**
     * Delete message in queue 
     * @param dbcon
     * @param messageId
     * @return true if at least one message deleted.
     * @throws Exception
     */
    public static boolean deleteMessage(java.sql.Connection dbcon, int messageId) throws Exception{
    	java.sql.PreparedStatement pstmt=null;
        try {
            pstmt=dbcon.prepareStatement(DELETE_MSG);
            pstmt.setInt(1, messageId);
            return (pstmt.executeUpdate()>0);
        }
        finally {
            if(pstmt !=null){ try{ pstmt.close();}catch(Exception e){}}
        }
    }
    /**
     * get receiver's first message from shortmessage queue in db
     * @param userId reveiver id
     * @param delete if true, will delete the retrieved message from db. You should delete it yourself when
     *  set delete to false
     */
    public static ShortMessage getOneMessage(java.sql.Connection dbcon, int userId, boolean delete) throws Exception{
        String receiver= getUserCode(userId);
        java.sql.PreparedStatement pstmt=null;
        java.sql.ResultSet rs=null;
        int id=-1;
        try {
            pstmt=dbcon.prepareStatement(FIRST_MSG);
            pstmt.setString(1, receiver);
            rs= pstmt.executeQuery();
            if ( rs.next()){
                id= rs.getInt(1);
            }
        }
        finally {
            if(rs !=null){ try{ rs.close();}catch(Exception e){}}
            if(pstmt !=null){ try{ pstmt.close();}catch(Exception e){}}
        }
        if (id >=0){
        	ShortMessage sm= loadFromDB(dbcon, id);
        	if(delete) deleteMessage(dbcon, id);
            return sm;
        }else
            return null;
    }
    /**
     * Load from database according to msgid
     * @param messageId is the db "id" column, not the message's "msgid"
     * @return null if not exists in db
     */
    public static ShortMessage loadFromDB(java.sql.Connection dbcon, int messageId) throws Exception{
        java.sql.PreparedStatement pstmt=null;
        java.sql.ResultSet rs=null;
        pstmt=dbcon.prepareStatement(SELECT_SQL);
        try {
        	pstmt.setInt(1, messageId);
            rs= pstmt.executeQuery();
            if ( rs.next()){
                ShortMessage sms= new ShortMessage();
                for (int i=0;i< propertyNames.length;i++){ // skip id
                   sms.setProperty(  propertyNames[i], rs.getString(1+ i));
                }
                return sms;
            }else return null;
        }
        finally {
            if(rs !=null){ try{ rs.close();}catch(Exception e){}}
            if(pstmt !=null){ try{ pstmt.close();}catch(Exception e){}}
        }

    }
    /**
     * Store to db
     */
    public static void storeToDB(ShortMessage sm, java.sql.Connection dbcon) throws Exception{
        java.sql.PreparedStatement pstmt=null;
        pstmt=dbcon.prepareStatement(INSERT_SQL);
        try {
//            pstmt.setInt(1, Tools.getInt(sm.getID(), -1));
            for (int i=0;i< propertyNames.length;i++){ // skip id
               pstmt.setString( i+1, sm.getProperty( propertyNames[i],""));
            }
            pstmt.executeUpdate();
        }
        finally {
            if(pstmt !=null){ try{ pstmt.close();}catch(Exception e){}}
        }

    }
    /**
      * cached usercode - userId here, if clients are too many, should discard this machinism
     */
    private static void initUserCodes() {
        htCodeId= new Hashtable();
        htIdCode=new Hashtable();
        String code;
        Integer id;
            // fetch from db
        java.sql.ResultSet rs= null;
        try {
            rs=nds.query.QueryEngine.getInstance().doQuery(ALL_CLIENTS);
            while( rs.next()){
                id=new Integer( rs.getInt(1));
                code= rs.getString(2);
                if(code==null) code=""; // in case of default client whose id=-1, and code =""
                htCodeId.put(code, id);
                htIdCode.put(id, code);
            }
        }catch(Exception e){
            logger.error("Could not retrieve information from smsclient table", e);
        }finally{
            try{if( rs !=null) rs.close();}catch(Exception e){}
        }

    }
    /**
     * There's a table in db named "sms_client" which contains code of client.
     * @return null if usercode not found
     */
    public static String getUserCode(int  userId) {
        if ( htIdCode ==null) initUserCodes();
        return (String)htIdCode.get(new Integer(userId));
    }
    public static Integer getUserId(String  userCode){
        if ( htIdCode ==null) initUserCodes();
        return (Integer)htCodeId.get( userCode);
    }

}