/******************************************************************
*
*$RCSfile: URLMappingsDAO.java,v $ $Revision: 1.1.1.1 $ $Author: Administrator $ $Date: 2005/03/15 11:23:16 $
*
*$Log: URLMappingsDAO.java,v $
*Revision 1.1.1.1  2005/03/15 11:23:16  Administrator
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
package nds.control.web;
import java.util.HashMap;
 /**
  * By heart, I did not want this class be public. It's only a private class
  * in URLMappingManager. But as for the limitation of XMLMapper, I had to make
  * it like this.
  *
  * Think it as a blemish
  */
 public class URLMappingsDAO{
    private HashMap urlmappings;
    private HashMap screenmappings;
    public URLMappingsDAO(){
        urlmappings=new HashMap();
        screenmappings=new HashMap();
    }
    public void addMappings( URLMappingHolder holder){
        URLMapping map= holder.toURLMapping();
        urlmappings.put(map.getURL(), map);
        screenmappings.put(map.getScreen(),map);
    }
    public HashMap getURLMappings(){
        return urlmappings;
    }
    public HashMap getScreenMappings(){
        return screenmappings;
    }
 }
