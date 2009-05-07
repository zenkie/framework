/******************************************************************
*
*$RCSfile: URLMappingHolder.java,v $ $Revision: 1.1.1.1 $ $Author: Administrator $ $Date: 2005/03/15 11:23:16 $
*
*$Log: URLMappingHolder.java,v $
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
  /**
  * This class is for generating URLMapping from a xml file
  *
  * By heart, I did not want this class be public. It's only a private class
  * in URLMappingManager. But as for the limitation of XMLMapper, I had to make
  * it like this.
  */
public class URLMappingHolder{

    private String url;
    private String flowHandler = null;
    private String requestHandler = null;
    private String screen;
    private boolean  isSecured= false;

    private String event=null;
    private String nextScreen=null;
    private HashMap flowItems;

    public URLMappingHolder(){
        flowItems=new HashMap();
    }
    public void setEvent(String event){
        this.event=event;
    }
    public void setNextScreen(String screen){
        this.nextScreen=screen;
    }
    public void addFlowItem(String command, String screen){
         flowItems.put(command,screen);
    }
    public void setURL(String url){
        // do some checks here
    	/* @changelog yfzhu 2004-11-17 for nds path in web context*/
        //this.url=nds.util.WebKeys.NDS_URI+url;
    	this.url=url;
    }
    public void setFlowHandler(String flow){
        // do some checks here
        this.flowHandler=flow;
    }
    public void setRequestHandler(String handler){
        // do some checks here
        this.requestHandler=handler;
    }
    public void setScreen(String screen){
        // do some checks here
        this.screen=screen;
    }
    public void setSecured(String b){
        try{
            isSecured=Boolean.valueOf(b).booleanValue();
        }catch(Exception e){
            System.err.println("[URLMappingHolder]Error setting boolean value by String \""+b+"\", and take it as true");
            //as long this method being called, setting value to true by default when error occurs
            isSecured=true;
        }
    }
    public URLMapping toURLMapping(){
        URLMapping map=new URLMapping(url,screen,requestHandler,flowHandler,
                event,nextScreen,flowItems,
                isSecured);
        return map;
    }

}
