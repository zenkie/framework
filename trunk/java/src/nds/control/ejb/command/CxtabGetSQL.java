/*
 * Agile Control Technologies Ltd,. CO.
 * http://www.agileControl.com
 */
package nds.control.ejb.command;
import java.io.File;
import java.rmi.RemoteException;
import java.util.*;
import java.sql.*;

import nds.control.ejb.Command;
import nds.control.event.DefaultWebEvent;
import nds.control.util.SecurityUtils;
import nds.control.util.ValueHolder;
import nds.query.*;
import nds.schema.*;
import nds.report.ReportTools;
import nds.security.Directory;
import nds.security.User;
import nds.util.*;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.export.*;
import net.sf.jasperreports.engine.util.JRLoader;

/**
 * 根据当前Cxtab的定义构造标准SQL语句，只针对指定了AD_TABLE_ID的CXTAB
 * 构造中的行列维度和度量将构成SELECT，WHERE条件通过虚拟方式构造
 * @author yfzhu@agilecontrol.com
 */

public class CxtabGetSQL extends Command{
	/**
	 * @param event parameters:
	 *    objectid - object id of ad_cxtab table		
	 */
    public ValueHolder execute(DefaultWebEvent event) throws NDSException, RemoteException{
    	logger.debug(event.toDetailString());
    	User usr=helper.getOperator(event);
    	int cxtabId=Tools.getInt( event.getParameterValue("objectid",true), -1);
    	if(!usr.name.equals("root"))
    		if (!nds.control.util.SecurityUtils.hasObjectPermission(usr.id.intValue(), usr.name, "AD_CXTAB", cxtabId, nds.security.Directory.WRITE, event.getQuerySession()))
    			throw new NDSException("@no-permission@");
    	
    	ValueHolder holder=new ValueHolder();
    	String message;
    	java.sql.Connection conn=QueryEngine.getInstance().getConnection();
    	try{
    		message=prepareReport(cxtabId, usr, conn);
    		holder.put("code","0");
    	}catch(Throwable e){
    		logger.error("User "+ usr.getName() + "@" + usr.getClientDomain()+" fail to refresh jreport:"+ cxtabId, e);
    		message="@jreport-found-error@:"+ e.getMessage();
    		throw new NDSException(message);
    	}finally{
    		try{
    			conn.close();
    		}catch(Exception e){}
    	}
		holder.put("message", message);
		holder.put("code", new Integer(0));//no change for current page		
    	return holder;
    }
    /**
	 * Create QueryRequest, if not on ad_table, return null;
	 * @param conn
	 * @throws Exception
	 */
	private String prepareReport(int cxtabId, User user,Connection conn) throws Exception{
        boolean isOnHTML=false;//isOnHTML when for html report, dimension that set "hidehtml"="N" will not queried
		QueryEngine engine=QueryEngine.getInstance();
		TableManager manager=TableManager.getInstance();
		String sql;
		List ed= engine.doQueryList("select ad_table_id,name from ad_cxtab where id="+ cxtabId, conn);
		int factTableId= Tools.getInt(((List)ed.get(0)).get(0),-1);
		
		if(factTableId==-1) return null;
		
		String cxtabDesc=(String) ((List)ed.get(0)).get(1);
		/*int factTableId= Tools.getInt(engine.doQueryOne(
				"select ad_table_id from ad_cxtab where id="+ cxtabId, conn), -1);*/
		Table factTable= manager.getTable(factTableId);
		
		List dimensionsH= engine.doQueryList("select columnlink, description, measure_order, hidehtml from ad_cxtab_dimension where ad_cxtab_id="+
				cxtabId+" and isactive='Y' and position_='H' "+ (isOnHTML?"and hidehtml='N'":"") +" order by orderno asc", conn);
		List dimensionsV= engine.doQueryList("select columnlink, description, measure_order,hidehtml from ad_cxtab_dimension where ad_cxtab_id="+
				cxtabId+" and isactive='Y' and position_='V' "+ (isOnHTML?"and hidehtml='N'":"") +" order by orderno asc", conn);
		
		// filter will be added to where clause directly
		String cxtabFilter= (String)engine.doQueryOne("select filter from ad_cxtab where id="+cxtabId, conn);
		
		
		
		Locale locale= user.locale;
		logger.debug("Locale for "+ user.getNameWithDomain()+"(id="+ user.id+") is "+ locale);
		QuerySession qsession= QueryUtils.createQuerySession(user.id.intValue(),user.getSecurityGrade(), "", user.locale);
		QueryRequestImpl query=engine.createRequest(qsession);
		query.setMainTable(factTableId,true, cxtabFilter);

		//select
		if(dimensionsH!=null && dimensionsH.size()>0)for(int i=0;i< dimensionsH.size();i++){
			List dim= (List)dimensionsH.get(i);
			ColumnLink cl=new ColumnLink((String) dim.get(0));
			query.addSelection(cl.getColumnIDs(), false, (String)dim.get(1));
		}
		if(dimensionsV!=null && dimensionsV.size()>0)for(int i=0;i< dimensionsV.size();i++){
			List dim= (List)dimensionsV.get(i);
			ColumnLink cl=new ColumnLink((String) dim.get(0));
			query.addSelection(cl.getColumnIDs(), false, (String)dim.get(1));
		}


		List measures=  engine.doQueryList("select ad_column_id, function_, userfact, description, VALUEFORMAT,valuename, param1,param2,param2 from ad_cxtab_fact where ad_cxtab_id="+
				cxtabId+" and isactive='Y' order by orderno asc",conn);
        //和平均有关的函数，包括avg, var,stdev，都不能让数据库进行group by 操作
        //而计数，最大，最小，累计等，可以先使用数据库完成有关group by运算
		//注意计算列 (以等号开头)的将不参与前期运算
        ArrayList facts=new ArrayList();
        ArrayList factDescs=new ArrayList();
        
        boolean isDBGroupByEnabled=true;
        boolean mustBeDBGroupBy=false;
        for(int i=0;i< measures.size();i++){
        	List mea= (List)measures.get(i);
        	String userFact= (String)mea.get(2);
        	if(Validator.isNotNull(userFact)){
        		if( userFact.startsWith("=")) continue;
        		// user fact 用于构造group by 语句，user fact 一般是一个汇总函数,如 count(distinct id)
        		facts.add(userFact);
        		factDescs.add(mea.get(3));
        		mustBeDBGroupBy=true;
        	}else{
            	String function= (String)mea.get(1);
        		int colId= Tools.getInt(mea.get(0),-1);
        		Column col= TableManager.getInstance().getColumn(colId);
        		
        		if(nds.jcrosstab.fun.FunUtil.isValidGroupByFunction(function)){
            		facts.add( function+"("+ factTable.getName()+"."+col.getName() + ")");
            		factDescs.add(mea.get(3));

        		}else{
        			isDBGroupByEnabled=false;
        		}
        	}
        }
        if(isDBGroupByEnabled){
        	sql= query.toGroupBySQL(facts );
        }else{
        	if(mustBeDBGroupBy) throw new NDSException("Cxtab configuration error, found user fact(db group by function) and invalid db group by function (e.g. avg) in the same time");
        	for(int i=0;i< measures.size();i++){
            	List mea= (List)measures.get(i);
            	// may not have user fact 
           		int colId= Tools.getInt(mea.get(0),-1);
           		Column col= TableManager.getInstance().getColumn(colId);
           		if(col!=null)query.addSelection( colId );
            }
        	sql= query.toSQL();
        }
		return sql;
	}
	    
}
