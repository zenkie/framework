/******************************************************************
*
*$RCSfile: TableMappingManagerTest.java,v $ $Revision: 1.1.1.1 $ $Author: Administrator $ $Date: 2005/03/15 11:23:24 $
*
*$Log: TableMappingManagerTest.java,v $
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

public class TableMappingManagerTest extends TestCase {
    public TableMappingManagerTest(String name) {
          super(name);
    }
    private TableMappingManagerTest manager;
    public void testMapping() throws Exception {
        manager = new TableMappingManagerTest("F:\\work2\\mit-nds\\source\\nds\\schema");
    }
}