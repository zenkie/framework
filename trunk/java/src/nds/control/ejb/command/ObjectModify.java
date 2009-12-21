package nds.control.ejb.command;
import java.math.BigDecimal;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import org.json.*;

import nds.control.ejb.Command;
import nds.control.ejb.MySQLObjectModifyImpl;
import nds.control.ejb.ObjectModifyImpl;
import nds.control.event.DefaultWebEvent;
import nds.control.event.NDSEventException;
import nds.control.util.ValueHolder;
import nds.control.web.WebUtils;
import nds.mail.NotificationManager;
import nds.query.*;
import nds.schema.*;
import nds.security.User;
import nds.util.Configurations;
import nds.util.NDSException;
import nds.util.Tools;
/**
 * Title:        NDS Project
 * Description:  San gao shui yuan, mu xiang ren jia
 * Copyright:    Copyright (c) 2001
 * Company:      aic
 * @author yfzhu
 * @version 1.0
 */

public class ObjectModify extends Command{
   /**
    * If event contains column_masks, then will first get all columns that modifiable,
    * then filter those not has any bit set in typer "column_masks". 
    * So the columns is less or equal than Column.MASK_MODIFY_EDIT
    * @param event
    * @param table 
    * @return
    * @throws Exception
    */
	private ArrayList getModifiableColumns(DefaultWebEvent event, Table table) throws NDSException{
		
		ArrayList colList=null;
		Object masks= event.getParameterValue("column_masks"); // JSONArray
		if(masks!=null && masks instanceof JSONArray){
			logger.debug(masks.toString());
			
			int[] mas= new int[((JSONArray)masks).length()];
			for(int k=0;k< mas.length;k++){
				try{
					mas[k]=Tools.getInt(((JSONArray)masks).get(k) ,-1);
				}catch(Throwable t){
					throw new NDSException("Fail to parse array of "+ masks.toString(), t);
				}
			}
			
			ArrayList al= table.getColumns(new int[]{Column.MASK_MODIFY_EDIT}, false);
			for(int i=al.size()-1;i>=0;i--){
				Column col= (Column)al.get(i);
				boolean isInMasks=false;
				for(int j=0;j<mas.length;j++){
					if( col.isMaskSet(mas[j])){
						isInMasks=true;
						break;
					}
				}
				if(!isInMasks) al.remove(i);
			}
			return al;
		}else{
	       colList= table.getModifiableColumns(
	       		(event.getParameterValue("arrayItemSelecter")!=null? nds.schema.Column.QUERY_SUBLIST:Column.MODIFY));
			
		}
		return colList;
	}
	  /**
	   * @return  if jsonobj found in event, and json lines splitted, will singal "jsonobj-splitted" 
	   * attribute to Boolean.TRUE in returned ValueHolder
	   */	
	public ValueHolder execute(DefaultWebEvent event) throws NDSException ,RemoteException{
		logger.debug(event.toDetailString());
		boolean jsonObjectCreated=false;	
		/**
  	 * 2005-11-15 增加了对status 字段的判断，如果status字段为2 则当前记录不允许修改。这种情况主要发生
  	 * 在支持审计的单视图模式下。此模式下所有的单据都在一个界面，对于已审计通过的单据应禁止修改。系统会在通过
  	 * 审计的单据上修改status=2，作为判断条件。
  	 */
   	/*try{
   		Thread.sleep(15*1000);
   	}catch(Exception ee){
   		logger.error(ee.toString());
   	}*/
    User usr= helper.getOperator(event);	
    int userId=usr.id.intValue();
    boolean isRoot= "root".equals(usr.name) ;
    
   	java.sql.Connection con=null;
       try{
       // 得到所要操作的表的名字
       TableManager manager = helper.getTableManager() ;
       int tableId = Tools.getInt(event.getParameterValue("table"),-1 ) ;
       Table table = manager.getTable(tableId) ;
       String tableName = table.getName();

       
       int recordLen = getRecordLength(event);

       QueryEngine engine = QueryEngine.getInstance() ;
       con= engine.getConnection();

       //Tools.getInt(event.getParameterValue("id"),-1);
       int objectId =event.getObjectId(table, usr.adClientId, con);
       if(objectId==-1) throw new NDSException("@object-not-found@");
       //event.setParameter("id", String.valueOf(objectId));
       
       int[] oids= new int[]{objectId};

       //check table records exist and modifiable
 	   helper.checkTableRowsModifiable(table, oids, con);
       
       ObjectModifyImpl modifyImpl = new ObjectModifyImpl(event,table,recordLen);

       // call proc before modify
       //helper.triggerBeforeModify(sheetTable,fkValue,con );

       ColumnValueImpl colValueImpl = new ColumnValueImpl();
       colValueImpl.setActionType("modify");
       HashMap hashMap = colValueImpl.getColumnHashMap(event,table,modifyImpl.getModifiableColumns(),recordLen, con);

       ArrayList sqlData = modifyImpl.getSQLData(hashMap, objectId);
       

       int realCount= sqlData.size();

       //##################### added by yfzhu for dispatching to shop
   	   // following dreprecated 2008-05-23 since no mysql in pos anymore
       /*if ( table.getDispatchType() != table.DISPATCH_NONE &&
                table.isActionEnabled(Table.SUBMIT)==false && table.isActionEnabled(Table.AUDIT)==false){
           MySQLObjectModifyImpl myc= new MySQLObjectModifyImpl();
           sqlVector.addAll(myc.getSqlArray(hashMap,event,table,recordLen));
       }*/
       
       String sql= modifyImpl.getPreparedStatementSQL();
       logger.debug(sql);
       int count = doUpdate(sqlData, con, sql,modifyImpl.getColumnTypes());
       
       boolean createAttributeDetail=false;//table.supportAttributeDetail();
       /**
        * There are two class types for jsonobj 
        * 1) org.json.JSONArray, even when jsonobj = []
        * 2) org.json.JSONObject$Null, when jsonobj=null
        * 
        * first case occurs when ui gets input from itemdetail.jsp (click ok on that page)
        * second case occurs when user does not update using itemdetail.jsp
        * 
        * Do not update attribute detail table for second case 
        */
       Object jsonobj= event.getParameterValue("jsonobj");
       //logger.debug("jsonobj="+jsonobj);
       //if (jsonobj!=null) logger.debug("json type:"+ jsonobj.getClass());
       BigDecimal[] pdtIds=null;
       int pdtId=-1;
       if(createAttributeDetail){
           if(jsonobj!=null){
	            Vector pdts =(Vector) hashMap.get("M_PRODUCT_ID");
	            if(pdts!=null)pdtIds = (BigDecimal[])pdts.elementAt(0);
	            
	            createAttributeDetail = pdtIds!=null;
	            logger.debug("pdtIds found "+createAttributeDetail );
           }else createAttributeDetail=false;
       }
       logger.debug("createAttributeDetail="+ createAttributeDetail);
       // check if attribute detail support table, since 3.0 at 2007-05-30
       if(createAttributeDetail){
       		//jsonobj will be stored into m_attributedetail table
       		if(pdtIds[0]==null || jsonobj.equals(JSONObject.NULL)  ){
       			logger.debug(" not found product id or jsonobj not updated");
       		}else{
       			try{
       				helper.createAttributeDetailRecordsByJSON(table, jsonobj, event, objectId, con);
       				jsonObjectCreated=true;
       			}catch(Throwable t){
       				logger.error("fail to do attribute creation", t);
       				throw new NDSEventException("@exception@",t);
       			}
       		}
       }       
       
       //doNotification(event, table, hashMap,con);
       // call proc after modify
       // after modify, first doing triggers on the current table
       // then do trigger on parent table, if exists.
       SPResult spr=helper.doTrigger("AM", table, oids, con);

       Configurations conf= (Configurations)WebUtils.getServletContextManager().getActor( nds.util.WebKeys.CONFIGURATIONS);

       //用户修改的数据，在修改后他是否必须是仍然有权限修改的
       boolean after_modify_check ="true".equals(conf.getProperty("object.modify.after_modify_check", "true")); 
       
       if(!isRoot && after_modify_check && table.isMenuObject()){
		   if( !nds.control.util.SecurityUtils.hasObjectPermission(userId, usr.name, 
				   table.getName(), objectId, nds.security.Directory.WRITE, event.getQuerySession())){
			   logger.debug("no permission to modify a record to uneditable one on table="+ table+", id="+ objectId+" by "+ usr.name+" of id"+ usr.id);
			   throw new NDSEventException("@no-permission@");
		   }
       }
       
       Table parent= helper.getParentTable( table,event);
 	   int[] poids= helper.getParentTablePKIDs(table,oids, con);
       logger.debug("parent of "+ table+ ":"+ parent+", poids="+ ( poids!=null? Tools.toString(poids): "null" ));
 	   //helper.checkTableRows(parent, poids, con, helper.PARENT_NOT_FOUND);
       //check parent table records exist and modifiable
 	   helper.checkTableRowsModifiable(parent, poids, con);

 	   
       helper.doTrigger("AM", parent, poids, con);

       ValueHolder v = new ValueHolder();
      String message  ="@total-records-updated@: "+ realCount ;
       v.put("message",message) ;
       v.put("jsonObjectCreated", new Boolean(jsonObjectCreated));
       v.put("spresult", spr);
       
       logger.info("modified table="+ table+", id="+objectId+" by "+ usr.name+" of id"+ usr.id);
       
       return v;
       }finally{
           try{if(con !=null) con.close(); }catch(Exception eee){}
       }
   }

