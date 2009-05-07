package nds.control.ejb.command;
import java.io.File;
import java.io.FileWriter;
import java.math.BigDecimal;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.*;

import nds.control.ejb.Command;
import nds.control.ejb.DefaultWebEventHelper;
import nds.control.ejb.MySQLObjectCreateImpl;
import nds.control.ejb.ObjectCreateImpl;
import nds.control.event.DefaultWebEvent;
import nds.control.event.NDSEventException;
import nds.control.util.ValueHolder;
import nds.mail.NotificationManager;
import nds.query.*;
import nds.schema.Column;
import nds.schema.RefByTable;
import nds.schema.Table;
import nds.schema.TableManager;
import nds.util.NDSException;
import nds.util.PairTable;
import nds.util.Tools;
import nds.util.Validator;

/**
 * Do object creation
 */
public class ObjectCreate extends Command{
	/**
     * Whether this command use internal transaction control. For normal command, transaction is controled by
     * caller, yet for some special ones, the command will control transaction seperativly, that is, the command
     * will new transaction and commit that one explicitly
     * @return false if use transaction from caller
     */
    public boolean internalTransaction(){
    	return true;
    }	
  /**
   * @return  if jsonobj found in event, and json lines splitted, will singal "jsonObjectCreated" 
   * attribute to Boolean.TRUE in returned ValueHolder
   */	
  public ValueHolder execute(DefaultWebEvent event) throws NDSException ,RemoteException{
  	  boolean jsonObjectCreated=false;	
  	//logger.debug(event.toDetailString());
      TableManager manager = helper.getTableManager();
      int tableId = Tools.getInt(event.getParameterValue("table"),-1 ) ;
      int userId= helper.getOperator(event).id.intValue();
      Table table = manager.getTable(tableId) ;
      String tableName = table.getName();          // 得到表的名字
      String tableDesc = table.getDescription(Locale.CHINA) ;
      
      
      String outputFile= (String) event.getParameterValue("outputfile");
      long beginTime=System.currentTimeMillis();
      try{
          // if begin time is set from event, then use it, this is useful to calculate
          // long time event preparation such as excel import
          String sbegin=(String)event.getParameterValue("begintime");
          if ( sbegin !=null && sbegin.length() > 0)
              beginTime= (new Long(sbegin)).longValue() ;
      }catch(Exception eb){logger.error("error parsing event parameter 'begintime'"+eb);}
      boolean bgRun="true".equalsIgnoreCase(""+event.getParameterValue("bgrun"));
      int startRow=Tools.getInt(event.getParameterValue("startRow"), 1);
      if(bgRun ){
          // run object create in bkground
          event.setParameter("bgrun", "false"); // to avoid bgrun next time
          if( outputFile ==null || (outputFile.equals("") ))
              throw new NDSEventException("Internal Error, must set file name(outputfile)");
          try{
              File f= new File((String)outputFile);
              return helper.handleEventBackground(event, f.getName());
          }catch(Exception e){
              throw new NDSEventException("@exception@",e);
          }
      }
      boolean bestEffort= false;
      try{
          bestEffort= (new Boolean((String) event.getParameterValue("best_effort"))).booleanValue() ;
      }catch(Exception e){}

      java.sql.Connection con=null;
      PreparedStatement stmt=null;
      //Table sheetTable=null;int sheetId=-1;
       try{
       	   con= helper.getConnection(event);
       	   if(bestEffort)con.setAutoCommit(false);

           ArrayList colArray = table.getAllColumns();  // 得到表的所有列名

           /*if( tableName.toLowerCase().lastIndexOf("item")== tableName.length()-4){
              sheetTable= manager.getTable(tableName.substring(0,tableName.length()-4 ));
              sheetId = Tools.getInt(event.getParameterValue("objectid"),-1 ) ;
           }*/

           int recordLen = getRecordLength(table,event);
           
           ColumnValueImpl colValueImpl = new ColumnValueImpl();
           colValueImpl.setActionType("create");
           // 得到所有列的值
           colValueImpl.enableBestEffort(bestEffort);
           HashMap hashMap = colValueImpl.getColumnHashMap(event,table,colArray,recordLen,con);
           //logger.debug("record length:"+ recordLen+", hashmap:"+ Tools.toString(hashMap));

           //key: row number (start from 0, integer) values ( string, msg for reason why that row invalid)
           //may be null, only valid in best_effort=true
           HashMap invalidRows= colValueImpl.getInvalidRows();
           
           Vector vecObj =(Vector) hashMap.get(table.getPrimaryKey().getName());
           logger.debug(table.getPrimaryKey()+ " vecObj="+vecObj.size() + " class=" + vecObj.elementAt(0).getClass()+ ", value="+ vecObj.elementAt(0));
           BigDecimal[] objectId = (BigDecimal[])vecObj.elementAt(0);
           if( objectId !=null ){
               //logger.debug("objectId.size="+objectId.length );
               //if ( objectId.length > 0)  logger.debug("objectID[0]="+objectId[0]) ;
           }
           int[] oids= new int[objectId.length];
           for(int i=0;i<oids.length;i++) oids[i]= objectId[i].intValue();
           Table parent= helper.getParentTable( table,event);
           
           ObjectCreateImpl createImpl = new ObjectCreateImpl(hashMap, event, table, recordLen);
           createImpl.setInvalidRows(invalidRows) ;
           //Vector sqlVector = createImpl.getSqlArray(hashMap,event,table,recordLen);
           ArrayList sqlData=  createImpl.getSQLData();
           int[] sqlDataColumnTypes= createImpl.getColumnTypes();
           ArrayList sqlIndex= createImpl.getSQLIndex() ; // index of sql in original sheet
           
           int realCount=sqlData.size();
           
           // the original records that current new data copied from. 2006-06-11
           String[] copyFromIds= event.getParameterValues("copyfromid");
           // is the table has refby table that should be bundled when create?
           ArrayList bundledTable =new ArrayList();
           for(Iterator it=table.getRefByTables().iterator();it.hasNext();){
           		RefByTable rbt= (RefByTable) it.next();
           		if(rbt.isBundledWhenCopy()) bundledTable.add(rbt);
           }
           boolean checkItems=  (copyFromIds!=null) && (bundledTable.size()>0);
           logger.debug("checkItems="+checkItems);
           int cfid;
           
           boolean createAttributeDetail=table.supportAttributeDetail();
           boolean createAttributeDetailByJson=false;
           boolean createAttributeDetailByCopy=false;
           Object jsonobj= event.getParameterValue("jsonobj");
           Object[] jo=null;
           BigDecimal[] pdtIds=null;
           if(createAttributeDetail){
	           if(jsonobj!=null){
               	  	if(jsonobj.getClass().isArray()){
               	  		jo= ((Object[])jsonobj);
               	  	}else jo= new Object[]{jsonobj};
		            Vector pdts =(Vector) hashMap.get("M_PRODUCT_ID");
		            if(pdts!=null)pdtIds = (BigDecimal[])pdts.elementAt(0);
		            createAttributeDetailByJson = pdts!=null;
	           }else{
	           		createAttributeDetailByCopy=(copyFromIds!=null);
	           }
           }
           logger.debug("createAttributeDetailByJson="+ createAttributeDetailByJson+
           			",createAttributeDetailByCopy="+createAttributeDetailByCopy);
           int realPos;
           
           String psql=createImpl.getPreparedStatementSQL();
           logger.debug(psql);
           stmt=con.prepareStatement(psql);
           QueryEngine engine = QueryEngine.getInstance() ;
           //String sql;
           ArrayList row;
           java.sql.Savepoint  sp=null;
           for( int i=0;i< sqlData.size();i++){
           	   realPos=((Integer)sqlIndex.get(i)).intValue();
           	   //Managed transaction in 
           	   if(bestEffort)sp= con.setSavepoint();
           	   
               try{
                   row= (ArrayList) sqlData.get(i);
                   //if(i< 10)logger.debug(sql); // only first 10 records will be displayed
                   
                   executeUpdate(stmt,row,sqlDataColumnTypes );
                   
                   // check if attribute detail support table, since 3.0 at 2007-05-30
                   if(createAttributeDetailByJson){
                   		//jsonobj will be stored into m_attributedetail table
                   		if(pdtIds[realPos]==null){
                   			logger.debug(" not found product id");
                   		}else{
                   			helper.createAttributeDetailRecordsByJSON(table, jo[realPos], event, oids[i], con);
                   			jsonObjectCreated=true;//"jsonobj-splitted"
                   		}
                   }else if(createAttributeDetailByCopy){
                   		// it's not possible that we do both createAttributeDetailByJson and createAttributeDetailByCopy
                   		/**
                   		 * copy from table: table, id: cfid= Tools.getInt(copyFromIds[i], -1)
                   		 * copy to   table: table, id: oids[i]
                   		 */
                   		cfid= Tools.getInt(copyFromIds[realPos], -1);
                   		if(cfid!=-1)
                   			helper.createAttributeDetailRecordsByCopy(table, userId, cfid,oids[realPos],con);
                   }
                   helper.doTrigger("AC", table, oids[realPos], con);
                   
                   // check refby table records should be copied.
                   // there will be a special param in event: copyfromid, if exists, that will be the
                   // record that current record copied from, and all the refby table records of original
                   // one should also dupicated to the new one.
                   
                   if(checkItems){
                   		cfid= Tools.getInt(copyFromIds[realPos], -1);
                   		if(cfid!=-1)
                   			copyRefbyTableRecords( table,cfid, oids[realPos],bundledTable,con , event);
                   }
                   //con.releaseSavepoint(sp); oracle not support this method currently
               }catch(Throwable e){
               	   logger.error("Failed ", e);
                   if( ! bestEffort ) throw new NDSEventException("@line@ "+ (1+realPos) +": "+ helper.getRootCauseMessage(e, event.getLocale()));
                   // should rollback insert sql here
                   if(bestEffort)con.rollback(sp);
                   if(invalidRows !=null) {
                       invalidRows.put(sqlIndex.get(i),helper.getRootCauseMessage(e, event.getLocale()) );
                   }
               }
           }
           // commit all 
           try{
        	   if(bestEffort)con.commit();
       	   }catch(Throwable t2){}           
       	   
           //##################### added by yfzhu for dispatching to shop
           // normally the mysql insertion will be fail, if do, cancel all transaction
       	   // following dreprecated 2008-05-23 since no mysql in pos anymore
           /*sqlVector=new Vector();
           if ( table.getDispatchType() != table.DISPATCH_NONE &&
                table.isActionEnabled(Table.SUBMIT)==false){
               MySQLObjectCreateImpl myc= new MySQLObjectCreateImpl();
               myc.setInvalidRows(invalidRows);
               sqlVector.addAll(myc.getSqlArray(hashMap,event,table,recordLen));
           }
           engine.doUpdate(sqlVector,con);
           */
     	   int[] poids= helper.getParentTablePKIDs(table,oids, con);
     	   helper.checkTableRows(parent, poids, con, helper.PARENT_NOT_FOUND);

           int successCount =recordLen;
           if (invalidRows !=null){
               successCount -= invalidRows.keySet().size();
           }
           //doNotification(event, table, hashMap,con);

           ValueHolder v = new ValueHolder();
           v.put("objectid",new Integer(objectId[0].intValue())) ; // this will be used in sheet_item.jsp to locate which sheet has been created
           v.put("jsonObjectCreated", new Boolean(jsonObjectCreated));
           StringBuffer sb=new StringBuffer();

           if(! bestEffort ){
               sb.append("@total-records-created-is@:"+ successCount +"("+tableDesc+")");
               v.put("message", sb.toString()) ;
           }else{
               sb.append("<pre>### "+ (new java.util.Date())+ ":@finished-import@(@consumed-to@ "+ (System.currentTimeMillis() -beginTime)/1000 + " @seconds@) ###");
               sb.append("\r\n");
               sb.append("@operate-table@："+tableDesc+"\r\n");
               sb.append("@total-lines@:"+ recordLen +", @success-import@:"+ successCount +" ,@fail-import@:"+(recordLen-successCount)+"\r\n");
               if(invalidRows !=null && successCount !=recordLen)for( int i=0;i< recordLen;i++){
                   String s= (String) invalidRows.get(new Integer(i));
                   if( s !=null)sb.append("@line@:"+ (i+startRow)+ ": "+ s+ "\r\n");
               }
               sb.append("</pre>");
               // if request to write to output file, then write to that file
               // else set to screen message
               if (outputFile !=null && outputFile.length() > 0){
                   //write to output file
                   FileWriter fw= new FileWriter(outputFile,false);
                   fw.write(sb.toString());
                   fw.close();
               }else{
                   v.put("message", sb.toString()) ;
               }

           }
           //helper.triggerAfterModify(sheetTable, sheetId, con);
           // after modify, first doing triggers on the current table
           // then do trigger on parent table, if exists.
//           helper.doTrigger("AC", table, oids, con);
     	  
           helper.doTrigger("AM", parent, poids, con);
//         commit all 
           try{
        	   if(bestEffort)con.commit();
       	   }catch(Throwable t2){}  
           return v;
       }catch(Exception e){
           try{
               if (outputFile !=null && outputFile.length() > 0){
                   FileWriter fw= new FileWriter(outputFile,false);
                   fw.write("### "+ (new java.util.Date())+ "@fail-import@(@consumed-to@ "+ (System.currentTimeMillis() -beginTime)/1000 + " @seconds@) ###");
                   fw.write("\r\n");
                   fw.write("@exception@:"+"\r\n");
                   fw.write(Tools.getExceptionStackTrace(e));
                   fw.close();
               }
           }catch(Exception e3){
               logger.error( "Found error writing to file:"+ outputFile, e3);
           }
           logger.error("Found error:", e);
           
           throw new NDSEventException(nds.util.StringUtils.getRootCause(e).getMessage(),e);

       }finally{
           try{if(stmt !=null) stmt.close(); }catch(Exception eee2){}
           helper.closeConnection(con, event);
       }

  }
  /**
   * Execute statement with data
   * @param pstmt
   * @param rowData
   * @param columnTypes elements are Column.NUMBER, STRING, DATENUMBER, DATE
   * @return execute result
   * @throws Exception
   */
  private int executeUpdate(PreparedStatement pstmt, ArrayList rowData, int[] columnTypes) throws Exception{
	  Object v;
	  for(int i=0;i< columnTypes.length;i++){
		  v=rowData.get(i);
		  switch(columnTypes[i]){
			  case Column.NUMBER:
			  case Column.DATENUMBER:
				  /* find oracle will check null in setBidDecimal, so we do not check here
				  if(v==null) pstmt.setNull(i+1,java.sql.Types.NUMERIC);
				  else pstmt.setBigDecimal(i+1,(BigDecimal)v);*/
				  pstmt.setBigDecimal(i+1,(BigDecimal)v);
				  break;
			  case Column.STRING:
				  pstmt.setString(i+1,(String)v );
				  break;
			  case Column.DATE:
				  pstmt.setTimestamp(i+1,new java.sql.Timestamp(((java.sql.Date)v).getTime()));
				  break;
			  default:
		       	 throw new NDSException("Unexpected column type:"+ columnTypes[i]);			  
		  }
	  }
	  return pstmt.executeUpdate();
  }
  /**
   * Create records of refbytable, duplicated from original one's ref records
   * @param table the main table 
   * @param oldId main table's original record id
   * @param newId main table's newly created record it
   * @param bundledTable main table's refby table which should be duplicated when create
   * @param conn connection
   * @throws Exception
   */
  private void copyRefbyTableRecords(Table table, int oldId, int newId,ArrayList bundledTable, Connection conn, DefaultWebEvent event) throws Exception{
  	//create CopyTo event to get the new ValueHoder object, then doing ObjectCreate over that
  	TableManager manager=TableManager.getInstance();
  	for(Iterator it=bundledTable.iterator();it.hasNext();){
	  	RefByTable itemTable=  (RefByTable) it.next();
	  	Column itemColumn= manager.getColumn(itemTable.getRefByColumnId());

	  	PairTable fixedColumns=new PairTable();
	  	fixedColumns.put(new Integer(itemColumn.getId()), ""+newId);
  		
	  	QueryRequestImpl query= CopyTo.getQuery( itemColumn.getTable(),null) ;
  		Expression expr=new Expression( new ColumnLink(new int[]{itemColumn.getId()}), "="+oldId, null);
  		query.addParam(expr);
  		logger.debug(query.toSQL());
  		QueryEngine engine= QueryEngine.getInstance();
  		//yfzhu 2007.5.10 previous call is doQueryNoRange
  		QueryResult result=engine.doQuery(query, conn);
  		if (result.getRowCount()==0) {
  			// no records found to copy
  			continue;
  		}
	  	DefaultWebEvent e=CopyTo.createCopyToEvent(result,-1);
	  	//logger.debug(e.toDetailString());
	  	
	  	// this event can be used directly for object creation
	  	e.setEventName("CommandEvent");
	  	e.setParameter("operatorid", (String)event.getParameterValue("operatorid"));
	  	e.setParameter("command", "ObjectCreate");
	  	e.setParameter("table",  ""+itemColumn.getTable().getId());
	  	e.setParameter("fixedcolumns", fixedColumns.toURLQueryString(""));
	  	e.put("nds.query.querysession", event.getQuerySession());
	  	e.put(helper.PARAM_SQL_CONNECTION, conn);
	  	helper.handleEvent(e);
  	}
  }

