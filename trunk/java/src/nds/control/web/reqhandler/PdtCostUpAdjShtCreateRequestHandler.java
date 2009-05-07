/******************************************************************
*
*$RCSfile: PdtCostUpAdjShtCreateRequestHandler.java,v $ $Revision: 1.3 $ $Author: Administrator $ $Date: 2005/12/18 14:06:14 $
*
*$Log: PdtCostUpAdjShtCreateRequestHandler.java,v $
*Revision 1.3  2005/12/18 14:06:14  Administrator
*no message
*
*Revision 1.2  2005/04/18 03:28:17  Administrator
*no message
*
*Revision 1.1.1.1  2005/03/15 11:23:16  Administrator
*init
*
*Revision 1.1.1.1  2002/11/29 02:50:34  yfzhu
*Active POS
*
*Revision 1.2  2002/01/04 01:43:22  yfzhu
*no message
*
*Revision 1.1  2001/12/09 03:48:07  yfzhu
*no message
*
*Revision 1.4  2001/11/29 00:48:36  yfzhu
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
package nds.control.web.reqhandler;

import java.util.ArrayList;
import java.util.Locale;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;

import nds.control.event.DefaultWebEvent;
import nds.control.event.NDSEvent;
import nds.control.event.NDSEventException;
import nds.log.Logger;
import nds.log.LoggerManager;
import nds.query.QueryEngine;
import nds.query.QueryException;
import nds.query.QueryRequestImpl;
import nds.schema.Column;
import nds.schema.TableManager;
import nds.util.Tools;

/**
 * 将页面请求转换为一条查询语句交给后台处理
 */
public class PdtCostUpAdjShtCreateRequestHandler extends RequestHandlerSupport {
    private final static int EXCLUDE_VALUE=0;// column.getValues() must be validate, while 0 is default not valid
    private final static int MAX_PARAM_COUNT=30;

    private static Logger logger=LoggerManager.getInstance().getLogger(PdtCostUpAdjShtCreateRequestHandler.class.getName());
    public PdtCostUpAdjShtCreateRequestHandler() {}
    public NDSEvent processRequest(HttpServletRequest request) throws NDSEventException {
        DefaultWebEvent event=new DefaultWebEvent("CommandEvent");
        String s=createSQL(request);

        event.setParameter("sql", s);
        event.setParameter("adjRate", request.getParameter("adjRate"));
        event.setParameter("command", "PdtCostUpAdjShtCreate");
        event.setParameter("directory", request.getParameter("directory"));
        return event;
    }
    private String createSQL(HttpServletRequest req)throws NDSEventException{
        try {
            //logger.debug(toString(req));
            TableManager manager=TableManager.getInstance();
            Locale locale = (Locale)req.getSession(true).getAttribute(org.apache.struts.Globals.LOCALE_KEY);            
            int mainTableId=Tools.getInt(getRequestParameter(req,"table"), -1);
            if( mainTableId == -1) {
                throw new QueryException("Intenal Error: table must be set");
            }
            QueryRequestImpl query=QueryEngine.getInstance().createRequest(null);
            query.setMainTable(mainTableId);


                    query.addSelection(manager.getTable(mainTableId).getPrimaryKey().getId());

                String cs;
                String param;
                int[] ids;

                int paramCount= Tools.getInt(getRequestParameter(req,"param_count"), MAX_PARAM_COUNT);
                for( int i=0;i<paramCount;i++) {
                    param="param/"+i+"/columns";
                    cs=getRequestParameter(req,param);
                    if( cs ==null)
                        continue;
                    ids=parseIntArray(cs);
                    if( ids ==null)
                        throw new QueryException("Intenal Error: can not parse '"+ param +"' to int[]");
                    param="param/"+i+"/value";
                    cs=getRequestParameter(req,param);

                    if( cs !=null && !cs.equals("")) {
                        // mind that GUI may send colum of values
                        Column lastColumn= manager.getColumn(ids[ids.length-1]);
                        if(lastColumn !=null && lastColumn.getValues(locale) !=null) {
                            if( (new Integer(cs.trim()).intValue()) ==EXCLUDE_VALUE) {
                                continue;
                            }
                        }
                        query.addParam(ids,cs);
                    }
                }
            String s=query.toSQL();
            logger.debug("SQL for PdtCostUpAdjShtCreate:"+ s );
            return s;
        } catch (Exception ex) {
            logger.debug("Error creating sql.", ex);
            throw new NDSEventException("Internal Error:"+ex,ex);
        }
    }
    /**
     * Parse <code>s</code> to a boolean vaule, if errors found, return <code>def</code>
     */
    public boolean parseBoolean(String s, boolean def) {
        if( "true".equalsIgnoreCase(s))
            return true;
        else if( "false".equalsIgnoreCase(s))
            return false;
        return def;
    }
    /**
     * Parse <code>s</code> to an int[], s should has following format:
     * "xxx,xxx,..."
     */
    public int[] parseIntArray(String s) {
        try {
            ArrayList is= new ArrayList();
            StringTokenizer st=new StringTokenizer(s,",");
            while(st.hasMoreTokens()) {
                Integer v=new Integer(st.nextToken());
                is.add(v);
            }
            int[] ret=new int[is.size()];
            for(int i=0;i<ret.length;i++) {
                ret[i]=( (Integer)is.get(i)).intValue();
            }
            return ret;
        } catch(Exception e) {
            //logger.debug("can not parse '"+s+"'as int[]");
            return null;
        }
    }
    /**
     * Every element in <code>s</code> should be an int
     */
    public int[] parseIntArray(String[] s) {
        if( s ==null )
            return null;
        try {
            int[] is=new int[s.length];
            for( int i=0;i< s.length;i++) {
                is[i]= (new Integer(s[i])).intValue();
            }
            return is;
        } catch(Exception e) {
            return null;
        }
    }
    private String getRequestParameter(HttpServletRequest req, String param) {
        String s=req.getParameter(param);
        if( s !=null)
            return s.trim();
        return null;
    }

}
