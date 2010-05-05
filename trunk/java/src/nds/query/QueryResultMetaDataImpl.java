/******************************************************************
*
*$RCSfile: QueryResultMetaDataImpl.java,v $ $Revision: 1.3 $ $Author: Administrator $ $Date: 2005/03/30 13:13:57 $
*
*$Log: QueryResultMetaDataImpl.java,v $
*Revision 1.3  2005/03/30 13:13:57  Administrator
*no message
*
*Revision 1.2  2005/03/23 17:56:02  Administrator
*no message
*
*Revision 1.1.1.1  2005/03/15 11:23:23  Administrator
*init
*
*Revision 1.1.1.1  2002/11/29 02:50:34  yfzhu
*Active POS
* 
*Revision 1.3  2001/12/09 03:43:32  yfzhu
*no message
*
*Revision 1.2  2001/11/07 20:58:47  yfzhu
*no message
*
*
********************************************************************/
//Source file: F:\\work2\\tmp\\nds\\query\\QueryResultMetaImpl.java

package nds.query;
import java.util.ArrayList;

import nds.schema.Column;
/**
 */
public class QueryResultMetaDataImpl implements QueryResultMetaData {
    private int selectionColumnCount;// all columns selected, including both those to be
                                     // displayed and those work as Object ID link
    private int columnCount;// display columns
    private String[] titles;// display column titles
    //private String[] columnLinks; // display column links @see Table.getRowURL()
    private ColumnLink[] columnLinks;
    private int[] columnIds;
    private int maintableAKPosInSelection=-1;
    /**
     * @roseuid 3B8541F6030B
     */
    public QueryResultMetaDataImpl(QueryRequest req) {
        int[] dc=req.getDisplayColumnIndices();
        columnCount=dc.length;
        titles= req.getDisplayColumnNames(false);
        //columnLinks=new String[columnCount];
        columnLinks=new ColumnLink[columnCount];
        ArrayList columns=req.getAllSelectionColumns();
        ArrayList selLinks= req.getAllSelectionColumnLinks();
        selectionColumnCount = columns.size();
        columnIds= new int[columnCount];
        int akId=-1;
        if( req.getMainTable().getAlternateKey()!=null)
        	akId= req.getMainTable().getAlternateKey().getId();
        for(int i=0;i<dc.length;i++){
            Column col=(Column)columns.get(dc[i]);
            ColumnLink cl=(ColumnLink) selLinks.get(dc[i]);
            if(cl.getColumnIDs()[0]== akId ){
            	maintableAKPosInSelection = i+1; // start from 1
            }
            //columnLinks[i]= col.getTable().getRowURL();
            columnLinks[i]= cl;
            columnIds[i]= col.getId();
        }
        
    }
    /**
     * Get Maintable's Alternate Key's position in display columns
     * Note this is not the pos in selection, but in display columns
     * @return -1 if ak is not to be displayed in the columns
     * @since 2.0
     */
    public int getMaintableAKPosInSelection(){
    	return maintableAKPosInSelection;
    }
    /**
     * @roseuid 3B8AFCFC0111
     */
    public int getColumnCount() {
        return columnCount;
    }
    /**
     * all columns selected, including both those to be
     * displayed and those work as Object ID link
     * This is for QueryResultImpl only
     * @see QueryResultImpl
     */
    int getSelectionColumnCount(){
        return selectionColumnCount;
    }
    /**
     * Find column position in query select, start from 0
     * @param column should be main table's column
     * @return position index
     */
    public int findPositionInSelection(Column column){
    	for(int i=0;i< columnLinks.length;i++){
    		if(columnLinks[i].length()==1 && columnLinks[i].getLastColumn().equals(column))
    			return i;
    	}
    	return -1;
    }
    /**
     * @param column starts from 1
     * @roseuid 3B8AFCFC0125
     */
    public String getColumnTitle(int column) {
        return titles[column -1 ];
    }
	public ColumnLink getColumnLink(int column){
		return columnLinks[column -1];
	}
    /**
     * @param column starts from 1
     * @return null if column has no link
     * @roseuid 3B8AFCFC0175
     * Old method, deprecated, and changed meaning for column link
     */
    /*public String getColumnLink(int column) {
        return columnLinks[column -1];
    }*;
    
    ///////// override Object
    public String toString(){
        String ret=" columns:"+columnCount+" titles=[";
        for(int i=0;i< titles.length;i++)
        {   if( i==0) ret +=titles[i];
            else  ret +=","+titles[i];
        }
        ret += "] links=[";
        for(int i=0;i< columnLinks.length;i++)
        {   if( i==0) ret +=columnLinks[i];
            else  ret +=","+columnLinks[i];
        }
        ret +="]";
        return ret;
    }
    /**
     * Get id of Column as <code>column</code> specified
     * @param column the index in QueryResult, start from 1
     * @return Column' ID
     */
    public int getColumnId(int column){
        return columnIds[column-1];
    }
	
}
