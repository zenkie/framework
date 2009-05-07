package nds.portlet.action;

import com.liferay.portal.model.Layout;
import com.liferay.portal.struts.PortletAction;
import com.liferay.portal.util.Constants;
import com.liferay.portal.util.WebKeys;
import com.liferay.util.InstancePool;
import com.liferay.util.ParamUtil;
import com.liferay.util.Validator;
import com.liferay.util.servlet.SessionErrors;

import java.util.List;

import javax.portlet.PortletConfig;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import javax.servlet.jsp.PageContext;
import com.liferay.portal.struts.PortletAction;
import com.liferay.portal.util.Constants;
import com.liferay.portal.util.PortalUtil;
import com.liferay.util.FileUtil;
import com.liferay.util.ParamUtil;
import com.liferay.util.servlet.SessionErrors;
import com.liferay.util.servlet.SessionMessages;
import com.liferay.util.servlet.UploadException;
import com.liferay.util.servlet.UploadPortletRequest;

import java.io.File;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletConfig;

import javax.servlet.jsp.PageContext;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import nds.log.Logger;
import nds.log.LoggerManager;

import nds.portlet.util.PortletUtils;
import nds.query.QueryEngine;
import nds.query.SPResult;
import nds.util.*;
import nds.control.util.ValueHolder;
import nds.control.web.*;
import nds.control.event.*;
import java.util.*;
/**
 * Set out state and assignee, or cancel them
 */
public class AuditSetupAction extends PortletAction {
	private static Logger logger= LoggerManager.getInstance().getLogger(AuditAction.class.getName());	

	public void processAction(
			ActionMapping mapping, ActionForm form, PortletConfig config,
			ActionRequest req, ActionResponse res)
		throws Exception {

			String cmd = ParamUtil.getString(req, Constants.CMD);

			if (cmd.equals(Constants.UPDATE)) {
				//save
				try{
			        SessionContextManager scmanager= WebUtils.getSessionContextManager(PortletUtils.getHttpServletRequest(req).getSession());
					UserWebImpl userWeb=((UserWebImpl)scmanager.getActor(nds.util.WebKeys.USER));
					DefaultWebEvent event=PortletUtils.createEvent(req, "CommandEvent");
					event.setParameter("command","AuditSetup");
			        ClientControllerWebImpl controller=(ClientControllerWebImpl)WebUtils.getServletContextManager().getActor(nds.util.WebKeys.WEB_CONTROLLER);
			        ValueHolder vh=controller.handleEvent(event);

			        logger.debug((String)vh.get("message"));
			        
			        SessionMessages.add(req, "audit_setup", vh.get("message"));
			        
				}catch (Throwable e) {
					logger.error("found error",e);
					req.setAttribute(PageContext.EXCEPTION, e);
					SessionErrors.add(req, NDSException.class.getName());
				}
				
			}
			setForward(req, "portlet.ndsaudit.setup");
	}

}