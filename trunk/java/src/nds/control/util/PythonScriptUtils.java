/*
 * Agile Control Technologies Ltd,. CO.
 * http://www.agileControl.com
 */
package nds.control.util;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.*;
import org.python.util.PythonInterpreter;
import org.python.core.*; 
/**
 * 
 * @author yfzhu@agilecontrol.com
 */

public class PythonScriptUtils {
	/**
	 * 
	 * @param psh
	 * @param script in format like "=a1+a2", a1, a2 should be exists in params
	 * @param params
	 * @return
	 * @throws EvalError
	 */
	public static Object evalScript(PythonInterpreter psh, String script, Map params) {
        Object result = null;
        String error = null;
        if(params!=null){
        	for(Iterator it=params.keySet().iterator();it.hasNext();){
        		String key=(String) it.next();
        		psh.set(key, params.get(key));
        	}
        }
        return psh.eval( script );
	}
	public static PythonInterpreter createInterpreter(){
        // Create a PrintStream to capture output
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream pout = new PrintStream( baos );

        // Create an interpreter instance with a null inputstream,
        // the capture out/err stream, non-interactive
        PythonInterpreter psh = new PythonInterpreter();
        psh.setErr(pout);
        psh.setOut(pout);
        return psh;
		
	}
	
	public static Object evalScript(
            String script, StringBuffer scriptOutput, boolean captureOutErr, Map params) throws Exception
    {
        // Create a PrintStream to capture output
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream pout = new PrintStream( baos );

        // Create an interpreter instance with a null inputstream,
        // the capture out/err stream, non-interactive
        PythonInterpreter psh = new PythonInterpreter( );
        if(params!=null){
        	for(Iterator it=params.keySet().iterator();it.hasNext();){
        		String key=(String) it.next();
        		psh.set(key, params.get(key));
        	}
        }
        // Eval the text, gathering the return value or any error.
        PyObject result = null;
        
        psh.setOut(pout);
        psh.setErr(pout);
        try {
            // Eval the user text
            psh.exec( script );
            result=psh.get("retObj");
        } catch (Throwable t) {
        	if(captureOutErr)t.printStackTrace(pout);
        	else throw new nds.util.NDSException(t.getMessage(),t);
        }
        
        pout.flush();
        scriptOutput.append( baos.toString() );
        return result;

    }
	public static int convertInt(Object pyObject){
			return ((PyObject)pyObject).asInt();
	}
}
