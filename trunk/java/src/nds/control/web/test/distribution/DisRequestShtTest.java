package nds.control.web.test.distribution;

import java.io.IOException;

import junit.framework.Test;
import junit.framework.TestSuite;
import nds.control.web.MainServlet;
import nds.log.Logger;
import nds.log.LoggerManager;

import org.apache.cactus.ServletTestCase;
import org.apache.cactus.WebRequest;



public class DisRequestShtTest extends ServletTestCase
{
    private static Logger logger= LoggerManager.getInstance().getLogger(DisRequestShtTest.class.getName());
    private String id;
    private MainServlet servlet=null;
    public DisRequestShtTest(String name) {
        super(name);
        id= "100004";// the id of user to be tested on
    }
    public static Test suite() {
        TestSuite suite= new TestSuite("DisRequestShtTest");
        //suite.addTest(new DisRequestShtTest("testCreate"));
        //suite.addTest(new DisRequestShtTest("testModify"));
        //suite.addTest(new DisRequestShtTest("testDelete"));
        suite.addTest(new DisRequestShtTest("testSubmit"));  // add submit
        System.out.println("suite in DisRequestShtTest called");
        return suite;
    }

    public void beginModify(WebRequest req)
    {
        // Set up HTTP related parameters
        req.setURL("172.16.0.5:7001", "/nds", "/control","/command", null);
        req.addParameter("command", "DisRequestShtModify");
        req.addParameter("id", id);

        req.addParameter("no" , "t04");
        req.addParameter("fillerId" , "1");
        req.addParameter("checkerId" , "1");
        req.addParameter("auditorId" , "1");
        req.addParameter("auditorNote" , "1");
        req.addParameter("status" , "1");

    }

    public void beginCreate(WebRequest req)
    {
        // Set up HTTP related parameters
        req.setURL("172.16.0.5:7001", "/nds", "/control",
            "/command", null);
        req.addParameter("command", "DisRequestShtCreate");
        req.addParameter("id", id);

        req.addParameter("no" , "t4");
        req.addParameter("fillerId" , "1");
        req.addParameter("checkerId" , "1");
        req.addParameter("auditorId" , "1");
        req.addParameter("auditorNote" , "1");
        req.addParameter("status" , "1");





    }

    public void beginDelete(WebRequest req)
    {
        // Set up HTTP related parameters
        req.setURL("172.16.0.5:7001", "/nds", "/control",
            "/command", null);
        req.addParameter("command", "DisRequestShtDelete");
        req.addParameter("id", id);

        req.addParameter("no" , "t");
        req.addParameter("fillerId" , "1");
        req.addParameter("checkerId" , "1");
        req.addParameter("auditorId" , "1");
        req.addParameter("auditorNote" , "1");
        req.addParameter("status" , "1");


    }
    public void beginSubmit(WebRequest req)
    {
        // Set up HTTP related parameters
        req.setURL("172.16.0.5:7001", "/nds", "/control","/command", null);
        req.addParameter("command", "DisRequestShtSubmit");
        req.addParameter("id", id);

        req.addParameter("no" , "11");
        req.addParameter("disRequestSht_no","1999");
        req.addParameter("customerNo","Customer008");
        req.addParameter("customerSort","2");
        req.addParameter("customerName","DC2部经销客户A");

        req.addParameter("fillerId" , "1");
        req.addParameter("fillerName","1");
        req.addParameter("checkerId" , "1");
        req.addParameter("auditorId" , "1");
        req.addParameter("auditorNote", "1");


        req.addParameter("productNo","C17-WS170-17XL");
        req.addParameter("requestDisQty","13");
        req.addParameter("realUp","23");
        req.addParameter("note", "1");

        req.addParameter("productNo","C21-WS210-Red75");
        req.addParameter("requestDisQty","15");
        req.addParameter("realUp","3");
        req.addParameter("note", "1");

        req.addParameter("productNo","C19-WS180-19XL");
        req.addParameter("requestDisQty","5");
        req.addParameter("realUp","7");
        req.addParameter("note", "21");

        req.addParameter("productNo","C19-WS180-19XL");
        req.addParameter("requestDisQty","15");
        req.addParameter("realUp","4");
        req.addParameter("note", "14");



    }












    public void testCreate()   throws Exception{
        System.out.println("testCreate in DisRequestShtTest called");
        askServlet();
    }
    public void endCreate(com.meterware.httpunit.WebResponse res) throws IOException, org.xml.sax.SAXException
    {
        checkSuccess(res);
    }
    public void testModify() throws Exception{
        System.out.println("testModify in DisRequestShtTest called");
        askServlet();

    }
    public void testSubmit() throws Exception{
        System.out.println("testSubmit in DisRequestShtTest called");
        askServlet();

    }


    public void endModify(com.meterware.httpunit.WebResponse res) throws IOException, org.xml.sax.SAXException
    {
        checkSuccess(res);
    }
    public void endSubmit(com.meterware.httpunit.WebResponse res) throws IOException, org.xml.sax.SAXException
    {
        checkSuccess(res);
    }

    public void testDelete() throws Exception{
        System.out.println("testDelete in DisRequestShtTest called");
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