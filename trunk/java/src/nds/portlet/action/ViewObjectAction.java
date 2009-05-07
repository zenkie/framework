package nds.portlet.action;
import javax.portlet.PortletConfig;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.servlet.jsp.PageContext;

import nds.query.QueryEngine;
import nds.query.QueryRequestImpl;
import nds.query.QueryResult;
import nds.schema.Column;
import nds.schema.Table;
import nds.schema.TableManager;
import nds.util.NDSException;
import nds.util.ParamUtils;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

public class ViewObjectAction extends DefaultPortletAction{
    private final static String IFM_VIEWER="/query/inc_objectview.jsp";
    private final static String NORMAL_VIEWER="/query/objectview.jsp";
    private final static String QUERY_ERRORPAGE="/error.jsp";

    public ViewObjectAction() {
    }
    /**
     * Display one object
     */
	public ActionForward render(
			ActionMapping mapping, ActionForm form, PortletConfig config,
			RenderRequest req, RenderResponse res)
		throws Exception {
			
        int tableId=-1;
        int id=-1;
        try {
            tableId= nds.util.Tools.getInt(req.getParameter("table"), -1)  ;
            TableManager manager= TableManager.getInstance();
            Table table = null;
            if(tableId ==-1) {
                String tn= req.getParameter("table");
                table = manager.getTable(tn);
                if( table ==null)
                	return mapping.findForward("portlet.nds.queryportal");
                    
                tableId= table.getId();
            }else
                table =  manager.getTable(tableId);
            id=nds.util.Tools.getInt(req.getParameter("id"), -1)  ;
            if(id ==-1) {
                return mapping.findForward("portlet.nds.query");
//                res.sendRedirect("../query/query?table="+ tableId);
            }

            QueryRequestImpl query=QueryEngine.getInstance().createRequest(null);
            query.setMainTable(tableId);
            //      3. "actiontype" any of "modify"|"add"|"query"
            query.addAllShowableColumnsToSelection(Column.QUERY_LIST);
            query.addParam(table.getPrimaryKey().getId(),""+id);
            QueryResult result=QueryEngine.getInstance().doQuery(query);
            req.setAttribute("result",result);
            req.setAttribute("id",new Integer(id));
            boolean isInternalFrame= ParamUtils.getBooleanParameter(req,"ifm");
            String viewer=isInternalFrame?"portlet.nds.inc_objectview":"portlet.nds.objectview";
			return mapping.findForward(viewer);
            
            
        } catch (Exception ex) {
        	logger.debug("Error when treating object view request from:"+
                                             "tableid="+tableId+",id="+id, ex);
            // direct to Error page to handle
            NDSException e=new NDSException("Error when treating object view request from:"+
                                            "tableid="+tableId+",id="+id ,ex);
			req.setAttribute(PageContext.EXCEPTION, e);

			return mapping.findForward(COMMON_ERROR);
        }



	}
				

}