  private void doNotification(DefaultWebEvent event, Table table, HashMap hashMap, Connection con)
  throws NDSException, RemoteException{
      TableManager manager = helper.getTableManager();
      Vector vecObj =(Vector) hashMap.get(table.getPrimaryKey().getName());
      BigDecimal[] objectId = (BigDecimal[])vecObj.get(0);

      //################### added by yfzhu for mailing notifications
      if ( table.getName().toLowerCase().lastIndexOf("item") == table.getName().length() -4){
          // is item table, only get title table's id
          int sheetObjectId = Tools.getInt(event.getParameterValue("objectid"),-1 ) ;
          String sheetTableName=table.getName().substring(0,table.getName().length() -4 );
          Table sheetTable= manager.getTable(sheetTableName);
          if( sheetTable !=null){
              if (sheetObjectId != -1)
                  Notify(sheetTable, sheetObjectId, helper.getOperator(event).getDescription(),con);
          }else{
              logger.error("Could not load table named " + sheetTableName + ", which is parsed as sheet table from "+ table.getName());
          }
      }else{
          // trying to figure out the alternateKey's value
          Vector vecAKValues= (Vector) hashMap.get(table.getAlternateKey().getName());
          String[] aks=null;
          if( vecAKValues !=null) aks= (String[]) vecAKValues.get(0);
          else aks= getAKData(table, objectId,con);
          if( vecAKValues !=null) aks= (String[]) vecAKValues.get(0);
          Notify(table, objectId, aks,helper.getOperator(event).getDescription(),con);
      }

  }
  /**
   * Get AK data accordint to PK data
   * @return null if any error occurs.
   */
  private String[] getAKData(Table table, BigDecimal[] Ids, Connection con){
      try{
      ArrayList al=new ArrayList();
      String q="select "+ table.getAlternateKey().getName() + " from "+ table.getName() +
               " where " + table.getPrimaryKey().getName() + "=?";
      PreparedStatement pstmt= con.prepareStatement(q);
      for (int i=0;i< Ids.length;i++){
          pstmt.setInt(1, Ids[i].intValue() );
          ResultSet rs= pstmt.executeQuery();
          if( rs.next()){
              al.add(rs.getString(1));
          }else{
              al.add("id="+Ids[i] );
          }
          try{rs.close();}catch(Exception e2){}
      }

      String[] s= new String[al.size()];
      for (int i=0;i< s.length;i++){
          s[i]=(String) al.get(i);
      }
      return s;
      }catch(Exception e){
          logger.error("Error in getAKData():" + e);
          return null;
      }
  }