   private int getRecordLength(DefaultWebEvent event){
       String[] itemIdStr = event.getParameterValues("itemid");
       if(itemIdStr!=null){
           return itemIdStr.length ;
       }else{
           return 1;
       }

   }
   /**
    * Update database according the SQL string in <code>vect</code>
    * @param columnTypes elements are Column.NUMBER, STRING, DATENUMBER, DATE
    * @param sqlData elements are List, contains data for each column  
    */
    public int doUpdate(List sqlData, Connection con, String sql, int[] columnTypes ) throws UpdateException{
        PreparedStatement pstmt=null;
        int count = 0; // start from 1 for user's convenience
        int line=1;
        try{
        	pstmt = con.prepareStatement(sql);
        	for(int i=0;i< sqlData.size();i++){
                count +=executeUpdate(pstmt, (ArrayList)sqlData.get(i),columnTypes );
                line++;
            }
        }catch(Exception e){
            logger.error(sql, e) ;
            throw new UpdateException("@error-at-line@ "+ line+":"+e.getLocalizedMessage());
        }finally{
            try{pstmt.close();}catch(Exception ea){}
        }
        return count;
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
	   Object v=null;
	   int i=0;
	   try{
	  
 	  for(i=0;i< columnTypes.length;i++){
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
		  //logger.debug((i+1)+":"+v );
 	  }
 	  int cnt= pstmt.executeUpdate();
 	  logger.debug("updated "+ cnt+ " records");
 	  return cnt;
 	  }catch(java.lang.ClassCastException e){
 		  logger.error(v+" is wrong type:"+ ( v==null?"null": v.getClass().getName()) +", position:"+ i,e);
 		  throw e;
 	  }
   }
   private void doNotification(DefaultWebEvent event, Table table, HashMap hashMap,Connection con)
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
            briefMsg=new StringBuffer();detailMsg=new StringBuffer();
            if( aks !=null) briefMsg.append(table.getDescription(Locale.CHINA) + "("+ aks[i] + ") 被"+ creatorDesc+ "修改.");
            else briefMsg.append(table.getDescription(Locale.CHINA) + "(id=" + objectId + ") 被"+ creatorDesc+ "修改.");
            String webroot=nm.getProperty("weburl", "http://mit:8001/nds");
            detailMsg.append(briefMsg );
            detailMsg.append("\n\r");
            detailMsg.append("请访问网页"+ webroot+ "/objext/sheet_title.jsp?table="+tableId+"&id="+ objectId);
            nm.handleObject(tableId, objectId, "create", briefMsg, detailMsg,con);
        }

  }
}