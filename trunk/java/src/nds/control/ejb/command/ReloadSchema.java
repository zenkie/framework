package nds.control.ejb.command;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.rmi.RemoteException;
import java.util.Properties;

import nds.control.ejb.Command;
import nds.control.event.DefaultWebEvent;
import nds.control.util.EJBUtils;
import nds.control.util.ValueHolder;
import nds.schema.TableManager;
import nds.util.NDSException;
import bsh.EvalError;
import bsh.Interpreter;

/**
 *  Reload Schema from db
 */
public class ReloadSchema extends Command{

    public synchronized ValueHolder execute(DefaultWebEvent event) throws NDSException ,RemoteException{
    	// check permission
    	event.setParameter("directory","RELOAD_SCHEMA");
    	helper.checkDirectoryReadPermission(event, helper.getOperator(event));
    	long t= System.currentTimeMillis();
    	// reload from db
    	
        Properties props=EJBUtils.getApplicationConfig().getConfigurations("schema").getProperties();
        
    	ValueHolder v = new ValueHolder();
        if(! "true".equalsIgnoreCase(props.getProperty("modify","true"))){
        	v.put("message","@exception@,@no-permission@") ;
        }else{
	    	/**
	    	 * Create a tmp instance and try loading, if success, will replace public instance
	    	 */
	    	TableManager.getTmpInstance().init(props, true);
	    	TableManager.replacePublicInstance();
	    	v.put("message","@complete@,@consumed-to@"+ (System.currentTimeMillis()-t)/1000.0 +" @seconds@!") ;
        }
    	return v;
    }
}