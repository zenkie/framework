package nds.control.web.test.distribution;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Title:        NDS Project
 * Description:  San gao shui yuan, mu xiang ren jia
 * Copyright:    Copyright (c) 2001
 * Company:      aic
 * @author yfzhu
 * @version 1.0
 */

public class AllTest implements java.io.Serializable {
	public static void main(String[] args)throws Exception {
            nds.log.LoggerManager.getInstance().init("/nds.properties");
	    junit.textui.TestRunner.run(suite());
	}

	public static Test suite() {
		TestSuite suite= new TestSuite("NDS web control tests");


                 suite.addTest(nds.control.web.test.distribution.DisRequestShtTest.suite());
//              suite.addTest(nds.control.web.test.distribution.DisShtTest.suite());
//ok               suite.addTest(nds.control.web.test.distribution.DisShipShtTest.suite());
//ok               suite.addTest(nds.control.web.test.distribution.PickShtTest.suite());

//ok               suite.addTest(nds.control.web.test.distribution.DisRequestShtItemTest.suite());
//ok               suite.addTest(nds.control.web.test.distribution.DisShtItemTest.suite());
//ok               suite.addTest(nds.control.web.test.distribution.DisShipShtItemTest.suite());
//ok               suite.addTest(nds.control.web.test.distribution.PickShtItemTest.suite());

//ok               suite.addTest(nds.control.web.test.distribution.DisPickRelationTest.suite());






		return suite;
	}
}

