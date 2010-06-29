/******************************************************************
*
*$RCSfile: ObjectViewer.java,v $ $Revision: 1.3 $ $Author: Administrator $ $Date: 2005/08/28 00:27:04 $
*
*$Log: ObjectViewer.java,v $
*Revision 1.3  2005/08/28 00:27:04  Administrator
*no message
*
*Revision 1.2  2005/04/18 03:28:18  Administrator
*no message
*
*Revision 1.1.1.1  2005/03/15 11:23:23  Administrator
*init
*
*Revision 1.1.1.1  2002/11/29 02:50:34  yfzhu
*Active POS
*
*Revision 1.4  2001/12/09 03:43:32  yfzhu
*no message
*
*Revision 1.3  2001/11/22 06:28:18  yfzhu
*no message
*
*Revision 1.2  2001/11/07 20:58:47  yfzhu
*no message
*
*
********************************************************************/
//Source file: F:\\work2\\tmp\\nds\\query\\ObjectViewer.java

package nds.query;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import nds.control.web.*;
import nds.log.Logger;
import nds.log.LoggerManager;
import nds.schema.Column;
import nds.schema.Table;
import nds.schema.TableManager;
import nds.util.NDSException;
import nds.util.ParamUtils;
import nds.util.WebKeys;
/**
 * Display one object, needed parameter:
 *      1. "table" should be id of Table
 *      2. "id"    should be id of object
 *
 */
public class ObjectViewer extends HttpServlet {
    private static Logger logger=LoggerManager.getInstance().getLogger(ObjectViewer.class.getName());
    private final static String IFM_VIEWER=nds.util.WebKeys.NDS_URI+"/query/inc_objectview.jsp";
    private final static String NORMAL_VIEWER=nds.util.WebKeys.NDS_URI+"/query/objectview.jsp";
    private final static String QUERY_ERRORPAGE=nds.util.WebKeys.NDS_URI+"/error.jsp";
    /**
     * Display one object
     */
    public void service(HttpServletRequest req, HttpServletResponse res) throws IOException,ServletException {
        int tableId=-1;
        int id=-1;
        try {
            TableManager manager= TableManager.getInstance();
            Table table = manager.findTable(req.getParameter("table"));
            if( table ==null){
                res.sendRedirect(nds.util.WebKeys.NDS_URI+"/query/query_portal.jsp");
                return;
            }
            tableId= table.getId();
            
            id=nds.util.Tools.getInt(req.getParameter("id"), -1)  ;
            if(id ==-1) {
                res.sendRedirect(nds.util.WebKeys.NDS_URI+"/query/query?table="+ tableId);
                return;
            }
		UserWebImpl userWeb = ((UserWebImpl)WebUtils.getSessionContextManager(req.getSession(true)).getActor(nds.util.WebKeys.USER));	
            QueryRequestImpl query=QueryEngine.getInstance().createRequest(userWeb.getSession());
            query.setMainTable(tableId);
            //      3. "actiontype" any of "modify"|"add"|"query"
            query.addAllShowableColumnsToSelection(Column.QUERY_LIST);
            query.addParam(table.getPrimaryKey().getId(),""+id);
            QueryResult result=QueryEngine.getInstance().doQuery(query);
            req.setAttribute("result",result);
            req.setAttribute("id",new Integer(id));
            boolean isInternalFrame= ParamUtils.getBooleanParameter(req,"ifm");
            String viewer=isInternalFrame?IFM_VIEWER:NORMAL_VIEWER;

            this.getServletContext().getRequestDispatcher(viewer).forward(req,res);
            return;
        } catch (Exception ex) {
            // direct to Error page to handle
            NDSException e=new NDSException("Error when treating object view request from:"+
                                            req.getRequestURL() + "tableid="+tableId+",id="+id ,ex);
            req.setAttribute("error",ex);
            // there has no flow for this page, direct it to unknown page
            String errorURL= this.QUERY_ERRORPAGE;
            getServletContext().getRequestDispatcher(errorURL).forward(req,res);
        }


    }
    private String getRequestParameter(HttpServletRequest req, String param) {
        String s=req.getParameter(param);
        if( s !=null)
            return s.trim();
        return null;
    }

}
