package nds.control.ejb;

import nds.control.event.DefaultWebEvent;
import nds.control.event.NDSEvent;
import nds.control.util.EJBUtils;
import nds.control.web.*;
import nds.log.Logger;
import nds.log.LoggerManager;
import nds.util.threadpool.DefaultThreadPool;
import nds.util.threadpool.ThreadPool;

/**
 * 
 * @author yfzhu
 * @deprecated
 */
public class AsyncControllerBean{
    private static Logger logger= LoggerManager.getInstance().getLogger(AsyncControllerBean.class.getName());
    private static ThreadPool threadPool=  new DefaultThreadPool(5);

    public void sendEvent(final DefaultWebEvent event ) throws Exception {
    	 if(true)throw new nds.util.NDSException("Deprecated, use ClientControllerWebImpl handleBackground instead");
//          logger.debug("Begin sending event:"+event);
          threadPool.invokeLater(
              new Runnable() {
                public void run() {
                    try{
                        /* yfzhu 20040316 ejb may not be used */
                        ClientController ccb;
                        try{
                            ccb= EJBUtils.getClientControllerHome().create();
                        }catch(Exception e){
                            logger.info("Cound not create ejb of ClientController, using plain class instead:"+ event, e);
                            ccb= new ClientControllerBean();
                            ((ClientControllerBean)ccb).ejbCreate();
                        }
                        ccb.handleEvent(event);
                    }catch(Exception ex) {
                      logger.error("Errors found on handling event:"+ event, ex);
                    }

                }
              }
            );
    }

/**
 * <p>Title: NDS Project</p>
 * <p>Description: San gao shui yuan, mu xiang ren jia</p>
 * <p>Copyright: Copyright (c) 2001</p>
 * <p>Company: aic</p>
 * @author yfzhu
 * @version 1.0
 *
 * Use this class to send and receive point-to-point messages.
 * To send a text message:
 * <code>
 * AsyncControllerBean asyncControllerBean = new AsyncControllerBean();
 * asyncControllerBean.setEnvironment(hashtable);  //Specify any vendor-specific JNDI settings here
 * asyncControllerBean.sendEvent(NDSEvent);
 * asyncControllerBean.close(); //Release resources
 * </code>
 *
 * <code>
 * To receive a message:
 * AsyncControllerBean asyncControllerBean = new AsyncControllerBean();
 * asyncControllerBean.getQueueReceiver();
 * </code>
 */

/*public class AsyncControllerBean implements MessageListener {
  private Logger logger= LoggerManager.getInstance().getLogger(AsyncControllerBean.class.getName());

  private static Context context = null;
  private boolean transacted = false;
  private int acknowledgementMode = Session.AUTO_ACKNOWLEDGE;
  private Hashtable environment = null;
  private QueueConnectionFactory queueConnectionFactory = null;
  private QueueConnection queueConnection = null;
  private QueueSession queueSession = null;
  private QueueSender queueSender = null;
  private QueueReceiver queueReceiver = null;
  private Queue queue = null;
  private String queueConnectionFactoryName = "/nds/jms/AsyncControllerConnFactory";
  private String queueName = "/nds/jms/AsyncControllerQueue";
  public boolean isTransacted() {
    return transacted;
  }
  public void setTransacted(boolean transacted) {
    this.transacted = transacted;
  }
  public int getAcknowledgementMode() {
    return acknowledgementMode;
  }
  public void setAcknowledgementMode(int acknowledgementMode) {
    this.acknowledgementMode = acknowledgementMode;
  }
  public Hashtable getEnvironment() {
    return environment;
  }
  public void setEnvironment(Hashtable environment) {
    this.environment = environment;
  }
  public String getQueueConnectionFactoryName() {
    return queueConnectionFactoryName;
  }
  public void setQueueConnectionFactoryName(String queueConnectionFactoryName) {
    this.queueConnectionFactoryName = queueConnectionFactoryName;
  }
  public String getQueueName() {
    return queueName;
  }
  public void setQueueName(String queueName) {
    this.queueName = queueName;
  }
  Context getContext() throws Exception {
    if (context == null) {
      try {
        context = new InitialContext(environment);
      }
      catch(Exception ex) {
        ex.printStackTrace();
        throw ex;
      }
    }
    return context;
  }
  public QueueConnectionFactory getQueueConnectionFactory() throws Exception {
    if (queueConnectionFactory == null) {
      Object obj = getContext().lookup(queueConnectionFactoryName);
      queueConnectionFactory = (QueueConnectionFactory) obj;
    }
    return queueConnectionFactory;
  }
  public QueueConnection getQueueConnection() throws Exception {
    if (queueConnection == null) {
      queueConnection = getQueueConnectionFactory().createQueueConnection();
    }
    return queueConnection;
  }
  public QueueSession getQueueSession() throws Exception {
    if (queueSession == null) {
      queueSession = getQueueConnection().createQueueSession(transacted, acknowledgementMode);
    }
    return queueSession;
  }
  public Queue getQueue() throws Exception {
    if (queue == null) {
      Object obj = getContext().lookup(queueName);
      queue = (Queue) obj;
    }
    return queue;
  }
  public QueueSender getQueueSender() throws Exception {
    if (queueSender == null) {
      queueSender = getQueueSession().createSender(getQueue());
    }
    return queueSender;
  }
  public QueueReceiver getQueueReceiver() throws Exception {
    if (queueReceiver == null) {
      queueReceiver = getQueueSession().createReceiver(getQueue());
      queueReceiver.setMessageListener(this);
      logger.debug("Successfully register as listener.");
    }
    return queueReceiver;
  }
  public void sendEvent(NDSEvent event ) throws Exception {
        logger.debug("Begin sending event:"+event);
        ObjectMessage objMsg=getQueueSession().createObjectMessage(event);
        getQueueSender().send(objMsg);
        if (isTransacted()) {
          getQueueSession().commit();
        }
        logger.debug("End sending event:"+event);
  }
  //**
   * Call ClientControllerBean to handle message, which should be NDSEvent
   * @param message Instance of NDSEvent, if not, nothing will be done except some logs
   *
  public void onMessage(Message message) {
    logger.debug("message recieved£º"+message);
    try{
        if ( message instanceof ObjectMessage ) {
          ObjectMessage objectMessage = (ObjectMessage) message;
          //Process objectMessage here
          Object obj=objectMessage.getObject();
          if( obj instanceof NDSEvent){
            ClientController ccb=EJBUtils.getClientControllerHome().create();
            ccb.handleEvent((NDSEvent)obj);
            //ccb.remove();
          }else{
            logger.error("Message content("+obj.getClass()+")is not NDSEvent, not supported. ");
          }
        } else {
              logger.error("Message ("+message.getClass()+")is not ObjectMessage, not supported. ");
        }
        if (isTransacted()) {
            getQueueSession().commit();
        }
    }catch(Exception ex) {
      logger.debug("Errors found on handling message:"+ message, ex);
    }
  }
  public void close() throws Exception {
    if (queueSender != null) {
      queueSender.close();
    }
    if (queueReceiver != null) {
      queueReceiver.close();
    }
    if (queueSession != null) {
      queueSession.close();
    }
    if (queueConnection != null) {
      queueConnection.close();
    }
  }*/

}