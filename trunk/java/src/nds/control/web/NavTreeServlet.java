/******************************************************************
*
*$RCSfile: NavTreeServlet.java,v $ $Revision: 1.1.1.1 $ $Author: Administrator $ $Date: 2005/03/15 11:23:15 $
*
*$Log: NavTreeServlet.java,v $
*Revision 1.1.1.1  2005/03/15 11:23:15  Administrator
*init
*
*Revision 1.3  2004/02/02 10:42:37  yfzhu
*<No Comment Entered>
*
*Revision 1.2  2002/12/17 05:53:45  yfzhu
*no message
*
*Revision 1.7  2001/12/28 14:20:02  yfzhu
*no message
*
*Revision 1.6  2002/01/04 01:43:22  yfzhu
*no message
*
*Revision 1.5  2001/11/29 00:48:31  yfzhu
*no message
*
*Revision 1.4  2001/11/20 22:36:09  yfzhu
*no message
*
*Revision 1.3  2001/11/16 11:42:40  yfzhu
*no message
*
*Revision 1.2  2001/11/07 20:58:46  yfzhu
*no message
*
*
********************************************************************/
package nds.control.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import nds.control.event.DefaultWebEvent;
import nds.control.util.NavNode;
import nds.control.util.ValueHolder;
import nds.log.Logger;
import nds.log.LoggerManager;
import nds.util.Configurations;
import nds.util.NDSException;
import nds.util.TimeLog;
import nds.util.WebKeys;

public class NavTreeServlet extends HttpServlet {
    private Logger logger=LoggerManager.getInstance().getLogger(NavTreeServlet.class.getName());
    private static final String CONTENT_TYPE = "text/html; charset=GB2312";
    /**Initialize global variables*/
    public void init() throws ServletException {}
    /**Process the HTTP Get request
       parameters that can be set in HttpServletRequest is:
       "reload" - "true" for reloading tree.xml from file system ( normally tree.xml is loaded from file only once and cache to memory)
       "reloadtable" - "true" for reload table definition
     */
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(true);

        //User ID
        int uid = -1;
        try {
            uid = ((UserWebImpl)WebUtils.getSessionContextManager(session).getActor(WebKeys.USER)).getUserId();
        } catch(Exception e) {
            //e.printStackTrace();
        }
        DefaultWebEvent event=new DefaultWebEvent("CommandEvent");
        event.setParameter("command", "GetNavigateTree");
        event.setParameter("operatorid", uid+"");
        event.setParameter("tree.xml", this.getServletContext().getResource("/WEB-INF/xml/tree.xml").toString());
        // only root is allowed to reload tree from disk
        String reload=request.getParameter("reload");
        if(  reload !=null && uid==0) event.setParameter("reload", reload);

        ClientControllerWebImpl controller= (ClientControllerWebImpl)WebUtils.getServletContextManager().getActor( WebKeys.WEB_CONTROLLER );

        // holder contains tree list
        ValueHolder holder=null;
        Vector nodes=null;
        int tid= TimeLog.requestTimeLog("NavTreeServlet.getTreeNodes");
        try {
            holder =controller.handleEvent(event);
            nodes=(Vector)holder.get("treenode");
        } catch(NDSException e) {
            logger.debug("Errors found ", e);
        }finally{
            TimeLog.endTimeLog(tid);
        }
        if( nodes==null)
            nodes=new Vector();

        response.setContentType(CONTENT_TYPE);
        PrintWriter out = response.getWriter();

        String linkRoot, iconRoot;
        URL url= new URL(request.getRequestURL().toString());
        // modified here for demo, yfzhu
        /* marked below
        String root=url.getProtocol()+"://"+ url.getHost();
        if ( url.getPort() !=-1)
            root += ":"+ url.getPort();
        root  ="";
        root += request.getContextPath();
        /*-----marked above----*/
        /*-----added below -----*/
        String root="";//removeStartMark(request.getContextPath());
        // since each nodes have url started with "/", we will remove it
        removeStartMark(nodes.elements());
        /*-----added above -----*/

        linkRoot= root;
        iconRoot= root;

        printTree(2, nodes.elements(), out, linkRoot, iconRoot);
        //printTree2(0, nodes.elements(), out, "");
        out.println("<script language=javascript>var nodes = "+nodes.size()+";</script>");

