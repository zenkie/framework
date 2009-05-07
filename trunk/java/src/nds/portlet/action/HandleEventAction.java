package nds.portlet.action;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import nds.control.event.DefaultWebEvent;
import nds.control.util.ValueHolder;
import nds.control.web.ClientControllerWebImpl;
import nds.control.web.RequestProcessor;
import nds.control.web.SessionContextManager;
import nds.control.web.UserWebImpl;
import nds.control.web.WebUtils;
import nds.util.StringUtils;
import nds.util.Tools;
import nds.util.WebKeys;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.liferay.util.ParamUtil;



/**
 * Handle chain event with following parameters:
*  "url" - request url, this must be only query part of URI
*  "eventtype" - event type of the request
 */
public class HandleEventAction extends DefaultPortletAction{
    private final static String DEFAULT_PAGE = "portlet.chain.blank";
    public HandleEventAction() {
    }
    /**
     * Test only
     */
    private void createDefaultUser( HttpServletRequest request)throws Exception {
        logger.debug("Create default user as root");
        request.setAttribute("j_password", "kkenshin");
        request.setAttribute("j_username", "root");
        RequestProcessor pro = (RequestProcessor) WebUtils.
            getServletContextManager().getActor(WebKeys.REQUEST_PROCESSOR);
        pro.processRequest(request,"nds.control.web.reqhandler.LoginRequestHandler");

    }
    public ActionForward execute(
                    ActionMapping mapping, ActionForm form, HttpServletRequest req,
                    HttpServletResponse res)throws Exception {
                  try{
                    logger.debug( "execute:"+ Tools.toString(req));
                    String nameSpace=ParamUtil.getString(req, "namespace");
                    String url = ParamUtil.getString(req, "url");
                    String chainType = ParamUtil.getString(req, "chainType");
                    HashMap map= new HashMap();
                    Tools.decodeURIQuery(url, map, StringUtils.ISO_8859_1);

                    HttpSession session =req.getSession(true);
                    SessionContextManager scm = WebUtils.getSessionContextManager(session);


                    UserWebImpl user = (UserWebImpl) scm.getActor(nds.util.WebKeys.USER);
                    if( user.isLoggedIn() ==false){
                        createDefaultUser(req);
                    }
                    DefaultWebEvent event = new DefaultWebEvent("CommandEvent");
            		if(user !=null && user.getSession()!=null)
            			event.put("nds.query.querysession",user.getSession());

                    event.setParameter("command", "HandleChain_"+chainType);
                    event.setParameter("operatorid", user.getUserId() + "");
                    event.setParameter("namespace", nameSpace);
                    for(Iterator it=map.keySet().iterator();it.hasNext();){
                        String n= (String) it.next();
                        event.setParameter(n, (String)map.get(n));
                    }

                    ClientControllerWebImpl controller = (ClientControllerWebImpl) WebUtils.
                        getServletContextManager().getActor(WebKeys.WEB_CONTROLLER);

                    // holder contains tree list
                    ValueHolder holder = null;
                    Vector nodes = null;
                    holder = controller.handleEvent(event);
                    req.setAttribute("nds.control.util.ValueHolder", holder);
                    String fwd =(String) holder.get("forward");
                    if(fwd==null) {
                        fwd = DEFAULT_PAGE;
                    }
                    logger.debug("Forward to " + fwd);
                    return mapping.findForward(fwd);

                    }
                    catch (Exception e) {
                        logger.error("Find error:", e);
                        return mapping.findForward(DEFAULT_PAGE);
                    }

    }

/*    public void processAction(
                            ActionMapping mapping, ActionForm form, PortletConfig config,
                            ActionRequest req, ActionResponse res)
                    throws Exception {
                    logger.debug("processAction:"+PortletUtils.toString(req));
                    try {
                        req.setAttribute("portlet.chain.iframe","true"); // used by ActionHandler to check where to redirect
                        String nameSpace=ParamUtil.getString(req, "namespace");
                        String url = ParamUtil.getString(req, "url");
                        logger.debug("url:" + url);
                        String chainType = ParamUtil.getString(req, "chainType");
                        //like this
                        //    /portal/layout?p_l_id=1&p_p_id=10&p_p_action=0&p_p_state=maximized&p_p_mode=view

                        String redirect= ParamUtil.getString(req, "redirect");
                        logger.debug("redirect:" + redirect);
                        URIBuilder ub= new URIBuilder(redirect, req.getCharacterEncoding());
                        ub.setQueryParam(nameSpace+"chainType", chainType);
                        ub.setQuery(url,nameSpace);
                        logger.debug("redirect to :" + ub.getURI().toString());
                        //req.getPortletSession().getPortletContext().getRequestDispatcher().include(req,res);

                        res.sendRedirect(ub.getURI().toString());
                    }
                    catch (Exception e) {
                        logger.error("Could not handle event:", e);
                        req.setAttribute("error", e);
                        SessionErrors.add(req, e.getClass().getName());

                        setForward(req, "portlet.chain.error");
                    }
                }

 */
 /*    public ActionForward render(
        ActionMapping mapping, ActionForm form, PortletConfig config,
        RenderRequest req, RenderResponse res) throws Exception {
	 logger.debug("Render: "+PortletUtils.toString(req));
//	 logger.debug("Unexpected into render:"+PortletUtils.toString(req));
//      return mapping.findForward(getForward(req, "portlet.chain.edit"));
        req.setAttribute("portlet.chain.iframe","true");

        PortletUtils.getHttpServletRequest(req).setAttribute("in_frame", "true");

        String nameSpace=ParamUtil.getString(req, "namespace");
        String url = ParamUtil.getString(req, "url");
        String chainType = ParamUtil.getString(req, "chainType");
        HashMap map= new HashMap();
        Tools.decodeURIQuery(url, map, StringUtils.ISO_8859_1);

        ActionForward af = null;
        try {

            if (chainType == null || chainType.length()==0) {
                chainType = req.getPreferences().getValue("chainType", null);
                logger.debug("get chainType from preference:" + chainType);
            }
            if (chainType != null && chainType.length() >0) {
                // let handler do it and return the forward page
                String fwd = ActionHandlerFactory.getInstance().getHandler(
                    chainType).handle(mapping, form, config, req, res,map);
                af = mapping.findForward(fwd);
                logger.debug("Forward to "+ fwd);
            }
            else{
                af = mapping.findForward(DEFAULT_PAGE);
                logger.debug("chainType not set");//, use Menu as default");
            }

            return af;
        }
        catch (Exception e) {
            logger.error("Find error:", e);
            return mapping.findForward(DEFAULT_PAGE);
        }

    }
*/

}
