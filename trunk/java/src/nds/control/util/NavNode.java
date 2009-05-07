/******************************************************************
*
*$RCSfile: NavNode.java,v $ $Revision: 1.1.1.1 $ $Author: Administrator $ $Date: 2005/03/15 11:23:15 $
*
*$Log: NavNode.java,v $
*Revision 1.1.1.1  2005/03/15 11:23:15  Administrator
*init
*
*Revision 1.1.1.1  2002/11/29 02:50:34  yfzhu
*Active POS
*
*Revision 1.3  2001/11/20 22:36:09  yfzhu
*no message
*
*Revision 1.2  2001/11/07 20:58:46  yfzhu
*no message
*
*
********************************************************************/
package nds.control.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Enumeration;
import java.util.Vector;

import nds.util.TreeNode;
/**
 * Node show web urls
 */
public class NavNode implements TreeNode, java.io.Serializable {
    protected Vector children=new Vector();// elements of TreeNode
    protected String label;
    protected String url;
    protected String icon;
    protected NavNode parent;
    /**
     * @param label - the label to be displayed on treee
     * @param url - the relative url path of tree node, started from "/"
     * @param icon - the relative icon path of this tree node, started from "/"
     */
    public NavNode(String label, String url, String icon) {
        this.label=label;
        this.url=url;
        this.icon=icon;
        parent=null;


    }
    public NavNode(){

    }
    /**
     * Previously the setter methods are not added, but in nds.control.ejb.command.GetNavigateTree,
     * there's requirement to reconfig url of navnode( using directory.getUrl() to override treeXML.getUrl()
     */
    public void setLabel(String label) {
        this.label  = label;
    }
    public void setURL(String url) {
        this.url=url;
    }
    public void setIcon(String icon) {
        this.icon=icon;
    }
    public String getLabel() {
        return label;
    }
    public String getURL() {
        return url;
    }
    public String getIcon() {
        return icon;
    }
    /**
      * Returns the child <code>TreeNode</code> at index
      * <code>childIndex</code>.
      */
    public TreeNode getChildAt(int childIndex) {
        return (TreeNode)children.elementAt(childIndex);
    }

    /**
     * Returns the number of children <code>TreeNode</code>s the receiver
     * contains.
     */
    public int getChildCount() {
        return children.size();
    }
    public void setParent(NavNode newParent) {
        this.parent= newParent;
    }
    /**
     * Returns the parent <code>TreeNode</code> of the receiver.
     */
    public TreeNode getParent() {
        return parent;
    }

    /**
     * Returns the index of <code>node</code> in the receivers children.
     * If the receiver does not contain <code>node</code>, -1 will be
     * returned.
     */
    public int getIndex(TreeNode node) {
        for(int i=0;i< children.size();i++) {
            TreeNode n=(TreeNode)children.elementAt(i);
            if( n.equals(node)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Returns true if the receiver allows children.
     */
    public boolean getAllowsChildren() {
        return true;
    }

    /**
     * Returns true if the receiver is a leaf.
     */
    public boolean isLeaf() {
        return children.size()==0;
    }

    /**
     * Returns the children of the reciever as an Enumeration.
     */
    public Enumeration children() {
        return children.elements();
    }
    public void addChild(NavNode node) {
        children.addElement(node);
        node.setParent(this);
    }
    public String toString() {
        StringWriter writer=new StringWriter();
        PrintWriter out=new PrintWriter(writer);
        out.println("<node label='"+ getLabel()+"' icon='"+
                    getIcon()+"' url='"+ getURL()+"' >");
        Enumeration enu= children.elements();
        printTree(4,enu, out, "", "");
        out.println("</node>");
        return writer.toString();
    }
    /**
     * @param indent - indent of node display in HTML source
     * @param tree - elements of NavNode
     */
    private void printTree(int indent, Enumeration tree,PrintWriter out, String linkRoot, String iconRoot) {

        while(tree.hasMoreElements()) {
            NavNode node= (NavNode) tree.nextElement();
            out.println(indentString(indent)+"<node label='"+ node.getLabel()+"' icon='"+linkRoot+
                        node.getIcon()+"' url='"+linkRoot+ node.getURL()+"' >");
            if(!node.isLeaf()) {
                printTree( indent+4, node.children(), out, linkRoot, iconRoot);
            }
            out.println( indentString(indent)+"</node>");
        }
    }
    private String indentString(int length) {
        char[] c=new char[length];
        for(int i=0;i<c.length;i++)
            c[i]=' ';
        return new String(c);
    }

}
