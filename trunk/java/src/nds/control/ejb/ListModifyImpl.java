package nds.control.ejb;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import nds.control.ejb.command.pub.Pub;
import nds.control.event.DefaultWebEvent;
import nds.control.event.NDSEventException;
import nds.schema.Column;
import nds.schema.Table;
import nds.util.JNDINames;
import nds.util.NDSException;
import nds.util.Tools;
/**
 * 
 * @author yfzhu
 * @deprecated
 */
public class ListModifyImpl extends SqlGenerateSupport{
  public Vector getSqlArray(HashMap hashColValue,DefaultWebEvent event,Table table,int length) throws NDSException,RemoteException{
  	/**
  	 * 2005-11-15 增加了对status 字段的判断，如果status字段为2 则当前记录不允许修改。这种情况主要发生
  	 * 在支持审计的单视图模式下。此模式下所有的单据都在一个界面，对于已审计通过的单据应禁止修改。系统会在通过
  	 * 审计的单据上修改status=2，作为判断条件。
  	 */
        int objectid = Tools.getInt(event.getParameterValue("id"),-1 ) ;
        String[] itemidStr = event.getParameterValues("itemid");

        int[] itemId = new int[length];
            if(itemidStr.length !=length){
                throw new NDSEventException("the length of the paramter is wrong");
            }
            for(int i = 0;i<itemidStr.length ;i++){
                itemId[i] = Tools.getInt(itemidStr[i],-1) ;
            }
        boolean bCheckStatus=( table.getColumn("status")!=null);    
        String tableName = table.getRealTableName() ;
        Column column;
        String columnName = null;
        ArrayList editColumnList = table.getModifiableColumns(
        		(event.getParameterValue("arrayItemSelecter")!=null? nds.schema.Column.QUERY_SUBLIST:Column.MODIFY));
        //yfzhu 2005-04-01 add common columns so will be modified also
        //since 2.0
        ObjectModifyImpl.addCommonModifiableColumns(editColumnList, table);

        Vector vec = new Vector();
        for(int i = 0;i<length;i++){
            Iterator colIte = editColumnList.iterator() ;
            String sql = "update "+tableName+" set ";
            while(colIte.hasNext() ){
               column = (Column)colIte.next();
               columnName = column.getName();
               Vector value = (Vector)hashColValue.get(columnName) ;
               Object[] valBig = (Object[])value.get(0);
               sql +=  columnName+" =";
               int colType = column.getType();
               switch( colType){
              	case Column.STRING:
                    sql += "'"+Pub.getDoubleQuote((String)valBig[i])+"',";
               		break;
               case Column.NUMBER:
               		sql += valBig[i]+",";
               		break;
               case Column.DATENUMBER:
	            	sql+= valBig[i]+",";
	            	break;
               	case Column.DATE:
                   if(valBig[i]==null)
                       sql += "null,";
                   else
                       sql +="to_date("+"'"+valBig[i]+"','YYYY/MM/DD HH24:MI:SS'),";
               		break;
                default:
                	throw new NDSException("Unexpeced column type:"+ colType+" for column:"+column);
               }
           }
           // 去掉sql语句后面的","号
           sql = Pub.removeLastString(sql,",");
           sql += " where id = "+itemId[i]+ (bCheckStatus?" and status<>"+JNDINames.STATUS_SUBMIT:"");
           debug("the value of sql for the end is:"+sql) ;

           vec.addElement(sql);
       }
       return vec;
  }
  private void debug(String s){
      if(false)	  logger.debug(s);
  }
}