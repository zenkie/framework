package nds.excel;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

import nds.log.Logger;
import nds.log.LoggerManager;
import nds.query.QueryEngine;
import nds.query.QueryRequestImpl;
import nds.query.QueryResult;
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
import org.apache.poi.hssf.util.HSSFColor;
import org.json.JSONObject;

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
    private HSSFCellStyle getDefaultStyle(HSSFWorkbook wb,boolean isnull){
        // Create a new font and alter it.
        HSSFFont font = wb.createFont();
        font.setFontHeightInPoints((short)10);
        font.setFontName("宋体");
      
        // Fonts are set into a style so create a new one to use.
        HSSFCellStyle style = wb.createCellStyle();
        //is not null background color is HSSFColor.RED
        if(!isnull){
        	//System.out.println(isnull);
            font.setColor((short) HSSFColor.RED.index);
            font.setBoldweight(font.BOLDWEIGHT_BOLD);
        }
        style.setFont(font);
   
        

        return style;
    }
    /**
     * Create excel template
     *   * jack add 关联名称输出 扩展属性{reflable:{"tabid":12853,"ref_id":1853}} 支持
     */
    private void createExcelTemplate(Table table){
        try {
            HSSFWorkbook wb = new HSSFWorkbook();
            HSSFSheet sheet = wb.createSheet(table.getName());
            HSSFRow row = sheet.createRow((short)0);
       
            boolean isASITable=( table instanceof nds.schema.AttributeDetailSupportTableImpl);
            ArrayList al= table.getAllColumns() ;
            int j=0;Column column;String desc;
            for ( int i=0;i< al.size();i++){
                column= (Column) al.get(i);
                if ( !column.isModifiable( Column.ADD )) continue;
                desc= column.getDescription(Locale.CHINA);
                JSONObject jo;
                try {
					jo = column.getJSONProps().getJSONObject("reflable");
					int lable_id = jo.optInt("ref_id", 0);
					int lable_tabid = jo.optInt("tabid", 0);
					TableManager manager = TableManager.getInstance();
					Table reftable = manager.getTable(lable_tabid);
					QueryRequestImpl query = QueryEngine.getInstance().createRequest(null);
					query.setMainTable(lable_tabid);
					query.addSelection(reftable.getAlternateKey().getId());
					query.addParam(reftable.getPrimaryKey().getId(), ""+ lable_id);
					QueryResult rs = QueryEngine.getInstance().doQuery(query);
					if (lable_id != 0|| (rs != null && rs.getTotalRowCount() > 0)) {
						while (rs.next()) {
							logger.debug(rs.getObject(1).toString());
							desc = rs.getObject(1).toString();
						}
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					// e.printStackTrace();
					//logger.debug("col get reflable is null");
				}
            
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
                HSSFCellStyle style=getDefaultStyle(wb,column.isNullable());
                // Create a cell and put a value in it.
                HSSFCell cell = row.createCell((short)j);
                cell.setCellType(HSSFCell.CELL_TYPE_STRING);
                cell.setCellStyle(style);
                //cell.setEncoding(HSSFCell.ENCODING_UTF_16);
                cell.setCellValue( desc);//+"("+ typeMark+")"
                short leng= (short)(column.getLength() * 256 );
                if ( desc.length()  * 256 * 2/* 2 byte chinese */ > leng) leng=(short)( desc.length()  * 256 * 2);
                //sheet.setColumnWidth((short)j,leng) ;
                sheet.autoSizeColumn((short)j) ;
              
                j ++;
            }

            sheet.createFreezePane(0,1,0,1);
            FileOutputStream fileOut = new FileOutputStream(root+"/"+ table.getName() +".xls");
            wb.write(fileOut);
            fileOut.close();

        }
        catch (IOException ex) {
            logger.error("Could not create Excel template file for table:"+ table.getName() , ex);
        }


    }
}