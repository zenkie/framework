package nds.excel;

import java.io.*;

import java.util.*;
import java.util.regex.Pattern;

import nds.control.event.DefaultWebEvent;
import nds.control.util.ValueHolder;
import nds.control.web.ClientControllerWebImpl;
import nds.log.Logger;
import nds.log.LoggerManager;
import nds.query.QueryUtils;
import nds.schema.Column;
import nds.schema.Table;
import nds.schema.TableManager;
import nds.util.*;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

/**
 * Import Excel file to db, will get file content from file system
 * then import each records in file to db, and log output(error) to
 * specified file
 * 
 * Excel file can be xls, txt and csv format
 */
public class ImportExcel implements Runnable{
    private static Logger logger=LoggerManager.getInstance().getLogger(ImportExcel.class.getName());
    private String srcFile;
    private int tableId;
    private int startRow;// logical, start from 0
    private boolean sendEmail;
    /**
     * 对于文本格式(file_format='txt')，支持分隔符(txt_type=token)和固定宽度(txt_type=fix)，
     * 分隔符(txt_token)支持tab(\t),space( ),comma(,),and others(xx)，连续分隔作为单个处理，
     * 固定宽度的需给出每个列的宽度定义(txt_fix_len)，不足用空格补齐 
     */
    private Properties params;
    private InputStream excelStream=null;
    private ClientControllerWebImpl controller;
    private Locale locale;
    
    
    
