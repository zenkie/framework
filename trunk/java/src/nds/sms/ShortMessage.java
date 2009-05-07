//Source file: F:\\work\\sms\\src\\nds\\sms\\ShortMessage.java

package nds.sms;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Properties;

import nds.connection.Message;
import nds.log.Logger;
import nds.log.LoggerManager;
import nds.util.StringBufferWriter;
import nds.util.xml.XmlMapper;

/**
 * SMS message
 */
public class ShortMessage implements Message
{
    private static Logger logger= LoggerManager.getInstance().getLogger(ShortMessage.class.getName());

    private final static DateFormat dateTimeFormatter =new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static XmlMapper xh; // xml mapper

    private Properties props;

    public ShortMessage()
    {
        props= new Properties();
    }

    /**
     * Parse xml string to Message
     * @param str message body
     * @param sender the sender that will override which specified in <code>xml</code>
     *               If null, will be ignored
     * @return ShortMessage
     * @roseuid 4047F63201E8
     */
    public static ShortMessage parse(String xml, String sender) throws Exception
    {
        byte[] bs=xml.getBytes();
        ByteArrayInputStream bis=new ByteArrayInputStream(bs );
        ShortMessage sm= new ShortMessage();
        sm.loadMapping(bis);
        if(sender !=null)sm.setSender(sender);
        return sm;
    }
    private void loadMapping(InputStream stream) throws Exception {
        String dtdURL = "file:" ;
        if( xh==null){
            xh=new XmlMapper();
            xh.setValidating(true);
            // By using dtdURL you brake most buildrs ( at least xerces )
            xh.register("-//Sun Microsystems, Inc.//DTD Web Application 2.2//EN",
                        dtdURL );
            xh.addRule("sms/prop", xh.methodSetter("setProperty",2) );
            xh.addRule("sms/prop/name", xh.methodParam(0) );
            xh.addRule("sms/prop/value", xh.methodParam(1) );

        }
        xh.readXml(stream, this);
    }

    public static ShortMessage parse(String str) throws Exception
    {
        return parse(str, null);
    }

    /**
     * @return String
     * @roseuid 404A98F203B6
     */
    public String getReceiver()
    {
        return props.getProperty("receiver");
    }

    /**
     * @param s
     * @roseuid 404A98F30000
     */
    public void setReceiver(String s)
    {
        setProperty("receiver", s);
    }
    public void setDuration(int hours){
        setProperty("duration", hours+"");
    }
    /**
     * @return -1 if not specified
     */
    public int getDuration(){
        return nds.util.Tools.getInt(props.getProperty("duration"), -1);
    }
    /**
     * @return String
     * @roseuid 404A98F30077
     */
    public String getContent()
    {
        return props.getProperty("content");
    }

    /**
     * @param c
     * @roseuid 404A98F300A9
     */
    public void setContent(String c)
    {
        setProperty("content", c);
    }

    /**
     * @return String
     * @roseuid 404A98F3012B
     */
    public String getMsgID()
    {
        return props.getProperty("msgid");
    }

    /**
     * @return Date
     * @roseuid 404A98F3015D
     */
    public Date getCreationDate()
    {
        Object o=props.get("creationdate");
        if ( o instanceof Date){
            return (Date)o;
        }
        try{
            return dateTimeFormatter.parse(""+o);
        }catch(Exception e){
            logger.error("Could not parse "+ o + " as Date", e);
            return null;
        }
    }

    /**
     * @param d
     * @roseuid 404A98F3018F
     */
    public void setCreationDate(Date d)
    {
        props.setProperty("creationdate", dateTimeFormatter.format(d));
    }

    /**
     * @return String
     * @roseuid 404A98F3021B
     */
    public String getSender()
    {
        return props.getProperty("sender");
    }

    /**
     * @param s
     * @roseuid 404A98F3024D
     */
    public void setSender(String s)
    {
        setProperty("sender", s);
    }

    /**
     * @return String
     * @roseuid 404A98F302E3
     */
    public String toXML(){
        StringBuffer buf=new StringBuffer();
        StringBufferWriter b= new StringBufferWriter(buf);
        b.println("<sms>");
        b.pushIndent();
        Enumeration enu=props.keys() ;
        while(enu.hasMoreElements()){
            b.println("<prop>");
            b.pushIndent();
            String n= (String) enu.nextElement();
            printNode(b, "name", n);
            printNode(b,"value", props.getProperty(n));
            b.popIndent();
            b.println("</prop>");
        }
        b.popIndent();
        b.println("</sms>");
        return buf.toString();
    }
    private void printNode(StringBufferWriter b, String name, Object value){
        b.println("<"+name+">"+ value+"</"+name+">");
    }
    public void setMsgID(String msgId)
    {
        props.setProperty("msgid",msgId);
    }

    /**
     * @return String
     * @roseuid 404A98F3031F
     */
    public String getParentMessageID()
    {
        return props.getProperty("parent");
    }

    /**
     * @roseuid 404A98F3035B
     */
    public String getType()
    {
        return props.getProperty("msgtype");
    }

    /**
     * @return int
     * @roseuid 404A98F3038D
     */
    public void setStatus(int s)
    {
        String st= "unknown";
        switch(s){
            case Message.INITIAL: st= "initial";break;
            case Message.PROCESSING: st="processing";break;
            case Message.RECIEVED: st="recieved";break;
            case Message.SENT:st= "sent";break;
            case Message.DISCARD: st="discard";break;
        }
        setProperty("status", st);
    }

    /**
     * @return int
     * @roseuid 404A98F303C9
     */
    public int getStatus()
    {
        String s= props.getProperty("status");
        int i;
        if ("initial".equals(s)) i= Message.INITIAL;
        else if("processing".equals(s)) i= Message.PROCESSING;
        else if("recieved".equals(s)) i=Message.RECIEVED;
        else if("sent".equals(s)) i=Message.SENT;
        else if("discard".equals(s)) i=Message.DISCARD;
        else i= -1;
        return i;
    }
    public void setProperty(String name , String value){
        props.put(name, value);
    }
    public String getProperty(String name, String defaultValue){
        return props.getProperty(name,defaultValue);
    }
    public Object getProperty(String name){
        return props.get(name);
    }
    public String toString(){
        return "Status:"+ getStatus()+",To:"+ getReceiver()+",From:"+ getSender()+",CreationDate:"+ getCreationDate();
    }
    public static String[] getProperyNames(){
        return SMDBUtils.getSMProperyNames();
    }
}