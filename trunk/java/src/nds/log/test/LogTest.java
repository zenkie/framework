/******************************************************************
*
*$RCSfile: LogTest.java,v $ $Revision: 1.1.1.1 $ $Author: Administrator $ $Date: 2005/03/15 11:23:18 $
*
*$Log: LogTest.java,v $
*Revision 1.1.1.1  2005/03/15 11:23:18  Administrator
*init
*
*Revision 1.2  2004/02/02 10:42:32  yfzhu
*<No Comment Entered>
*
*Revision 1.1.1.1  2002/11/29 02:50:34  yfzhu
*Active POS
*
*Revision 1.2  2001/11/07 20:58:47  yfzhu
*no message
*
*
********************************************************************/
package nds.log.test;
import nds.log.ConsoleLogger;
import nds.log.Logger;
/**
 * Title:        NDS Project
 * Description:  San gao shui yuan, mu xiang ren jia
 * Copyright:    Copyright (c) 2001
 * Company:      aic
 * @author yfzhu
 * @version 1.0
 */

public class LogTest extends junit.framework.TestCase {

    public LogTest(String name) {
        super(name);
    }
    public void testWebLogicLogger(){
        System.setProperty("weblogic.log.FileName", "c:/weblogicloggertest.log" );
        /*WebLogicLogger logger=new WebLogicLogger();
        logger.init("Test", null, "128");
        logTest(logger);*/
    }
    private void logTest(Logger logger){
        logger.debug("First");
        Exception e=new Exception("Test Exception");
        logger.debug("Second", e);
        logger.error("Third");
        logger.error("Forth", e);
        logger.info("Fifth");
        logger.info("Sixth", e);

    }
    public void testConsoleLogger(){
        ConsoleLogger logger=new ConsoleLogger("ConsoleTest");
        logger.init("Console", null, "128");
        logTest(logger);
    }
}