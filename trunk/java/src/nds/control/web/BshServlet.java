package nds.control.web;

import java.io.*;
import java.util.Properties;

import javax.servlet.*;
import javax.servlet.http.*;
import bsh.*;
import org.python.util.PythonInterpreter;

import nds.control.util.EJBUtils;
import nds.control.util.ValueHolder;
import nds.util.*;
    /**
        This servlet should will only interact with programs, and only user who has permission
        to write ad_script table will has this permission to execute
	
	֧³ՂeanShellº̐ython
    */
public class BshServlet extends HttpServlet{

    private boolean isUserValid(HttpServletRequest request, HttpServletResponse response){
        HttpSession session = request.getSession(true);
        UserWebImpl user= ((UserWebImpl)WebUtils.getSessionContextManager(session).getActor(WebKeys.USER));
        return ((user.getPermission("SHELL") & nds.security.Directory.WRITE) ==nds.security.Directory.WRITE);
        
    }
    public void doPost(
            HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        doGet( request, response );
    }
    
    public void doGet(
            HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        response.setContentType("text/html; charset=UTF-8");
        boolean allowed=true;
        Properties props=EJBUtils.getApplicationConfig().getConfigurations("schema").getProperties();
        if(! "true".equalsIgnoreCase(props.getProperty("modify","true"))) allowed=false;
        
        if (!allowed || !isUserValid(request,response)){
            response.sendError(response.SC_UNAUTHORIZED,"Unauthorized.");
            PrintWriter out = response.getWriter();
            out.println("Unauthorized.");
            out.flush();
            return;
        }
        String script = request.getParameter("bsh.script");
        String client = request.getParameter("bsh.client");
        String output = request.getParameter("bsh.servlet.output");
        
        String captureOutErr =
                request.getParameter("bsh.servlet.captureOutErr");
        
        String scriptType= request.getParameter("bsh.type");
        if(nds.util.Validator.isNull(scriptType))scriptType="beanshell";
        boolean capture = false;
        if ( captureOutErr != null && captureOutErr.equalsIgnoreCase("true") )
            capture = true;

        Object scriptResult = null;
        Exception scriptError = null;
        StringBuffer scriptOutput = new StringBuffer();
        if ( script != null ) {
            try {
                if("beanshell".equalsIgnoreCase(scriptType))
                	scriptResult = evalBeanShellScript(
                        script, scriptOutput, capture, request, response );
                else if("python".equalsIgnoreCase(scriptType))
                	scriptResult = evalJythonScript(
                            script, scriptOutput, capture, request, response );
                	
            } catch ( Exception e ) {
                scriptError = e;
            }
        }
        if(scriptOutput!=null)
        	request.setAttribute("bsh.out", escape(String.valueOf(scriptOutput)));
        
        if(scriptResult!=null)
        	request.setAttribute("bsh.result",escape(String.valueOf(scriptResult)) );
        
        if(scriptError!=null)
        	request.setAttribute("bsh.exception", formatScriptResultHTML( 
				script, scriptResult, scriptError, scriptOutput ));
        
        getServletContext().getRequestDispatcher(WebKeys.NDS_URI+ "/shell/index.jsp").forward(request,response);
        
    }

    private String formatScriptResultHTML( 
    		String script, Object result, Exception error, 
    		StringBuffer scriptOutput ) 
    		throws IOException
    	{


    			String errString;

    			if ( error instanceof bsh.EvalError )
    			{
    				int lineNo = ((EvalError)error).getErrorLineNumber();
    				String msg = error.getMessage();
    				int contextLines = 4;
    				errString = escape(msg);
    				if ( lineNo > -1 )
    					errString += "<hr>" 
    						+ showScriptContextHTML( script, lineNo, contextLines );
    			} else
    				errString = escape( error.toString() );
    		
    			   return errString ;
    	
    	}    
    private Object evalJythonScript(
            String script, StringBuffer scriptOutput, boolean captureOutErr,
            HttpServletRequest request, HttpServletResponse response )
            throws EvalError
    {
        // Create a PrintStream to capture output
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream pout = new PrintStream( baos );

        // Create an interpreter instance with a null inputstream,
        // the capture out/err stream, non-interactive
        PythonInterpreter psh = new  PythonInterpreter( );

        // set up interpreter
        psh.set( "httpServletRequest", request );
        psh.set( "httpServletResponse", response );

        // Eval the text, gathering the return value or any error.
        Object result = "";
       
    	psh.setOut(pout);
    	psh.setErr(pout);
        try {
            // Eval the user text
            psh.exec( script );
            result= psh.get("retObj");
        } catch (Throwable t) {
        	t.printStackTrace(pout);
        }
        pout.flush();
        scriptOutput.append( baos.toString() );
        return result;
    }


    private Object evalBeanShellScript(
            String script, StringBuffer scriptOutput, boolean captureOutErr,
            HttpServletRequest request, HttpServletResponse response )
            throws EvalError
    {
        // Create a PrintStream to capture output
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream pout = new PrintStream( baos );

        // Create an interpreter instance with a null inputstream,
        // the capture out/err stream, non-interactive
        Interpreter bsh = new Interpreter( null, pout, pout, false );

        // set up interpreter
        bsh.set( "bsh.httpServletRequest", request );
        bsh.set( "bsh.httpServletResponse", response );

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
        }catch(Throwable t){
        	t.printStackTrace(pout);
        }
        finally {
            if ( captureOutErr ) {
                System.setOut( sout );
                System.setErr( serr );
            }
        }
        pout.flush();
        scriptOutput.append( baos.toString() );
        return result;
    }
    /*
	Show context number lines of string before and after target line.
	Add HTML formatting to bold the target line.
*/
private String showScriptContextHTML( String s, int lineNo, int context ) 
{
	StringBuffer sb = new StringBuffer();
	BufferedReader br = new BufferedReader( new StringReader(s) );

	int beginLine = Math.max( 1, lineNo-context );
	int endLine = lineNo + context;
	for( int i=1; i<=lineNo+context+1; i++ )
	{
		if ( i < beginLine )
		{
			try { 
				br.readLine();
			} catch ( IOException e ) { 
				throw new RuntimeException( e.toString() );
			}
			continue;
		}
		if ( i > endLine )
			break;

		String line;
		try { 
			line = br.readLine();
		} catch ( IOException e ) { 
			throw new RuntimeException( e.toString() );
		}

		if ( line == null ) 
			break;
		if ( i == lineNo )
			sb.append( "<font color=\"red\">"+i+": "+line +"</font><br/>" );
		else
			sb.append( i+": " +line +"<br/>" );
	}

	return sb.toString();
}

/**
 * Convert special characters to entities for XML output
 */
 public static String escape(String value) 
	{
		String search =	"&<>";
		String[] replace = {"&amp;", "&lt;", "&gt;"};

		StringBuffer buf = new StringBuffer();
	
		for (int i = 0;	i < value.length(); i++) 
		{
	    	char c = value.charAt(i);
	    	int	pos = search.indexOf(c);
	    	if (pos < 0)
				buf.append(c);
	    	else
				buf.append(replace[pos]);
		}

		return buf.toString();
 }



}


