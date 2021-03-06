package nds.excel;

import java.io.*;

import java.util.*;
import java.util.regex.Pattern;

import nds.control.event.DefaultWebEvent;
import nds.control.util.ValueHolder;
import nds.control.web.*;
import nds.log.Logger;
import nds.log.LoggerManager;
import nds.query.QueryUtils;
import nds.schema.Column;
import nds.schema.Table;
import nds.schema.TableManager;
import nds.util.*;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;

import org.apache.poi.poifs.filesystem.POIFSFileSystem;  
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Cell;  
import org.apache.poi.ss.usermodel.DateUtil;  
import org.apache.poi.ss.usermodel.Row;  
import org.apache.poi.ss.usermodel.Sheet;  
import org.apache.poi.ss.usermodel.Workbook;  
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

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
    private int startColumn; //start from 0
    private int skipChars; //start from 0, skip first chars in the beginning of the line
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
    /**
     * @param col start from 1
     */
    public void setStartColumn(int col){
        startColumn= col -1;
    }
    /**
     * Skip some chars in the beginning of text line (fixed length) 
     * @param startSkipChars start from 0
     */
    public void setStartSkip(int startSkipChars){
    	skipChars=startSkipChars;
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
    * for xxcel 2007
    */
   private String getxxCellValue(int rowNum, XSSFCell  cell, Column col){
       if( cell==null) return "";
       String s="",t; int colType; long l;double d;
       try{
           if( col.isValueLimited()){
               t=cell.getStringCellValue();
               s= (t==null?"":t.trim());
               s= TableManager.getInstance().getColumnValueByDescription(col.getId(),s,locale);
           }else{
           	int cellType= cell.getCellType();
           	if(cellType== Cell.CELL_TYPE_BLANK){
           		s="";// empty;
           	}else{
           	switch(col.getType() ){
           	case Column.STRING :
	                try{t=cell.getStringCellValue();}
	                catch(NumberFormatException e2){
	                    // cell is a number format, so try using numeric
	                    d = cell.getNumericCellValue();
	                    if((long)d==d) t=( (long)d)+"";
	                	else t= d+"";
	                }
	                //字符类型的字段值，所对应的EXCEL 可能是数字型的错误补货
	                catch(IllegalStateException e3){
	                    d = cell.getNumericCellValue();
	                    if((long)d==d) t=( (long)d)+"";
	                	else t= d+"";
	                }
	                s= (t==null?"":t.trim());
	                break;
               case Column.NUMBER:
                   if( cellType== Cell.CELL_TYPE_STRING) s = cell.getStringCellValue();
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
           }
       }catch(Exception e){
           logger.error("Error parsing cell(" +rowNum+","+ cell.getColumnIndex() + ") as column " + col+":"+ e+"-"+cell.getCellType());
       }
       //logger.debug(" cell(" +rowNum+","+ cell.getCellNum() + ")="+ s);
       return s;
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
            	if(cellType== Cell.CELL_TYPE_BLANK){
            		s="";// empty;
            	}else{
            	switch(col.getType() ){
            	case Column.STRING :
	                try{t=cell.getStringCellValue();}
	                catch(NumberFormatException e2){
	                    // cell is a number format, so try using numeric
	                    d = cell.getNumericCellValue();
	                    if((long)d==d) t=( (long)d)+"";
	                	else t= d+"";
	                }
	                //字符类型的字段值，所对应的EXCEL 可能是数字型的错误补货
	                catch(IllegalStateException e3){
	                    d = cell.getNumericCellValue();
	                    if((long)d==d) t=( (long)d)+"";
	                	else t= d+"";
	                }
	                s= (t==null?"":t.trim());
	                break;
                case Column.NUMBER:
                    if( cellType== Cell.CELL_TYPE_STRING) s = cell.getStringCellValue();
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
            }
        }catch(Exception e){
            logger.error("Error parsing cell(" +rowNum+","+ cell.getColumnIndex() + ") as column " + col+":"+ e+"-"+cell.getCellType());
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
    /**
     * ObjectCreate event
     * @return
     * @throws Exception
     */
    public DefaultWebEvent createEvent (UserWebImpl user) throws Exception{
    	DefaultWebEvent event;
    	if("true".equals(params.getProperty("partial_update"))){
    		event= createEventListUpdate();
    	}else{
    		event=createEventObjCreate(user);
    	}
    	event.put("nds.query.querysession",user.getSession());
    	event.put("JAVA.UTIL.LOCALE", user.getLocale());

    	return event;
    	
    }
    /**
     * BatchModify event, has parameter "update_columns" specify moidify columns
     * @return
     * @throws Exception
     */
    private DefaultWebEvent createEventListUpdate() throws Exception{
    	
        DefaultWebEvent event=new DefaultWebEvent("CommandEvent");
       	event.setParameter("command","ListUpdate");
       	event.setParameter("nds.start_time",String.valueOf(System.currentTimeMillis()));
       	event.setParameter("nds.control.ejb.UserTransaction", "N");// will have no transaction control inside, and multiple transactions will be triggered then 
       	String key;
        for(Iterator it=params.keySet().iterator() ;it.hasNext();){
            key=(String) it.next();
            event.setParameter(key, params.getProperty(key));
        }
        logger.debug(event.toDetailString());
        
        Table table= TableManager.getInstance().getTable(tableId);

        Column col;
        String cv;
        
        ArrayList columns=new ArrayList();
        String columnStr= params.getProperty("update_columns");
        StringTokenizer st=new StringTokenizer(columnStr,",");

        while(st.hasMoreTokens()){
        	col=table.getColumn(st.nextToken());
        	columns.add(col);
        }

        ArrayList directColumnOfData= new ArrayList(); // if column is fk, the fk table's ak will be set here, so make it easier check input data type

        ArrayList colNames=new ArrayList();// if column is FK, the name will be like "col_ak", such as "product(id)_no"
        for( int i=0;i< columns.size();i++){
            col=(Column) columns.get(i);
            colNames.add(getColumnName(col));
            if( col.getReferenceTable() !=null){
                directColumnOfData.add( col.getReferenceTable().getAlternateKey());
            }else{
                directColumnOfData.add( col);
            }
        }
        
    	//按行来构造数据，而非ObjectCreate的按列方式
    	ArrayList rows=new ArrayList();
        try{

	        if(!"xls".equals(params.getProperty("file_format"))){
	        	//txt file handling
	            /**
	             * 对于文本格式(file_format='txt')，支持分隔符(txt_type=token)和固定宽度(txt_type=fix)，
	             * 分隔符(txt_token)支持tab(\t),space( ),comma(,),and others(xx)，连续分隔作为单个处理，
	             * 固定宽度的需给出每个列的宽度定义(txt_fix_len)，不足用空格补齐 
	             */
	        	if(excelStream==null) throw new NDSException("must set inputstream");
	        	BufferedReader  isr=new BufferedReader(new InputStreamReader(excelStream, "GBK"));
	        	boolean txtLineTypeIsToken;
	        	if( "pandian".equals(params.getProperty("file_format"))){
	        		txtLineTypeIsToken=false;
	        	}else{
	        		txtLineTypeIsToken="token".equals(params.getProperty("txt_type","token"));
	        	}
	        	StringTokenizer fixedLength=new StringTokenizer( params.getProperty("txt_fix_len","20,5"),",");
	        	String token=  params.getProperty("txt_token");
	        	
	        	Pattern tokenPattern=null;
	        	
	        	int[] fixedLengths=new int[columns.size()];
	        	int[] startIdx=new int[columns.size()];
	        	int[] endIdx=new int[columns.size()];
	        	if(txtLineTypeIsToken){
	        		//
	        		if(Validator.isNull(token) || "undefined".equals(token)) throw new NDSException("未设置分隔符");
	        		tokenPattern= Pattern.compile(token);
	        	}else{
		        	int cnt=0;
	        		 while(fixedLength.hasMoreTokens()){ 
	        			 fixedLengths[cnt]=Tools.getInt(fixedLength.nextToken(), 0);
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
	        	
	        	
	        	String line= isr.readLine();
	        	int lcnt =0;
	        	String colPart;
	        	while(line!=null){
	        		if(line.trim().length()>0){
	        			DefaultWebEvent dwe=createRowModifyEvent();
	        			
		        		if(txtLineTypeIsToken){
		        			//分隔符
		        			String[] s=tokenPattern.split(line); //=起始忽略的列+实际对应的字段列
		        			for(int i=0;i<colNames.size() && i< s.length-startColumn;i++ ){
		        				//skip start columns, if not 0
		        				dwe.setParameter((String)colNames.get(i), s[i+startColumn].trim());
		        			}
		        			// s element count may less than expected
		        			for(int i=s.length-startColumn;i<colNames.size();i++ )
		        				dwe.setParameter((String)colNames.get(i), "");
		        			
		        		}else{
		        			//固定宽度，前skipChars个字符是要删除的,故字符获取的起始索引是skipChars
		        			for(int i=0;i<fixedLengths.length;i++){
		        				try{
		        					colPart= line.substring(startIdx[i]+skipChars, endIdx[i]+skipChars);
		        				}catch(IndexOutOfBoundsException  e){
		        					if(endIdx[i]>line.length()){
		        						try{
		        							colPart= line.substring(startIdx[i]+skipChars);
		        						}catch(IndexOutOfBoundsException  e2){
		        							colPart="";
		        						}
		        					}else
		        						colPart="";
		        				}
		        				dwe.setParameter((String)colNames.get(i), colPart.trim());
		        			}
		        		}
		        		rows.add(dwe);
		        		lcnt++;
		        		dwe.setParameter("nds.row.index",String.valueOf(lcnt));
	        		}
	        		line= isr.readLine();
	        	}
	        	isr.close();
	        	if ( this.excelStream !=null)excelStream.close();
	        }else{
	        	//xls file handling
	        	//Workbook wb = null; 
	        	if (srcFile.endsWith(".xls")) {  
	            POIFSFileSystem fs ;
	            // check input stream first, if not found, use srcFile name
	            if ( this.excelStream !=null) fs= new POIFSFileSystem(excelStream);
	            else fs=new POIFSFileSystem(new FileInputStream(srcFile));
	               // inp = new FileInputStream(FilePath);  
	               // wb = (Workbook) new HSSFWorkbook(fs);  
//	            } else if (srcFile.endsWith(".xlsx")) {   
//	                wb = (Workbook) new XSSFWorkbook(excelStream);  
//	            }
	        	//Sheet sheet = wb.getSheetAt(0);
	            HSSFWorkbook wb = new HSSFWorkbook(fs);
	            HSSFSheet sheet = wb.getSheetAt(0);
	            logger.debug("Last row num:"+  sheet.getLastRowNum());
//	            String[][] colData= new String[columns.size()][1+sheet.getLastRowNum() -startRow];
	            logger.debug("Create array["+columns.size()+"]["+ (sheet.getLastRowNum() -startRow)+"]" );
	            for ( int i= startRow; i<= sheet.getLastRowNum();i++){
	            	HSSFRow row = sheet.getRow(i);
	                if( row==null) continue;
	                DefaultWebEvent dwe=createRowModifyEvent();
	                dwe.setParameter("nds.row.index",String.valueOf(i));
	                for( int j=0;j< colNames.size();j++){
	                    col= (Column) directColumnOfData.get(j);
	                    HSSFCell cell = row.getCell((j+startColumn)); //列从startColumn开始
	                    cv= getCellValue(i, cell, col);
	                    dwe.setParameter((String)colNames.get(j), cv);
	                }
	                rows.add(dwe);
	            }
	           
		        
	        }else if (srcFile.endsWith(".xlsx")) {   
	        	XSSFWorkbook  wb =(XSSFWorkbook)WorkbookFactory.create(excelStream);
	        	XSSFSheet  sheet = wb.getSheetAt(0);
	            logger.debug("Last row num:"+  sheet.getLastRowNum());
//	            String[][] colData= new String[columns.size()][1+sheet.getLastRowNum() -startRow];
	            logger.debug("Create array["+columns.size()+"]["+ (sheet.getLastRowNum() -startRow)+"]" );
	            for ( int i= startRow; i<= sheet.getLastRowNum();i++){
	            	XSSFRow  row = sheet.getRow(i);
	                if( row==null) continue;
	                DefaultWebEvent dwe=createRowModifyEvent();
	                dwe.setParameter("nds.row.index",String.valueOf(i));
	                for( int j=0;j< colNames.size();j++){
	                    col= (Column) directColumnOfData.get(j);
	                    XSSFCell cell = row.getCell((j+startColumn)); //列从startColumn开始
	                    cv= getxxCellValue(i, cell, col);
	                    dwe.setParameter((String)colNames.get(j), cv);
	                }
	                rows.add(dwe);
	            }
	        	
	        }
	        	if ( this.excelStream !=null)excelStream.close();
	        }

        }catch(Exception e){
            logger.error("Error exporting to excel" , e);
            if ( this.excelStream !=null)excelStream.close();
            throw new NDSException("在处理请求时出现异常："+ e.getLocalizedMessage() );
        }
        event.put("rows", rows);
        return event;    	
    }
    private DefaultWebEvent createRowModifyEvent(){
    	DefaultWebEvent e= new DefaultWebEvent("CommandEvent");
    	return e;
    	//e.setParameter("command", "ObjectModid")
    }
    /**
     * 支持部分更新 partial_update=true 时，不做Create，而是 Update, Update的字段从update_columns参数里获取。
     * 
     * @return
     * @throws NDSException
     */
    private DefaultWebEvent createEventObjCreate(UserWebImpl user) throws NDSException{
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
        //考虑到导入的时候应符合导入模版的匹配 所以获取字段时强制字段级别为最大2147483647
        ArrayList columns=table.getColumns(new int[]{Column.MASK_CREATE_EDIT}, false,2147483647);
        
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
            if(multiplyNum!=1 && col.getName().contains("QTY") && 
            		col.getType()== Column.NUMBER && multiplyColumnIndex==-1){
            	multiplyColumnIndex=i;
            }
            if( col.getReferenceTable() !=null){
                directColumnOfData.add( col.getReferenceTable().getAlternateKey());
            }else{
                directColumnOfData.add( col);
            }
        }
        logger.debug("colNames is:"+String.valueOf(colNames));
        
        try{
        
	        if(!"xls".equals(params.getProperty("file_format"))){
	        	//txt file handling
	            /**
	             * 对于文本格式(file_format='txt')，支持分隔符(txt_type=token)和固定宽度(txt_type=fix)，
	             * 分隔符(txt_token)支持tab(\t),space( ),comma(,),and others(xx)，连续分隔作为单个处理，
	             * 固定宽度的需给出每个列的宽度定义(txt_fix_len)，不足用空格补齐 
	             */
	        	if(excelStream==null) throw new NDSException("must set inputstream");
	        	BufferedReader  isr=new BufferedReader(new InputStreamReader(excelStream, "GBK"));
	        	
	        	boolean txtLineTypeIsToken;
	        	if("pandian".equals(params.getProperty("file_format")))
	        		txtLineTypeIsToken=false;
	        	else
	        		txtLineTypeIsToken= "token".equals(params.getProperty("txt_type","token"));
	        	
	        	StringTokenizer fixedLength=new StringTokenizer( params.getProperty("txt_fix_len","20,5"),",");
	        	String token=  params.getProperty("txt_token");
	        	
	        	Pattern tokenPattern=null;
	        	
	        	int[] fixedLengths=new int[columns.size()];
	        	int[] startIdx=new int[columns.size()];
	        	int[] endIdx=new int[columns.size()];
	        	if(txtLineTypeIsToken){
	        		//
	        		if(Validator.isNull(token) || "undefined".equals(token)) throw new NDSException("未设置分隔符");
	        		tokenPattern= Pattern.compile(token);
	        	}else{
		        	int cnt=0;
	        		 while(fixedLength.hasMoreTokens()){ 
	        			 fixedLengths[cnt]=Tools.getInt(fixedLength.nextToken(), 0);
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
		        			String[] s=tokenPattern.split(line); //前startColumn个列是忽略的
		        			for(int i=0;i<colData.length && i< s.length-startColumn;i++ ){
		        				colData[i].add(s[i+startColumn].trim());
		        			}
		        			// s element count may less than expected
		        			for(int i=s.length-startColumn;i<colData.length;i++ )colData[i].add("");
		        			
		        		}else{
		        			//固定宽度，前skipChars个字符是要删除的,故字符获取的起始索引是skipChars
		        			for(int i=0;i<fixedLengths.length;i++){
		        				try{
		        					colPart= line.substring(startIdx[i]+skipChars, endIdx[i]+skipChars);
		        				}catch(IndexOutOfBoundsException  e){
		        					if(endIdx[i]>line.length()){
		        						try{
		        							colPart= line.substring(startIdx[i]+skipChars);
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
            	 	Column cl=(Column)columns.get(j);
            	 	if(cl.getSecurityGrade()> user.getSecurityGrade())continue;

	        		 //处理倍增请求
	        		if(multiplyColumnIndex==j){
	        			event.setParameter( (String)colNames.get(j) ,multiply(colData[j].toArray(),multiplyNum)  );
	        		}else
	        			event.setParameter( (String)colNames.get(j) ,colData[j].toArray() );
	        	}
	        }else{
	        	//xls file handling
	        	
	        	//Workbook wb = null; 
	            logger.debug(srcFile);
	        	if (srcFile.endsWith(".xls")) {  
	            POIFSFileSystem fs ;
	            // check input stream first, if not found, use srcFile name

	            if ( this.excelStream !=null) fs= new POIFSFileSystem(excelStream);
	            else fs=new POIFSFileSystem(new FileInputStream(srcFile));
	               // inp = new FileInputStream(FilePath);  
	               // wb = (Workbook) new HSSFWorkbook(fs);  
	             //else if (srcFile.endsWith(".xlsx")) {   
	            //if ( this.excelStream !=null) fs= new POIFSFileSystem(excelStream);
	                //wb = (Workbook) new XSSFWorkbook(excelStream);  
	            //}
	        	//Sheet sheet = wb.getSheetAt(0);
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
	                    HSSFCell cell = row.getCell((j+startColumn));
	                    cv= getCellValue(i, cell, col);
	                    logger.debug("Cell data is:["+String.valueOf(j)+"]["+String.valueOf(i-startRow)+"]"+cv);
	                    colData[j][i-startRow]= cv;
	                }
	            }
	             for( int j=0;j< columns.size();j++){
	            	 	//filter no permission rows
	            	 	Column cl=(Column)columns.get(j);
	            	 	if(cl.getSecurityGrade()> user.getSecurityGrade())continue;
	            	 
	//                 logger.debug("param for "+ columns.get(j)+ ":"+ (String)colNames.get(j));
//	            	处理倍增请求
		        		if(multiplyColumnIndex==j){
		        			event.setParameter( (String)colNames.get(j) ,multiply(colData[j],multiplyNum)  );
		        		}else
		        			event.setParameter( (String)colNames.get(j) ,colData[j] );
	             }
	            // System.out.print(colData);
	             logger.debug("imp data xls is:"+colData.toString());        
	        }else if (srcFile.endsWith(".xlsx")) {   
	        	//suport xlsx 2007
	        	XSSFWorkbook  wb =(XSSFWorkbook)WorkbookFactory.create(excelStream);
	        	XSSFSheet  sheet = wb.getSheetAt(0);
	            logger.debug("Last row num:"+  sheet.getLastRowNum());
	            String[][] colData= new String[columns.size()][1+sheet.getLastRowNum() -startRow];
	            logger.debug("Create array["+columns.size()+"]["+ (sheet.getLastRowNum() -startRow)+"]" );
	            for ( int i= startRow; i<= sheet.getLastRowNum();i++){
	            	XSSFRow  row = sheet.getRow(i);
	                if( row==null) continue;
	                for( int j=0;j< directColumnOfData.size();j++){
	                    col= (Column) directColumnOfData.get(j);
	                    XSSFCell  cell = row.getCell((j+startColumn));
	                    cv= getxxCellValue(i, cell, col);
	                    logger.debug("Cell data is:["+String.valueOf(j)+"]["+String.valueOf(i-startRow)+"]"+cv);
	                    colData[j][i-startRow]= cv;
	                }
	            }
	             for( int j=0;j< columns.size();j++){
	            	 	//filter no permission rows
	            	 	Column cl=(Column)columns.get(j);
	            	 	if(cl.getSecurityGrade()> user.getSecurityGrade())continue;
	            	 
	//                 logger.debug("param for "+ columns.get(j)+ ":"+ (String)colNames.get(j));
//	            	处理倍增请求
		        		if(multiplyColumnIndex==j){
		        			event.setParameter( (String)colNames.get(j) ,multiply(colData[j],multiplyNum)  );
		        		}else
		        			event.setParameter( (String)colNames.get(j) ,colData[j] );
	             }
	            // System.out.print(colData);
	             logger.debug("imp data xlsx is:"+colData.toString());        
	        }
	        	if ( this.excelStream !=null)excelStream.close();
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
    	throw new NDSRuntimeException("deprecated, pls check");
        /*try{
             DefaultWebEvent event= createEvent(null);
             ValueHolder holder=controller.handleEvent(event);

        }catch(Exception e){
            logger.error("Error exporting to excel" , e);
        }*/

    }
}