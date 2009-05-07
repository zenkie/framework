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

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

//import nds.olap.OLAPUtils;
import nds.portlet.util.PortletUtils;
import nds.util.*;
import nds.control.web.*;
import nds.control.event.*;
import java.util.*;
/**
 Refresh the portlet, will recreate cache file
 *
 */
public class GraphRefreshAction extends PortletAction {

	public ActionForward render(
			ActionMapping mapping, ActionForm form, PortletConfig config,
			RenderRequest req, RenderResponse res)
		throws Exception {

		try {
			Layout layout = (Layout) req.getAttribute(WebKeys.LAYOUT);

			String cmd = ParamUtil.getString(req, Constants.CMD);

			try {
				SessionContextManager scmanager = WebUtils
						.getSessionContextManager(PortletUtils
								.getHttpServletRequest(req).getSession());
				UserWebImpl userWeb = ((UserWebImpl) scmanager
						.getActor(nds.util.WebKeys.USER));
				
				String layoutId = null;
				if (layout != null) {
					layoutId = layout.getLayoutId();
				}
				/**
				 * User layoutId+"."+ portletName as file name of cached report, so 
				 * each layout can have different report of the same portlet.
				 */
				String reportName= layoutId+"."+ config.getPortletName();
				
				Properties props = userWeb.getPreferenceValues(reportName, false);
				int queryId = Tools
						.getInt(props.getProperty("query", "-1"), -1);
				// only when query id changed, will reload the query
				if (queryId != -1) {
					// 	load query into cache file directly
/*					OLAPUtils.createQueryResultToFile(queryId, userWeb
							.getUserId(), reportName, userWeb.getUserName(), userWeb.getClientDomain());*/
				}
				
				return mapping.findForward("portlet.ndsgraph.view");
			} catch (Exception e) {
				req.setAttribute(PageContext.EXCEPTION, e);
				SessionErrors.add(req, NDSException.class.getName());
			}

			return mapping.findForward("portlet.ndsgraph.setup");
		}
		catch (Exception e) {
			req.setAttribute(PageContext.EXCEPTION, e);

			return mapping.findForward(Constants.COMMON_ERROR);
		}
	}

}