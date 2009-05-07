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

public class CustIncomeShtTest extends ServletTestCase {
    private static Logger logger= LoggerManager.getInstance().getLogger(CustIncomeShtTest.class.getName());
    private String id;
    private MainServlet servlet=null;


    public CustIncomeShtTest(String name) {
        super(name);
        id= "111";// the id of user to be tested on
    }
    public static Test suite() {
        TestSuite suite= new TestSuite("CustIncomeShtTest");
//        suite.addTest(new CustIncomeShtTest("testCreate"));
//        suite.addTest(new CustIncomeShtTest("testModify"));
        suite.addTest(new CustIncomeShtTest("testDelete"));
        System.out.println("suite in CustIncomeShtTest called");
        return suite;
    }

    public void beginModify(WebRequest req)
    {
        // Set up HTTP related parameters
        req.setURL("localhost:9001", "/nds", "/control","/command", null);
        req.addParameter("command", "CustIncomeShtModify");
        req.addParameter("id", id);
        req.addParameter("operatorid","0");
        req.addParameter("directoryid","21");


        req.addParameter("customerNo","1111");
        req.addParameter("checkNo","ghjgdh54");
        req.addParameter("auditorNo","1111");
        req.addParameter("auditorNote","Here is nnn,egeg,Nothing");

    }


   public void beginCreate(WebRequest req)
    {
        // Set up HTTP related parameters
        req.setURL("localhost:9001", "/nds", "/control",
            "/command", null);
        req.addParameter("command", "CustIncomeShtCreate");
        req.addParameter("operatorid","0");
        req.addParameter("directoryid","21");

        req.addParameter("id", id);

        req.addParameter("no","1121");
        req.addParameter("fillerId","1111");
        req.addParameter("customerNo","1111");
        req.addParameter("checkNo","1111");
        req.addParameter("auditorNo","1111");
        req.addParameter("auditorNote","Here is Nothing");


    }

    public void beginDelete(WebRequest req)
    {
        // Set up HTTP related parameters
        req.setURL("localhost:9001", "/nds", "/control",
            "/command", null);
        req.addParameter("command", "CustIncomeShtDelete");
        req.addParameter("id", id);
        req.addParameter("operatorid","0");
        req.addParameter("directoryid","21");


        req.addParameter("customerNo","1111");
        req.addParameter("checkNo","1111");
        req.addParameter("auditorNo","1111");
        req.addParameter("auditorNote","Here is Nothing");


    }


    public void ertestCreate()   throws Exception{
        System.out.println("testCreate in CustIncomeShtTest called");
        askServlet();
    }
    public void endCreate(com.meterware.httpunit.WebResponse res) throws IOException, org.xml.sax.SAXException
    {
        checkSuccess(res);
    }


    public void testModify() throws Exception{
        System.out.println("testModify in CustIncomeShtTest called");
        askServlet();

    }
    public void endModify(com.meterware.httpunit.WebResponse res) throws IOException, org.xml.sax.SAXException
    {
        checkSuccess(res);
    }
    public void testDelete() throws Exception{
        System.out.println("testDelete in CustIncomeShtTest called");
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