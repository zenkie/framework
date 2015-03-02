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
import java.util.*;

import nds.util.*;
import nds.control.web.AttachmentManager;
import nds.control.web.WebUtils;
import nds.control.ejb.Command;
import nds.control.ejb.DefaultWebEventHelper;
import nds.control.ejb.MySQLObjectCreateImpl;
import nds.control.ejb.ObjectCreateImpl;
import nds.control.event.DefaultWebEvent;
import nds.control.event.NDSEventException;
import nds.control.util.ValueHolder;
import nds.mail.NotificationManager;
import nds.monitor.MonitorManager;
import nds.monitor.ObjectActionEvent;
import nds.monitor.ObjectActionEvent.ActionType;
//Edit by Robin 2010-07-30
//import nds.monitor.MonitorManager;
//import nds.monitor.ObjectActionEvent;

//import nds.monitor.ObjectActionEvent.ActionType;
import nds.query.*;
import nds.schema.Column;
import nds.schema.RefByTable;
import nds.schema.Table;
import nds.schema.TableManager;
import nds.security.User;
import nds.util.Attachment;
import nds.util.NDSException;
import nds.util.PairTable;
import nds.util.Tools;
import nds.util.Validator;
import org.json.*;
/**
 * Do object creation
 */
public class ObjectCreate extends Command{
	public ObjectCreate(){
		Configurations conf=(Configurations)nds.control.web.WebUtils.getServletContextManager().getActor(nds.util.WebKeys.CONFIGURATIONS);
		showOriginalRowInfo="true".equals(conf.getProperty("object.create.show.original.row", "false"));
	}
	/**
     * Whether this command use internal transaction control. For normal command, transaction is controled by
     * caller, yet for some special ones, the command will control transaction seperativly, that is, the command
     * will new transaction and commit that one explicitly
     * @return false if use transaction from caller
     */
	/*
    public boolean internalTransaction(){
    	return true;
    }
    */
    public boolean internalTransaction(DefaultWebEvent paramDefaultWebEvent)
      {
    	boolean	bestEffort=false;
		try {
			 	bestEffort = new Boolean(
					(String) paramDefaultWebEvent
							.getParameterValue("best_effort")).booleanValue();
		} catch (Exception e) {
		}
		return bestEffort;
      }    
    
    /**
     * 纤丝鸟的需求 
     */
    private boolean showOriginalRowInfo=false;

