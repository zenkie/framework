package nds.jcrosstab;

/** Helper class to take a jCrosstabResultSet object and format
	the data into a cleaner display.
*/

import java.io.*;
import java.util.Properties;

import nds.jcrosstab.fun.FunUtil;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


import jxl.*;
import jxl.write.*;
import jxl.biff.*;
import jxl.format.Border;
import jxl.format.BorderLineStyle;
import jxl.format.Colour;
import jxl.format.Font;
import jxl.format.UnderlineStyle;


public class DisplayConverter
{
	private static final Log logger = LogFactory.getLog(DisplayConverter.class);
	public static final String NBSP="&nbsp;";
/*	public String getTabDelimitedTable (jCrosstabResultSet jxrs)
	{
		StringBuffer str = new StringBuffer("\n");
		String[][] hor = jxrs.getHorizontalGrid();
		for (int i = 0; i<hor.length; i++)
		{
			for (int x = 0; x < jxrs.vertical_axis.slices.size(); x++)
				str.append("\t");

			for (int j = 0; j<hor[i].length; j++)
			{
				if (hor[i][j].length() > 7)
					str.append(hor[i][j].substring(0,7) + "\t");
				else
					str.append(hor[i][j] + "\t");
			}
			str.append("\n");
		}

		String[][] vert = jxrs.getVerticalGrid();
		for (int i = 0; i<vert.length; i++)
		{
			for (int j = 0; j<vert[i].length; j++)
			{
				str.append(vert[i][j] + "\t");
			}

			for (int l = 0; l<jxrs.data_rows[i].length; l++)
			{
				str.append(jxrs.data_rows[i][l] + "\t");
			}
			str.append("\n");
		}

		str.append("\n");

		return str.toString();
	}*/

