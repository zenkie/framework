/******************************************************************
*
*$RCSfile: OracleTypeConverterTest.java,v $ $Revision: 1.1.1.1 $ $Author: Administrator $ $Date: 2005/03/15 11:23:24 $
*
*$Log: OracleTypeConverterTest.java,v $
*Revision 1.1.1.1  2005/03/15 11:23:24  Administrator
*init
*
*Revision 1.1.1.1  2002/11/29 02:50:35  yfzhu
*Active POS
*
*Revision 1.2  2001/11/07 20:58:47  yfzhu
*no message
*
*
********************************************************************/
package nds.schema.test;
import junit.framework.TestCase;
import nds.log.Logger;
import nds.log.LoggerManager;
import nds.schema.OracleTypeConverter;
/**
 * Title:        NDS Project
 * Description:  San gao shui yuan, mu xiang ren jia
 * Copyright:    Copyright (c) 2001
 * Company:      aic
 * @author yfzhu
 * @version 1.0
 */

public class OracleTypeConverterTest extends TestCase {
    private Logger logger=LoggerManager.getInstance().getLogger(OracleTypeConverterTest.class.getName());
    public OracleTypeConverterTest(String name) {
        super(name);
    }
    public void testAll()throws Exception{
        OracleTypeConverter c=new OracleTypeConverter();
        show(c.convert("number(10,1)"));
        show(c.convert("number(9)"));
        show(c.convert("date"));
        show(c.convert("smallint"));
    }
    private void show(Object obj){
        //logger.debug(""+obj);
    }
}