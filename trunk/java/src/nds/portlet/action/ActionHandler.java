package nds.portlet.action;

import java.util.HashMap;

import javax.portlet.PortletConfig;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import nds.log.Logger;
import nds.log.LoggerManager;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;

import com.liferay.util.ParamUtil;

/**
 * Called by DefaultPortletAction to handle different screen request
 */
public abstract class ActionHandler {
    protected Logger logger= LoggerManager.getInstance().getLogger(getClass().getName());
    /**
     * If req has parameter "iframe" set then the return string would be actionPath+".iframe"
     * For instance, action path= "portlet.chain.view.menu", then the return path will be
     * "portlet.chain.view.menu.iframe"
     * @param actionPath String
     * @param req RenderRequest
     * @param res RenderResponse
     * @return String
     */
    protected String getChainForward(String actionPath, RenderRequest req, RenderResponse res){

        if ( "true".equals(req.getParameter("iframe")) 
        	|| "true".equals(req.getAttribute("portlet.chain.iframe"))){
            return actionPath+".iframe";
        }
        return actionPath;
    }
    protected String getParamString(String name, RenderRequest req,HashMap paramMap){
    	
    	String s=null;
    	if(paramMap!=null){
    		Object o=  paramMap.get(name);
    		if(o!=null) s=o.toString();
    	}else
    		s=ParamUtil.getString(req,name);
    	return s;
    }
	/**
	* @param paramMap take predence over parameters in req
	*/
    public abstract String handle(
                    ActionMapping mapping, ActionForm form, PortletConfig config,
                    RenderRequest req, RenderResponse res, HashMap paramMap)throws Exception ;

}
