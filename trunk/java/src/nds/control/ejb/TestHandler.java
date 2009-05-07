/******************************************************************
*
*$RCSfile: TestHandler.java,v $ $Revision: 1.1.1.1 $ $Author: Administrator $ $Date: 2005/03/15 11:23:14 $
*
*$Log: TestHandler.java,v $
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

import java.util.Iterator;

import nds.control.event.DefaultWebEvent;
import nds.control.event.NDSEvent;
import nds.control.event.NDSEventException;
import nds.control.util.ValueHolder;
import nds.log.Logger;
import nds.log.LoggerManager;

public class TestHandler extends StateHandlerSupport{
    private Logger logger= LoggerManager.getInstance().getLogger(TestHandler.class.getName());
    public ValueHolder perform(NDSEvent e) throws NDSEventException{
        DefaultWebEvent event= (DefaultWebEvent) e;
        Iterator it=event.getParameterNames();
        String log="Handle TestEvent by TestHandler:\n";
        ValueHolder vd= new ValueHolder();
        while(it.hasNext()){
            String n= (String) it.next();
            Object v= event.getParameterValue(n);
            log += "name="+n+", value="+ v;
            vd.put(n ,v);
        }
        logger.debug(log);
        return  vd;
 }

}