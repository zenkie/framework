/******************************************************************
*
*$RCSfile: ClientControllerBean.java,v $ $Revision: 1.4 $ $Author: Administrator $ $Date: 2006/03/14 10:52:32 $
*
*$Log: ClientControllerBean.java,v $
*Revision 1.4  2006/03/14 10:52:32  Administrator
*no message
*
*Revision 1.3  2005/09/14 01:52:54  Administrator
*no message
*
*Revision 1.2  2005/03/30 13:13:54  Administrator
*no message
*
*Revision 1.1.1.1  2005/03/15 11:23:14  Administrator
*init
*
*Revision 1.4  2004/02/02 10:42:58  yfzhu
*<No Comment Entered>
*
*Revision 1.3  2003/03/30 08:11:56  yfzhu
*Updated before subtotal added
*
*Revision 1.2  2002/12/17 08:45:36  yfzhu
*no message
*
*Revision 1.4  2001/12/28 14:20:01  yfzhu
*no message
*
*Revision 1.3  2002/01/04 01:43:21  yfzhu
*no message
*
*Revision 1.2  2001/11/07 20:58:46  yfzhu
*no message
*
*
********************************************************************/
package nds.control.ejb;

import java.rmi.RemoteException;

import javax.ejb.EJBHome;
import javax.ejb.EJBObject;
import javax.ejb.Handle;
import javax.ejb.RemoveException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.transaction.UserTransaction;

import nds.control.event.DefaultWebEvent;
import nds.control.event.NDSEvent;
import nds.control.util.EJBUtils;
import nds.control.util.ValueHolder;
import nds.log.Logger;
import nds.mail.NotificationManager;
import nds.util.NDSException;
import nds.util.Tools;

public class ClientControllerBean implements SessionBean,ClientController {
    private static Logger logger;
    private StateMachine sm;
    private SessionContext sessionContext;
    private int transactionTimeout=30*60;// in seconds, default to 30 minutes
//    private static AsyncControllerBean acb=null;// jms
    public ClientControllerBean(){
    }
//get StateMachine
    public StateMachine getStateMachine()
    {
      return this.sm;
    }
    public void ejbCreate() {
        try{
        logger= EJBUtils.getLogger(ClientControllerBean.class.getName());
        // for minutes in conf
        int i=Tools.getInt(EJBUtils.getApplicationConfigurations().getProperty("controller.transaction.timeout"),-1);
        if(i!=-1 )transactionTimeout= i*60;
        logger.debug("ejbCreat()");
        sm = new StateMachine(this, sessionContext);
        initNotificationManager();
  /*      if( acb ==null){
            // start queue reciever
            acb=new AsyncControllerBean();
            acb.getQueueConnection().start();
            acb.getQueueReceiver();
        }*/
        }catch(Exception e){
        	logger.error("Fail to init ClientControllerBean", e);
        	throw new nds.util.NDSRuntimeException("Internal Error: fail to init ClientControllerBean", e);
            //e.printStackTrace();
        }
    }
    private void initNotificationManager(){
        NotificationManager.getInstance().init();
    }

    /** Handle event
     *  @param ese if event instanceof DefaultWebEvent, and contains "nds.control.ejb.UserTransaction"="N"
     *   then will not create transaction. else create user definied transaction.
     *   nds.control.ejb.UserTransaction="N" occurs when command class need to handle transaction itself.
     * 	 Such as ExportSchema.
     */
    public ValueHolder handleEvent(DefaultWebEvent ese) throws NDSException {
        //logger.debug(ese.toString());
    	long currentTime= System.currentTimeMillis();
    	int timeLogId=-1;
    	UserTransaction ut=null;
        try{
            if(sessionContext ==null){
                // in this case, the bean is not work as a session bean,  just a normal class
                // so we will create a user transaction ourselves.
//            	if(ese instanceof DefaultWebEvent){
//            		timeLogId=nds.util.TimeLog.requestTimeLog((String)((DefaultWebEvent)ese).getParameterValue("command"));
//            		doCreateUserTrans=Tools.getYesNo(((DefaultWebEvent)ese).getParameterValue("nds.control.ejb.UserTransaction"), true);
//            		if(doCreateUserTrans){
//                		//should check whether command using interal transaction or not
//            			//not implemented yet
//            		}
//            	}
            	String coomd;
            	if ((coomd = (String)ese.getParameterValue("command")) != null)
            	{
            		timeLogId=nds.util.TimeLog.requestTimeLog(coomd);
            	}
            	boolean doCreateUserTrans;
            	if(doCreateUserTrans= ese.shouldCreateUserTransaction()){
	                ut= EJBUtils.getUserTransaction();
	                logger.debug("Using user transaction instead.");
	                ut.setTransactionTimeout(transactionTimeout); // 30 minutes default
	                ut.begin();
            	}
            }
            ValueHolder holder= (sm.handleEvent(ese));
            if(ut !=null) ut.commit();
        	
           // if(ese instanceof DefaultWebEvent){
            //	DefaultWebEvent de=(DefaultWebEvent)ese;
            	String cmd=(String)ese.getParameterValue("command");
            	logger.debug("Duration("+ cmd+"):"+ (System.currentTimeMillis()-currentTime)/1000.0+" s ( since event creation:"+
            			(System.currentTimeMillis()-((DefaultWebEvent)ese).getCreationDate().getTime())/1000.0+" s");
            	
            if (timeLogId > 0)nds.util.TimeLog.endTimeLog(timeLogId);
            	
         //   }

            return holder;
        }catch( Throwable ee){
            if( ese instanceof DefaultWebEvent)
                logger.debug("Error handling " + ((DefaultWebEvent)ese).toDetailString() , ee);
            else
                logger.debug("Error handling " + ese , ee);

            if(sessionContext!=null)sessionContext.setRollbackOnly();
            else if (ut !=null) {
                try{ ut.rollback();}catch(Throwable e){
                    logger.error("Could not rollback.", e);
                }
            }
            if( ee instanceof NDSException) throw (NDSException)ee;
            else{
            	if (ee instanceof Error) throw new NDSException("“Ï≥£:"+ee.getMessage());
				else	
					throw new NDSException("“Ï≥£",(Exception)ee);
            }
        }
    }
    public void setSessionContext(SessionContext sc) {
        this.sessionContext = sc;
    }
    public void ejbRemove() {
        logger.debug("ejbRemove()");
        sm = null;
        // this method will be called at the time of sign off.
        // destroy all the EJB's created by the client controller.

    }

    public void ejbActivate() {
        logger.debug("ejbActivate()");
    }

    public void ejbPassivate() {
        logger.debug("ejbPassivate()");
    }
    // Methods, from EJBObject
    public EJBHome getEJBHome() throws RemoteException{ return null;}
    public Handle getHandle() throws RemoteException{ return null;}
    public Object getPrimaryKey() throws RemoteException{ return null;}
    public boolean isIdentical(EJBObject eJBObject) throws RemoteException{ return false;}
    public void remove() throws RemoteException, RemoveException{ 
    	try{
    		sm.destroy();
    	}catch(Throwable t){
    		throw new RemoveException(t.getMessage());
    	}
    }

}