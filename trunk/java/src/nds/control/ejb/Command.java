/******************************************************************
*
*$RCSfile: Command.java,v $ $Revision: 1.2 $ $Author: Administrator $ $Date: 2005/03/16 09:04:49 $
*
*$Log: Command.java,v $
*Revision 1.2  2005/03/16 09:04:49  Administrator
*no message
*
*Revision 1.1.1.1  2005/03/15 11:23:14  Administrator
*init
*
*Revision 1.2  2004/02/02 10:42:58  yfzhu
*<No Comment Entered>
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

import java.rmi.RemoteException;

import nds.control.event.DefaultWebEvent;
import nds.control.util.ValueHolder;
import nds.log.Logger;
import nds.log.LoggerManager;
import nds.util.NDSException;
/**
 * Wrapper the execution of specified operation. Detailed execution process should be implemented
 * by subclasses, which normally should be positioned in package nds.control.ejb.command.
 *
 * This class must be thread-safe, since it's cached in CommandFactory, and will be called by mutiple
 * thread. It's strongly commanded that subclasses <font color=red>must not</font> use class variable.
 *
 * 声明：出于多线程的安全考虑， 子类中禁止声明变量存放处理的中间结果。
 *
 * @see CommandFactory#getCommand
 */
public abstract class Command{
    protected Logger logger;
    protected DefaultWebEventHelper helper;
    
    /**
     * So CommandFactory can new an instance
     */
    public Command(){
        logger=LoggerManager.getInstance().getLogger(getClass().getName());
        helper= new DefaultWebEventHelper();
    }
    public void setStateMachine(StateMachine machine){
    	helper.setStateMachine(machine);
    }
    /**
     * Whether this command use internal transaction control. For normal command, transaction is controled by
     * caller, yet for some special ones, the command will control transaction seperativly, that is, the command
     * will new transaction and commit that one explicitly
     * @return false if use transaction from caller
     */
    public boolean internalTransaction(){
    	return false;
    }
    /**
     * The main method of this class, execute special command
     */
    public abstract ValueHolder execute(DefaultWebEvent event) throws NDSException, RemoteException;

}