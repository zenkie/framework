package nds.util;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

/**
 * This class is used to create and control software actors and resources.
 *
 */

public class Manager
  extends Hashtable implements Actor, Factory, Director, DestroyListener {

    static {
        // Tomcat 3.1 classloader workaround
        try {
            Class.forName ("nds.util.DestroyListener");
        }
        catch (ClassNotFoundException ex) {
            ex.printStackTrace ();
        }
    }

    /**
     * Initialize the actor by indicating their director.
     */
    public void init(Director director) {
    }

    /**
     * Create the instance of a class given its name.
     */
    public Object create(String name) {
        return create(name, null,null);
    }
    public Object create(String name, ClassLoader classLoader) {
        return create(name, null, classLoader);
    }
    public Object create(String name, Configurations conf) throws RuntimeException {
        return create(name, conf,null);
    }
    /**
     * Create the instance of a class and, if configurable, use
     * the given configurations to configure it.
     */
    public Object create(String name, Configurations conf, ClassLoader loader) throws RuntimeException {
        try {
            Object object = null;
            ClassLoader cl =loader;
            if(cl ==null) cl= this.getClass().getClassLoader();
            if (cl != null) {
                object =java.beans.Beans.instantiate(cl, name);
            } else {
                object = Class.forName(name).newInstance();
            }

            if (object instanceof Actor) {
                ((Actor) object).init((Director) this);
            }

            if ((object instanceof Configurable) && (conf != null)) {
                ((Configurable) object).init(conf);
            }

            return object;
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Error creating " + name + ": class is not found");
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Error creating " + name + ": does not have access");
        } catch (InstantiationException e) {
            throw new RuntimeException("Error creating " + name + ": could not instantiate " + e.getMessage());
        } catch (RuntimeException e) {
            throw e;
        } catch (NoClassDefFoundError e) {
            throw new RuntimeException("Error creating " + name + ": make sure the needed classes can be found in the classpath (" + e.getMessage() + ")");
        } catch (Throwable e) {
            throw new RuntimeException("Exception when creating \" " + name + "\" : \n" + e);
        }
    }

    /**
     * Create a vector of instances.
     */
    public Vector create(Vector names) {
        return create(names, null);
    }

    /**
     * Create a vector of instances with given configurations.
     */
     public Vector create(Vector names, Configurations conf) {
         Vector v = new Vector(names.size());
         Enumeration e = names.elements();
         while (e.hasMoreElements()) {
             v.addElement(create((String) e.nextElement(), conf));
         }
         return v;
     }

    public void destroy () {
      destroyAll ();
    }

    /**
     * Calls destroy() on all components that are instances of DestroyListener
     */
    public void destroyAll () {
       Enumeration e = elements ();
       while (e.hasMoreElements ()) {
          Object x = e.nextElement ();
          if (x instanceof DestroyListener && x != this) {

            ((DestroyListener) x).destroy ();
          }
       }
    }

    /**
     * Get the actor currently playing the given role.
     */
    public Object getActor(String role) {
        return this.get(role);
    }

    /**
     * Set the actor for the role.
     */
    public void setRole(String role, Object actor) {
        this.put(role, actor);
    }

    /**
     * Get the roles currently set.
     */
    public Enumeration getRoles() {
        return this.keys();
    }
    public String toString(){
        return "Manager"+ System.identityHashCode(this);
    }
}
