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

import com.liferay.util.servlet.*;
import com.liferay.util.*;

import javax.portlet.*;


/*
 View content, will forward to specified pages according to following information in order:
 RenderRequest.chainType
 portlet.preferences("chainType")
 if chain type set, then will load the handle class, else direct to "portlet.chain.blank"

 */
public class MenuEditConfigurationAction
    extends DefaultPortletAction {
    
    public MenuEditConfigurationAction() {
    }

    public void processAction(ActionMapping mapping, ActionForm form,
                              PortletConfig config, ActionRequest req,
                              ActionResponse res) throws Exception {

        
    }

    public ActionForward render(
        ActionMapping mapping, ActionForm form, PortletConfig config,
        RenderRequest req, RenderResponse res) throws Exception {

    	return mapping.findForward("portlet.mainmenu.edit_configuration");
    }

}
