/******************************************************************
*
*$RCSfile: QueryResultMetaData.java,v $ $Revision: 1.2 $ $Author: Administrator $ $Date: 2005/03/30 13:13:57 $
*
*$Log: QueryResultMetaData.java,v $
*Revision 1.2  2005/03/30 13:13:57  Administrator
*no message
*
*Revision 1.1.1.1  2005/03/15 11:23:23  Administrator
*init
*
*Revision 1.1.1.1  2002/11/29 02:50:34  yfzhu
*Active POS
*
*Revision 1.2  2001/11/07 20:58:47  yfzhu
*no message
* 
*
********************************************************************/
//Source file: F:\\work2\\tmp\\nds\\query\\QueryResultMetaData.java

package nds.query;

import java.io.Serializable;

import nds.schema.Column;
/**
 */
public interface QueryResultMetaData extends Serializable {

    /**
     * selection column not include fk object id columns
     */
    public int getColumnCount();

    /**
     * @roseuid 3B8383D600EC
     */
    public String getColumnTitle(int column);

    /**
     * @roseuid 3B8384040070
     */
    public ColumnLink getColumnLink(int column);

    /**
     * Get id of Column as <code>column</code> specified
     * @param column the index in QueryResult, start from 1
     * @return Column' ID
     */
    public int getColumnId(int column);
    
    /**
     * Find column position in query select, start from 0
     * @param column should be main table's column
     * @return position index, -1 means not found
     */
    public int findPositionInSelection(Column column);    
    
    /**
     * Find column position in query select, start from 0
     * @param column should be main table's columnlink
     * @return position index, -1 means not found
     */
    public int findPositionInSelection(ColumnLink cl);    
}
