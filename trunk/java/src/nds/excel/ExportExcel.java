package nds.excel;

import java.io.File;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.*;
import java.util.Locale;

import nds.log.Logger;
import nds.log.LoggerManager;
import nds.query.ColumnLink;
import nds.query.QueryEngine;
import nds.query.QueryRequest;
import nds.query.QueryUtils;
import nds.schema.Column;
import nds.schema.TableManager;
import nds.util.StringHashtable;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;

/**
 * Currently only Chinese excel template supported
 * 
 * @author yfzhu@agilecontrol.com
 * @deprecated use nds.control.ejb.command.ExportExcel instead
 */
public class ExportExcel implements Runnable{
    private static Logger logger=LoggerManager.getInstance().getLogger(ExportExcel.class.getName());
    private HashMap params=new HashMap();
    private Exception exp=null;
    private Locale locale;
    public ExportExcel(Locale locale) {
    	this.locale=locale;
    }
    public void setParameters(HashMap map){
    	params=map;
    }
    public void setParameter(String name, Object value){
        params.put(name, value);
    }
    /**
     * @return column description concatenated by references.
     * 举例：要显示的是定单的申请人所在的部门名称，跨越的column是：
     *  order.applierID, employee.departmentID, department.name
     *  对应的column名称分别是:申请人, 部门，名称。则合成的名称为：
     *      申请人部门名称
     */
    private String[] getDisplayColumnNames(boolean showNullableIndicator, QueryRequest req, boolean pk, boolean ak) {
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
    /**
     * Get last error when executing
     * @return null if no exception found
     */
    public Exception getLastError(){
    	return exp;
    }
    public void run( ) {
        String sql, location, fileName;
        QueryRequest req;
        boolean ak,pk;
        sql=(String) params.get("sql");
        location= (String) params.get("location");
        File svrDir = new File(location);
        if(!svrDir.isDirectory()){
            svrDir.mkdirs();
        }
        
        fileName=(String) params.get("filename");
        req= (QueryRequest) params.get("query");
        ak=((Boolean)params.get("ak")).booleanValue() ;
        pk=((Boolean)params.get("pk")).booleanValue() ;

        Connection con=null;
        ResultSet rs=null;
        try {
            HSSFWorkbook wb = new HSSFWorkbook();
            HSSFSheet sheet = wb.createSheet("download");
            HSSFCellStyle style=getDefaultStyle(wb,false);

            short i;
            // Create a row and put some cells in it. Rows are 0 based.
            short row=0;
            HSSFRow excel_row = sheet.createRow(row);
            HSSFCell cell;
            // create header information
            String[] colNames=getDisplayColumnNames(false,req,pk,ak);
            for( i=0;i< colNames.length ; i++){
                cell= excel_row.createCell((short)i);
                cell.setCellType(HSSFCell.CELL_TYPE_STRING);
                cell.setCellStyle(getDefaultStyle(wb, true));
                cell.setEncoding(HSSFCell.ENCODING_UTF_16);
                cell.setCellValue(colNames[i]) ;
            }
            Column[] cols= getDisplayColumns(req,pk,ak);

            con= QueryEngine.getInstance().getConnection();
            rs=con.createStatement().executeQuery(sql);
            java.util.Date date;
            double d;
            int dn;
            String s;
            while( rs.next() ){
                row ++;
                //logger.debug("row:"+ row);
                excel_row = sheet.createRow(row);
                for ( i=0 ;i< cols.length;i++){
                    cell=excel_row.createCell(i);
                    switch( cols[i].getType()){
                    case Column.DATE:
                        date= rs.getDate(i+1);
                        if( ! rs.wasNull() ) cell.setCellValue(date );
                        break;
                    case Column.NUMBER :
                        d= rs.getDouble(i+1);
                        if( ! rs.wasNull() ){
                            cell.setCellValue(d );
                        }
                        break;
                    case Column.DATENUMBER :
                        dn= rs.getInt(i+1);
                        if( ! rs.wasNull() ){
                        	try{
                        		cell.setCellValue( QueryUtils.dateNumberToDate(dn));
                        	}catch(Throwable t){
                        		cell.setCellValue( dn);
                        	}
                        }
                        break;
                    case Column.STRING :
                        s= rs.getString(i+1);
                        if( ! rs.wasNull() ){
                            cell.setCellType(HSSFCell.CELL_TYPE_STRING);
                            cell.setCellStyle(style);
                            cell.setEncoding(HSSFCell.ENCODING_UTF_16);
                            cell.setCellValue(s );
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
                            }
                            cell=sheet.getRow(j).createCell(i);
                            cell.setCellType(HSSFCell.CELL_TYPE_STRING);
                            cell.setCellStyle(style);
                            cell.setEncoding(HSSFCell.ENCODING_UTF_16);
                            cell.setCellValue(s);
                        }catch(Exception e){
                            logger.error("Could not interpret cell(" + j + ","+ (i+1)+"):" , e);
                        }

                    }
                }
            }

            // Write the output to a file
            FileOutputStream fileOut = new FileOutputStream(location + File.separator + fileName);
            wb.write(fileOut);
            fileOut.close();
            sheet=null;
            wb=null;
        }
        catch (Exception ex) {
        	this.exp=ex;
            logger.error("Could not export to excel file:" + location + File.separator + fileName, ex);
        }finally{
            if( rs !=null){try{ rs.close();}catch(Exception e2){}}
            if( con !=null){try{ QueryEngine.getInstance().closeConnection(con);}catch(Exception e){}}
        }

    }
}