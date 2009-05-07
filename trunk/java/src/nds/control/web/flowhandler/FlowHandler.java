/******************************************************************
*
*$RCSfile: FlowHandler.java,v $ $Revision: 1.2 $ $Author: Administrator $ $Date: 2005/03/30 13:13:56 $
*
*$Log: FlowHandler.java,v $
*Revision 1.2  2005/03/30 13:13:56  Administrator
*no message
*
*Revision 1.1.1.1  2005/03/15 11:23:16  Administrator
*init
*
*Revision 1.1.1.1  2002/11/29 02:50:34  yfzhu
*Active POS
*
*Revision 1.3  2001/11/20 22:36:09  yfzhu
*no message
*
*Revision 1.2  2001/11/07 20:58:46  yfzhu
*no message
*
*
********************************************************************/
//Source file: F:\\work2\\tmp\\nds\\control\\web\\flowhandler\\FlowHandler.java

package nds.control.web.flowhandler;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import nds.control.util.ValueHolder;
import nds.util.NDSException;
import nds.util.Validator;
import nds.util.WebKeys;

/**
 * This class is the base interface to flow handlers on the
 * web tier.
 *
*/
public abstract class FlowHandler implements java.io.Serializable {

    public abstract String processFlow(HttpServletRequest request) throws NDSException;
    public abstract void init(ServletContext context);
    /**
     * Precedence: ValueHolder/Attribute/Parameter
     * @param req
     * @return
     * @since 2.0
     */
    protected String getNextScreen(HttpServletRequest req){
    	String ns=null;
    	Object obj= req.getAttribute(WebKeys.VALUE_HOLDER);
		if(obj !=null && obj instanceof ValueHolder){
			ns= (String)((ValueHolder)obj).get("next-screen");
		}
		if(Validator.isNull(ns)){
			ns=(String)req.getAttribute("next-screen");
			if(Validator.isNull(ns))
				ns=req.getParameter("next-screen"); 
		}
		return ns;
    	
    }    
}
