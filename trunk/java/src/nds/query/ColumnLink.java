package nds.query;
import java.io.IOException;

import nds.schema.Column;
import nds.schema.TableManager;
import nds.util.Tools;
import java.util.*;
import org.json.*;

/**
 * 结构是这样的：
 * 每个Column都是上一个Column所指明的referenceTable的某一列
 */
public class ColumnLink implements java.io.Serializable, JSONString {
    private transient Column[] columns;//not thread safe, but fast. columns should at least have 2 element
    private transient int hashCode;
    private String description;
    private Object tag;

    /**
     *
     * @param columnNames the first element should has format like "table.column",
     * later elements only has column name
     * @throws QueryException
     */
    public ColumnLink(String[] columnNames)throws QueryException {
        setup(columnNames);
    }
   
    /**
     * Append a column to column link
     * @param col must be column in last column's referenced table
     */
    public void addColumn(Column col) throws QueryException{
    	if(col.getTable().equals( columns[columns.length -1].getReferenceTable(true))){
    		Column[] c= new Column[columns.length+1];
    		System.arraycopy(columns, 0, c, 0, columns.length);
    		c[columns.length]= col;
    		columns=c;
    	}else
    		throw new QueryException("not linked column for "+ col +" of "+ this.toString());
    	
    }
    private void setup(String[] columnNames)throws QueryException {
        TableManager tm= TableManager.getInstance();
        int[] columnIDs= new int[columnNames.length ];
        if ( columnNames.length > 0) {
            String s= columnNames[0];
            int dotIdx= s.indexOf(".");
            if( dotIdx > 0) {
                columnIDs[0]= tm.getColumn(s.substring(0,dotIdx), s.substring(dotIdx+1)).getId() ;
            }else throw new QueryException("First item in column link is not valid: "+ s);
        }
        Column pc;
        for(int i=1;i< columnIDs.length ;i++){
            pc=tm.getColumn(columnIDs[i-1]);
            if (pc==null) throw new QueryException("Not found column named as: "+ columnIDs[i-1]);
            columnIDs[i]= tm.getColumn( pc.getReferenceTable(true).getName() , columnNames[i] ).getId();
        }
        setup(columnIDs);
    }
    public int length(){
    	return columns.length;
    }
    
    /**
     * 
     * @param columns format : table.column,column2,column3
     * First column should has table name prefixed with "."
     * Later all columns should seperatated by comman ";" or ":" and no table name set
     * comments: ";" is not permitted in html 4.0 spec as input id or name while ":" is allowed
     * @throws QueryException
     */
    public ColumnLink(String columns) throws QueryException {
    	StringTokenizer st= new StringTokenizer(columns,";:");
    	ArrayList al=new ArrayList();
    	while(st.hasMoreTokens()){
    		al.add(st.nextToken());
    	}
    	String[] columnNames=new String[al.size()];
    	for(int i=0;i<columnNames.length;i++ )
    		columnNames[i]= (String)al.get(i);
    	setup(columnNames);
    	
    }
    public ColumnLink(int[] columnIDs) throws QueryException {
        setup(columnIDs);
    }
    private void setup(int[] columnIDs)throws QueryException {
        TableManager tableManager= TableManager.getInstance();
        columns=new Column[columnIDs.length];
        for( int i=0;i< columnIDs.length;i++) {
            Column c=tableManager.getColumn(columnIDs[i]);
            if( i>0) {
                if(!c.getTable().equals( columns[i-1].getReferenceTable(true) ) )
                    throw new QueryException("Columns "+Tools.toString(columnIDs)+" are not linked.");
            }
            columns[i]=c;
        }
        // caculate hash code
        long h =0;
        for(int i=0;i< (columnIDs.length> 3?3:columnIDs.length);i++) {
            h +=(long) Math.pow(10.0, i*2.0)* columnIDs[i];
        }
        hashCode=(int) (h% Integer.MAX_VALUE);

    }
    /**
     * @return all columns in this link, 每个Column都是上一个Column所指明的referenceTable的某一列
     */
    public Column[] getColumns() {
        return columns;
    }
    public int[] getColumnIDs(){
        int[] ids= new int[ columns.length ];
        for (int i=0;i< columns.length ;i++) ids[i]= columns[i].getId();
        return ids;
    }
    /**
     * @return last column in this link
     */
    public Column getLastColumn() {
        if( columns.length ==0)
            return null;
        return columns[columns.length -1];
    }

