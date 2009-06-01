/******************************************************************
*
*$RCSfile: GroupSetPermission.java,v $ $Revision: 1.4 $ $Author: Administrator $ $Date: 2005/08/28 00:27:02 $
*
*$Log: GroupSetPermission.java,v $
*Revision 1.4  2005/08/28 00:27:02  Administrator
*no message
*
*Revision 1.3  2005/05/27 05:01:47  Administrator
*no message
*
*Revision 1.2  2005/04/27 03:25:30  Administrator
*no message
*
*Revision 1.1.1.1  2005/03/15 11:23:14  Administrator
*init
*
*Revision 1.4  2004/02/02 10:43:00  yfzhu
*<No Comment Entered>
*
*Revision 1.3  2003/09/29 07:37:15  yfzhu
*before removing entity beans
*
*Revision 1.2  2003/03/30 08:11:58  yfzhu
*Updated before subtotal added
*
*Revision 1.1.1.1  2002/11/29 02:50:34  yfzhu
*Active POS
*
*Revision 1.3  2001/11/13 07:19:00  yfzhu
*no message
*
*Revision 1.2  2001/11/07 20:58:46  yfzhu
*no message
*
*
********************************************************************/
package nds.control.ejb.command;

import java.rmi.RemoteException;
import java.util.List;
import java.util.Vector;

import nds.control.ejb.Command;
import nds.control.event.DefaultWebEvent;
import nds.control.util.ValueHolder;
import nds.query.QueryEngine;
import nds.util.NDSException;
import nds.util.Tools;
public class GroupSetPermission extends Command{

    /**
     * @param event - special parameters:
     *      1*. "type" - String "catalog" | "directory"
     *         1). if "catalog", the selected catalog will be set in "directory_catalog"
     *          and each catalog's perm will be set in ${directory_catalog[i]}'s value,
     *          (may be array such as [1,3,5,9], means read, write, submit, permit)
     *          such as
     *             directory_catalog={"POS", "ÍË»õ"}
     *             POS={1,5} ÍË»õ={1,3,5,9}
     *
     *         2). if "directory", the belonging catalog will be set in "catalog"
     *          and selected directories will be set in "director_id"
     *          each directory's perm will be set in "d"+ ${director_id[i]}'s value
     *          such as,
     *             director_id={"123", "544","344"}
     *             d123={1,5} d544={1,3} d344={1,9}
     *
     *       catalog is in fact ad_tablecategory_id in "directory" table 
     */
    public ValueHolder execute(DefaultWebEvent event) throws NDSException, RemoteException{
        helper.checkOperatorIsAdmin(event);
        /**
         * Find object according to "id" attribute ( locate object )
         * and "command" attribute ( locate object type )
         */
        int groupid = Tools.getInt(event.getParameterValue("groupid"),-1);
        int ad_client_id= helper.getAdClientId(event.getQuerySession(), true);
        String  type=(String)event.getParameterValue("type");
        ValueHolder v = new ValueHolder();
        Vector sqls=new Vector();
        QueryEngine engine = QueryEngine.getInstance();
        if(type.equals("catalog")){
          String[] catalogs=(String[]) event.getParameterValues("directory_catalog");
            if(catalogs!=null)for(int i = 0;i<catalogs.length ;i++){
                    logger.debug("The value of catalogs is:"+catalogs[i]) ;
                    String[] perms =event.getRawParameterValues(catalogs[i]);
                    int perm=0;
                    for( int j=0;j< perms.length;j++){
                      try{
                         perm |= (new Integer(perms[j])).intValue();
                      }catch(Exception eee){}
                    }
                    sqls.addElement("delete from groupperm where groupid="+ groupid +
                            " and directoryid in (select id from Directory where ad_tablecategory_id = "+ catalogs[i]+")");
                    sqls.addElement("insert into groupperm ( id, ad_client_id, groupid, directoryid, permission,sqlfilter, filterdesc)" +
                                    "select GET_SEQUENCES('GroupPerm'),"+ad_client_id+","+ groupid+ ", Directory.id, "+ perm +", sec_get_filter("+ groupid+",Directory.id)"+ ", sec_get_filterdesc("+ groupid+",Directory.id)"+
                                    " from Directory where Directory.ad_tablecategory_id ="+catalogs[i]+"") ;
            }
            // copy back from tmp_groupperm the sqldesc and sql
            //sqls.addElement("update groupperm set (sqlfilter,filterdesc)= (select a.sqlfilter ,a.filterdesc from tmp_groupperm a where a.groupid=groupperm.groupid and a.directoryid= groupperm.directoryid) where groupid="+ groupid);
          }else{
        	  //add system;
        	  String[] subsystems=(String[]) event.getParameterValues("directory_subsystem");  
              if(subsystems!=null)for(int i = 0;i<subsystems.length ;i++){
                      logger.debug("The value of subsystems is:"+subsystems[i]) ;
                      String[] perms =event.getRawParameterValues(subsystems[i]);
                      int perm=0;
                      for( int j=0;j< perms.length;j++){
                        try{
                           perm |= (new Integer(perms[j])).intValue();
                        }catch(Exception eee){}
                      }
                      for(int j = 0;j<subsystems.length ;j++){
                    	  List tablecategoryid=(List)QueryEngine.getInstance().doQueryList("select id from ad_tablecategory where ad_subsystem_id="+subsystems[j]);
                    	  if(tablecategoryid.size()>0){
                    		  for(int k=0;k<tablecategoryid.size();k++){
                    			  sqls.addElement("delete from groupperm where groupid="+ groupid +
                                          " and directoryid in (select id from Directory where ad_tablecategory_id = "+tablecategoryid.get(k)+")");
                    			  sqls.addElement("insert into groupperm ( id, ad_client_id, groupid, directoryid, permission,sqlfilter, filterdesc)" +
                                      "select GET_SEQUENCES('GroupPerm'),"+ad_client_id+","+ groupid+ ", Directory.id, "+ perm +", sec_get_filter("+ groupid+",Directory.id)"+ ", sec_get_filterdesc("+ groupid+",Directory.id)"+
                                      " from Directory where Directory.ad_tablecategory_id ="+tablecategoryid.get(k)+"") ;
                    		  }
                    	  }
                      }
              }
          }
          // copy back from tmp_groupperm the sqldesc and sql
//          sqls.addElement("update groupperm set (sqlfilter,filterdesc)= (select a.sqlfilter, a.filterdesc from tmp_groupperm a where a.groupid=groupperm.groupid and a.directoryid= groupperm.directoryid) where groupid="+ groupid+
//                          " and directoryid in (select id from Directory where catalog = '"+ catalog+"')");
        if (sqls.size()> 0){
          engine.doUpdate(sqls);
        }
        v.put("message","@group-permission-update@") ;
         return v;

    }
}