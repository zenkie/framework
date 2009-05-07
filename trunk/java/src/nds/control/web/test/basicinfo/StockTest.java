package nds.control.web.test.basicinfo;


import java.io.IOException;

import junit.framework.Test;
import junit.framework.TestSuite;
import nds.control.web.MainServlet;
import nds.log.Logger;
import nds.log.LoggerManager;

import org.apache.cactus.ServletTestCase;
import org.apache.cactus.WebRequest;



public class StockTest extends ServletTestCase
{
    private static Logger logger= LoggerManager.getInstance().getLogger(StockTest.class.getName());
    private String id;
    private MainServlet servlet=null;
    public StockTest(String name) {
        super(name);
        id= "110001";// the id of user to be tested on
    }
    public static Test suite() {
        TestSuite suite= new TestSuite("StockTest");
        suite.addTest(new StockTest("testCreate"));
        suite.addTest(new StockTest("testModify"));
        suite.addTest(new StockTest("testDelete"));
        System.out.println("suite in StockTest called");
        return suite;
    }

    public void beginModify(WebRequest req)
    {
        // Set up HTTP related parameters
        req.setURL("172.16.0.5:7001", "/nds", "/control","/command", null);
        req.addParameter("command", "StockModify");
        req.addParameter("id", id);

        req.addParameter("no" , "1t");
        req.addParameter("name" , "1t");
        req.addParameter("warehouseId" , "1");
        req.addParameter("sort" , "1");
        req.addParameter("note" , "1");




    }
    public void beginCreate(WebRequest req)
    {
        // Set up HTTP related parameters
        req.setURL("172.16.0.5:7001", "/nds", "/control",
            "/command", null);
        req.addParameter("command", "StockCreate");
        req.addParameter("id", id);

        req.addParameter("no" , "11");
        req.addParameter("name" , "11");
        req.addParameter("warehouseId" , "1");
        req.addParameter("sort" , "1");
        req.addParameter("note" , "1");



    }
    public void beginDelete(WebRequest req)
    {
        // Set up HTTP related parameters
        req.setURL("172.16.0.5:7001", "/nds", "/control",
            "/command", null);
        req.addParameter("command", "StockDelete");
        req.addParameter("id", id);

        req.addParameter("no" , "1");
        req.addParameter("name" , "1");
        req.addParameter("warehouseId" , "1");
        req.addParameter("sort" , "1");
        req.addParameter("note" , "1");

    }

    public void testCreate()   throws Exception{
        System.out.println("testCreate in StockTest called");
        askServlet();
    }
    public void endCreate(com.meterware.httpunit.WebResponse res) throws IOException, org.xml.sax.SAXException
    {
        checkSuccess(res);
    }
    public void testModify() throws Exception{
        System.out.println("testModify in StockTest called");
        askServlet();

    }
    public void endModify(com.meterware.httpunit.WebResponse res) throws IOException, org.xml.sax.SAXException
    {
        checkSuccess(res);
    }
    public void testDelete() throws Exception{
        System.out.println("testDelete in StockTest called");
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