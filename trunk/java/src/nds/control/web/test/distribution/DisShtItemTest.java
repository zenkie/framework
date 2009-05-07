package nds.control.web.test.distribution;



import java.io.IOException;

import junit.framework.Test;
import junit.framework.TestSuite;
import nds.control.web.MainServlet;
import nds.log.Logger;
import nds.log.LoggerManager;

import org.apache.cactus.ServletTestCase;
import org.apache.cactus.WebRequest;



public class DisShtItemTest extends ServletTestCase
{
    private static Logger logger= LoggerManager.getInstance().getLogger(DisShtItemTest.class.getName());
    private String id;
    private MainServlet servlet=null;
    public DisShtItemTest(String name) {
        super(name);
       // id= "100003";// the id of user to be tested on
    }
    public static Test suite() {
        TestSuite suite= new TestSuite("DisShtItemTest");
        suite.addTest(new DisShtItemTest("testCreate"));
        suite.addTest(new DisShtItemTest("testModify"));
        //suite.addTest(new DisShtItemTest("testDelete"));
        System.out.println("suite in DisShtItemTest called");
        return suite;
    }

    public void beginModify(WebRequest req)
    {
        // Set up HTTP related parameters
        req.setURL("172.16.0.5:7001", "/nds", "/control","/command", null);
        req.addParameter("command", "DisShtItemModify");
      //  req.addParameter("id", id);

        req.addParameter("id", "17100");
        req.addParameter("productNo" , "1");
        req.addParameter("requestDisQty" , "21");
        req.addParameter("disQty" ,"20");
        req.addParameter("realUp" ,"13");
        req.addParameter("note" , "sjjjjjjjjs1");

        req.addParameter("id", "17101");
        req.addParameter("productNo" , "2");
        req.addParameter("requestDisQty" , "12");
        req.addParameter("disQty" ,"23");
        req.addParameter("realUp" ,"1");
        req.addParameter("note" , "33ddddddfff2231");

        req.addParameter("id", "17102");
        req.addParameter("productNo" , "3");
        req.addParameter("requestDisQty" , "15");
        req.addParameter("disQty" ,"27");
        req.addParameter("realUp" ,"11");
        req.addParameter("note" , "vvvvvvvvvvv");

    }

    public void beginCreate(WebRequest req)
    {
        // Set up HTTP related parameters
        req.setURL("172.16.0.5:7001", "/nds", "/control",
            "/command", null);
        req.addParameter("command", "DisShtItemCreate");

        req.addParameter("objectid" , "10117");

        req.addParameter("id", "17100");
        req.addParameter("productNo" , "1");
        req.addParameter("requestDisQty" , "21");
        req.addParameter("disQty" ,"20");
        req.addParameter("realUp" ,"13");
        req.addParameter("note" , "sjjjjjjjjs1");

        req.addParameter("id", "17101");
        req.addParameter("productNo" , "2");
        req.addParameter("requestDisQty" , "12");
        req.addParameter("disQty" ,"23");
        req.addParameter("realUp" ,"1");
        req.addParameter("note" , "33ddddddfff2231");

        req.addParameter("id", "17102");
        req.addParameter("productNo" , "3");
        req.addParameter("requestDisQty" , "15");
        req.addParameter("disQty" ,"27");
        req.addParameter("realUp" ,"11");
        req.addParameter("note" , "vvvvvvvvvvv");





    }

    public void beginDelete(WebRequest req)
    {
        // Set up HTTP related parameters
        req.setURL("172.16.0.5:7001", "/nds", "/control",
            "/command", null);
        req.addParameter("command", "DisShtItemDelete");

        req.addParameter("id", "17100");
        req.addParameter("productNo" , "1");
        req.addParameter("requestDisQty" , "21");
        req.addParameter("disQty" ,"20");
        req.addParameter("realUp" ,"13");
        req.addParameter("note" , "sjjjjjjjjs1");

        req.addParameter("id", "17101");
        req.addParameter("productNo" , "2");
        req.addParameter("requestDisQty" , "12");
        req.addParameter("disQty" ,"23");
        req.addParameter("realUp" ,"1");
        req.addParameter("note" , "33ddddddfff2231");

        req.addParameter("id", "17102");
        req.addParameter("productNo" , "3");
        req.addParameter("requestDisQty" , "15");
        req.addParameter("disQty" ,"27");
        req.addParameter("realUp" ,"11");
        req.addParameter("note" , "vvvvvvvvvvv");


    }


    public void testCreate()   throws Exception{
        System.out.println("testCreate in DisShtItemTest called");
        askServlet();
    }
    public void endCreate(com.meterware.httpunit.WebResponse res) throws IOException, org.xml.sax.SAXException
    {
        checkSuccess(res);
    }
    public void testModify() throws Exception{
        System.out.println("testModify in DisShtItemTest called");
        askServlet();

    }


    public void endModify(com.meterware.httpunit.WebResponse res) throws IOException, org.xml.sax.SAXException
    {
        checkSuccess(res);
    }

    public void testDelete() throws Exception{
        System.out.println("testDelete in DisShtItemTest called");
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