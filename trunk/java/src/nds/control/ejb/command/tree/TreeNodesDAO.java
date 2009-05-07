/******************************************************************
*
*$RCSfile: TreeNodesDAO.java,v $ $Revision: 1.1.1.1 $ $Author: Administrator $ $Date: 2005/03/15 11:23:15 $
*
*$Log: TreeNodesDAO.java,v $
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
import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;

import nds.util.StringHashtable;

 public class TreeNodesDAO{
    private Vector roots;// elements of TreeNodeHolder
    private StringHashtable names;// key: node.getName()(String), value: TreeNodeHolder
    private boolean ready=false;
    public TreeNodesDAO(){
        roots=new Vector();
        names=new StringHashtable(10);
    }
    public void addNode( TreeNodeHolder node){
        ready=false;
        String parent= node.getParent();
        if(parent==null || parent.trim().equals("")){
            roots.addElement(node);
        }
        names.put(node.getName(), node);
    }
    /**
    * @return vector of elements TreeNodeHolder, only root nodes returned, with children
    * contained
    *
    */
    public Vector getNodes(){
        if( ready) return roots;
        for(int i=0;i< roots.size();i++){
            prepareNode((TreeNodeHolder)roots.elementAt(i));
        }
        ready=true;
        return roots;
    }
    private void prepareNode(TreeNodeHolder root){
            root.removeAllChildren();
            Vector ch=findNodesByParent( root.getName());
            if(ch !=null)for( int i=0;i< ch.size();i++){
                TreeNodeHolder child=(TreeNodeHolder)ch.elementAt(i);
                prepareNode( child);
                child.setParentNode( root);
                root.addChild(child);
            }
    }
    private Vector findNodesByParent(String name){
        Vector v=new Vector();
        Collection c=names.values();
        if( c!=null && c.size()>0){
            Iterator it= c.iterator();
            while(it.hasNext()){
                TreeNodeHolder node=(TreeNodeHolder)it.next();
                if(node.getParent()!=null && node.getParent().equals(name)){
                    v.addElement(node);
                }
            }
        }
        return v;
    }
 }
