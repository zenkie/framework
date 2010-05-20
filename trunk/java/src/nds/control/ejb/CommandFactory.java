/******************************************************************
*
*$RCSfile: CommandFactory.java,v $ $Revision: 1.4 $ $Author: Administrator $ $Date: 2006/03/13 01:16:21 $
*
*$Log: CommandFactory.java,v $
*Revision 1.4  2006/03/13 01:16:21  Administrator
*no message
*
*Revision 1.3  2005/11/16 02:57:20  Administrator
*no message
*
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
*Revision 1.3  2001/11/20 22:36:09  yfzhu
*no message
*
*Revision 1.2  2001/11/07 20:58:46  yfzhu
*no message
*
*
********************************************************************/
package nds.control.ejb;

import java.util.Hashtable;

import nds.control.event.NDSEventException;
import nds.log.Logger;
import nds.log.LoggerManager;
import nds.util.Tools;
import nds.control.web.WebUtils;
import nds.util.Configurations;
/**
 * Command factory.
 */
public final class CommandFactory {

    private static CommandFactory instance=null;
    private static Logger logger= LoggerManager.getInstance().getLogger(CommandFactory.class.getName());

    private Hashtable commands;//key: command name(String), value: Command
    private CommandFactory() {
        // check file, if invalid, exit system
/*        if((!"8f6df39680a71c88df7dfeb2a908b4e0".equals(Tools.getFileCheckSum(this.getClass(), "nds.control.web.MainServlet")))){
				logger.error("Important file changed, will exit.");
				System.exit(1099);
        }
    	*/
        commands=new Hashtable();
    }
    /**
     * Different to getCommand in that this method will always create new
     * command object
     * @param name
     * @return
     * @throws NDSEventException
     * @since 2.0
     */
    public Command createCommand(String name ) throws NDSEventException {
    	// modified 2005-10-26 to allow command from other package
    	Command command=null;
    	Class c=null;
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
                }else throw new NDSEventException("Internal Error: can not find class to handle event with command:"+ name, e);
            }catch(ClassNotFoundException e2){
                throw new NDSEventException("Internal Error: can not find class to handle event with command:"+ name, e2);
            }
        }
        try{
            command=(Command) c.newInstance();
            logger.debug("Command :"+ name +" created and ready for handling.");
        }catch(Exception e3){
            throw new NDSEventException("Internal Error: can not instantiate class to handle event with command:"+ name, e3);
        }  
        return command;
    }
    /**
     * Get command according its name. Command are all in package named nds.control.ejb.command
     * and class name must be same as to the request name, for easier location.
     * @throws NDSExcption if Command could not be loaded
     * @deprecated
     */
    public Command getCommand(String name) throws NDSEventException {
        Command command=(Command) commands.get(name);
        if( command == null) {
        	command=createCommand(name);
            commands.put(name, command);
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
    public static synchronized CommandFactory getInstance() {
        if(instance==null) {
            instance=new CommandFactory();
        }
        return instance;
    }
}
