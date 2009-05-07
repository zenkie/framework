/*
 * Agile Control Technologies Ltd,. CO.
 * http://www.agileControl.com
 */
package nds.web.bean;



import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import nds.util.*;

/**
 *
 *  This class includes several utility functions used by various input
 *  tags.  Functionality common to several classes is located here on
 *  the relatively renegade premise that building variability into a design
 *  is better than using even single inheritance.  (For example, the interfaces
 *  to all utility functions is clearly outlined here , and the utility
 *  functions don't have access to private members of the "interesting"
 *  classes.)  I'll defend that this is more straightforward than a base
 *  class that includes these any day.
 *
 */

public class Util  {

	/** Print out any HTML tag attributes we might have been passed. */
    public static void printAttributes(StringBuffer out, Map attributes)
            throws IOException {
        if (attributes != null) {
            Iterator i = attributes.keySet().iterator();
            while (i.hasNext()) {
                Object oKey = i.next();
                Object oVal = attributes.get(oKey);

                /*
                 * If the attribute contains non-Strings, give the user
                 * a more meaningful message than what he or she would get
                 * if we just propagated a ClassCastException back.
                 * (This'll get caught below.)
                 */
                if (!(oKey instanceof String) ||
                        (oVal != null && !(oVal instanceof String)))
                    throw new IOException(
                        "all members in attributes Map must be Strings");
                String key = (String) oKey;
                String value = (String) oVal;

                if(key.equals("ai")/* || key.equals("imr")*/)continue;//ArrayIndex Hawke(from aic-world) Added
                // check for illegal keys
                if (key.equals("name") || key.equals("value")
                        || key.equals("type") || key.equals("checked"))
                    throw new IOException(
                        "illegal key '" + key + "'found in attributes Map");

                /*
                 * Print the key and value.
                 * If the value is null, make it equal to the key.
                 * This follows the conventions of XHTML 1.0
                 * and does not break regular HTML.
                 */
                if (value == null) value = key;

                out.append(StringUtils.escapeHTMLTags(key) ).append( "=\"").append( StringUtils.escapeHTMLTags(value)).append( "\" ");
            }
        }
    }	


    
    
}
