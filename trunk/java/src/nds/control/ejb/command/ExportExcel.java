package nds.control.ejb.command;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.*;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook; 

import org.apache.poi.poifs.filesystem.POIFSFileSystem;  
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row.MissingCellPolicy;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Cell;  
import org.apache.poi.ss.usermodel.DateUtil;  
import org.apache.poi.ss.usermodel.Row;  
import org.apache.poi.ss.usermodel.Sheet;  
import org.apache.poi.ss.usermodel.Workbook;  
import org.json.JSONException;
import org.json.JSONObject;
//import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.context.i18n.LocaleContextHolder;

//import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import nds.control.ejb.Command;
import nds.control.event.DefaultWebEvent;
import nds.control.event.NDSEventException;
import nds.control.util.ValueHolder;
import nds.query.*;
import nds.schema.Column;
import nds.schema.Table;
import nds.schema.TableManager;
import nds.util.NDSException;
import nds.util.Tools;
import nds.util.Validator;
import nds.web.config.QueryListConfig;


/**
 * Create text file by java instead of db procedure
 * @since 3.0
 */

public class ExportExcel extends Command {
	private final static int MAX_REPORT_LINES= 65535; //��󵼳�����
	
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
    
	
    //03 xls max 65535
    if(fileName.endsWith(".xls")&&QueryEngine.getInstance().getTotalResultRowCount(req)> MAX_REPORT_LINES )
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
     Locale locale= LocaleContextHolder.getLocale();//event.getLocale();
     
