/******************************************************************
*
*$RCSfile: CommandMappingsDAO.java,v $ $Revision: 1.1.1.1 $ $Author: Administrator $ $Date: 2005/03/15 11:23:14 $
*
*$Log: CommandMappingsDAO.java,v $
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
import java.util.HashMap;
 /**
  * By heart, I did not want this class be public. It's only a private class
  * in URLMappingManager. But as for the limitation of XMLMapper, I had to make
  * it like this.
  */
 public class CommandMappingsDAO{
    private HashMap mappings;
    public CommandMappingsDAO(){
        mappings=new HashMap();
    }
    public void addMappings( CommandMapping map){
        mappings.put(map.getCommand(), map);
    }
    public HashMap getMappings(){
        return mappings;
    }
 }
