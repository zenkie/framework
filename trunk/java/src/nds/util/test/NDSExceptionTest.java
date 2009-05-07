/******************************************************************
*
*$RCSfile: NDSExceptionTest.java,v $ $Revision: 1.1.1.1 $ $Author: Administrator $ $Date: 2005/03/15 11:23:27 $
*
*$Log: NDSExceptionTest.java,v $
*Revision 1.1.1.1  2005/03/15 11:23:27  Administrator
*init
*
*Revision 1.1.1.1  2002/11/29 02:50:35  yfzhu
*Active POS
*
*Revision 1.3  2001/11/10 04:12:33  yfzhu
*no message
*
*Revision 1.2  2001/11/07 20:58:48  yfzhu
*no message
*
*
********************************************************************/
package nds.util.test;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import nds.control.event.NDSEventException;
import nds.util.NDSException;

public class NDSExceptionTest  extends TestCase {
    public NDSExceptionTest(String name) {
        super(name);
    }
    public void testNDSEventException(){
        String m=null;
        String msg="当前登录用户不是员工，或者对应员工记录未建立.";
        try{
        throwException(msg);
        }catch(NDSException e){
            m=e.getSimpleMessage();
        }
        this.assertEquals(msg,m);
    }
    private void throwException(String s) throws NDSException{
        throw new NDSEventException(s);
    }
	public static void main(String[] args) {
		junit.textui.TestRunner.run(suite());
	}
	public static Test suite() {
		TestSuite suite= new TestSuite("NDS util tests");
		suite.addTestSuite(NDSExceptionTest.class);
		return suite;
	}

}