    //////////io
    private void readObject(java.io.ObjectInputStream stream)throws IOException, ClassNotFoundException {
        int[] ids=(int[]) stream.readObject();
        try {
            setup(ids);
        } catch(QueryException e) {
            throw new IOException(e.getMessage());
        }
    }
    private void writeObject(java.io.ObjectOutputStream stream) throws IOException {
        int[] ids= new int[columns.length];
        for(int i=0;i<ids.length;i++) {
            ids[i]=columns[i].getId();
        }
        stream.writeObject(ids);
    }

    /**
     * @return the column before last, in this link
     */
    public ColumnLink getColumnLinkExcludeLastColumn()throws QueryException {
        if( columns.length < 2)
            return null;
        int[] ids=new int[columns.length -1];
        for(int i=0;i< ids.length;i++) {
            ids[i]= columns[i].getId();
        }
        return new ColumnLink(ids);

    }
    public String getDescription(java.util.Locale locale){
        if(description==null){
	    	String s="";
	        if( TableManager.getInstance().getDefaultLocale().hashCode()==locale.hashCode()){
		        for(int i=0;i< columns.length;i++) {
		            s +=columns[i].getDescription(locale);
		        }
	        }else{
		        for(int i=0;i< columns.length;i++) {
		            s +=(i>0?".":"")+columns[i].getDescription(locale);
		        }
	        }
	        description=s;
        }
        return description;
    }
    /**
     * smple link is : 12,13,14. the reverse one will be 14,13,12
     * @return
     */
    public ColumnLink reverse() throws QueryException {
    	int[] cl= new int[columns.length];
    	for(int i=0;i<cl.length;i++)
    		cl[i]= columns[cl.length-1-i].getId();
    	return new ColumnLink(cl);
    }
    /**
     * For name or id as in html since ";" is not allowed in html 4.0 spec
     * Format like: [table].[column1]:[column2]
     * Note current implimentation can not allow column name contains ";" or ":" 
     * You can use this string to recontruct ColumnLink
     * @return
     */
    public String toHTMLString(){
    	return toString();
    	/*if (columns.length==0) return "";
    	
        StringBuffer  s=new StringBuffer();
        s.append( columns[0].getTable().getName()).append(".").append(columns[0].getName());
        for(int i=1;i< columns.length;i++) {
            s.append(":").append(columns[i].getName());
        }
        return s.toString();*/    	
    
    }
    /**
     * Format like : [table].[column1];[column2]
     * Note current implimentation can not allow column name contains ";" 
     * You can use this string to recontruct ColumnLink
     * @since 2.0
     */
    public String toString() {
    	if (columns.length==0) return "";
    	
        StringBuffer  s=new StringBuffer();
        s.append( columns[0].getTable().getName()).append(".").append(columns[0].getName());
        for(int i=1;i< columns.length;i++) {
            s.append(";").append(columns[i].getName());
        }
        return s.toString();
    }
    
    public int hashCode() {
        return hashCode;
    }
    public boolean equals(Object c) {
        if(c !=null &&( c instanceof ColumnLink) ) {
            ColumnLink col=(ColumnLink)c;
            if( col.columns.length !=columns.length)
                return false;
            for(int i=0;i< columns.length;i++) {
                if( !columns[i].equals(col.columns[i]))
                    return false;
            }
            return true;
        }
        return false;
    }
    /**
     * Create column link with only one column
     * @param col
     * @return
     */
    public static ColumnLink createLink(Column col) throws QueryException{
    	return new ColumnLink(new int[]{col.getId()});
    }
	
	public void setDescription(String description) {
		this.description = description;
	}
	public Object getTag() {
		return tag;
	}
	/**
	 * Can set anything here for tag, one use case is set boolean value for order by columns
	 * @param tag
	 */
	public void setTag(Object tag) {
		this.tag = tag;
	}
	public String toJSONString()  {
		try{
			JSONObject jo=new JSONObject();
			jo.put("c",toString());
			jo.put("d", this.getDescription(TableManager.getInstance().getDefaultLocale()));
			if(this.tag!=null)jo.put("t", tag);
			return jo.toString();
		}catch(Throwable t){
			return "";
		}
	}
	public static ColumnLink parseJSONObject(JSONObject jo) throws QueryException{
		ColumnLink cl=new ColumnLink( jo.optString("c"));
		cl.setDescription(jo.optString("d"));
		cl.setTag(jo.opt("t"));
		return cl;
	}
}