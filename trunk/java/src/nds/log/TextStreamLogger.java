/******************************************************************
*
*$RCSfile: TextStreamLogger.java,v $ $Revision: 1.1.1.1 $ $Author: Administrator $ $Date: 2005/03/15 11:23:18 $
*
*$Log: TextStreamLogger.java,v $
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

import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

import nds.util.StringUtils;

public abstract class TextStreamLogger implements LogConstants

{

    public TextStreamLogger()
    {
        dformat = DateFormat.getDateTimeInstance(2, 1, myLocale);
    }

    public void close()
    {
        out.close();
    }

    public void flush()
    {
        out.flush();
    }

    protected final String formatDate(Date date)
    {
        return dformat.format(date);
    }

    protected StringBuffer formatMessage(int i/*level*/, String s/*subsystem*/, long l/*time*/, String s1/*message*/)
    {
        String s2 = formatDate(new Date(l));
        StringBuffer stringbuffer = new StringBuffer(FIELD_PREFIX + s2 + FIELD_SUFFIX);
        stringbuffer.append(FIELD_PREFIX);
        stringbuffer.append(getHeader(i));
        stringbuffer.append(FIELD_SUFFIX);
        stringbuffer.append(FIELD_PREFIX);
        stringbuffer.append(s);
        stringbuffer.append(FIELD_SUFFIX);
        stringbuffer.append(FIELD_PREFIX);
        stringbuffer.append(s1);
        stringbuffer.append(FIELD_SUFFIX);
        return stringbuffer;
    }

    protected String getHeader(int i)
    {
		String header=null;
		switch(i){
			case EMERGENCY: header="Emergency";break;
			case ALERT: header="Alert";break;
			case CRITICAL:header="Critical";break;
			case NOTICE:header="Notice";break;
			case ERROR: header="Error";break;
			case WARNING:header="Warning";break;
			case INFO: header="Info";break;
			case DEBUG: header="Debug";break;
			default:
				header="Unknown";
		}
		return header;
    }
    public void log(int i, String s, long l, String s1)
        throws IOException
    {
        out.println(formatMessage(i, s, l, s1));
    }

    public void log(int i, String s, long l, String s1, Throwable throwable)
        throws IOException
    {
        log(i, s, l, s1 + EOL + StringUtils.toString(throwable));
    }

    public void log(String s, int i, String s1, Date date, Object aobj[], String s2, Throwable athrowable[])
        throws IOException
    {
        if(athrowable.length == 0)
            log(i, s1, date.getTime(), s2);
        else
            log(i, s1, date.getTime(), s2, athrowable[0]);
    }

    public abstract void open()
        throws IOException;

    public static String FIELD_PREFIX = "<";
    public static String FIELD_SUFFIX = "> ";
    protected PrintWriter out;
    private final DateFormat dformat;
    public static final Locale myLocale = Locale.getDefault();
	public static final String EOL = System.getProperty("line.separator");
}
