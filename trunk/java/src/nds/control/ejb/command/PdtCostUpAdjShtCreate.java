package nds.control.ejb.command;

import java.rmi.RemoteException;
import java.util.ArrayList;

import nds.control.ejb.Command;
import nds.control.event.DefaultWebEvent;
import nds.control.event.NDSEventException;
import nds.control.util.ValueHolder;
import nds.query.SPResult;
import nds.security.User;
import nds.util.NDSException;
import nds.util.Tools;

/**
 * Title:        NDS Project
 * Description:  San gao shui yuan, mu xiang ren jia
 * Copyright:    Copyright (c) 2001
 * Company:      act
 * @author yfzhu
 * @version 1.0
 */

public class PdtCostUpAdjShtCreate extends Command {
  public ValueHolder execute(DefaultWebEvent event) throws RemoteException, NDSException {
    try{
    logger.debug("PdtCostUpAdjShtCreate" + event.toDetailString());

    /* -- Check directory permission on creation -- */
    User commander = helper.getOperator(event);
    helper.checkDirectoryWritePermission(event, commander);

    String sql= (String)event.getParameterValue("sql");
    int adjRate=Tools.getInt( event.getParameterValue("adjRate"), -1);
    if( adjRate == -1) throw new NDSEventException("成本单价调整比例未设置，或不是数字型.");
    Tools.checkIsValid(event.getParameterValue("adjRate").toString(), true);
    int operId= commander.getId().intValue();
    ArrayList params=new ArrayList();
    params.add(sql);
    params.add(new Integer(adjRate));
    params.add(new Integer(operId));

    SPResult res= helper.executeStoredProcedure("PDTCOSTUPADJSHTCREATE", params, true);
        ValueHolder v=new ValueHolder();
        if( res.isSuccessful()){
            v.put("message", "产品成本单价调整单创建成功");
        }else{
            logger.debug(res.toString());
            throw new NDSEventException(res.getDebugMessage());
        }

      return v;
    }catch(Exception e){
      logger.error("", e);
      if(!(e instanceof NDSException ))throw new NDSEventException("Error found.", e);
      else throw (NDSException)e;
    }
  }
}