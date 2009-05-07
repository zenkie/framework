
package nds.net.test;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {
	public static void main(String[] args)throws Exception {
        nds.log.LoggerManager.getInstance().init("nds.properties");
	    junit.textui.TestRunner.run(suite());
	}

	public static Test suite() {
		TestSuite suite= new TestSuite("NDS query tests");
        // suite.addTestSuite(nds.net.test.TestSyncManager.class);
		return suite;
	}
}
