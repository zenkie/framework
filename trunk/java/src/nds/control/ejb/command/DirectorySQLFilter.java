/******************************************************************
*
*$RCSfile: DirectorySQLFilter.java,v $ $Revision: 1.2 $ $Author: Administrator $ $Date: 2005/12/18 14:06:13 $
*
********************************************************************/
package nds.control.ejb.command;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Vector;

import nds.control.ejb.Command;
import nds.control.event.DefaultWebEvent;
import nds.control.util.ValueHolder;
import nds.query.ColumnLink;
import nds.query.Expression;
import nds.query.QueryEngine;
import nds.query.QueryException;
import nds.schema.Column;
import nds.schema.Table;
import nds.schema.TableManager;
import nds.util.LinkArrayConverter;
import nds.util.NDSException;
import nds.util.Tools;
import nds.util.Validator;
public class DirectorySQLFilter extends Command{

    private static final String UPDATE_GROUPPERM="update groupperm set sqlfilter=?,  FILTERDESC=? where groupid=? and directoryid=?";
    private static final String UPDATE_RELATETABLE_EXIST="update  groupperm set sqlfilter='<expr>' || ? || '<oper>1</oper>' || sqlfilter || '</expr>', filterdesc=filterdesc || ' ²¢ÇÒ '||? where (not sqlfilter is null) and groupperm.directoryid in (select id from directory where tablename=?) and groupid=? and (not sqlfilter like ?)";
    private static final String UPDATE_RELATETABLE_NOT_EXIST="update  groupperm set sqlfilter=?, filterdesc=? where (sqlfilter is null) and groupperm.directoryid in (select id from directory where tablename=?)   and groupid=?  ";
    private static final String GET_TABLE="select tablename from directory where id=?";

    /**
     * @param event - special parameters:
     *     1. action - add/modify/delete
     *     2. groupid - the user group's id
     *     3. dirid   - the driectory id
     *     4). sql     - the user specified sql format like  Expression.toString
     *                 valid when action != delete
     *     5). sqldesc - the sql description,valid when action != delete
     *
     */
    public ValueHolder execute(DefaultWebEvent event) throws NDSException, RemoteException{
        helper.checkOperatorIsAdmin(event);
        /**
         * Find object according to "id" attribute ( locate object )
         * and "command" attribute ( locate object type )
         */
        int groupid = Tools.getInt(event.getParameterValue("groupid"),-1);
        String [] dirids=event.getRawParameterValues("tab_action");
        ValueHolder v = new ValueHolder();
        Vector sqls=new Vector();
        Connection con = QueryEngine.getInstance().getConnection() ;
        PreparedStatement pstmt=null;
        int ad_client_id= helper.getAdClientId(event.getQuerySession(), true);
        String  type=(String)event.getParameterValue("type");
        QueryEngine engine = QueryEngine.getInstance();
        sqls.addElement("delete from tmp_groupperm");
        try{
        	if(dirids!=null){
	        	for(int k=0;k<dirids.length;k++){
	        		int dirid=Integer.parseInt(dirids[k]);
			        pstmt= con.prepareStatement(UPDATE_GROUPPERM);
			        String sql=null, sqldesc=null,expr=null;boolean isSave=true;
			        sqldesc= (String)event.getParameterValue("filter_"+dirid);
			        System.out.print(sqldesc);
			        sql= (String)event.getParameterValue("filter_"+dirid+"_sql");
			        expr= (String)event.getParameterValue("filter_"+dirid+"_expr");
			        if(Validator.isNull(expr) || expr.trim().equalsIgnoreCase("undefined") ){
			           int tableId = Tools.getInt(QueryEngine.getInstance().doQueryOne("select ad_table_id from directory where id="+ dirid, con),-1);
			           Table table= TableManager.getInstance().getTable(tableId);
			           Expression ep=new Expression(new ColumnLink( new int[]{table.getPrimaryKey().getId()}), sql, sqldesc);
			           expr= ep.toString();
			        } 
			        pstmt.setString(1,expr);
			        pstmt.setString(2,sqldesc);
			        pstmt.setInt(3,groupid);
			        pstmt.setInt(4,dirid);
			        pstmt.executeUpdate();
			        
	        	}
        	}
        }catch(Exception e){
            throw new NDSException("@exception@:"+ e, e);
        }finally{
            if( pstmt!=null) try{ pstmt.close();}catch(Exception e1){}
            if( con!=null) try{ con.close();}catch(Exception e1){}
        }
        
        String[] directories= (String[])event.getParameterValues("directory_id");
        String catalog=(String)event.getParameterValue("catalog");
        // save sqlfilter to tmp_groupperm
        sqls.addElement("insert into tmp_groupperm select * from groupperm where sqlfilter is not null and  groupid="+ groupid+
                         " and directoryid in (select id from Directory where ad_tablecategory_id = "+ catalog+")");
        sqls.addElement("delete from groupperm where groupid="+ groupid +
                        " and directoryid in (select id from Directory where ad_tablecategory_id = "+ catalog+")");
        if(directories !=null){
          for(int i = 0;i<directories.length ;i++){
            String[] perms =event.getRawParameterValues("d"+directories[i]);
            int perm=0;
            for( int j=0;j< perms.length;j++){
              try{
                 perm |= (new Integer(perms[j])).intValue();
              }catch(Exception eee){}
            }
            sqls.addElement("insert into groupperm ( id, ad_client_id, groupid, directoryid, permission,sqlfilter, filterdesc) values " +
                            "(GET_SEQUENCES('GroupPerm'),"+ad_client_id+","+ groupid+ ","+ directories[i]+ ","+ perm+", sec_get_filter("+ groupid+","+directories[i]+")"+ ", sec_get_filterdesc("+ groupid+","+directories[i]+")"+")"
                            );

          }
        }
        if (sqls.size()> 0){
            engine.doUpdate(sqls);
        }
        v.put("message","@group-permission-update@") ;
         return v;

    }
}