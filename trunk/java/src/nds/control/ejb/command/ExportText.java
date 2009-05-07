package nds.control.ejb.command;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.*;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import nds.control.ejb.Command;
import nds.control.event.DefaultWebEvent;
import nds.control.event.NDSEventException;
import nds.control.util.ValueHolder;
import nds.query.*;
import nds.schema.Column;
import nds.schema.TableManager;
import nds.util.NDSException;
import nds.util.StringUtils;
import nds.util.Validator;


/**
 * Create text file by java instead of db procedure
 * @since 3.0
 */

public class ExportText extends Command {
	/**
	 * From 2.0, add two parameter in event: 
	 *  "request" - QueryRequest of the original query
	 * 
	 */
  public ValueHolder execute(DefaultWebEvent event) throws RemoteException, NDSException {
    Connection con=null;
    ResultSet rs=null;

  	try{
	    String sql = (String)event.getParameterValue("sql");
	    logger.debug(sql);
	    String location= (String)event.getParameterValue("location");
	    QueryRequest req=(QueryRequest) event.getParameterValue("request");
	    String fileName = (String)event.getParameterValue("filename");
	
	    //  html or txt, from 2.0 add taxifc for C_Invoice interface
	    String separator = (String)event.getParameterValue("separator"); 
	    boolean ak= ((Boolean)event.getParameterValue("ak")).booleanValue() ;
	    boolean pk= ((Boolean)event.getParameterValue("pk")).booleanValue() ;
	    boolean showColumnName= ((Boolean)event.getParameterValue("showname")).booleanValue() ;
	    String[] colNames = (String[]) event.getParameterValue("columnnames");
	    Column[] cols = (Column[])event.getParameterValue("columns");
	    
	    SPResult res = null;
	    
        File svrDir = new File(location);
        if(!svrDir.isDirectory()){
            svrDir.mkdirs();
        }
        String fullFileName=location+ File.separator+fileName;
        
/*        
        File file = new File(fileName);

        if(file.exists()){
            throw new NDSEventException("@file@" +" ("+file.getName()+") "+ "@already-exists@");
        }
*/

        FileOutputStream fos=new FileOutputStream(fullFileName, false);
        OutputStreamWriter w= new OutputStreamWriter(fos, "UTF-8");
        
            int i;
            // Create a row and put some cells in it. Rows are 0 based.
            if(showColumnName){
            	String header;
            	if(colNames==null)
            		header= getHeader(pk,ak,separator, req);
            	else{
            		StringBuffer sb=new StringBuffer();
            		for(int d=0;d< colNames.length;d++){
            			if(d>0)sb.append(",");
            			sb.append(colNames[d]);
            		}
            		header= sb.toString();
            	}
            	w.write(header);
            	w.write(StringUtils.LINE_SEPARATOR);
            }

            if(cols==null)cols= getDisplayColumns(req,pk,ak);

            con= QueryEngine.getInstance().getConnection();
            rs=con.createStatement().executeQuery(sql);
            java.util.Date date;
            double d;
            StringBuffer sb;//=new StringBuffer();
            String s;Object o;
            TableManager tm= TableManager.getInstance();
            java.util.Locale locale= event.getLocale();
            while( rs.next() ){
            	sb=new StringBuffer();
                for ( i=0 ;i< cols.length;i++){
                	if(i!=0) sb.append(separator);
                	Column col= cols[i];
                	if ( col.isValueLimited() ){
                		o= rs.getObject(i+1);
                		if(!rs.wasNull()){
                			sb.append(tm.getColumnValueDescription(col.getId(),o,locale));
                		}
                	}else{
                		switch(col.getType()){
	                    case Column.DATE:
	                        date= rs.getDate(i+1);
	                        if( ! rs.wasNull() ){
	                        	sb.append(((java.text.SimpleDateFormat)QueryUtils.dateFormatter.get()).format(date));
	                        }
	                        break;
	                    case Column.NUMBER :
	                        d= rs.getDouble(i+1);
	                        if( ! rs.wasNull() ){
	                        	sb.append(d);
	                        }
	                        break;
	                    case Column.STRING :
	                        s= rs.getString(i+1);
	                        if( ! rs.wasNull() ){
	                            sb.append(s);
	                        }
	                        break;
	                    default:
	                        logger.error("Find at col(" + (i+1)+") type is invalid:"+ cols[i].getType());
	                    }
                	}
                }
                w.write(sb.toString());
            	w.write(StringUtils.LINE_SEPARATOR);
                
            }
            
            // Write the output to a file
	        w.flush();
	        fos.close();

	        ValueHolder v=new ValueHolder();
	        v.put("message", "@complete@");
	        return v;
    }catch(Exception e){
      logger.error("", e);
      if(!(e instanceof NDSException ))throw new NDSEventException("@exception@", e);
      else throw (NDSException)e;
    }
    finally{
        if( rs !=null){try{ rs.close();}catch(Exception e2){}}
        if( con !=null){try{ QueryEngine.getInstance().closeConnection(con);}catch(Exception e){}}
    }	    
  }
  private String getHeader(boolean pk,boolean ak,String separator,QueryRequest qRequest){
    String[] showNames = qRequest.getDisplayColumnNames(true);
    int[] showColumns = qRequest.getDisplayColumnIndices();
    if(separator == null || separator.trim().length() ==0)
        separator = ",";
    StringBuffer buff = new StringBuffer();
    boolean first=true;
    for( int i=0;i<showColumns.length;i++) {
        int[] cLink = qRequest.getSelectionColumnLink(showColumns[i]);
        if(!pk && cLink[cLink.length - 1] == qRequest.getMainTable().getPrimaryKey().getId())
            continue;//如果不需要显示主键，就不显示
        if(!ak && cLink[cLink.length - 1] == qRequest.getMainTable().getAlternateKey().getId())
            continue;//如果不需要显示AK，就不显示
        if(!first)
            buff.append(separator);
        buff.append(showNames[i]);
        first=false;
    }
    return buff.toString();
}
  /**
   * @return column description concatenated by references.
   * 举例：要显示的是定单的申请人所在的部门名称，跨越的column是：
   *  order.applierID, employee.departmentID, department.name
   *  对应的column名称分别是:申请人, 部门，名称。则合成的名称为：
   *      申请人部门名称
   */
  private String[] getDisplayColumnNames(boolean showNullableIndicator, QueryRequest req, boolean pk, boolean ak,Locale locale) {
      int[] ids=  req.getReportDisplayColumnIndices(pk,ak);
      ArrayList selections=req.getAllSelectionColumnLinks();
      String[] dcns=new String[ids.length];
      for(int i=0;i< ids.length;i++) {
          ColumnLink clink=(ColumnLink)selections.get(ids[i]);
          dcns[i]="";
          int len=clink.getColumns().length;
          for(int j=0;j< len;j++) {
              dcns[i] +=  clink.getColumns()[j].getDescription(locale)+(j<len-1? ".":"");
          }
          dcns[i] += (showNullableIndicator && !clink.getColumns()[0].isNullable())?"*":" ";
      }
      return dcns;
  }
  private Column[] getDisplayColumns( QueryRequest req, boolean pk, boolean ak) {
      int[] ids=  req.getReportDisplayColumnIndices(pk,ak);
      ArrayList selections=req.getAllSelectionColumnLinks();
      Column[] cols=new Column[ids.length];
      for(int i=0;i< ids.length;i++) {
          ColumnLink clink=(ColumnLink)selections.get(ids[i]);
          cols[i]= clink.getLastColumn() ;
      }
      return cols;

  }  
}