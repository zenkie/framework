package nds.net;

import java.util.Properties;

import nds.log.Logger;
import nds.log.LoggerManager;

public abstract class ThreadProcess {
    protected Logger logger= LoggerManager.getInstance().getLogger(this.getClass().getName());
  /**
   * any process needed schedule could implements this interface
   * in the function,process execute the busness logic
   */
  public abstract void execute() ;
  public abstract void init(Properties props);

}