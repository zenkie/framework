/******************************************************************
*
*$RCSfile: CommandMappingManagerTest.java,v $ $Revision: 1.1.1.1 $ $Author: Administrator $ $Date: 2005/03/15 11:23:15 $
*
*$Log: CommandMappingManagerTest.java,v $
*Revision 1.1.1.1  2005/03/15 11:23:15  Administrator
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
package nds.control.ejb.test;

import junit.framework.TestCase;
import nds.control.ejb.CommandMappingManager;

public class CommandMappingManagerTest extends TestCase {
    public CommandMappingManagerTest(String name) {
          super(name);
    }
    private CommandMappingManager manager;
    public void testMapping() throws Exception {
        manager = CommandMappingManager.getInstance();
        this.assertEntity("User","UserCreate");
        this.assertEntity("User","UserModify");
        this.assertEntity("User","UserDelete");
        this.assertEntity("Group","GroupCreate");
        this.assertEntity("Group","GroupModify");
        this.assertEntity("Group","GroupDelete");
    }
    private void assertEntity(String entity , String command){
        this.assertEquals(entity,manager.getMappingByCommand(command).getEntity());

    }
}