/******************************************************************
*
*$RCSfile: MainServletTest.java,v $ $Revision: 1.1.1.1 $ $Author: Administrator $ $Date: 2005/03/15 11:23:16 $
*
*$Log: MainServletTest.java,v $
*Revision 1.1.1.1  2005/03/15 11:23:16  Administrator
*init
*
*Revision 1.1.1.1  2002/11/29 02:50:34  yfzhu
*Active POS
*
*Revision 1.3  2001/11/16 11:42:40  yfzhu
*no message
*
*Revision 1.2  2001/11/07 20:58:46  yfzhu
*no message
*
*
********************************************************************/
package nds.control.web.test;
import javax.servlet.ServletException;

import nds.control.web.MainServlet;

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

public class MainServletTest extends ServletTestCase {
    public MainServletTest(String name) {
        super(name);
    }
    public void beginMain(WebRequest theRequest) {
        // Set up HTTP related parameters
        theRequest.setURL("nds.control.web.MainServlet", "/nds", "/control/login.jsp",
                          null, null);
    }

    public void testMain()   throws ServletException {
        // Initialize class to test
        MainServlet servlet = new MainServlet();
        //        SampleServletConfig servlet = new SampleServletConfig();

        // VERY IMPORTANT : Call the init() method in order to initialize the
        //                  Servlet ServletConfig object.
        servlet.init(config);

        servlet.init();
        // Set a variable in session as the doSomething() method that we are testing need
        // this variable to be present in the session (for example)
        session.setAttribute("name", "value");

        // Call the method to test, passing an HttpServletRequest object (for example)
        //        String result = servlet.doSomething(request);

        // Perform verification that test was successful
        //    assertEquals("something", result);
    }


}
