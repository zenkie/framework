package nds.control.ejb.command;

import java.rmi.RemoteException;
import java.util.ArrayList;

import nds.control.ejb.Command;
import nds.control.event.DefaultWebEvent;
import nds.control.event.NDSEventException;
import nds.control.util.ValueHolder;
import nds.query.SPResult;
import nds.util.NDSException;

/**
 * Title:        NDS Project
 * Description:  San gao shui yuan, mu xiang ren jia
 * Copyright:    Copyright (c) 2001
 * Company:      aic
 * @author yfzhu
 * @version 1.0
 * @deprecated
 */

public class SaveReqParam extends Command {
    /**
     * Find bugs, since control has only name without table identity, different controls with
     * sam in name in different tables will share the same value.
     * Solution: we will add table name before control name. Take example, suppose
     * control named "id" in table "users", then the control name stored will be "USERS.ID"
     */
  public ValueHolder execute(DefaultWebEvent event) throws RemoteException, NDSException {
    try{
        logger.debug("Save Request Parameters");
        //logger.debug("begin SaveReqParam" + event.toDetailString());
        String tableName= (String)event.getParameterValue("table");// may be null in some cases

        String[] needSaved = event.getRawParameterValues("mustBeSaved");
        if(needSaved == null) return null;
        StringBuffer param = new StringBuffer("");
        //Iterator it = event.getParameterNames();
        for(int j = 0;j < needSaved.length;j++)/*while(it.hasNext())*/{
            //String name = (String)it.next();
            String name =tableName+"."+ needSaved[j];
            //String name = needSaved[j];
            String[] value = event.getRawParameterValues(needSaved[j]);
            //logger.debug(name.toString() + ":" + value == null?"NULL":value.toString());
            if(value == null);
            else if(value.length == 1){
                if(!(value[0] == null || value[0] ==""))
                    param.append(name.toUpperCase() + "," + value[0] + ",");
            }else{
                for(int i = 0;i < value.length;i++)
                    if(!(value[i] == null || value[i] ==""))
                        param.append(name.toUpperCase() + "," + value[i] + ",");
            }
        }
        //logger.debug("param:" + param.toString());
        if(param.toString().equals(""))
            return null;
        ArrayList params=new ArrayList();
        params.add(param.toString());
        params.add(helper.getOperator(event).getName());

        SPResult res = null;
        res = helper.executeStoredProcedure("sp_saveReqParam", params, true);

        ValueHolder v=new ValueHolder();
        if( res.isSuccessful()){
            v.put("message", res.getMessage());
        }else{
            logger.debug(res.toString());
            throw new NDSEventException(res.getDebugMessage());
        }
        //logger.debug("end SaveReqParam");

      return v;
    }catch(Exception e){
      logger.error("", e);
      if(!(e instanceof NDSException ))throw new NDSEventException("Òì³£", e);
      else throw (NDSException)e;
    }
  }
}