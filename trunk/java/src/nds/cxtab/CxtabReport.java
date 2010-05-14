/*
 * Agile Control Technologies Ltd,. CO.
 * http://www.agileControl.com
 */
package nds.cxtab;

import java.util.*;
import java.sql.*;
import java.io.*;

import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.poifs.filesystem.*;
import nds.report.ReportTools;
import nds.schema.*;
import nds.security.Directory;
import nds.security.User;
import nds.control.util.SecurityUtils;
import nds.control.web.*;
import nds.control.event.*;
import nds.log.Logger;
import nds.log.LoggerManager;
import nds.query.*;
import nds.util.*;

import nds.jcrosstab.*;


/** 
 * Create cxtab report
 * 
 * @author yfzhu@agilecontrol.com
 */

public class CxtabReport {
    private static Logger logger= LoggerManager.getInstance().getLogger((CxtabReport.class.getName()));
	
	private String cxtabName;
	private int cxtabId=-1;
	private String filterExpr;
	private Expression filterExpression;
	private String filterSQL;
	private String filterDesc; // filter description
	private String fileName;
	private int userId;
	private String fileType;
	private int reportInstanceId=-1; // check ad_cxtab.AD_COLUMN_CXTABINST_ID , if set, will used this as instance id
									  // of query, and data will be limited to the range specified
	private jCrosstabResultSet jxrs;
	private ResultSet rs;
	private Properties props;
	private long startTime;
	private Table factTable;
	private String sql;
	private User user;
	private int processInstanceId; // ad_pinstance.id. This will be needed when doing cube exporting
	private ArrayList factDescs;
	
	private int recordsCount;// how many records read from db to construct this report
	
	private int dimCount=0; // dimension count, for sqlite table creation
	private int meaCount=0; // measure count, for sqlite table creation
	
