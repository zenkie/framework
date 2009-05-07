package nds.control.web.test.basicinfo;


import java.io.IOException;

import junit.framework.Test;
import junit.framework.TestSuite;
import nds.control.web.MainServlet;
import nds.log.Logger;
import nds.log.LoggerManager;

import org.apache.cactus.ServletTestCase;
import org.apache.cactus.WebRequest;



public class EmployeeTest extends ServletTestCase
{
    private static Logger logger= LoggerManager.getInstance().getLogger(EmployeeTest.class.getName());
    private String id;
    private MainServlet servlet=null;
    public EmployeeTest(String name) {
        super(name);
        id= "100001";// the id of user to be tested on
    }
    public static Test suite() {
        TestSuite suite= new TestSuite("EmployeeTest");
        suite.addTest(new EmployeeTest("testCreate"));
        suite.addTest(new EmployeeTest("testModify"));
        suite.addTest(new EmployeeTest("testDelete"));
        System.out.println("suite in EmployeeTest called");
        return suite;
    }

    public void beginModify(WebRequest req)
    {
        // Set up HTTP related parameters
        req.setURL("172.16.0.5:7001", "/nds", "/control","/command", null);
        req.addParameter("command", "EmployeeModify");
        req.addParameter("id", id);

        req.addParameter("operatorid","0");
        req.addParameter("directoryid","21");

        req.addParameter("userId" , "2");
        req.addParameter("users_name" , "adsf");
        req.addParameter("email" , "1");
        req.addParameter("name" , "1");
        req.addParameter("gender" , "1");
        req.addParameter("no" , "1");
        req.addParameter("departmentId" , "1");
        req.addParameter("position" , "1");
        req.addParameter("officePhone","1");
        req.addParameter("joininDate" , "2000/10/10");
        req.addParameter("birthday" , "2000/10/10");
        req.addParameter("homeAddress" , "1");
        req.addParameter("homePostcode" , "1");
        req.addParameter("homePhone","1");
        req.addParameter("mobile","1");




    }
    public void beginCreate(WebRequest req)
    {
        // Set up HTTP related parameters
        req.setURL("172.16.0.5:7001", "/nds", "/control",
            "/command", null);
        req.addParameter("command", "EmployeeCreate");
        req.addParameter("id", id);

        req.addParameter("operatorid","0");
        req.addParameter("directoryid","21");

        req.addParameter("userId" , "2");
        req.addParameter("users_name" , "adsf");
        req.addParameter("email" , "1");
        req.addParameter("name" , "1");
        req.addParameter("gender" , "1");
        req.addParameter("no" , "1");
        req.addParameter("departmentId" , "1");
        req.addParameter("position" , "1");
        req.addParameter("officePhone","1");
        req.addParameter("joininDate" , "2000/10/10");
        req.addParameter("birthday" , "2000/10/10");
        req.addParameter("homeAddress" , "1");
        req.addParameter("homePostcode" , "1");
        req.addParameter("homePhone","1");
        req.addParameter("mobile","1");




    }
    public void beginDelete(WebRequest req)
    {
        // Set up HTTP related parameters
        req.setURL("172.16.0.5:7001", "/nds", "/control",
            "/command", null);
        req.addParameter("command", "EmployeeDelete");
        req.addParameter("id", id);

        req.addParameter("operatorid","0");
        req.addParameter("directoryid","21");

        req.addParameter("userId" , "2");
        req.addParameter("users_name" , "adsf");
        req.addParameter("email" , "1");
        req.addParameter("name" , "1");
        req.addParameter("gender" , "1");
        req.addParameter("no" , "1");
        req.addParameter("departmentId" , "1");
        req.addParameter("position" , "1");
        req.addParameter("officePhone","1");
        req.addParameter("joininDate" , "2000/10/10");
        req.addParameter("birthday" , "2000/10/10");
        req.addParameter("homeAddress" , "1");
        req.addParameter("homePostcode" , "1");
        req.addParameter("homePhone","1");
        req.addParameter("mobile","1");


    }

    public void testCreate()   throws Exception{
        System.out.println("testCreate in EmployeeTest called");
        askServlet();
    }
    public void endCreate(com.meterware.httpunit.WebResponse res) throws IOException, org.xml.sax.SAXException
    {
        checkSuccess(res);
    }
    public void testModify() throws Exception{
        System.out.println("testModify in EmployeeTest called");
        askServlet();

    }
    public void endModify(com.meterware.httpunit.WebResponse res) throws IOException, org.xml.sax.SAXException
    {
        checkSuccess(res);
    }
    public void testDelete() throws Exception{
        System.out.println("testDelete in EmployeeTest called");
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