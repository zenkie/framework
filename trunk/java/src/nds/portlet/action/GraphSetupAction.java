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

import nds.log.Logger;
import nds.log.LoggerManager;
//import nds.olap.OLAPUtils;
import nds.portlet.util.PortletUtils;
import nds.query.QueryEngine;
import nds.query.SPResult;
import nds.util.*;
import nds.control.web.*;
import nds.control.event.*;
import java.util.*;
/**
 *
 */
public class GraphSetupAction extends PortletAction {
	private static Logger logger= LoggerManager.getInstance().getLogger(GraphSetupAction.class.getName());	

	public ActionForward render(
			ActionMapping mapping, ActionForm form, PortletConfig config,
			RenderRequest req, RenderResponse res)
		throws Exception {

		try {
			Layout layout = (Layout)req.getAttribute(WebKeys.LAYOUT);
			String layoutId = null;
			if (layout != null) {
				layoutId = layout.getLayoutId();
			}
			/**
			 * User layoutId+"."+ portletName as file name of cached report, so 
			 * each layout can have different report of the same portlet.
			 */
			String reportName= layoutId+"."+ config.getPortletName();
			String cmd = ParamUtil.getString(req, Constants.CMD);

			if (cmd.equals(Constants.UPDATE)) {
				int queryId =Tools.getInt(req.getParameter("query"), -1);
				String queue = ParamUtil.getString(req, "queue");
				String title = ParamUtil.getString(req, "title");
				boolean showBorders = ParamUtil.get(req, "show_borders", true);
				
				//save
				try{
					if(queryId==-1) throw new NDSException("@query-not-set@");
					
			        SessionContextManager scmanager= WebUtils.getSessionContextManager(PortletUtils.getHttpServletRequest(req).getSession());
					UserWebImpl userWeb=((UserWebImpl)scmanager.getActor(nds.util.WebKeys.USER));
					Properties props= userWeb.getPreferenceValues(reportName,false);
					DefaultWebEvent event=new DefaultWebEvent("CommandEvent");
					event.setParameter("command","SavePreference");
					event.setParameter("operatorid",""+userWeb.getUserId());
					// useless, since call ClientControllerWebImpl directly 
					//event.setParameter("request-handler","nds.control.web.reqhandler.SavePreferenceRequestHandler");
					event.setParameter("userid",""+userWeb.getUserId());
					event.setParameter("module", reportName);
					event.setParameter("preferences","query,show_borders,title");
					event.setParameter("query", queryId+"");
					//event.setParameter("schedule", schedule);
					event.setParameter("title", title);
					event.setParameter("show_borders", showBorders?"1":"0");
			        ClientControllerWebImpl controller=(ClientControllerWebImpl)WebUtils.getServletContextManager().getActor(nds.util.WebKeys.WEB_CONTROLLER);
			        controller.handleEvent(event);

			        // Invalidate cache for module preference
			        userWeb.invalidatePreferences( reportName);
			        // only when query id changed, will reload the query
			        if(queryId!=Tools.getInt(props.getProperty("query","-1"),-1)){
			        	// 	load query into cache file directly
/*						OLAPUtils.createQueryResultToFile(queryId, userWeb
								.getUserId(),reportName, userWeb.getUserName(), userWeb.getClientDomain());*/

			        }
			        
			        // create ad_pinstance into specified queue
			        ArrayList list =new ArrayList();
			        list.add(new Integer(userWeb.getUserId()));
			        list.add(queue);
			        list.add(reportName);
			        list.add("nds.process.RefreshReport");
			        // pinstance_para
			        list.add("portletName="+reportName+";queryId="+queryId);
			        ArrayList resu=new ArrayList();
			        resu.add(Integer.class);
			        QueryEngine.getInstance().executeFunction("ad_pinstance_create", list,resu );
			        
			        /*list.add("delete from ad_pinstance where ad_user_id="+ userWeb.getUserId()+" and record_no='"+ config.getPortletName()+
			        		"' and ad_process_id in (select id from ad_process where classname='nds.process.RefreshReport')");
			        if(Validator.isNotNull(queue)){
			        	// only when queue is not null will the pinstance be created 
			        	list.add( "insert into ad_pinstance(id,ad_client_id,ad_org_id,ad_processqueue_id,ad_process_id,ad_user_id,"+
			        			"state,record_no,ownerid,creationdate, modifierid,modifieddate,isactive)"+
			        			"select get_sequences('ad_pinstance'), u.ad_client_id,u.ad_org_id,q.id,p.id,u.id,'U','"+config.getPortletName()+
								"',u.id,sysdate,u.id,sysdate,'Y' from users u, ad_process p, ad_processqueue q where u.id="+ userWeb.getUserId()+
								" and p.classname='nds.process.RefreshReport' and q.name='"+queue+"'");
			        }
			        QueryEngine.getInstance().doUpdate(list);
			        */
					return mapping.findForward("portlet.ndsgraph.view");
				}catch (Exception e) {
					logger.error("found error",e);
					req.setAttribute(PageContext.EXCEPTION, e);
					SessionErrors.add(req, NDSException.class.getName());
				}
				
			}
			return mapping.findForward("portlet.ndsgraph.setup");
		}
		catch (Exception e) {
			logger.error("found error",e);
			req.setAttribute(PageContext.EXCEPTION, e);

			return mapping.findForward(Constants.COMMON_ERROR);
		}
	}

}