	public String getHtmlTable (jCrosstabResultSet jxrs)
	{
		if( jxrs.getRowsFetched()==0) 
			return "";
		StringBuffer str = new StringBuffer("<table class=\"jcrosstab-data-table\">");

		//This is the "spacer" above the vertical axis columns.
		int factCount= jxrs.getDataColumnCount();
		String vNext, vCurrent; 
		//Headers
		String[][] hor = jxrs.getHorizontalGrid();
		for (int i = 0; i<hor.length; i++)
		{
			str.append("<tr class=\"horizontal-axis\">");

			if (i == 0)
			{ 
				if( factCount>1){	
					str.append("<td colspan=" + jxrs.vertical_axis.slices.size() + 
							" rowspan=" + (jxrs.horizontal_axis.slices.size()) + " class=\"vertical-spacer\"></td>");
				}else{
					if(jxrs.horizontal_axis.slices.size()>1){
						str.append("<td colspan=" + jxrs.vertical_axis.slices.size() + 
								" rowspan=" + (jxrs.horizontal_axis.slices.size()-1) + " class=\"vertical-spacer\"></td>");
					}else{
						/** add column description row*/
						for(int j=0;j< jxrs.vertical_axis.slices.size();j++ )
							str.append("<td class='vertical-axis-desc'>")
								.append(((SliceDefinition)jxrs.vertical_axis.slices.get(j)).getDescription())
								.append("</td>");
					}
				}
			}else if(factCount==1 && i==hor.length-1){
				// not 0 and at the last row, so jxrs.vertical_axis.slices.size()>1
				/** add column description row*/
				for(int j=0;j< jxrs.vertical_axis.slices.size();j++ )
					str.append("<td class='vertical-axis-desc'>")
						.append(((SliceDefinition)jxrs.vertical_axis.slices.get(j)).getDescription())
						.append("</td>");
			}
			MemberFormatter mf= jxrs.getHorizontalFormatter(i);
			
			for (int j = 0; j<hor[i].length; j++)
			{
				str.append("<td class=\"axis horizontal-axis\" ");
				//Look ahead.
				int colspan = factCount;
				while ((j<hor[i].length-1) &&( ( hor[i][j] == hor[i][j+1]) || (hor[i][j]!=null && hor[i][j].equals(hor[i][j+1]))))
				{
					//When previous row item changed, do not combine the lower line data
					if(i >0){
						vCurrent=hor[i-1][j];
						vNext=hor[i-1][j+1];
						if(vNext!=vCurrent ||  ( vNext!=null && !vNext.equals(vCurrent)) ) break;
					}
					j++;
					colspan+=factCount;
				}

				if (colspan > 1)
					str.append(" colspan=" + colspan);

				str.append(" nowrap>");

				str.append((mf==null?(hor[i][j]==null?NBSP:hor[i][j]):mf.format(hor[i][j])));

				str.append("</td>\n");
			}
			str.append("</tr>");
		}
		String[][] vert = jxrs.getVerticalGrid();
		/** add column description row, infollowing condtion:
		 *  factCount>1 
		 */
		if(factCount>1){
			str.append("<tr class=\"column-desc\">");
			for(int i=0;i< jxrs.vertical_axis.getSliceCount();i++ )
				str.append("<td class='vertical-axis-desc'>")
					.append(((SliceDefinition)jxrs.vertical_axis.slices.get(i)).getDescription())
					.append("</td>");
			for(int i=0;i< jxrs.horizontal_axis.getColumnCount();i++){
				for(int j=0;j< factCount;j++)
					str.append("<td class='fact-column-desc'>").append(
							jxrs.getDataColumnDescription(j)).append("</td>");
			}
			str.append("</tr>");
		}
		/** end column description row*/
		
		int[] skip_cells = new int[] {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1}; // at most 20 slices

		boolean odd_row = true;
		String odd_even_row = "data-odd-row";

		for (int i = 0; i<vert.length; i++)
		{
			if(skip_cells.length<vert[i].length ) throw new IndexOutOfBoundsException("Internal Error:Maximum slices count for vertical axis is "+ skip_cells.length+", while current axis has "+vert[i].length+" slices");
			if (odd_row)
				odd_even_row = "odd-row";
			else
				odd_even_row = "even-row";

			str.append("<tr class=\"" + odd_even_row + "\">");
			for (int j = 0; j<vert[i].length; j++)
			{
				if (skip_cells[j] > 1)
				{
					skip_cells[j]--;
				}
				else
				{
					str.append("<td class=\"axis vertical-axis\" nowrap ");
		
					int x = i;
					MemberFormatter mf= jxrs.getVerticalFormatter(j);

					while ((x<vert.length-1) && (vert[x][j]==vert[x+1][j] || (vert[x][j]!=null && vert[x][j].equals(vert[x+1][j]))))
					{
						if(j>0){
							vCurrent=vert[x][j-1]; 
							vNext=vert[x+1][j-1];
							if(vNext!=vCurrent ||  ( vNext!=null && !vNext.equals(vCurrent)) ) break;
						}
						x++;
						skip_cells[j]++;
					}

					str.append(" rowspan=" + skip_cells[j] + " ");
					str.append(">" + (mf==null?(vert[i][j]==null?NBSP:vert[i][j]):mf.format(vert[i][j]))).append("</td>");
				}
				
			}
			
			boolean odd_column = true;
			String odd_even_column = "odd-column";

			java.lang.Number v;
			Object d;
			for (int l = 0; l<jxrs.data[i].length; l++)
			{
				for(int f=0;f<factCount;f++){
					if (odd_column)
						odd_even_column = "odd-column";
					else
						odd_even_column = "even-column";
	
					v=((java.lang.Number)jxrs.data[i][l][f]);
					if( FunUtil.nullValue.equals(v)){
						d=NBSP;
					}else
						d= jxrs.getValueFormatter(f).format( v);
					str.append("<td class=\"data-grid " + odd_even_row + " " + odd_even_column + "\">" + d + "</td>\n");
					odd_column = !odd_column;
				}
				
			}
			
			odd_row = !odd_row;
			str.append("</tr>");
		}
		return str.append("</table>\n").toString();
	}
	
