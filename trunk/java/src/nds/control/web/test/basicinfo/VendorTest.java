package nds.control.web.test.basicinfo;

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
public class VendorTest extends ServletTestCase {
    private static Logger logger= LoggerManager.getInstance().getLogger(VendorTest.class.getName());
    private String id;
    private MainServlet servlet=null;
    public VendorTest(String name) {
        super(name);
        id= "1027";// the id of user to be tested on
    }
    public static Test suite() {
        TestSuite suite= new TestSuite("VendorTest");
        suite.addTest(new VendorTest("testCreate"));
        suite.addTest(new VendorTest("testModify"));
        suite.addTest(new VendorTest("testDelete"));
        System.out.println("suite in VendorTest called");
        return suite;
    }

    public void  beginCreate(WebRequest req)
    {
        // Set up HTTP related parameters
        req.setURL("localhost:7001", "/nds", "/control","/command", null);
        req.addParameter("command", "VendorCreate");
        req.addParameter("id", id);

        req.addParameter("operatorid","0");
        req.addParameter("directoryid","21");

        req.addParameter("no","1027");
        req.addParameter("name","1027");
        req.addParameter("country","1");
        req.addParameter("province","1");
        req.addParameter("city","1");
        req.addParameter("address","1");
        req.addParameter("postcode","1");
        req.addParameter("linkman","2000/10/10");
        req.addParameter("linkmanDept","2000.10.10");
        req.addParameter("position","1");
        req.addParameter("officePhone","1");
        req.addParameter("officeFax","1");
        req.addParameter("homePhone","1");
        req.addParameter("mobile","1");
        req.addParameter("registerBank","1");
        req.addParameter("taxNo","1");



    }
    public void beginModify(WebRequest req)
    {
         // Set up HTTP related parameters
        req.setURL("localhost:7001", "/nds", "/control","/command", null);
        req.addParameter("command", "VendorModify");
        req.addParameter("id", id);

        req.addParameter("operatorid","0");
        req.addParameter("directoryid","21");

        req.addParameter("no","1027");
        req.addParameter("name","1027");
        req.addParameter("country","1");
        req.addParameter("province","1");
        req.addParameter("city","1");
        req.addParameter("address","1");
        req.addParameter("postcode","1");
        req.addParameter("linkman","2000/10/10");
        req.addParameter("linkmanDept","2000.10.10");
        req.addParameter("position","1");
        req.addParameter("officePhone","1");
        req.addParameter("officeFax","1");
        req.addParameter("homePhone","1");
        req.addParameter("mobile","1");
        req.addParameter("registerBank","1");
        req.addParameter("taxNo","1");

    }
    public void beginDelete(WebRequest req)
    {
        // Set up HTTP related parameters
        req.setURL("localhost:7001", "/nds", "/control","/command", null);
        req.addParameter("command", "VendorDelete");
        req.addParameter("id", id);

        req.addParameter("operatorid","0");
        req.addParameter("directoryid","21");

        req.addParameter("no","1027");
        req.addParameter("name","1027");
        req.addParameter("country","1");
        req.addParameter("province","1");
        req.addParameter("city","1");
        req.addParameter("address","1");
        req.addParameter("postcode","1");
        req.addParameter("linkman","1");
        req.addParameter("linkmanDept","1");
        req.addParameter("position","1");
        req.addParameter("officePhone","1");
        req.addParameter("officeFax","1");
        req.addParameter("homePhone","1");
        req.addParameter("mobile","1");
        req.addParameter("registerBank","1");
        req.addParameter("taxNo","1");

    }

    public void testCreate()   throws Exception{
        System.out.println("testCreate in VendorTest called");
        askServlet();
    }
    public void endCreate(com.meterware.httpunit.WebResponse res) throws IOException, org.xml.sax.SAXException
    {
        checkSuccess(res);
    }

    public void testModify() throws Exception{
        System.out.println("testModify in VendorTest called");
        askServlet();

    }
    public void endModify(com.meterware.httpunit.WebResponse res) throws IOException, org.xml.sax.SAXException
    {
        checkSuccess(res);
    }
    public void testDelete() throws Exception{
        System.out.println("testDelete in VendorTest called");
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