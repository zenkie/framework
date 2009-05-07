package nds.control.ejb.command.test;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import bsh.EvalError;
import bsh.Interpreter;

public class BshScriptTest {

    public BshScriptTest() {
    }
    public static void main(String[] args) {
        BshScriptTest bshScriptTest1 = new BshScriptTest();

    }
    public Object evalScript(
            String script, StringBuffer scriptOutput, boolean captureOutErr)
            throws EvalError
    {
        // Create a PrintStream to capture output
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream pout = new PrintStream( baos );

        // Create an interpreter instance with a null inputstream,
        // the capture out/err stream, non-interactive
        Interpreter bsh = new Interpreter( null, pout, pout, false );


        // Eval the text, gathering the return value or any error.
        Object result = null;
        String error = null;
        try {
            // Eval the user text
            result = bsh.eval( script );
        } finally {
        }
        pout.flush();
        scriptOutput.append( baos.toString() );
        return result;
    }
}