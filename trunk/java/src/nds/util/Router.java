/******************************************************************
*
*$RCSfile: Router.java,v $ $Revision: 1.1.1.1 $ $Author: Administrator $ $Date: 2005/03/15 11:23:26 $
*
*$Log: Router.java,v $
*Revision 1.1.1.1  2005/03/15 11:23:26  Administrator
*init
*
*Revision 1.1.1.1  2002/11/29 02:50:35  yfzhu
*Active POS
*
*Revision 1.1  2001/11/16 11:42:40  yfzhu
*no message
*
********************************************************************/
//Source file: f:\DownLoad\java\farm\source\farm\framework\Router.java

package nds.util;

import java.util.Enumeration;
import java.util.Hashtable;

/**
* This class implements a basic Router implementation that is used
 * by those classes that must assign the execution of a particular
 * pluggable instance depending on some "type reaction".
 *
 * @version $Revision: 1.1.1.1 $ $Date: 2005/03/15 11:23:26 $
*/
public abstract class Router extends AbstractActor implements Status, Configurable {
    protected Hashtable objects;
    protected String defaultType;

    /**
    @roseuuuid 3AAC42210036
    */
    public void init(Configurations conf) {
        Factory factory = (Factory) director.getActor("manager");
        this.defaultType = (String) conf.get("default");
        this.objects = new Hashtable();

        Configurations types = conf.getConfigurations("type");
        Enumeration e = types.keys();
        while (e.hasMoreElements()) {
            String type = (String) e.nextElement();
            String name = ((String) types.get(type)).trim ();
            objects.put(type, factory.create(name, conf.getConfigurations(type)));
        }
    }

    /**
    @roseuuuid 3AAC4221005E
    */
    public String getStatus() {
        StringBuffer buffer = new StringBuffer();
        if (defaultType != null)
            buffer.append("<li><b>Default type</b> = " + defaultType);
        Enumeration e = objects.keys();
        while (e.hasMoreElements()) {
            String type = (String) e.nextElement();
            Object o = objects.get(type);
            buffer.append("<li><b>" + type + "</b>");
            if (o instanceof Status) {
                buffer.append(": " + ((Status) o).getStatus());
            }
            buffer.append("</li>");
        }
        return buffer.toString();
    }
}