        // reload table if needed , added by yfzhu 2003-11-26, (I know it's not good putting such code here)
        // only root is allowed to reload tables from disk
        String reloadTable=request.getParameter("reloadtable");
        if(uid==0 && "true".equalsIgnoreCase(reloadTable) ){
            Configurations conf=(Configurations)nds.control.web.WebUtils.getServletContextManager().getActor(nds.util.WebKeys.CONFIGURATIONS);
            Properties props=conf.getConfigurations("schema").getProperties();
            logger.debug("Reload table definitions");
            nds.schema.TableManager.getInstance().init(props, true);
        }
        // end above reload table

    }
    /**
     * Remove start mark "/" in nodes' url and image path
     */
    private void removeStartMark(Enumeration nodes){
        if( nodes ==null) return;
        while( nodes.hasMoreElements()){
            NavNode node= (NavNode)nodes.nextElement();
            node.setIcon(removeStartMark(node.getIcon()));
            node.setURL(removeStartMark(node.getURL()));
            removeStartMark(node.children());
        }
    }
    /**
     * Remove start mark "/" in string
     */
    private String removeStartMark(String s){
        if( s==null) return null;
        int p= s.indexOf('/');
        if( p< 0) return s;
        String before= s.substring(0, p);
        if( before.trim().equals("")){
            // yes, it is started from "/", so we remove it
            return s.substring(p+1);
        }
        return s;
    }
    /**
     * Print the tree as feature list
     * It should has format as output:
     *  parent xx-xx-xx, parent and children are sepereated by "-"
     *  such as:
     *      01, 订货入库
     *      0101，订货预算
     *      0102，订货到货通知
     *      02，退货入库
     *      ...
     */
    private void printTree2(int indent, Enumeration tree,PrintWriter out, String parentNumber ) {
        int i = 1;
        char[] c=new char[indent];
        for(int j=0;j< c.length;j++) c[j]=' ';
        String ind=new String(c);
        while(tree.hasMoreElements()) {
            NavNode node= (NavNode) tree.nextElement();
            out.println(parentNumber+ formatNumber(i)+",\""+ ind+node.getLabel()+"\"");
            if(node.isLeaf()) {
                //out.println(parentNumber+ formatNumber(i)+","+ node.getLabel());
            }else{
                printTree2( indent+2, node.children(), out, parentNumber+ formatNumber(i));
            }
            i++;
        }
    }
    /**
     *
     * @return string of int in format
     * 00
     */
    private String formatNumber(int num){
        return intFormatter.format(num);
    }
    private final static java.text.DecimalFormat intFormatter=new java.text.DecimalFormat("00") ;

    /**
     * @param indent - indent of node display in HTML source
     * @param tree - elements of NavNode
     */
    private void printTree(int indent, Enumeration tree,PrintWriter out, String linkRoot, String iconRoot) {
        int i = 1;
        while(tree.hasMoreElements()) {
            NavNode node= (NavNode) tree.nextElement();
            if(node.isLeaf()) {
                if(node.getParent() == null){
                  out.println("<div id=el"+i+"Parent class=parent>" + indentString(indent)
                             +" <img src=\""+iconRoot + node.getIcon()+"\" border=0> "
                             +"<a onclick=m(href) href=\""+linkRoot + node.getURL()+"\">"
                             +node.getLabel()
                             +"</a><br></div>");
                  out.println((node.getParent() == null)?"<div id=el"+i+"Child class=child></div>":"");
                }else{
                  out.println(indentString(indent)
                             +" <img src=\""+iconRoot + node.getIcon()+"\" border=0> "
                             +"<a onclick=m(href) href=\""+linkRoot + node.getURL()+"\">"
                             +node.getLabel()
                             +"</a><br>");
                }
            }else{
                out.println("<div id=el"+i+"Parent class=parent>"
                           +indentString(indent)+"<a href=# onclick=expand("+i+");return(false)>"
                           +"<img name=img"+i+" id=img"+i+" src=\""+iconRoot + "images/NDS_folder_close.gif\""+" border=0></a>"
                           +" <img src=\""+iconRoot + node.getIcon()+"\" border=0> "
                           +"<a onclick=m(href) href=\""+linkRoot + node.getURL()+"\">"+node.getLabel()+"</a>"
                           +"</div>"
                           );
                /*out.println(indentString(indent)+"<div id=el"+i+"Parent class=parent><table border=0><tr><td>"
                           +"<a href=# onclick=expand("+i+");return(false)>"
                           +"<img name=img"+i+" src="+iconRoot + "/images/NDS_folder_close.gif"+" border=0></a>"
                           +"<img src="+iconRoot + node.getIcon()+" border=0> "
                           +"<a onclick=m(href) href="+linkRoot + node.getURL()+">"+node.getLabel()+"</a>"
                           +"</td></tr></table></div>"
                           );*/
                out.println("<div id=el"+i+"Child class=child>");
                printTree( indent+2, node.children(), out, linkRoot, iconRoot);
                out.println("</div>");
            }
            i++;
            /*out.println(indentString(indent)+"<node label='"+ node.getLabel()+"' icon='"+linkRoot+
                        node.getIcon()+"' url='"+linkRoot+ node.getURL()+"' >");
            if(!node.isLeaf()) {
                printTree( indent+4, node.children(), out, linkRoot, iconRoot);
            }
            out.println( indentString(indent)+"</node>");
            */
        }
    }
    private String indentString(int length) {//Modified by Hawke
        String c = "";
        for(int i=0;i<length;i++)
            c += "&nbsp;";
        return c;
    }
    /**Clean up resources*/
public void destroy() {}
}