	private void addReportProperties(WritableSheet s, Properties props) throws WriteException{
		s.insertRow(0);//subject
		s.insertRow(0);
		s.insertRow(0);
		s.insertRow(0);
		s.insertRow(0);
		//c.setAlignment(jxl.format.Alignment.LEFT);
		//c.setVerticalAlignment(jxl.format.VerticalAlignment.CENTRE);
		WritableFont nf = new WritableFont(WritableFont.ARIAL, 10,WritableFont.NO_BOLD,false,UnderlineStyle.NO_UNDERLINE, Colour.DARK_BLUE);
		WritableCellFormat c = new WritableCellFormat (nf); 
		//c.setFont(new FontRecord());
		s.addCell(new Label(0,1, props.getProperty("creator"), c));
		s.addCell(new Label(0,2, props.getProperty("creationdate"), c));
		s.addCell(new Label(0,3, props.getProperty("description"), c));
//		s.addCell(new Label(0,4, props.getProperty("axis_h"), c));
//		s.addCell(new Label(0,5, props.getProperty("axis_v"), c));
//		s.addCell(new Label(0,6, props.getProperty("facts"), c));
		WritableFont arial18font = new WritableFont(WritableFont.ARIAL, 14,WritableFont.BOLD,false,UnderlineStyle.NO_UNDERLINE, Colour.DARK_RED);
		c = new WritableCellFormat (arial18font); 
		s.addCell(new Label(0,0, props.getProperty("subject"), c));
		
	}
	/** This method writes an Excel workbook, using JExcelApi from sf.net.
	 * 
	*/
	public void writeWorkbook (OutputStream os, jCrosstabResultSet jxrs, Properties props)
	{
		String[][] hor = jxrs.getHorizontalGrid();
		String[][] vert = jxrs.getVerticalGrid();
		int factCount= jxrs.getDataColumnCount();
		try
		{
			WritableWorkbook w = Workbook.createWorkbook(os);
			WritableSheet s = w.createSheet(props.getProperty("facttable","crosstab"), 0);
			if( jxrs.getRowsFetched()==0) {
				// add report desc 
				addReportProperties( s, props);
				w.write();
				w.close();
				return;
			}

			WritableCellFormat h_cell = new WritableCellFormat();
			h_cell.setAlignment(jxl.format.Alignment.CENTRE);
			h_cell.setVerticalAlignment(jxl.format.VerticalAlignment.CENTRE);
			h_cell.setBackground(jxl.format.Colour.GREY_25_PERCENT);
			h_cell.setBorder(Border.ALL, BorderLineStyle.THIN);
			
			int base_col = vert[0].length; // position used by vertial axis 
			for (int i = 0; i<hor.length; i++)
			{
				// i line of horizton
				MemberFormatter mf= jxrs.getHorizontalFormatter(i);
				for (int j = 0; j<hor[i].length; j++)
				{
					s.addCell(new Label(j* factCount +base_col, i, mf==null? hor[i][j]:mf.format(hor[i][j]), h_cell));

					int cell_count_to_merge = 0;
					int j_start = j;
					while ((j<hor[i].length-1) && (hor[i][j] == hor[i][j+1]))
					{
						//s.addCell(new Label(j+base_col, i, hor[i][j], h_cell));
						cell_count_to_merge++;
						j++;
					}

					if (cell_count_to_merge > 0 || factCount>1)
					{
						s.mergeCells(j_start* factCount+base_col, i, j_start* factCount+factCount-1+base_col+cell_count_to_merge*factCount, i);
					}
				}
			}
			/*
			 * Column descriptions
			 */
			int base_row ;
			if( factCount>1) base_row= hor.length; // row below h axis
			else base_row= hor.length-1; // last row of h axis
			WritableCellFormat column_desc_cell = new WritableCellFormat();
			column_desc_cell.setAlignment(jxl.format.Alignment.CENTRE);
			column_desc_cell.setVerticalAlignment(jxl.format.VerticalAlignment.CENTRE);
			column_desc_cell.setBackground(jxl.format.Colour.AQUA);
			column_desc_cell.setBorder(Border.ALL, BorderLineStyle.THIN);
			for(int i=0;i< jxrs.vertical_axis.getSliceCount();i++ ){
				s.addCell(new Label(i, base_row, ((SliceDefinition)jxrs.vertical_axis.slices.get(i)).getDescription(), column_desc_cell));
			}
			if(factCount>1){// there's no need to set fact column desc if only one fact column exists
				int colIndex=jxrs.vertical_axis.getSliceCount();
				for(int i=0;i< jxrs.horizontal_axis.getColumnCount();i++){
					for(int j=0;j< factCount;j++){
						s.addCell(new Label(colIndex, base_row, jxrs.getDataColumnDescription(j), column_desc_cell));
						colIndex++;
					};
				}			
			}
			base_row++;
			for (int i = 0; i<vert.length; i++)
			{
				for (int j = 0; j<vert[i].length; j++)
				{
					MemberFormatter mf= jxrs.getVerticalFormatter(j);
					s.addCell(new Label(j,i+base_row,mf==null? vert[i][j]:mf.format(vert[i][j]), h_cell));
				}
			}

			//Re-read the vertical axis and merge cells.
			for (int j=0; j<vert[0].length; j++)
			{
				for (int i=0; i<vert.length; i++)
				{
					int i_start = i;
					int cell_count_to_merge = 0;

					while ((i<vert.length-1) && (vert[i][j] == vert[i+1][j]))
					{
						cell_count_to_merge++;
						i++;
					}

					if (cell_count_to_merge > 0)
					{
						s.mergeCells(j, i_start+base_row, j, i_start+base_row+cell_count_to_merge);
					}
				}
			}
			WritableCellFormat[] factCellFormat=new WritableCellFormat[factCount];
			//format of fact column cells
			for( int i=0;i< factCount;i++){
				NumberFormat f=new  NumberFormat(jxrs.getValueFormatter(i).toPattern());
				factCellFormat[i]=new WritableCellFormat(f);
				factCellFormat[i].setBorder(Border.ALL, BorderLineStyle.THIN);
			}
			
			java.lang.Number v;
			for (int i = 0; i<vert.length; i++)
			{
				for (int l = 0; l<jxrs.data[i].length; l++)
				{
					for(int f=0;f< factCount;f++){
						v=((java.lang.Number)jxrs.data[i][l][f]);
						if( FunUtil.nullValue.equals(v)){
							s.addCell(new Blank( l*factCount +f +base_col ,i+base_row, factCellFormat[f]));
						}else{
							
							s.addCell(new jxl.write.Number(l*factCount +f +base_col,i+base_row, v.doubleValue(), factCellFormat[f]) ); // column, row
						}
					}
				}
			}
 
			WritableCellFormat grid_cell_name = new WritableCellFormat();
			grid_cell_name.setAlignment(jxl.format.Alignment.CENTRE);
			grid_cell_name.setVerticalAlignment(jxl.format.VerticalAlignment.CENTRE);
			grid_cell_name.setBorder(Border.ALL, BorderLineStyle.THIN);

			if(factCount>1){
				s.mergeCells(0,0,vert[0].length-1, hor.length-1);
			}else if(hor.length>2){
				s.mergeCells(0,0,vert[0].length-1, hor.length-2);
			}
			// add report desc 
			addReportProperties( s, props);
			w.write();
			w.close();
		}
		catch (IOException ioe)
		{
			System.out.println("DisplayConverter.java, 250: " + "IOException caught " + ioe.getMessage());
		}
		catch (jxl.write.WriteException we)
		{
			System.out.println("DisplayConverter.java, 254: " + "jxl.write.WriteException caught " + we.getMessage());
		}
	}
}
