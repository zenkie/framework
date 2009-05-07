/******************************************************************
*
*$RCSfile: Logger.java,v $ $Revision: 1.1.1.1 $ $Author: Administrator $ $Date: 2005/03/15 11:23:18 $
*
*$Log: Logger.java,v $
*Revision 1.1.1.1  2005/03/15 11:23:18  Administrator
*init
*
*Revision 1.1.1.1  2002/11/29 02:50:34  yfzhu
*Active POS
*
*Revision 1.2  2001/11/07 20:58:47  yfzhu
*no message
*
*
********************************************************************/

package nds.log;


public interface Logger extends java.io.Serializable
{
    /**
     * This method must be called before normal log operation
     * @param system the system's name
     * @param fileName the log file
     * @param level an integer string, normally it should be one of LogConstants
     */
    public void init(String system, String fileName, String level);
    public void debug(String s);
    public void debug(String s, Throwable throwable);
    public void error(String s);

    public void error(String s, Throwable throwable);

    public void info(String s);

    public void info(String s, Throwable throwable);

    public void warning(String s);

    public void warning(String s, Throwable throwable);
}
