/******************************************************************
*
*$RCSfile: GetNavigateTree.java,v $ $Revision: 1.1.1.1 $ $Author: Administrator $ $Date: 2005/03/15 11:23:14 $
*
*$Log: GetNavigateTree.java,v $
*Revision 1.1.1.1  2005/03/15 11:23:14  Administrator
*init
*
*Revision 1.3  2004/02/02 10:43:00  yfzhu
*<No Comment Entered>
*
*Revision 1.2  2003/03/30 08:11:58  yfzhu
*Updated before subtotal added
*
*Revision 1.1.1.1  2002/11/29 02:50:34  yfzhu
*Active POS
*
*Revision 1.7  2001/12/28 14:20:01  yfzhu
*no message
*
*Revision 1.6  2002/01/04 01:43:22  yfzhu
*no message
*
*Revision 1.5  2001/12/09 03:43:31  yfzhu
*no message
*
*Revision 1.4  2001/11/29 13:13:14  yfzhu
*no message
*
*Revision 1.3  2001/11/20 22:36:09  yfzhu
*no message
*
*Revision 1.2  2001/11/07 20:58:46  yfzhu
*no message
*
*
********************************************************************/
package nds.control.ejb.command;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Vector;

import nds.control.ejb.Command;
import nds.control.ejb.command.tree.TreeNodeHolder;
import nds.control.ejb.command.tree.TreeNodeManager;
import nds.control.event.DefaultWebEvent;
import nds.control.event.NDSEventException;
import nds.control.util.NavNode;
import nds.control.util.ValueHolder;
import nds.query.QueryEngine;
import nds.security.Directory;
import nds.security.User;
import nds.util.NDSException;
import nds.util.NDSRuntimeException;

/**
 * For nds.control.web.NavTreeServlet to get tree nodes of logged in user
 */
public class GetNavigateTree extends Command {
    private static String GET_PERMISSION="select GetUserPermissionByDirId(?,?) from dual";
    private static String GET_DIR="select id, url from directory where name=?";