    public ImportExcel(Locale locale) {
        params= new  Properties();
        this.locale=locale;
    }
    public void setController( ClientControllerWebImpl ctrl){
        controller= ctrl;
    }
    /**
     * @param lineNo start from 1
     */
    public void setStartRow(int lineNo){
        startRow= lineNo -1;
    }
    public void setMainTable(int tableId){
        this.tableId= tableId;
    }
    public void setSourceFile(String excelFile){
        this.srcFile= excelFile;
    }
    public void setSourceInputStream(InputStream excelStream){
        this.excelStream=excelStream;
    }
    public void setParameter(String paramName, String paramValue){
        params.setProperty(paramName, paramValue);
    }
      /**
     * Get cell value which should be defined in by column, note col may not
     * be maintable's column, it can be fk_table's ak column.
     */
    private String getCellValue(int rowNum, HSSFCell cell, Column col){
        if( cell==null) return "";
        String s="",t; int colType; long l;double d;
        try{
            if( col.isValueLimited()){
                t=cell.getStringCellValue();
                s= (t==null?"":t.trim());
                s= TableManager.getInstance().getColumnValueByDescription(col.getId(),s,locale);
            }else{
            	int cellType= cell.getCellType();
            switch(col.getType() ){
            	case Column.STRING :
	                try{t=cell.getStringCellValue();}
	                catch(NumberFormatException e2){
	                    // cell is a number format, so try using numeric
	                    d = cell.getNumericCellValue();
	                    if((long)d==d) t=( (long)d)+"";
	                	else t= d+"";
	                }
	                s= (t==null?"":t.trim());
	                break;
                case Column.NUMBER:
                    if( cellType== cell.CELL_TYPE_STRING) s = cell.getStringCellValue();
                	else {
                		d= cell.getNumericCellValue();
                        s= String.valueOf(d); // null will be '0'
                	}
                    break;
                case Column.DATENUMBER:
                    try{
                    	java.util.Date dt= cell.getDateCellValue();
                    	s=(dt==null?"":((java.text.SimpleDateFormat)QueryUtils.dateNumberFormatter.get()).format( dt));
                    }catch(Exception ed){
                    	// not a date format, try using string 
                    	s= cell.getStringCellValue();
                    }
                    break;
                case Column.DATE:
                    try{
                    	java.util.Date dt= cell.getDateCellValue();
                    	s=(dt==null?"":((java.text.SimpleDateFormat)QueryUtils.dateFormatter.get()).format( dt));
                    }catch(Exception ed){
                    	// not a date format, try using string 
                    	s= cell.getStringCellValue();
                    }
                    break;
                default:
                    logger.error("Unsupported column type:"+ col.getType());
            }
            }
        }catch(Exception e){
            logger.error("Error parsing cell(" +rowNum+","+ cell.getCellNum() + ") as column " + col+":"+ e);
        }
        //logger.debug(" cell(" +rowNum+","+ cell.getCellNum() + ")="+ s);
        return s;
    }
    /**
     *  sample :col = Employee.DepartmentID, then return Department_NO
     *          col = Employee.ModifierID, then return Modifier_No
     *          col = Employee.CreationDate, then return CreationDate
     * special  col = Employee.OwnerName( user table's name field) then return
     *                OwnerName_name
     * @since 2.0, sample will be:
     *  col=Employee.DepartmentID, then return departmentid__no
     *  col=Employee.OwnerName, then return ownername__name
     */
    private String getColumnName(Column col){
        String colName;
        if( col.getReferenceTable() !=null){
            Column ak= col.getReferenceTable().getAlternateKey();
            int len= col.getName().length();
            /*
            if( col.getName().toUpperCase().lastIndexOf("ID")== len-2){
                colName= col.getName().substring(0, len-2)+"_"+ ak.getName() ;
            }else{
                colName= col.getName()+"_"+ ak.getName() ;
            }*/
            colName=( col.getName()+"__"+ ak.getName()).toLowerCase();
        }else{
            colName= col.getName();
        }
//        logger.debug("colName for "+ col+ ":"+ colName);
        return colName;
    }
    public DefaultWebEvent createEvent() throws NDSException{
        DefaultWebEvent event=new DefaultWebEvent("CommandEvent");
        event.setParameter("command", "ObjectCreate"); // default value, may override by out parameters
//        event.setParameter("nds.control.ejb.UserTransaction", "N"); set in jsp
        String key;
        for(Iterator it=params.keySet().iterator() ;it.hasNext();){
            key=(String) it.next();
            event.setParameter(key, params.getProperty(key));
        }

        Table table= TableManager.getInstance().getTable(tableId);

        Column col;
        String cv;

        ArrayList asc=table.getShowableColumns(Column.ADD);
        // filter not modifiable columns
        ArrayList columns=new ArrayList();
        for( int i=0;i< asc.size();i++){
            col= (Column) asc.get(i);
            if( col.isModifiable(Column.ADD)) columns.add(col);
        }
        ArrayList directColumnOfData= new ArrayList(); // if column is fk, the fk table's ak will be set here, so make it easier check input data type

        ArrayList colNames=new ArrayList();// if column is FK, the name will be like "col_ak", such as "product(id)_no"
        /**
         * 需求：在导入盘点单明细时，数量需要乘以一个倍数作为整箱货品记入
         */
        int multiplyNum=Tools.getInt(params.getProperty("multiply_num"), -1);
        int multiplyColumnIndex=-1; // index in columns array
        for( int i=0;i< columns.size();i++){
            col=(Column) columns.get(i);
            colNames.add(getColumnName(col));
            if(multiplyNum>1 && col.getName().contains("QTY") && 
            		col.getType()== Column.NUMBER && multiplyColumnIndex==-1){
            	multiplyColumnIndex=i;
            }
            if( col.getReferenceTable() !=null){
                directColumnOfData.add( col.getReferenceTable().getAlternateKey());
            }else{
                directColumnOfData.add( col);
            }
        }
        
        try{
        
	        if("txt".equals(params.getProperty("file_format"))){
	        	//txt file handling
	            /**
	             * 对于文本格式(file_format='txt')，支持分隔符(txt_type=token)和固定宽度(txt_type=fix)，
	             * 分隔符(txt_token)支持tab(\t),space( ),comma(,),and others(xx)，连续分隔作为单个处理，
	             * 固定宽度的需给出每个列的宽度定义(txt_fix_len)，不足用空格补齐 
	             */
	        	if(excelStream==null) throw new NDSException("must set inputstream");
	        	BufferedReader  isr=new BufferedReader(new InputStreamReader(excelStream, "GBK"));
	        	
	        	boolean txtLineTypeIsToken= "token".equals(params.getProperty("txt_type","token"));
	        	StringTokenizer fixedLength=new StringTokenizer( params.getProperty("txt_fix_len","20,5"),",");
	        	String token=  params.getProperty("txt_token");
	        	
	        	Pattern tokenPattern=null;
	        	
	        	int[] fixedLengths=new int[columns.size()];
	        	int[] startIdx=new int[columns.size()];
	        	int[] endIdx=new int[columns.size()];
	        	if(txtLineTypeIsToken){
	        		//
	        		if(Validator.isNull(token)) throw new NDSException("未设置分隔符");
	        		tokenPattern= Pattern.compile(token);
	        	}else{
		        	int cnt=0;
	        		 while(fixedLength.hasMoreTokens()){ 
	        			 fixedLengths[cnt]=Tools.getInt(fixedLength.nextToken(), -1);
	        			 cnt++;
	        		 }
	        		 startIdx[0]=0;
	        		 endIdx[0]=fixedLengths[0];
	        		 
        			 for(int i=0;i<fixedLengths.length;i++){
        				 if(fixedLengths[i] <1){
        					 //nullable?
        					 if(! ((Column)columns.get(i)).isNullable())
        						 throw new NDSException("固定宽度列定义错误："+params.getProperty("txt_fix_len","20,5"));
        				 }
        				 if(i>0){
        					 startIdx[i]=startIdx[i-1]+fixedLengths[i-1];
        					 endIdx[i]=startIdx[i]+fixedLengths[i];
        				 }
        			 }
	        	}
	        	
	        	ArrayList[] colData= new ArrayList[columns.size()];
	        	for(int i=0;i<columns.size();i++) colData[i]=new ArrayList();
	        	String line= isr.readLine();
	        	int lcnt =0;
	        	String colPart;
	        	while(line!=null){
	        		if(line.trim().length()>0){
		        		if(txtLineTypeIsToken){
		        			//分隔符
		        			String[] s=tokenPattern.split(line);
		        			for(int i=0;i<colData.length && i< s.length;i++ ){
		        				colData[i].add(s[i].trim());
		        			}
		        			// s element count may less than expected
		        			for(int i=s.length;i<colData.length;i++ )colData[i].add("");
		        			
		        		}else{
		        			//固定宽度
		        			for(int i=0;i<fixedLengths.length;i++){
		        				try{
		        					colPart= line.substring(startIdx[i], endIdx[i]);
		        				}catch(IndexOutOfBoundsException  e){
		        					if(endIdx[i]>line.length()){
		        						try{
		        							colPart= line.substring(startIdx[i]);
		        						}catch(IndexOutOfBoundsException  e2){
		        							colPart="";
		        						}
		        					}else
		        						colPart="";
		        				}
		        				colData[i].add(colPart.trim());
		        			}
		        		}
		        		lcnt++;
	        		}
	        		line= isr.readLine();
	        	}
	        	isr.close();
	        	for( int j=0;j< columns.size();j++){
	        		 //处理倍增请求
	        		if(multiplyColumnIndex==j){
	        			event.setParameter( (String)colNames.get(j) ,multiply(colData[j].toArray(),multiplyNum)  );
	        		}else
	        			event.setParameter( (String)colNames.get(j) ,colData[j].toArray() );
	        	}
	        }else{
	        	//xls file handling
	            POIFSFileSystem fs ;
	            // check input stream first, if not found, use srcFile name
	            if ( this.excelStream !=null) fs= new POIFSFileSystem(excelStream);
	            else fs=new POIFSFileSystem(new FileInputStream(srcFile));
	
	            HSSFWorkbook wb = new HSSFWorkbook(fs);
	            HSSFSheet sheet = wb.getSheetAt(0);
	            logger.debug("Last row num:"+  sheet.getLastRowNum());
	            String[][] colData= new String[columns.size()][1+sheet.getLastRowNum() -startRow];
	            logger.debug("Create array["+columns.size()+"]["+ (sheet.getLastRowNum() -startRow)+"]" );
	            for ( int i= startRow; i<= sheet.getLastRowNum();i++){
	                HSSFRow row = sheet.getRow(i);
	                if( row==null) continue;
	                for( int j=0;j< directColumnOfData.size();j++){
	                    col= (Column) directColumnOfData.get(j);
	                    HSSFCell cell = row.getCell((short)j);
	                    cv= getCellValue(i, cell, col);
	                    colData[j][i-startRow]= cv;
	                }
	            }
	             for( int j=0;j< columns.size();j++){
	//                 logger.debug("param for "+ columns.get(j)+ ":"+ (String)colNames.get(j));
//	            	处理倍增请求
		        		if(multiplyColumnIndex==j){
		        			event.setParameter( (String)colNames.get(j) ,multiply(colData[j],multiplyNum)  );
		        		}else
		        			event.setParameter( (String)colNames.get(j) ,colData[j] );
	             }
		
		        
	        }
	       
    		

        }catch(Exception e){
            logger.error("Error exporting to excel" , e);
            throw new NDSException("在处理请求时出现异常："+ e.getLocalizedMessage() );
        }
        return event;

    }
    /**
     * Multiply every element with multiplyNum
     * @param e
     * @param multiplyNum
     * @return
     */
    private Object[] multiply(Object[] e, int multiplyNum ){
    	Object[] r=new Object[e.length];
    	for(int i=0;i< e.length;i++){
    		Object ele=e[i];
    		if(ele==null) r[i]=null;
    		else{
    			try{
    				r[i] = Double.parseDouble(ele.toString()) * multiplyNum;
    			}catch(Throwable t){
    				logger.error("Fail to convert to double:"+ele.toString()+":"+ t);
    				r[i]=null;
    			}
    		}
    		
    	}
    	return r;
    }
    /**
     * Create a DefaultWebEvent of command ObjectCreate and send to controller to handle
     * parse ValueHolder and set into output file(txt)
     *
     */
    public void run(){
        try{
             DefaultWebEvent event= createEvent();
             ValueHolder holder=controller.handleEvent(event);

        }catch(Exception e){
            logger.error("Error exporting to excel" , e);
        }

    }
}