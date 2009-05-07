/******************************************************************
*
*$RCSfile: RequestHandler.java,v $ $Revision: 1.1.1.1 $ $Author: Administrator $ $Date: 2005/03/15 11:23:16 $
*
*$Log: RequestHandler.java,v $
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
//Source file: F:\\work2\\tmp\\nds\\control\\web\\reqhandler\\RequestHandler.java

package nds.control.web.reqhandler;

import javax.servlet.http.HttpServletRequest;

import nds.control.event.NDSEvent;
import nds.control.event.NDSEventException;
/**
 * This class is the base interface to request handlers on the
 * web tier.
 *
*/
public interface RequestHandler extends java.io.Serializable {

    //public void setServletContext(ServletContext context);
    public void doStart(HttpServletRequest request);
    public NDSEvent processRequest(HttpServletRequest request) throws NDSEventException;
    public void doEnd(HttpServletRequest request);
}
