/******************************************************************
*
*$RCSfile: AllTests.java,v $ $Revision: 1.2 $ $Author: Administrator $ $Date: 2005/12/18 14:06:14 $
*
*$Log: AllTests.java,v $
*Revision 1.2  2005/12/18 14:06:14  Administrator
*no message
*
*Revision 1.1.1.1  2005/03/15 11:23:15  Administrator
*init
*
*Revision 1.2  2004/02/02 10:42:52  yfzhu
*<No Comment Entered>
*
*Revision 1.1.1.1  2002/11/29 02:50:34  yfzhu
*Active POS
*
*Revision 1.2  2001/11/07 20:58:46  yfzhu
*no message
*
*
********************************************************************/
package nds.control.ejb.test;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests implements java.io.Serializable{
	public static void main(String[] args)throws Exception {
            nds.log.LoggerManager.getInstance().init("e:/aic/conf/nds.properties");
	    junit.textui.TestRunner.run(suite());
	}

	public static Test suite() {
		TestSuite suite= new TestSuite("NDS web control tests");
		//suite.addTestSuite(nds.control.web.test.URLMappingManagerTest.class);
                // suite.addTestSuite(nds.control.web.test.MainServletTest.class);
                //suite.addTestSuite(nds.control.ejb.test.CommandMappingManagerTest.class);
        //        suite.addTestSuite(nds.control.ejb.test.TreeNodeManagerTest.class);
        //        suite.addTestSuite(nds.control.ejb.test.TreeNodeManagerTest.class);
                //suite.addTestSuite(nds.control.ejb.test.PickBoxSht_AMTest.class);
		return suite;
	}
}