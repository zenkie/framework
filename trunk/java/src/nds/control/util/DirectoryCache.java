/******************************************************************
*
*$RCSfile: DirectoryCache.java,v $ $Revision: 1.1.1.1 $ $Author: Administrator $ $Date: 2005/03/15 11:23:15 $
*
*$Log: DirectoryCache.java,v $
*Revision 1.1.1.1  2005/03/15 11:23:15  Administrator
*init
*
*Revision 1.1  2003/09/29 07:36:55  yfzhu
*before removing entity beans
*
*Revision 1.2  2003/08/17 14:25:08  yfzhu
*before adv security
*
*Revision 1.1.1.1  2002/11/29 02:50:34  yfzhu
*Active POS
*
*Revision 1.2  2001/11/20 22:36:09  yfzhu
*no message
*
*Revision 1.1  2001/11/16 11:42:40  yfzhu
*no message
*
********************************************************************/
package nds.control.util;

import java.util.Hashtable;

import nds.log.Logger;
import nds.log.LoggerManager;

import org.shiftone.cache.Cache;

public class DirectoryCache implements java.io.Serializable {

    private static Logger logger= LoggerManager.getInstance().getLogger(DirectoryCache.class.getName());
    private Hashtable dirs;//key:String (dirName), value:Integer(permission)
    private Cache cache; // key: Object, value: Expression (user's filter on directory, or preferences)
    /**
     * @param timeout the miliseconds
     * @param maxSize the maximum elements in cache
     */
    public DirectoryCache(long timeout,int maxSize ) {
        dirs=new Hashtable(20);
        // at most contains 30 elements
        //cache=new SoftCache( org.shiftone.cache.CacheManager.getInstance().newCache(timeout, maxSize));
        cache= org.shiftone.cache.CacheManager.getInstance().newCache(timeout, maxSize);
    }
    public Object getCachedObject(Object key){
        return cache.getObject(key);
    }
    public void addCachedObject(Object key, Object value){
        cache.addObject(key, value);

    }
    public Object removeCachedObject(Object key){
        return cache.remove(key);
    }
    public int getCachedSize(){
        return cache.size();
    }


    /**
    @return 0 if directory has not been load yet, or Directory.READ/ Directory.WRITE
    @roseuid 3BF3C57F0277
    */
    public int getPermission(String dirName) {
        Integer perm=(Integer) dirs.get(dirName);
        if( perm ==null)
            return 0;
        return perm.intValue();
    }

    /**
    @roseuid 3BF3C7060157
    */
    public void clear(String dirName) {
        dirs.remove(dirName);
//        logger.debug("directory:"+ dirName+" cleared from cache");
    }

    /**
    @roseuid 3BF3C71B00FD
    */
    public void set(String dirName, int permission) {
        dirs.put(dirName, new Integer(permission));
//        logger.debug("directory:"+ dirName+"(perm="+permission+") added to cache");
    }

    /**
    @roseuid 3BF3C72A0018
    */
    public void clearAll() {
        dirs.clear();
        cache.clear();
//        logger.debug("directory cache pruned");

    }
}
