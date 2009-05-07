/******************************************************************
*
*$RCSfile: AbstractActor.java,v $ $Revision: 1.1.1.1 $ $Author: Administrator $ $Date: 2005/03/15 11:23:26 $
*
*$Log: AbstractActor.java,v $
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
This class implements the usual method to store and keep the director reference for later use.
*/
public class AbstractActor implements Actor {
    protected Director director;

    /**
     * Initialize the actor by indicating their director.
     * @roseuuuid 3A74BD900161
    */
    public void init(Director director) {
        this.director = director;
    }
}
