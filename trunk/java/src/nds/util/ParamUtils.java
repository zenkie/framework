/******************************************************************
*
*$RCSfile: ParamUtils.java,v $ $Revision: 1.2 $ $Author: Administrator $ $Date: 2006/01/07 11:47:34 $
*
*$Log: ParamUtils.java,v $
*Revision 1.2  2006/01/07 11:47:34  Administrator
*no message
*
*Revision 1.1.1.1  2005/03/15 11:23:26  Administrator
*init
*
*Revision 1.2  2003/09/29 07:37:23  yfzhu
*before removing entity beans
*
*Revision 1.1.1.1  2002/11/29 02:50:35  yfzhu
*Active POS
*
*Revision 1.4  2002/01/04 01:43:22  yfzhu
*no message
*
*Revision 1.3  2001/12/09 03:43:32  yfzhu
*no message
*
*Revision 1.2  2001/11/07 20:58:47  yfzhu
*no message
*
*
********************************************************************/
package nds.util;
import javax.servlet.ServletRequest;
import javax.portlet.RenderRequest;
/**
 *  This class assists skin writers in getting parameters.
 */
public class ParamUtils {

    /**
     *  Gets a parameter as a string.
     *  @param request The ServletRequest object, known as "request" in a
     *  JSP page.
     *  @param paramName The name of the parameter you want to get
     *  @return The value of the parameter or null if the parameter was not
     *  found or if the parameter is a zero-length string.
     */
        public static String getParameter( ServletRequest request, String paramName ) {
                return getParameter( request, paramName, false );
        }

    /**
     *  Gets a parameter as a string.
     *  @param request The ServletRequest object, known as "request" in a
     *  JSP page.
     *  @param paramName The name of the parameter you want to get
     *  @param emptyStringsOK Return the parameter values even if it is an empty string.
     *  @return The value of the parameter or null if the parameter was not
     *  found.
     */
        public static String getParameter( ServletRequest request, String paramName, boolean emptyStringsOK ) {
                String temp = request.getParameter(paramName);
        if( temp != null ) {
            if( temp.equals("") && !emptyStringsOK ) {
                return null;
            }
            else {
                return temp;
            }
        }
        else {
            return null;
        }
    }

    /**
     *  Gets a parameter as a boolean.
     *  @param request The ServletRequest object, known as "request" in a
     *  JSP page.
     *  @param paramName The name of the parameter you want to get
     *  @return True if the value of the parameter was "true", false otherwise.
     */
        public static boolean getBooleanParameter( ServletRequest request, String paramName ) {
                String temp = request.getParameter(paramName);
                if( temp != null && temp.trim().equalsIgnoreCase("true") ) {
                        return true;
                } else {
                        return false;
                }
        }
        public static boolean getBooleanParameter( RenderRequest request, String paramName ) {
                String temp = request.getParameter(paramName);
                if( temp != null && temp.trim().equalsIgnoreCase("true") ) {
                        return true;
                } else {
                        return false;
                }
        }
        /**
         * Only when request has no parameter, the default value will be returned
         * @param request
         * @param paramName
         * @param defaultValue
         * @return
         */
        public static boolean getBooleanParameter(ServletRequest request, String paramName ,boolean defaultValue) {
            String temp = request.getParameter(paramName);
            if(temp ==null ) return defaultValue;
            if( temp != null && temp.trim().equalsIgnoreCase("true") ) {
                    return true;
            } else {
                    return false;
            }
    }

    /**
     *  Gets a parameter as a int.
     *  @param request The ServletRequest object, known as "request" in a
     *  JSP page.
     *  @param paramName The name of the parameter you want to get
     *  @return The int value of the parameter specified or the default value if
     *  the parameter is not found.
     */
        public static int getIntParameter( ServletRequest request, String paramName, int defaultNum ) {
                String temp = request.getParameter(paramName);
                if( temp != null && !temp.equals("") ) {
            int num = defaultNum;
            try {
                num = Integer.parseInt(temp);
            }
            catch( Exception ignored ) {}
                        return num;
                } else {
                        return defaultNum;
                }
        }

