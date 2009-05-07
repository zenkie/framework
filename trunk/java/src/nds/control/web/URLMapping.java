/******************************************************************
*
*$RCSfile: URLMapping.java,v $ $Revision: 1.1.1.1 $ $Author: Administrator $ $Date: 2005/03/15 11:23:16 $
*
*$Log: URLMapping.java,v $
*Revision 1.1.1.1  2005/03/15 11:23:16  Administrator
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
package nds.control.web;
import java.util.HashMap;
import java.util.Iterator;

/**
 * URL information
 */
public class URLMapping implements java.io.Serializable{
    private String url;
    private String flowHandler = null;
    private String requestHandler = null;
    private String screen;
    private boolean isSecured   = false;

    private String event=null;
    private String nextScreen=null;
    private HashMap flowItems=null;

    public URLMapping(String url,
                                    String screen,
                                    String requestHandler,
                                    String flowHandler,
                                    String event,
                                    String nextScreen,
                                    HashMap flowItems,
                                    boolean isSecured) {
        this.url = url;
        this.flowHandler = flowHandler;
        this.requestHandler = requestHandler;
        this.isSecured = isSecured;
        this.screen = screen;
        this.nextScreen=nextScreen;
        this.event=event;
        this.flowItems=flowItems;
    }

    public String getNextScreen(){
        return nextScreen;
    }
    public String getEvent(){
        return event;
    }
    public String getNextScreen(String command){
        if(flowItems!=null)
            return (String)flowItems.get(command);
        return null;
    }
    public boolean isSecured(){
        return isSecured;
    }
    public String getRequestHandler() {
        return requestHandler;
    }

    public String getFlowHandler() {
        return flowHandler;
    }

    public String getScreen() {
        return screen;
    }
    /**
     * @return url
     */
    public String getURL(){
        return url;
    }
    public String toString() {
        StringBuffer buf=new StringBuffer();
        String s=
            "<url-mapping>\n"+
            "   <url>"+ url+"</url>\n"+
            "   <screen>"+screen+"</screen>\n"+
            "   <secured>"+isSecured+"</secured>\n";
        buf.append(s);
        if( requestHandler !=null){
            buf.append("   <request-handler>"+ requestHandler+"</request-handler>\n");
        }else if( event !=null){
            buf.append("    <event>"+event+"</event>\n");
        }
        if( flowHandler !=null){
            buf.append("   <flow-handler>"+flowHandler+"</flow-handler>\n");
        }else if( nextScreen !=null){
            buf.append("    <next-screen>"+nextScreen+"</next-screen>\n");
        }else if( flowItems !=null){
            Iterator it=flowItems.keySet().iterator();
            while(it.hasNext()){
            buf.append("    <flow-item>\n");
                String name=(String) it.next();
                String value=(String) flowItems.get(name);
                buf.append("        <screen>"+name+"</screen>\n");
                buf.append("        <command>"+value+"</command>\n");
            buf.append("    </flow-item>\n");
            }
        }
        buf.append("</url-mapping>");

        return buf.toString();
    }
}

