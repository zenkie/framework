/******************************************************************
*
*$RCSfile: GroupPermissionTest.java,v $ $Revision: 1.1.1.1 $ $Author: Administrator $ $Date: 2005/03/15 11:23:16 $
*
*$Log: GroupPermissionTest.java,v $
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
import java.io.IOException;

import junit.framework.Test;
import junit.framework.TestSuite;
import nds.control.web.MainServlet;
import nds.log.Logger;
import nds.log.LoggerManager;

import org.apache.cactus.ServletTestCase;
import org.apache.cactus.WebRequest;

/**
 * Title:        NDS Project
 * Description:  San gao shui yuan, mu xiang ren jia
 * Copyright:    Copyright (c) 2001
 * Company:      aic
 * @author yfzhu
 * @version 1.0
 */

public class GroupPermissionTest extends ServletTestCase
{
    private static Logger logger= LoggerManager.getInstance().getLogger(GroupPermissionTest.class.getName());
    private String id;
    private MainServlet servlet=null;
    public GroupPermissionTest(String name) {
        super(name);
        id= "0";// the id of user to be tested on
    }
    public static Test suite() {
        TestSuite suite= new TestSuite("GroupPermissionTest");
        suite.addTest(new GroupPermissionTest("testSet"));
        suite.addTest(new GroupPermissionTest("testRemove"));
        System.out.println("suite in GroupPermissionTest called");
        return suite;
    }
    /**
     * @param event - special parameters:
     *      1*. catalog(array) | directoryid(array)
     *      2*. permission( "read" | "write" | null[only if action="delete"])
     *      3*. action( "remove" | "set")
     */

    public void beginSet(WebRequest req)
    {
        // Set up HTTP related parameters
        req.setURL("yfzhu:7001", "/nds", "/control","/command", null);
        req.addParameter("command", "GroupSetPermission");
        req.addParameter("id", id);
        req.addParameter("action", "set");
        req.addParameter("catalog", "test");
        req.addParameter("permission", "write");

    }
    public void beginRemove(WebRequest req)
    {
        // Set up HTTP related parameters
        req.setURL("yfzhu:7001", "/nds", "/control",
            "/command", null);
        req.addParameter("command", "GroupSetPermission");
        req.addParameter("id", id);
        req.addParameter("action", "remove");
        req.addParameter("catalog", "test");
        req.addParameter("permission", "write");

    }

    public void testSet()   throws Exception{
        askServlet();
    }
    public void endSet(com.meterware.httpunit.WebResponse res) throws IOException, org.xml.sax.SAXException
    {
        checkSuccess(res);
    }
    public void endRemove(com.meterware.httpunit.WebResponse res) throws IOException, org.xml.sax.SAXException
    {
        checkSuccess(res);
    }
    public void testRemove() throws Exception{
        askServlet();

    }

    public void askServlet() throws Exception{
    // Initialize class to test
        if(servlet ==null){
        servlet = new MainServlet();
        // VERY IMPORTANT : Call the init() method in order to initialize the
        //                  Servlet ServletConfig object.
        servlet.init(config);
        servlet.init();
        }
        servlet.doGet(this.request, this.response);

    }

    public void checkSuccess(com.meterware.httpunit.WebResponse res) throws IOException, org.xml.sax.SAXException{
        String content= res.getText();
        assertTrue(content, res.isHTML());
            String name=res.getTitle();
            boolean b=  "信息".equals(name);
            if( !b) logger.error(content);
            assertEquals("信息", name);

    }

}