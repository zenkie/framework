/******************************************************************
*
*$RCSfile: GroupTest.java,v $ $Revision: 1.1.1.1 $ $Author: Administrator $ $Date: 2005/03/15 11:23:16 $
*
*$Log: GroupTest.java,v $
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

public class GroupTest extends ServletTestCase
{
    private static Logger logger= LoggerManager.getInstance().getLogger(GroupTest.class.getName());
    private String id;
    private MainServlet servlet=null;
    public GroupTest(String name) {
        super(name);
        id= "100001";// the id of Group to be tested on
    }
    public static Test suite() {
        TestSuite suite= new TestSuite("GroupTest");
        suite.addTest(new GroupTest("testCreate"));
        suite.addTest(new GroupTest("testModify"));
        suite.addTest(new GroupTest("testAddUser"));
        suite.addTest(new GroupTest("testRemoveUser"));
        suite.addTest(new GroupTest("testDelete"));
        System.out.println("suite in GroupTest called");
        return suite;
    }
    public void beginAddUser(WebRequest req){
        // Set up HTTP related parameters
        req.setURL("yfzhu:7001", "/nds", "/control",
            "/command", null);
        req.addParameter("command", "GroupModify");
        req.addParameter("action", "addUser");
        req.addParameter("id", id);
        req.addParameter("username", "root");
    }
    public void beginRemoveUser(WebRequest req){
        // Set up HTTP related parameters
        req.setURL("yfzhu:7001", "/nds", "/control",
            "/command", null);
        req.addParameter("command", "GroupModify");
        req.addParameter("action", "removeUser");
        req.addParameter("id", id);
        req.addParameter("username", "root");
    }
    public void beginModify(WebRequest req)
    {
        // Set up HTTP related parameters
        req.setURL("yfzhu:7001", "/nds", "/control",
            "/command", null);
        req.addParameter("command", "GroupModify");
        req.addParameter("action", "modify");
        req.addParameter("id", id);
        req.addParameter("name", RandomGen.getString(12));
        req.addParameter("description", "朱叶峰");

    }
    public void beginCreate(WebRequest req)
    {
        // Set up HTTP related parameters
        req.setURL("yfzhu:7001", "/nds", "/control",
            "/command", null);
        req.addParameter("command", "GroupCreate");
        req.addParameter("id", id);
        req.addParameter("name", RandomGen.getString(12));
        req.addParameter("description", "朱叶峰");

    }
    public void beginDelete(WebRequest req)
    {
        // Set up HTTP related parameters
        req.setURL("yfzhu:7001", "/nds", "/control",
            "/command", null);
        req.addParameter("command", "GroupDelete");
        req.addParameter("id", id);
    }

    public void testCreate()   throws Exception{
        System.out.println("testCreate in GroupTest called");
        askServlet();
    }
    public void endCreate(com.meterware.httpunit.WebResponse res) throws IOException, org.xml.sax.SAXException
    {
        checkSuccess(res);
    }
    public void testModify() throws Exception{
        System.out.println("testModify in GroupTest called");
        askServlet();

    }
    public void testAddUser() throws Exception{
        askServlet();

    }
    public void endAddUser(com.meterware.httpunit.WebResponse res) throws IOException, org.xml.sax.SAXException
    {
        checkSuccess(res);
    }
    public void testRemoveUser() throws Exception{
        askServlet();

    }
    public void endRemoveUser(com.meterware.httpunit.WebResponse res) throws IOException, org.xml.sax.SAXException
    {
        checkSuccess(res);
    }
    public void testDelete() throws Exception{
        System.out.println("testDelete in GroupTest called");
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
            boolean b=  "信息".equals(name);
            if( !b) logger.error(content);
            assertEquals("信息", name);

    }

}