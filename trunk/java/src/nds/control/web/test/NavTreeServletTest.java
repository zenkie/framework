/******************************************************************
*
*$RCSfile: NavTreeServletTest.java,v $ $Revision: 1.1.1.1 $ $Author: Administrator $ $Date: 2005/03/15 11:23:16 $
*
*$Log: NavTreeServletTest.java,v $
*Revision 1.1.1.1  2005/03/15 11:23:16  Administrator
*init
*
*Revision 1.1.1.1  2002/11/29 02:50:34  yfzhu
*Active POS
*
*Revision 1.2  2001/11/07 20:58:46  yfzhu
*no message
*
*
********************************************************************/
package nds.control.web.test;
import java.io.IOException;

import javax.servlet.ServletException;

import nds.control.web.NavTreeServlet;

import org.apache.cactus.ServletTestCase;
import org.apache.cactus.WebRequest;
/**
 * Title:        NDS Project
 * Description:  San gao shui yuan, mu xiang ren jia
 * Copyright:    Copyright (c) 2001
 * Company:      aic
 * @author yfzhu
 * @version 1.0
 */

public class NavTreeServletTest extends ServletTestCase
{
    public NavTreeServletTest(String name) {
        super(name);
    }
    public void beginMain(WebRequest theRequest)
    {
    // Set up HTTP related parameters
        theRequest.setURL("nds.control.web.NavTreeServlet", "/nds", "/navtreeservlet",
            null, null);
    }

    public void testMain()   throws ServletException,IOException{
    // Initialize class to test
        NavTreeServlet servlet = new NavTreeServlet();

        // VERY IMPORTANT : Call the init() method in order to initialize the
        //                  Servlet ServletConfig object.
        servlet.init(config);

        servlet.init();
        servlet.doGet(this.request, this.response);

    }
    public void endMain(com.meterware.httpunit.WebResponse res) throws IOException, org.xml.sax.SAXException
    {
        System.out.println(res.getText());
    }


}