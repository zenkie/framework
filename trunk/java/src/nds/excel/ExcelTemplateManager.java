package nds.excel;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

import nds.log.Logger;
import nds.log.LoggerManager;
import nds.schema.Column;
import nds.schema.Table;
import nds.schema.TableManager;
import nds.util.Tools;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

/**
 * Currently only Chinese excel template supported
 * 
 * @author yfzhu@agilecontrol.com
 */
public class ExcelTemplateManager {
    private static Logger logger=LoggerManager.getInstance().getLogger(ExcelTemplateManager.class.getName());

    private String root; // root path, such as /posdb, must not have '/' at the end
    public ExcelTemplateManager() {

    }

    /**
     * @param rootPath the root path to store all excel templates
     */
    public void init(String rootPath){
        root=rootPath;
        checkPathExist(root);
    }
    private void checkPathExist(String path){
        try{
        File f= new File(path);
        f.mkdirs() ;
        }catch(Exception e){
            logger.error("Error creating path:"+ path, e);
        }
    }
    /**
     * @return excel template file path
     */
    public String getTemplate(int tableId){
        TableManager manager=TableManager.getInstance();
        Table table= manager.getTable(tableId);
        String filePath=root+"/"+ table.getName() +".xls";
        File file= new File(filePath);
        if(! file.exists() || shouldCreate(table, file)){
            createExcelTemplate(table);
        }
        return filePath;
    }
    /**
     * Check whether table should be create or not
     * 目前的处理逻辑：
     * 如果表的最后修改日期 > 文件的创建日期，则返回true
     * @param tb
     * @return false if table is up to date
     */
    private boolean shouldCreate(Table tb, File file){
    	 return file.lastModified() < tb.getModifiedDate().getTime();
    }
    private HSSFCellStyle getDefaultStyle(HSSFWorkbook wb){
        // Create a new font and alter it.
        HSSFFont font = wb.createFont();
        font.setFontHeightInPoints((short)10);
        font.setFontName("宋体");

        // Fonts are set into a style so create a new one to use.
        HSSFCellStyle style = wb.createCellStyle();
        style.setFont(font);
        return style;
    }
    /**
     * Create excel template
     */
    private void createExcelTemplate(Table table){
        try {
            HSSFWorkbook wb = new HSSFWorkbook();
            HSSFSheet sheet = wb.createSheet(table.getName());
            HSSFRow row = sheet.createRow((short)0);
            HSSFCellStyle style=getDefaultStyle(wb);
            boolean isASITable=( table instanceof nds.schema.AttributeDetailSupportTableImpl);
            ArrayList al= table.getAllColumns() ;
            int j=0;Column column;String desc;
            for ( int i=0;i< al.size();i++){
                column= (Column) al.get(i);
                if ( !column.isModifiable( Column.ADD )) continue;
                desc= column.getDescription(Locale.CHINA);
                /*if ( column.getReferenceTable() !=null){
                    desc += column.getReferenceTable().getAlternateKey().getDescription(Locale.CHINA) ;
                }*/
                if(isASITable && column.getName().equals("M_PRODUCT_ID")) desc="条码";
                if(isASITable && column.getName().equals("M_ATTRIBUTESETINSTANCE_ID")) desc="(留空)";
                
                String typeMark;
                switch( column.getType()){
	                case Column.NUMBER : typeMark="数字型";break;
	                case Column.DATENUMBER : typeMark="数字型";break;
                    case Column.DATE : typeMark="日期型";break;
                    case Column.STRING  : typeMark="字符型";break;
                    default: typeMark="";
                }
                // Create a cell and put a value in it.
                HSSFCell cell = row.createCell((short)j);
                cell.setCellType(HSSFCell.CELL_TYPE_STRING);
                cell.setCellStyle(style);
                cell.setEncoding(HSSFCell.ENCODING_UTF_16);
                cell.setCellValue( desc);//+"("+ typeMark+")"
                short leng= (short)(column.getLength() * 256 );
                if ( desc.length()  * 256 * 2/* 2 byte chinese */ > leng) leng=(short)( desc.length()  * 256 * 2);
                //sheet.setColumnWidth((short)j,leng) ;
                sheet.autoSizeColumn((short)j) ;
                j ++;
            }


            FileOutputStream fileOut = new FileOutputStream(root+"/"+ table.getName() +".xls");
            wb.write(fileOut);
            fileOut.close();

        }
        catch (IOException ex) {
            logger.error("Could not create Excel template file for table:"+ table.getName() , ex);
        }


    }
}