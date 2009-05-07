/*
 * Agile Control Technologies Ltd,. CO.
 * http://www.agileControl.com
 */
package nds.cxtab;

import java.io.*;
import java.text.DecimalFormat;
import java.util.*;

import nds.log.Logger;
import nds.log.LoggerManager;
import nds.util.Configurations;
import nds.util.Tools;

import nds.jcrosstab.DisplayConverter;
import nds.jcrosstab.jCrosstabResultSet;

public class HtmlFactory
{
    private static Logger logger= LoggerManager.getInstance().getLogger((HtmlFactory.class.getName()));

 public HtmlFactory()
 {
 }

 private String getReportHTMLProperties(Properties props){
 	StringBuffer sb=new StringBuffer();
 	sb.append("<table border=0 width='100%' class='report-header'><tr><td colspan='2' class='report-subject'>")
		.append(props.getProperty("subject")).append("</td></tr><tr><td class='report-creator'>")
		.append(props.getProperty("creator")).append("</td><td class='report-date'>")
		.append(props.getProperty("creationdate")).append("</td></tr><tr><td colspan='2' class='report-desc'>")
		.append(props.getProperty("description")).append("</td></tr>")
		/*.append("<tr><td class='axis-h-desc'>")
		.append(props.getProperty("axis_h")).append("</td></tr><tr><td class='axis-v-desc'>")
		.append(props.getProperty("axis_v")).append("</td></tr><tr><td class='fact-desc'>")
		.append(props.getProperty("facts"))
		.append("</td></tr>")*/
		.append("</table>");
 	return sb.toString();
 }
 public void writeHtmlBody(jCrosstabResultSet jxrs,Properties props,Writer w) throws Exception{
 	w.write(getReportHTMLProperties(props));
    DisplayConverter dc = new DisplayConverter();
    String table= dc.getHtmlTable(jxrs);
    if(nds.util.Validator.isNull(table)){
    	String nodata= props.getProperty("no-data");
    	if(nodata ==null) nodata= nds.util.MessagesHolder.getInstance().getMessage(
    			nds.schema.TableManager.getInstance().getDefaultLocale(),"no-data");
    	w.write("<div class='no-data'>"+nodata+"</div>");
    }else{
    	w.write( table);
    }
 	
 	if( Boolean.TRUE.equals(props.get("isdebug"))){
 		String sql= props.getProperty("sql");
 		long rowsfetched=((Long) props.get("rowsfetched")).longValue();
 		long startTime=((Long) props.get("startfrom")).longValue();
 		
 		float duration=(int)((System.currentTimeMillis()-startTime)/1000.0);
 		DecimalFormat df=new DecimalFormat("#0.000"); 
 		w.write("<p><div class='sql'>SQL:"+ sql+"</div><br>");
 		w.write("<p><div class='rowsfetched'>Rows fetched:"+ rowsfetched+"</div><br>");
 		w.write("<div class='duration'>Time:"+  df.format(duration) +" seconds </div>");
 	}
 }
 public void writeHtmlFile(String fileName, jCrosstabResultSet jxrs,Properties props) throws Exception{
 	logger.debug("write "+fileName);
 	FileOutputStream fos= new FileOutputStream(fileName);
 	OutputStreamWriter w= new OutputStreamWriter(new BufferedOutputStream(fos), "UTF-8");
 	String reportWebRoot =((Configurations)nds.control.web.WebUtils.getServletContextManager().getActor(nds.util.WebKeys.CONFIGURATIONS)).getProperty("server.url", "http://localhost");
 	w.write("<html xmlns=\"http://www.w3.org/1999/xhtml\"><head>");
 	w.write("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" /><meta name=\"generator\" content=\"Agile NEA 4\" />");
 	w.write("<link rel=\"stylesheet\" type=\"text/css\" href=\""+ reportWebRoot+"/html/nds/css/cxtab.css\" />");
 	w.write("<title>");
 	w.write(props.getProperty("title",""));
 	w.write("</title></head><body>");
 	writeHtmlBody(jxrs, props, w);
 	w.write("</body></html>");
 	w.flush();
 	w.close();
 }
 /*
 public String getHtmlTable(jCrosstabResultSet jxrs)
 {
    //TODO check data[][][], previouse data_grid[][] 
 	StringBuffer str = new StringBuffer("<!-- Starting table for jcrosstab data from HtmlFactory -->\n");
     str.append("<table class=\"jcrosstab-data-table\">");
     String hor[][] = jxrs.getHorizontalGrid();
     for(int i = 0; i < hor.length; i++)
     {
         str.append("<tr class=\"horizontal-axis\">");
         if(i == 0)
             str.append((new StringBuffer()).append("<td colspan=").append(jxrs.getVerticalAxisSliceCount()).append(" rowspan=").append(jxrs.getHorizontalAxisSliceCount()).append(" class=\"vertical-spacer\">").append("&nbsp;").append("\n").toString());
         for(int j = 0; j < hor[i].length; j++)
         {
             str.append("<td class=\"axis horizontal-axis\" ");
             if(j < hor[i].length - 1)
             {
                 int colspan;
                 for(colspan = 1; j < hor[i].length - 1 && hor[i][j] == hor[i][j + 1]; colspan++)
                     j++;

                 if(colspan > 1)
                     str.append((new StringBuffer()).append(" colspan=").append(colspan).toString());
             }
             str.append(" nowrap>");
             if(i == hor.length - 1)
             {
                 str.append("&nbsp;");
                 str.append("<select style=\"width:5px;height:5px;background-color:#a0bcde;position:relative;top:5px;\" ONCHANGE=\"location = this.options[this.selectedIndex].value;\">\n");
                 str.append("<option SELECTED>This Column...</option>\n");
                 str.append("<option>&nbsp;&nbsp;Sort...</option>\n");
                 str.append((new StringBuffer()).append("<option value=index.html?action=sort_on_column&sort_type=ascending&col_idx=").append(j).append(">").append("&nbsp;&nbsp;&nbsp;&nbsp;&bull;Ascending</option>\n").toString());
                 str.append((new StringBuffer()).append("<option value=index.html?action=sort_on_column&sort_type=descending&col_idx=").append(j).append(">").append("&nbsp;&nbsp;&nbsp;&nbsp;&bull;Descending</option>\n").toString());
                 str.append("</select>\n");
             }
             str.append(hor[i][j]);
             str.append("</td>\n");
         }

     }

     String vert[][] = jxrs.getVerticalGrid();
     int skip_cells[] = {
         1, 1, 1, 1, 1, 1, 1, 1, 1, 1
     };
     boolean odd_row = true;
     String odd_even_row = "data-odd-row";
     for(int i = 0; i < vert.length; i++)
     {
         if(odd_row)
             odd_even_row = "odd-row";
         else
             odd_even_row = "even-row";
         str.append((new StringBuffer()).append("<tr class=\"").append(odd_even_row).append("\">").toString());
         for(int j = 0; j < vert[i].length; j++)
         {
             if(skip_cells[j] > 1)
             {
                 skip_cells[j]--;
                 continue;
             }
             str.append("\t<td class=\"axis vertical-axis\" nowrap ");
             if(j < vert[i].length - 1)
             {
                 for(int x = i; x < vert.length - 1 && vert[x][j].equals(vert[x + 1][j]);)
                 {
                     x++;
                     skip_cells[j]++;
                 }

                 str.append((new StringBuffer()).append(" rowspan=").append(skip_cells[j]).append(" ").toString());
             }
             str.append(">");
             if(j == vert[i].length - 1)
             {
                 str.append("&nbsp;");
                 str.append("<select style=\"width:5px;height:5px;background-color:#a0bcde;position:relative;top:5px;\" ONCHANGE=\"location = this.options[this.selectedIndex].value;\">\n");
                 str.append("<option selected>This Row...</option>\n");
                 str.append("<option>&nbsp;&nbsp;Sort...</option>\n");
                 str.append((new StringBuffer()).append("<option value=index.html?action=sort_on_row&sort_type=ascending&row_idx=").append(i).append(">").append("&nbsp;&nbsp;&nbsp;&nbsp;&bull;Ascending L-to-R</option>\n").toString());
                 str.append((new StringBuffer()).append("<option value=index.html?action=sort_on_row&sort_type=descending&row_idx=").append(i).append(">").append("&nbsp;&nbsp;&nbsp;&nbsp;&bull;Descending L-to-R</option>\n").toString());
                 str.append("</select>\n");
             }
             str.append(vert[i][j]);
             str.append("</td>\n");
         }

         boolean odd_column = true;
         String odd_even_column = "odd-column";
         int data_rows[][] = jxrs.getDataGrid();
         for(int l = 0; l < data_rows[i].length; l++)
         {
             if(odd_column)
                 odd_even_column = "odd-column";
             else
                 odd_even_column = "even-column";
             str.append((new StringBuffer()).append("\t<td class=\"data-grid ").append(odd_even_row).append(" ").append(odd_even_column).append("\">").append(data_rows[i][l]).append("</td>\n").toString());
             odd_column = !odd_column;
         }

         str.append("</tr>\n");
         odd_row = !odd_row;
     }

     str.append("</table>\n");
     str.append("<!-- Close of data table from HtmlFactory -->\n");
     System.out.println("HtmlFactory.java, 179: Exiting getHtmlTable");
     return str.toString();
 }*/
}