    // name: file name of tree.xml to be retrieved
    // value: Vector of tree nodes
    private static Hashtable trees=new Hashtable();
    /**
     * @param event - can has following parameters:
     *     1. tree.xml - the string of file to be retrived out nodes information
     *                   if not specified, "/tree.xml" will be used.
     *     2. reload - "true" for reload from file, others as not reload (yfzhu 2003/11/26)
     *
     * @return valueholder contains a Vector named "treenode", which has elements of NavNode
     */
    public ValueHolder execute(DefaultWebEvent event) throws NDSException, RemoteException {

        User user=helper.getOperator(event);
        String treexml= (String)event.getParameterValue("tree.xml");
        if( treexml ==null)
            try {
                treexml= getClass().getResource("/tree.xml").toString();
            } catch(Exception e) {
                logger.error("Error found", e );
            }
        String reload= (String)event.getParameterValue("reload");
        boolean isToReload= "true".equalsIgnoreCase(reload);
        Vector nodes= (Vector) trees.get(treexml);
        if( nodes ==null || isToReload) {
            // load file
            logger.debug((isToReload?"Reload" :"Load")+" nodes from :"+ treexml);
            TreeNodeManager manager=new TreeNodeManager();
            manager.init(treexml);
            nodes= manager.getTreeNodes();
            trees.put(treexml, nodes);
        }
        try {
            ValueHolder v=new ValueHolder();
            Vector usernodes=getReadableTree( user, nodes);
            /*            StringBuffer buf=new StringBuffer();
                        for( int i=0;i< usernodes.size();i++){
                            buf.append( usernodes.elementAt(i));
                        }
                        logger.debug("Nodes:"+ buf.toString());*/
            v.put("treenode",usernodes );
            return v;
        } catch(Exception e) {
            logger.error("", e);
            throw new NDSEventException("Error found.", e);
        }
    }
    /**
     * Will filter <code>nodes</code>, return only those that the user can read
     * @param nodes Vector of TreeNodeHolder
     * @return Vector of NavNode
     */
    public Vector getReadableTree(User user, Vector nodes) throws Exception {
        Vector v=new Vector();

        Connection con= QueryEngine.getInstance().getConnection();
        PreparedStatement pstmtPerm= null, pstmtDir=null;

        try{
            pstmtPerm=con.prepareStatement(GET_PERMISSION);
            pstmtDir=con.prepareStatement(GET_DIR);

            for(int i=0;i<nodes.size();i++) {
                TreeNodeHolder holder=(TreeNodeHolder) nodes.elementAt(i);
                NavNode node=filter(user,pstmtPerm, pstmtDir , holder);
                if( node !=null)
                    v.addElement(node);
            }
        }finally{
            if(pstmtPerm!=null){try{pstmtPerm.close();}catch(Exception e){}}
            if(pstmtDir!=null){try{pstmtDir.close();}catch(Exception e){}}
            try{con.close();}catch(Exception e){}
        }
        return v;
    }
    /**
     * @return null when holder its self is filtered and no children, or node which
     *      has no child that <code>user</code> can read
     */
    private NavNode filter(User user,PreparedStatement pstmtPerm ,PreparedStatement pstmtDir,
                           TreeNodeHolder holder)throws Exception {
        NavNode node=new NavNode(holder.getLabel(),holder.getURL(),holder.getIcon());
        //logger.debug(node.toString() );
        Vector v= holder.getChildren();
        if( v !=null)
            for( int i=0;i< v.size();i++) {
                NavNode child=filter(user,pstmtPerm, pstmtDir ,  (TreeNodeHolder)v.elementAt(i));
                if( child !=null){
                    node.addChild(child);
                    child.setParent(node);
                }
            }
        if( node.isLeaf()) {
            // check its security and may filter it
            String dirName=holder.getDirectory();
            if( dirName !=null && !dirName.equals("")) {
                    // get directory url, and id
                    int dirId=-1; String dirURL="";
                    ArrayList al= getDirectoryInfo(dirName,pstmtDir);
                    if( al !=null){
                        dirId= ((Integer)al.get(0)).intValue();
                        dirURL= (String) al.get(1);
                    }
                    if(dirId  !=-1) {
                        String info="Found directory(id="+dirId+") named "+ dirName;
                        // skip root check
                        int perm= 0;
                        if( user.getId().intValue() !=0){
                            perm=getPermission(dirId, user.getId().intValue(), pstmtPerm);
                        }else{
                            perm= Directory.WRITE;
                        }
                        if( (perm & Directory.READ) ==0) {
//                            logger.debug(info +", and user has no any permission");
                            // no read and write permission, and is leaf, so return null
                            return null;
                        }
                        // if directory has an url, use it instead of holder's one
                        /**
                         * by Hawkins
                         * 关于url的取值顺序
                         * 1.tree里面node的url,如果为空
                         * 2.数据库Directory表中的对应的url
                         */
                         if( dirURL!=null &&! dirURL.equals("")) {
                        //if((node.getURL() == null || node.getURL().trim().equals(""))&& dir.getUrl()!=null &&! dir.getUrl().trim().equals("")) {
                            node.setURL(dirURL);
//                            logger.debug("Set URL:" + dir.getUrl());
                        }
//                        logger.debug(info +", and user has permission "+ perm);
                    }
            }
        }
        if( holder.getChildren()!=null&& holder.getChildren().size()>0 &&node.getChildCount() <1){
            // since this trunk has no a single child, remove it also
            //logger.debug("prune node:"+ node+" as no children");
            node=null;
        }
        return node;
    }
    /**
     * @return 2 elements, 1 is dirId(Integer) -1 if not found, 2 is dir url
     *  array list will be null if error found
     */
    private ArrayList getDirectoryInfo(String dirName, PreparedStatement pstmt) throws NDSException, java.rmi.RemoteException {
        ResultSet rs=null;
        int dirId=-1; String url="";ArrayList al=null;
        try {
            pstmt.setString(1, dirName);
            rs= pstmt.executeQuery();
            if( rs.next()){
                dirId= rs.getInt(1);
                url= rs.getString(2);
                al=new ArrayList();
                al.add(new Integer(dirId));
                al.add(url);
            }
        } catch (Exception ce) {
            logger.error("Error getting directory id for "+dirName, ce);
            throw new NDSRuntimeException("Error getting DirectoryId",ce);
        }finally{
            if( rs !=null){ try{ rs.close();}catch(Exception e){}}
        }
        return al;
    }
    public int getPermission(int dirId, int userId, PreparedStatement pstmt)throws NDSException, java.rmi.RemoteException {

        ResultSet rs=null;
        try {
            pstmt.setInt(1, dirId);
            pstmt.setInt(2, userId);
            rs= pstmt.executeQuery();
            if( rs.next() ){
                return rs.getInt(1);
            }
        } catch (Exception ce) {
            logger.error("Error getting permission for "+dirId+" of userId="+userId,ce);
            throw new NDSRuntimeException("Error getting permission",ce);
        }finally{
            if( rs !=null){ try{ rs.close();}catch(Exception e){}}
        }
        return 0;
    }

}
