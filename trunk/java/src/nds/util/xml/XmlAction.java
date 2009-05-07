/******************************************************************
*
*$RCSfile: XmlAction.java,v $ $Revision: 1.1.1.1 $ $Author: Administrator $ $Date: 2005/03/15 11:23:27 $
*
*$Log: XmlAction.java,v $
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



/** Each rule in Xml Mapper can invoke certain actions.
    An action implementation will be notified for each matching rule
    on start and end of the tag that matches.

    After all end actions are called, a special cleanup call will allow
    actions to remove temporary data.
*/
public abstract class XmlAction {
    public void start( SaxContext ctx) throws Exception {
    }

    public void end( SaxContext ctx) throws Exception {
    }

    public void cleanup( SaxContext ctx) throws Exception {
    }
}

