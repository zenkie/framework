package nds.control.web.test.basicinfo;


import java.io.IOException;

import junit.framework.Test;
import junit.framework.TestSuite;
import nds.control.web.MainServlet;
import nds.log.Logger;
import nds.log.LoggerManager;

import org.apache.cactus.ServletTestCase;
import org.apache.cactus.WebRequest;



public class ProductTest extends ServletTestCase
{
    private static Logger logger= LoggerManager.getInstance().getLogger(ProductTest.class.getName());
    private String id;
    private MainServlet servlet=null;
    public ProductTest(String name) {
        super(name);
        id= "100001";// the id of user to be tested on
    }
    public static Test suite() {
        TestSuite suite= new TestSuite("ProductTest");
        suite.addTest(new ProductTest("testCreate"));
        suite.addTest(new ProductTest("testModify"));
        suite.addTest(new ProductTest("testDelete"));
        System.out.println("suite in ProductTest called");
        return suite;
    }

    public void beginModify(WebRequest req)
    {
        // Set up HTTP related parameters
        req.setURL("172.16.0.5:7001", "/nds", "/control","/command", null);
        req.addParameter("command", "ProductModify");
        req.addParameter("id", id);

        req.addParameter("operatorid","0");
        req.addParameter("directoryid","21");

        req.addParameter("no" , "w");
        req.addParameter("oldNo" , "1");
        req.addParameter("barCode" , "1");
        req.addParameter("colorRigidity" , "1");
        req.addParameter("sizeAngel" , "1");
        req.addParameter("name" , "w");
        req.addParameter("nameEnglish" , "1");
        req.addParameter("vendorId" , "1");
        req.addParameter("producingArea" , "1");
        req.addParameter("yearSeason" , "1");
        req.addParameter("productCategory" , "1");
        req.addParameter("productSortId" , "1");
        req.addParameter("unit" , "1");
        req.addParameter("retailUp" , "1");
        req.addParameter("ifImport" , "1");
        req.addParameter("ifConcession" , "1");
        req.addParameter("costUp" , "1");
        req.addParameter("orderLandQtyAccum" , "1");
        req.addParameter("bgtTotalQtyAccum" , "1");
        req.addParameter("storage" , "1");




    }
    public void beginCreate(WebRequest req)
    {
        // Set up HTTP related parameters
        req.setURL("172.16.0.5:7001", "/nds", "/control",
            "/command", null);
        req.addParameter("command", "ProductCreate");
        req.addParameter("id", id);

        req.addParameter("operatorid","0");
        req.addParameter("directoryid","21");

       req.addParameter("no" , "1");
        req.addParameter("oldNo" , "1");
        req.addParameter("barCode" , "1");
        req.addParameter("colorRigidity" , "1");
        req.addParameter("sizeAngel" , "1");
        req.addParameter("name" , "1");
        req.addParameter("nameEnglish" , "1");
        req.addParameter("vendorId" , "1");
        req.addParameter("producingArea" , "1");
        req.addParameter("yearSeason" , "1");
        req.addParameter("productCategory" , "1");
        req.addParameter("productSortId" , "1");
        req.addParameter("unit" , "1");
        req.addParameter("retailUp" , "1");
        req.addParameter("ifImport" , "1");
        req.addParameter("ifConcession" , "1");
        req.addParameter("costUp" , "1");
        req.addParameter("orderLandQtyAccum" , "1");
        req.addParameter("bgtTotalQtyAccum" , "1");
        req.addParameter("storage" , "1");




    }
    public void beginDelete(WebRequest req)
    {
        // Set up HTTP related parameters
        req.setURL("172.16.0.5:7001", "/nds", "/control",
            "/command", null);
        req.addParameter("command", "ProductDelete");
        req.addParameter("id", id);

        req.addParameter("operatorid","0");
        req.addParameter("directoryid","21");

        req.addParameter("no" , "1");
        req.addParameter("oldNo" , "1");
        req.addParameter("barCode" , "1");
        req.addParameter("colorRigidity" , "1");
        req.addParameter("sizeAngel" , "1");
        req.addParameter("name" , "1");
        req.addParameter("nameEnglish" , "1");
        req.addParameter("vendorId" , "1");
        req.addParameter("producingArea" , "1");
        req.addParameter("yearSeason" , "1");
        req.addParameter("productCategory" , "1");
        req.addParameter("productSortId" , "1");
        req.addParameter("unit" , "1");
        req.addParameter("retailUp" , "1");
        req.addParameter("ifImport" , "1");
        req.addParameter("ifConcession" , "1");
        req.addParameter("costUp" , "1");
        req.addParameter("orderLandQtyAccum" , "1");
        req.addParameter("bgtTotalQtyAccum" , "1");
        req.addParameter("storage" , "1");



    }

    public void testCreate()   throws Exception{
        System.out.println("testCreate in ProductTest called");
        askServlet();
    }
    public void endCreate(com.meterware.httpunit.WebResponse res) throws IOException, org.xml.sax.SAXException
    {
        checkSuccess(res);
    }
    public void testModify() throws Exception{
        System.out.println("testModify in ProductTest called");
        askServlet();

    }
    public void endModify(com.meterware.httpunit.WebResponse res) throws IOException, org.xml.sax.SAXException
    {
        checkSuccess(res);
    }
    public void testDelete() throws Exception{
        System.out.println("testDelete in ProductTest called");
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