     Connection con=null;
     ResultSet rs=null;
     Statement stmt =null;
     try {
         //HSSFWorkbook wb = new HSSFWorkbook();
         //Workbook wb = null;  
         int max_rowcount=65535;
         if (fileName.endsWith(".xls")) {  
            // inp = new FileInputStream(FilePath);  
        	 Workbook wb = (Workbook) new HSSFWorkbook();  
        	 Sheet sheet = wb.createSheet("download");
             CellStyle style=getDefaultStyle(wb,false);
             org.apache.poi.ss.usermodel.DataFormat format = wb.createDataFormat();
             //org.apache.poi.hssf.usermodel.HSSFDataFormat format = wb.createDataFormat();
             CellStyle dateCellStyle = wb.createCellStyle();
             dateCellStyle.setDataFormat(format.getFormat("yyyy-MM-dd"));
             
             CellStyle datetimeCellStyle = wb.createCellStyle();
             datetimeCellStyle.setDataFormat(format.getFormat("yyyy-MM-dd hh:mm:ss"));

             CellStyle stringCellStyle = wb.createCellStyle();
             stringCellStyle.setDataFormat(format.getFormat("text"));

             CellStyle numberCellStyle = wb.createCellStyle();
             numberCellStyle.setDataFormat(format.getFormat("General"));
             
             
             short i;
             // Create a row and put some cells in it. Rows are 0 based.
             int row=0;
             Row excel_row ;
             Cell cell;
             if(showColumnName){
             	excel_row= sheet.createRow(row);
    	         // create header information
    	         if(colNames==null)colNames=getDisplayColumnNames(false,req,pk,ak, locale,event);
    	         for( i=0;i< colNames.length ; i++){
    	             cell= excel_row.createCell((short)i);
    	             cell.setCellType(HSSFCell.CELL_TYPE_STRING);
    	             cell.setCellStyle(getDefaultStyle(wb, true));
    	             //cell.setEncoding(HSSFCell.ENCODING_UTF_16);
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
             stmt=con.createStatement(java.sql.ResultSet.TYPE_FORWARD_ONLY,
                     java.sql.ResultSet.CONCUR_READ_ONLY);
             stmt.setFetchSize(200);
             rs=stmt.executeQuery(sql);
             //System.out.println("Starting to retrieve data. Memory Used: "+ getUsedMemory());
             java.util.Date date;
             double d;
             String s;
             int dn;
             while( rs.next() ){
                 row ++;
                 if(row <0 || row>max_rowcount){
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
                            //cell.setEncoding(HSSFCell.ENCODING_UTF_16);
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
                             //cell.setEncoding(HSSFCell.ENCODING_UTF_16);
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
         } else if (fileName.endsWith(".xlsx")) {   
        	 
        	 SXSSFWorkbook wb = new SXSSFWorkbook(100);  
        	 wb.setCompressTempFiles(true);
             max_rowcount=1048576;
           	 Sheet sheet = wb.createSheet("download");
             CellStyle style=getDefaultStyle(wb,false);
             org.apache.poi.ss.usermodel.DataFormat format = wb.createDataFormat();
             //org.apache.poi.hssf.usermodel.HSSFDataFormat format = wb.createDataFormat();
             CellStyle dateCellStyle = wb.createCellStyle();
             dateCellStyle.setDataFormat(format.getFormat("yyyy-MM-dd"));
             
             CellStyle datetimeCellStyle = wb.createCellStyle();
             datetimeCellStyle.setDataFormat(format.getFormat("yyyy-MM-dd hh:mm:ss"));

             CellStyle stringCellStyle = wb.createCellStyle();
             stringCellStyle.setDataFormat(format.getFormat("text"));

             CellStyle numberCellStyle = wb.createCellStyle();
             numberCellStyle.setDataFormat(format.getFormat("General"));
             
             
             short i;
             // Create a row and put some cells in it. Rows are 0 based.
             int row=0;
             Row excel_row ;
             Cell cell;
             if(showColumnName){
             	excel_row= sheet.createRow(row);
    	         // create header information
    	         if(colNames==null)colNames=getDisplayColumnNames(false,req,pk,ak, locale,event);
                 logger.debug("colNames length is:"+colNames.length);
    	         for( i=0;i< colNames.length ; i++){
    	             cell= excel_row.createCell(i);
    	             cell.setCellType(Cell.CELL_TYPE_STRING);
    	             cell.setCellStyle(stringCellStyle);
    	             //cell.setCellStyle(getDefaultStyle(wb, true));
    	             //cell.setEncoding(HSSFCell.ENCODING_UTF_16);
    	             cell.setCellValue(colNames[i]) ;
    	         }
             }else{
             	row=-1;
             }
             if(cols==null) cols= getDisplayColumns(req,pk,ak);
//             for(i=0;i<cols.length;i++){
//            	 Column column= cols[i];
//                 short leng= (short)(column.getLength() * 256 );
//                 sheet.setColumnWidth((short)i,leng) ;
//             }
             con= QueryEngine.getInstance().getConnection();
             stmt=con.createStatement(java.sql.ResultSet.TYPE_FORWARD_ONLY,
                     java.sql.ResultSet.CONCUR_READ_ONLY);
             stmt.setFetchSize(200);
             rs=stmt.executeQuery(sql);
             java.util.Date date;
             double d;
             String s=null;
             int dn;
             TableManager tm= TableManager.getInstance();
             while( rs.next() ){
                 row ++;
                 if(row <0 || row>max_rowcount){
                	 logger.warning("Exit loop since row count exceed excel max value:"+ row);
                	 break;
                 }
                 //logger.debug("row:"+ row);
                 excel_row = sheet.createRow(row);
                 for ( i=0 ;i< cols.length;i++){
                     cell=excel_row.createCell(i);
                     Column col= cols[i];
                     int colId=col.getId();
                     switch( cols[i].getType()){
                     case Column.STRING :
                        s= rs.getString(i+1);
                        if( ! rs.wasNull()&&col.isValueLimited() ){
                        	s=tm.getColumnValueDescription(colId, s,locale);
                            cell.setCellType(Cell.CELL_TYPE_STRING);
                            cell.setCellStyle(stringCellStyle);
                            cell.setCellValue(s );
                        }else if(! rs.wasNull()&&!col.isValueLimited()){
                            cell.setCellType(Cell.CELL_TYPE_STRING);
                            cell.setCellStyle(stringCellStyle);
                            //cell.setEncoding(HSSFCell.ENCODING_UTF_16);
                            cell.setCellValue(s );
                        }
                        break;
                     case Column.NUMBER :
                        d= rs.getDouble(i+1);
                        if( ! rs.wasNull()&&col.isValueLimited() ){
                        	s=tm.getColumnValueDescription(colId, String.valueOf((int)d),locale);
                            cell.setCellType(Cell.CELL_TYPE_STRING);
                            cell.setCellStyle(stringCellStyle);
                            cell.setCellValue(s );
                        }else if(! rs.wasNull()&&!col.isValueLimited()){
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
                 // manually control how rows are flushed to disk 
                 if(row % 100 == 0) {
                      ((SXSSFSheet)sheet).flushRows(100); // retain 100 last rows and flush all others

                      // ((SXSSFSheet)sh).flushRows() is a shortcut for ((SXSSFSheet)sh).flushRows(0),
                      // this method flushes all rows
                 }
             }
//             TableManager tm= TableManager.getInstance();
//             //check columns of ColumnInterpreter
//             for ( i=0;i< cols.length;i++){
//                 Column col= cols[i];
//                 int colId=col.getId();
//                 if ( col.isValueLimited() ){
//                     for ( int j=1;j<= row;j++){
//                         try{
//                         	//s="";
//                             Row introw=sheet.getRow(j);
//                             Cell  intcell=introw.getCell(i,Row.CREATE_NULL_AS_BLANK);
//                             //cell=sheet.getRow(j).createCell(i);
//                             switch( col.getType()){
//                             case Column.NUMBER :
//                             	d=intcell.getNumericCellValue();
//                             	// yfzhu 2005-05-16 all limitvalue will be string after 2.0
//                             	s=tm.getColumnValueDescription(colId, String.valueOf((int)d),locale);
//                                 break;
//                             case Column.STRING :
//                             	s= intcell.getStringCellValue();
//                             	s=tm.getColumnValueDescription(colId, s,locale);
//                             	break;
//                             default:
//                             	throw new NDSException("Unexpected column type:"+ col.getType()+" for column:"+ col);
//                             }
//                             //intcell=sheet.getRow(j).createCell(i);
//                             intcell.setCellStyle(stringCellStyle);
//                             intcell.setCellType(Cell.CELL_TYPE_STRING);
//                             //cell.setEncoding(HSSFCell.ENCODING_UTF_16);
//                             intcell.setCellValue(s);
//                         }catch(Exception e){
//                             logger.error("Could not interpret cell(" + j + ","+ (i+1)+"):");
//                         }
//
//                     }
//                 }
//             }
////             for( i=0;i< colNames.length ; i++){
////            	 sheet.autoSizeColumn(i);
////             }
//	           for(i=0;i<cols.length;i++){
//	        	 Column column= cols[i];
//	             short leng= (short)(column.getLength() * 256 );
//	             sheet.setColumnWidth(i,leng) ;
//	           }
             // Write the output to a file
             FileOutputStream fileOut = new FileOutputStream(fullFileName);
             wb.write(fileOut);
             fileOut.close();
             wb.dispose();
         }  
         //SXSSFWorkbook wb = new SXSSFWorkbook(100); 
         //System.out.println("������д��ִ�����!");  
         ValueHolder v=new ValueHolder();
         v.put("message", "@complete@:"+ fileName);
         return v;
        
     }
     catch (Exception e) {
         logger.error("Could not export to excel file:" + location + File.separator + fileName, e);
         if(!(e instanceof NDSException ))throw new NDSEventException("@exception@", e);
         else throw (NDSException)e;
         
     }finally{
    	 if(stmt!=null){try{ stmt.close();}catch(Exception e){}}
         if( rs !=null){try{ rs.close();}catch(Exception e2){}}
         if( con !=null){try{ QueryEngine.getInstance().closeConnection(con);}catch(Exception e){}}
     }
    
  }
  /**
   * @return column description concatenated by references.
   * ������Ҫ��ʾ���Ƕ��������������ڵĲ������ƣ���Խ��column�ǣ�
   *  order.applierID, employee.departmentID, department.name
   *  ��Ӧ��column���Ʒֱ���:������, ���ţ����ơ���ϳɵ�����Ϊ��
   *      �����˲�������
   */
  private String[] getDisplayColumnNames(boolean showNullableIndicator, QueryRequest req,
		  boolean pk, boolean ak, Locale locale,DefaultWebEvent event) throws QueryException {
	  List<ColumnLink> selections=null;
	  List<ColumnLink> qlcselections=null;
		
      int[] ids=  req.getReportDisplayColumnIndices(pk,ak);
       selections=req.getAllSelectionColumnLinks();
	    //support qlc
	    int qlcId=Tools.getInt(event.getParameterValue("qlcid"),-1);
	    logger.debug("getDisplayColumnNames qlcid->"+qlcId);
		QueryListConfig qlc=null;
		if(qlcId>0&&qlcId!=-1) {
			qlc=nds.web.config.QueryListConfigManager.getInstance().getQueryListConfig(qlcId);
			qlcselections=qlc.getSelections();
		}
      String[] dcns=new String[ids.length];
      ArrayList cls=req.getMainTable().getAllColumns();
      Column col=null;
//     for(int j=0;j< cls.size();j++){
//    	  col= (Column ) cls.get(j);
//    	  ColumnLink colLink=col.getColumnLink();
//    	  if(col.getReferenceColumn()!=null){ // ���� FK����Ҫ��ȡFK.AK
//    		  colLink.addColumn(col.getReferenceTable().getAlternateKey());
//    	  }
//   		  logger.debug("for cls : "+ colLink);
//      }
      
      if(qlcselections!=null&&qlcselections.size()>0)for(int j=0;j< qlcselections.size();j++){
    		  logger.debug("get equals: "+ qlcselections.get(j));
	  }
      for(int i=0;i< ids.length;i++) {
          ColumnLink clink=(ColumnLink)selections.get(ids[i]);
          
   
          
          
          //logger.debug(clink.toString());
          /**
           * �������в�����clink һ�µ��ֶΣ�����У��������ֶ�������ʾ
           * 9.34 add ����������� ��չ����{reflable:{"tabid":12853,"ref_id":1853}} ֧��
           */
          if(qlcId<1){
          for(int j=0;j< cls.size();j++){
        	  col= (Column ) cls.get(j);
        	  ColumnLink colLink=col.getColumnLink();
        	  JSONObject jo;
				try {
					jo = col.getJSONProps().getJSONObject("reflable");
					int lable_id = jo.optInt("ref_id", 0);
					int lable_tabid = jo.optInt("tabid", 0);
					TableManager manager = TableManager.getInstance();
					Table reftable = manager.getTable(lable_tabid);
					QueryRequestImpl query = QueryEngine.getInstance()
							.createRequest(req.getSession());
					query.setMainTable(lable_tabid);
					query.addSelection(reftable.getAlternateKey().getId());
					query.addParam(reftable.getPrimaryKey().getId(), ""+ lable_id);
					QueryResult rs = QueryEngine.getInstance().doQuery(query);
					if (lable_id != 0|| (rs != null && rs.getTotalRowCount() > 0)) {
						while (rs.next()) {
							logger.debug(rs.getObject(1).toString());
							dcns[i] = rs.getObject(1).toString();
						}
					}
		        	  if(col.getReferenceColumn()!=null){ // ���� FK����Ҫ��ȡFK.AK
		        		  colLink.addColumn(col.getReferenceTable().getAlternateKey());
		        	  }
		        	  if( clink.equals(colLink)){
		        		  //dcns[i]= col.getDescription(locale);
		        		  //logger.debug("get equals: "+ col);
		        		  break;
		        	  }
					if(dcns[i]!=null) continue;
				} catch (Exception e) {
					// TODO Auto-generated catch block
					// e.printStackTrace();
					//logger.debug("col get reflable is null");
				}

        	  if(col.getReferenceColumn()!=null){ // ���� FK����Ҫ��ȡFK.AK
        		  colLink.addColumn(col.getReferenceTable().getAlternateKey());
        	  }
        	  if( clink.equals(colLink)){
        		  dcns[i]= col.getDescription(locale);
        		  //logger.debug("get equals: "+ col);
        		  break;
        	  }
          }
          }else{
        	  logger.debug("clink is: "+ clink.getDescription(locale));
        	  logger.debug("clink last is: "+ clink.getColumnLinkExcludeLastColumn());
        	  logger.debug("clink is: "+ clink);
        	  
        	  if(qlcselections!=null&&qlcselections.size()>0){
        		  dcns[i]= qlcselections.get(i).getDescription(locale);
        		  logger.debug("get equals: "+ qlcselections.get(i).getDescription(locale));
        		  
        	  }
//        	  for(int j=0;j< qlcselections.size();j++){
//        		  if( clink.equals(qlcselections.get(j))){
//            		  dcns[i]= qlcselections.get(j).getDescription(locale);
//            		  logger.debug("get equals: "+ qlcselections.get(j).getDescription(locale));
//            		  break;
//            	  }
//        	  }
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
  private CellStyle getDefaultStyle(Workbook wb, boolean  isBold){
      // Create a new font and alter it.
      Font font = wb.createFont();
      font.setFontHeightInPoints((short)10);
      font.setFontName("����");
      if(isBold){
         font.setColor((short) HSSFColor.DARK_BLUE.index);
          font.setBoldweight(font.BOLDWEIGHT_BOLD);
      }
      // Fonts are set into a style so create a new one to use.
      CellStyle style = wb.createCellStyle();
      style.setFont(font);

      return style;
  }
  
  public void run( ) {
     
  }  
}