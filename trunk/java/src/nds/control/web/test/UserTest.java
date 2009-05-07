/******************************************************************
*
*$RCSfile: UserTest.java,v $ $Revision: 1.1.1.1 $ $Author: Administrator $ $Date: 2005/03/15 11:23:16 $
*
*$Log: UserTest.java,v $
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

public class UserTest extends ServletTestCase
{
    private static Logger logger= LoggerManager.getInstance().getLogger(UserTest.class.getName());
    private String id;
    private MainServlet servlet=null;
    public UserTest(String name) {
        super(name);
        id= "100001";// the id of user to be tested on
    }
    public static Test suite() {
        TestSuite suite= new TestSuite("UserTest");
        suite.addTest(new UserTest("testCreate"));
        suite.addTest(new UserTest("testModify"));
        suite.addTest(new UserTest("testDelete"));
        System.out.println("suite in UserTest called");
        return suite;
    }

    public void beginModify(WebRequest req)
    {
        // Set up HTTP related parameters
        req.setURL("yfzhu:7001", "/nds", "/control","/command", null);
        req.addParameter("command", "UserModify");
        req.addParameter("id", id);
        req.addParameter("name", RandomGen.getString(12));
        req.addParameter("password", "abc123");
        req.addParameter("isEnabled", "1");
        req.addParameter("isEmployee", "1");
        req.addParameter("isAdmin", "1");
        req.addParameter("description", "÷Ï“∂∑Â");

    }
    public void beginCreate(WebRequest req)
    {
        // Set up HTTP related parameters
        req.setURL("yfzhu:7001", "/nds", "/control",
            "/command", null);
        req.addParameter("command", "UserCreate");
        req.addParameter("id", id);
        req.addParameter("name", RandomGen.getString(12));
        req.addParameter("password", "abc123");
        req.addParameter("isEnabled", "1");
        req.addParameter("isEmployee", "1");
        req.addParameter("isAdmin", "1");
        req.addParameter("description", "÷Ï“∂∑Â");

    }
    public void beginDelete(WebRequest req)
    {
        // Set up HTTP related parameters
        req.setURL("yfzhu:7001", "/nds", "/control",
            "/command", null);
        req.addParameter("command", "UserDelete");
        req.addParameter("id", id);
        req.addParameter("name", RandomGen.getString(12));
        req.addParameter("password", "abc123");
        req.addParameter("isEnabled", "1");
        req.addParameter("isEmployee", "1");
        req.addParameter("isAdmin", "1");
        req.addParameter("description", "÷Ï“∂∑Â");

    }

    public void testCreate()   throws Exception{
        System.out.println("testCreate in UserTest called");
        askServlet();
    }
    public void endCreate(com.meterware.httpunit.WebResponse res) throws IOException, org.xml.sax.SAXException
    {
        checkSuccess(res);
    }
    public void testModify() throws Exception{
        System.out.println("testModify in UserTest called");
        askServlet();

    }
    public void endModify(com.meterware.httpunit.WebResponse res) throws IOException, org.xml.sax.SAXException
    {
        checkSuccess(res);
    }
    public void testDelete() throws Exception{
        System.out.println("testDelete in UserTest called");
        askServlet();

    }
    public void endDelete(com.meterware.httpunit.WebResponse res) throws IOException, org.xml.sax.SAXException
    {
        checkSuccess(res);
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
            boolean b=  "–≈œ¢".equals(name);
            if( !b) logger.error(content);
            assertEquals("–≈œ¢", name);

    }

}