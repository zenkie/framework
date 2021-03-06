package nds.control.web.test.distribution;

import java.io.IOException;

import junit.framework.Test;
import junit.framework.TestSuite;
import nds.control.web.MainServlet;
import nds.log.Logger;
import nds.log.LoggerManager;

import org.apache.cactus.ServletTestCase;
import org.apache.cactus.WebRequest;



public class PickShtItemTest extends ServletTestCase
{
    private static Logger logger= LoggerManager.getInstance().getLogger(PickShtItemTest.class.getName());
    private String id;
    private MainServlet servlet=null;
    public PickShtItemTest(String name) {
        super(name);
        id= "100004";// the id of user to be tested on
    }
    public static Test suite() {
        TestSuite suite= new TestSuite("PickShtItemTest");
        suite.addTest(new PickShtItemTest("testCreate"));
        suite.addTest(new PickShtItemTest("testModify"));
        suite.addTest(new PickShtItemTest("testDelete"));
        System.out.println("suite in PickShtItemTest called");
        return suite;
    }

    public void beginModify(WebRequest req)
    {
        // Set up HTTP related parameters
        req.setURL("172.16.0.5:7001", "/nds", "/control","/command", null);
        req.addParameter("command", "PickShtItemModify");
        req.addParameter("id", id);

        req.addParameter("picShtId" , "1");
        req.addParameter("pickShtId" , "1");
        req.addParameter("makeNo" , "1");
        req.addParameter("productId" , "1");
        req.addParameter("shipStockId" , "1");
        req.addParameter("requestShipQty" , "1");


    }

    public void beginCreate(WebRequest req)
    {
        // Set up HTTP related parameters
        req.setURL("172.16.0.5:7001", "/nds", "/control",
            "/command", null);
        req.addParameter("command", "PickShtItemCreate");
        req.addParameter("id", id);

        req.addParameter("picShtId" , "1");
        req.addParameter("pickShtId" , "1");
        req.addParameter("makeNo" , "1");
        req.addParameter("productId" , "1");
        req.addParameter("shipStockId" , "1");
        req.addParameter("requestShipQty" , "1");





    }

    public void beginDelete(WebRequest req)
    {
        // Set up HTTP related parameters
        req.setURL("172.16.0.5:7001", "/nds", "/control",
            "/command", null);
        req.addParameter("command", "PickShtItemDelete");
        req.addParameter("id", id);

        req.addParameter("picShtId" , "1");
        req.addParameter("makeNo" , "1");
        req.addParameter("productId" , "1");
        req.addParameter("shipStockId" , "1");
        req.addParameter("requestShipQty" , "1");

    }


    public void testCreate()   throws Exception{
        System.out.println("testCreate in PickShtItemTest called");
        askServlet();
    }
    public void endCreate(com.meterware.httpunit.WebResponse res) throws IOException, org.xml.sax.SAXException
    {
        checkSuccess(res);
    }
    public void testModify() throws Exception{
        System.out.println("testModify in PickShtItemTest called");
        askServlet();

    }


    public void endModify(com.meterware.httpunit.WebResponse res) throws IOException, org.xml.sax.SAXException
    {
        checkSuccess(res);
    }

    public void testDelete() throws Exception{
        System.out.println("testDelete in PickShtItemTest called");
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