 /*
 *  返回可以操作的列在web页面中的参数
 */
  private Vector getOperateParameterNames(Table table) throws NDSException{

      ArrayList list = table.getShowableColumns(Table.ADD);
      Iterator ite = list.iterator();
      String parameterName = null;
      Table refTable = null;
      Vector vec = new Vector();
      while(ite.hasNext() ){
          Column column = (Column)ite.next();
          String columnName = column.getName();
//          logger.debug("the value of columnName si:"+columnName) ;
          if(column.isModifiable(Column.ADD )){
              refTable = column.getReferenceTable();
              if(refTable!=null){
                  Column akColumn = refTable.getAlternateKey();
                  String alColumnName  =  akColumn.getName() ;
                  /*
                  int lastIndex = columnName.lastIndexOf("ID");
                  String colStr = columnName.substring(0,lastIndex) ;
                  parameterName = colStr.trim() +"_"+alColumnName;*/
                  parameterName= (columnName+"__"+ alColumnName).toLowerCase();
//                  logger.debug("the value of parameter name is:"+parameterName) ;
              }else{
                  parameterName = columnName;
//                  logger.debug("the value of parameter name is:"+parameterName) ;
              }
              vec.addElement(parameterName);
          }
      }
      return vec;
  }
  // 返回要操作的记录的个数，一般情况下是1，在sheetitem中时是根据具体情况而定
  private int getRecordLength(Vector paramVec,DefaultWebEvent event) throws NDSException{
//    logger.debug("begin to execute getRecordLength") ;
    Iterator ite = paramVec.iterator() ;
    if(paramVec.isEmpty() ){
//        logger.debug("No related parameter！"+paramVec.size() ) ;
        throw new NDSEventException("Internal Error:No related parameter in the web page");
    }
    int maxLength = 1;
    while(ite.hasNext() ){
        String paraName = (String)ite.next();
//        logger.debug("the value of paraName is:"+paraName) ;
        String[] value = event.getParameterValues(paraName);

       /*// if(value.length >1){
//            logger.debug("The new paraName of the parameter is:"+paraName);
//            logger.debug("The length of the parameter is:"+value.length );

       // }
        if(value.length >maxLength){
            maxLength = value.length;
        }
        */
    }
    return maxLength;
  }

