/******************************************************************
*
*$RCSfile: QueryResultImpl.java,v $ $Revision: 1.5 $ $Author: Administrator $ $Date: 2005/12/18 14:06:16 $
*
*$Log: QueryResultImpl.java,v $
*Revision 1.5  2005/12/18 14:06:16  Administrator
*no message
*
*Revision 1.4  2005/11/16 02:57:21  Administrator
*no message
*
*Revision 1.3  2005/05/27 05:01:49  Administrator
*no message
*
*Revision 1.2  2005/03/23 17:56:02  Administrator
*no message
*
*Revision 1.1.1.1  2005/03/15 11:23:23  Administrator
*init
*
*Revision 1.6  2004/02/02 10:42:54  yfzhu
*<No Comment Entered>
*
*Revision 1.5  2003/05/29 19:40:18  yfzhu
*<No Comment Entered>
*
*Revision 1.4  2003/04/03 09:28:21  yfzhu
*<No Comment Entered>
*
*Revision 1.3  2003/03/30 08:11:53  yfzhu
*Updated before subtotal added
*
*Revision 1.2  2002/12/17 08:45:37  yfzhu
*no message
*
*Revision 1.1.1.1  2002/11/29 02:50:34  yfzhu
*Active POS
*
*Revision 1.1.1.1  2002/01/08 03:40:23  Administrator
*My new CVS module.
*
*Revision 1.8  2001/12/28 14:20:02  yfzhu
*no message
*
*Revision 1.7  2002/01/04 01:43:22  yfzhu
*no message
*
*Revision 1.6  2001/12/09 03:43:32  yfzhu
*no message
*
*Revision 1.5  2001/11/20 22:36:09  yfzhu
*no message
*
*Revision 1.4  2001/11/14 23:31:27  yfzhu
*no message
*
*Revision 1.3  2001/11/11 12:45:39  yfzhu
*no message
*
*Revision 1.2  2001/11/07 20:58:47  yfzhu
*no message
*
*
********************************************************************/
//Source file: F:\\work2\\tmp\\nds\\query\\QueryResultImpl.java

package nds.query;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;

import org.json.*;

import nds.log.Logger;
import nds.log.LoggerManager;
import nds.schema.Column;
import nds.schema.SQLTypes;
import nds.schema.SumMethodFactory;
import nds.schema.TableManager;
import nds.util.CollectionValueHashtable;
import nds.util.StringUtils;
/**
 */
public class DummyQueryResultImpl extends QueryResultImpl {
    private static Logger logger= LoggerManager.getInstance().getLogger(DummyQueryResultImpl.class.getName());
    private String errorInfo;
    /**
     *@param totalRowCount the total row count including those not fetched
     *@param resultWithRange if true, we take result set as having retrieved only part of whole data from
     *      database( as range and startIndex in <code>req</code>.
     *@param errorInfo error information for why dummy result obtained
     * @see QueryRequestImpl#toSQLWithRange
     * @roseuid 3B854142038E
     */
    public DummyQueryResultImpl( QueryRequest req, String errorInfo) {
        manager= TableManager.getInstance();
        totalRowCount=0;
        meta=new QueryResultMetaDataImpl(req);
        request=req;
        displayColumnIndices=req.getDisplayColumnIndices();
        rows=new ArrayList();
        cursor=-1;
        this.errorInfo= errorInfo;
    }
    /**
     * Additional message that will describe this result. 
     * When result is dummy, this message can be error information
     * @return null if no addional message
     */
    protected String getAdditionalMessage(){ return errorInfo;}
    
    
}
