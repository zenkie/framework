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
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;

import nds.control.ejb.Command;
import nds.control.event.DefaultWebEvent;
import nds.control.event.NDSEventException;
import nds.control.util.ValueHolder;
import nds.query.*;
import nds.schema.Column;
import nds.schema.TableManager;
import nds.util.NDSException;
import nds.util.Validator;


/**
 * Create text file by java instead of db procedure
 * @since 3.0
 */

public class ExportExcel extends Command {
	private final static int MAX_REPORT_LINES= 65535; //最大导出行数
	
	/**
	 * From 2.0, add two parameter in event: 
	 *  "request" - QueryRequest of the original query
	 * 
	 */
  public ValueHolder execute(DefaultWebEvent event) throws RemoteException, NDSException {
    String sql = (String)event.getParameterValue("sql");
    logger.debug(sql);
    String location= (String)event.getParameterValue("location");
    QueryRequest req=(QueryRequest) event.getParameterValue("request");
    String fileName = (String)event.getParameterValue("filename");
    boolean showColumnName= ((Boolean)event.getParameterValue("showname")).booleanValue() ;
    String[] colNames = (String[]) event.getParameterValue("columnnames");
    Column[] cols = (Column[])event.getParameterValue("columns");
    //  html or txt, from 2.0 add taxifc for C_Invoice interface
    String separator = (String)event.getParameterValue("separator"); 
    boolean ak= ((Boolean)event.getParameterValue("ak")).booleanValue() ;
    boolean pk= ((Boolean)event.getParameterValue("pk")).booleanValue() ;

    if ( QueryEngine.getInstance().getTotalResultRowCount(req)> MAX_REPORT_LINES )
     	throw new NDSException("@file-lines-greater-than@"+MAX_REPORT_LINES+",@please-export-by-page@");
    
     File svrDir = new File(location);
     if(!svrDir.isDirectory()){
         svrDir.mkdirs();
     }
     String fullFileName=location+ File.separator+fileName;
/*     File file = new File(fullFileName );

     if(file.exists()){
         throw new NDSEventException("@file@" +" ("+file.getName()+") "+ "@already-exists@");
     }
*/     
     Locale locale= event.getLocale();
     
     Connection con=null;
     ResultSet rs=null;
     try {
         HSSFWorkbook wb = new HSSFWorkbook();
         HSSFSheet sheet = wb.createSheet("download");
         HSSFCellStyle style=getDefaultStyle(wb,false);

         org.apache.poi.hssf.usermodel.HSSFDataFormat format = wb.createDataFormat();
         HSSFCellStyle dateCellStyle = wb.createCellStyle();
         dateCellStyle.setDataFormat(format.getFormat("yyyy-MM-dd"));
         
         HSSFCellStyle datetimeCellStyle = wb.createCellStyle();
         datetimeCellStyle.setDataFormat(format.getFormat("yyyy-MM-dd hh:mm:ss"));

         HSSFCellStyle stringCellStyle = wb.createCellStyle();
         stringCellStyle.setDataFormat(format.getFormat("text"));

         HSSFCellStyle numberCellStyle = wb.createCellStyle();
         numberCellStyle.setDataFormat(format.getFormat("General"));
         
         
         short i;
         // Create a row and put some cells in it. Rows are 0 based.
         int row=0;
         HSSFRow excel_row ;
         HSSFCell cell;
         if(showColumnName){
         	excel_row= sheet.createRow(row);
	         // create header information
	         if(colNames==null)colNames=getDisplayColumnNames(false,req,pk,ak, locale);
	         for( i=0;i< colNames.length ; i++){
	             cell= excel_row.createCell((short)i);
	             cell.setCellType(HSSFCell.CELL_TYPE_STRING);
	             cell.setCellStyle(getDefaultStyle(wb, true));
	             cell.setEncoding(HSSFCell.ENCODING_UTF_16);
	             cell.setCellValue(colNames[i]) ;
	         }
         }else{
         	row=-1;
         }
         if(cols==null) cols= getDisplayColumns(req,pk,ak);
         for(i=0;i<cols.length;i++){
        	 Column column= cols[i];
             short leng= (short)(column.getLength() * 256 );
             sheet.setColumnWidth((short)i,leng) ;
         }
         con= QueryEngine.getInstance().getConnection();
         rs=con.createStatement().executeQuery(sql);
         java.util.Date date;
         double d;
         String s;
         int dn;
         while( rs.next() ){
             row ++;
             if(row <0 || row>65534){
            	 logger.warning("Exit loop since row count exceed excel max value:"+ row);
            	 break;
             }
             //logger.debug("row:"+ row);
             excel_row = sheet.createRow(row);
             for ( i=0 ;i< cols.length;i++){
                 cell=excel_row.createCell(i);
                 switch( cols[i].getType()){
                 case Column.STRING :
                    s= rs.getString(i+1);
                    if( ! rs.wasNull() ){
                        cell.setCellType(HSSFCell.CELL_TYPE_STRING);
                        cell.setCellStyle(stringCellStyle);
                        cell.setEncoding(HSSFCell.ENCODING_UTF_16);
                        cell.setCellValue(s );
                        
                    }
                    break;
                 case Column.NUMBER :
                    d= rs.getDouble(i+1);
                    if( ! rs.wasNull() ){
                        cell.setCellValue(d );
                        cell.setCellStyle(numberCellStyle);
                    }
                    break;
                 case Column.DATENUMBER:
                    dn= rs.getInt(i+1);
                    if( ! rs.wasNull() ) { 
                    	try{
                    		cell.setCellValue(((java.text.SimpleDateFormat)QueryUtils.dateNumberFormatter.get()).parse(String.valueOf(dn)));
                    		cell.setCellStyle(dateCellStyle);
                    	}catch(Throwable t){
                    		cell.setCellValue(dn);
                    	}
                    }
                    break;
                 case Column.DATE:
                	 if(cols[i].getSQLType() == nds.schema.SQLTypes.TIMESTAMP){
                    	 date= rs.getTimestamp(i+1);
                    	 if( ! rs.wasNull() ) {
                    		 cell.setCellValue(date );
                    		 cell.setCellStyle(datetimeCellStyle);
                    	 }
                	 }else{
                		 date= rs.getDate(i+1);
                		 if( ! rs.wasNull() ) {
                    		 cell.setCellValue(date );
                    		 cell.setCellStyle(dateCellStyle);
                    	 }
                	 }
                     break;
                 default:
                     logger.debug("Find at cell(" + row + ","+ (i+1)+") type is invalid");
                 }

             } 
         }
         TableManager tm= TableManager.getInstance();
         //check columns of ColumnInterpreter
         for ( i=0;i< cols.length;i++){
             Column col= cols[i];
             int colId=col.getId();
             if ( col.isValueLimited() ){
                 for ( int j=1;j<= row;j++){
                     try{
                     	s="";
                         cell=sheet.getRow(j).getCell(i);
                         switch( col.getType()){
                         case Column.NUMBER :
                         	d=cell.getNumericCellValue();
                         	// yfzhu 2005-05-16 all limitvalue will be string after 2.0
                         	s=tm.getColumnValueDescription(colId, String.valueOf((int)d),locale);
                             break;
                         case Column.STRING :
                         	s= cell.getStringCellValue();
                         	s=tm.getColumnValueDescription(colId, s,locale);
                         	break;
                         default:
                         	throw new NDSException("Unexpected column type:"+ col.getType()+" for column:"+ col);
                         }
                         cell=sheet.getRow(j).createCell(i);
                         cell.setCellStyle(stringCellStyle);
                         cell.setCellType(HSSFCell.CELL_TYPE_STRING);
                         cell.setEncoding(HSSFCell.ENCODING_UTF_16);
                         cell.setCellValue(s);
                     }catch(Exception e){
                         logger.error("Could not interpret cell(" + j + ","+ (i+1)+"):" , e);
                     }

                 }
             }
         }
         for( i=0;i< colNames.length ; i++){
        	 sheet.autoSizeColumn(i);
         }
         // Write the output to a file
         FileOutputStream fileOut = new FileOutputStream(fullFileName);
         wb.write(fileOut);
         fileOut.close();
         sheet=null;
         wb=null;
         ValueHolder v=new ValueHolder();
         v.put("message", "@complete@:"+ fileName);
         return v;
     }
     catch (Exception e) {
         logger.error("Could not export to excel file:" + location + File.separator + fileName, e);
         if(!(e instanceof NDSException ))throw new NDSEventException("@exception@", e);
         else throw (NDSException)e;
         
     }finally{
         if( rs !=null){try{ rs.close();}catch(Exception e2){}}
         if( con !=null){try{ QueryEngine.getInstance().closeConnection(con);}catch(Exception e){}}
     }
    
  }
  /**
   * @return column description concatenated by references.
   * 举例：要显示的是定单的申请人所在的部门名称，跨越的column是：
   *  order.applierID, employee.departmentID, department.name
   *  对应的column名称分别是:申请人, 部门，名称。则合成的名称为：
   *      申请人部门名称
   */
  private String[] getDisplayColumnNames(boolean showNullableIndicator, QueryRequest req,
		  boolean pk, boolean ak, Locale locale) throws QueryException {
      int[] ids=  req.getReportDisplayColumnIndices(pk,ak);
      ArrayList selections=req.getAllSelectionColumnLinks();
      String[] dcns=new String[ids.length];
      ArrayList cls=req.getMainTable().getAllColumns();
      Column col=null;
     /*for(int j=0;j< cls.size();j++){
    	  col= (Column ) cls.get(j);
    	  ColumnLink colLink=col.getColumnLink();
    	  if(col.getReferenceColumn()!=null){ // 对于 FK表，需要获取FK.AK
    		  colLink.addColumn(col.getReferenceTable().getAlternateKey());
    	  }
   		  logger.debug("for cls : "+ colLink);
      }*/
      for(int i=0;i< ids.length;i++) {
          ColumnLink clink=(ColumnLink)selections.get(ids[i]);
          
          //logger.debug(clink.toString());
          /**
           * 在主表中查找与clink 一致的字段，如果有，以主表字段名称显示
           */
          for(int j=0;j< cls.size();j++){
        	  col= (Column ) cls.get(j);
        	  ColumnLink colLink=col.getColumnLink();
        	  if(col.getReferenceColumn()!=null){ // 对于 FK表，需要获取FK.AK
        		  colLink.addColumn(col.getReferenceTable().getAlternateKey());
        	  }
        	  if( clink.equals(colLink)){
        		  dcns[i]= col.getDescription(locale);
        		  //logger.debug("get equals: "+ col);
        		  break;
        	  }
          }
          if(dcns[i]!=null) continue;
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
  private HSSFCellStyle getDefaultStyle(HSSFWorkbook wb, boolean  isBold){
      // Create a new font and alter it.
      HSSFFont font = wb.createFont();
      font.setFontHeightInPoints((short)10);
      font.setFontName("宋体");
      if(isBold){
         font.setColor((short) HSSFColor.DARK_BLUE.index);
          font.setBoldweight(font.BOLDWEIGHT_BOLD);
      }
      // Fonts are set into a style so create a new one to use.
      HSSFCellStyle style = wb.createCellStyle();
      style.setFont(font);

      return style;
  }
  
  public void run( ) {
     
  }  
}