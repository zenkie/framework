package nds.report;

import java.io.File;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import nds.control.web.SessionContextManager;
import nds.control.web.UserWebImpl;
import nds.control.web.WebUtils;
import nds.log.Logger;
import nds.log.LoggerManager;
import nds.query.QueryEngine;
import nds.query.QueryRequest;
import nds.query.QueryRequestImpl;
import nds.query.QueryResult;
import nds.query.web.SumFieldInterpreter;
import nds.query.web.TableQueryModel;
import nds.schema.Column;
import nds.schema.Table;
import nds.schema.TableManager;
import nds.util.Configurations;
import nds.util.WebKeys;

/**
 * Title:        NDS Project
 * Description:  San gao shui yuan, mu xiang ren jia
 * Copyright:    Copyright (c) 2001
 * Company:      aic
 * @author yfzhu
 * @version 1.0
 */

public class ReportUtils {
  private static  Logger logger=LoggerManager.getInstance().getLogger(ReportUtils.class.getName());
  
  String userName;
  UserWebImpl user = null;
  Configurations conf = null;
  Locale locale=null;
  public ReportUtils(HttpServletRequest request) {
      Locale locale = (Locale)request.getSession(true).getAttribute(org.apache.struts.Globals.LOCALE_KEY);
      if(locale==null)locale= TableManager.getInstance().getDefaultLocale();
      SessionContextManager mgr=WebUtils.getSessionContextManager(request.getSession(true));
      if( mgr !=null)
          user =( (UserWebImpl)mgr.getActor(WebKeys.USER));
      // get configuation from servlet context
      conf=(Configurations)nds.control.web.WebUtils.getServletContextManager().getActor(nds.util.WebKeys.CONFIGURATIONS);
      
  }
  
  public String getUserName(){
      return  user.getUserName();
  }
  
  public UserWebImpl getUser(){
  	return user;
  }
  // get root path used by web tier to find exported files
  public String getExportRootPath(){
      return conf.getProperty("export.root.nds","/aic/home");
  }
  // get root path used by oracle, note oracle and weblogic may not exist on same machine, so this directory is probably different to ndsExportRootPath
  public String getOracleExportRootPath(){
      return conf.getProperty("export.root.oracle","/aic/home");
  }
  public long getQuota(){
      String quota = conf.getProperty("export.quota","5M");
//      logger.debug("QUOTA"+quota);
      if(quota.substring(quota.length() - 1).equalsIgnoreCase("m"))
          return Integer.valueOf(quota.substring(0,quota.length()-1)).longValue()*1024*1024;
      else if(quota.substring(quota.length() - 1).equalsIgnoreCase("k"))
          return Integer.valueOf(quota.substring(0,quota.length()-1)).longValue()*1024;
      else
          return Integer.valueOf(quota.substring(0,quota.length())).longValue();
  }
  public long getSpaceUsed(){
      File dir = new File(getExportRootPath()+File.separator+ user.getClientDomain()+File.separator+  getUserName());
      
      if(!dir.exists() || !dir.isDirectory())
          return 0;
      File[] files = dir.listFiles();
      long spaceUsed = 0;
      for(int i = 0;i < files.length;i++){
          if(files[i].isFile())
              spaceUsed += files[i].length();
      }
      return spaceUsed;
  }
  public static String getHeader(boolean pk,boolean ak,String separator,QueryRequest qRequest){
      String[] showNames = qRequest.getDisplayColumnNames(true);
      int[] showColumns = qRequest.getDisplayColumnIndices();
      if(separator == null || separator.trim().length() ==0)
          separator = ",";
      StringBuffer buff = new StringBuffer();
      for( int i=0;i<showColumns.length;i++) {
          int[] cLink = qRequest.getSelectionColumnLink(showColumns[i]);
          if(!pk && cLink[cLink.length - 1] == qRequest.getMainTable().getPrimaryKey().getId())
              continue;//如果不需要显示主键，就不显示
          if(!ak && cLink[cLink.length - 1] == qRequest.getMainTable().getAlternateKey().getId())
              continue;//如果不需要显示AK，就不显示
          if(i>0)
              buff.append(separator);
          buff.append(showNames[i]);
      }
      return buff.toString();
  }
  public static String getHeaderHtml(boolean pk,boolean ak,QueryRequest qRequest){
      String[] showNames = qRequest.getDisplayColumnNames(true);
      int[] showColumns = qRequest.getDisplayColumnIndices();
      String buff = "";
      buff += "<tr>";
      for( int i=0;i<showColumns.length;i++) {
          int[] cLink = qRequest.getSelectionColumnLink(showColumns[i]);
          if(!pk && cLink[cLink.length - 1] == qRequest.getMainTable().getPrimaryKey().getId())
              continue;//如果不需要显示主键，就不显示
          if(!ak && cLink[cLink.length - 1] == qRequest.getMainTable().getAlternateKey().getId())
              continue;//如果不需要显示AK，就不显示
          buff += "<td>";
          buff += showNames[i];
          buff += "</td>";
      }
      buff += "</tr>";
      return buff;
  }
  /**
   * 为flinks相关的文件报告生成报告头
   * @param query
   * @param tableColumns
   * @param header
   * @param separator
   * @return
   * @throws Exception
   */
 /* public String getFlinkHeader(QueryRequest query,int tableColumns,String header,String separator) throws Exception{
      int count = 0;
      StringBuffer res = new StringBuffer("");
      String title = "",commas = "";
      ArrayList columns = query.getMainTable().getShowableColumns(Column.QUERY_LIST);
      if(query.getMainTable().getAlternateKey() == null)
          throw new Exception("table("+query.getMainTable().getName()+")没有指定AlternateKey");
      Column akColumn = null;
      for(int i=0;i<columns.size();i++){
          Column col = (Column)columns.get(i);
          if(col.getId() != query.getMainTable().getAlternateKey().getId()){
              title += separator + col.getDescription();
              commas += separator;
          }else
              akColumn = col;
      }
      if(akColumn != null)//加入有ak,就添加相应的空格
      for(int i=0;i<tableColumns - 1;i++)
          res.append(separator);
      if(akColumn != null){
          String sql = null;
          if(akColumn.getReferenceTable() != null)
               sql = "select distinct a0."+akColumn.getReferenceTable().getAlternateKey().getName()
                   + " from "+ query.getMainTable().getName() + " a1," + akColumn.getReferenceTable().getName() + " a0"
                   + " where a1." + akColumn.getName() + " = a0.ID"
                   + " order by a0."+ akColumn.getReferenceTable().getAlternateKey().getName();
          else
               sql = "select distinct " + akColumn.getName()
                   + " from "+ query.getMainTable().getName()
                   + " order by "+ akColumn.getName();
          ResultSet rs = QueryEngine.getInstance().doQuery(sql);
          while(rs.next()){
              String value = null;
              if(akColumn.getType() == Column.DATE)
                  value = rs.getDate(1).toString();
              else
                  value = rs.getString(1);
              res.append(separator + value + commas.substring(1));
          }
          rs.last();
          count = rs.getRow();
          rs.close();
      }
      res.append("\n"+header);
      for(int i=0;i<count;i++)
          res.append(title);
      return res.toString();
  }*/
  
