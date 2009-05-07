package nds.control.web.test;
import java.io.File;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.tree.DefaultMutableTreeNode;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import nds.control.web.HelpControl;
import nds.log.Logger;
import nds.log.LoggerManager;

public class HelpControlTest extends TestCase
{
    private static Logger logger= LoggerManager.getInstance().getLogger(HelpControlTest.class.getName());
    HelpControl control;
    private String spaces;
    public HelpControlTest(String name) throws Exception{
        super(name);
        control= new HelpControl();
        File file= new File("c:/tmp/jh1.1/demos/hs/holidays/HolidayHistory.hs");
        control.initHelpSet( file.toURL());
        char[] a=new char[500];
        for(int i=0;i< 500;i++) a[i]=' ';
        spaces=new String(a);
    }
    public static Test suite() {
        TestSuite suite= new TestSuite("HelpControlTest");
        suite.addTestSuite(HelpControlTest.class);
        return suite;
    }
    public static void main(String[] args)throws Exception {
            nds.log.LoggerManager.getInstance().init("/nds.properties");
	    junit.textui.TestRunner.run(suite());
    }
    public void testTOC(){
        DefaultMutableTreeNode node=control.getTOCItems();
        printNode(node,0);
    }

    public void testIndex(){
        DefaultMutableTreeNode node=control.getIndexItems();
        printNode(node,0);

    }

    public void testSearch(){
        Vector v=control.doSearch("CHANUKAH");
        for( int i=0;i< v.size();i++){
            printNode((DefaultMutableTreeNode)v.elementAt(i),0);
        }
    }
    public void testFindID() throws Exception{
        logger.debug(""+control.getHelpURL("hol_intro"));
        logger.debug(""+control.getHelpURL("aprilfools"));
        logger.debug(""+control.getHelpURL("halloween"));
    }
    private void printNode(DefaultMutableTreeNode node,int level){
        System.out.println(spaces.substring(0,level)+node);
        Enumeration enu=node.children();
        while(enu.hasMoreElements()){
            DefaultMutableTreeNode nod= (DefaultMutableTreeNode) enu.nextElement();
            printNode(nod,level +4);
        }
    }
}