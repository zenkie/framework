/******************************************************************
*
*$RCSfile: CommandHandler.java,v $ $Revision: 1.2 $ $Author: Administrator $ $Date: 2005/03/16 09:04:49 $
*
*$Log: CommandHandler.java,v $
*Revision 1.2  2005/03/16 09:04:49  Administrator
*no message
*
*Revision 1.1.1.1  2005/03/15 11:23:14  Administrator
*init
*
*Revision 1.3  2004/02/02 10:42:58  yfzhu
*<No Comment Entered>
*
*Revision 1.2  2003/03/30 08:11:56  yfzhu
*Updated before subtotal added
*
*Revision 1.1.1.1  2002/11/29 02:50:34  yfzhu
*Active POS
*
*Revision 1.2  2001/11/07 20:58:46  yfzhu
*no message
*
*
********************************************************************/
package nds.control.ejb;

import java.rmi.RemoteException;
import java.util.Hashtable;

import nds.control.ejb.command.SaveReqParam;
import nds.control.event.DefaultWebEvent;
import nds.control.event.NDSEvent;
import nds.control.util.ValueHolder;
import nds.log.Logger;
import nds.log.LoggerManager;
import nds.util.NDSException;
import nds.util.Tools;

public class CommandHandler extends StateHandlerSupport{
	
    private static Logger logger= LoggerManager.getInstance().getLogger(CommandHandler.class.getName());
    private Hashtable commands=new Hashtable();//key: command name(String), value: Command
    
    private Command getCommand(String name) throws NDSException{
    	Command command=(Command) commands.get(name);
        if( command == null) {
        	command= CommandFactory.getInstance().createCommand(name);
        	command.setStateMachine( this.machine);
            commands.put(name, command);
        }
        return command;
    }
    public ValueHolder perform(NDSEvent e) throws NDSException, RemoteException {
        String command= "No value";

        DefaultWebEvent event= (DefaultWebEvent) e;
        command= (String)event.getParameterValue("command");
        Command comd= getCommand(command);


        /**
         * We got some commands that use internal transaction, so we should make sure that event contains
         * parameter that notify outside not creating transaction.
         * 
         * This is only for developer usage, when system is stable, comments these lines out for performance enhancement.
         */
    	/*boolean doCreateUserTrans=Tools.getYesNo(event.getParameterValue("nds.control.ejb.UserTransaction"), true);
        if(comd.internalTransaction()){// has internal transaction
        	if(doCreateUserTrans){
        		String errmsg="Internal error: command "+ comd.getClass()+" has internal transaction while controller creates UserTransaction for it";
                logger.error(errmsg);
        		logger.error(event.toDetailString());
        		throw new NDSException(errmsg);
        	}
        }else{
        	if(!doCreateUserTrans){
        		String errmsg="Internal error: command "+ comd.getClass()+" has no internal transaction while controller does not create UserTransaction either";
                logger.error(errmsg);
        		logger.error(event.toDetailString());
        		throw new NDSException(errmsg);
        	}
        }*/
        
        ValueHolder vd= comd.execute(event);
        return  vd;

    }


}