package nds.control.web.binhandler;

import java.util.ServiceLoader;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nds.log.Logger;
import nds.log.LoggerManager;
import argparser.*;
import org.json.*;
import nds.web.shell.*;

/**
 * Handle command line request
 * 
 * @author yfzhu
 *
 */
public class Shell implements BinaryHandler{
	private Logger logger= LoggerManager.getInstance().getLogger(Shell.class.getName());
	/**
	 * Scan package of nds.web.shell and load all Command into memory to construct valid command list
	 */
	public void init(ServletContext context){
		
	}
	/**
	 * Find command according to cmdline
	 * @param cmdline first none empty string seperated with space will be command 
	 * @return
	 * @throws Exception
	 */
	private ShellCmd getCommand(String cmdline)throws Exception{
		return null;
	}
	private void showHelpPage(HttpServletRequest request,HttpServletResponse  response) throws Exception{
		ServiceLoader.load(Shell.class);
	}
	/**
	 * 
	 * 
	 */
	public void process(HttpServletRequest request,HttpServletResponse  response)  throws Exception{
		String cmdline= request.getParameter("cmdline");
		
		String env= request.getParameter("env");
		JSONObject envObj;
		if(nds.util.Validator.isNotNull(env))envObj= new JSONObject(env);
		else envObj=new JSONObject();
		envObj.put("request", request);
		envObj.put("response", response);
		
		
		ShellCmd cmd= getCommand(cmdline);
		if(cmd==null){
			showHelpPage(request,response);
		}else{
			cmd.execute(cmdline, envObj);
		}
		
		ArgParser parser = new ArgParser("");
	    StringHolder cmdParam = new StringHolder();
	    StringHolder tableParam = new StringHolder();
	    BooleanHolder debug = new BooleanHolder();
	    BooleanHolder forceParam = new BooleanHolder();
	    
		parser.addOption ("-c %s #command to execute", cmdParam);
		parser.addOption ("-t %s #table name or description", tableParam);
		parser.addOption ("-f %v #force without prompt", forceParam);
	}
	
	public static void main(String[] args){
	    // create holder objects for storing results ...
		String param="-theta 7.8 -debug -file /ai/lloyd/bar aaa bbb ccc ddd";
		args=param.split(" "); 
	    DoubleHolder theta = new DoubleHolder();
	    StringHolder fileName = new StringHolder();
	    BooleanHolder debug = new BooleanHolder();
	    BooleanHolder vf = new BooleanHolder();
	    // create the parser and specify the allowed options ...
	 
	    ArgParser parser = new ArgParser("");
	    parser.addOption ("-theta %f #theta value (in degrees)", theta); 
	    parser.addOption ("-file %s #name of the operating file", fileName);
	    parser.addOption ("-debug %v #enables display of debugging info", debug);
	    parser.addOption ("-v,--verbose %v #print lots of info",vf);

	    // match the arguments ...
	 
	    String[] unmatched =
	        parser.matchAllArgs (args, 0, parser.EXIT_ON_ERROR);
	    
	    for (int i = 0; i < unmatched.length; i++){ 
	    	System.out.println ("unmatched["+i+"]=" + unmatched[i]);
	    }

	    // and print out the values

	    System.out.println ("theta=" + theta.value);
	    System.out.println ("fileName=" + fileName.value);
	    System.out.println ("debug=" + debug.value);
	    System.out.println ("vf=" + vf.value);
	    
	}
}
