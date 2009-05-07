/******************************************************************
*
*$RCSfile: CommandFlowHandler.java,v $ $Revision: 1.3 $ $Author: Administrator $ $Date: 2005/04/27 03:25:31 $
*
*$Log: CommandFlowHandler.java,v $
*Revision 1.3  2005/04/27 03:25:31  Administrator
*no message
*
*Revision 1.2  2005/03/30 13:13:55  Administrator
*no message
*
*Revision 1.1.1.1  2005/03/15 11:23:16  Administrator
*init
*
*Revision 1.2  2003/03/30 08:11:50  yfzhu
*Updated before subtotal added
*
*Revision 1.1.1.1  2002/11/29 02:50:34  yfzhu
*Active POS
*
*Revision 1.1.1.1  2002/01/08 03:40:26  Administrator
*My new CVS module.
*
*Revision 1.7  2002/01/04 01:43:22  yfzhu
*no message
*
*Revision 1.6  2001/12/09 03:43:32  yfzhu
*no message
*
*Revision 1.5  2001/11/29 13:13:14  yfzhu
*no message
*
*Revision 1.4  2001/11/20 22:36:09  yfzhu
*no message
*
*Revision 1.3  2001/11/16 11:42:40  yfzhu
*no message
*
*Revision 1.2  2001/11/14 23:33:20  yfzhu
*no message
*
*Revision 1.1  2001/11/13 22:37:14  yfzhu
*no message
*
*Revision 1.2  2001/11/07 20:58:46  yfzhu
*no message
*
*
********************************************************************/
package nds.control.web.flowhandler;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import nds.control.web.URLMapping;
import nds.control.web.URLMappingManager;
import nds.control.web.WebUtils;
import nds.log.Logger;
import nds.log.LoggerManager;
import nds.util.NDSException;
import nds.util.StringUtils;
import nds.util.*;

public class CommandFlowHandler extends FlowHandler {
    private static Logger logger=LoggerManager.getInstance().getLogger(CommandFlowHandler.class.getName());

    private String infoScreen=null; // the default screen working as default result page

    public CommandFlowHandler() {}
    public void init(ServletContext context) {
        URLMappingManager umanager=(URLMappingManager)WebUtils.getServletContextManager().getActor(WebKeys.URL_MANAGER);
        URLMapping mapping=umanager.getMappingByScreen("INFO");
        if( mapping !=null)
            infoScreen= mapping.getURL();
        if( infoScreen ==null) {
            logger.error("INFO screen not found in URLMappingManager, which working as default result page for those without next-screen\n\r"+
                         " Better create a URLMapping in URLMappingManager with screen named \"INFO\"");
        }

    }
    
    /**
     */
    public String processFlow(HttpServletRequest request) throws NDSException {
    	
        String nextScreen=getNextScreen(request);
        if( Validator.isNotNull(nextScreen)){
            logger.debug("Next screen used:"+nextScreen);
            return nextScreen;
        }
        String command= request.getParameter("command");    
        String formRequest=request.getParameter("formRequest");
        if( Validator.isNotNull(formRequest)){
        	String ref_by_table=request.getParameter("ref_by_table");
        	if (Tools.getInt(ref_by_table, -1)==-1){
        		// object handling on the main table record
        		if(isTailedWith(command, "Delete" ) && !isTailedWith(command, "ListDelete" )){
        	    	// delete action, direct to infor screen
        			// only object deletion, not multiple object deletion
        			return WebKeys.NDS_URI+infoScreen;
        		}
        	}
        	
        	return formRequest;
        }	     
        if(isTailedWith(command, "ListDelete" )
        		||isTailedWith(command, "ListSubmit" ) ){
        	return WebKeys.NDS_URI+"/objext/list.jsp";
        }
        
        
        return WebKeys.NDS_URI+infoScreen;
/*        if( isTailedWith(command, "ItemModify" )){
            // in item modify page, there will be formRequest to direct page
            String formRequest=request.getParameter("formRequest");
            if(formRequest !=null && !formRequest.trim().equals("")){
                return formRequest;
            }else return WebKeys.NDS_URI+"/objext/sheet_item.jsp";
        }else if(isTailedWith(command, "ItemDelete" )
                ||isTailedWith(command, "ItemCreate" )
                ||isTailedWith(command, "ShtCreate" )
                //||isTailedWith(command, "ShtModify")
                ) {

            return WebKeys.NDS_URI+"/objext/sheet_item.jsp";
        } else if("UserUpdate".equals(command)) {
            logger.debug("UserModify command got, return infor page:"+ infoScreen);
            return WebKeys.NDS_URI+infoScreen;
        } else if("PickShtSubmit".equals(command)) {
            logger.debug("PickShtSubmit command got, return infor page:"+ infoScreen);
            return WebKeys.NDS_URI+infoScreen;
        }else if (isTailedWith(command,"Modify")){
        	return WebKeys.NDS_URI+"/object/object.jsp";
        }

        return WebKeys.NDS_URI+"/objext/sheet_list.jsp";
*/

    }
    private boolean isTailedWith(String str, String tail) {
        return StringUtils.isTailedWith(str.toUpperCase(),tail.toUpperCase());
    }
    public String toString() {
        return name;
    }
    private final static String name="CommandFlowHandler";
}
