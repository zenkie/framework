package nds.control.ejb.command;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.rmi.RemoteException;

import nds.control.ejb.Command;
import nds.control.event.DefaultWebEvent;
import nds.control.util.ValueHolder;
import nds.util.*;
import bsh.EvalError;
import bsh.Interpreter;

/**
 * Execute bean shell script, script result will be put in
 * ValueHolder's "result"
 */
public class BshScript extends Command{

    public ValueHolder execute(DefaultWebEvent event) throws NDSException ,RemoteException{
        StringBuffer out=new StringBuffer();
        ValueHolder vh=new ValueHolder();
        int code=0;
        String script =(String) event.getParameterValue("bsh.script",true);
        logger.debug("bsh.script:\n"+script) ;
        String client =(String) event.getParameterValue("bsh.client",true);
//        String output = (String) event.getParameterValue("bsh.servlet.output");
        String captureOutErr =
                (String) event.getParameterValue("bsh.servlet.captureOutErr",true);
        boolean capture = false;
        if ( captureOutErr != null && captureOutErr.equalsIgnoreCase("true") )
            capture = true;

        Object scriptResult = null;
        Exception scriptError = null;
        StringBuffer scriptOutput = new StringBuffer();
        if ( script != null ) {
            try {
                scriptResult = BshScriptUtils.evalScript(
                        script, scriptOutput, capture , null);
            } catch ( Exception e ) {
                scriptError = e;
            }
        }


        if ( scriptError != null ){
            code=500;
            out.append( "Script Error:\n"+scriptError );
        }else
            out.append( scriptOutput.toString() );

        vh.put("message", out.toString() );
        vh.put("code", new Integer(code));
        return vh;
    }

    
}