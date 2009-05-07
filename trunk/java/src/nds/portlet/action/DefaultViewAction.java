package nds.portlet.action;

import javax.portlet.PortletConfig;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import nds.portlet.util.PortletUtils;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.liferay.util.ParamUtil;

/*
View content, will forward to specified pages according to following information in order:
 RenderRequest.chainType
 portlet.preferences("chainType")
 if chain type set, then will load the handle class, else direct to "portlet.chain.blank"

*/
public class DefaultViewAction extends DefaultPortletAction{
    private final static String DEFAULT_PAGE = "portlet.chain.blank";
    public DefaultViewAction() {
    }

    /* extends PortletAction*/
    public ActionForward render(
        ActionMapping mapping, ActionForm form, PortletConfig config,
        RenderRequest req, RenderResponse res) throws Exception {
        ActionForward af = null;
        try {
            logger.debug(PortletUtils.toString(req));
            String chainType = ParamUtil.getString(req, "chainType");
            if (chainType == null || chainType.length()==0) {
                chainType = req.getPreferences().getValue("chainType", null);
                logger.debug("get chainType from preference:" + chainType);
            }
            if (chainType != null && chainType.length() >0) {
                // let handler do it and return the forward page
                String fwd = ActionHandlerFactory.getInstance().getHandler(
                    chainType).handle(mapping, form, config, req, res,null);
                af = mapping.findForward(fwd);
            }
            else{
            	//defalult to Menu
            	/*af= mapping.findForward(ActionHandlerFactory.getInstance().getHandler(
                    "Menu").handle(mapping, form, config, req, res));*/
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

}
