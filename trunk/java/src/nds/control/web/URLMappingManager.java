/******************************************************************
*
*$RCSfile: URLMappingManager.java,v $ $Revision: 1.1.1.1 $ $Author: Administrator $ $Date: 2005/03/15 11:23:16 $
*
*$Log: URLMappingManager.java,v $
*Revision 1.1.1.1  2005/03/15 11:23:16  Administrator
*init
*
*Revision 1.1.1.1  2002/11/29 02:50:34  yfzhu
*Active POS
*
*Revision 1.4  2001/11/20 22:36:09  yfzhu
*no message
*
*Revision 1.3  2001/11/16 11:42:40  yfzhu
*no message
*
*Revision 1.2  2001/11/07 20:58:46  yfzhu
*no message
*
*
********************************************************************/
package nds.control.web;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;

import javax.servlet.ServletContext;

import nds.log.Logger;
import nds.log.LoggerManager;
import nds.util.Director;
import nds.util.ServletContextActor;
import nds.util.Tools;
import nds.util.xml.XmlMapper;
/**
 * URL information loader
 */
public class URLMappingManager implements ServletContextActor,java.io.Serializable {

    private transient static Logger logger= LoggerManager.getInstance().getLogger(URLMappingManager.class.getName());

    private HashMap urlMappings;
    private HashMap screenMappings;

    public URLMappingManager() {}
    public void destroy() {
        if( urlMappings !=null) {
            urlMappings.clear();
            urlMappings=null;
        }
        if( screenMappings !=null) {
            screenMappings.clear();
            screenMappings=null;
        }
        logger.debug("URLMappingManager destroied.");

    }
    public void init(Director director) {
    }
    public void init(String url) {
    	logger.debug("[URLMappingManager] Initializing URLMappingManager using: " + url);
        String requestMappingsURL = url;
        try {
            URL urlm = new URL(requestMappingsURL);
            URLMappingsDAO maps=loadMappings(urlm.openStream());
            urlMappings = maps.getURLMappings();
            screenMappings= maps.getScreenMappings();

        } catch (Exception ex) {
            logger.debug("[URLMappingManager] Initializing URLMappingManager exception: " + ex);
            ex.printStackTrace();
        }
        if(logger==null)
            logger= LoggerManager.getInstance().getLogger(URLMappingManager.class.getName());

    }

    public void init(ServletContext context) {
        String requestMappingsURL = null;
        try {
            requestMappingsURL = context.getResource("/WEB-INF/xml/urlmappings.xml").toString();
        	logger.debug("[URLMappingManager] Initializing URLMappingManager using: " + requestMappingsURL);
            URL url = new URL(requestMappingsURL);
            URLMappingsDAO maps=loadMappings(url.openStream());
            urlMappings = maps.getURLMappings();
            screenMappings= maps.getScreenMappings();

        } catch(Exception e) {
            logger.debug("[URLMappingManager] Initializing URLMappingManager exception: " + e);
            e.printStackTrace();
        }
        if(logger==null)
            logger= LoggerManager.getInstance().getLogger(URLMappingManager.class.getName());
        logger.debug("URLMappingManager initialized.");
    }
    private URLMappingsDAO loadMappings(InputStream stream) throws Exception {
        URLMappingsDAO maps=new URLMappingsDAO();
        String dtdURL = "file:" ;
        XmlMapper xh=new XmlMapper();
        xh.setValidating(true);

        // By using dtdURL you brake most buildrs ( at least xerces )
        xh.register("-//Sun Microsystems, Inc.//DTD Web Application 2.2//EN",
                    dtdURL );
        xh.addRule("mappings/url-mapping", xh.objectCreate("nds.control.web.URLMappingHolder") );
        xh.addRule("mappings/url-mapping", xh.addChild("addMappings", null) ); // remove it from stack when done
        xh.addRule("mappings/url-mapping/url", xh.methodSetter("setURL",0) );
        xh.addRule("mappings/url-mapping/screen", xh.methodSetter("setScreen",0) );
        xh.addRule("mappings/url-mapping/request-handler", xh.methodSetter("setRequestHandler",0) );
        xh.addRule("mappings/url-mapping/flow-handler", xh.methodSetter("setFlowHandler",0));

        xh.addRule("mappings/url-mapping/event", xh.methodSetter("setEvent",0));
        xh.addRule("mappings/url-mapping/next-screen", xh.methodSetter("setNextScreen",0));
        xh.addRule("mappings/url-mapping/flow-item", xh.methodSetter("addFlowItem",2));
        xh.addRule("mappings/url-mapping/flow-item/command", xh.methodParam(0) );
        xh.addRule("mappings/url-mapping/flow-item/screen", xh.methodParam(1) );

        xh.addRule("mappings/url-mapping/secured",xh.methodSetter("setSecured",0));
        xh.readXml(stream, maps);
        return maps;

    }

    /**
     * The UrlMapping object contains information that will match
     * a url to a mapping object that contains information about
     * the current screen, the RequestHandler that is needed to
     * process a request, and the RequestHandler that is needed
     * to insure that the propper screen is displayed.
     *
     * @param url the URLMapping.getURL
     */
    public URLMapping getMappingByURL(String url) {
        if ((urlMappings != null) && urlMappings.containsKey(url)) {
            return (URLMapping)urlMappings.get(url);
        } else {
        	//logger.debug(Tools.toString(urlMappings));
            return null;
        }
    }
    /**
     * @param screen the URLMapping.getScreen
     */
    public URLMapping getMappingByScreen(String screen) {
        if ((screenMappings != null) && screenMappings.containsKey(screen)) {
            return (URLMapping)screenMappings.get(screen);
        } else {
            return null;
        }
    }




}
