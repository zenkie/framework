package nds.control.ejb.command;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Vector;

import nds.control.ejb.Command;
import nds.control.ejb.MySQLListModifyImpl;
import nds.control.ejb.ListModifyImpl;
import nds.control.ejb.ObjectModifyImpl;
import nds.control.event.DefaultWebEvent;
import nds.control.event.NDSEventException;
import nds.control.util.ValueHolder;
import nds.mail.NotificationManager;
import nds.query.QueryEngine;
import nds.schema.Column;
import nds.schema.Table;
import nds.schema.TableManager;
import nds.util.NDSException;
import nds.util.Tools;
/**
 * @deprecated since not support binding value sql update at 2010-4
 * @author yfzhu
 */
public class ListModify extends Command{

   public ValueHolder execute(DefaultWebEvent event) throws NDSException ,RemoteException{
	 if(true)throw new NDSException("ListModify is deprecated, not support binding value sql update");   
  	/**
  	 * 2005-11-15 增加了对status 字段的判断，如果status字段为2 则当前记录不允许修改。这种情况主要发生
  	 * 在支持审计的单视图模式下。此模式下所有的单据都在一个界面，对于已审计通过的单据应禁止修改。系统会在通过
  	 * 审计的单据上修改status=2，作为判断条件。
  	 */
   	java.sql.Connection con=null;
       try{
       // 得到所要操作的表的名字
       TableManager manager = helper.getTableManager() ;
       Table table = manager.findTable(event.getParameterValue("table"));
       int tableId = table.getId();
       String tableName = table.getName();
       String tableDesc = table.getDescription(Locale.CHINA) ;

       // 根据 arrayItemSelecter 的存在性判断是list modify 还是 objecct modify
       ArrayList colList = table.getModifiableColumns(
       		(event.getParameterValue("arrayItemSelecter")!=null? nds.schema.Column.QUERY_SUBLIST:Column.MODIFY));
       //yfzhu 2005-04-01 add common columns so will be modified also
       //since 2.0
       ObjectModifyImpl.addCommonModifiableColumns(colList, table);

       int recordLen = getRecordLength(event);

       String[] objectStr = event.getParameterValues("itemid");
       
       if(objectStr==null){
        throw new NDSEventException("@pls-select-items@");
       }
       QueryEngine engine = QueryEngine.getInstance() ;
       con= engine.getConnection();

       int[] ids= new int[objectStr.length];
       for(int i=0;i<ids.length;i++) ids[i]= (Integer.parseInt(objectStr[i]));

       Table parent= helper.getParentTable(table,event);
       //logger.debug(" parent table of " + table + " is " + parent);
       int[] poids= helper.getParentTablePKIDs(table, ids,con);
       //logger.debug(" parent id of " + table + " is " + Tools.toString(poids));
       // 检查父表的存在性，如果不存在或未找到，抛出错误，这种情况发生在以下情况：
       // 父表的状态被改变了，而子表的界面仍然保留在那里，故用户可以对子表进行操作，导致
       // 父表出现错误
       //举例：  m_v_inout 被提交生成了 m_v_2_inout, 而用户仍可以对m_v_inoutitem表
       //中的内容进行修改。这是不允许的。
       helper.checkTableRows(parent, poids, con, helper.PARENT_NOT_FOUND);
       

       // call proc before modify
       //helper.triggerBeforeModify(sheetTable,fkValue,con );

       //createImpl.getSqlArray();
       ColumnValueImpl colValueImpl = new ColumnValueImpl();
       colValueImpl.setActionType("modify");
       HashMap hashMap = colValueImpl.getColumnHashMap(event,table,colList,recordLen,con);

       // 产生sql语句
       ListModifyImpl modifyImpl = new ListModifyImpl();
       Vector sqlVector = modifyImpl.getSqlArray(hashMap,event,table,recordLen);

       int realCount= sqlVector.size();



       int count = engine.doUpdate(sqlVector, con);

       //doNotification(event, table, hashMap,con);



       // call proc after modify
       helper.doTrigger("AM",table ,ids ,con); //after modify
       
       helper.doTrigger("AM", parent, poids, con);
       
       ValueHolder v = new ValueHolder();
      String message  ="@updated-lines@:"+ realCount ;
       v.put("message",message) ;
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

   private void doNotification(DefaultWebEvent event, Table table, HashMap hashMap,Connection con)
   throws NDSException, RemoteException{
       TableManager manager = helper.getTableManager();
       Vector vecObj =(Vector) hashMap.get(table.getPrimaryKey().getName());
       Integer[] objectId = (Integer[])vecObj.get(0);

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
   private String[] getAKData(Table table, Integer[] Ids, Connection con){
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
    private void Notify(Table table, Integer[] objIds, String[] aks,String creatorDesc,Connection con){
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