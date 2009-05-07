/*
 * Agile Control Technologies Ltd,. CO.
 * http://www.agileControl.com
 */
package  nds.ws;

import nds.log.Logger;
import nds.log.LoggerManager;
import nds.security.User;

import org.codehaus.xfire.MessageContext;
import org.codehaus.xfire.transport.Session;
import org.codehaus.xfire.transport.http.XFireHttpSession;

import nds.control.web.*;
import nds.control.util.*;
import nds.util.MessagesHolder;
import nds.util.Tools;
import nds.util.WebKeys;
import nds.web.SessionController;
import nds.control.event.DefaultWebEvent;
/**
 * Work as web service
 * @author yfzhu@agilecontrol.com
 */

public class DocComponent {
	private static Logger logger= LoggerManager.getInstance().getLogger(DocComponent.class.getName());
	
    private Doc invalidUserDoc;
    
    public DocComponent(){
    	invalidUserDoc=new Doc(-1,"","",Doc.DOCTYPE_RESPONSE, Doc.CODE_INVALID_SESSION, "@invalid-session@");
    }
	
/*    public Object onCall(UMOEventContext context) throws Exception
    {
        super.onCall(context);
        return context.getTransformedMessage();
    }*/
    public  String echo(String type){
    	return "You called " + type;
    }
    
    private Doc execute(String command,String docNO, MessageContext context){
    	int userId=-1;
    	Session session=context.getSession();
    	String sessionId="";
    	UserWebImpl userWeb=null;
    	if(session !=null && session instanceof XFireHttpSession){
    		XFireHttpSession s=((XFireHttpSession)session);
    		userWeb= ((UserWebImpl)WebUtils.getSessionContextManager(
    				s.getSession()).getActor(WebKeys.USER));
    		if(userWeb!=null) userId= userWeb.getUserId();
    		sessionId=s.getSession().getId();
    	}

    	Doc doc=null;
    	if(userId ==-1){
    		// not a valid session
    		doc= invalidUserDoc;
    	}else{
	    	DefaultWebEvent event=new DefaultWebEvent("CommandEvent");
	        event.setParameter("operatorid", userId+"");
	    	event.setParameter("docno", docNO );
	    	event.setParameter("command", command);
	    	event.setParameter("sessionid", sessionId);
	    	event.put("nds.query.querysession",userWeb.getSession());	    	
	    	try{
		    	ClientControllerWebImpl controller=(ClientControllerWebImpl)
					WebUtils.getServletContextManager().getActor(WebKeys.WEB_CONTROLLER);
		    	ValueHolder holder = (ValueHolder)controller.handleEvent(event);
		    	
		    	doc=(Doc) holder.get("doc");
		    	doc.setReturnMessage(MessagesHolder.getInstance().translateMessage(doc.getReturnMessage(),
		    			userWeb.getLocale()) );
	    	}catch(Throwable t){
	    		logger.error("Fail to execute doc "+ docNO+" by user "+ userId, t);
	    		doc=new Doc(-1,"","",Doc.DOCTYPE_RESPONSE,Doc.CODE_DOC_INTERNAL_ERROR,
	    				MessagesHolder.getInstance().translateMessage("@exception@:"+
	    						nds.util.StringUtils.getRootCause(t).getMessage(), userWeb.getLocale()) );
	    	}
    	}
    	return doc;
    }
    public Doc finish(String docNO,MessageContext context){
    	return execute("DocFinish", docNO, context);
    }
    public Doc delete(String docNO,MessageContext context){
    	return execute("DocDelete", docNO, context);
    }
    
	public Doc prepare(String docNO,MessageContext context){
		return execute("DocPrepare", docNO, context);
	}
}
