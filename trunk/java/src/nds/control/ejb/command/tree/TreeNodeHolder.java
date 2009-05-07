/******************************************************************
*
*$RCSfile: TreeNodeHolder.java,v $ $Revision: 1.1.1.1 $ $Author: Administrator $ $Date: 2005/03/15 11:23:15 $
*
*$Log: TreeNodeHolder.java,v $
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
package nds.control.ejb.command.tree;
import java.util.Vector;

import nds.control.util.NavNode;
public class TreeNodeHolder {//implements java.io.Serializable, mark it to disallow transfer over net{

    private String name=null;
    private String label=null;
    private String icon=null;
    private String url=null;
    private String directory=null;
    private String parent=null;
    private TreeNodeHolder parentNode=null;
    private Vector children;
    public TreeNodeHolder(){
        children=new Vector();
    }
    public void setName(String name){
        this.name=name;
    }
    public void setLabel(String label){
        this.label=label;
    }
    public void setURL(String url){
        this.url=url;
    }
    public void setDirectory(String directory){
        this.directory=directory;
    }
    public void setIcon(String icon){
        this.icon=icon;
    }
    public void setParent(String parent){
        this.parent=parent;
    }
    public void setParentNode(TreeNodeHolder node){
        this.parentNode=node;
    }
    public  void addChild(TreeNodeHolder node){
        children.addElement(node);
    }
    public void removeAllChildren(){
        children.removeAllElements();
    }
    public Vector getChildren(){
        return children;
    }
    public String getName(){
        return name;
    }
    public String getLabel(){
        return label;
    }
    public String getIcon(){
        return icon;
    }
    public String getParent(){
        return parent;
    }
    public TreeNodeHolder getParentNode(){
        return parentNode;
    }
    public String getDirectory(){
        return directory;
    }
    public String getURL(){
        return url;
    }
    public NavNode toNavNode(){
		return new NavNode(label, url, icon);
    }
}
