package nds.control.ejb.command;
import java.rmi.RemoteException;
import java.util.ArrayList;

import nds.control.ejb.Command;
import nds.control.event.DefaultWebEvent;
import nds.control.event.NDSEventException;
import nds.control.util.ValueHolder;
import nds.query.QueryEngine;
import nds.query.SPResult;
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

public class CustIncomeShtSubmit extends Command{
    public ValueHolder execute(DefaultWebEvent event) throws NDSException ,RemoteException{
        Integer pid = new Integer(Tools.getInt(event.getParameterValue("id",true) ,-1));
        logger.debug("the vlaue of pid is:"+pid.intValue() ) ;
        logger.debug(event.toDetailString() ) ;
        String spName = "TonyTest";
        ArrayList list = new ArrayList();
        ValueHolder v = new ValueHolder();
        try{
            list.add(pid);
            QueryEngine engine = QueryEngine.getInstance() ;
            SPResult result = engine.executeStoredProcedure(spName,list,true);
            if(result.isSuccessful() ){
                v.put("message","@execute-successfully@") ;
            }else{
                v.put("message","执行出现异常：存储过程为"+spName) ;
            }
            return v;
        }catch(Exception e){
            throw new NDSEventException("出现异常!");
        }
    }
}