package nds.sms.test;

import java.util.Date;

import nds.connection.ConnectionEvent;
import nds.connection.ConnectionListener;
import nds.connection.Message;
import nds.connection.MessageListener;
import nds.control.util.EJBUtils;
import nds.log.Logger;
import nds.log.LoggerManager;
import nds.sms.SMConnection;
import nds.sms.ShortMessage;


public class Client implements ConnectionListener, MessageListener{
    private Logger logger= LoggerManager.getInstance().getLogger(Client.class.getName());

    private SMConnection conn;
    public Client() {
        init();
    }
    public void init(){
    }
    public void connect() throws Exception{
        conn= new SMConnection();
        conn.addConnectionListener(this);
        conn.addMessageListener(this);
        conn.init(EJBUtils.getApplicationConfigurations());
        conn.connect();

    }
    public void onMessageArrived(Message msg){
        logger.debug("onMessageArrived " +msg);
        logger.debug( msg.toXML());
    }

    public void connectionEstablished(ConnectionEvent e){
        logger.debug("connectionEstablished " +e);
        ShortMessage msg= new ShortMessage();
        msg.setContent("hello");
        msg.setCreationDate(new Date());
        msg.setReceiver("13061613691");
        msg.setSender("client");
        //conn.sendMessage(msg);
        //logger.debug( msg.toXML());
    }
    public void connectionClosed(ConnectionEvent e){
        logger.debug("connectionClosed " +e);
    }

    public static void main(String[] args)throws Exception {
        System.out.println("[Control.EJBUtils] Ininitializing logger system");
        String propFile=System.getProperty("nds.config.path","/nds.properties");
        LoggerManager.getInstance().init(propFile,true);
        EJBUtils.initConfigurations(EJBUtils.createConfigurations(propFile));

        Client client1 = new Client();
        client1.connect();
        Thread.sleep(1000000000);
    }
}