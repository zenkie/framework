package nds.web.shell;

import javax.servlet.http.HttpServletResponse;

import nds.log.Logger;
import nds.log.LoggerManager;
import nds.query.*;
import nds.schema.*;
import nds.util.Tools;
import nds.control.util.SecurityUtils;
import nds.control.web.*;
import org.json.*;

import com.liferay.util.Validator;
import nds.util.*;
import argparser.ArgParser;
import java.util.*;
/**
 * 
 * Work as command line in portal
 * 
 * @author yfzhu
 *
 */
public abstract class AbstractShellCmd implements ShellCmd{
	
	protected Logger logger=LoggerManager.getInstance().getLogger(getClass().getName());
	
	
	protected String[] loadIds(Table table,String queryString,boolean fuzzy, int dirPerm, int range,
			UserWebImpl userWeb ) throws Exception{
		QueryRequest quest= parseQuery(table, queryString,fuzzy, dirPerm, range, userWeb);
		List al= QueryEngine.getInstance().doQueryList(quest.toSQL());
		String[] ids=new String[al.size()];
		for(int i=0;i<al.size();i++){
			ids[i]= al.get(i).toString();
		}
		return ids;
	}
	/**
	 * Load first id or -1 if not find
	 * @param table
	 * @param queryString
	 * @param dirPerm
	 * @param range
	 * @param userWeb
	 * @return
	 * @throws Exception
	 */
	protected int loadFirstId(Table table,String queryString,boolean fuzzy, int dirPerm, int range,
			UserWebImpl userWeb ) throws Exception{
		QueryRequest quest= parseQuery(table, queryString, fuzzy,dirPerm, 1, userWeb);
		return Tools.getInt( QueryEngine.getInstance().doQueryOne(quest.toSQLWithRange()),-1);
	}
	
	/**
	 * id will be selected
	 * 
	 * @param table
	 * @param queryString query 参数指查询内容，用于定位数据行。查询内容以逗号分隔，
	 * 	每个元素都可以是表的id/ak或ak2内容，或其他字段，这些字段可以通过ad_table的扩展属性 query_columns 定义，
	 * 	详见下文 。只有元素可以被解析为整数，才会作为id 进行查找。
	 *  关于 query_columns， 定义除了AK 和 AK2 字段外其他的当前表字段作为查询条件。
	 *  当界面上通过模糊条件定义搜索记录的时候，这些字段也将作为搜索的字段内容。
	 *  此功能首次开发主要用于shell 命令中查询语句的构造，例如delete, query 这些命令都涉及到。
		元素为字符串，有效的ColumnLink，不需书写主表名称。
	 *  
	 *  如查询内容为 * ，则表示全查
	 * @param dirPerm nds.security.Directory#READ, WRITE,SUBMIT so on
	 * @param fuzzy query element should be identical or fuzzy 
	 * @param userWeb
	 * @return 
	 * @throws Exception
	 */
	protected QueryRequestImpl parseQuery(Table table,String queryString,boolean fuzzy, int dirPerm,int range,
			UserWebImpl userWeb ) throws Exception{
		if(Validator.isNull(queryString)){
			throw new NDSException("@cmd-argument-error@: query is empty");
		}
		QueryRequestImpl query=QueryEngine.getInstance().createRequest(userWeb.getSession());
		query.setMainTable(table.getId());
		query.addSelection(table.getPrimaryKey().getId());
		//query.addSelection(table.getAlternateKey().getId());
		
		// security
		//security param
		// directory perm
		Expression expr=SecurityUtils.getSecurityFilter(table.getName(), dirPerm, 
				userWeb.getUserId(), userWeb.getSession());
		
		Expression expr2;
		if(dirPerm==nds.security.Directory.WRITE){
			// try filter status column for only status=1 rows
			Column column= table.getColumn("status");
	    	if ( column!=null){
	    		ColumnLink cl=new ColumnLink(new int[]{column.getId()});
	    		expr2= new Expression(cl,"=1", "not-submit");
	        	expr=expr2.combine(expr, SQLCombination.SQL_AND,null);
	    	}
		}		
		//order by id desc
		query.addOrderBy(new int[]{table.getPrimaryKey().getId()}, false);
		
		//range
		query.setRange(0, range);
		
		
		if(queryString.trim().equals("*")){
			if(fuzzy)
				throw new NDSException("@cmd-argument-error@: query is fuzzy");
			query.addParam(expr);
			return query;
		}
		String[] qs= queryString.split("[,， ]");
		JSONArray ja= table.getJSONProps()==null?null:table.getJSONProps().optJSONArray("query_columns");
		if(ja==null)ja=new JSONArray();
		boolean idIsAk= (table.getAlternateKey().getId()==table.getPrimaryKey().getId());
		ja.put(table.getAlternateKey().getName());
		if(table.getAlternateKey2()!=null)ja.put(table.getAlternateKey2().getName());
		// search column by column
		expr2=null;
		Expression expr3;
		for(int i=0;i<ja.length();i++){
			String cls= ja.getString(i);
			for(int j=0;j< qs.length;j++){
				if(Validator.isNotNull(qs[j])){
					expr3= new Expression( new ColumnLink(table.getName()+"."+ cls),
							fuzzy?qs[j]:"="+qs[j],null);
					expr2= expr3.combine(expr2, Expression.SQL_OR, null);
				}
			}
		}
		// id query
		for(int j=0;j< qs.length;j++){
			if(Validator.isNotNull(qs[j])){
				try{
					int qid= Integer.parseInt(qs[j]);
					if(qid>0){
						expr3= new Expression( new ColumnLink(table.getName()+".ID"),
								"="+qs[j],null);
						expr2= expr3.combine(expr2, Expression.SQL_OR, null);
					}
				}catch(Throwable t){}
				
			}
		}
		
		if(expr!=null){
			expr= expr.combine(expr2, Expression.SQL_AND,null);
		}else expr= expr2;
		query.addParam(expr);
		
		return query ;
	}
	
}
