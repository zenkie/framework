package nds.control.web.test.income;

/**
 * Title:        NDS Project
 * Description:  San gao shui yuan, mu xiang ren jia
 * Copyright:    Copyright (c) 2001
 * Company:      aic
 * @author yfzhu
 * @version 1.0
 */

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTest implements java.io.Serializable{
	public static void main(String[] args)throws Exception {
            nds.log.LoggerManager.getInstance().init("/nds.properties");
	    junit.textui.TestRunner.run(suite());
	}

	public static Test suite() {
		TestSuite suite= new TestSuite("NDS web control tests");
		//suite.addTestSuite(nds.control.web.test.URLMappingManagerTest.class);
                // suite.addTestSuite(nds.control.web.test.MainServletTest.class);
                 suite.addTestSuite(nds.control.web.test.income.CustIncomeShtItemTest.class);
                //suite.addTest(nds.control.web.test.UserTest.suite());
                //suite.addTest(nds.control.web.test.GroupTest.suite());
                //suite.addTest(nds.control.web.test.GroupPermissionTest.suite());
		return suite;
	}
}