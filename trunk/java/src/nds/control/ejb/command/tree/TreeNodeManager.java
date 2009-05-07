/******************************************************************
*
*$RCSfile: TreeNodeManager.java,v $ $Revision: 1.1.1.1 $ $Author: Administrator $ $Date: 2005/03/15 11:23:15 $
*
*$Log: TreeNodeManager.java,v $
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
import java.io.InputStream;
import java.net.URL;
import java.util.Vector;

import nds.log.Logger;
import nds.log.LoggerManager;
import nds.util.xml.XmlMapper;
/**
 * URL information loader
 */
public class TreeNodeManager implements java.io.Serializable {

    private static Logger logger= LoggerManager.getInstance().getLogger(TreeNodeManager.class.getName());

    private Vector nodes;

    public TreeNodeManager(){
    }

    public void init(String url) {
        try {
            URL urlm = new URL(url);
            TreeNodesDAO maps=loadTree(urlm.openStream());
            nodes = maps.getNodes();

        } catch (Exception ex) {
            logger.debug("Errors found.", ex);
        }

	}
        /**
         * elemnts of TreeNodeHolder, in tree structure, that is, only roots of nodes will
         * be in vector.
         */
	public Vector getTreeNodes(){
		return nodes;
	}
    private TreeNodesDAO loadTree(InputStream stream) throws Exception{
        TreeNodesDAO maps=new TreeNodesDAO();
	    String dtdURL = "file:" ;
	    XmlMapper xh=new XmlMapper();
            xh.setValidating(true);
	    // By using dtdURL you brake most buildrs ( at least xerces )
	    xh.register("-//Sun Microsystems, Inc.//DTD Web Application 2.2//EN",
			dtdURL );
	    xh.addRule("tree/node", xh.objectCreate("nds.control.ejb.command.tree.TreeNodeHolder") );
	    xh.addRule("tree/node", xh.addChild("addNode", null) ); // remove it from stack when done
	    xh.addRule("tree/node/name", xh.methodSetter("setName", 0) );
	    xh.addRule("tree/node/icon", xh.methodSetter("setIcon",0) );
	    xh.addRule("tree/node/label", xh.methodSetter("setLabel",0) );
	    xh.addRule("tree/node/url", xh.methodSetter("setURL",0) );
	    xh.addRule("tree/node/directory", xh.methodSetter("setDirectory",0) );
	    xh.addRule("tree/node/parent", xh.methodSetter("setParent",0) );
            xh.readXml(stream, maps);
           return maps;
    }



}
