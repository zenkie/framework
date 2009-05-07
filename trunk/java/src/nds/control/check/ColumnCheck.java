package nds.control.check;
import java.rmi.RemoteException;

import nds.log.Logger;
import nds.log.LoggerManager;
import nds.schema.Column;
import nds.util.NDSException;
/**
 * Title:        NDS Project
 * Description:  San gao shui yuan, mu xiang ren jia
 * Copyright:    Copyright (c) 2001
 * Company:      aic
 * @author yfzhu
 * @version 1.0
 */

public  abstract class ColumnCheck {
    protected Logger logger;
    public ColumnCheck() {
        logger = LoggerManager.getInstance().getLogger(getClass().getName());
    }
    public abstract void isColumnValid(Column col,String objStr) throws NDSException,RemoteException;
}