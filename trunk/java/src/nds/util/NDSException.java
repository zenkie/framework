/******************************************************************
*
*$RCSfile: NDSException.java,v $ $Revision: 1.2 $ $Author: Administrator $ $Date: 2006/01/31 03:01:58 $
*
********************************************************************/

package nds.util;

/**
 * Allow for nested exception
 */
public class NDSException extends Exception
{
    public NDSException()
    {
        
    }

    public NDSException(String s)
    {
        super(s);
    }

    public NDSException(String s, Throwable exception)
    {
        super(s, exception);
    }

    public Throwable getNextException()
    {
        return  this.getCause();
    }

    public synchronized boolean setNextException(Throwable exception)
    {
       this.initCause(exception);
       return true;
    }
    /**
     * 当NDSException 由服务器EJB端发出，传递到web端时，weblogic会插入一段代码表明stack trace,
     * 这时可以使用本方法获得原始的简单message,而带stackTrace的message 可以通过getMessage()获得。
     */
    public String getSimpleMessage(){
        Throwable e = this;
        String s=this.getMessage();
        while (e != null) {
          if(Validator.isNotNull(e.getMessage())) s= e.getMessage();
          Throwable prev = e;
          e = e.getCause();
          if (e == prev)
            break;
        }
        return s;
    }

    
}
