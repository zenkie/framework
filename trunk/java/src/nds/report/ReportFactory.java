/*
 * Agile Control Technologies Ltd,. CO.
 * http://www.agileControl.com
 */
package nds.report;

import java.io.*;
import java.util.*;

import nds.schema.*;
import nds.util.*;
import nds.query.*;
import nds.log.*;
/**
 * Create report according to template
 * @author yfzhu@agilecontrol.com
 */

public class ReportFactory {
	
	/**
	 * Data retrieved from db can be sized by bytes(toString), to print it out, we use pixel unit.
	 *  
	 */
	public static final int PIXELS_PER_BYTE= 5;
	
	private static Logger logger= LoggerManager.getInstance().getLogger(ReportFactory.class.getName());
	public static int test2() throws Exception{
		String templateFolder= "E:/portal/server/default/deploy/liferay.war/html/nds/reports/template";
		TableManager manager= TableManager.getInstance();
		Table table= manager.getTable("c_crossorderitem");
		ArrayList al=new ArrayList();
		al.add(manager.getTable("c_crossorderitem"));
		String xml= Tools.readFile( templateFolder+"/subreport_list.jrxml", "UTF-8");
		createListReport(xml, table, "中路", table.getName().toLowerCase()+"_s", Column.PRINT_SUBLIST);
		return 0;
		
	}
	public static int test() throws Exception{
		String templateFolder= "E:/portal/server/default/deploy/liferay.war/html/nds/reports/template";
		TableManager manager= TableManager.getInstance();
		Table table= manager.getTable("c_crossorder");
		ArrayList al=new ArrayList();
		al.add(manager.getTable("c_crossorderitem"));
		String xml= Tools.readFile( templateFolder+"/object.jrxml", "UTF-8");
		createObjectReport( xml, table, "中路",table.getName().toLowerCase()+"_o", 319);
		return 0;
	}
	public static InputStream createObjectReportStream(String templateXML, Table table, String adClientName, String reportName, int objectId) throws IOException, NDSException{
		String s=createObjectReport(templateXML, table, adClientName,reportName, objectId);
		InputStream is=new ByteArrayInputStream(s.getBytes("UTF-8"));
		return is;
	}
	public static String createObjectReport(String templateXML, Table table, String adClientName, String reportName, int objectId) throws IOException, NDSException{
		String f=templateXML;// Tools.readFile(templateFile,"UTF-8");
		f=f.replaceAll("@TableDescription", table.getDescription(TableManager.getInstance().getDefaultLocale()));
		f=f.replaceAll("@Company", adClientName);
		f=f.replaceAll("@report_name",reportName);
		QueryRequestImpl query=QueryEngine.getInstance().createRequest(null);
		query.setMainTable(table.getId());
		query.addAllShowableColumnsToSelection(Column.PRINT_OBJECT, true);
		query.setRange(0, 1); // only one records
		query.addParam( table.getPrimaryKey().getId(), "="+ objectId);
		// insert field, column header and sql(limit to 100 rows)
		String s= getFieldsXML(table, query);
		f= StringUtils.replace(f, "<!--fields-->", s);
		Integer titleBandHeight= new Integer(45); // default height
		ArrayList p= new ArrayList();
		p.add(titleBandHeight);
		s= getObjectXML(table, query,p );
		f= StringUtils.replace(f, "<!--object_detail-->", s);
		titleBandHeight = (Integer)p.get(0);
		f= StringUtils.replace(f, "@title_band_height",titleBandHeight.toString());
		s="";
		String param="";
		
		ArrayList relateTables=table.getRefByTables();
		TableManager manager= TableManager.getInstance();
		for(int i=0;i<relateTables.size();i++ ){
			RefByTable rf= (RefByTable) relateTables.get(i);
			Table rft= manager.getColumn( rf.getRefByColumnId()).getTable();
			//only for main table's item table, will be set as subreport
			if(  rft.getName().equals(table.getName() +"ITEM")|| 
					rft.getName().equals(table.getName() +"LINE")){
				s += getSubReportXML(rft, i,rf.getId(), adClientName);
				param+= getSubReportSQLParamXML(rft, rf.getId());
			}
		}
		f= StringUtils.replace(f, "<!--relate_tables-->", s);
		f= StringUtils.replace(f, "@detail_band_height",""+( relateTables.size()*30));
		f= StringUtils.replace(f, "<!--subreport_sql_param-->", param);
		f= StringUtils.replace(f, "@sql_main", query.toSQLWithRange());
		//Tools.writeFile(destFolder+"/"+ reportName+".jrxml", f, "UTF-8"); 
		return f;
	}
	/**
	 * 
	 * @param table main table of subreport
	 * @param refTableId
	 * @return
	 */
	public static String getSubReportSQLParamName(Table table, int refTableId){
		return "sql_"+table.getName().toLowerCase()+"_"+refTableId;
	}
	private static String getSubReportSQLParamXML(Table table, int paramId) throws NDSException{
		String s= SUBREPORT_SQL_PARAM;
		//param name, should be distinct, so add "i" at the end.(main table may refer to same relate table twice.
		s= StringUtils.replace(s, "@param", getSubReportSQLParamName(table,paramId));
		QueryRequestImpl query=QueryEngine.getInstance().createRequest(null);
		query.setMainTable(table.getId());
		query.addAllShowableColumnsToSelection(Column.PRINT_SUBLIST, false);
		query.setRange(0, 100); // only 100 records
		
		s= StringUtils.replace(s, "@sql", query.toSQLWithRange());
		return s;
	}
	
