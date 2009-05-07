/******************************************************************
*
*$RCSfile: TreeNodeManagerTest.java,v $ $Revision: 1.1.1.1 $ $Author: Administrator $ $Date: 2005/03/15 11:23:15 $
*
*$Log: TreeNodeManagerTest.java,v $
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

import java.util.Vector;

import junit.framework.TestCase;
import nds.control.ejb.command.tree.TreeNodeManager;

public class TreeNodeManagerTest extends TestCase {
    public TreeNodeManagerTest(String name) {
          super(name);
    }
    private TreeNodeManager manager;
    public void testMapping() throws Exception {
        manager = new TreeNodeManager();
        manager.init( getClass().getResource("/tree.xml").toString());
        Vector v= manager.getTreeNodes();
        assertNotNull(v);
        for( int i=0;i<v.size();i++){

        }
    /*    DefaultWebEvent event=new DefaultWebEvent("Command");
        event.setParameter("command", "GetNavigateTree");
        event.setParameter("operatorid", "0");
        event.setParameter("tree.xml", "/tree.xml");
        Command c=new nds.control.ejb.command.GetNavigateTree();
        ValueHolder holder=c.execute(event);
        Vector v=(Vector) holder.get("treenode");
        for( int i=0;i< v.size();i++){
            System.out.println(v.elementAt(i));
        }*/
    }

}