    // 返回要操作的记录的个数，一般情况下是1，在sheetitem中时是根据具体情况而定
  private int getRecordLength(Table table,DefaultWebEvent event) throws NDSException{
      ArrayList list = table.getShowableColumns(Table.ADD);
      Iterator ite = list.iterator();
//      logger.debug("The new size is:::::"+list.size() ) ;
      String parameterName = null;
      Table refTable = null;
      Vector vec = new Vector();
      int newlength = 0;
      PairTable fixedColumns=DefaultWebEventHelper.getFixedColumns(event);
      while(ite.hasNext() ){
          Column column = (Column)ite.next();
          if( fixedColumns.get( new Integer(column.getId()))!=null){
          	// fixed column will not show in the screen
         	continue;
         }          
          String columnName = column.getName();
          if(column.isModifiable(Column.ADD )){
              refTable = column.getReferenceTable();
              if(refTable!=null){
                  Column akColumn = refTable.getAlternateKey();
                  String alColumnName  =  akColumn.getName() ;
                  /*int lastIndex = columnName.lastIndexOf("ID");
                  String colStr = columnName.substring(0,lastIndex) ;
                  parameterName = colStr.trim() +"_"+alColumnName;*/
                  parameterName= (columnName+"__"+ alColumnName).toLowerCase();
//                  logger.debug("the value of parameter name is:"+parameterName) ;
              }else{
                  parameterName = columnName;
//                  logger.debug("the value of parameter name is:"+parameterName) ;
              }
              if(column.isNullable()==false ){
                  String[] tt = event.getParameterValues(parameterName);
                  if(tt ==null ) throw new NDSException("Could not found value(s) for "+parameterName );
                  for(int i = 0;i<tt.length ;i++){
                     if((tt[i]!=null)&&(!"".equals(tt[i])) ){
                         newlength+=1;
                     }

                  }
//                  logger.debug("The value of newlength is:"+newlength) ;
                  return newlength;
              }
          }
      }
      if(newlength==0) throw new NDSException("Internal error: There must have at least one 'not null' column and be modifiable (and not fixed)in the screen.");
      return newlength;
  }
  /**
  * Notify of sheet table modification
  */
  private void Notify(Table sheetTable, int sheetObjectId, String creatorDesc, Connection con){
      // first get the table record description
      try{
          String no=null;
          StringBuffer briefMsg=new StringBuffer(), detailMsg=new StringBuffer();
          NotificationManager nm=nds.mail.NotificationManager.getInstance();
          ResultSet rs= QueryEngine.getInstance().doQuery("select "+ sheetTable.getAlternateKey().getName() +
                  " from "+ sheetTable.getName()+ " where id=" + sheetObjectId);
          if( rs.next() ){
              no= rs.getString(1);
          }
          try{ rs.close();} catch(Exception es){}
          if(no !=null) briefMsg.append( sheetTable.getDescription(Locale.CHINA) + "("+ no + ") 被"+ creatorDesc+ "修改.");
          else briefMsg.append( sheetTable.getDescription(Locale.CHINA) + "(id=" + sheetObjectId + ") 被"+ creatorDesc+ "修改.");
          String webroot=nm.getProperty("weburl", "http://mit:8001/nds");
          detailMsg.append(briefMsg );
          detailMsg.append("\n\r");
          detailMsg.append("请访问网页"+ webroot+ "/objext/sheet_title.jsp?table="+sheetTable.getId() +"&id="+ sheetObjectId);
          nm.handleObject(sheetTable.getId(), sheetObjectId, "modify", briefMsg, detailMsg,con);

      }catch(Exception e){
          logger.error("Could not notify modification of " + sheetTable.getName()+ ", id=" +sheetObjectId, e);
      }
  }
  /**
   * Should consider when table is Item table, then only the primary table should be notified
   * @param table the main table
   * @param objIds the main table row's id
   * @param if table has ak, then ak value will be displayed, or null means not found
   */
  private void Notify(Table table, BigDecimal[] objIds, String[] aks,String creatorDesc,Connection con){
      int tableId= table.getId();
      int objectId;
      StringBuffer briefMsg, detailMsg;
      NotificationManager nm=nds.mail.NotificationManager.getInstance();
      for ( int i=0;i< objIds.length;i++){
          objectId= objIds[i].intValue() ;
          if(objectId ==-1) continue;
          briefMsg=new StringBuffer();detailMsg=new StringBuffer();
          if( aks !=null) briefMsg.append(table.getDescription(Locale.CHINA) + "("+ aks[i] + ") 被"+ creatorDesc+ "创建.");
          else briefMsg.append(table.getDescription(Locale.CHINA) + "(id=" + objectId + ") 被"+ creatorDesc+ "创建.");
          String webroot=nm.getProperty("weburl", "http://mit:8001/nds");
          detailMsg.append(briefMsg );
          detailMsg.append("\n\r");
          detailMsg.append("请访问网页"+ webroot+ "/objext/sheet_title.jsp?table="+tableId+"&id="+ objectId);
          nm.handleObject(tableId, objectId, "create", briefMsg, detailMsg,con);
      }

  }
}

