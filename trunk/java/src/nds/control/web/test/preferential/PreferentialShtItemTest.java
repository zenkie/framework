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

public class PreferentialShtItemTest extends ServletTestCase {
    private static Logger logger = LoggerManager.getInstance().getLogger(PreferentialShtItemTest.class.getName());
    private String id;
    private MainServlet servlet = null;


    public PreferentialShtItemTest(String name) {
        super(name);
        /* -- The ID of user to be tested on -- */
        id = "11111";
    }

    public static Test suite() {
        TestSuite suite= new TestSuite("PreferentialShtItemTest");
        suite.addTest(new PreferentialShtItemTest("testCreate"));
        //suite.addTest(new PreferentialShtItemTest("testModify"));
        //suite.addTest(new PreferentialShtItemTest("testDelete"));
        System.out.println("suite in PreferentialShtItemTest called");
        return suite;
    }

   public void beginCreate(WebRequest req) {
        /* -- Set up HTTP related parameters -- */
        req.setURL("localhost:7001", "/nds", "/control", "/command", null);
        req.addParameter("command", "PreferentialShtItemCreate");
        req.addParameter("preferentialShtId","10");
        req.addParameter("id", id);
        req.addParameter("pftProductNo","11111");
        req.addParameter("realUp","11111");
        req.addParameter("gift1No","11111");
        req.addParameter("gift1RealUp","11111");
        req.addParameter("gift1Qty","11111");
        req.addParameter("gift2No","11111");
        req.addParameter("gift2RealUp","11111");
        req.addParameter("gift2Qty","11111");
        req.addParameter("gift3No","11111");
        req.addParameter("gift3RealUp","11111");
        req.addParameter("gift3Qty","11111");
    }

    public void beginModify(WebRequest req) {
        /* -- Set up HTTP related parameters -- */
        req.setURL("localhost:7001", "/nds", "/control", "/command", null);
        req.addParameter("command", "PreferentialShtItemCreate");
        req.addParameter("preferentialShtId","11111");
        req.addParameter("id", id);
        req.addParameter("markNo","22222");
        req.addParameter("pftProductNo","22222");
        req.addParameter("realUp","22222");
        req.addParameter("gift1No","22222");
        req.addParameter("gift1RealUp","22222");
        req.addParameter("gift1Qty","22222");
        req.addParameter("gift2No","22222");
        req.addParameter("gift2RealUp","22222");
        req.addParameter("gift2Qty","22222");
        req.addParameter("gift3No","22222");
        req.addParameter("gift3RealUp","22222");
        req.addParameter("gift3Qty","22222");
    }

    public void beginDelete(WebRequest req) {
        /* -- Set up HTTP related parameters -- */
        req.setURL("localhost:7001", "/nds", "/control", "/command", null);
        req.addParameter("command", "PreferentialShtItemCreate");
        req.addParameter("preferentialShtId","11111");
        req.addParameter("id", id);
        req.addParameter("markNo","22222");
        req.addParameter("pftProductNo","22222");
        req.addParameter("realUp","22222");
        req.addParameter("gift1No","22222");
        req.addParameter("gift1RealUp","22222");
        req.addParameter("gift1Qty","22222");
        req.addParameter("gift2No","22222");
        req.addParameter("gift2RealUp","22222");
        req.addParameter("gift2Qty","22222");
        req.addParameter("gift3No","22222");
        req.addParameter("gift3RealUp","22222");
        req.addParameter("gift3Qty","22222");
    }


    public void testCreate() throws Exception{
        System.out.println("testCreate in PreferentialShtItemTest called");
        askServlet();
    }
    public void endCreate(com.meterware.httpunit.WebResponse res) throws IOException, org.xml.sax.SAXException
    {
        checkSuccess(res);
    }


    public void testModify() throws Exception{
        System.out.println("testModify in PreferentialShtItemTest called");
        askServlet();
    }
    public void endModify(com.meterware.httpunit.WebResponse res) throws IOException, org.xml.sax.SAXException
    {
        checkSuccess(res);
    }

    public void testDelete() throws Exception{
        System.out.println("testDelete in PreferentialShtItemTest called");
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