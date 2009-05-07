package nds.net;

import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;

import nds.log.Logger;
import nds.log.LoggerManager;
import nds.util.NDSException;
import nds.util.ObjectQueue;
import nds.util.StringHashtable;
import nds.util.StringUtils;

import org.jdom.Attribute;
import org.jdom.Element;

import com.echomine.jabber.AbstractJabberMessage;
import com.echomine.jabber.DelayXMessage;
import com.echomine.jabber.ErrorMessage;
import com.echomine.jabber.JID;
import com.echomine.jabber.Jabber;
import com.echomine.jabber.JabberChatMessage;
import com.echomine.jabber.JabberCode;
import com.echomine.jabber.JabberContext;
import com.echomine.jabber.JabberIQMessage;
import com.echomine.jabber.JabberJDOMMessage;
import com.echomine.jabber.JabberMessage;
import com.echomine.jabber.JabberMessageEvent;
import com.echomine.jabber.JabberMessageException;
import com.echomine.jabber.JabberMessageListener;
import com.echomine.jabber.JabberPresenceMessage;
import com.echomine.jabber.JabberSession;
import com.echomine.jabber.JabberStringMessage;
import com.echomine.jabber.LastIQMessage;
import com.echomine.net.ConnectionEvent;
import com.echomine.net.ConnectionListener;
import com.echomine.net.ConnectionVetoException;

/**
 * Control Session to Jabber
 */
public class SessionController {
    public static final String XMLNS_ACT_POS = "act:pos";
    public static final int QUEUE_MAXLENGTH=100;
    public static final long RECONNECT_WAIT_TIME=1000; //1 seconds
    private Logger logger= LoggerManager.getInstance().getLogger(SessionController.class.getName());

    private SessionManager commander;// ServerManager or SyncManager

    private Properties props;
    private StringHashtable listeners ; // key: Listener.ID(String), value: SessionListener
    private JabberSession session ;
    private String myJID; // in format of "username@server/resource"
    private boolean isConnected ;
    private JabberContext context;

    private ObjectQueue queue; // the queue recieving MsgAndListener object
    private SessionMsgHandler msgHandler; // the handler works in a seperate thread listening on queue

    private boolean  bForceShutdown=false;// if true, controller will not try to connect again and again
    private NOP nop=new NOP(); // do nothing but send "" every minute

    private class MsgAndListener{
        public SessionMsg sessionMsg;
        public SessionListener sessionListener;
    }
    class SessionMsgHandler implements Runnable{
        private ObjectQueue msgs;
        private boolean stop=false;
        private boolean running=false;

        public SessionMsgHandler( ObjectQueue q){
            msgs=q;
        }
        public void kill(){
            stop =true;
            if(! running){
                msgs=null;
            }
            System.out.println("SessionMsgHandler destroied");
        }
        public boolean isRunning(){
            return running;
        }
        public void run(){
            running =true;
            logger.debug("SessionMsgHandler started.");
            while( !stop && msgs.hasMoreElements()){
              try{
                MsgAndListener ml=(MsgAndListener)queue.nextElement();
                //logger.debug("Using "+ ml.sessionListener.getID() +" to handle msg:" + ml.sessionMsg );
                ml.sessionListener.onMessage(ml.sessionMsg);
              }catch(Exception e){
                logger.debug("Error in SessionMsgHandler",e);
              }
            }
            running =false;
            if( stop){
                msgs=null;
            }
            logger.debug("SessionMsgHandler stopped");
        }
    }

