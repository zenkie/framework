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
	 * Run process instance
	 * @param fileName python file name, or module name
	 * @param pInstanceId ad_pinstance_id
	 * @return
	 * @throws Exception
	 */
	public static String runProcess(String fileName, int pInstanceId)throws Exception{
		JythonObjectFactory factory = new JythonObjectFactory(
				nds.process.ProcessRunner.class, fileName, fileName);
		nds.process.ProcessRunner pr=(nds.process.ProcessRunner)factory.createObject();
		return pr.execute(pInstanceId);

	}
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

/**
 * Jython Object Factory using PySystemState
 */
class JythonObjectFactory {

 private final Class interfaceType;
 private final PyObject klass;

 // Constructor obtains a reference to the importer, module, and the class name
 public JythonObjectFactory(PySystemState state, Class interfaceType, String moduleName, String className) {
     this.interfaceType = interfaceType;
     PyObject importer = state.getBuiltins().__getitem__(Py.newString("__import__"));
     PyObject module = importer.__call__(Py.newString(moduleName));
     klass = module.__getattr__(className);
     System.err.println("module=" + module + ",class=" + klass);
 }

 // This constructor passes through to the other constructor
 public JythonObjectFactory(Class interfaceType, String moduleName, String className) {
     this(new PySystemState(), interfaceType, moduleName, className);
 }

 // All of the followng methods return
 // a coerced Jython object based upon the pieces of information
 // that were passed into the factory. The differences are
 // between them are the number of arguments that can be passed
 // in as arguents to the object.

 public Object createObject() {
     return klass.__call__().__tojava__(interfaceType);
 }


 public Object createObject(Object arg1) {
     return klass.__call__(Py.java2py(arg1)).__tojava__(interfaceType);
 }

 public Object createObject(Object arg1, Object arg2) {
     return klass.__call__(Py.java2py(arg1), Py.java2py(arg2)).__tojava__(interfaceType);
 }

 public Object createObject(Object arg1, Object arg2, Object arg3)
 {
     return klass.__call__(Py.java2py(arg1), Py.java2py(arg2),
         Py.java2py(arg3)).__tojava__(interfaceType);
 }

 public Object createObject(Object args[], String keywords[]) {
     PyObject convertedArgs[] = new PyObject[args.length];
     for (int i = 0; i < args.length; i++) {
         convertedArgs[i] = Py.java2py(args[i]);
     }

     return klass.__call__(convertedArgs, keywords).__tojava__(interfaceType);
 }

 public Object createObject(Object... args) {
     return createObject(args, Py.NoKeywords);
 }

}