/*
 * Agile Control Technologies Ltd,. CO.
 * http://www.agileControl.com
 */
package nds.util;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.*;
import bsh.EvalError;
import bsh.Interpreter;
import bsh.ParseException;
import bsh.TargetError;

/**
 * 
 * @author yfzhu@agilecontrol.com
 */

public class BshScriptUtils {
	/**
	 * 
	 * @param bsh
	 * @param script in format like "=a1+a2", a1, a2 should be exists in params
	 * @param params
	 * @return
	 * @throws EvalError
	 */
	public static Object evalScript(Interpreter bsh, String script, Map params)throws EvalError{
        Object result = null;
        String error = null;
        if(params!=null){
        	for(Iterator it=params.keySet().iterator();it.hasNext();){
        		String key=(String) it.next();
        		bsh.set(key, params.get(key));
        	}
        }
        bsh.eval( "retValue" +script );
        result= bsh.get("retValue");
        return result;		
	}
	public static Interpreter createInterpreter(){
        // Create a PrintStream to capture output
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream pout = new PrintStream( baos );

        // Create an interpreter instance with a null inputstream,
        // the capture out/err stream, non-interactive
        Interpreter bsh = new Interpreter( null, pout, pout, false );
        return bsh;
		
	}
	
	public static Object evalScript(
            String script, StringBuffer scriptOutput, boolean captureOutErr, Map params)
            throws EvalError, NDSException
    {
        // Create a PrintStream to capture output
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream pout = new PrintStream( baos );

        // Create an interpreter instance with a null inputstream,
        // the capture out/err stream, non-interactive
        Interpreter bsh = new Interpreter( null, pout, pout, false );
        if(params!=null){
        	for(Iterator it=params.keySet().iterator();it.hasNext();){
        		String key=(String) it.next();
        		bsh.set(key, params.get(key));
        	}
        }
        // Eval the text, gathering the return value or any error.
        Object result = null;
        String error = null;
        PrintStream sout = System.out;
        PrintStream serr = System.err;
        if ( captureOutErr ) {
            System.setOut( pout );
            System.setErr( pout );
        }
        try {
            // Eval the user text
            result = bsh.eval( script );
        } catch (ParseException pe) {
        	pe.printStackTrace();
        	} catch (TargetError te) {
        		if(te.getTarget() instanceof NDSException) throw (NDSException)te.getTarget();
        } catch (EvalError ee) {
        	//throw new NDSException(ee.getMessage());
        	ee.printStackTrace();
        } finally {
            if ( captureOutErr ) {
                System.setOut( sout );
                System.setErr( serr );
            }
        }
        pout.flush();
        scriptOutput.append( baos.toString() );
        return result;
    }
}
