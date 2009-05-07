/******************************************************************
*
*$RCSfile: CommandMappingManager.java,v $ $Revision: 1.1.1.1 $ $Author: Administrator $ $Date: 2005/03/15 11:23:14 $
*
*$Log: CommandMappingManager.java,v $
*Revision 1.1.1.1  2005/03/15 11:23:14  Administrator
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
package nds.control.ejb;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;

import nds.log.Logger;
import nds.log.LoggerManager;
import nds.util.xml.XmlMapper;
/**
 * URL information loader
 */
public class CommandMappingManager implements java.io.Serializable {

    private static Logger logger= LoggerManager.getInstance().getLogger(CommandMappingManager.class.getName());

    private HashMap mappings;

    private CommandMappingManager(){
    }

    public void init(String theFileName) {
        try {
            URL urlm = this.getClass().getResource(theFileName);
            CommandMappingsDAO maps=loadMappings(urlm.openStream());
            mappings = maps.getMappings();

        } catch (Exception ex) {
            logger.debug("Errors found.", ex);
        }

    }
    private CommandMappingsDAO loadMappings(InputStream stream) throws Exception{
        CommandMappingsDAO maps=new CommandMappingsDAO();
	    String dtdURL = "file:" ;
	    XmlMapper xh=new XmlMapper();
            xh.setValidating(true);

	    // By using dtdURL you brake most buildrs ( at least xerces )
	    xh.register("-//Sun Microsystems, Inc.//DTD Web Application 2.2//EN",
			dtdURL );
	    xh.addRule("mappings/command", xh.objectCreate("nds.control.ejb.CommandMapping") );
	    xh.addRule("mappings/command", xh.addChild("addMappings", null) ); // remove it from stack when done
	    xh.addRule("mappings/command/name", xh.methodSetter("setCommand", 0) );
	    xh.addRule("mappings/command/entity", xh.methodSetter("setEntity",0) );

            xh.readXml(stream, maps);
           return maps;
    }

    /**
     * @param command the Command name, which will also be parameter("command") in DefaultWebEvent.
     */
    public CommandMapping getMappingByCommand(String command) {
        if ((mappings != null) && mappings.containsKey(command)) {
            return (CommandMapping)mappings.get(command);
        } else {
            return null;
        }
    }
    private static CommandMappingManager instance=null;
    public static synchronized CommandMappingManager getInstance(){
        if( instance ==null){
            instance=new CommandMappingManager();
            instance.init("/commandmappings.xml");
        }
        return instance;
    }



}
