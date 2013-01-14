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
import com.liferay.util.Validator;
import nds.control.ejb.command.SaveReqParam;
import nds.control.event.DefaultWebEvent;
import nds.control.event.NDSEvent;
import nds.control.event.NDSEventException;
import nds.control.util.ValueHolder;
import nds.control.web.WebUtils;
import nds.log.Logger;
import nds.log.LoggerManager;
import nds.util.Configurations;
import nds.util.NDSException;
import nds.util.Tools;

public class CommandHandler extends StateHandlerSupport{
	
    private static Logger logger= LoggerManager.getInstance().getLogger(CommandHandler.class.getName());
    private Hashtable commands=new Hashtable();//key: command name(String), value: Command
    
    private Command getCommand(String name) throws NDSException{
    	Command command=(Command) commands.get(name);
        if( command == null) {
        	command= createCommand(name);
        	command.setStateMachine( this.machine);
            commands.put(name, command);
        }
        return command;
    }
    /**
     * Sequence for looking up Command
     * 1) plugin (Since plugin class can be change anytime, developer can modify their classed on-site)
     * 2) inner command class (such as SaveCxtabJson
     * 3) inner common class (such as ObjectCreate for M_PURCHASECreate)
     */
    public ValueHolder perform(NDSEvent e) throws NDSException, RemoteException {
        String command= "No value";
        
        DefaultWebEvent event= (DefaultWebEvent) e;
        command= (String)event.getParameterValue("command");
		if (Validator.isNull(command)) {
			logger.warning("command not found in event:"
					+ event.toDetailString());
			ValueHolder vd = new ValueHolder();
			vd.put("message", "");
			vd.put("code", Integer.valueOf(0));
			return vd;
		}
        
        nds.io.PluginController pc=(nds.io.PluginController) WebUtils.getServletContextManager().getActor(nds.util.WebKeys.PLUGIN_CONTROLLER);
        Command cmd= pc.findPluginCommand(command);
        
        if(cmd!=null) cmd.setStateMachine(machine);
        else{
        	cmd= getCommand(command);
        	
        }
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
        /**
         * 如果command是一个内部事件的话，用户不能再创建一个用户事件的判定
         */
		if (cmd.internalTransaction() && event.shouldCreateUserTransaction()) {
			String errmsg = "Internal error: command "
					+ cmd.getClass()
					+ " has internal transaction while controller creates UserTransaction for it";
			logger.error(errmsg);
			logger.error(event.toDetailString());
			throw new NDSException(errmsg);
		}
        ValueHolder vd= cmd.execute(event);
        return  vd;

    }
    /**
     * Different to getCommand in that this method will always create new
     * command object
     * @param name
     * @return
     * @throws NDSEventException
     * @since 4.1
     */
    public Command createCommand(String name ) throws NDSEventException {
    	// modified 2005-10-26 to allow command from other package
    	Command command=null;
    	Class c=null;
    	String orgName=name;
    	name= name.trim();
        try {
            // try figure the special command name, such as PromotionAShtSubmit
        	if( name.indexOf('.')>0 ) c= Class.forName(name);
            else c= Class.forName("nds.control.ejb.command."+ name);
        } catch (ClassNotFoundException e) {
            try{
                // try figure the default command name, such as ObjectCreate
                name=commandName(name);
                if (name !=null){
                	if(name.indexOf('.')>0 ) c= Class.forName(name);
                    else{
                    	/**
                    	 will search for command class in following package order:
                    	  nds.control.ejb.command,
                    	  package specified by Configuration property: nds.control.ejb.command.ext.package
                    	  
                    	*/	
                    	try{
                    		c= Class.forName("nds.control.ejb.command."+ name);
                    	}catch(ClassNotFoundException c2){
                    		Configurations conf= (Configurations)WebUtils.getServletContextManager().getActor( nds.util.WebKeys.CONFIGURATIONS);
                    		String packageName =conf.getProperty("nds.control.ejb.command.ext.package","nds.control.ejb.command.ext");
                    		logger.debug("try loading class "+ packageName+"."+ name);
                    		c= Class.forName(packageName+"."+ name);
                    	}
                    }
                }else throw new NDSEventException("Internal Error: can not find class to handle event with command:"+ orgName, e);
            }catch(ClassNotFoundException e2){
                throw new NDSEventException("Internal Error: can not find class to handle event with command:"+ orgName, e2);
            }
        }
        try{
            command=(Command) c.newInstance();
            logger.debug("Command :"+ name +" created and ready for handling.");
        }catch(Exception e3){
            throw new NDSEventException("Internal Error: can not instantiate class to handle event with command:"+ orgName, e3);
        }  
        return command;
    }
    
    /** Copied from nds.control.web.DefaultRequestHandler, which was created by tony
        I move it here for late handling, I will first trying to find the command, if
        not success, then use the default one.

        I do this because some sheet(PromotionASht) need special handling when submit.
    **/
    private String commandName(String command){
    	if(command.endsWith("ListCreate")){
            return "ObjectCreate";
        }else if(command.endsWith("ListModify")){
            return "ListModify";
        }else if(command.endsWith("ListDelete")){
            return "ListDelete";
        }else if(command.endsWith("ListSubmit")){
            return "ListSubmit";
        }else if(command.endsWith("Create")){
            return "ObjectCreate";
        }else if(command.endsWith("Modify")){
            return "ObjectModify";
        }else if(command.endsWith("Delete")){
            return "ObjectDelete";
        }else if(command.endsWith("Submit")){
            return "ObjectSubmit";
        }else if(command.endsWith("Unsubmit")){
            return "ObjectUnsubmit";
        }else if(command.endsWith("Accept")){
            return "ObjectAccept";
        }else if(command.endsWith("Reject")){
            return "ObjectReject";
        }else if(command.endsWith("Request")){
            return "ObjectRequest";
        }else if(command.endsWith("Void")){
            return "ObjectVoid";
        }else if(command.endsWith("Unvoid")){
            return "ObjectUnvoid";
        }else if(command.endsWith("ListVoid")){
            return "ListVoid";
        }else if(command.endsWith("ListUnvoid")){
            return "ListUnvoid";
        }
        return null;
    }

}