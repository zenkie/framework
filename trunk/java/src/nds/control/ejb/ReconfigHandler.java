/******************************************************************
*
*$RCSfile: ReconfigHandler.java,v $ $Revision: 1.1.1.1 $ $Author: Administrator $ $Date: 2005/03/15 11:23:14 $
*
*$Log: ReconfigHandler.java,v $
*Revision 1.1.1.1  2005/03/15 11:23:14  Administrator
*init
*
*Revision 1.1.1.1  2002/11/29 02:50:34  yfzhu
*Active POS
*
*Revision 1.2  2001/11/07 20:58:46  yfzhu
*no message
*
*
********************************************************************/
package nds.control.ejb;

import nds.control.event.DefaultWebEvent;
import nds.control.event.NDSEvent;
import nds.control.event.NDSEventException;
import nds.control.util.ValueHolder;
import nds.log.Logger;
import nds.log.LoggerManager;

public class ReconfigHandler extends StateHandlerSupport{
 private static Logger logger= LoggerManager.getInstance().getLogger(ReconfigHandler.class.getName());

 public ValueHolder perform(NDSEvent e) throws NDSEventException{
        DefaultWebEvent event= (DefaultWebEvent) e;
        ValueHolder vd= new ValueHolder();
        vd.put("message", "应用重新配置完成");
        logger.info("Successfully reconfigured.");
        return  vd;
 }
}