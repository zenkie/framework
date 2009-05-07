package nds.control.web.test.preferential;

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

public class AllTests implements java.io.Serializable {
  public static void main(String[] args)throws Exception {
    nds.log.LoggerManager.getInstance().init("/nds.properties");
    junit.textui.TestRunner.run(suite());
  }

  public static Test suite() {
    TestSuite suite= new TestSuite("NDS web control tests");
    //suite.addTest(nds.control.web.test.preferential.PreferentialShtTest.suite());
    suite.addTest(nds.control.web.test.preferential.PreferentialShtItemTest.suite());
    return suite;
  }
}