//Source file: F:\\work\\sms\\src\\nds\\connection\\MessageBundle.java

package nds.connection;

import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;

import nds.util.StringBufferWriter;

/*
 Wrapper multiple messages into a package
*/
public class MessageBundle implements java.io.Serializable
{
   private Vector msgs; // elements are Message
   public final static MessageBundle EMPTY=new MessageBundle();
   /**
    * @roseuid 404A97EB0151
    */
   public MessageBundle()
   {
       msgs=new Vector();
   }
   /*
   * @param msgs elements are Messages
   */
   public MessageBundle(Collection msgs)
   {
       msgs.addAll(msgs);
   }

   /**
    * @param msg
    * @roseuid 4047F5680129
    */
   public void addMessage(Message msg)
   {
       msgs.addElement(msg);
   }

   /**
    * @return Iterator
    * @roseuid 4047F58000E7
    */
   public Iterator iterator()
   {
       return msgs.iterator();
   }

   /**
    * @return int
    * @roseuid 4047F59703B1
    */
   public int size()
   {
    return msgs.size();
   }

   /**
    * @return String
    * @roseuid 4047F5B70295
    */
   public String toXML()
   {
       StringBuffer buf=new StringBuffer();
       StringBufferWriter b= new StringBufferWriter(buf);
       b.print("<?xml version=\"1.0\"?>");
       b.println("<messagebundle>");
       b.pushIndent();
       for(Iterator it= msgs.iterator();it.hasNext();)
           b.println(((Message)it.next()).toXML());
       b.popIndent();
       b.println("</messagebundle>");
       return b.toString();
   }

   /**
    * @param str
    * @return MessageBundle
    * @roseuid 4047F5BF00B6
    */
   public static MessageBundle parse(String str)
   {
       return null;
   }
}