    class DefaultRequestListener implements JabberMessageListener {
        public void messageReceived(JabberMessageEvent event) {
            JabberMessage msg = event.getMessage();
            try {
                //first make 100% sure that the message is not an error message
                //and that the message is a get message
                //otherwise, we'll go into a loop
                if ((msg.getMessageType() == JabberCode.MSG_IQ_LAST)) {
                    LastIQMessage lmsg = (LastIQMessage)msg;
                    //reply back with an idle time of 2000 secs for testing
                    if (!lmsg.isError() && lmsg.getType().equals(JabberIQMessage.TYPE_GET))
                        event.getSession().getClientService().sendIdleTimeReply(
                            new JID(lmsg.getFrom()), lmsg.getMessageID(), 2000);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    class DefaultMessageListener implements JabberMessageListener {
        public void messageReceived(JabberMessageEvent event) {
            JabberMessage msg = event.getMessage();
            try {
                AbstractJabberMessage jmsg = (AbstractJabberMessage)msg;
                if (jmsg.isError()) {
                    logger.info("Got Error JabberMessage:" + msg);
                } else {
                    logger.debug("Got msg" + msg);
                }
            } catch (ClassCastException ex) {
                //logger.info("[Message ID " + msg.getMessageID() + "] " + msg);
            }
        }
    }

    class DefaultPresenceListener implements JabberMessageListener {
        public void messageReceived(JabberMessageEvent event) {
            if (event.getMessageType() != JabberCode.MSG_PRESENCE) return;
            JabberPresenceMessage msg = (JabberPresenceMessage)event.getMessage();
            DelayXMessage delay = msg.getDelayMessage();
            String timestamp = new Date().toString();
            if (delay != null && delay.getTimeInLocal() != null)
                timestamp = delay.getTimeInLocal().toString();
            System.out.println("[Presence Listener (" + timestamp + ")] " + msg.getFrom() + " is " + msg.getType() +
                ": " + msg.getStatus());
        }
    }
    public SessionManager getCommander(){
        return commander;
    }
    public SessionController(Properties props, SessionManager commander) {
        this.commander= commander;
        this.props= props;
        Enumeration enu= props.keys() ;
        while ( enu.hasMoreElements() ){
            String k= (String) enu.nextElement();
            System.out.println("Property " + k+ " =" + props.get(k));
        }
        queue= new ObjectQueue(QUEUE_MAXLENGTH);
        queue.setInDataPreparing(true);
        msgHandler= new SessionMsgHandler(queue);
        Thread msgThread= new Thread(msgHandler);
        msgThread.setDaemon(true);
        try{
            msgThread.start();
        }catch(Exception e){
            logger.error("Could not start SessionMsgHandler.", e);
        }
        isConnected= false;
        listeners= new StringHashtable();
        myJID= props.getProperty("username","ndsjava")+"@"+
                    props.getProperty("server","localhost")+"/"+
                    props.getProperty("resource","Work");
        logger.debug("Login in Jabber using " + myJID);
        context = new JabberContext(props.getProperty("username","ndsjava"),
                                    props.getProperty("password"),
                                    props.getProperty("server", "localhost"));
        Jabber jabber = new Jabber();
        session= jabber.createSession(context);

        session.getConnection().addConnectionListener(
                new ConnectionListener() {
            public void connectionStarting(ConnectionEvent event) throws ConnectionVetoException {
                //logger.debug("Jabber Connection starting: " + event.getConnectionModel());
            }
            public void connectionEstablished(ConnectionEvent event) {
                logger.debug("Jabber Connection established: " + event.getConnectionModel());
            }
            public void connectionClosed(ConnectionEvent event) {
                isConnected=false;
                logger.error("Jabber Connection closed: " + event.getConnectionModel()+ "," + event.getSource() );
                if ( event.isError() ) {
                   logger.error("Jabber Connection closed on Error:" + event.getErrorMessage() );
                }else if (event.isRejected() ) {
                    logger.error("Jabber Connection closed on Rejected");
                }else if (event.isVetoed() ) {
                    logger.error("Jabber Connection closed on Vetoed");
                }
                if( bForceShutdown == false){
                    try{
                        logger.debug("Will sleep "+ (RECONNECT_WAIT_TIME/1000)+ " seconds then retry connection.");
                        Thread.sleep(RECONNECT_WAIT_TIME);
                        start();
                    }catch(Exception e){
                        logger.error("Errors found when connect:"+ e);
                    }

                }
            }
        });
        session.getConnection().addMessageListener(new JabberMessageListener() {
            public void messageReceived(JabberMessageEvent event) {
                if (event.getMessageType() != JabberCode.MSG_CHAT) return;
                JabberChatMessage msg = (JabberChatMessage)event.getMessage();

                onChatMessage(msg);
            }
        });
        session.getConnection().addMessageListener(new DefaultMessageListener());
        session.getConnection().addMessageListener(new DefaultPresenceListener());
        session.getConnection().addMessageListener(new DefaultRequestListener());
    }
    private void sendHelloMsg(){
        try{
        JabberChatMessage msg=new JabberChatMessage("chat");
        msg.setBody("hello, this is java.");
        msg.setSubject("hello");
        msg.setTo("winjab@localhost");

        session.sendMessage(msg);
        }catch(Exception e){
            logger.debug("Error in say hello to winjab@localhost/Work", e);
        }
    }
    /**
     * Handling jabber message
     */
    private void onChatMessage(JabberChatMessage msg){
        try{
        ErrorMessage em=null; String errorString; int errorCode=0;
        if (msg.isError()) {
            logger.debug("[Error ID " + msg.getMessageID() + "] " + msg);
            em=msg.getErrorMessage();
            reportError("Controller", em.getMessage() );
            return;
           // errorString= em.getMessage();
           // errorCode= em.getCode();

        }
        // get X tags
        JabberMessage xmsg= msg.getXMessage(XMLNS_ACT_POS);
        Element ele=null;
        if ( xmsg ==null || !(xmsg instanceof JabberJDOMMessage)){
            logger.debug("Msg can not be converted to SessionMsg:" + msg.toString() );
            return;
        }
        ele= ((JabberJDOMMessage)xmsg).getDOM();
//        logger.debug("act:pos:" + ele);
        SessionMsg sms=new SessionMsg(commander.getSessionMsgPrefix() );
        // set origional msg
        sms.setOrigionalJabberMessage(msg);

        Attribute attrib;
//        String pn, pv;
        for ( Iterator it= ele.getAttributes().iterator();it.hasNext();){
            attrib=(Attribute) it.next();
           // pv= ele.getAttributeValue(pn);
            sms.addParam(attrib.getName() , attrib.getValue() );
        }
        SessionListener slsnr= findOrCreateListener(sms);
        if ( slsnr ==null) {
            logger.debug("Can not find or create SessionListener for msg:" + sms);
            return;
        }
        //slsnr.onMessage(sms);
        MsgAndListener ml= new MsgAndListener();
        ml.sessionListener = slsnr;
        ml.sessionMsg = sms;
        queue.addElement( ml);// just add the element to queue, and the SessionMsgHandler will handle it.
        }catch(Exception e){
            logger.error("Error handling msg:"+ msg, e);
        }
    }
    /**
     * Find or Create SessionListener for messsage, if could not handle, return null.
     * Accroding to "CommandType" in sms.
     */
    private  SessionListener findOrCreateListener(SessionMsg sms){
        SessionListener lsnr=null; String name;
        name=sms.getParam("CommandType");
        if ( name==null ){
            logger.error("SessionMsg has no 'CommandType', could not create listener.");
            return null;
        }

        lsnr=(SessionListener) listeners.get(name);
        if ( lsnr==null){
            // try to create that one, and make it prepare to listen message
            try {
                Class c= Class.forName("nds.net."+ name);
                lsnr =(SessionListener) c.newInstance();
                //System.err.println("Lsnr.getType():" + lsnr.getType()+ ",Name:" + name);
                listeners.put(lsnr.getType() ,lsnr);
                lsnr.setController(this);
                lsnr.start();
                logger.debug("Deamon :"+ name +" created and ready for handling.");
            } catch (Exception e) {
                logger.error("Could not start daemon: " +name, e);
            }

        }
        return lsnr;
    }
    /**
     * start daemon processes
     */
/*    private void startDaemons(){
        // daemons has elements seperated by comma
        StringTokenizer ds= new StringTokenizer(props.getProperty("daemons", "DBImport"),",");
        while( ds.hasMoreTokens()){
            String name=ds.nextToken().trim();
            try {
                Class c= Class.forName("nds.net."+ name);
                SessionListener lsnr =(SessionListener) c.newInstance();
                listeners.put(lsnr.getID(),lsnr);
                lsnr.setController(this);
                lsnr.start();
                logger.debug("Deamon :"+ name +" created and ready for handling.");
            } catch (Exception e) {
                logger.error("Could not start daemon: " +name);
            }
        }
    }*/
    public String getAttribute(String name, String defaultValue){
        return props.getProperty(name, defaultValue);
    }
    public void reportError(String moduleName, String error){
        logger.error("[" + moduleName+"]:" + error);
    }
    public void reportInfo(String moduleName, String info){
        logger.info("[" + moduleName+"]:" + info);
    }
    public void start() throws NDSException{
        int port =5222;
        try {
            port = (new Integer( props.getProperty("port", "5222"))).intValue() ;
        }catch(Exception ee){
            port = 5222;
        }
        try{
            session.connect(props.getProperty("server", "localhost"), port);
            session.getUserService().login();
            isConnected= true;
        } catch (JabberMessageException uex) {
            if( uex.getErrorCode()==401){
                // user not exist, so create one
                logger.debug("Could not login as unauthorized, try register...");
                try{
                HashMap fields = new HashMap();
                fields.put("username", context.getUsername());
                fields.put("password", context.getPassword());
                fields.put("email", props.getProperty("email","yfzhu@agileControl.com"));
                fields.put("name", context.getUsername());
                session.getUserService().register(context.getServerName(), fields);
                logger.debug("Register successful.");
                }catch(Exception ere){
                    logger.error("Regiester failed, abort jabber connection."+ ere);
                    throw new NDSException("Login and Register both failed." , ere);
                }
                try{
                    logger.debug("Login again after register...");
                    session.getUserService().login();
                    logger.debug("Login successful.");
                    isConnected= true;
                }catch(Exception ale){
                    logger.error("After registering, still could not logged in"+ ale);
                    throw new NDSException("Login and Register both failed.", ale);
                }
            }else
                logger.error("Error:Code=" + uex.getErrorCode() + ",message= "+ uex.getErrorMessage()  );
        } catch(com.echomine.common.SendMessageFailedException ex){
            throw new NDSException("Send message failed::" , ex);
        }catch(com.echomine.net.ConnectionFailedException cfe){
            throw new NDSException("Connection failed::" , cfe);
        }catch(java.net.UnknownHostException uhe){
            throw new NDSException("Unknown host::" + props.getProperty("server", "localhost") , uhe);
        }finally{
            if(isConnected== false)session.disconnect() ;
            else{
                try{
                    session.getPresenceService().setToAvailable(null, null);
                }catch(Exception pe){
                    logger.debug("Could not send presence information:", pe);
                }

                keepAlive();
                /*try{
                    sendHelloMsg();
                }catch(Exception er){
                    logger.debug("Could not send hello messge:"+ er);
                }*/
            }
        }

    }
    /*public void registerPresence(String friend){

    }*/
    public boolean isConnected(){
        return isConnected;
    }
    private void keepAlive(){
        try{
        nop.setSession(session);
        Thread t=new Thread(nop);
        t.setDaemon(true);
        t.start();
        }catch(Exception e){
            logger.error("Could not start NOP:" , e);
        }
    }

    public void kill(){
        msgHandler.kill() ;
        nop.kill() ;
        for( Iterator it= listeners.values().iterator(); it.hasNext() ;)
            ((SessionListener)it.next() ).kill();
        try{
            bForceShutdown=true;
            session.disconnect() ;
        }catch(Exception e){
            logger.error("Disconnect error:" , e);
        }
    }
    public void sendXML(String msgID, String xml, String peerJID){
//        String msg;
//        msg = "<message id='" + msgID + "' to='" + peerJID + "' type='chat'>" + xml + " < body > " +
//              StringUtils.escapeForXML(xml) + "<body></message>";
        JabberChatMessage jcm= new JabberChatMessage("chat");
        jcm.setFrom(myJID);
        jcm.setTo(peerJID);
        jcm.setBody(StringUtils.escapeForXML(xml));
        JabberStringMessage jsm=new JabberStringMessage(JabberCode.MSG_INIT , xml);
        jcm.setXMessage("x",jsm );

        try{
            session.sendMessage(jcm);
        }catch(Exception e){
            logger.error("Could not send xml message:" + jcm);
        }
    }
    /**
     * Each listener class instance will have only one listener be put, will use
     * listener#getType as listener identity.
     */
    public void addListener(SessionListener listener){
        listeners.put(listener.getType(), listener);
    }
    public void removeListener(SessionListener listener){
        listeners.remove(listener.getType());
    }
    public void sendMsg(SessionMsg msg){
        sendMsg(msg,msg.getTo(), null);
    }
    public void sendMsg(SessionMsg msg,  String to, String threadId){
        JabberChatMessage jms= new JabberChatMessage("chat");
        jms.setBody(msg.getBody() );
        jms.setFrom(myJID);

        if ( threadId ==null )
            threadId= msg.getThreadID();
        if ( threadId !=null)
            jms.setThreadID(threadId);
        else logger.debug("Thread ID not found where sending msg: " + msg);

        jms.setSubject(msg.getSubject());
        jms.setTo(to);

        Element ele =new Element("x", XMLNS_ACT_POS);
        Enumeration enu=msg.getParamNames() ;
        String pn, pv;
        while( enu.hasMoreElements() ){
            pn= (String) enu.nextElement();
            pv= msg.getParam(pn);
            ele.setAttribute(pn, pv);
        }
        JabberJDOMMessage dm= new JabberJDOMMessage(ele);
        jms.setXMessage(XMLNS_ACT_POS, dm  );
        try{
            logger.debug("Sending out message:"+ jms);
            session.sendMessage(jms);
        }catch(Exception e){
            logger.error("Error Sending msg:" , e);
        }
    }

}

