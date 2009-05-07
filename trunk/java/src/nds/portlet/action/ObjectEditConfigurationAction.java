package nds.portlet.action;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;



import nds.query.QueryEngine;
import nds.query.QueryUtils;
import nds.util.Tools;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;


import com.liferay.portal.struts.PortletAction;
import com.liferay.portal.util.Constants;
import com.liferay.portlet.PortletPreferencesFactory;
import com.liferay.util.ParamUtil;
import com.liferay.util.servlet.SessionMessages;

import javax.portlet.*;

/*
  */
public class ObjectEditConfigurationAction
    extends DefaultPortletAction {
    
    public ObjectEditConfigurationAction() {
    }
    /**
     * Got name of config and store id of config records
     */
    public void processAction(ActionMapping mapping, ActionForm form,
                              PortletConfig config, ActionRequest req,
                              ActionResponse res) throws Exception {

        //logger.debug("processAction:"+PortletUtils.toString(req));

    		String cmd = ParamUtil.getString(req, Constants.CMD);

    		if (!cmd.equals(Constants.UPDATE)) {
    			return;
    		}
    		String uiConfig = ParamUtil.getString(req, "uiConfig");
    		uiConfig =String.valueOf(Tools.getInt( QueryEngine.getInstance().
    				doQueryOne("select id from ad_objuiconf where name="+ 
    						QueryUtils.TO_STRING(uiConfig)+" and isactive='Y'" ),-1));

    		
    		String portletResource = ParamUtil.getString(req, "portletResource");

    		PortletPreferences prefs = PortletPreferencesFactory.getPortletSetup(
    			req, portletResource, true, true);

    		prefs.setValue("uiConfig", uiConfig);

    		prefs.store();

    		SessionMessages.add(req, config.getPortletName() + ".doConfigure");

    }

    public ActionForward render(
        ActionMapping mapping, ActionForm form, PortletConfig config,
        RenderRequest req, RenderResponse res) throws Exception {

    	return mapping.findForward("portlet.ndsobj.edit_configuration");
    }

}
