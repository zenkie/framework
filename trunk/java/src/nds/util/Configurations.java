/******************************************************************
*
*$RCSfile: Configurations.java,v $ $Revision: 1.1.1.1 $ $Author: Administrator $ $Date: 2005/03/15 11:23:26 $
*
*$Log: Configurations.java,v $
*Revision 1.1.1.1  2005/03/15 11:23:26  Administrator
*init
*
*Revision 1.2  2003/09/29 07:37:23  yfzhu
*before removing entity beans
*
*Revision 1.1.1.1  2002/11/29 02:50:35  yfzhu
*Active POS
*
*Revision 1.3  2001/11/15 05:10:28  yfzhu
*no message
*
*Revision 1.2  2001/11/07 20:58:47  yfzhu
*no message
*
*
********************************************************************/

package nds.util;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Vector;

/**
 * This class encapsulates all the configurations needed by a Configurable
 * class to work.
 *
 * @version $Revision: 1.1.1.1 $ $Date: 2005/03/15 11:23:26 $
 */

public class Configurations implements java.io.Serializable{
    protected Configurations root;
    protected String baseName;
    protected Properties properties;

    // if configurations are retrieved from file, this will store the file URL
    private String configURL=null;

    public Configurations() {
        properties = new Properties();
        root = this;
    }

    /**
     * Create the class from a the file
     */
    public Configurations(String file) throws IOException {
        this(file, null);
    }
    public Configurations(Properties p){
        properties=p;
        root= this;
        configURL= null;
    }
    /**
     * Create the class with given defaults and from the file
     */
    public Configurations(String file, Configurations defaults) throws IOException {
        if (defaults != null) {
            properties = new Properties(defaults.properties);
        } else {
            properties = new Properties();
        }
        InputStream input = getClass().getResourceAsStream(file);
        properties.load(input);
        input.close();
        root = this;
        configURL= getClass().getResource(file).getPath();

    }

    /**
     * Create the class from a the InputStream
     */
    public Configurations(InputStream stream) throws IOException {
        this(stream, null);
    }

    /**
     * Create the class with given defaults and from the URL resource
     */
    public Configurations(InputStream stream, Configurations defaults) throws IOException {
        if (defaults != null) {
            properties = new Properties(defaults.properties);
        } else {
            properties = new Properties();
        }
        properties.load(stream);
        root = this;
    }

    /**
     * Create the class with given defaults.
     */
    public Configurations(Configurations c) {
        properties = new Properties(c.properties);
        root = this;
    }

    /**
     * Create a subconfiguration starting from the base node.
     */
    public Configurations(Configurations parent, String base) {
        properties = new Properties();
        root = parent.root;
        setBasename((parent.baseName == null) ? base : parent.baseName + "." + base);
        String prefix = base + ".";

        Enumeration keys = parent.properties.propertyNames();
        while (keys.hasMoreElements()) {
            String key = (String) keys.nextElement();

            if (key.startsWith(prefix)) {
                set(key.substring(prefix.length()), parent.get(key));
            } else if (key.equals(base)) {
                set("", parent.get(key));
            }
        }
    }
    /**
     * Return the file absolute path if this configuation is retrieved from a file
     * return null if configurations are not from file
     */
    public String getConfigFilePath(){
        return configURL;
    }
    /**
     * Save configurations to default path, if configurations is originally
     * retrieved from a disk file, then path will be that.
     * If configurations is newly created, then will first try to get property
     * named "path" as store location, if not exists, will store to system default
     * path, and that path will also be set to "path" property
     * @throws IOException if error occurs
     */
    public void save() throws IOException{
        String path;
        if( configURL !=null){
            // the resource is from a disk file
            path= configURL;
        }else if(   properties.getProperty("path") !=null){
            path=properties.getProperty("path");
        }else{
            // create a default file
            path=System.getProperty("nds.config.path",nds.util.WebKeys.NDS_PROPERTIES_FILE);
        }
        save(path);
        // store the path to configURL
        configURL=path;

    }
    /**
     * Saves properties to disk.
     * @param path the file path to be saved to
     */
    public void save(String path) throws IOException {
        path = path.trim();
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(path);
            properties.store(out, "store date -- " + (new java.util.Date()));
        }
        catch (Exception ioe) {
            System.err.println("There was an error writing to " + path + ". " +
                "Ensure that the path exists and that the process has permission " +
                "to write to it -- " + ioe);
            ioe.printStackTrace();
        }
        finally {
            try {
               out.close();
            } catch (Exception e) { }
        }
    }

    /**
     * Set the configuration.
     */
    public void set(String key, Object value) {
        properties.put(key, value);
    }

    /**
     * The JavaModule interpreter is using this method. should it?
     */
    public void put(String key, Object value) {
        properties.put(key,value);
    }

    /**
     * Get the configuration.
     */
    public Object get(String key) {
        return properties.get(key);
    }

    public String getProperty(String key){
    	return (String)properties.get(key);
    }
    public String getProperty(String key, String defaultValue){
    	String value=(String)properties.get(key);
    	if( value==null) value=defaultValue;
    	return value;
    }

    /**
     * Get the configuration and use the given default value if not found.
     */
    public Object get(String key, Object def) {
        Object o = properties.get(key);
        return (o == null) ? def : o;
    }

    /**
     * Get the configuration, throw an exception if not present.
     */
    public Object getNotNull(String key) {
        Object o = properties.get(key);
        if (o == null) {
            throw new RuntimeException("Configuration item '" +
              ((baseName == null) ? "" : baseName + "." + key) + "' is not set");
        } else {
            return o;
        }
    }

    /**
     * Get a vector of configurations when the syntax is incremental
     */
    public Vector getVector(String key) {
        Vector v = new Vector();

        for (int i = 0; ; i++) {
            Object n = get(key + "." + i);
            if (n == null) break;
            v.addElement(n);
        }

        return v;
    }

    /**
     * Create a subconfiguration starting from the base node.
     */
    public Configurations getConfigurations(String base) {
        return new Configurations(this, base);
    }

    /**
     * For use by superclasses and support classes.
     * Gets the configuration for the
     * specified class from the root configurations object.
     *
     * @param c - the superclass that is to be configured.
     * @param x - the object that is to be configured, for validation.
     * @throws IllegalAccessException if this object is not allowed
     * to access configuration information for the given class.
     */
    public Configurations getAnyConfig (Class c, Object x)
    throws IllegalAccessException {
      if (!c.isInstance (x)) {
        throw new IllegalAccessException
          (x + " cannot access configuration for " + c);
      }
      return root.getConfigurations (c.getName ());
    }

    /**
     * Get the Properties from the Configuration
     */
    public Properties getProperties() {
        return this.properties;
    }

    public void setBasename(String baseName) {
        this.baseName = baseName;
    }

    /**
     * Used by XSPProcessor, but this maybe should be getKeys()?
     */
    public Enumeration keys() {
        return properties.keys();
    }
    public  static void main(String[] args) throws Exception{
        Properties props= new Properties();
        props.setProperty("a.b.c", "abc");
        props.setProperty("a.b", "hello");
        props.setProperty("c.d", "cd");
        Configurations conf= new Configurations(props);
        Configurations conf2= conf.getConfigurations("a.b");

    }
}
