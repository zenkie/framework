package nds.portlet.action;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;


import com.liferay.portal.struts.PortletAction;
import com.liferay.portal.util.Constants;
import com.liferay.portlet.PortletPreferencesFactory;
import com.liferay.util.ParamUtil;
import com.liferay.util.servlet.SessionMessages;

import javax.portlet.*;
import nds.query.*;
import nds.util.Tools;
/*
  */
public class ListEditConfigurationAction
    extends DefaultPortletAction {
    
    public ListEditConfigurationAction() {
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
    		String normalStateUIConfig = ParamUtil.getString(req, "normalStateUIConfig");
    		String maxStateUIConfig = ParamUtil.getString(req, "maxStateUIConfig");
    		String dataConfig = ParamUtil.getString(req, "dataConfig");
    		
    		normalStateUIConfig =String.valueOf(Tools.getInt( QueryEngine.getInstance().
    				doQueryOne("select id from ad_listuiconf where name="+ 
    						QueryUtils.TO_STRING(normalStateUIConfig)+" and isactive='Y'" ),-1));
    		
    		maxStateUIConfig =String.valueOf(Tools.getInt( QueryEngine.getInstance().doQueryOne
    				("select id from ad_listuiconf where name="+ 
    						QueryUtils.TO_STRING(maxStateUIConfig)+" and isactive='Y'" ),-1));

    		dataConfig =String.valueOf(Tools.getInt( QueryEngine.getInstance().
    				doQueryOne("select id from ad_listdataconf where name="+ 
    						QueryUtils.TO_STRING(dataConfig)+" and isactive='Y'" ),-1));

    		String portletResource = ParamUtil.getString(req, "portletResource");

    		PortletPreferences prefs = PortletPreferencesFactory.getPortletSetup(
    			req, portletResource, true, true);
    		prefs.setValue("normalStateUIConfig", normalStateUIConfig);
    		prefs.setValue("maxStateUIConfig", maxStateUIConfig);
    		prefs.setValue("dataConfig", dataConfig);

    		prefs.store();
    		logger.debug("portletResource="+portletResource+", normalStateUIConfig="+normalStateUIConfig+
    				"maxStateUIConfig="+maxStateUIConfig+",dataConfig="+dataConfig+ ",prefs="+prefs);

    		SessionMessages.add(req, config.getPortletName() + ".doConfigure");

    }

    public ActionForward render(
        ActionMapping mapping, ActionForm form, PortletConfig config,
        RenderRequest req, RenderResponse res) throws Exception {

    	return mapping.findForward("portlet.ndslist.edit_configuration");
    }

}
