package nds.control.ejb.command;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Vector;
import java.math.BigDecimal;
import nds.control.event.DefaultWebEvent;
import nds.control.event.NDSEventException;
import nds.query.QueryEngine;
import nds.schema.Column;
import nds.schema.Table;
import nds.util.NDSException;
/**

 * @author yfzhu
 * @version 1.0
 */

public class PKColumnObtain extends ColumnObtain{
//    int length ;// 要操作的记录数的长度
    public PKColumnObtain() {
//        this.length = length;
    }
    /**
     * @param 
     * @return elements are BigDecimal
     */
    public Vector getColumnValue(DefaultWebEvent event,Table table,Column col,int length) throws NDSException,RemoteException{

//        int length = this.getLength();
        QueryEngine engine = QueryEngine.getInstance();
        ArrayList coll =new ArrayList();
        // to get the table name

        String tableName = table.getName() ;
        coll.add(tableName);
        Vector objVec = new Vector();
        int i=0;
        BigDecimal[] sequenceRes = new BigDecimal[length];
        try{

            for ( i= 0;i<length;i++){
                if( this.isInvalidRow(i)) sequenceRes[i] = new BigDecimal(-1);
                else{
                    int  resultInt = engine.getSequence(tableName,conn);
                    sequenceRes[i] = new BigDecimal(resultInt);
                }
            }
            objVec.add(sequenceRes) ;
        }catch(Exception e){
            if(! this.isBestEffort ){
                throw new NDSEventException("在获取主键序列号时出现异常",e);
            }else{
                // mark all following vectors to null;
                for( int j=i;j< length;j++){
                    sequenceRes[i] = new BigDecimal(-1);
                    this.setRowInvalid(j, "在获取主键序列号时出现异常:"+e);
                }
                objVec.add(sequenceRes) ;
            }
        }
        // execute a sp
         return objVec;
    }
/*    public int getLength(){
        return length;
    }

    */
}