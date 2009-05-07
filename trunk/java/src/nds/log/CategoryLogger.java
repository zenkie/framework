/******************************************************************
*
*$RCSfile: CategoryLogger.java,v $ $Revision: 1.1.1.1 $ $Author: Administrator $ $Date: 2005/03/15 11:23:18 $
*
*$Log: CategoryLogger.java,v $
*Revision 1.1.1.1  2005/03/15 11:23:18  Administrator
*init
*
*Revision 1.1.1.1  2002/11/29 02:50:34  yfzhu
*Active POS
*
*Revision 1.3  2001/11/13 22:37:14  yfzhu
*no message
*
*Revision 1.2  2001/11/07 20:58:47  yfzhu
*no message
*
*
********************************************************************/

package nds.log;
import org.apache.log4j.Category;

public class CategoryLogger implements Logger
{
    private transient Category logStream;
    private Logger alternateLogger=null;
    private String category;
    public CategoryLogger(String category)
    {
        this.logStream=Category.getInstance(category);
        this.category=category;
    }
    public void setAdditionalLogger(Logger logger){
        //this.alternateLogger=logger;
    }
    public void init(String system, String fileName, String level){
    }

    public void debug(String s)
    {
        if(logStream==null) logStream=Category.getInstance(category);
        logStream.debug(s);
        if( alternateLogger !=null){
            alternateLogger.debug(s);
        }
    }
    public void debug(String s, Throwable throwable)
    {
        if(logStream==null) logStream=Category.getInstance(category);
        logStream.debug(s, throwable);
        if( alternateLogger !=null){
            alternateLogger.debug(s, throwable);
        }
    }

    public void error(String s)
    {
        if(logStream==null) logStream=Category.getInstance(category);
        logStream.error(s);
        if( alternateLogger !=null){
            alternateLogger.error(s);
        }
    }

    public void error(String s, Throwable throwable)
    {
        if(logStream==null) logStream=Category.getInstance(category);
        logStream.error(s, throwable);
        if( alternateLogger !=null){
            alternateLogger.error(s, throwable);
        }
    }

    public void info(String s)
    {
        if(logStream==null) logStream=Category.getInstance(category);
        logStream.info(s);
        if( alternateLogger !=null){
            alternateLogger.info(s);
        }
    }

    public void info(String s, Throwable throwable)
    {
        if(logStream==null) logStream=Category.getInstance(category);
        logStream.info(s, throwable);
        if( alternateLogger !=null){
            alternateLogger.info(s, throwable);
        }
    }

    public void warning(String s)
    {
        if(logStream==null) logStream=Category.getInstance(category);
        logStream.warn(s);
        if( alternateLogger !=null){
            alternateLogger.warning(s);
        }
    }

    public void warning(String s, Throwable throwable)
    {
        if(logStream==null) logStream=Category.getInstance(category);
        logStream.warn(s, throwable);
        if( alternateLogger !=null){
            alternateLogger.warning(s, throwable);
        }
    }
    public void fatal(String s){
        if(logStream==null) logStream=Category.getInstance(category);
        logStream.fatal(s);
    }
    public void fatal(String s, Throwable throwable){
        if(logStream==null) logStream=Category.getInstance(category);
        logStream.fatal(s,throwable);
    }

}
