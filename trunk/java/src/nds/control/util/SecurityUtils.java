/*
 * Agile Control Technologies Ltd,. CO.
 * http://www.agileControl.com
 */
package nds.control.util;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Locale;

import nds.control.web.SecurityManagerWebImpl;
import nds.control.web.WebUtils;
import nds.log.Logger;
import nds.log.LoggerManager;
import nds.query.ColumnLink;
import nds.query.Expression;
import nds.query.QueryEngine;
import nds.query.QueryException;
import nds.query.QueryRequestImpl;
import nds.query.QueryResult;
import nds.query.QuerySession;
import nds.query.SQLCombination;
import nds.schema.Column;
import nds.schema.RefByTable;
import nds.schema.Table;
import nds.schema.TableManager;
import nds.security.Directory;
import nds.security.User;
import nds.util.MessagesHolder;
import nds.util.NDSException;
import nds.util.NDSRuntimeException;
import nds.util.StringHashtable;
import nds.util.TimeLog;
import nds.util.Tools;
import nds.util.Validator;
import nds.util.WebKeys;

/**
 * 
 * @author yfzhu@agilecontrol.com
 */

public class SecurityUtils {
    private static Logger logger= LoggerManager.getInstance().getLogger(SecurityUtils.class.getName());
    private static final String GET_SECURITY_FILTER="select sqlfilter, filterdesc,t.name from groupperm, directory, ad_table t where (t.id=directory.ad_table_id) and  groupid in (select groupid from groupuser where userid=? ) and directoryid in (select id from directory where upper(name)=?) and bitand(permission,?)+0=? and directory.id=directoryid";
    private final static String GET_PERMISSION="select GetUserPermission(?,?) from dual";
    private final static String GET_USER_BY_NAME_AND_CLIENT="select u.id, u.name, u.isactive,u.ad_client_id, u.ad_org_id, u.language, c.name, u.isadmin from users u, ad_client c where u.name=? and u.ad_client_id=c.id and c.domain=?";
    private final static String GET_USER_BY_EMAIL_AND_CLIENT="select u.id, u.name, u.isactive,u.ad_client_id, u.ad_org_id, u.language, c.name ,u.isadmin from users u, ad_client c where u.email=? and u.ad_client_id=c.id and c.domain=?";
    private final static String GET_USER_BY_ID="select u.name,c.domain,u.isactive, u.description, u.ad_client_id, u.ad_org_id, u.language, c.name,u.isadmin  from users u,ad_client c where u.ID=? and c.id=u.ad_client_id";
 
