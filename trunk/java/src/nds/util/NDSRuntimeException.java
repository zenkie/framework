/******************************************************************
*
*$RCSfile: NDSRuntimeException.java,v $ $Revision: 1.2 $ $Author: Administrator $ $Date: 2006/01/31 03:01:58 $
*
*$Log: NDSRuntimeException.java,v $
*Revision 1.2  2006/01/31 03:01:58  Administrator
*no message
*
*Revision 1.1.1.1  2005/03/15 11:23:26  Administrator
*init
*
*Revision 1.1.1.1  2002/11/29 02:50:35  yfzhu
*Active POS
*
*Revision 1.2  2001/11/07 20:58:47  yfzhu
*no message
*
*
********************************************************************/

package nds.util;

/**
 * Allow for nested exception
 */
public class NDSRuntimeException extends RuntimeException
{

	public NDSRuntimeException()
    {
        
    }

    public NDSRuntimeException(String s)
    {
        super(s);
    }

    public NDSRuntimeException(String s, Throwable exception)
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
     * 当NDSRuntimeException 由服务器EJB端发出，传递到web端时，weblogic会插入一段代码表明stack trace,
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
