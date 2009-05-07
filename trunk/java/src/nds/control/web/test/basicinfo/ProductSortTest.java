package nds.control.web.test.basicinfo;


import java.io.IOException;

import junit.framework.Test;
import junit.framework.TestSuite;
import nds.control.web.MainServlet;
import nds.log.Logger;
import nds.log.LoggerManager;

import org.apache.cactus.ServletTestCase;
import org.apache.cactus.WebRequest;



public class ProductSortTest extends ServletTestCase
{
    private static Logger logger= LoggerManager.getInstance().getLogger(ProductSortTest.class.getName());
    private String id;
    private MainServlet servlet=null;
    public ProductSortTest(String name) {
        super(name);
        id= "100001";// the id of user to be tested on
    }
    public static Test suite() {
        TestSuite suite= new TestSuite("ProductSortTest");
        suite.addTest(new ProductSortTest("testCreate"));
        suite.addTest(new ProductSortTest("testModify"));
        suite.addTest(new ProductSortTest("testDelete"));
        System.out.println("suite in ProductSortTest called");
        return suite;
    }

    public void beginModify(WebRequest req)
    {
        // Set up HTTP related parameters
        req.setURL("172.16.0.5:7001", "/nds", "/control","/command", null);
        req.addParameter("command", "ProductSortModify");
        req.addParameter("id", id);

        req.addParameter("operatorid","0");
        req.addParameter("directoryid","21");

        req.addParameter("name" , "2");



    }
    public void beginCreate(WebRequest req)
    {
        // Set up HTTP related parameters
        req.setURL("172.16.0.5:7001", "/nds", "/control",
            "/command", null);
        req.addParameter("command", "ProductSortCreate");
        req.addParameter("id", id);

        req.addParameter("operatorid","0");
        req.addParameter("directoryid","21");

        req.addParameter("name" , "2");






    }
    public void beginDelete(WebRequest req)
    {
        // Set up HTTP related parameters
        req.setURL("172.16.0.5:7001", "/nds", "/control",
            "/command", null);
        req.addParameter("command", "ProductSortDelete");
        req.addParameter("id", id);

        req.addParameter("operatorid","0");
        req.addParameter("directoryid","21");

        req.addParameter("name" , "2");




    }

    public void testCreate()   throws Exception{
        System.out.println("testCreate in ProductSortTest called");
        askServlet();
    }
    public void endCreate(com.meterware.httpunit.WebResponse res) throws IOException, org.xml.sax.SAXException
    {
        checkSuccess(res);
    }
    public void testModify() throws Exception{
        System.out.println("testModify in ProductSortTest called");
        askServlet();

    }
    public void endModify(com.meterware.httpunit.WebResponse res) throws IOException, org.xml.sax.SAXException
    {
        checkSuccess(res);
    }
    public void testDelete() throws Exception{
        System.out.println("testDelete in ProductSortTest called");
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