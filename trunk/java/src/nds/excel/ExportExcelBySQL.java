package nds.excel;

import java.io.File;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import nds.log.Logger;
import nds.log.LoggerManager;
import nds.query.QueryEngine;
import nds.query.QueryRequest;
import nds.query.QueryUtils;
import nds.schema.Column;
import nds.schema.SQLTypes;
import nds.util.NDSRuntimeException;
import nds.util.StringHashtable;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;

/**
 * The difference to ExportExcel is that it accepts raw sql string
 * Accept parameter:
 *        datasource - string datasource,default to "java:/DataSource"
 *        sql      - sql string
 *        filename - the excel file
 *        location - path, full file path is
 *                  location+File.separator + fileName
 */
public class ExportExcelBySQL implements Runnable{
    private static Logger logger=LoggerManager.getInstance().getLogger(ExportExcelBySQL.class.getName());
    private StringHashtable params=new StringHashtable();
    public ExportExcelBySQL() {
    }
    public void setParameter(String name, Object value){
        params.put(name, value);
    }
    /**
     * Get column name according to ResultSetMetaData
     * @return elements are String
     */
    private String[] getColumnNames(ResultSet rs) throws SQLException{
        ResultSetMetaData mt=rs.getMetaData();
        String[] s=new String[mt.getColumnCount()];
        for (int ct = 1;ct <= mt.getColumnCount();ct++) {
             s[ct-1]=rs.getMetaData().getColumnLabel(ct);
        }
        return s;

    }
    /**
     * Get column type according to ResultSetMetaData
     * @return elements are of Column.type, such as Column.STRING
     */
    private int[] getColumnTypes(ResultSet rs) throws SQLException{
        ArrayList al=new ArrayList();
        ResultSetMetaData mt=rs.getMetaData();
        int[] types=new int[mt.getColumnCount()];
        for (int ct = 1;ct <= mt.getColumnCount();ct++) {
            int type=SQLTypes.convertToSQLType( mt.getColumnType(ct));
            switch(type) {
                    case SQLTypes.DECIMAL:
                    case SQLTypes.NUMERIC:
                    case SQLTypes.BIGINT:
                    case SQLTypes.INT:
                    case SQLTypes.SMALLINT:
                    case SQLTypes.TINYINT:
                    case SQLTypes.FLOAT:
                    case SQLTypes.REAL:
                    case SQLTypes.DOUBLE:
                        types[ct-1]= Column.NUMBER ;
                        break;
                    case SQLTypes.DATENUMBER:
                        types[ct-1]= Column.DATENUMBER ;
                        break;
                    case SQLTypes.TIME:
                    case SQLTypes.TIMESTAMP:
                    case SQLTypes.DATE:
                        types[ct-1]= Column.DATE  ;
                        break;
                    case SQLTypes.VARCHAR:
                    case SQLTypes.LONGVARCHAR:
                    case SQLTypes.CHAR:
                        types[ct-1]= Column.STRING;
                        break;
                    default:
                        throw new SQLException("Unexpected column type:"+type);
            }//end switch
        }
        return types;

    }
    private HSSFCellStyle getDefaultStyle(HSSFWorkbook wb, boolean  isBold){
        // Create a new font and alter it.
        HSSFFont font = wb.createFont();
        font.setFontHeightInPoints((short)10);
        font.setFontName("ËÎÌו");
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
     * @return the running thread, so out side process can check whether the thread is end or not
     */
    public static Thread execute(String sql, String shortFileName, String path){
        ExportExcelBySQL ex= new ExportExcelBySQL();
        ex.setParameter("sql", sql);
        ex.setParameter("filename", shortFileName);
        ex.setParameter("location", path);
        Thread thread=new Thread(ex);
        thread.start();
        return thread;
    }
    public void run(){
        String sql, location, fileName;
        QueryRequest req;
        boolean ak,pk;
        location= (String) params.get("location");
        fileName=(String) params.get("filename");
        String datasource =(String) params.get("datasource");
        if(nds.util.Validator.isNull(datasource)) datasource= "java:/DataSource";
        
        Connection con=null;
        try {
            Context ctx = new InitialContext();
            DataSource ds = (DataSource) ctx.lookup (datasource);
            con= ds.getConnection();
            HSSFWorkbook wb = new HSSFWorkbook();

            HSSFSheet sheet;
            sql=(String) params.get("sql");
            if (sql !=null){
                sheet= wb.createSheet("download");
                createSheetData(wb,sheet, con, sql);
            }else{
                // multiple sheet
                String[] sqls= (String[]) params.get("sqls"); // 2 pair elements, first is sheet name, second is sql
                for(int i=0;i< sqls.length;i+=2){
                     sheet= wb.createSheet(sqls[i]);
                     createSheetData(wb,sheet, con, sqls[i+1]);
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
            logger.error("Could not export to excel file:" + location + File.separator + fileName, ex);
            throw new NDSRuntimeException("Could not export to excel file:" + location + File.separator + fileName, ex);
        }finally{
            if( con !=null){try{ QueryEngine.getInstance().closeConnection(con);}catch(Exception e){}}
        }
    }
    public void createSheetData(HSSFWorkbook wb,HSSFSheet sheet, Connection con,String sql) throws Exception{
        QueryRequest req;
        ResultSet rs=null;
        try {
            HSSFCellStyle style=getDefaultStyle(wb,false);

            short i;
            // Create a row and put some cells in it. Rows are 0 based.
            short row=0;
            HSSFRow excel_row = sheet.createRow(row);
            HSSFCell cell;
            rs=con.createStatement().executeQuery(sql);

            // create header information
            String[] colNames=getColumnNames(rs);
            for( i=0;i< colNames.length  ; i++){
                cell= excel_row.createCell((short)i);
                cell.setCellType(HSSFCell.CELL_TYPE_STRING);
                cell.setCellStyle(getDefaultStyle(wb, true));
                cell.setEncoding(HSSFCell.ENCODING_UTF_16);
                cell.setCellValue(colNames[i]) ;
            }
            int[] cols= this.getColumnTypes(rs);
            java.util.Date date;
            double d;
            int dn;
            String s;
            while( rs.next() ){
                row ++;
                excel_row = sheet.createRow(row);
                for ( i=0 ;i< cols.length;i++){
                    cell=excel_row.createCell(i);
                    switch( cols[i]){
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

        }finally{
            if( rs !=null){try{ rs.close();}catch(Exception e2){}}
        }

    }
}