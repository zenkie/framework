package nds.control.ejb.command;
import java.math.BigDecimal;
import java.rmi.RemoteException;
import java.util.Vector;

import nds.control.event.DefaultWebEvent;
import nds.control.event.NDSEventException;
import nds.schema.Column;
import nds.schema.Table;
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

public class FKColumnObtain extends ColumnObtain{
//    public int length;


    public FKColumnObtain() {
//        this.length  = length;
    }
    public Vector getColumnValue(DefaultWebEvent event,Table table,Column col,int length) throws NDSException,RemoteException{
//        int length = this.getLength() ;
        int fkId = Tools.getInt(event.getParameterValue("objectid"),-1 ) ;
        if(fkId == -1){
            throw new  NDSEventException("Õ‚º¸Œ¥’“µΩ");
        }
        Vector vec = new Vector();
        BigDecimal[] fkResult = new BigDecimal[length];
        for(int i= 0;i<length;i++){
            fkResult[i] = new BigDecimal(fkId);
        }
        vec.add(fkResult) ;
        return vec;
    }
/*    public int getLength(){
        return length;
    }
    */
}