package nds.control.web.test.income;
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

public class CustIncomeShtItemTest extends ServletTestCase {

    private static Logger logger= LoggerManager.getInstance().getLogger(CustIncomeShtItemTest.class.getName());
    private String id;
    private MainServlet servlet=null;


    public CustIncomeShtItemTest(String name) {
        super(name);
  //      id= "1";// the id of user to be tested on
    }
    public static Test suite() {
        TestSuite suite= new TestSuite("CustIncomeShtItemTest");
        suite.addTest(new CustIncomeShtItemTest("testCreate"));
//        suite.addTest(new CustIncomeShtItemTest("testModify"));
//        suite.addTest(new CustIncomeShtItemTest("testDelete"));
        System.out.println("suite in CustIncomeShtItemTest called");
        return suite;
    }

    public void beginModify(WebRequest req)
    {
        // Set up HTTP related parameters
        req.setURL("localhost:7001", "/nds", "/control","/command", null);
        req.addParameter("command", "CustIncomeShtItemModify");
        req.addParameter("id", "10003");
         req.addParameter("id", "10004");
//        req.addParameter("operatorid","0");
//        req.addParameter("directoryid","21");


        req.addParameter("billType","modi1");
        req.addParameter("billType","modi2");
        req.addParameter("billNo","modi-001");
        req.addParameter("billNo","modi-002");
        req.addParameter("incomeAmt","123");
        req.addParameter("incomeAmt","456");
        req.addParameter("note","modi-test");
        req.addParameter("note","modi-test2");

    }


   public void beginCreate(WebRequest req)
    {
        // Set up HTTP related parameters
        req.setURL("localhost:7001", "/nds", "/control",
            "/command", null);
        req.addParameter("command", "CustIncomeShtItemCreate");
//        req.addParameter("operatorid","0");
//        req.addParameter("directoryid","21");

//        req.addParameter("id", id);
        req.addParameter("custIncomeShtId","44");
        req.addParameter("markNo","1");
        req.addParameter("markNo","2");
        req.addParameter("billType","10");
        req.addParameter("billType","11");
        req.addParameter("billNo","10001");
        req.addParameter("billNo","10002");
        req.addParameter("incomeAmt","9000");
        req.addParameter("incomeAmt","7001");
        req.addParameter("note","test");
        req.addParameter("note","test2");


    }

    public void beginDelete(WebRequest req)
    {
        // Set up HTTP related parameters
        req.setURL("localhost:7001", "/nds", "/control",
            "/command", null);
        req.addParameter("command", "CustIncomeShtItemDelete");
        req.addParameter("id", "10003");
        req.addParameter("id", "10004");
//        req.addParameter("operatorid","0");
//        req.addParameter("directoryid","21");


    }


    public void testCreate()   throws Exception{
        System.out.println("testCreate in CustIncomeShtItemTest called");
        askServlet();
    }
    public void endCreate(com.meterware.httpunit.WebResponse res) throws IOException, org.xml.sax.SAXException
    {
        checkSuccess(res);
    }


    public void stestModify() throws Exception{
        System.out.println("testModify in CustIncomeShtItemTest called");
        askServlet();

    }
    public void endModify(com.meterware.httpunit.WebResponse res) throws IOException, org.xml.sax.SAXException
    {
        checkSuccess(res);
    }
    public void stestDelete() throws Exception{
        System.out.println("testDelete in CustIncomeShtItemTest called");
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