  /**
   *
   * @param query
   * @param tableColumns
   * @param header
   * @return
   * @throws Exception
   */
  /*public String getFlinkHeaderHtml(QueryRequest query,int tableColumns,String header) throws Exception{
      int count = 0;
      StringBuffer res = new StringBuffer("<tr>");
      String title = "",commas = "";
      ArrayList columns = query.getMainTable().getShowableColumns(Column.QUERY_LIST);
      if(query.getMainTable().getAlternateKey() == null)
          throw new Exception("table("+query.getMainTable().getName()+")没有指定AlternateKey");
      Column akColumn = null;
      for(int i=0;i<columns.size();i++){
          Column col = (Column)columns.get(i);
          if(col.getId() != query.getMainTable().getAlternateKey().getId()){
              title += "<td>" + col.getDescription() + "</td>";
              commas += "<td>&nbsp;</td>";
          }else
              akColumn = col;
      }
      if(akColumn != null)//加入有ak,就添加相应的空格
      for(int i=0;i<tableColumns;i++)
          res.append("<td>&nbsp;</td>");
      if(akColumn != null){
          String sql = null;
          if(akColumn.getReferenceTable() != null)
               sql = "select distinct a0."+akColumn.getReferenceTable().getAlternateKey().getName()
                   + " from "+ query.getMainTable().getName() + " a1," + akColumn.getReferenceTable().getName() + " a0"
                   + " where a1." + akColumn.getName() + " = a0.ID"
                   + " order by a0."+ akColumn.getReferenceTable().getAlternateKey().getName();
          else
               sql = "select distinct " + akColumn.getName()
                   + " from "+ query.getMainTable().getName()
                   + " order by "+ akColumn.getName();
          ResultSet rs = QueryEngine.getInstance().doQuery(sql);
          while(rs.next()){
              String value = null;
              if(akColumn.getType() == Column.DATE)
                  value = rs.getDate(1).toString();
              else
                  value = rs.getString(1);
              res.append("<td>" + value + commas.substring("<td>&nbsp;</td>".length()) + "</td>");
          }
          rs.last();
          count = rs.getRow();
          rs.close();
          res.append("</tr>");
      }
      res.append("\n"+header.substring(0,header.length() - "</tr>".length()));
      for(int i=0;i<count;i++)
          res.append(title);
      res.append("</tr>");
      return res.toString();
  }*/
  /**
   * 取得title
   * @param sheet
   * @param objectId
   * @param separator
   * @return
   * @throws Exception
   */
  public String getTitle(String sheet,String objectId,String separator) throws Exception{
      TableManager manager= TableManager.getInstance();
      int sheetId = Integer.parseInt(sheet);
      Table sheetTable = manager.getTable(sheetId);

      QueryRequestImpl query=QueryEngine.getInstance().createRequest(null);
      query.setMainTable(sheetId);
      query.addAllShowableColumnsToSelection(Column.QUERY_LIST);
      query.addParam(sheetTable.getPrimaryKey().getId(),objectId );
      QueryResult result= QueryEngine.getInstance().doQuery( query);

      if(result.getRowCount() == 0)
          return "没有选中数据（"+sheet+","+objectId+"）";
      else if(result.getRowCount()==1)
          result.next();
      else
          return result.getRowCount()+"条数据（"+sheet+","+objectId+"）";

      ArrayList columns=sheetTable.getShowableColumns(Column.QUERY_LIST);
      TableQueryModel model= new TableQueryModel(sheetId,Column.QUERY_LIST,false, locale);

      String title = "",data = "";
      for(int i=0;i < columns.size();i++){
          Column column=(Column)columns.get(i);

          if(!title.equals("")){
              title += separator;
              data += separator;
          }
          title += model.getDescriptionForColumn(column);
          data += result.getString(i+1,false);
      }
      for ( Iterator it=sheetTable.getSumFields();it.hasNext();){
            Column col=(Column) it.next();
            String interpreter=col.getValueInterpeter();
            if(!title.equals("")){
                title += separator;
                data += separator;
            }
            title += col.getDescription(TableManager.getInstance().getDefaultLocale());
            int type= col.getSQLType();
            SumFieldInterpreter sfi=new SumFieldInterpreter(sheetTable.getName()+"Id", type, interpreter);
            data += sfi.parseValue(objectId+"");
      }

      return title + "\n" + data + "\n";
  }
  /**
   *
   * @param sheet
   * @param objectId
   * @return
   * @throws Exception
   */
  public String getTitleHtml(String sheet,String objectId) throws Exception{
      TableManager manager= TableManager.getInstance();
      int sheetId = Integer.parseInt(sheet);
      Table sheetTable = manager.getTable(sheetId);

      QueryRequestImpl query=QueryEngine.getInstance().createRequest(null);
      query.setMainTable(sheetId);
      query.addAllShowableColumnsToSelection(Column.QUERY_LIST);
      query.addParam(sheetTable.getPrimaryKey().getId(),objectId );
      QueryResult result= QueryEngine.getInstance().doQuery( query);

      if(result.getRowCount() == 0)
          return "没有选中数据（"+sheet+","+objectId+"）";
      else if(result.getRowCount()==1)
          result.next();
      else
          return result.getRowCount()+"条数据（"+sheet+","+objectId+"）";

      ArrayList columns=sheetTable.getShowableColumns(Column.QUERY_LIST);
      TableQueryModel model= new TableQueryModel(sheetId,Column.QUERY_LIST,false,locale);

      String title = "<tr>",data = "<tr>";
      for(int i=0;i < columns.size();i++){
          Column column=(Column)columns.get(i);

          title += "<td>"+model.getDescriptionForColumn(column)+"</td>";
          data += "<td>"+result.getString(i+1,false)+"</td>";
      }
      for ( Iterator it=sheetTable.getSumFields();it.hasNext();){
            Column col=(Column) it.next();
            String interpreter=col.getValueInterpeter();

            title += "<td>"+col.getDescription(TableManager.getInstance().getDefaultLocale())+"</td>";
            int type= col.getSQLType();
            SumFieldInterpreter sfi=new SumFieldInterpreter(sheetTable.getName()+"Id", type, interpreter);
            data += "<td>"+sfi.parseValue(objectId+"")+"</td>";
      }
      title += "</tr>";
      data += "</tr>";

      return title + "\n" + data + "\n";
  }
}