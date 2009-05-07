package nds.control.web.test.preferential;

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

public class PreferentialShtTest extends ServletTestCase {
    private static Logger logger = LoggerManager.getInstance().getLogger(PreferentialShtTest.class.getName());
    private String id;
    private MainServlet servlet = null;


    public PreferentialShtTest(String name) {
        super(name);
        /* -- The ID of user to be tested on -- */
        id = "11112";
    }

    public static Test suite() {
        TestSuite suite= new TestSuite("PreferentialShtTest");
        suite.addTest(new PreferentialShtTest("testCreate"));
        //suite.addTest(new PreferentialShtTest("testModify"));
        //suite.addTest(new PreferentialShtTest("testDelete"));
        System.out.println("suite in PreferentialShtTest called");
        return suite;
    }

   public void beginCreate(WebRequest req) {
        /* -- Set up HTTP related parameters -- */
        req.setURL("localhost:7001", "/nds", "/control", "/command", null);
        req.addParameter("command", "PreferentialShtCreate");
        req.addParameter("operatorid","0");
        req.addParameter("directoryid","21");
        req.addParameter("id", id);
        req.addParameter("no","11111");
        req.addParameter("pftBuyerSort_No","2");
        req.addParameter("actionDate","2001/11/21");
        req.addParameter("endDate","2002/11/21");
        req.addParameter("checker_No","Employee001");
        req.addParameter("auditor_No","Employee002");
        req.addParameter("auditorNote","Here is Nothing");
    }

    public void beginModify(WebRequest req) {
        /* -- Set up HTTP related parameters -- */
        req.setURL("localhost:7001", "/nds", "/control","/command", null);
        req.addParameter("command", "PreferentialShtModify");
        req.addParameter("operatorid","0");
        req.addParameter("directoryid","21");
        req.addParameter("id", id);
        req.addParameter("pftBuyerSort_No","2");
        req.addParameter("actionDate","2001/11/22");
        req.addParameter("endDate","2003/11/22");
        req.addParameter("checker_No","Employee000");
        req.addParameter("auditor_No","Employee001");
        req.addParameter("auditorNote","Here is something has been modified");
    }

    public void beginDelete(WebRequest req) {
        /* -- Set up HTTP related parameters -- */
        req.setURL("localhost:7001", "/nds", "/control", "/command", null);
        req.addParameter("command", "PreferentailShtDelete");
        req.addParameter("operatorid","0");
        req.addParameter("directoryid","21");
        req.addParameter("id", id);
        req.addParameter("pftBuyerSort_No","2");
        req.addParameter("actionDate","2001/11/22");
        req.addParameter("endDate","2001/11/22");
        req.addParameter("checker_No","22222");
        req.addParameter("auditor_No","22222");
        req.addParameter("auditorNote","Here is something has been modified");
    }


    public void testCreate() throws Exception{
        System.out.println("testCreate in PreferentialShtTest called");
        askServlet();
    }
    public void endCreate(com.meterware.httpunit.WebResponse res) throws IOException, org.xml.sax.SAXException
    {
        checkSuccess(res);
    }


    public void testModify() throws Exception{
        System.out.println("testModify in PreferentialShtTest called");
        askServlet();
    }
    public void endModify(com.meterware.httpunit.WebResponse res) throws IOException, org.xml.sax.SAXException
    {
        checkSuccess(res);
    }

    public void testDelete() throws Exception{
        System.out.println("testDelete in PreferentialShtTest called");
        askServlet();
    }
    public void endDelete(com.meterware.httpunit.WebResponse res) throws IOException, org.xml.sax.SAXException
    {
        checkSuccess(res);
    }

    public void askServlet() throws Exception{
        /* -- Initialize class to test -- */
        if(servlet == null){
        servlet = new MainServlet();
        /* -- VERY IMPORTANT : Call the init() method in order to initialize the
                               Servlet ServletConfig object. -- */
        servlet.init(config);
        servlet.init();
        }
        servlet.doGet(this.request, this.response);
    }

    public void checkSuccess(com.meterware.httpunit.WebResponse res) throws IOException, org.xml.sax.SAXException{
        String content = res.getText();
        assertTrue(content, res.isHTML());
        String name = res.getTitle();
        boolean b = "信息".equals(name);
        if( !b) logger.error(content);
        assertEquals("信息", name);
    }
}