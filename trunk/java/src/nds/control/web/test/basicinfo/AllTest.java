package nds.control.web.test.basicinfo;

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
		//suite.addTestSuite(nds.control.web.test.URLMappingManagerTest.class);
                // suite.addTestSuite(nds.control.web.test.MainServletTest.class);

                suite.addTest(nds.control.web.test.basicinfo.VendorTest.suite());
                suite.addTest(nds.control.web.test.basicinfo.CustomerSortTest.suite());
                suite.addTest(nds.control.web.test.basicinfo.PftBuyerSortTest.suite());
                suite.addTest(nds.control.web.test.basicinfo.CustomerTest.suite());
                suite.addTest(nds.control.web.test.basicinfo.StockTest.suite());
                suite.addTest(nds.control.web.test.basicinfo.WarehouseTest.suite());
                suite.addTest(nds.control.web.test.basicinfo.ProductTest.suite());
                suite.addTest(nds.control.web.test.basicinfo.DelivererTest.suite());
                suite.addTest(nds.control.web.test.basicinfo.DepartmentTest.suite());
                suite.addTest(nds.control.web.test.basicinfo.EmployeeTest.suite());
                suite.addTest(nds.control.web.test.basicinfo.ProductSortTest.suite());


		return suite;
	}
}

