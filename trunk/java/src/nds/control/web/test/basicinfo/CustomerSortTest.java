package nds.control.web.test.basicinfo;

import java.io.IOException;

import junit.framework.Test;
import junit.framework.TestSuite;
import nds.control.web.MainServlet;
import nds.log.Logger;
import nds.log.LoggerManager;

import org.apache.cactus.ServletTestCase;
import org.apache.cactus.WebRequest;



public class CustomerSortTest extends ServletTestCase
{
    private static Logger logger= LoggerManager.getInstance().getLogger(CustomerSortTest.class.getName());
    private String id;
    private MainServlet servlet=null;
    public CustomerSortTest(String name) {
        super(name);
        id= "100001";// the id of user to be tested on
    }
    public static Test suite() {
        TestSuite suite= new TestSuite("CustomerSortTest");
        suite.addTest(new CustomerSortTest("testCreate"));
        suite.addTest(new CustomerSortTest("testModify"));
        suite.addTest(new CustomerSortTest("testDelete"));
        System.out.println("suite in CustomerSortTest called");
        return suite;
    }

    public void beginModify(WebRequest req)
    {
        // Set up HTTP related parameters
        req.setURL("172.16.0.5:7001", "/nds", "/control","/command", null);
        req.addParameter("command", "CustomerSortModify");
        req.addParameter("id", id);

        req.addParameter("operatorid","0");
        req.addParameter("directoryid","21");

        req.addParameter("customerSort", "2");
        req.addParameter("customerSortDetail", "2");





    }
    public void beginCreate(WebRequest req)
    {
        // Set up HTTP related parameters
        req.setURL("172.16.0.5:7001", "/nds", "/control",
            "/command", null);
        req.addParameter("command", "CustomerSortCreate");
        req.addParameter("id", id);

        req.addParameter("operatorid","0");
        req.addParameter("directoryid","21");

        req.addParameter("customerSort", "2");
        req.addParameter("customerSortDetail", "2");



    }
    public void beginDelete(WebRequest req)
    {
        // Set up HTTP related parameters
        req.setURL("172.16.0.5:7001", "/nds", "/control",
            "/command", null);
        req.addParameter("command", "CustomerSortDelete");
        req.addParameter("id", id);

        req.addParameter("operatorid","0");
        req.addParameter("directoryid","21");



    }

    public void testCreate()   throws Exception{
        System.out.println("testCreate in CustomerSortTest called");
        askServlet();
    }
    public void endCreate(com.meterware.httpunit.WebResponse res) throws IOException, org.xml.sax.SAXException
    {
        checkSuccess(res);
    }
    public void testModify() throws Exception{
        System.out.println("testModify in CustomerSortTest called");
        askServlet();

    }
    public void endModify(com.meterware.httpunit.WebResponse res) throws IOException, org.xml.sax.SAXException
    {
        checkSuccess(res);
    }
    public void testDelete() throws Exception{
        System.out.println("testDelete in CustomerSortTest called");
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
            boolean b=  "��Ϣ".equals(name);
            if( !b) logger.error(content);
            assertEquals("��Ϣ", name);

    }

}