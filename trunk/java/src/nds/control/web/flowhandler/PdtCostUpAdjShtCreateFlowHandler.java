/******************************************************************
*
*$RCSfile: PdtCostUpAdjShtCreateFlowHandler.java,v $ $Revision: 1.2 $ $Author: Administrator $ $Date: 2005/03/30 13:13:56 $
*
********************************************************************/
package nds.control.web.flowhandler;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import nds.log.Logger;
import nds.log.LoggerManager;
import nds.util.NDSException;

public class PdtCostUpAdjShtCreateFlowHandler extends FlowHandler {
    private static Logger logger=LoggerManager.getInstance().getLogger(PdtCostUpAdjShtCreateFlowHandler.class.getName());

    public PdtCostUpAdjShtCreateFlowHandler() {}
    public void init(ServletContext context) {
    }

    /**
     */
    public String processFlow(HttpServletRequest request) throws NDSException {
        return "/popup_close.jsp";
    }
    public String toString() {
        return name;
    }
    private final static String name="PdtCostUpAdjShtCreateFlowHandler";
}