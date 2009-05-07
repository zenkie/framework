package nds.control.ejb;
import java.util.Hashtable;

import nds.control.ejb.command.ColumnObtain;
import nds.control.event.NDSEventException;
import nds.log.Logger;
import nds.log.LoggerManager;
/**
 * Title:        NDS Project
 * Description:  San gao shui yuan, mu xiang ren jia
 * Copyright:    Copyright (c) 2001
 * Company:      aic
 * @author yfzhu
 * @version 1.0
 */

public final class ColumnCalFactory {
    private static ColumnCalFactory instance=null;
    private static Logger logger= LoggerManager.getInstance().getLogger(ColumnCalFactory.class.getName());
    Hashtable hash;

    private ColumnCalFactory() {
        hash = new Hashtable();
    }
     public ColumnObtain getColumnObtain(String name) throws NDSEventException {
        ColumnObtain columnObtain=(ColumnObtain) hash.get(name);

        if( columnObtain == null) {
            try {
                Class c= Class.forName("nds.control.ejb.command."+ name.trim());
                columnObtain=(ColumnObtain) c.newInstance();
                hash.put(name, columnObtain);
                logger.debug("columnObtain :"+ name +" created and ready for handling.");
            } catch (Exception e) {
                throw new NDSEventException("Internal Error: can not find class to handle event with command:"+ name, e);
            }
        }
        return columnObtain;
    }

     public static synchronized ColumnCalFactory getInstance() {
        if(instance==null) {
            instance=new ColumnCalFactory();
        }
        return instance;
    }
}