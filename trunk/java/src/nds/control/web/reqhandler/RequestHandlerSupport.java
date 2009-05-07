/******************************************************************
*
*$RCSfile: RequestHandlerSupport.java,v $ $Revision: 1.1.1.1 $ $Author: Administrator $ $Date: 2005/03/15 11:23:16 $
*
*$Log: RequestHandlerSupport.java,v $
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
//Source file: F:\\work2\\tmp\\nds\\control\\web\\reqhandler\\RequestHandlerSupport.java

package nds.control.web.reqhandler;

import javax.servlet.http.HttpServletRequest;

/**
 * This class is the default implementation of the RequestHandler
 *
*/
public abstract class RequestHandlerSupport implements RequestHandler {

    //protected transient ServletContext context;

    /*    public void setServletContext(ServletContext context) {
         //   this.context  = context;
        }*/

    public void doStart(HttpServletRequest request) {}
    public void doEnd(HttpServletRequest request) {}
    public String toString(){
    	return this.getClass().getName();
    }
}
