/******************************************************************
*
*$RCSfile: AllTests.java,v $ $Revision: 1.1.1.1 $ $Author: Administrator $ $Date: 2005/03/15 11:23:16 $
*
*$Log: AllTests.java,v $
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

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests implements java.io.Serializable{
	public static void main(String[] args)throws Exception {
            nds.log.LoggerManager.getInstance().init("/nds.properties");
	    junit.textui.TestRunner.run(suite());
	}

	public static Test suite() {
		TestSuite suite= new TestSuite("NDS web control tests");
		//suite.addTestSuite(nds.control.web.test.URLMappingManagerTest.class);
                // suite.addTestSuite(nds.control.web.test.MainServletTest.class);
 //                suite.addTestSuite(nds.control.web.test.NavTreeServletTest.class);
                suite.addTest(nds.control.web.test.UserTest.suite());
                //suite.addTest(nds.control.web.test.GroupTest.suite());
                //suite.addTest(nds.control.web.test.GroupPermissionTest.suite());
		return suite;
	}
}