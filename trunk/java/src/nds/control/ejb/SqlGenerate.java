package nds.control.ejb;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Vector;

import nds.control.event.DefaultWebEvent;
import nds.log.Logger;
import nds.log.LoggerManager;
import nds.schema.Table;
import nds.util.NDSException;
/**
 * Title:        NDS Project
 * Description:  San gao shui yuan, mu xiang ren jia
 * Copyright:    Copyright (c) 2001
 * Company:      aic
 * @author yfzhu
 * @version 1.0
 * @drepcated
 */

public abstract class SqlGenerate {
    protected Logger logger;
    public SqlGenerate() {
        logger=LoggerManager.getInstance().getLogger(getClass().getName());
    }

    public abstract Vector getSqlArray(HashMap hashColValue,DefaultWebEvent event,Table table,int length) throws NDSException,RemoteException;


}