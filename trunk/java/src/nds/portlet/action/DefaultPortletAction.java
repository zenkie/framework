package nds.portlet.action;
import nds.log.Logger;
import nds.log.LoggerManager;

import com.liferay.portal.struts.PortletAction;

public class DefaultPortletAction extends PortletAction{
	  public static final String COMMON_ERROR = "/common/error.jsp";
	  
    protected Logger logger= LoggerManager.getInstance().getLogger(this.getClass().getName());

    public DefaultPortletAction() {
    }


}