	private static String getSubReportXML(Table table, int i, int paramId, String clientName) throws NDSException{
		int x= 0, y=i* 30; // subreport height is 20
		int width= 510, height=20;
		String sql="$P{"+ getSubReportSQLParamName(table,paramId)+"}";
		String url="nds.report.ReportTools.getReportURL(\"s\",\""+  table.getName() +"\",\""+ clientName +"\")";
		
		String s=( i==0? SUBREPORT_FIRST:SUBREPORT_NOT_FIRST);

		s= StringUtils.replace(s,"@x", x+"");
		s= StringUtils.replace(s,"@y", y+"");
		s= StringUtils.replace(s,"@width", width+"");
		s= StringUtils.replace(s,"@height", height+"");
		s= StringUtils.replace(s,"@sql", sql);
		s= StringUtils.replace(s,"@url", url);
		return s;
	}
	/**
	 * 
	 * @param templateXML
	 * @param table
	 * @param adClientName
	 * @param reportName
	 * @param columnMask ColumnMask , can be like Column.PRINT_LIST, OR Column.PRINT_SUBLIST
	 * @return
	 * @throws IOException
	 * @throws NDSException
	 */
	public static InputStream createListReportStream(String templateXML, Table table, String adClientName,String reportName, int columnMask) throws Exception{
		String s=createListReport(templateXML, table, adClientName, reportName,columnMask);
		InputStream is=new ByteArrayInputStream(s.getBytes("UTF-8"));
		return is;
	}
	public static String createListReport(String templateXML, Table table, String adClientName, String reportName, int columnMask) throws Exception{
		
		String f= templateXML;//Tools.readFile(templateXML,"UTF-8");
		f=f.replaceAll("@TableDescription", table.getDescription(TableManager.getInstance().getDefaultLocale()));
		f=f.replaceAll("@Company", adClientName);
		f=f.replaceAll("@report_name", reportName);
		QueryRequestImpl query=QueryEngine.getInstance().createRequest(null);
		query.setMainTable(table.getId());
		query.addAllShowableColumnsToSelection(columnMask, false);
		query.setRange(0, 100);
		
		// insert field, column header and sql(limit to 100 rows)
		String s= getFieldsXML(table, query);
		f= StringUtils.replace(f, "<!--fields-->", s);
		s= getColumnHeadersXML(table, query);
		f= StringUtils.replace(f, "<!--column_headers-->", s);
		s= getTextFieldsXML(table, query);
		f= StringUtils.replace(f, "<!--text_fields-->", s);
		f= StringUtils.replace(f, "@sql", query.toSQLWithRange());
		//Tools.writeFile(destFolder+"/"+ reportName+".jrxml", f, "UTF-8"); 
		return f;
	}
	private static String getObjectXML(Table table, QueryRequestImpl query , ArrayList p) throws NDSException{
		Integer titleBandHeight= (Integer)p.get(0);
		ColumnLengthEvaluator cle= new ColumnLengthEvaluator(); 
		StringBuffer sb=new StringBuffer();
		StringBufferWriter sbw= new StringBufferWriter(sb);
		ArrayList cols=query.getAllSelectionColumnLinks();
		int[] didx =query.getDisplayColumnIndices();
		String[] dcns= query.getDisplayColumnNames(false);
		int defaultStartX=10;
		int x=defaultStartX,y=titleBandHeight.intValue(),width,height=16, colIdx=-1, maxHeight=16;
		DisplaySetting ds=null; String s; 
		for(int i=0;i<didx.length;i++){
			colIdx++;
			if( colIdx >=2) {
				colIdx=0; // max 2 columns per row
				y+=maxHeight;
				maxHeight=16;
				height=16;
			}
			if(colIdx==0){
				x =defaultStartX;
			}else x=265; // 265 is 530/2, half size of the A4 portrait
			
			ColumnLink clink= (ColumnLink)cols.get(didx[i]);
			Column c=clink.getLastColumn();
			ds= c.getDisplaySetting();
			int printWidth=-1;
			try{printWidth=cle.getColumnPrintLength(c);}catch(Exception e){}
			if(ds.isUIController()){
	        	if (ds.getObjectType()==DisplaySetting.OBJ_HR){
	        		// occupy whole row
	        		if(colIdx !=0){
	        			y+=maxHeight; // next row
	        			maxHeight=16;
	        			x=defaultStartX;
	        			colIdx ++;
	        		}else{
	        			colIdx =2;
	        		}
	        		s=OBJ_HR;
	    			s=StringUtils.replace(s, "@x", ""+x);
	    			s=StringUtils.replace(s, "@y_text", ""+y);
	    			s=StringUtils.replace(s, "@y_line", ""+(y+14));
    				s=StringUtils.replace(s, "@text", c.getDescription(TableManager.getInstance().getDefaultLocale()) );
	    			s=StringUtils.replace(s, "@key_text", "hrt_"+didx[i]);
	    			s=StringUtils.replace(s, "@key_line", "hrl_"+didx[i]);
	    			sbw.println(s);
	        	}else{
	        		// blank
	        		if( ds.getColumns()> 1){
	        			// occupy whole row
	        			if(colIdx !=0){
	        				// current is at col 2, so start from row+2
	        				y+=16*2;
	        				colIdx=2;
	        			}else{
	        				// current is at col 1, so start from row+1
	        				y+=16;
	        				colIdx=2;
	        			}
	        		}// else do nothing
	        	}
	       		continue;

	        }
			if(colIdx >0 && ds.getColumns()>1){
				// whole row, and at column 2, so skip to next row
				x= defaultStartX;
				y +=maxHeight;
				maxHeight=16;
			}
			// display name:
            /*String dcns="";
            for(int j=0;j< clink.getColumns().length;j++) {
                dcns += "" + clink.getColumns()[j].getDescription();
            }
            dcns +=":";*/
            
			s= COLUMN_HEADER_LEFT;
			s=StringUtils.replace(s, "@x", ""+x);
			s=StringUtils.replace(s, "@y", ""+y);
			s=StringUtils.replace(s, "@width", "100");
			s=StringUtils.replace(s, "@height", ""+height);
			s=StringUtils.replace(s, "@text", dcns[i]);
			s=StringUtils.replace(s, "@key", "ch_"+didx[i]);
			sbw.println(s);
			
			// column field
			int cType= c.getType();
			// not consider button type, should not let button type column printable
			if( c.isValueLimited() || c.getValueInterpeter() !=null){
				width=100;
				s= TEXT_FIELD_STRING;
			}else{
			switch( cType ){
			case Column.DATE:
				if(  c.getSQLType()== SQLTypes.TIMESTAMP){
					width=100;
					s= TEXT_FIELD_DATETIME;
				}
				else {
					width=60;
					s= TEXT_FIELD_DATE;
				}
				
				break;
			case Column.NUMBER:
				width= 80;
				if ( c.getScale()>0) s= TEXT_FIELD_DOUBLE_LEFT;
				else s= TEXT_FIELD_LONG_LEFT;
				break;
			case Column.STRING:
			case Column.DATENUMBER:
				if( ds.getColumns()>1){
					width= 355;
					colIdx ++;
				}else{
					width= (printWidth>20? printWidth*PIXELS_PER_BYTE: 100);
				}
				//MAKE IT SIMPLE, ALL SET TO ONE ROW FOR PRINT (2007-10-10) 
				height = 16;
				//height = ds.getRows()* 16;
				maxHeight= height> maxHeight?height:maxHeight;
				/*if(ds.getRows()<2)
					s= TEXT_FIELD_STRING;
				else
					s= TEXT_FIELD_STRING_BORDER;
				*/
//				MAKE IT SIMPLE, ALL SET with no BORDER (2007-10-10) 
				s= TEXT_FIELD_STRING;
				break;
			default:
		        throw new NDSException("Not supported column type:"+ c.getType()+"(columnlink="+ clink+")");
			}
			}
			
			s= StringUtils.replace(s, "@x", ""+(x+100)); // take the column header into account
			s=StringUtils.replace(s, "@y", ""+y);
			
			s=StringUtils.replace(s, "@width", ""+width);
			s=StringUtils.replace(s, "@height", ""+height);
			if( c.isValueLimited() || c.getValueInterpeter() !=null){
				s=StringUtils.replace(s, "@field", "nds.schema.TableManager.getInstance().getColumnValueDescription("+ c.getId() +",\"\"+$F{b"+ didx[i]+"},nds.schema.TableManager.getInstance().getDefaultLocale())");
			}else{
				s=StringUtils.replace(s, "@field", "$F{b"+ didx[i]+"}");
			}
			s=StringUtils.replace(s, "@key", "fd_"+didx[i]);
			sbw.println(s);
			
		}
		titleBandHeight = new Integer(y+height);
		p.set(0,titleBandHeight );
		return sb.toString();				
	}
	/**
	 * Used for list report
	 * @param table
	 * @param query
	 * @return
	 * @throws NDSException
	 */
	private static String getTextFieldsXML(Table table, QueryRequestImpl query) throws Exception{
		ColumnLengthEvaluator cle= new ColumnLengthEvaluator(); 
		StringBuffer sb=new StringBuffer();
		StringBufferWriter sbw= new StringBufferWriter(sb);
		ArrayList cols=query.getAllSelectionColumnLinks();

		int[] didx =query.getDisplayColumnIndices();
		//logger.debug("cols_size:"+ cols.size()+ ",didx="+didx.length+", query="+ query.toSQL());
		int defaultStartX=10;
		int x=defaultStartX,y=1,width,height=14;
		for(int i=0;i<didx.length;i++){
			ColumnLink clink= (ColumnLink)cols.get(didx[i]);
			Column c=clink.getLastColumn();
			//logger.debug("clink:"+ clink.toString());
			width= cle.getColumnPrintLength(c)* PIXELS_PER_BYTE;
			int cType= c.getType();
			String s;
			//not consider button type, should not let button type column printable
			if( c.isValueLimited() || c.getValueInterpeter() !=null){
				s= TEXT_FIELD_STRING;
			}else{
			switch( cType ){
			case Column.DATE:
				if(  c.getSQLType()== SQLTypes.TIMESTAMP){
					s= TEXT_FIELD_DATETIME;
				}
				else {
					s= TEXT_FIELD_DATE;
				}
				
				break;
			case Column.NUMBER:
				if ( c.getScale()>0) s= TEXT_FIELD_DOUBLE_RIGHT;
				else s= TEXT_FIELD_LONG_RIGHT;
				break;
			case Column.STRING:
			case Column.DATENUMBER:
				s= TEXT_FIELD_STRING;
				break;
			default:
		        throw new NDSException("Not supported column type:"+ c.getType()+"(columnlink="+ clink+")");
			}
			}
            String dcns="";
            for(int j=0;j< clink.getColumns().length;j++) {
                dcns += "" + clink.getColumns()[j].getDescription(TableManager.getInstance().getDefaultLocale());
            }
            
			s= StringUtils.replace(s, "@x", ""+x);
			s=StringUtils.replace(s, "@y", ""+y);
			s=StringUtils.replace(s, "@width", ""+width);
			s=StringUtils.replace(s, "@height", ""+height);
			if( c.isValueLimited() || c.getValueInterpeter() !=null){
				s=StringUtils.replace(s, "@field", "nds.schema.TableManager.getInstance().getColumnValueDescription("+ c.getId() +",\"\"+$F{b"+ didx[i]+"},nds.schema.TableManager.getInstance().getDefaultLocale())");
			}else{
				s=StringUtils.replace(s, "@field", "$F{b"+ didx[i]+"}");
			}
			s=StringUtils.replace(s, "@key", "fd_"+didx[i]);
			sbw.println(s);
			x +=width+2;
		}
		return sb.toString();		
	}	
	/**
	 * For List report
	 * @param table
	 * @param query
	 * @return
	 * @throws NDSException
	 */
	private static String getColumnHeadersXML(Table table, QueryRequestImpl query) throws Exception{
		StringBuffer sb=new StringBuffer();
		StringBufferWriter sbw= new StringBufferWriter(sb);
		ArrayList cols=query.getAllSelectionColumnLinks();
		int[] didx =query.getDisplayColumnIndices();
		int defaultStartX=10;
		int x=defaultStartX,y=1,width,height=14;
		ColumnLengthEvaluator cle= new ColumnLengthEvaluator();
		String[] dcns= query.getDisplayColumnNames(false);
		for(int i=0;i<didx.length;i++){
			
			ColumnLink clink= (ColumnLink)cols.get(didx[i]);
			Column c=clink.getLastColumn();
			width= cle.getColumnPrintLength(c) * PIXELS_PER_BYTE; 
/*			switch( c.getType()){
			case Column.DATE:
				if(  c.getSQLType()== SQLTypes.TIMESTAMP) width=100;
				else width=60;
				break;
			case Column.NUMBER:
				width= 80;
				break;
			case Column.STRING:
				width= 100;
				break;
			default:
		        throw new NDSException("Not supported column type:"+ c.getType()+"(columnlink="+ clink+")");
			}
*/			
            /*String dcns="";
            for(int j=0;j< clink.getColumns().length;j++) {
                dcns += "" + clink.getColumns()[j].getDescription();
            }*/
			
            
			String s= COLUMN_HEADER.replaceAll("@x", ""+x);
			s=StringUtils.replace(s, "@y", ""+y);
			s=StringUtils.replace(s, "@width", ""+width);
			s=StringUtils.replace(s, "@height", ""+height);
			s=StringUtils.replace(s, "@text", dcns[i]);
			s=StringUtils.replace(s, "@key", "ch_"+didx[i]);
			sbw.println(s);
			x +=width+2;
		}
		return sb.toString();		
	}
	/**
	 * Fields are named like "field0", "field1"..., the number is as from query selection list, start from 0
	 * @param table
	 * @param query
	 * @return
	 * @throws NDSException
	 */
	private static String getFieldsXML(Table table, QueryRequestImpl query) throws NDSException{
		StringBuffer sb=new StringBuffer();
		StringBufferWriter sbw= new StringBufferWriter(sb);
		ArrayList cols=query.getAllSelectionColumnLinks();
		for(int i=0;i<cols.size();i++){
			ColumnLink col= (ColumnLink)cols.get(i);
			Column c=col.getLastColumn();
			String className="";
			switch( c.getType()){
				case Column.DATE:
					if(  c.getSQLType()== SQLTypes.TIMESTAMP) className= "java.sql.Timestamp";
					else className="java.util.Date";
					break;
				case Column.NUMBER:
					if ( c.getScale()>0) className="java.lang.Double";
					else className= "java.lang.Long";
					break;
				case Column.STRING:
				case Column.DATENUMBER:
					className="java.lang.String";
					break;
				default:
			        throw new NDSException("Not supported column type:"+ c.getType()+"(columnlink="+ col+")");
			}
			String s= "<field name=\"b"+ i+"\" class=\""+ className+ "\"><fieldDescription><![CDATA["+
				 col.toString()+"]]></fieldDescription></field>";
			sbw.println(s);
		}
		return sb.toString();
	}
	/**
	 * contains @x,@y,@width,@height,@text,@key
	 */
	private final static String COLUMN_HEADER="<staticText><reportElement mode=\"Opaque\" x=\"@x\" y=\"@y\" width=\"@width\" height=\"@height\" forecolor=\"#000000\" backcolor=\"#FFFFFF\" key=\"@key\" stretchType=\"NoStretch\"	positionType=\"FixRelativeToTop\"	isPrintRepeatedValues=\"true\" isRemoveLineWhenBlank=\"false\" isPrintInFirstWholeBand=\"false\" isPrintWhenDetailOverflows=\"true\"/> <box topBorder=\"None\" topBorderColor=\"#000000\" leftBorder=\"None\" leftBorderColor=\"#000000\" rightBorder=\"None\" rightBorderColor=\"#000000\" bottomBorder=\"None\" bottomBorderColor=\"#000000\"/> <textElement textAlignment=\"Center\" verticalAlignment=\"Top\" rotation=\"None\" lineSpacing=\"Single\">	<font reportFont=\"song\"/></textElement>	<text><![CDATA[@text]]></text>	</staticText>";
	private final static String COLUMN_HEADER_LEFT="<staticText><reportElement mode=\"Opaque\" x=\"@x\" y=\"@y\" width=\"@width\" height=\"@height\" forecolor=\"#000000\" backcolor=\"#FFFFFF\" key=\"@key\" stretchType=\"NoStretch\"	positionType=\"FixRelativeToTop\"	isPrintRepeatedValues=\"true\" isRemoveLineWhenBlank=\"false\" isPrintInFirstWholeBand=\"false\" isPrintWhenDetailOverflows=\"true\"/> <box topBorder=\"None\" topBorderColor=\"#000000\" leftBorder=\"None\" leftBorderColor=\"#000000\" rightBorder=\"None\" rightBorderColor=\"#000000\" bottomBorder=\"None\" bottomBorderColor=\"#000000\"/> <textElement textAlignment=\"Left\" verticalAlignment=\"Top\" rotation=\"None\" lineSpacing=\"Single\">	<font reportFont=\"song\"/></textElement>	<text><![CDATA[@text]]></text>	</staticText>";
	private final static String TEXT_FIELD_STRING="<textField isStretchWithOverflow=\"false\" pattern=\"\" isBlankWhenNull=\"true\" evaluationTime=\"Now\" hyperlinkType=\"None\"  hyperlinkTarget=\"Self\" ><reportElement mode=\"Opaque\" x=\"@x\" y=\"@y\" width=\"@width\" height=\"@height\" forecolor=\"#000000\" backcolor=\"#FFFFFF\" key=\"@key\" stretchType=\"NoStretch\" positionType=\"FixRelativeToTop\" isPrintRepeatedValues=\"true\" isRemoveLineWhenBlank=\"false\" isPrintInFirstWholeBand=\"false\" isPrintWhenDetailOverflows=\"false\"/> <box topBorder=\"None\" topBorderColor=\"#000000\" leftBorder=\"None\" leftBorderColor=\"#000000\" rightBorder=\"None\" rightBorderColor=\"#000000\" bottomBorder=\"None\" bottomBorderColor=\"#000000\"/> <textElement textAlignment=\"Left\" verticalAlignment=\"Top\" rotation=\"None\" lineSpacing=\"Single\"> <font reportFont=\"song\"/> </textElement> <textFieldExpression   class=\"java.lang.String\"><![CDATA[@field]]></textFieldExpression> </textField>";
	private final static String TEXT_FIELD_STRING_BORDER="<textField isStretchWithOverflow=\"false\" pattern=\"\" isBlankWhenNull=\"true\" evaluationTime=\"Now\" hyperlinkType=\"None\"  hyperlinkTarget=\"Self\" ><reportElement mode=\"Opaque\" x=\"@x\" y=\"@y\" width=\"@width\" height=\"@height\" forecolor=\"#000000\" backcolor=\"#FFFFFF\" key=\"@key\" stretchType=\"NoStretch\" positionType=\"FixRelativeToTop\" isPrintRepeatedValues=\"true\" isRemoveLineWhenBlank=\"false\" isPrintInFirstWholeBand=\"false\" isPrintWhenDetailOverflows=\"false\"/> <box topBorder=\"Thin\" topBorderColor=\"#000000\" leftBorder=\"Thin\" leftBorderColor=\"#000000\" rightBorder=\"Thin\" rightBorderColor=\"#000000\" bottomBorder=\"Thin\" bottomBorderColor=\"#000000\"/> <textElement textAlignment=\"Left\" verticalAlignment=\"Top\" rotation=\"None\" lineSpacing=\"Single\"> <font reportFont=\"song\"/> </textElement> <textFieldExpression   class=\"java.lang.String\"><![CDATA[@field]]></textFieldExpression> </textField>";
	//private final static String TEXT_FIELD_LONG_RIGHT="<textField isStretchWithOverflow=\"false\" pattern=\"#,##0\" isBlankWhenNull=\"true\" evaluationTime=\"Now\" hyperlinkType=\"None\"  hyperlinkTarget=\"Self\" ><reportElement mode=\"Opaque\" x=\"@x\" y=\"@y\" width=\"@width\" height=\"@height\" forecolor=\"#000000\" backcolor=\"#FFFFFF\" key=\"@key\" stretchType=\"NoStretch\" positionType=\"FixRelativeToTop\" isPrintRepeatedValues=\"true\" isRemoveLineWhenBlank=\"false\" isPrintInFirstWholeBand=\"false\" isPrintWhenDetailOverflows=\"false\"/> <box topBorder=\"None\" topBorderColor=\"#000000\" leftBorder=\"None\" leftBorderColor=\"#000000\" rightBorder=\"None\" rightBorderColor=\"#000000\" bottomBorder=\"None\" bottomBorderColor=\"#000000\"/> <textElement textAlignment=\"Right\" verticalAlignment=\"Top\" rotation=\"None\" lineSpacing=\"Single\"> <font reportFont=\"song\"/> </textElement> <textFieldExpression   class=\"java.lang.Long\"><![CDATA[@field]]></textFieldExpression> </textField>";
	private final static String TEXT_FIELD_LONG_RIGHT="<textField isStretchWithOverflow=\"false\" pattern=\"\" isBlankWhenNull=\"true\" evaluationTime=\"Now\" hyperlinkType=\"None\"  hyperlinkTarget=\"Self\" ><reportElement mode=\"Opaque\" x=\"@x\" y=\"@y\" width=\"@width\" height=\"@height\" forecolor=\"#000000\" backcolor=\"#FFFFFF\" key=\"@key\" stretchType=\"NoStretch\" positionType=\"FixRelativeToTop\" isPrintRepeatedValues=\"true\" isRemoveLineWhenBlank=\"false\" isPrintInFirstWholeBand=\"false\" isPrintWhenDetailOverflows=\"false\"/> <box topBorder=\"None\" topBorderColor=\"#000000\" leftBorder=\"None\" leftBorderColor=\"#000000\" rightBorder=\"None\" rightBorderColor=\"#000000\" bottomBorder=\"None\" bottomBorderColor=\"#000000\"/> <textElement textAlignment=\"Right\" verticalAlignment=\"Top\" rotation=\"None\" lineSpacing=\"Single\"> <font reportFont=\"song\"/> </textElement> <textFieldExpression   class=\"java.lang.Long\"><![CDATA[@field]]></textFieldExpression> </textField>";
	private final static String TEXT_FIELD_DOUBLE_RIGHT="<textField isStretchWithOverflow=\"false\" pattern=\"#,##0.00\" isBlankWhenNull=\"true\" evaluationTime=\"Now\" hyperlinkType=\"None\"  hyperlinkTarget=\"Self\" ><reportElement mode=\"Opaque\" x=\"@x\" y=\"@y\" width=\"@width\" height=\"@height\" forecolor=\"#000000\" backcolor=\"#FFFFFF\" key=\"@key\" stretchType=\"NoStretch\" positionType=\"FixRelativeToTop\" isPrintRepeatedValues=\"true\" isRemoveLineWhenBlank=\"false\" isPrintInFirstWholeBand=\"false\" isPrintWhenDetailOverflows=\"false\"/> <box topBorder=\"None\" topBorderColor=\"#000000\" leftBorder=\"None\" leftBorderColor=\"#000000\" rightBorder=\"None\" rightBorderColor=\"#000000\" bottomBorder=\"None\" bottomBorderColor=\"#000000\"/> <textElement textAlignment=\"Right\" verticalAlignment=\"Top\" rotation=\"None\" lineSpacing=\"Single\"> <font reportFont=\"song\"/> </textElement> <textFieldExpression   class=\"java.lang.Double\"><![CDATA[@field]]></textFieldExpression> </textField>";
	//private final static String TEXT_FIELD_LONG_LEFT="<textField isStretchWithOverflow=\"false\" pattern=\"#,##0\" isBlankWhenNull=\"true\" evaluationTime=\"Now\" hyperlinkType=\"None\"  hyperlinkTarget=\"Self\" ><reportElement mode=\"Opaque\" x=\"@x\" y=\"@y\" width=\"@width\" height=\"@height\" forecolor=\"#000000\" backcolor=\"#FFFFFF\" key=\"@key\" stretchType=\"NoStretch\" positionType=\"FixRelativeToTop\" isPrintRepeatedValues=\"true\" isRemoveLineWhenBlank=\"false\" isPrintInFirstWholeBand=\"false\" isPrintWhenDetailOverflows=\"false\"/> <box topBorder=\"None\" topBorderColor=\"#000000\" leftBorder=\"None\" leftBorderColor=\"#000000\" rightBorder=\"None\" rightBorderColor=\"#000000\" bottomBorder=\"None\" bottomBorderColor=\"#000000\"/> <textElement textAlignment=\"Left\" verticalAlignment=\"Top\" rotation=\"None\" lineSpacing=\"Single\"> <font reportFont=\"song\"/> </textElement> <textFieldExpression   class=\"java.lang.Long\"><![CDATA[@field]]></textFieldExpression> </textField>";
	private final static String TEXT_FIELD_LONG_LEFT="<textField isStretchWithOverflow=\"false\" pattern=\"\" isBlankWhenNull=\"true\" evaluationTime=\"Now\" hyperlinkType=\"None\"  hyperlinkTarget=\"Self\" ><reportElement mode=\"Opaque\" x=\"@x\" y=\"@y\" width=\"@width\" height=\"@height\" forecolor=\"#000000\" backcolor=\"#FFFFFF\" key=\"@key\" stretchType=\"NoStretch\" positionType=\"FixRelativeToTop\" isPrintRepeatedValues=\"true\" isRemoveLineWhenBlank=\"false\" isPrintInFirstWholeBand=\"false\" isPrintWhenDetailOverflows=\"false\"/> <box topBorder=\"None\" topBorderColor=\"#000000\" leftBorder=\"None\" leftBorderColor=\"#000000\" rightBorder=\"None\" rightBorderColor=\"#000000\" bottomBorder=\"None\" bottomBorderColor=\"#000000\"/> <textElement textAlignment=\"Left\" verticalAlignment=\"Top\" rotation=\"None\" lineSpacing=\"Single\"> <font reportFont=\"song\"/> </textElement> <textFieldExpression   class=\"java.lang.Long\"><![CDATA[@field]]></textFieldExpression> </textField>";
	private final static String TEXT_FIELD_DOUBLE_LEFT="<textField isStretchWithOverflow=\"false\" pattern=\"#,##0.00\" isBlankWhenNull=\"true\" evaluationTime=\"Now\" hyperlinkType=\"None\"  hyperlinkTarget=\"Self\" ><reportElement mode=\"Opaque\" x=\"@x\" y=\"@y\" width=\"@width\" height=\"@height\" forecolor=\"#000000\" backcolor=\"#FFFFFF\" key=\"@key\" stretchType=\"NoStretch\" positionType=\"FixRelativeToTop\" isPrintRepeatedValues=\"true\" isRemoveLineWhenBlank=\"false\" isPrintInFirstWholeBand=\"false\" isPrintWhenDetailOverflows=\"false\"/> <box topBorder=\"None\" topBorderColor=\"#000000\" leftBorder=\"None\" leftBorderColor=\"#000000\" rightBorder=\"None\" rightBorderColor=\"#000000\" bottomBorder=\"None\" bottomBorderColor=\"#000000\"/> <textElement textAlignment=\"Left\" verticalAlignment=\"Top\" rotation=\"None\" lineSpacing=\"Single\"> <font reportFont=\"song\"/> </textElement> <textFieldExpression   class=\"java.lang.Double\"><![CDATA[@field]]></textFieldExpression> </textField>";
	private final static String TEXT_FIELD_DATE="<textField isStretchWithOverflow=\"false\" pattern=\"yyyy/MM/dd\" isBlankWhenNull=\"true\" evaluationTime=\"Now\" hyperlinkType=\"None\"  hyperlinkTarget=\"Self\" ><reportElement mode=\"Opaque\" x=\"@x\" y=\"@y\" width=\"@width\" height=\"@height\" forecolor=\"#000000\" backcolor=\"#FFFFFF\" key=\"@key\" stretchType=\"NoStretch\" positionType=\"FixRelativeToTop\" isPrintRepeatedValues=\"true\" isRemoveLineWhenBlank=\"false\" isPrintInFirstWholeBand=\"false\" isPrintWhenDetailOverflows=\"false\"/> <box topBorder=\"None\" topBorderColor=\"#000000\" leftBorder=\"None\" leftBorderColor=\"#000000\" rightBorder=\"None\" rightBorderColor=\"#000000\" bottomBorder=\"None\" bottomBorderColor=\"#000000\"/> <textElement textAlignment=\"Left\" verticalAlignment=\"Top\" rotation=\"None\" lineSpacing=\"Single\"> <font reportFont=\"song\"/> </textElement> <textFieldExpression   class=\"java.util.Date\"><![CDATA[@field]]></textFieldExpression> </textField>";
	private final static String TEXT_FIELD_DATETIME="<textField isStretchWithOverflow=\"false\" pattern=\"yyyy/MM/dd HH:mm:ss\" isBlankWhenNull=\"true\" evaluationTime=\"Now\" hyperlinkType=\"None\"  hyperlinkTarget=\"Self\" ><reportElement mode=\"Opaque\" x=\"@x\" y=\"@y\" width=\"@width\" height=\"@height\" forecolor=\"#000000\" backcolor=\"#FFFFFF\" key=\"@key\" stretchType=\"NoStretch\" positionType=\"FixRelativeToTop\" isPrintRepeatedValues=\"true\" isRemoveLineWhenBlank=\"false\" isPrintInFirstWholeBand=\"false\" isPrintWhenDetailOverflows=\"false\"/> <box topBorder=\"None\" topBorderColor=\"#000000\" leftBorder=\"None\" leftBorderColor=\"#000000\" rightBorder=\"None\" rightBorderColor=\"#000000\" bottomBorder=\"None\" bottomBorderColor=\"#000000\"/> <textElement textAlignment=\"Left\" verticalAlignment=\"Top\" rotation=\"None\" lineSpacing=\"Single\"> <font reportFont=\"song\"/> </textElement> <textFieldExpression   class=\"java.sql.Timestamp\"><![CDATA[@field]]></textFieldExpression> </textField>";
	private final static String OBJ_HR="<staticText><reportElement mode=\"Opaque\" x=\"@x\" y=\"@y_text\" width=\"150\" height=\"16\" forecolor=\"#000000\" backcolor=\"#FFFFFF\" key=\"@key_text\" stretchType=\"NoStretch\" positionType=\"FixRelativeToTop\" isPrintRepeatedValues=\"true\" isRemoveLineWhenBlank=\"false\" isPrintInFirstWholeBand=\"false\" isPrintWhenDetailOverflows=\"true\"/> <box topBorder=\"None\" topBorderColor=\"#000000\" leftBorder=\"None\" leftBorderColor=\"#000000\" rightBorder=\"None\" rightBorderColor=\"#000000\" bottomBorder=\"None\" bottomBorderColor=\"#000000\"/><textElement textAlignment=\"Center\" verticalAlignment=\"Top\" rotation=\"None\" lineSpacing=\"Single\"> <font reportFont=\"song\" isBold=\"true\"/></textElement><text><![CDATA[@text]]></text></staticText><line direction=\"TopDown\"><reportElement mode=\"Opaque\" x=\"@x\" y=\"@y_line\" width=\"260\" height=\"0\" forecolor=\"#000000\" backcolor=\"#FFFFFF\" key=\"@key_line\" stretchType=\"NoStretch\" positionType=\"FixRelativeToTop\" isPrintRepeatedValues=\"true\" isRemoveLineWhenBlank=\"false\" isPrintInFirstWholeBand=\"false\" isPrintWhenDetailOverflows=\"false\"/><graphicElement stretchType=\"NoStretch\" pen=\"Thin\" fill=\"Solid\" /></line>";
	//positionType= FixRelativeToTop
	private final static String SUBREPORT_FIRST="<subreport  isUsingCache=\"true\"><reportElement mode=\"Opaque\" x=\"@x\" y=\"@y\" width=\"@width\" height=\"@height\" forecolor=\"#000000\" backcolor=\"#FFFFFF\" key=\"subreport\" stretchType=\"NoStretch\" positionType=\"FixRelativeToTop\" isPrintRepeatedValues=\"false\" isRemoveLineWhenBlank=\"true\" isPrintInFirstWholeBand=\"false\" isPrintWhenDetailOverflows=\"false\"/> <subreportParameter  name=\"sql\"> <subreportParameterExpression><![CDATA[@sql]]></subreportParameterExpression></subreportParameter><connectionExpression><![CDATA[$P{REPORT_CONNECTION}]]></connectionExpression><subreportExpression  class=\"java.lang.String\"><![CDATA[@url]]></subreportExpression></subreport>";
	//positionType= Float
	private final static String SUBREPORT_NOT_FIRST="<subreport  isUsingCache=\"true\"><reportElement mode=\"Opaque\" x=\"@x\" y=\"@y\" width=\"@width\" height=\"@height\" forecolor=\"#000000\" backcolor=\"#FFFFFF\" key=\"subreport\" stretchType=\"NoStretch\" positionType=\"Float\" isPrintRepeatedValues=\"false\" isRemoveLineWhenBlank=\"true\" isPrintInFirstWholeBand=\"false\" isPrintWhenDetailOverflows=\"false\"/> <subreportParameter  name=\"sql\"> <subreportParameterExpression><![CDATA[@sql]]></subreportParameterExpression></subreportParameter><connectionExpression><![CDATA[$P{REPORT_CONNECTION}]]></connectionExpression><subreportExpression  class=\"java.lang.String\"><![CDATA[@url]]></subreportExpression></subreport>";
	private final static String SUBREPORT_SQL_PARAM="<parameter name=\"@param\" isForPrompting=\"true\" class=\"java.lang.String\"><defaultValueExpression ><![CDATA[\"@sql\"]]></defaultValueExpression></parameter>";
}
