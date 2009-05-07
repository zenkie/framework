/******************************************************************
*
*$RCSfile: AllTests.java,v $ $Revision: 1.2 $ $Author: Administrator $ $Date: 2005/12/18 14:06:16 $
*
*$Log: AllTests.java,v $
*Revision 1.2  2005/12/18 14:06:16  Administrator
*no message
*
*Revision 1.1.1.1  2005/03/15 11:23:23  Administrator
*init
*
*Revision 1.1.1.1  2002/11/29 02:50:34  yfzhu
*Active POS
*
*Revision 1.2  2001/11/07 20:58:47  yfzhu
*no message
*
*
********************************************************************/
package nds.query.test;

import junit.framework.Test;
import junit.framework.TestSuite;
public class AllTests {
	public static void main(String[] args)throws Exception {
            nds.log.LoggerManager.getInstance().init("/nds.properties");

/*           String s= QueryUtils.toSQLClause("aaa","> =1999/1/1",Column.DATE);
           s+="\n"+ QueryUtils.toSQLClause("aaa","1999/1/1-1999/34/34",Column.DATE);
           s+="\n"+ QueryUtils.toSQLClause("aaa","<=4334",Column.NUMBER);
           s+="\n"+ QueryUtils.toSQLClause("aaa","gxlu",Column.STRING);
           System.out.println(s);
           if(true)System.exit(1);
*/
	    junit.textui.TestRunner.run(suite());
	}

	public static Test suite() {
		TestSuite suite= new TestSuite("NDS query tests");
//		suite.addTestSuite(nds.query.test.QueryUtilsTest.class);
//		suite.addTestSuite(nds.query.test.QueryRequestTest.class);
        //suite.addTestSuite(nds.query.test.QueryEngineTest.class);
		//suite.addTestSuite(nds.query.test.QueryEngineTest2.class);
		return suite;
	}
}