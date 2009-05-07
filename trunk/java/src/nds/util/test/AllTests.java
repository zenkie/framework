/******************************************************************
*
*$RCSfile: AllTests.java,v $ $Revision: 1.2 $ $Author: Administrator $ $Date: 2006/03/13 01:12:59 $
*
*$Log: AllTests.java,v $
*Revision 1.2  2006/03/13 01:12:59  Administrator
*no message
*
*Revision 1.1.1.1  2005/03/15 11:23:27  Administrator
*init
*
*Revision 1.2  2003/03/30 08:12:00  yfzhu
*Updated before subtotal added
*
*Revision 1.1.1.1  2002/11/29 02:50:35  yfzhu
*Active POS
*
*Revision 1.2  2001/11/07 20:58:48  yfzhu
*no message
*
*
********************************************************************/
package nds.util.test;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {
	public static void main(String[] args) {
		junit.textui.TestRunner.run(suite());
	}

	public static Test suite() {
		TestSuite suite= new TestSuite("NDS util tests");
/*                suite.addTestSuite(nds.util.test.NDSExceptionTest.class);
		suite.addTestSuite(nds.util.test.IntHashtableTest.class);*/
 		suite.addTestSuite(nds.util.test.LicenseTest.class);
        //		suite.addTestSuite(nds.util.test.CommandExecuterTest.class);
		return suite;
	}
}