    /**
     * This is only for creation list, that is all columns minus those that current user 
     * has no permission to read (column's security grade is greater that user's sgrade)
     * @return elements are Column
     */
    private ArrayList prepareModifiableColumns(DefaultWebEvent event, Table table){
 	   QuerySession qs= event.getQuerySession();
 	   int sg=(qs==null?0: qs.getSecurityGrade());
 	   if(sg==0) return table.getAllColumns() ;
 	   ArrayList al=new ArrayList();
 	   ArrayList ac = table.getAllColumns() ;
 	   for(int i=0;i<ac.size();i++){
 		   Column c=(Column)ac.get(i);
 		   if(c.getSecurityGrade()<=sg) al.add(c);
 	   }
 	   return al;
    }    
  /**
   * Support update for object create when param "update_on_unique_constraints" is set to "Y" (default is "N")
   * 	"output_json" - "y"|"n"(default) json array to contain error info
   * @return  if jsonobj found in event, and json lines splitted, will singal "jsonObjectCreated"
   *  
   * attribute to Boolean.TRUE in returned ValueHolder
   */	
  public ValueHolder execute(DefaultWebEvent event) throws NDSException ,RemoteException{
  	  boolean jsonObjectCreated=false;	
  	  logger.debug(event.toDetailString());
      TableManager manager = helper.getTableManager();
      Table table = manager.findTable(event.getParameterValue("table"));
      int tableId = table.getId();
      User usr=helper.getOperator(event);
      int userId= usr.id.intValue();
      boolean isRoot= "root".equals(usr.name) ;
      String tableName = table.getName();          // 得到表的名字
      String tableDesc = table.getDescription(Locale.CHINA) ;
      QuerySession qsession=event.getQuerySession();
      
      Configurations conf= (Configurations)WebUtils.getServletContextManager().getActor( nds.util.WebKeys.CONFIGURATIONS);

      //用户修改的数据，在修改后他是否必须是仍然有权限修改的
      boolean after_modify_check ="true".equals(conf.getProperty("object.modify.after_modify_check", "true")); 
      
      boolean isOutputJSONError=Tools.getYesNo(
    		  event.getParameterValue("output_json", true), false);
      
      /*
       *When update_on_unique_constraints, insert exception will be catched and try update in consequence
      	Currently only supported for single record insertion (matrix input format not supported)
      	and batch insert mode (excel importing). 
      	带界面的输入如果重复很难判定该如何刷新明细（例如：两行新增，第二行实际需要update到第一行里，该如何显示呢？
      */
      boolean updateOnUniqueConstraints=Tools.getYesNo(
    		  event.getParameterValue("update_on_unique_constraints", true), false);
      logger.debug("update_on_unique_constraints="+ updateOnUniqueConstraints);
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
      PreparedStatement stmt=null; // insert into xxx () values (?,?)
      PreparedStatement stmtUpdate=null; // update xxx set a=?,c=? where id=?
      PreparedStatement stmtIDByUdx=null; // select id from xxx where c=?
      SPResult spr=null; // trigger result
      boolean shouldAddModifierIdToUpdateStatement=false;
      ResultSet rs=null;// check key result
      //Table sheetTable=null;int sheetId=-1;
       try{
       	   con= helper.getConnection(event);
       	   if(bestEffort)con.setAutoCommit(false);

       	   //lock parent record specified by fixedcolumns first, must below bestEffort, since data should be locked
       	   String fc=(String)event.getParameterValue("fixedcolumns");
       	   if(fc!=null ){
		       	PairTable fixedColumns=PairTable.parseIntTable(fc, null);
				for( Iterator it=fixedColumns.keys();it.hasNext();){
		        	Integer key=(Integer) it.next();
		            Column col=manager.getColumn( key.intValue());
		            if(col.getReferenceTable()!=null){
		            	//lock col.rt with pk id
		            	int ptoId= Tools.getInt(fixedColumns.get(key),-1);
		            	if( ptoId!=-1){
		            		QueryUtils.lockRecord(col.getReferenceTable(), ptoId, con);
		            	}
		            }
		        }
       	   }
       	   
           ArrayList colArray = this.prepareModifiableColumns(event, table);  // 得到表的所有列名

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

           String psql=createImpl.getPreparedStatementSQL();
           logger.debug(psql);
           stmt=con.prepareStatement(psql);

           QueryEngine engine = QueryEngine.getInstance() ;
           
           boolean createAttributeDetail=table.supportAttributeDetail();
           boolean createAttributeDetailByJson=false;

           
           boolean createAttributeDetailByCopy=false;
           Object jsonobj= event.getParameterValue("jsonobj");
           Object[] jo=null;
           BigDecimal[] pdtIds=null;

           logger.debug(event.toDetailString());
           
           int[] asiRelateColumnsPosInStatement=null;
           if(createAttributeDetail){
	           if(jsonobj!=null && !jsonobj.equals(org.json.JSONObject.NULL)){
               	  	if(jsonobj.getClass().isArray()){
               	  		jo= ((Object[])jsonobj);
               	  	}else jo= new Object[]{jsonobj};
		            Vector pdts =(Vector) hashMap.get("M_PRODUCT_ID");
		            if(pdts!=null)pdtIds = (BigDecimal[])pdts.elementAt(0);
		            createAttributeDetailByJson = pdts!=null;
	           }else{
	        	   /**
	        	    * deprecated, no longer support m_attributedetail table.
	        	    * yfzhu 2009-06-06
	        	    */
	        	   //createAttributeDetailByCopy=(copyFromIds!=null);
	           }
	           if(createAttributeDetailByJson){
	        	   asiRelateColumnsPosInStatement=createImpl.getASIRelateColumnsPosInStatement();
		           if(asiRelateColumnsPosInStatement[0]==-1 || asiRelateColumnsPosInStatement[1]==-1 
		        		   || asiRelateColumnsPosInStatement[2]==-1)
		        	   throw new NDSException("Not expected condition: not found ID, ASI or *QTY* column in createAttributeDetail request");
	           }
           }
           logger.debug("createAttributeDetailByJson="+ createAttributeDetailByJson+
           			",createAttributeDetailByCopy="+createAttributeDetailByCopy);
           int realPos;
           
           //String sql;
    	   
           ArrayList row;
           java.sql.Savepoint  sp=null;
           int[] sqlDataColumnTypesForUpdate=null, sqlDataColumnTypesForUdx=null; // elements: Column.STRING/NUMBER/DATENUMBER
           int[] sqlDataIndexForUpdate=null, sqlDataIndexForUdx=null; // elements: input which col in row of sqlData
           
           String uniqueIndexName= manager.getUniqueIndexName(table);
           
           for( int i=0;i< sqlData.size();i++){
           	   realPos=((Integer)sqlIndex.get(i)).intValue();
           	   //Managed transaction in 
           	   if(bestEffort)sp= con.setSavepoint();
           	   
               try{
                   row= (ArrayList) sqlData.get(i);
                   //if(i< 10)logger.debug(sql); // only first 10 records will be displayed
                   
                   setData(stmt,row,sqlDataColumnTypes,usr,table);
                   
                   // check if attribute detail support table, since 3.0 at 2007-05-30
                   if(createAttributeDetailByJson){
                   		//jsonobj will be stored into m_attributedetail table
                	   
                   		if(pdtIds[realPos]==null){
                   			logger.debug(" not found product id");
                   		}else{
                   			spr=helper.execStmtOfAttributeDetailRecordsByJSON(table, jo[realPos], 
                   					oids[i], con, stmt,asiRelateColumnsPosInStatement,"AC");
                   			jsonObjectCreated=true;//"jsonobj-splitted"
                   		}
                   }else if(createAttributeDetailByCopy){
                   		// it's not possible that we do both createAttributeDetailByJson and createAttributeDetailByCopy
                	   if(true)throw new NDSException("no longer supportd");
                   		/**
                   		 * copy from table: table, id: cfid= Tools.getInt(copyFromIds[i], -1)
                   		 * copy to   table: table, id: oids[i]
                   		 */
                   		cfid= Tools.getInt(copyFromIds[realPos], -1);
                   		stmt.executeUpdate();
                   		if(cfid!=-1)
                   			helper.createAttributeDetailRecordsByCopy(table, userId, cfid,oids[realPos],con);
                   		spr=helper.doTrigger("AC", table, oids[realPos], con);
                   }else{
                	   //normal update and ac procedure
                	   try{
                		    stmt.executeUpdate();
            			    LicenseManager.validateLicense(nds.util.WebKeys.pdt_name,"5.0","",false);
            				Iterator b=LicenseManager.getLicenses();
            				int un=0,pn = 0;
            			    while (b.hasNext()) {
            			    	LicenseWrapper o = (LicenseWrapper)b.next();
            			    	un=o.getNumUsers();
            					pn=o.getNumPOS();
            			    }

                	        if(table.getName().equals("C_STORE")){
                	        	rs= con.createStatement().executeQuery("select count(*) from c_store t where t.isactive='Y' and t.isretail='Y'");
                	          	rs.next();
                		    	int cpos=rs.getInt(1);
                		    	if(cpos>pn){
                		    		logger.debug("now pos:"+String.valueOf(cpos));
                		    		logger.debug("licences pos:"+String.valueOf(pn));
                		    		throw new NDSEventException("当前pos点数已超！请联系商家！");
                		    	}
                				   
                	        }else if(table.getName().equals("USERS")){
                	        	rs= con.createStatement().executeQuery("select count(*) from users t where t.isactive='Y' and t.IS_SYS_USER!='Y'");
                	          	rs.next();
                		    	int cus=rs.getInt(1);
                		    	if(cus>un){
                		    		logger.debug("now users:"+String.valueOf(cus));
                		    		logger.debug("licences users:"+String.valueOf(pn));
                		    		throw new NDSEventException("当前用户点数已超！请联系商家！");
                		    	}
                			    
                		   }
                		   
                		   spr=helper.doTrigger("AC", table, oids[realPos], con);
                		  // Edit by Robin 2010-07-30
                		   //monitor plugin
                		   JSONObject cxt=new JSONObject();
                		   cxt.put("source", this);
                		   cxt.put("connection", con);
                		   cxt.put("statemachine", this.helper.getStateMachine());
                		   cxt.put("javax.servlet.http.HttpServletRequest", 
                		   event.getParameterValue("javax.servlet.http.HttpServletRequest", true));
                		   ObjectActionEvent oae=new ObjectActionEvent(table.getId(),
                				   oids[realPos], usr.adClientId,ActionType.AC, usr, cxt);
                		   MonitorManager.getInstance().dispatchEvent(oae);
                		   // check write permission on that record, 若不校验界面上可生成无写权限访问的记录, 
                		   //权限校验仅针对菜单项单据 yfzhu 2009-12-13, root晃过
                		   if( !isRoot && table.isMenuObject()&& !nds.control.util.SecurityUtils.hasObjectPermission(con,userId, usr.name, 
                				   table.getName(), oids[realPos], nds.security.Directory.WRITE, qsession)){
                			   logger.debug("no permission to create a uneditable record on table="+ table+", id="+ oids[realPos]+" by "+ usr.name+" of id"+ usr.id);
                			   throw new NDSEventException("@no-permission@");
                		   }	   
                	   }catch(SQLException sqlex){
                		   //support for unique constraints confliction, and try update
                		   //如果是Unique Index 错误，要找到存在的记录，做更新
                		   if(updateOnUniqueConstraints && isUniqueConstraintsError(sqlex,uniqueIndexName)){
                			   if(stmtUpdate==null){
                				   //createImpl.prepareUpdateForUdx();
                				   // select id from xxx where udx_col=? and udx_col2=? 
                				   String psqlIdByUdx= createImpl.getPreparedStatementSQLForUdx();
                				   logger.debug(psqlIdByUdx);
                				   stmtIDByUdx=con.prepareStatement(psqlIdByUdx);
                		           sqlDataColumnTypesForUdx=createImpl.getSQLDataColumnTypesForUdx();
                		           sqlDataIndexForUdx=createImpl.getSQLDataIndexForUdx();
                				   // update xxx set a=?, b=?
                				   String psqlUpdate=createImpl.getPreparedStatementSQLForUpdate();
                		           logger.debug(psqlUpdate);
                				   stmtUpdate=con.prepareStatement(psqlUpdate);
                				   sqlDataColumnTypesForUpdate=createImpl.getSQLDataColumnTypesForUpdate();
                				   sqlDataIndexForUpdate =createImpl.getSQLDataIndexForUpdate();
                				   shouldAddModifierIdToUpdateStatement=createImpl.shouldAddModifierIdToUpdateStatement();
                			   }
                			   logger.debug( Tools.toString(row, ","));
                			   setDataIdByUdx(stmtIDByUdx,row,sqlDataIndexForUdx,sqlDataColumnTypesForUdx );
                			   int idByUdx=-1;
                			   ResultSet rsIdByUdx= stmtIDByUdx.executeQuery();
                			   try{
	                			   if(rsIdByUdx!=null && rsIdByUdx.next()){
	                				   idByUdx= rsIdByUdx.getInt(1);
	                			   }else{
	                				   throw new NDSException("unexpected condition, rsIdByUdx may not be null or no records");
	                			   }
                			   }finally{
                				   if(rsIdByUdx!=null) try{rsIdByUdx.close();}catch(Throwable txter){}
                			   }
                			   if(idByUdx!=-1){
                				   // check write permission, 若不校验界面上可修改无写权限写的记录, 
                        		   //权限校验仅针对菜单项单据 yfzhu 2009-12-13
                        		   if(!isRoot && table.isMenuObject()&& !nds.control.util.SecurityUtils.hasObjectPermission(con,userId, usr.name, 
                        				   table.getName(), idByUdx, nds.security.Directory.WRITE, qsession)){
                        			   logger.debug("no permission to modify(by udx) a uneditable record on table="+ table+", id="+ idByUdx+" by "+ usr.name+" of id"+ usr.id);
                        			   throw new NDSEventException("@no-permission@");
                        		   }
                				   setDataForUpdate(stmtUpdate,row,sqlDataIndexForUpdate,
                						   sqlDataColumnTypesForUpdate,userId, idByUdx,shouldAddModifierIdToUpdateStatement);
                				   int ucnt=stmtUpdate.executeUpdate();
                				   //do am trigger on that row upated
                				   spr=helper.doTrigger("AM", table, idByUdx, con);
                				   oids[realPos]=idByUdx; // not the created one, but the old value
                				   // 再校验一次权限，否则用户可以将自己有权限改的数据改成自己没有权限修改的数据。
                				   // 例如，用户只能修改上海的单据，可是他将上海字段内容修改为成都了。
                				   if(!isRoot  && after_modify_check && table.isMenuObject()){
                            		   if( !nds.control.util.SecurityUtils.hasObjectPermission(con,userId, usr.name, 
                            				   table.getName(), idByUdx, nds.security.Directory.WRITE, qsession)){
                            			   logger.debug("no permission to modify a record to uneditable one on table="+ table+", id="+ idByUdx+" by "+ usr.name+" of id"+ usr.id);
                            			   throw new NDSEventException("@no-permission@");
                            		   }	   
                					   
                				   }
                			   }
                			   
                		   }else{
                			   throw sqlex;
                		   }
                	   }
                   }
                   
                   
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

           //check parent table records exist and modifiable
     	   int[] poids= helper.getParentTablePKIDs(table,oids, con);
     	   helper.checkTableRowsModifiable(parent, poids, con);
           
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

           int successCount =recordLen;
           if (invalidRows !=null){
               successCount -= invalidRows.keySet().size();
           }
           //doNotification(event, table, hashMap,con);
           ValueHolder v = new ValueHolder();
           v.put("objectid",new Integer(objectId[0].intValue())) ; // this will be used in sheet_item.jsp to locate which sheet has been created

           
           JSONObject restResult=new JSONObject();// for rest call
           restResult.put("objectid", new Integer(objectId[0].intValue()));
           v.put("restResult", restResult);

           v.put("jsonObjectCreated", new Boolean(jsonObjectCreated));
           v.put("spresult", spr);
           StringBuffer sb=new StringBuffer();

           logger.info("created table="+ table+", lines="+ recordLen+", success="+  successCount+", failed="+(recordLen-successCount)+", first id="+objectId[0]+" by "+ usr.name+" of id"+ usr.id);
           
           if(! bestEffort ){
        	   sb.append("@total-records-created-is@:"+ successCount +"("+tableDesc+")");
               v.put("message", sb.toString()) ;
           }else{
        	   
               sb.append("<pre>### "+ (new java.util.Date())+ ":@finished-import@(@consumed-to@ "+ (System.currentTimeMillis() -beginTime)/1000 + " @seconds@) ###");
               sb.append("\r\n");
               sb.append("@operate-table@："+tableDesc+"\r\n");
               sb.append("@total-lines@:"+ recordLen +", @success-import@:"+ successCount +" ,@fail-import@:"+(recordLen-successCount)+"\r\n");
               if(invalidRows !=null && successCount !=recordLen){
                   JSONArray ja=new JSONArray();
                   JSONObject job;
            	   for( int i=0;i< recordLen;i++){
	                   String s= (String) invalidRows.get(new Integer(i));
	                   if( s !=null){
	                	   if(isOutputJSONError){
	                		   job=new JSONObject();
	                		   job.put("lineno",(i+startRow) );
	                		   job.put("errmsg",s);
	                		   ja.put(job);
	                	   }else{
	                		   if(showOriginalRowInfo){
	                			   //按纤丝鸟要求，返回所有输入的列,2010-3-25
	                			   sb.append("@line@:"+ (i+startRow)+ ": "+ s+" ("+
	                					   createImpl.getRowOrigInfo(i) +")\r\n");
	                		   }else
	                			   sb.append("@line@:"+ (i+startRow)+ ": "+ s+ "\r\n");
	                	   }
	                   }
	                   
            	   }
            	   restResult.put("errors", ja);
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
            	   if(isOutputJSONError){
            		   v.put("message","complete");
            	   }else
            		   v.put("message", sb.toString()) ;
               }

           }


     	  
           helper.doTrigger("AM", parent, poids, con);
//         commit all 
           try{
        	   if(bestEffort)con.commit();
       	   }catch(Throwable t2){}  
           return v;
       }catch(Exception e){
    	   try{
    		   if(bestEffort)con.rollback();
    	   }catch(Throwable t3){}
    	   
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
           try{if(stmtUpdate !=null) stmtUpdate.close(); }catch(Exception eee2){}
           try{if(stmtIDByUdx !=null) stmtIDByUdx.close(); }catch(Exception eee2){}
           try{if(rs!=null) rs.close();}catch(Throwable t){}
           
           helper.closeConnection(con, event);
       }

  }
  /**
   * set statement with data
   * @param pstmt
   * @param rowData
   * @param columnTypes elements are Column.NUMBER, STRING, DATENUMBER, DATE
   * @return execute result
   * @throws Exception
   */
  private void setData(PreparedStatement pstmt, ArrayList rowData, int[] columnTypes,User userWeb,Table table) throws Exception{
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
				  String valueOne=(String)v;
				  if(valueOne!=null){
		            	if(valueOne.indexOf("&objectpath=")>0){
		            	String tmpath=valueOne.substring(valueOne.indexOf("&objectpath=")+12);
		            	int colpos=valueOne.indexOf("&column=");
		            	int objpos=valueOne.indexOf("&objectid=");
		            	int colid=Integer.parseInt(valueOne.substring(colpos+8,objpos));
		            	valueOne=valueOne.substring(0,objpos+10)+String.valueOf(rowData.get(0));
		            	/*
		            	System.out.print("!!!!!!!!!!!!!!!!!!!!!!!id"+rowData.get(0));     
		            	System.out.print("!!!!!!!!!!!!!!!!!!!!!!!colid"+colid); 
		            	System.out.print("!!!!!!!!!!!!!!!!!!!!!!!valueOne"+valueOne);
		            	System.out.print("!!!!!!!!!!!!!!!!!!!!!!!!"+tmpath); 
		            	*/  
		            	Column col=table.getColumn(colid);
		            	AttachmentManager attm=(AttachmentManager)WebUtils.getServletContextManager().getActor(nds.util.WebKeys.ATTACHMENT_MANAGER);
		        		attm.renameattDir(userWeb.getClientDomain()+"/" + table.getRealTableName()+"/"+col.getName(),tmpath,String.valueOf(rowData.get(0)));

		            	}
				  }
				  pstmt.setString(i+1,valueOne);
				  break;
			  case Column.DATE:
				  if( v==null) pstmt.setNull(i+1, java.sql.Types.TIMESTAMP);
				  else pstmt.setTimestamp(i+1,new java.sql.Timestamp(((java.sql.Date)v).getTime()));
				  break;
			  default:
		       	 throw new NDSException("Unexpected column type:"+ columnTypes[i]);			  
		  }
	  }
	  
  }
  //(stmtUpdate,row,sqlDataIndexForUpdate,sqlDataColumnTypesForUpdate,userId, idByUdx)
/**
   * set statement with data
   * @param pstmt
   * @param rowData
   * @param sqlDataIndexForUdx colum index in rowData(start from 0)
   * @param columnTypes elements are Column.NUMBER, STRING, DATENUMBER, DATE
   * 
   * @return execute result
   * @throws Exception
   */
  private void setDataForUpdate(PreparedStatement pstmt, ArrayList rowData, 
		  int[] sqlDataIndexForUpdate, int[] columnTypes, int userId, int objectId,
		  boolean shouldAddModifierIdToUpdateStatement) throws Exception{
	  Object v;
	  for(int i=0;i< columnTypes.length;i++){
		  v=rowData.get(sqlDataIndexForUpdate[i]);
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
				  if(v==null)pstmt.setNull(	i+1,java.sql.Types.TIMESTAMP);
 				  else pstmt.setTimestamp(i+1,new java.sql.Timestamp(((java.sql.Date)v).getTime()));
				  
				  break;
			  default:
		       	 throw new NDSException("Unexpected column type:"+ columnTypes[i]);			  
		  }
	  }
	  if(shouldAddModifierIdToUpdateStatement){
		  pstmt.setInt(columnTypes.length+1, userId);
		  pstmt.setInt(columnTypes.length+2, objectId);
	  }else{
		  pstmt.setInt(columnTypes.length+1, objectId);
	  }
	  
  }  
  /**
   * set statement with data
   * @param pstmt
   * @param rowData
   * @param sqlDataIndexForUdx colum index in rowData(start from 0)
   * @param columnTypes elements are Column.NUMBER, STRING, DATENUMBER, DATE
   * 
   * @return execute result
   * @throws Exception
   */
  private void setDataIdByUdx(PreparedStatement pstmt, ArrayList rowData, int[] sqlDataIndexForUdx, int[] columnTypes) throws Exception{
	  Object v;
	  for(int i=0;i< columnTypes.length;i++){
		  v=rowData.get(sqlDataIndexForUdx[i]);
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
	  
  }
  /**
   * ORA-00001 is unique index error, and the error should not be ID confliction 
   * @param indexName the unique index to be checked
   * @return true if is unique constraints error (not PK )
   */
  private boolean isUniqueConstraintsError(SQLException t, String indexName){
	  	String s= nds.util.StringUtils.getRootCause(t).getMessage();
  		if(s==null) s=t.getMessage();
  		boolean b= (s.indexOf("ORA-00001") > -1 && s.indexOf(indexName) > -1);
  	  	logger.debug("check isUniqueConstraintsError("+s+") ="+ b);
  	  	return b;

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
                  logger.debug("the value of parameter name is:"+parameterName) ;
              }else{
                  parameterName = columnName;
                  logger.debug("the value of parameter name is:"+parameterName) ;
              }
              if(column.isNullable()==false ){
                  String[] tt = event.getParameterValues(parameterName);
                  if(tt ==null ) throw new NDSException("Could not found value(s) for "+parameterName );
                  for(int i = 0;i<tt.length ;i++){
                     if((tt[i]!=null)&&(!"".equals(tt[i])) ){
                         newlength+=1;
                     }

                  }
                  logger.debug("The value of newlength is:"+newlength) ;
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


