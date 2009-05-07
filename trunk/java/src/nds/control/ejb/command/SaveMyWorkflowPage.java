package nds.control.ejb.command;

import java.io.File;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;



import nds.control.ejb.Command;
import nds.control.event.DefaultWebEvent;
import nds.control.event.NDSEventException;
import nds.control.util.SecurityUtils;
import nds.control.util.ValueHolder;
import nds.query.*;
import nds.schema.*;
import nds.security.User;
import nds.util.Configurations;
import nds.util.NDSException;
import nds.util.StringUtils;
import nds.util.Tools;
import nds.util.Validator;


/**
 * Save personal workflow page to person's home directory
 * as ${export.root.nds}/$client/$username/workflow/wfmy.html
 *
 */

public class SaveMyWorkflowPage  extends Command {
	/**
	 * 
	 * @param event params:
	 *   clob_content - content of page
	 */
  public ValueHolder execute(DefaultWebEvent event) throws RemoteException, NDSException {
  	User user= helper.getOperator(event);
	String rawContent= (String)event.getParameterValue("clob_content");
	String content= com.liferay.util.JS.decodeURIComponent( rawContent);

	Configurations conf=(Configurations)nds.control.web.WebUtils.getServletContextManager().getActor(nds.util.WebKeys.CONFIGURATIONS);
	String exportRootPath=conf.getProperty("export.root.nds","/aic/home");
	
	String filePath =exportRootPath + File.separator+ 
	user.getClientDomain()+File.separator+ user.getName()+File.separator+"workflow";
	File f= new File(filePath);
	if(!f.exists())f.mkdirs();
	File wfFile=new File(filePath+File.separator+"wfmy.html");
	try{
	Tools.writeFile(wfFile.getAbsolutePath(), content, "UTF-8");
	}catch(Exception e){
		throw new NDSException("Fail to write file", e);
	}
	    ValueHolder v=new ValueHolder();
	    v.put("message", "@finished@");
	    return v;
  }
}