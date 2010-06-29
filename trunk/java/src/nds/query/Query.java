/******************************************************************
*
*$RCSfile: Query.java,v $ $Revision: 1.1.1.1 $ $Author: Administrator $ $Date: 2005/03/15 11:23:23 $
*
*$Log: Query.java,v $
*Revision 1.1.1.1  2005/03/15 11:23:23  Administrator
*init
*
*Revision 1.3  2004/02/02 10:42:54  yfzhu
*<No Comment Entered>
*
*Revision 1.2  2003/05/29 19:40:17  yfzhu
*<No Comment Entered>
*
*Revision 1.1.1.1  2002/11/29 02:50:34  yfzhu
*Active POS
*
*Revision 1.2  2001/11/07 20:58:47  yfzhu
*no message
*
*
********************************************************************/
/**
 * @todo The query UI Factory is not funtion well
 */

package nds.query;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nds.log.Logger;
import nds.log.LoggerManager;
import nds.schema.Table;
import nds.schema.TableManager;
/**
 * 查询页面入口
 */
public class Query extends HttpServlet {
    private final static String QUERY_SERVLET="/servlets/query"; // the url of myself

    private static Logger logger= LoggerManager.getInstance().getLogger(Query.class.getName());
    /**
     * 如果request参数中未指明表名，则转到queryportal.jsp，这张页面中将提供所有
     * 可以供查找的表的链接。
     * 如果指明了表，则先从指定目录load表对应的文件（包含显示用的HTML文本），
     * 如果不存在则按照规则创建，并存盘（见QueryUIFactory）。然后显示即可。
     * 如果参数中包含了QueryResult，则将result定向传输到QueryResult.getQueryRequest().getResultHandler()显示。
     *
     * @roseuid 3B84FD9B0002
     */
    public void service(HttpServletRequest req, HttpServletResponse resp) throws IOException,ServletException {
      //TableName is named table_name

      int tableId= nds.util.Tools.getInt(req.getParameter("table"), -1)  ;
      TableManager manager = TableManager.getInstance();

      Table table = manager.findTable(req.getParameter("table"));
      if(table != null)
         tableId = table.getId();

      if(table ==null){
         getServletContext().getRequestDispatcher(nds.util.WebKeys.NDS_URI+"/query/query_portal.jsp").forward(req,resp);
         return;
      }else {
          String tableName = manager.getTable(tableId).getName() ;
          // suppose query_[table_name].jsp file exist
          boolean exist = true;
          // check if the file is exist
/**
 * Following code marked, which is for UI reload
 *
        String temp = getServletContext().getRealPath(WebKeiys.NDS_URI+"/query/query_"+tableName.toLowerCase()+".html");
          logger.debug("The query page of  "+tableName+" stored in "+temp);
          try {
            (new FileReader(temp)).close();
            exist = true;
          }
          catch (FileNotFoundException ex) {
            exist =false;
          }
           if(!exist){// not exist then save it
             nds.query.webbean.SaveJsp saveTool = new nds.query.webbean.SaveJsp();
             saveTool.setfilePath(temp);
             String url = "http://"+req.getServerName()+":"+req.getServerPort()+
                        req.getContextPath()+"/query/query.jsp?table="+tableId;
             saveTool.setUrl(url);
             if(saveTool.saveHttpResult()) logger.debug("Saved");
             else logger.debug("Failed");
           }
*/         if(req.getAttribute("query")!=null){// there has result redirect to result.jsp
                QueryRequestImpl query= (QueryRequestImpl)req.getAttribute("query");
//                QueryResult result=(QueryResult)req.getAttribute("result");
//                QueryRequest query= result.getQueryRequest();
                String handler= query.getResultHandler();
                if( handler ==null || "".equals(handler) || QUERY_SERVLET.equalsIgnoreCase(handler) ) handler=nds.util.WebKeys.NDS_URI+"/query/result.jsp";
                getServletContext().getRequestDispatcher(handler).forward(req,resp);
            }else{
//                 getServletContext().getRequestDispatcher("/servlets/QueryInputHandler").forward(req,resp);
                 getServletContext().getRequestDispatcher(nds.util.WebKeys.NDS_URI+"/query/query.jsp").forward(req,resp);
            }


     }
    }
}
