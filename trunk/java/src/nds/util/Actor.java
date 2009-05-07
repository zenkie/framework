/******************************************************************
*
*$RCSfile: Actor.java,v $ $Revision: 1.1.1.1 $ $Author: Administrator $ $Date: 2005/03/15 11:23:26 $
*
*$Log: Actor.java,v $
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
package nds.util;


/**
 *  This interface must be implemented by all acting classes.
 *  These are those classes that must be aware of other actors in in order to complete their jobs.
*/
public interface Actor {

    /**
     Initialize the actor by indicating their director.
    */
    public void init(Director director);
}