	/**
	 * This will be needed when doing cube exporting
	 * @param d
	 */
	public void setAD_PInstance_ID(int d){
		this.processInstanceId=d;
	}
	/**
	 * Create report and save to file
	 *
	 */
	public String create(Connection conn) throws Exception{
			long startTime=System.currentTimeMillis();
			
			String file;	
			// prepare file
	        Configurations conf=(Configurations)nds.control.web.WebUtils.getServletContextManager().getActor(nds.util.WebKeys.CONFIGURATIONS);
			String exportRootPath=conf.getProperty("export.root.nds","/act/home");
			boolean isSystemInDebug=false;// "develope".equals( conf.getProperty("schema.mode", "production"));
			
			user= SecurityUtils.getUser( userId);    
			String filePath =exportRootPath + File.separator+user.getClientDomain()+File.separator+ user.getName();
			if("xls".equalsIgnoreCase(fileType)){
				QueryRequest query=this.prepareReport(conn,true, false,false); // generate sql only
				
				File f= new File(filePath);
				if(!f.exists())f.mkdirs();

				file=this.fileName+ ".xls";
				this.writeExcelFile(filePath+File.separator+file,query, conn);
				
				
		    }else if("htm".equalsIgnoreCase(fileType)){
		    	//default to html
				this.prepareReport(conn,false,true,false);
				
				File f= new File(filePath);
				if(!f.exists())f.mkdirs();

				HtmlFactory htmlFactory= new HtmlFactory();
		    	file=this.fileName+ ".htm";
		    	htmlFactory.writeHtmlFile(filePath+File.separator+file,jxrs ,props);
		    	if(this.cxtabId==250)logger.debug(jxrs.toDetailString());
		    }else if("cbx".equalsIgnoreCase(fileType)){
		    	//sqlite file for later analize in client
				QueryRequest query=this.prepareReport(conn,true, false,true); // generate sql only
				File f= new File(filePath);
				if(!f.exists())f.mkdirs();

				file=this.fileName+ ".cbx";
		    	this.writeSQLiteFile(filePath+File.separator+file,query, conn);
		    	
		    }else{
		    	//cube
				QueryRequest query=this.prepareReport(conn,true, false,true); // generate sql only
				File f= new File(filePath);
				if(!f.exists())f.mkdirs();

				file=this.fileName+ ".cub";
		    	this.writeCubeFile(filePath+File.separator+file,query, conn);
		    }
		    // create description file for report
		    createDescriptionFile(filePath+File.separator+file);
			
	  		//log running time
	  		logRunningTime(cxtabId, userId, (int)(System.currentTimeMillis()-startTime),recordsCount, conn);
		    
		    return file;
	    		    
	}
	/**
	 * Create description file in folder of user web folder, contents is from filterDesc
	 * @param file
	 */
	private void createDescriptionFile(String file){
		try{
			File f=new File(file);
			File descFolder=new File(f.getParent() + File.separator+ "desc");
			if(!descFolder.exists()) descFolder.mkdirs();
			String descFile= f.getParent() + File.separator+ "desc"+  File.separator+  f.getName();
			String p;
			if(nds.util.Validator.isNull(filterDesc)) p="";
			else p= ":"+filterDesc;
			nds.util.Tools.writeFile(descFile, "["+cxtabName+"]"+p, "UTF-8");
			 
		}catch(Throwable t){
			logger.error("Fail to write desc file for "+file+":"+ t);
		}
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
	 * Create excel file according to template, will write db records into
	 * sheet named "data"
	 * @param filePath the absolute path with file name
	 * @param query the group by query
	 */
	private void writeExcelFile(String filePath, QueryRequest query, Connection conn) throws Exception{
		// get excel template file 
		String template= (String)QueryEngine.getInstance().doQueryOne("select ATTR1 from ad_cxtab where id="+cxtabId, conn);
		logger.debug("reading template xls:"+ template);
		File f=template !=null? new File(template):null;
		if(f==null || !f.exists()) throw new NDSException("@template-file-not-found@");
		
		startTime=System.currentTimeMillis();
		ResultSet rs=null;
		Locale locale= user.locale;
		if(locale==null) locale= TableManager.getInstance().getDefaultLocale();
		try{
			rs=conn.createStatement(ResultSet.TYPE_FORWARD_ONLY,
						ResultSet.CONCUR_READ_ONLY).executeQuery(sql);
			FileInputStream fis=new FileInputStream(template);

			POIFSFileSystem fs=new POIFSFileSystem(fis);
            HSSFWorkbook wb = new HSSFWorkbook(fs);
            HSSFSheet sheet = wb.getSheet("data");
            if(sheet==null) throw new NDSException("@template-file-error@");
            //if(sheet==null) sheet=wb.createSheet("data");
            /**
             * @todo add excel macro to do test 
             */
            
            HSSFCellStyle style=getDefaultStyle(wb,false);

            org.apache.poi.hssf.usermodel.HSSFDataFormat format = wb.createDataFormat();
            HSSFCellStyle dateCellStyle = wb.createCellStyle();
            dateCellStyle.setDataFormat(format.getFormat("yyyy-MM-dd"));
            
            HSSFCellStyle datetimeCellStyle = wb.createCellStyle();
            datetimeCellStyle.setDataFormat(format.getFormat("yyyy-MM-dd hh:mm:ss"));
            
            short i;
            short factDescsSize= (short)factDescs.size();
            // Create a row and put some cells in it. Rows are 0 based.
            int row=0;
            HSSFRow excel_row ;
            HSSFCell cell;
           	row=0;// fist row should be title
           	excel_row = sheet.createRow(row);
           	ArrayList colNames=query.getAllSelectionDescriptions();
           	for( i=0;i< colNames.size() ; i++){
	             cell= excel_row.createCell((short)i);
	             cell.setCellType(HSSFCell.CELL_TYPE_STRING);
	             //cell.setCellStyle(getDefaultStyle(wb, true));
	             cell.setCellValue((String)colNames.get(i)) ;
	             
	         }  
           	for( short j=0;j< factDescsSize ;j++){
	             cell= excel_row.createCell((short)(j+i));
	             cell.setCellType(HSSFCell.CELL_TYPE_STRING);
	             //cell.setCellStyle(getDefaultStyle(wb, true));
	             cell.setCellValue((String)factDescs.get(j)) ;
	         }
           	
           	Column[] cols = getDisplayColumns(query,false,false);
            java.util.Date date;
            double d;
            String s;
            int dn;
            while( rs.next() ){
                row ++;
                //logger.debug("row:"+ row);
                excel_row = sheet.createRow(row);
                for ( i=0 ;i< cols.length;i++){
                    cell=excel_row.createCell(i);
                    switch( cols[i].getType()){
                    case Column.STRING :
                       s= rs.getString(i+1);
                       if( ! rs.wasNull() ){
                           cell.setCellType(HSSFCell.CELL_TYPE_STRING);
                           //cell.setCellStyle(style);
                           cell.setCellValue(s );
                       }
                       break;
                    case Column.NUMBER :
                       d= rs.getDouble(i+1);
                       if( ! rs.wasNull() ){
                           cell.setCellValue(d );
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
                       		 //cell.setCellStyle(datetimeCellStyle);
                       	 }
                   	 }else{
                   		 date= rs.getDate(i+1);
                   		 if( ! rs.wasNull() ) {
                       		 cell.setCellValue(date );
                       		 //cell.setCellStyle(dateCellStyle);
                       	 }
                   	 }
                        break;
                    default:
                        logger.debug("Find at cell(" + row + ","+ (i+1)+") type is invalid");
                    }
                }//end dimenstions
                for( short j=0;j< factDescsSize ;j++){
                	cell=excel_row.createCell((short)(j+i));
                	d= rs.getDouble(i+j+1);
                    if( ! rs.wasNull() ){
                        cell.setCellValue(d );
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
                            cell.setCellType(HSSFCell.CELL_TYPE_STRING);
                            //cell.setCellStyle(style);
                            cell.setCellValue(s);
                        }catch(Exception e){
                            logger.error("Could not interpret cell(" + j + ","+ (i+1)+"):" , e);
                        }

                    }
                }
            }

            // Write the output to a file
            FileOutputStream fileOut = new FileOutputStream(filePath);
            wb.write(fileOut);
            fileOut.close();
            fis.close();
            sheet=null;
            wb=null;
			
		}finally{
			if(rs!=null)try{ rs.close();}catch(Throwable t){}
	        int duration=(int)((System.currentTimeMillis()-startTime)/1000);
	        String sqlInfo="["+user.getNameWithDomain()+"]"+"("+duration+" s) "+factTable.getDescription(user.locale)+":"+filePath +" "+ processInstanceId+",size:"+ (f.length()/1024.0/1024.0)+"MB";

	        if (duration > 10) logger.info(sqlInfo );
			else logger.debug(sqlInfo);
		}	
	}

	/**
	 * 将 sql 语句写入ad_pinstance_para, name='filter' 的 info 字段
	 * 将 filepath 全路径，写入ad_pinstance_para, name='filename' 的 p_string_to 字段
	 * 
	 * @param filePath the absolute path with file name
	 * @param query the group by query
	 */
	private void writeCubeFile(String filePath, QueryRequest query, Connection conn) throws Exception{
		
		startTime=System.currentTimeMillis();
		
		ArrayList vec=new ArrayList();
		vec.add("update ad_pinstance_para set info="+ QueryUtils.TO_STRING(sql)+" where name='filter' and ad_pinstance_id="+processInstanceId );
		vec.add("update ad_pinstance_para set info="+ QueryUtils.TO_STRING(filePath)+" where name='filename' and ad_pinstance_id="+processInstanceId );
		QueryEngine.getInstance().doUpdate(vec, conn);
		
		// 调用程序 pcube.exe <processInstanceId> 完成数据准备
	    Configurations conf= (Configurations)WebUtils.getServletContextManager().getActor( nds.util.WebKeys.CONFIGURATIONS);
	    String pcube= conf.getProperty("program.pcube", "e:/act/bin/pcube.exe");
	    String log= conf.getProperty("program.pcube.log", "e:/act/tmp/pcube.log");
		
		CommandExecuter cmd= new CommandExecuter(log);
		
		int err=cmd.run(pcube +" "+ processInstanceId);
		// confirm cube file created
		File f=new File(filePath);
		if(!f.exists()){
			throw new NDSException("@cube-creation-failed@:CXB"+ this.processInstanceId);
		}
        int duration=(int)((System.currentTimeMillis()-startTime)/1000);
        String sqlInfo="["+user.getNameWithDomain()+"]"+"("+duration+" s) "+factTable.getDescription(user.locale)+":"+filePath +" "+ processInstanceId+",size:"+ (f.length()/1024.0/1024.0)+"MB";
        if (duration > 10) logger.info(sqlInfo );
		else logger.debug(sqlInfo);
	}	
	/**
	 * 生成SQLITE 文件供PC端分析，未完成
	 * 
	 * @param filePath the absolute path with file name
	 * @param query the group by query
	 */
	private void writeSQLiteFile(String filePath, QueryRequest query, Connection conn) throws Exception{
		
		startTime=System.currentTimeMillis();
		
		ArrayList vec=new ArrayList();
		vec.add("update ad_pinstance_para set info="+ QueryUtils.TO_STRING(sql)+" where name='filter' and ad_pinstance_id="+processInstanceId );
		vec.add("update ad_pinstance_para set info="+ QueryUtils.TO_STRING(filePath)+" where name='filename' and ad_pinstance_id="+processInstanceId );
		QueryEngine.getInstance().doUpdate(vec, conn);
		
		// 生成下载文件，包含：一个数据表，一个定义表
        Connection connection = null;  
        ResultSet resultSet = null, rs=null;  
        Statement stat = null;  
        PreparedStatement pstmt=null;
        //create table sql
        StringBuffer ctb=new StringBuffer("create table cxtabdata(");
        StringBuffer isb=new StringBuffer("insert into cxtabdata values(");
        for(int i=0;i<this.dimCount;i++) {
        	ctb.append("d").append(i).append(",");
        	isb.append("?,");
        }
        for(int i=0;i<this.meaCount-1;i++){
        	ctb.append("s").append(i).append(",");
        	isb.append("?,");
        }
        ctb.append("s").append(this.meaCount-1).append(")");
        isb.append("?)");
        
        try {  
            Class.forName("org.sqlite.JDBC");  
            connection = DriverManager  
                    .getConnection("jdbc:sqlite:"+ filePath);  
            stat = connection.createStatement();
            stat.executeUpdate("drop table if exists cxtabdef");
            stat.executeUpdate("create table cxtabdef(props text)");
            
            stat.executeUpdate("drop table if exists cxtabdata");
            stat.executeUpdate(ctb.toString());
            stat.close();
            pstmt = connection.prepareStatement("insert into cxtabdef values(?)");//prepare insert into sqlite
            //create json object
            
            /**
             * @todo not finished，忽然感觉到即便推到PC机器上进行运算，大数量的报表仍然不能快速完成运算
             * 按照客户要求定制一次性报表可能才是正道。
             * 
             */
            
            pstmt.close();
            pstmt = connection.prepareStatement(isb.toString());//prepare insert into sqlite
            int colcnt= this.dimCount+ this.meaCount;
            rs= conn.createStatement().executeQuery(sql); //load from oracle
            int c=0;
            // batch mode 
            connection.setAutoCommit(false);
            while(rs.next()){
            	for(int i=1;i<=colcnt;i++) pstmt.setObject(i, rs.getObject(i));
            	pstmt.addBatch();
            	c++;
            	if(c==10000){//commit every 10000 line
            		pstmt.executeBatch();
            		connection.commit();
            		c=0;
            	}
            }
            pstmt.executeBatch();
    		connection.commit();
            
        } finally {  
            try {rs.close();  }catch(Throwable t){}  
            try {pstmt.close();  }catch(Throwable t){}
            try {connection.close();  }catch(Throwable t){}
        }  
		
		// confirm cube file created
		File f=new File(filePath);
		if(!f.exists()){
			throw new NDSException("@cube-creation-failed@:CXB"+ this.processInstanceId);
		}
        int duration=(int)((System.currentTimeMillis()-startTime)/1000);
        String sqlInfo="["+user.getNameWithDomain()+"]"+"("+duration+" s) "+factTable.getDescription(user.locale)+":"+filePath +" "+ processInstanceId+",size:"+ (f.length()/1024.0/1024.0)+"MB";
        if (duration > 10) logger.info(sqlInfo );
		else logger.debug(sqlInfo);
	}	
	/**
	 * 
	 * @param column must isValueLimited=true
	 * @param pos, pos in select, 
	 * @return like decode( "b"+pos, v1, d1,v2,d2... "")
	 */
	private String getDecodeSQL(Column column, int pos){
		PairTable pt=column.getValues(user.locale);
		StringBuffer sb=new StringBuffer("DECODE(b");
		sb.append(pos);
		List keys=pt.keyList();
		for(int i=0;i<keys.size();i++){
			String key= (String)keys.get(i);
			String value=(String) pt.get(key);
			sb.append(",'").append(key).append("','").append( value).append("'");
		}
		sb.append(",'N/A')");
		return sb.toString();
	}
	/**
	 * Create jxrs or sql only  
	 * @param conn
	 * @param sqlOnly only generate sql for pivot contained excel
	 * @param isOnHTML when for html report, dimension that set "hidehtml"="N" will not queried
	 * @param translateLimitValue if true, will generate sql informat like 
	 *  "select c1,c2,decode(c3,'a','n','b','m',''), d1,d2 from (
	 *  	select c1,c2,c3,d1,d2 from xxx where yyy group by ggg)
	 *    
	 * @throws Exception
	 */
	private QueryRequest prepareReport(Connection conn, boolean sqlOnly, boolean isOnHTML,boolean translateLimitValue) throws Exception{
        startTime=System.currentTimeMillis();
		if(!sqlOnly && translateLimitValue) throw new java.lang.IllegalArgumentException("translateLimitValue only can be true when sqlOnly is true");
		QueryEngine engine=QueryEngine.getInstance();
		TableManager manager=TableManager.getInstance();
		if(cxtabId ==-1)cxtabId=Tools.getInt(engine.doQueryOne(
				"select id from ad_cxtab where name="+QueryUtils.TO_STRING(cxtabName)+
				" and ad_client_id=(select ad_client_id from users where id="+userId+")", conn), -1);
		
		List ed= engine.doQueryList("select ad_table_id,name,maxrows from ad_cxtab where id="+ cxtabId, conn);
		int factTableId= Tools.getInt(((List)ed.get(0)).get(0),-1);
		String cxtabDesc=(String) ((List)ed.get(0)).get(1);
		int maxRows= Tools.getInt(((List)ed.get(0)).get(2),-1);
		/*int factTableId= Tools.getInt(engine.doQueryOne(
				"select ad_table_id from ad_cxtab where id="+ cxtabId, conn), -1);*/
		factTable= manager.getTable(factTableId);
		
		/**
		 * 由于DCube的原因，必须先在sql 语句里构造V列，再构造H列，参见Dcube.AddRowEx 方法
		 */
		List dimensionsV= engine.doQueryList("select columnlink, description, measure_order,hidehtml from ad_cxtab_dimension where ad_cxtab_id="+
				cxtabId+" and isactive='Y' and position_='V' "+ (isOnHTML?"and hidehtml='N'":"") +" order by orderno asc", conn);
		List dimensionsH= engine.doQueryList("select columnlink, description, measure_order, hidehtml from ad_cxtab_dimension where ad_cxtab_id="+
				cxtabId+" and isactive='Y' and position_='H' "+ (isOnHTML?"and hidehtml='N'":"") +" order by orderno asc", conn);
		List dimensionsP= engine.doQueryList("select columnlink, description, measure_order, hidehtml from ad_cxtab_dimension where ad_cxtab_id="+
				cxtabId+" and isactive='Y' and position_='P' "+ (isOnHTML?"and hidehtml='N'":"") +" order by orderno asc", conn);
		
		// filter will be added to where clause directly
		String cxtabFilter= (String)engine.doQueryOne("select filter from ad_cxtab where id="+cxtabId, conn);
		
		//user= SecurityUtils.getUser( userId); 
		// check user output folder available size
		if ( ReportTools.getAvailableSpaceSize(user)<0){
			throw new NDSException("@no-free-space-for-user@:"+user.name);
		}
		
		
		Locale locale= user.locale;
		logger.debug("Locale for "+ user.getNameWithDomain()+"(id="+ userId+") is "+ locale);
		QuerySession qsession= QueryUtils.createQuerySession(userId, user.getSecurityGrade(),"", user.locale);
		QueryRequestImpl query=engine.createRequest(qsession);
		query.setMainTable(factTableId,true, cxtabFilter);

		//select
		if(dimensionsV!=null && dimensionsV.size()>0)for(int i=0;i< dimensionsV.size();i++){
			List dim= (List)dimensionsV.get(i);
			ColumnLink cl=new ColumnLink((String) dim.get(0));
			query.addSelection(cl.getColumnIDs(), false, (String)dim.get(1));
		}else{
			if(!sqlOnly)query.addSelection("1", "");
		}
		if(dimensionsH!=null && dimensionsH.size()>0)for(int i=0;i< dimensionsH.size();i++){
			List dim= (List)dimensionsH.get(i);
			ColumnLink cl=new ColumnLink((String) dim.get(0));
			query.addSelection(cl.getColumnIDs(), false, (String)dim.get(1));
		}else{
			if(!sqlOnly)query.addSelection("1", "");
		}
		if(!isOnHTML){
			// currently axis_p is not supported on html (yfzhu 2009-4-13)
			if(dimensionsP!=null && dimensionsP.size()>0)for(int i=0;i< dimensionsP.size();i++){
				List dim= (List)dimensionsP.get(i);
				ColumnLink cl=new ColumnLink((String) dim.get(0));
				query.addSelection(cl.getColumnIDs(), false, (String)dim.get(1));
			}
		}
		logger.debug("filterExpr="+ filterExpr);
		logger.debug("filterSQL="+ filterSQL);
		// where
		Expression expr=null;
        // user-defined filter
        if(filterExpression==null){
			if(filterExpr==null || filterExpr.trim().equalsIgnoreCase("undefined") ){
	        	// sql in format like 'in (xxx,yyy)', will save as expression also
	        	if(filterSQL!=null)
	        		expr=new Expression(new ColumnLink( new int[]{factTable.getPrimaryKey().getId()}), filterSQL, null);
	        }else{
	        	expr=new Expression(filterExpr);
	        }
        }else{
        	expr= filterExpression;
        }
        logger.debug("expr="+expr);
		//security filter
        Expression sexpr= SecurityUtils.getSecurityFilter(factTable.getName(), Directory.READ,userId,qsession);
        logger.debug("sexpr="+sexpr);
        if(sexpr!=null && !sexpr.isEmpty() ){
        	if ((expr!=null && !expr.isEmpty()))
        		sexpr= expr.combine(sexpr,SQLCombination.SQL_AND,null);
        }else{
        	sexpr= expr;
        }
        
		// column that stores report instance id, data should be limited to this range
		int reportInstanceColumnId=Tools.getInt(engine.doQueryOne(
				"select AD_COLUMN_CXTABINST_ID  from ad_cxtab where id="+cxtabId, conn), -1);
		Column reportInstanceColumn=null;
		if(reportInstanceColumnId!=-1){
			//reportInstanceColumn=manager.getColumn(reportInstanceColumnId);
			// check reportInstanceColumn must be column in fact table
			reportInstanceColumn =factTable.getColumn(reportInstanceColumnId);
		}
		
        if(reportInstanceColumn!=null && reportInstanceId!=-1){
        	Expression instanceExpr= new Expression(new ColumnLink(new int[]{reportInstanceColumn.getId()}),"="+reportInstanceId,null );
        	sexpr = instanceExpr.combine(sexpr,SQLCombination.SQL_AND,null);
        }
        logger.debug("sexpr="+sexpr);
        query.addParam(sexpr);
        
		List measures=  engine.doQueryList("select ad_column_id, function_, userfact, description,sgrade, VALUEFORMAT,valuename, param1,param2,param2 from ad_cxtab_fact where ad_cxtab_id="+
				cxtabId+" and isactive='Y' order by orderno asc",conn);
        //和平均有关的函数，包括avg, var,stdev，都不能让数据库进行group by 操作
        //而计数，最大，最小，累计等，可以先使用数据库完成有关group by运算
		//注意计算列 (以等号开头)的将不参与前期运算
        ArrayList facts=new ArrayList();
        factDescs=new ArrayList();
        int userSecurityGrade= user.getSecurityGrade();
        boolean isDBGroupByEnabled=true;
        boolean mustBeDBGroupBy=false;
        for(int i=0;i< measures.size();i++){
        	List mea= (List)measures.get(i);
        	int sgrade= Tools.getInt( mea.get(4),0);
        	if(sgrade>userSecurityGrade){
        		//current user should not see this column
        		continue;
        	}
        	String userFact= (String)mea.get(2);
        	if(Validator.isNotNull(userFact)){
        		if( userFact.startsWith("=")) continue;
        		// user fact 用于构造group by 语句，user fact 一般是一个汇总函数,如 count(distinct id)
        		facts.add(userFact);
        		factDescs.add(mea.get(3));
        		mustBeDBGroupBy=true;
        	}else{
            	String function= (String)mea.get(1);
        		int colId= Tools.getInt(mea.get(0),-1);
        		Column col= TableManager.getInstance().getColumn(colId);
        		
        		if(nds.jcrosstab.fun.FunUtil.isValidGroupByFunction(function)){
            		if(col.isVirtual())
            			facts.add( function+"("+ col.getName() + ")");
            		else 
            			facts.add( function+"("+ factTable.getName()+"."+col.getName() + ")");
            		factDescs.add(mea.get(3));

        		}else{
        			isDBGroupByEnabled=false;
        		}
        		
        		
        	}
        }
        // check record limit
    	String cntSQL= query.toCountSQL();
    	recordsCount= Tools.getInt(engine.doQueryOne(cntSQL, conn),-1);
    	if(maxRows >0 && recordsCount > maxRows )throw new NDSException("@report-rows-exeed-limit@("+recordsCount +">"+ maxRows+")");
        
        //if(isDBGroupByEnabled || sqlOnly){ // yfzhu marked up here 2009/4/14 since isDBGroupByEnabled=false, we should not do group by then 
        if(isDBGroupByEnabled){
            if(facts.size()==0) throw new NDSException("No fact valid for current report, check sum fields and their security grade");
        	sql= query.toGroupBySQL(facts );
        	
        	this.dimCount =query.getSelectionCount();
        	this.meaCount= facts.size();
        }else{
        	if(mustBeDBGroupBy) throw new NDSException("Cxtab configuration error, found user fact(db group by function) and invalid db group by function (e.g. avg) in the same time");
        	
        	this.dimCount= query.getSelectionCount();
        	this.meaCount =measures.size();
        	
        	for(int i=0;i< measures.size();i++){
            	List mea= (List)measures.get(i);
            	int sgrade= Tools.getInt( mea.get(4),0);
            	if(sgrade>userSecurityGrade){
            		//current user should not see this column
            		continue;
            	}
            	// may not have user fact 
           		int colId= Tools.getInt(mea.get(0),-1);
           		Column col= TableManager.getInstance().getColumn(colId);
           		if(col!=null)query.addSelection( colId );
           		else query.addSelection("1", "1") ;
            }
        	sql= query.toSQL();
        	 
        }
        
        /**
         * Handle sql for those contained limit value (should translate db value to description tha readable)
         */
        if( sqlOnly){
        	if(translateLimitValue){
        		//wrap in a sql
        		boolean shouldWrap=false;
        		 /*  "select b0,b1,decode(b2,'a','n','b','m',''), s0,s1 from (
        		 *  	select b0,b1,b2,s0,s1 from xxx where yyy group by ggg)
        		 */
        		StringBuffer gsql=new StringBuffer("SELECT ");
        		String decodeSQL;
        		int pos=0; 
        		if(dimensionsV!=null && dimensionsV.size()>0)for(int i=0;i< dimensionsV.size();i++){
        			List dim= (List)dimensionsV.get(i);
        			ColumnLink cl=new ColumnLink((String) dim.get(0));
        			if(cl.getLastColumn().isValueLimited()){
        				shouldWrap=true;
        				decodeSQL=getDecodeSQL(cl.getLastColumn(), pos);
        			}else{
        				decodeSQL= "b"+ pos;
        			}
        			if(pos>0)gsql.append(",");
        			pos++;
        			gsql.append(decodeSQL);
        		}
        		if(dimensionsH!=null && dimensionsH.size()>0)for(int i=0;i< dimensionsH.size();i++){
        			List dim= (List)dimensionsH.get(i);
        			ColumnLink cl=new ColumnLink((String) dim.get(0));
        			if(cl.getLastColumn().isValueLimited()){
        				shouldWrap=true;
        				decodeSQL=getDecodeSQL(cl.getLastColumn(), pos);
        			}else{
        				decodeSQL= "b"+ pos;
        			}
        			if(pos>0)gsql.append(",");
        			pos++;
        			gsql.append(decodeSQL);
        		}
        		if(dimensionsP!=null && dimensionsP.size()>0)for(int i=0;i< dimensionsP.size();i++){
        			List dim= (List)dimensionsP.get(i);
        			ColumnLink cl=new ColumnLink((String) dim.get(0));
        			if(cl.getLastColumn().isValueLimited()){
        				shouldWrap=true;
        				decodeSQL=getDecodeSQL(cl.getLastColumn(), pos);
        			}else{
        				decodeSQL= "b"+ pos;
        			}
        			if(pos>0)gsql.append(",");
        			pos++;
        			gsql.append(decodeSQL);
        		}    
        		if(shouldWrap){
        			if(isDBGroupByEnabled){
        				// every fact is named sxxx in sql
            			for(int i=0;i< facts.size();i++){
            				gsql.append(",s").append(i);
            			}
            			gsql.append(" FROM (").append(sql).append(")");
            			sql= gsql.toString();
        			}else{
        				// facts are named as bxxxx in sql
        				for(int i=0;i< measures.size();i++){
            				gsql.append(",b").append(pos);
            				pos++;
            			}
            			gsql.append(" FROM (").append(sql).append(")");
            			sql= gsql.toString();
        			}
        		}
        	}
        	return query;
        }
		
        //create jCrosstabber, if dimensionH or dimensionV is size=0, or more than one fact, will only create
        //list instead of crosstab
        /*if(dimensionsH==null || dimensionsH.size()==0 || dimensionsV==null || dimensionsV.size()==0 || measures.size()!=1){
        	DefaultWebEvent event=createEvent();
            ClientControllerWebImpl controller=(ClientControllerWebImpl)WebUtils.getServletContextManager().getActor(nds.util.WebKeys.WEB_CONTROLLER);
            if("xls".equalsIgnoreCase(fileType)){
        	    event.setParameter("command", "ExportExcel");
            }else{
        	    event.setParameter("command", "ExportHtml");
        	}
          	nds.control.util.ValueHolder vh=controller.handleEvent(event);
          	if(Tools.getInt( vh.get("code"), 0)!=0){
          		throw new NDSException((String)vh.get("message"));
          	}
        	
        }else{*/
        	// Crosstab
			jCrosstabber jx = new jCrosstabber();
			int pos=1;
			ColumnValueFormatter ft;
			ColumnInterpreter ci;
			StringBuffer dimensionsVDesc=new StringBuffer(); 
			if(dimensionsV!=null && dimensionsV.size()>0)for(int i=0;i< dimensionsV.size();i++){
				List dim= (List)dimensionsV.get(i);
				ColumnLink cl=new ColumnLink((String) dim.get(0));
	
				ci= manager.getColumnInterpreter( cl.getLastColumn().getId());
				if(ci!=null)
					ft= new ColumnValueFormatter(ci , locale);
				else ft=null;
				String desc= (String)dim.get(1);
				if(Validator.isNull(desc)) desc= cl.getDescription(locale);
				
				jx.addVerticalSliceByTableColumnIndex(pos,ft,desc);
				pos++;
				if(i>0) dimensionsVDesc.append(",");
				dimensionsVDesc.append( desc);
			}else{
				jx.addVerticalSliceByTableColumnIndex(pos,FakeColumnFormatter.INSTANCE,"");
				pos++;
			}
			StringBuffer dimensionsHDesc=new StringBuffer(); 
			if(dimensionsH!=null && dimensionsH.size()>0)for(int i=0;i< dimensionsH.size();i++){
				List dim= (List)dimensionsH.get(i);
				ColumnLink cl=new ColumnLink((String) dim.get(0));
				
				ci= manager.getColumnInterpreter( cl.getLastColumn().getId());
				if(ci!=null)
					ft= new ColumnValueFormatter(ci , locale);
				else ft=null;
				String desc= (String)dim.get(1);
				if(Validator.isNull(desc)) desc= cl.getDescription(locale);
				jx.addHorizontalSliceByTableColumnIndex(pos,ft,desc);
				pos++; 
				if(i>0) dimensionsHDesc.append(",");
				dimensionsHDesc.append( desc );
			}else{
				jx.addHorizontalSliceByTableColumnIndex(pos, FakeColumnFormatter.INSTANCE,"");
				pos++;
			}
			/*List measures=  engine.doQueryList("select ad_column_id, function_, userfact, description,param1,param2,param3 from ad_cxtab_fact where ad_cxtab_id="+
					cxtabId+" and isactive='Y'",conn);*/
			StringBuffer factDesc=new StringBuffer();
			for(int i=0;i< measures.size();i++){
				//TODO add param
				HashMap params=null;
				// should chang count to sum here
				String function= (String) ((List)measures.get(i)).get(1);
				if(isDBGroupByEnabled && "COUNT".equalsIgnoreCase(function)) function="SUM";
				
				String userFact=(String) ((List)measures.get(i)).get(2);
				
				
				jx.addDataRowsColumnByIndex(pos,function,params,
						(String) ((List)measures.get(i)).get(3), 
						(String) ((List)measures.get(i)).get(4)/*format*/,
						userFact/*userfact*/,
						(String) ((List)measures.get(i)).get(5)/*valuename*/
						);
				if(i>0) factDesc.append(","); 
				factDesc.append((String) ((List)measures.get(i)).get(3));
				if(!(userFact!=null && userFact.startsWith("=")))pos++;
				
			}
			String file=null;
			
			ResultSet rs=null;
			try{
				rs=conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY).executeQuery(sql);
				
	
			    jxrs = jx.getCrosstabResultSet(rs);
			  //  jxrs.setDataName(factDesc);
				
				props=new Properties();
				String filterDesc= query.getParamDesc(true);
				props.setProperty("title", factTable.getDescription(user.locale)+" - "+filterDesc+ " - "+  MessagesHolder.getInstance().getMessage(user.locale, "cxtabreport"));
				props.setProperty("subject", MessagesHolder.getInstance().getMessage(user.locale, "cxtabreport")+" - "+cxtabDesc/*factTable.getDescription(user.locale) */ );
				props.setProperty("facttable", factTable.getDescription(user.locale)  );
				props.setProperty("creator", MessagesHolder.getInstance().getMessage(user.locale, "creator")+" : "+user.name);
				props.setProperty("creationdate", MessagesHolder.getInstance().getMessage(user.locale, "creationdate")+" : "+((java.text.SimpleDateFormat)QueryUtils.dateTimeSecondsFormatter.get()).format(new java.util.Date()));
				props.setProperty("description", MessagesHolder.getInstance().getMessage(user.locale, "filter-description")+" : "+filterDesc);
				props.setProperty("axis_h", MessagesHolder.getInstance().getMessage(user.locale, "axis-h")+" : "+dimensionsHDesc.toString() );
				props.setProperty("axis_v", MessagesHolder.getInstance().getMessage(user.locale, "axis-v")+" : "+dimensionsVDesc.toString() );
				props.setProperty("facts", MessagesHolder.getInstance().getMessage(user.locale, "fact-desc")+" : "+factDesc.toString() );

				props.put("startfrom",  new Long(startTime));
				props.put("rowsfetched", new Long(jxrs.getRowsFetched()));
				props.setProperty("sql",  sql);
			}finally{
				if(rs!=null)try{ rs.close();}catch(Throwable t){}
		        int duration=(int)((System.currentTimeMillis()-startTime)/1000);
		        
		        String sqlInfo="["+user.getNameWithDomain()+"]"+"("+duration+" s) "+factTable.getDescription(user.locale)+":"+sql;
		        if (duration > 10) logger.info(sqlInfo );
				else logger.debug(sqlInfo);
			}		
			return query;
	}
	public void writeHtmlContent(Writer w) throws Exception{
		Connection conn=null;
		try{
			conn= QueryEngine.getInstance().getConnection();
			this.prepareReport(conn,false,true,false);
			
			// prepare file
	        Configurations conf=(Configurations)nds.control.web.WebUtils.getServletContextManager().getActor(nds.util.WebKeys.CONFIGURATIONS);
			boolean isSystemInDebug= "develope".equals( conf.getProperty("schema.mode", "production"));
			

			// for debug
			props.put("isdebug", new Boolean(isSystemInDebug));
			    
			
	    	HtmlFactory htmlFactory= new HtmlFactory();
	    	htmlFactory.writeHtmlBody(jxrs ,props, w);
		    
			    
		}finally{
			if(rs!=null)try{ rs.close();}catch(Throwable t){}
			if(conn!=null)try{ conn.close();}catch(Throwable t){}
	        int duration=(int)((System.currentTimeMillis()-startTime)/1000);
	        String sqlInfo="["+user.getNameWithDomain()+"]"+"("+duration+" s) "+factTable.getDescription(user.locale)+":"+sql;
	        if (duration > 10) logger.info(sqlInfo );
			else logger.debug(sqlInfo);
			
		}		
	}
	
	
	public int getReportInstanceId() {
		return this.reportInstanceId;
	}
	public void setReportInstanceId(int id) {
		this.reportInstanceId = id;
	}
	public int getUserId() {
		return userId;
	}
	public String getFileType() {
		return fileType;
	}
	public void setFileType(String fileType) {
		this.fileType = fileType;
	}
	public void setUserId(int userId) {
		this.userId = userId;
	}
	public String getCxtabName() {
		return cxtabName;
	}
	public String getFileName() {
		return fileName;
	}
	public String getFilterExpr() {
		return filterExpr;
	}
	public String getFilterSQL() {
		return filterSQL;
	}
	public void setCxtabName(String CxtabName) {
		this.cxtabName = CxtabName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public void setFilterExpr(String filterExpr) {
		this.filterExpr = filterExpr;
	}
	public void setFilterSQL(String filterSQL) {
		this.filterSQL = filterSQL;
	}

	public int getCxtabId() {
		return cxtabId;
	}
	public void setCxtabId(int cxtabId) {
		this.cxtabId = cxtabId;
	}
	public Expression getFilterExpression() {
		return filterExpression;
	}
	public void setFilterExpression(Expression filterExpression) {
		this.filterExpression = filterExpression;
	}
	public void setFilterDesc(String s){
		this.filterDesc=s;
	}
	/**
	 * Log report running time
	 * @param cxtabId
	 * @param userId
	 * @param duration miliseconds
	 * @param conn
	 * @throws Exception
	 */
	public static void logRunningTime(int cxtabId, int userId, int duration, int recordCnt, Connection conn) throws Exception{
		int seconds=(int)(duration / 1000);
		PreparedStatement pstmtm= conn.prepareStatement("merge into ad_cxtab_stat g using(select ? ad_cxtab_id, id user_ID, ad_client_id,ad_org_id from users where id=?  )b on(b.ad_cxtab_id=g.ad_cxtab_id and b.user_id=g.user_id)"+
				" when matched then update set g.cnt=g.cnt+1, g.tot_time=g.tot_time+?,g.tot_rows=g.tot_rows+?,g.max_time=GREATEST(g.max_time,?), g.max_rows=GREATEST(g.max_rows,?), last_duration=?,last_rows=?, last_time=sysdate "+
				" when not matched then insert (g.id, g.ad_client_id, g.ad_org_id, g.user_id,g.ad_cxtab_id,cnt,tot_time,tot_rows,max_time,max_rows,last_duration,last_rows,last_time)"+
				" values( get_sequences('ad_cxtab_stat'), b.ad_client_id, b.ad_org_id, b.user_ID,  b.ad_cxtab_id, 1, ?,?,?,?,?,?,sysdate)");
  		try{
  			int i=0;
  			pstmtm.setInt(++i,cxtabId);
  			pstmtm.setInt(++i,userId);
  			pstmtm.setInt( ++i,seconds);
  			pstmtm.setInt( ++i,recordCnt);
  			pstmtm.setInt( ++i,seconds);
  			pstmtm.setInt( ++i,recordCnt);
  			pstmtm.setInt( ++i,seconds);
  			pstmtm.setInt( ++i,recordCnt);
  			pstmtm.setInt( ++i,seconds);
  			pstmtm.setInt( ++i,recordCnt);
  			pstmtm.setInt( ++i,seconds);
  			pstmtm.setInt( ++i,recordCnt);
  			pstmtm.setInt( ++i,seconds);
  			pstmtm.setInt( ++i,recordCnt);
  			pstmtm.executeUpdate();
  		}finally{
  			try{pstmtm.close();}catch(Throwable tx){}
  		}		
	}
}
