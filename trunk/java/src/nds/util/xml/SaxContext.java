/******************************************************************
*
*$RCSfile: SaxContext.java,v $ $Revision: 1.1.1.1 $ $Author: Administrator $ $Date: 2005/03/15 11:23:27 $
*
*$Log: SaxContext.java,v $
*Revision 1.1.1.1  2005/03/15 11:23:27  Administrator
*init
*
*Revision 1.1.1.1  2002/11/29 02:50:35  yfzhu
*Active POS
*
*Revision 1.2  2001/11/07 20:58:48  yfzhu
*no message
*
*
********************************************************************/
package nds.util.xml;

//import org.apache.tomcat.util.*;
import java.util.Stack;

import org.xml.sax.AttributeList;

// XXX this interface is not final, but a prototype.

/** SAX Context - used to match and perform actions
 *    provide access to the current stack and XML elements.
 *
 * @author costin@dnt.ro
 */
public interface SaxContext  {

    // -------------------- Access to parsing context

    /** Depth of the tag stack.
     */
    public int getTagCount();

    /** Access attributes of a particular tag
     */
    public AttributeList getAttributeList( int pos );

    /** Access a particular tag
     */
    public String getTag( int pos );

    /** Body of the last tag
     */
    public String getBody();

    // -------------------- Object stack

    /**
       The root object is either set by caller before starting the parse
       or can be created using the first tag. It is used to set object in
       the result graph by navigation ( using root and a path). Please
       use the stack, it's much faster and better.
    */
    public Object getRoot();

    /** We maintain a stack to keep java objects that are generated
	as result of parsing. You can either use the stack ( which is
	very powerfull construct !), or use the root object
	and navigation in the result tree.
    */
    public Stack getObjectStack();

    // -------------------- Utilities

    public int getDebug();

    public void log( String s );
}