    /**
    @roseuid 3BF38E580012
    */
    public static int getPermission(String dirName, int userId)throws QueryException,java.rmi.RemoteException {
        Connection con=QueryEngine.getInstance().getConnection();
        PreparedStatement pstmt= null;
        ResultSet rs=null;
        try {
            pstmt= con.prepareStatement(GET_PERMISSION);
            //    private static String GET_PERMISSION="select GetUserPermission(?,?) from dual";
            pstmt.setString(1,dirName);
            pstmt.setInt(2, userId);
            rs= pstmt.executeQuery();
            if( rs.next() ){
                return rs.getInt(1);
            }
        } catch (Exception ce) {
            logger.error("Error getting permission for "+dirName+" of userId="+userId);
            throw new NDSRuntimeException("Error getting SbSecurityManager",ce);
        }finally{
            if( rs !=null){ try{ rs.close();}catch(Exception e){}}
            if( pstmt !=null){ try{ pstmt.close();}catch(Exception e){}}
            if( con !=null){ try{ con.close();}catch(Exception e){}}
        }
        return 0;
    }
    /**
     * write a int array to String, for example,
     * int[4]={1,3,545,3};
     * the return string will be: "1,3,545,3"
     */
    public static String toString(Object[] cs) {
    	if (cs==null ) return "";
        String s="";
        for(int i=0;i< cs.length;i++) {
            if( i==0)
                s += ""+cs[i];
            else
                s +=","+cs[i];
        }
        return s+"";
    }
    public static boolean hasPermissionOnAll(int userId, String userName, String tableName, 
    		Object[] objectIds, int permission, QuerySession qsession)throws Exception{
    	return hasPermissionOnAll(userId, userName, tableName, objectIds, permission, qsession, null);
    }
    /**
     * Check whether has permission on all objects
     * @param userId
     * @param userName
     * @param tableName
     * @param objectIds
     * @param permission
     * @param qsessions
     * @param addtionalFilter in same format as table.getFilter()
     * @return
     * @throws Exception
     */
    public static boolean hasPermissionOnAll(int userId, String userName, String tableName, 
    		Object[] objectIds, int permission, QuerySession qsession, String addtionalFilter)throws Exception{
    	if( /*"root".equals(userName) ||*/ objectIds==null || objectIds.length==0) return true; //root
    	TableManager manager=TableManager.getInstance();
    	QueryEngine engine= QueryEngine.getInstance();
    	Table table=manager.getTable(tableName);
    	//if(permission == nds.security.Directory.SUBMIT|| permission == Directory.EXPORT )throw new NDSException("Internal error: perm is not supported currently");
    	if(!"root".equals(userName) &&  (getPermission(table.getSecurityDirectory(), userId) & permission)!=permission) return false; 
    	QueryRequestImpl query= engine.createRequest(qsession);
    	query.setMainTable(table.getId());
    	query.addSelection(table.getPrimaryKey().getId() );
    	Expression expr= new Expression();
    	expr.setColumnLink(tableName+"." + table.getPrimaryKey().getName());
    	expr.setCondition(" IN (" + toString(objectIds)+")");
    	if(Validator.isNotNull(addtionalFilter)){
    		Expression expr2=new Expression(null, addtionalFilter , null);    		
    		expr= expr.combine(expr2, SQLCombination.SQL_AND, null);
    	}
    	Expression exprw= getSecurityFilter(tableName,permission,userId, qsession );
    	if (! exprw.isEmpty())
    		query.addParam( expr.combine(exprw, expr.SQL_AND, " AND ") );
    	else
    		query.addParam( expr);
    	QueryResult result= engine.doQuery(query);
    	logger.debug(query.toSQL());
    	return result.getTotalRowCount() ==  objectIds.length ;
    	
    }	
    /**
     * Check user's permission on specified object
     * @param tableName the table name of the object
     * @param objectId  the pk id of the table
     * @param permission nds.security.Directory.READ/WRITE/AUDIT
     * @return true if current user has that permission on the object
     * @throws Exception
     */
    public static boolean hasObjectPermission(int userId, String userName, String tableName, 
    		int objectId, int permission, QuerySession qsession) throws QueryException, RemoteException{
    	//if( "root".equals(userName) ) return true; //root
    	TableManager manager=TableManager.getInstance();
    	QueryEngine engine= QueryEngine.getInstance();
    	Table table=manager.getTable(tableName);
    	//if(permission == nds.security.Directory.SUBMIT|| permission == Directory.EXPORT )throw new NDSException("Internal error: perm is not supported currently");
    	if( !"root".equals(userName) && (getPermission(table.getSecurityDirectory(), userId) & permission)!=permission) return false; 
    	QueryRequestImpl query= engine.createRequest(qsession);
    	query.setMainTable(table.getId());
    	query.addSelection(table.getPrimaryKey().getId() );
    	Expression expr= new Expression();
    	expr.setColumnLink(tableName+"." + table.getPrimaryKey().getName());
    	expr.setCondition("=" + objectId);
    	expr.setDescription("("+ table.getDescription(qsession.getLocale())+".ID=" + objectId+")");
    	
    	Expression exprw= getSecurityFilter(tableName,permission,userId, qsession );
    	expr= expr.combine(exprw,  expr.SQL_AND, " AND ") ;
    	
    	// check column "status"
    	if(((permission & Directory.WRITE)==Directory.WRITE) &&   table.isActionEnabled(Table.SUBMIT)){
    		exprw=new Expression();
    		exprw.setColumnLink(tableName+"." + "STATUS");
    		exprw.setCondition("<>2");
    		exprw.setDescription("("+ table.getDescription(qsession.getLocale())+".STATUS<>2)");
        	expr= expr.combine(exprw,  expr.SQL_AND, " AND ") ;
    	}
    	query.addParam(expr);
    	QueryResult result= engine.doQuery(query);
    	logger.debug(query.toSQL());
    	return result.getTotalRowCount()> 0 ;
    	
    }	
    /**
     * 根据tablename 找到对应的directory, 获得相应的过滤器，以下为特殊情况：
     * 1）有些表（如订单行）使用其他表（订单）的安全过滤器，
     * 2）另外一些表（如供应商）由于是另外表（往来户）的子集，自身也没有过滤器。
     * 
     * 对于第1种情况，由于表的ID不同，需要重新构造过滤器， 而情况2由于ID相同，无需重构。
     * 下面是情况1的重构方法
     * 假设订单的过滤器为 order.column1=data and order.column2=data2
     * 订单和订单行存在关系：order.id=oreritem.orderid, 则订单行的过滤器为
     * exists (select 1 from order where order.id=orderitem.orderid and
     *  order.id in (select id from order where (order.column1=data and order.column2=data2))
     * 进一步简化为：
     * exists (select id from order where order.id=orderitem.orderid and
     * 	order.column1=data and order.column2=data2)
     * 
     * 标准格式：exists (select ID from <DIR_TABLE> WHERE <RELATIONSHIP> AND
     *   <DIR_FILTER>)
     * 
     * 设计时应注意：directory 中的tablename 必须是数据库中真实的table 或 view
     * @param permission, 1 for read, 3 for write, 5 for submit, 9 for audit, combine, so 7 for read/write/submit
     * @return empty exprssion if not found, nerver return null!
     *
     */
    public static Expression getSecurityFilter(String tableName, int permission, int userId, QuerySession qs) throws QueryException{
        Connection con= null;
        ResultSet rs=null;
        PreparedStatement pstmt=null;
        try{
            con=QueryEngine.getInstance().getConnection();
            pstmt= con.prepareStatement(GET_SECURITY_FILTER);
            pstmt.setInt(1, userId);
            pstmt.setString(2,TableManager.getInstance().getTable(tableName).getSecurityDirectory() );
            pstmt.setInt(3, permission);
            pstmt.setInt(4, permission);
            rs= pstmt.executeQuery();
            
            StringHashtable st=new StringHashtable(10, 4000); // the maximum sql length is 2000, limited by db
            String sql,desc,tn;
            while( rs.next()){
                sql= rs.getString(1);
                desc= rs.getString(2);
                tn= rs.getString(3);
                if ( sql !=null && sql.length() > 1){
                    st.put(sql, new String[]{desc, tn});
//                    logger.debug("found sql:" + sql +", with desc=" + desc);
                }else{
                	//has at least one group which allows current user to work on all data
                	st.clear();
                	break;
                }

            }
            if( st.size() > 0){
                TableManager tm= TableManager.getInstance();
                Table table= tm.getTable(tableName);
                int[] cids= new int[1]; // contains only pk column
                cids[0]= table.getPrimaryKey().getId() ;
                ColumnLink cl= new ColumnLink( cids);

                // or them
                Enumeration enu= st.keys();// key is just value
                Expression expr1, expr=null;
                while( enu.hasMoreElements() ){
                    sql=(String) enu.nextElement(); // the sqlfilter is xml=Expression.toString() with CDATA indeed.
                    String[] v=(String[]) st.get(sql);
                    desc=v[0];
                    tn= v[1];
                    //expr1= new Expression(cl, sql,desc);
                    if( tableName.equalsIgnoreCase(tn)|| nds.util.Validator.isNull(tn)) 
                    	expr1=new Expression(sql); // xml
                    else
                    	expr1=constructExistsExpr(sql, desc, table, tn, qs);
                    if(expr1!=null && !expr1.isEmpty()){
                    	if( expr==null) expr= expr1;
                    	else expr= expr.combine(expr1,SQLCombination.SQL_OR, null); // or relationship
                    }
                    logger.debug("expr="+ expr);
                }
                return expr;
            }else{
                logger.debug("getSecurityFilter("+ tableName+","+ userId+","+permission+")= empty");
                //return new Expression(null, "1=1","1=1");
                return Expression.EMPTY_EXPRESSION;
            }
        }catch(SQLException e){
        	throw new QueryException("Error when checking security",e);
        }finally{
            if( rs !=null) try{rs.close();}catch(Exception e){}
            if( pstmt !=null) try{pstmt.close();}catch(Exception e2){}
            if( con!=null) try{con.close();}catch(Exception e3){}
        }
    }
    /**
     * 2009-12-11 现在碰到这样一种情况：
     *    配置人员在复制视图的时候，将A表的directory复制到了B表上
     *    而AB表一般是同一个物理表的2个视图（即AB表是兄弟关系，而非原来考虑的头明细父子关系），
     *    现在，如果A 表定义的directory  设置了查询条件一般也针对A表，如 A表的c字段的查询范围是某某，如何让B表条件
     *    构造时变为 B表的c 字段（必定同名，如果没找到就报错）的查询范围是某某。
     *    
     *    以前在查询到这种情况时，会报告："Could not found relation ship from table " 
    			+ dirTableName +" to sub table " + table
				+ ", while these two tables have same security driectory." 现在要求自动识别并转换
				
     *    
     * @param sql used to construct DIR_FILTER, stored in directory.sqlfilter, working on dirTable
     * @param desc sql description
     * @param table the table that return expression will put on, this one is what we want to query
     * @param dirTableName DIR_TABLE 
     * @return Expression
     *  标准格式：exists (select ID from <DIR_TABLE> WHERE <RELATIONSHIP> AND
     *   <DIR_FILTER>)
     *   
     */
    private static Expression constructExistsExpr(String sql, String desc, Table table, String dirTableName,QuerySession qsession) throws QueryException{
    	Expression expr=null;
    	Expression dirFilter=new Expression(sql);
    	dirFilter.setDescription(desc);
    	TableManager manager=TableManager.getInstance();
    	Table dirTable=manager.getTable(dirTableName);
    	if(dirTable==null)throw new QueryException("error in directory setting, table " + dirTableName +" not found");
    	// find relationship between dirTable and table
    	// dirTable will be the main table( think it as order, and table as order line)
    	// relationship would like "dirTable.id=table.refbycolumnId"
    	RefByTable rbt=null;
    	for(Iterator it= dirTable.getRefByTables().iterator();it.hasNext();){
    		rbt= (RefByTable) it.next();
    		if( rbt.getTableId() == table.getId() ){
    			//父子关系
    			Column rbc=  manager.getColumn(rbt.getRefByColumnId());
    			ColumnLink cl= new ColumnLink(new int[]{dirTable.getPrimaryKey().getId()});
    			expr=new Expression(cl, "=" + table.getName()+"." + rbc.getName(), null);
    		}
    	}
    	/* 2009-12-11 注释掉(yfzhu) 支持兄弟关系
    	 * if(expr ==null) throw new QueryException("Could not found relation ship from table " 
		+ dirTableName +" to sub table " + table
		+ ", while these two tables have same security driectory.");*/
    	if(expr==null){
    		//兄弟表，发现还有这种情况：两张兄弟表在数据库里定义了视图，而不是在AD_TABLE里定义视图，所以对于PORTAL来说，
    		//不知道兄弟关系的存在，我们只能默认:用户配置的权限字段，在dirTable上存在，应该也要在table上存在，如果不存在，
    		//也应该在数据库里存在（有些人会定义视图时遗漏此字段，这时报错可以让开发人员知道）
    		/**
    		 * 重构expr, 因为expr里的dirTableName不是table，而此条件实际上就是要应用到table上去的
    		 */
    		//偷个懒，不做字段的逐个比对了，直接替换，替换的方式：前后都不能是字母
    		String nsql=sql.replaceAll("\\b"+ dirTableName+"\\b", table.getName());
    		logger.debug("brother relationship found:from "+ sql);
    		logger.debug("replaced to "+ nsql);
    		expr=new Expression(nsql);
    		return expr;// return directly 
    	}else{
    		expr= expr.combine(dirFilter, SQLCombination.SQL_AND, MessagesHolder.getInstance().getMessage(qsession.getLocale(), "correspond-") 
    			+ dirTable.getDescription(qsession.getLocale())+" "+MessagesHolder.getInstance().getMessage(qsession.getLocale(), "-satisfy-")+ desc +")");
    	}
    	QueryRequestImpl query=QueryEngine.getInstance().createRequest(qsession);
    	query.setMainTable(dirTable.getId());
    	query.addSelection(dirTable.getPrimaryKey().getId());
    	query.addParam(expr);
    	String exSQL=query.toSQL();
    	return new Expression(null, "exists (" + exSQL +")" , MessagesHolder.getInstance().getMessage(qsession.getLocale(), "-exists-")+ "(" + expr.getDescription()+")");
    }
    public static User getUser(String lportalUserId){
		String uName, adclientName ;
		int p=lportalUserId.indexOf("@");
		if ( p>0){
			 uName= lportalUserId.substring(0,p );
			 adclientName= lportalUserId.substring(p+1);
			 return getUser(uName, adclientName);
		}
    	User usr=new User();
    	usr.setId(new Integer(-1));
    	return usr;
    }
    /**
     * return -1 if not found
     * @param userNameOrEmail, whether email or user name is base upon 
     * 	nds.control.web.WebUtils#isServerRunInSingleCompanyMode
     * when simple company mode, userNameOrEmail is email, else, user name
     * @param clientName
     * @return
     */
    public static  User getUser(String userNameOrEmail, String clientName){
    	User usr=new User();
    	usr.setId(new Integer(-1));
    	Connection con= null;;
    	ResultSet rs=null;
    	PreparedStatement pstmt=null;
    	try{
    		con=nds.query.QueryEngine.getInstance().getConnection();
    		pstmt=con.prepareStatement(nds.control.web.WebUtils.isServerRunInSingleCompanyMode()?
    				GET_USER_BY_EMAIL_AND_CLIENT:GET_USER_BY_NAME_AND_CLIENT);
	    	pstmt.setString(1, userNameOrEmail);
	    	pstmt.setString(2, clientName);
	    	rs=pstmt.executeQuery();
	    	if(rs.next()){
	    		usr.setId(new Integer( rs.getInt(1)));
	        	usr.setName(rs.getString(2));
	    		usr.setActive(Tools.getYesNo( rs.getString(3), true));
	        	usr.setClientDomain(clientName);
	    		usr.adClientId=rs.getInt(4);
	    		usr.adOrgId =rs.getInt(5);
	    		usr.locale = getLocale( rs.getString(6) );
	    		usr.clientDomainName= rs.getString(7);
	    		usr.setIsAdmin(rs.getInt(8));
	    	}
    	}catch(Exception e){
    		logger.error("Could not fetch user according to userNameOrEmail="+ userNameOrEmail+
    				", client="+ clientName+" with isServerRunInSingleCompanyMode="+
    				nds.control.web.WebUtils.isServerRunInSingleCompanyMode(), e);
    	}finally{
    		if(rs!=null)try{rs.close();}catch(Exception e2){}
    		if(pstmt!=null)try{pstmt.close();}catch(Exception e2){}
    		if(con!=null)try{con.close();}catch(Exception e2){}
    	}
    	return usr;
    	
    }
    public static  User getUser(int userId) throws Exception{
    	User usr=new User();
    	usr.setId(new Integer(userId));
    	Connection con= null;;
    	ResultSet rs=null;
    	PreparedStatement pstmt=null;
    	try{
    		con=nds.query.QueryEngine.getInstance().getConnection();
    		pstmt=con.prepareStatement(GET_USER_BY_ID);
	    	pstmt.setInt(1, userId);
	    	rs=pstmt.executeQuery();
	    	if(rs.next()){
	    		usr.setName(rs.getString(1));
	    		usr.setClientDomain(rs.getString(2));
	    		usr.setActive(Tools.getYesNo( rs.getString(3), true));
	    		usr.setDescription(rs.getString(4));
	    		usr.adClientId= rs.getInt(5);
	    		usr.adOrgId = rs.getInt(6);
	    		usr.locale = getLocale( rs.getString(7) );
	    		usr.clientDomainName= rs.getString(8);
	    		usr.setIsAdmin(rs.getInt(9));
	    	}
    	}/*catch(Exception e){
    		logger.error("Could not fetch user according to user id="+ userId, e);
    	}*/finally{
    		if(rs!=null)try{rs.close();}catch(Exception e2){}
    		if(pstmt!=null)try{pstmt.close();}catch(Exception e2){}
    		if(con!=null)try{con.close();}catch(Exception e2){}
    	}
    	return usr;
    	
    }
    /**
     * 
     * @param loc
     * @return locale or default from tablemanager
     */
    private static Locale getLocale(String loc){
    	try{
    		if(Validator.isNull(loc)) return TableManager.getInstance().getDefaultLocale();
    		return Tools.getLocale(loc);
    	}catch(Throwable t){
    		logger.error("Locale "+ loc +" is not valid");
    		return TableManager.getInstance().getDefaultLocale();
    	}
    }
    
}