    /**
     *  Gets a checkbox parameter value as a boolean.
     *  @param request The ServletRequest object, known as "request" in a
     *  JSP page.
     *  @param paramName The name of the parameter you want to get
     *  @return True if the value of the checkbox is "on", false otherwise.
     */
    public static boolean getCheckboxParameter( ServletRequest request, String paramName ) {
        String temp = request.getParameter(paramName);
        if( temp != null && temp.equals("on") ) {
            return true;
        } else {
            return false;
        }
    }

    /**
     *  Gets a parameter as a string.
     *  @param request The ServletRequest object, known as "request" in a
     *  JSP page.
     *  @param attribName The name of the parameter you want to get
     *  @return The value of the parameter or null if the parameter was not
     *  found or if the parameter is a zero-length string.
     */
        public static String getAttribute( ServletRequest request, String attribName ) {
                return getAttribute( request, attribName, false );
        }

    /**
     *  Gets a parameter as a string.
     *  @param request The ServletRequest object, known as "request" in a
     *  JSP page.
     *  @param attribName The name of the parameter you want to get
     *  @param emptyStringsOK Return the parameter values even if it is an empty string.
     *  @return The value of the parameter or null if the parameter was not
     *  found.
     */
        public static String getAttribute(ServletRequest request, String attribName, boolean emptyStringsOK ) {
                String temp = (String)request.getAttribute(attribName);
        if( temp != null ) {
            if( temp.equals("") && !emptyStringsOK ) {
                return null;
            }
            else {
                return temp;
            }
        }
        else {
            return null;
        }
    }

    /**
     *  Gets an attribute as a boolean.
     *  @param request The ServletRequest object, known as "request" in a
     *  JSP page.
     *  @param attribName The name of the attribute you want to get
     *  @return True if the value of the attribute is "true", false otherwise.
     */
        public static boolean getBooleanAttribute( ServletRequest request, String attribName ) {
                String temp = (String)request.getAttribute(attribName);
                if( temp != null && temp.trim().equalsIgnoreCase("true") ) {
                        return true;
                } else {
                        return false;
                }
        }
        /**
         *  Gets an attribute as a boolean.
         *  @param request The ServletRequest object, known as "request" in a
         *  JSP page.
         *  @param attribName The name of the attribute you want to get
         *  @param defaultValue when attribName not exists in request, will return this one
         *  @return True if the value of the attribute is "true", false if attributes exists and not se to "true".
         */
      public static boolean getBooleanAttribute( ServletRequest request, String attribName , boolean defaultValue) {
            String temp = (String)request.getAttribute(attribName);
            if(temp==null) return defaultValue;
            if( temp != null && temp.trim().equalsIgnoreCase("true") ) {
                    return true;
            } else {
                    return false;
            }
    }

    /**
     *  Gets an attribute as a int.
     *  @param request The ServletRequest object, known as "request" in a
     *  JSP page.
     *  @param attribName The name of the attribute you want to get
     *  @return The int value of the attribute or the default value if the attribute is not
     *  found or is a zero length string.
     */
        public static int getIntAttribute( ServletRequest request, String attribName, int defaultNum ) {
                String temp = ""+request.getAttribute(attribName);
                if( temp != null && !temp.equals("") ) {
            int num = defaultNum;
            try {
                num = Integer.parseInt(temp);
            }
            catch( Exception ignored ) {}
                        return num;
                } else {
                        return defaultNum;
                }
        }
    /**
     * Search both parameter and attribute of <code>req</code> for specified value.
     * Will first read in attribute, and in parameter list
     */
    public static int getIntAttributeOrParameter(ServletRequest req,String name, int def){
        int i= getIntAttribute(req,name,def);
        if( i== def){
            i= getIntParameter(req,name,def);

        }
        return i;
    }
    /**
     * Search both parameter and attribute of <code>req</code> for specified value.
     * Will first read in attribute, and in parameter list
     */
    public static String getAttributeOrParameter(ServletRequest req,String name){
        String v=(String) getAttribute(req,name);
        if( Validator.isNull(v)){
            v= getParameter(req,name);
        }
        return v;
    }
}
