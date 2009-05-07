/******************************************************************
*
*$RCSfile: URLMappingManagerTest.java,v $ $Revision: 1.1.1.1 $ $Author: Administrator $ $Date: 2005/03/15 11:23:16 $
*
*$Log: URLMappingManagerTest.java,v $
*Revision 1.1.1.1  2005/03/15 11:23:16  Administrator
*init
*
*Revision 1.1.1.1  2002/11/29 02:50:34  yfzhu
*Active POS
*
*Revision 1.2  2001/11/07 20:58:46  yfzhu
*no message
*
*
********************************************************************/
package nds.control.web.test;

import java.io.File;
import java.net.URL;

import junit.framework.TestCase;
import nds.control.web.URLMapping;
import nds.control.web.URLMappingManager;

public class URLMappingManagerTest extends TestCase {
    public URLMappingManagerTest(String name) {
          super(name);
    }
    public void testHugeMapping() throws Exception {
        String fileName= "f:/urlmappings.xml";
        int urlCount =1000, screenCount=1000;
        // generate file

        //---------------------un comment this if you have no such file----------------

//        URLMappingXMLGenerator gen= new URLMappingXMLGenerator(fileName, urlCount,screenCount);

        // do test
        URLMappingManager manager= new URLMappingManager();
        File file=new File(fileName);
        URL url=null;
        try{
            url=file.toURL();
        }catch(Exception e){
            this.assertTrue("Found Exception:"+ e, true);
            e.printStackTrace();
        }

        manager.init(url.toString());
        for( int i=1;i< screenCount+1;i++){
            URLMapping map= manager.getMappingByScreen("SCREEN"+i);
            this.assertNotNull("Not found SCREEN"+ i+" screen in "+ url,map );
        }
        for( int i=1;i< urlCount +1;i++){
            URLMapping map= manager.getMappingByURL("URL"+i);
            this.assertNotNull("Not found URL"+ i+" url in "+ url,map );
        }

    }
    public void testCorrect(){
        String fileName="f:/urlmappings.xml.bak";
        // do test
        URLMappingManager manager= new URLMappingManager();
        File file=new File(fileName);
        URL url=null;
        try{
            url=file.toURL();
        }catch(Exception e){
            this.assertTrue("Found Exception:"+ e, true);
            e.printStackTrace();
        }

        manager.init(url.toString());
        URLMapping map= manager.getMappingByURL("URL0");
        this.assertEquals("SCREEN0", map.getScreen());
        this.assertEquals("com.farm.req.Handler1", map.getRequestHandler());
        this.assertEquals("com.farm.flow.Handle1", map.getFlowHandler());
        this.assertEquals(false, map.isSecured());

         map= manager.getMappingByScreen("SCREEN1");
        this.assertEquals("URL1", map.getURL());
        this.assertNull(map.getRequestHandler());
        this.assertEquals("java:comp/env/event/TestEvent", map.getEvent());
        this.assertNull(map.getFlowHandler());
        this.assertEquals("SCREEN1", map.getNextScreen());
        this.assertEquals(true, map.isSecured());

        map= manager.getMappingByURL("URL2");
        this.assertEquals("SCREEN2", map.getScreen());
        this.assertNull(map.getRequestHandler());
        this.assertEquals("java:comp/env/event/ModemEvent", map.getEvent());
        this.assertNull(map.getFlowHandler());
        this.assertEquals("SCREEN34", map.getNextScreen("save"));
        this.assertEquals("SCREEN3", map.getNextScreen("insert"));
        this.assertEquals(false, map.isSecured());

    }

}