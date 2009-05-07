package nds.sms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;

import nds.control.web.SessionInfo;
import nds.log.Logger;
import nds.log.LoggerManager;
import nds.util.NDSException;
import nds.util.ObjectQueue;
/**
 *  Collection of ClientMsgQueue. Each client will construct a queue here to store messages.
 * 
 *  Clients are identified by user id, so one user should only has one connection to the manager.
 *
 *  Since messages should be cached to database to avoid message lost, this client manager
 *  contains connection to db ( shared by all clients)
 */
public class SMClientManager {
    private Hashtable clients ;// key client userId (Integer); value: ClientMsgQueue
    private java.sql.Connection dbcon;
    /**
     * Will try to get connection to db, and will throw Exception if failed
     */
    public SMClientManager() throws NDSException{
        clients=new Hashtable();
        dbcon= nds.query.QueryEngine.getInstance().getConnection();
    }

    /**
     * If client already exists, update ClientMsgQueue's sessionInfo
     * @throws Exception when userid is not a valid one( valid one should exist in smsclient table)
     */
    public ClientMsgQueue put(SessionInfo client) throws Exception{
        Integer uid=new Integer(client.getUserId());
        ClientMsgQueue c=(ClientMsgQueue) clients.get(uid);
        if (c ==null){
            c=new ClientMsgQueue(client,dbcon);
            clients.put(uid, c);
        }else{
            c.getSessionInfo().copy(client);
        }
        return c;
    }
    public ClientMsgQueue get(int userId){
        return (ClientMsgQueue) clients.get(new Integer(userId));
    }
    /**
     * Put default client (id=-1, user code is empty)
     * @return
     */
    public ClientMsgQueue putDefaultClient()  throws Exception{
    	SessionInfo client=new SessionInfo(-1, "default", "", System.currentTimeMillis(), "127.0.0.1", null);
    	ClientMsgQueue c=new ClientMsgQueue(client,dbcon);
        clients.put(new Integer(-1), c);
        return c;
    }
    /**
     * @return null if not found
     */
    public ClientMsgQueue get(String userCode){
        Integer userId= SMDBUtils.getUserId(userCode);
        if (userId==null) return null;
        return (ClientMsgQueue) clients.get(userId);
    }
    public int size(){
        return clients.size();
    }
    /**
     * Remove all client connection information and destroy all queue
     */
    public void clear(){
        ArrayList al=new ArrayList(clients.size());
        for(Iterator it= clients.values().iterator();it.hasNext();){
            ((ClientMsgQueue)it.next()).destroy();
        }
        clients.clear();
    }

    /**
     * Collection of SessionInfo
     */
    public Collection getAll(){
        ArrayList al=new ArrayList(clients.size());
        for(Iterator it= clients.values().iterator();it.hasNext();){
            al.add( ((ClientMsgQueue)it.next()).getSessionInfo());
        }
        return al;
    }
}
/**
 * This client queue contains messages for specified client.
 * 
 * All messages will be stored to database to avoid lost. Note when retrieved, the message will
 * still be remained in db. 
 */
class ClientMsgQueue {
    private static Logger logger=LoggerManager.getInstance().getLogger(ClientMsgQueue.class.getName());

    private java.sql.Connection dbcon;
    /**
     * Client session
     */
    private SessionInfo session;
    /**
     * incoming ShortMessage, ClientMsgQueue works as proxy of inbox, and cache all inbox changement to databse
     * only 1 msg will load in queue, and the remaining stayed to database. So when client asks
     * for messages when queue is empty, it will be blocked until new msg comes, and the internal
     * machenism will make sure that the once new msg comes, the queue will have at least one.
     */
    private ObjectQueue inbox;
    /**
     * Copy information from si
     */
    ClientMsgQueue(SessionInfo si, java.sql.Connection con) throws Exception{
        session=si.duplicate();
        inbox= new ObjectQueue(1); // only 1 msg will be set in queue, others , if have, will be stored in db
        inbox.setInDataPreparing(true);
        // load at most one msg from db
        dbcon=con;
        initInbox();
    }
    /**
     * Load at most one msg from db, if many, only smallest id will be retrieved
     * @throws Exception when userid is not a valid one
     */
    private void initInbox() throws Exception{
    	/**
    	 * Will not delete the message until client retrieve it. 
    	 * @see nextElement
    	 */
        ShortMessage msg= SMDBUtils.getOneMessage(dbcon, session.getUserId(), false);
        if ( msg !=null){
            inbox.addElement(msg);
        }
    }
    public SessionInfo getSessionInfo(){
        return session;
    }
    /**
     * @throw Exception when fail to write to db
     */
    public void addElement(ShortMessage msg) throws Exception{
        SMDBUtils.storeToDB(msg, dbcon);
        if ( inbox.size() ==0) initInbox() ;
    }
    public boolean hasMoreElements(){
        return inbox.hasMoreElements();
    }
    public int size(){
        return inbox.size();
    }
    /**
     * May wait until one message come
     * Will delete the retrieved message from db
     */
    public ShortMessage nextElement() {
        ShortMessage msg= (ShortMessage)inbox.nextElement();
        try{
            initInbox();
        }catch(Exception e){
            logger.error("Could not retrieve more elments from db for client "+ session.getUserName(), e);
        }
        if(msg!=null){
        	try{
        		SMDBUtils.deleteMessage( dbcon, msg.getMsgID());
        	}catch(Throwable t){
        		logger.error("Fail to delete msg id="+  msg.getMsgID(), t);
        	}
        }
        return msg;
    }
    /**
     * stop queue while not remove msg from db
     */
    public void destroy(){
        inbox.destroy();
    }
}