package nds.query.web;

import java.util.*;
import java.sql.*;
import javax.servlet.http.HttpServletRequest;

import nds.query.*;
import nds.control.web.*;
import nds.log.Logger;
import nds.log.LoggerManager;
import nds.schema.*;
import nds.security.NDSSecurityException;
import nds.util.*;

/**
 * Create Sub System view for user
 * @author yfzhu
 *
 */
public class SubSystemView {
	private static Logger logger=LoggerManager.getInstance().getLogger(SubSystemView.class.getName());
	 
	/**
	 * People can view the specified subsystem
	 */
	public final static int PERMISSION_VIEWABLE=0; 
	/**
	 * People has no permission on this subsystem
	 */
	public final static int PERMISSION_NO_PERM=1; 
	/**
	 * Company not buy the license for the subsystem
	 */
	public final static int PERMISSION_NO_LICENSE=2; 
	
	
	private List<SubSystem> subSystemNoLicense=null;//elements are SubSystem(not in TableManager)
	
	public SubSystemView(){}
	
	/**
	 * 
	 * @param request
	 * @param permissionType PERMISSION_VIEWABLE, PERMISSION_NO_PERM or PERMISSION_NO_LICENSE
	 * @return never null, elements are nds.schema.SubSystem
	 */
	public List<SubSystem> getSubSystems(HttpServletRequest request, int permissionType  ) throws Exception{
		if(permissionType==PERMISSION_VIEWABLE) return getSubSystems(request);

		UserWebImpl userWeb= ((UserWebImpl)WebUtils.getSessionContextManager(request.getSession()).getActor(nds.util.WebKeys.USER));
		
		String subsystems=(String)QueryEngine.getInstance().doQueryOne("SELECT subsystems from users where id="+userWeb.getUserId());
		if (Validator.isNotNull(subsystems))
		{
			return Collections.EMPTY_LIST;
		}
		TableManager manager=TableManager.getInstance();
	 
		
		if(permissionType==PERMISSION_NO_PERM){
			ArrayList subs=new ArrayList();
			List al= (List)userWeb.getProperty("subsystems");// elements are subystem.id
			if (al==null){
				getSubSystems(request);
				al=(List)userWeb.getProperty("subsystems");
			}
//			 no perm
			List ss =manager.getSubSystems();
			for(int i=0;i< ss.size();i++){
				SubSystem sa= (SubSystem)ss.get(i);
				boolean found=false;
				for(int j=0;j< al.size();j++){
					if( ((Integer)al.get(j)).intValue() == sa.getId() ){
						found=true;
						break;
					}
				}
				if(!found) subs.add(sa);
			}
			return subs;
			
		}//else{
			// no license
			if(subSystemNoLicense==null){
				subSystemNoLicense=new ArrayList<SubSystem>();
				List al=QueryEngine.getInstance().doQueryList("select id, name, orderno, iconurl,url from ad_subsystem s where exists(select 1 from ad_tablecategory c where c.ad_subsystem_id=s.id) order by orderno asc");
				for(int i=0;i<al.size();i++){
					List als= (List)al.get(i);
					if( manager.getSubSystem( Tools.getInt( als.get(0), -1) ) ==null  ){
						SubSystem ss=new SubSystem();
						ss.setId(Tools.getInt( als.get(0), -1) );
						ss.setName((String)als.get(1));
						ss.setOrderno(Tools.getInt( als.get(2), -1));
						ss.setIconURL((String)als.get(3));
						ss.setPageURL((String)als.get(4));
						subSystemNoLicense.add(ss);
					}
				}
			}
			return subSystemNoLicense;
		//}
		
		
	}
	/**
	 * Get viewable subsystem list
	 * @param request
	 * @return never null, elements are nds.schema.SubSystem
	 */
	public List getSubSystems(HttpServletRequest request){
		UserWebImpl userWeb= ((UserWebImpl)WebUtils.getSessionContextManager(request.getSession()).getActor(nds.util.WebKeys.USER));
		ArrayList subs=new ArrayList();
		if(userWeb.getUserId()==userWeb.GUEST_ID){
			return subs;
		}
		List al= (List)userWeb.getProperty("subsystems");// elements are subystem.id
		TableManager manager=TableManager.getInstance();
		if(al!=null){
			
			for(int i=0;i< al.size();i++){
				int sid= ((Integer)al.get(i)).intValue();
				SubSystem ss= manager.getSubSystem(sid );
				if(ss!=null)
					subs.add(ss);
			}
		}else{
			// search all tablecategoris for subsystem
			// add users subsystems param
			al=new ArrayList();
			String[] sub_list;
			try
			{
				String subsystems=(String)QueryEngine.getInstance().doQueryOne("SELECT subsystems from users where id="+userWeb.getUserId());
				if (Validator.isNotNull(subsystems)){
					sub_list = subsystems.split(",");
					for (int m = 0; m < sub_list.length; m++)
					{
						String subname= (String)sub_list[m].trim();
						SubSystem usersub = manager.getSubSystem(subname);
						if (usersub != null)
						{
							
							  al.add(new Integer(usersub.getId()));
							  subs.add(usersub);
						}
					}
					userWeb.setProperty("subsystems", al);
					return subs;
				}
			}
			catch (QueryException e) {
				logger.error("Fail to load subsystems from users", e);
			}			
			
			
			
			for(int i=0;i< manager.getSubSystems().size();i++){
				SubSystem ss=(SubSystem) manager.getSubSystems().get(i);
				if(containsViewableChildren(request, ss)){
					al.add(new Integer(ss.getId()));
					subs.add(ss);
				}
			}
			userWeb.setProperty("subsystems",al);
		}
		return subs;
	}
	/**
	 * 
	 * @param request
	 * @param subSystemId
	 * @return
	 */
	private boolean containsViewableActions(HttpServletRequest request, SubSystem ss){
		List<WebAction> list=ss.getWebActions();
    	Connection conn=null;
    	try{
		UserWebImpl userWeb= ((UserWebImpl)WebUtils.getSessionContextManager(request.getSession()).getActor(nds.util.WebKeys.USER));
    	conn=QueryEngine.getInstance().getConnection();
    	HashMap webActionEnv=new HashMap();
    	webActionEnv.put("connection",conn);
    	webActionEnv.put("httpservletrequest",request);
    	webActionEnv.put("userweb",userWeb);
		
		for(int i=0;i<list.size();i++){
			WebAction wa=list.get(i);
			if(wa.canDisplay(webActionEnv)){
				return true;
			}
		}
    	}catch(Throwable t){
    		logger.error("Fail to load subsystem webaction", t);
    	}finally{
    		try{if(conn!=null)conn.close();}catch(Throwable te){}
    	}
    	return false;
	}
	/**
	 * if contains one category (and at least one table is menu object), will return true;
	 * @param request
	 * @param subSystemId
	 * @return
	 */
	private boolean containsViewableChildren(HttpServletRequest request, SubSystem ss){
//		 Create categories and their tables in hashtable
		TableManager manager=TableManager.getInstance();
        //Iterator tables = manager.getAllTables().iterator();
        //Hashtable categories = new Hashtable(50,20); // key:Integer(category id), values :List of table
        Integer tableCategoryId;Table table ;
        WebAction action;
        ArrayList cats = new ArrayList();
        Connection conn= null;
        try{
        	UserWebImpl userWeb= ((UserWebImpl)WebUtils.getSessionContextManager(request.getSession()).getActor(nds.util.WebKeys.USER));
        	conn=QueryEngine.getInstance().getConnection();
        	HashMap webActionEnv=new HashMap();
        	webActionEnv.put("connection",conn);
        	webActionEnv.put("httpservletrequest",request);
        	webActionEnv.put("userweb",userWeb);
        	
	        List categories= ss.children();
	        for(int i=0;i<categories.size();i++ ){
	        	Object o= categories.get(i); // TableCategory or WebAction
	        	if(o instanceof TableCategory){
		        	TableCategory tc= (TableCategory)o;
		        	List children= tc.children();
		        	ArrayList catschild= new ArrayList();
		        	for(int j=0;j< children.size();j++){
		        		if(children.get(j) instanceof Table){
		        			table=(Table)children.get(j);
		        			if(!table.isMenuObject()){
		                    	continue;
		                    }
		        			try{
		                        WebUtils.checkTableQueryPermission(table.getName(),request );
		                    }catch(NDSSecurityException e){
		                        continue;
		                    }
		                    // table is ok for current user to list
		                    return true;
		        		}else if(children.get(j) instanceof WebAction){
		        			action=(WebAction)children.get(j);
		        			if(action.canDisplay(webActionEnv))
		        				return true;
		        		}else{
		        			throw new NDSRuntimeException("Unsupported element in TableCategory children:"+ 
		        					children.get(j).getClass());
		        			
		        		}
		        	}
		        	
	        	}else if(o instanceof WebAction){
	        		if(((WebAction)o).canDisplay(webActionEnv)){
		        		return true;
	        		}
	        	}else{
	        		throw new NDSException("Unexpected class in subsystem (id="+ ss.getId()+"), class is "+ o.getClass());
	        	}
	        	
	        }
        }catch(Throwable t){
        	logger.error("Fail to load subsystem tree", t);
        }finally{
        	try{if(conn!=null)conn.close();}catch(Throwable e){}
        }
        
        return false;            
	}

	/**
	 * 事实表和关联报表属于当前传入数组的交叉报表
	 * @param request
	 * @return elements are ArrayList, first is cxtab id, second is cxtab name
	 */
	public List getCxtabs(HttpServletRequest request, int tableCategoryId){
		List tables= getChildrenOfTableCategory(request, tableCategoryId,false);
		ArrayList<Integer> al=new ArrayList();
		for(int i=0;i< tables.size();i++){
			al.add( ((Table)tables.get(i)).getId() );
		}
		return getCxtabs(request, al);
        
	}	
	/**
	 * 事实表和关联报表属于当前传入数组的交叉报表
	 * @param request
	 * @param tables elements are table.id
	 * @return elements are ArrayList, first is cxtab id, second is cxtab name
	 */
	public List getCxtabs(HttpServletRequest request, List<Integer> tables){
		TableManager manager=TableManager.getInstance();
        UserWebImpl userWeb= ((UserWebImpl)WebUtils.getSessionContextManager(request.getSession()).getActor(nds.util.WebKeys.USER));
        StringBuffer sb=new StringBuffer();
        for(int i=0;i<tables.size();i++){
        	//Table t= tables.get(i);
        	if(i>0 )sb.append(",");
        	sb.append(tables.get(i));
        }
        String ts= sb.toString();
		try{
	        Table cxtabTable= manager.getTable("AD_CXTAB"); 
	    	QueryRequestImpl queryData;
	    	// only pk,dk will be selected, order by ak asc
	    	queryData=QueryEngine.getInstance().createRequest(userWeb.getSession());
	    	queryData.setMainTable(cxtabTable.getId());
	    	
	    	queryData.addSelection(cxtabTable.getPrimaryKey().getId());
	    	queryData.addSelection(cxtabTable.getDisplayKey().getId());
	
	    	Column colOrderNo=cxtabTable.getColumn("orderno") ;
	    	queryData.setOrderBy( new int[]{ colOrderNo.getId()}, true);
	    	queryData.setRange(0, Integer.MAX_VALUE);
	    	
	    	Expression expr= new Expression(null, "(AD_CXTAB.AD_TABLE_ID in ("+ts+
	    			") or exists (select 1 from ad_cxtab_reftable r where r.ad_cxtab_id=AD_CXTAB.id and r.ad_table_id in ("+
	    			ts+")))",null);
	    	
	    	
	    	//set reporttype to "S"
	    	expr=expr.combine(new Expression(new ColumnLink("AD_CXTAB.REPORTTYPE"), "=S",null), SQLCombination.SQL_AND,null);
	    	expr=expr.combine(new Expression(new ColumnLink("AD_CXTAB.ISACTIVE"), "=Y",null), SQLCombination.SQL_AND,null);
	    	expr=expr.combine(new Expression(new ColumnLink("AD_CXTAB.ISPUBLIC"), "=Y",null), SQLCombination.SQL_AND,null);
	    	expr=expr.combine(userWeb.getSecurityFilter(cxtabTable.getName(), 1) ,  SQLCombination.SQL_AND,null);
	    	queryData.addParam(expr);//read permission

    		return QueryEngine.getInstance().doQueryList(queryData.toSQL());
		}catch(Throwable t){
			logger.error("Fail to load reports for user "+userWeb.getUserId()+" with table ids: "+ ts, t);
		}
		return Collections.EMPTY_LIST;
        
	}	
	/**
	 * 事实表属于当前传入数组的交叉报表
	 * @param request
	 * @param tables elements are table.id
	 * @return elements are ArrayList, first is cxtab id, second is cxtab name
	 */
	/*public List getCxtabs(HttpServletRequest request, List<Integer> tables){
		TableManager manager=TableManager.getInstance();
        UserWebImpl userWeb= ((UserWebImpl)WebUtils.getSessionContextManager(request.getSession()).getActor(nds.util.WebKeys.USER));
        StringBuffer ts=new StringBuffer();
        for(int i=0;i<tables.size();i++){
        	//Table t= tables.get(i);
        	if(i>0 )ts.append(",");
        	ts.append(tables.get(i));
        }
		try{
	        Table cxtabTable= manager.getTable("AD_CXTAB"); 
	    	QueryRequestImpl queryData;
	    	// only pk,dk will be selected, order by ak asc
	    	queryData=QueryEngine.getInstance().createRequest(userWeb.getSession());
	    	queryData.setMainTable(cxtabTable.getId());
	    	
	    	queryData.addSelection(cxtabTable.getPrimaryKey().getId());
	    	queryData.addSelection(cxtabTable.getDisplayKey().getId());
	
	    	Column colOrderNo=cxtabTable.getColumn("orderno") ;
	    	queryData.setOrderBy( new int[]{ colOrderNo.getId()}, true);
	    	queryData.setRange(0, Integer.MAX_VALUE);
	    	
	    	Expression expr= new Expression(new ColumnLink("AD_CXTAB.AD_TABLE_ID")," IN("+ts+")" ,null);
	    	
	    	//set reporttype to "S"
	    	expr=expr.combine(new Expression(new ColumnLink("AD_CXTAB.REPORTTYPE"), "=S",null), SQLCombination.SQL_AND,null);
	    	expr=expr.combine(new Expression(new ColumnLink("AD_CXTAB.ISACTIVE"), "=Y",null), SQLCombination.SQL_AND,null);
	    	expr=expr.combine(new Expression(new ColumnLink("AD_CXTAB.ISPUBLIC"), "=Y",null), SQLCombination.SQL_AND,null);
	    	expr=expr.combine(userWeb.getSecurityFilter(cxtabTable.getName(), 1) ,  SQLCombination.SQL_AND,null);
	    	queryData.addParam(expr);//read permission

    		return QueryEngine.getInstance().doQueryList(queryData.toSQL());
		}catch(Throwable t){
			logger.error("Fail to load reports for user "+userWeb.getUserId()+" with table ids: "+ ts, t);
		}
		return Collections.EMPTY_LIST;
        
	}*/
	
	/**
	 * 
	 * @param request
	 * @param tableCategoryId
	 * @paqram includeAction if true, will load webactions also
	 * @return elements are Table or WebAction
	 */
	public List getChildrenOfTableCategory(HttpServletRequest request, int tableCategoryId, boolean includeAction){
		TableManager manager=TableManager.getInstance();
		
		WebAction action;
        ArrayList cats = new ArrayList();
        Connection conn= null;
        HashMap webActionEnv=null;
        Table table;
        UserWebImpl userWeb= ((UserWebImpl)WebUtils.getSessionContextManager(request.getSession()).getActor(nds.util.WebKeys.USER));
        
    	
        TableCategory tc= manager.getTableCategory(tableCategoryId);
    	List children= tc.children();
    	ArrayList catschild= new ArrayList();
    	try{
            if(includeAction){
	    		conn=QueryEngine.getInstance().getConnection();
	        	webActionEnv=new HashMap();
	        	webActionEnv.put("connection",conn);
	        	webActionEnv.put("httpservletrequest",request);
	        	webActionEnv.put("userweb",userWeb);
            }
	    	for(int j=0;j< children.size();j++){
	    		if(children.get(j) instanceof Table){
	    			table=(Table)children.get(j);
	    			if(!table.isMenuObject()){
	                	continue;
	                }
	    			try{
	                    WebUtils.checkTableQueryPermission(table.getName(),request );
	                }catch(NDSSecurityException e){
	                    continue;
	                }
	                // table is ok for current user to list
	                catschild.add(table);
	    		}else if(children.get(j) instanceof WebAction){
	        		if(includeAction){	
		    			action=(WebAction)children.get(j);
		        		if(action.canDisplay(webActionEnv))
		        			catschild.add(action);
	        		}
	    		}else{
	    			throw new NDSRuntimeException("Unsupported element in TableCategory children:"+ 
	    					children.get(j).getClass());
	    			
	    		}
	    	}
    	}catch(Throwable t){
        	logger.error("Fail to load subsystem tree", t);
        }finally{
        	try{if(conn!=null)conn.close();}catch(Throwable e){}
        }
    	return catschild;
	}

	/**
	 * menu action
	 * @throws Exception  cyl
	 * @param request
	 * @param tableCategoryId desgin menu list
	 * @paqram includeAction if true, will load webactions also
	 * @return elements are Table or WebAction and menu list
	 */
	public List getChildrenOfTableCategorybymenu(HttpServletRequest request, int tableCategoryId, boolean includeAction) throws Exception{
		TableManager manager=TableManager.getInstance();
		
		WebAction action;
        ArrayList cats = new ArrayList();
        Connection conn= null;
        HashMap webActionEnv=null;
        Table table;
        List al=QueryEngine.getInstance().doQueryList("select e.id,e.name from ad_table g,AD_ACCORDION e where g.AD_ACCORDION_id=e.id and g.ad_tablecategory_id="+tableCategoryId+" group by e.id,e.name,e.orderno order by e.orderno asc");
        UserWebImpl userWeb= ((UserWebImpl)WebUtils.getSessionContextManager(request.getSession()).getActor(nds.util.WebKeys.USER));
        TableCategory tc= manager.getTableCategory(tableCategoryId);
    	List children= tc.children();
    	
    	//ArrayList prow= new ArrayList();
    	if(al.size()>0){
			for(int i=0;i<al.size();i++){
				List als= (List)al.get(i);
				int  ACCORDION=Tools.getInt( als.get(0), -1);
				logger.debug("ACCORDION~~~~~~~~~~"+String.valueOf(ACCORDION));
				ArrayList catschild= new ArrayList();
				String	ACCORDION_name=(String)als.get(1);
				try{
		            if(includeAction){
			    		conn=QueryEngine.getInstance().getConnection();
			        	webActionEnv=new HashMap();
			        	webActionEnv.put("connection",conn);
			        	webActionEnv.put("httpservletrequest",request);
			        	webActionEnv.put("userweb",userWeb);
		            }
			    	for(int j=0;j< children.size();j++){
			    		if(children.get(j) instanceof Table){
			    			table=(Table)children.get(j);
			    			logger.debug("getAccordid~~~~~~~~~~"+String.valueOf(table.getAccordid()));
			    			if(!table.isMenuObject()){
			                	continue;
			                }
			    			else if(ACCORDION!=table.getAccordid()){
			    				logger.debug(String.valueOf(ACCORDION)+"!="+String.valueOf(table.getAccordid()));
			    				continue;
			    			}
			    			try{
			                    WebUtils.checkTableQueryPermission(table.getName(),request );
			                }catch(NDSSecurityException e){
			                    continue;
			                }
			                // table is ok for current user to list
			                logger.debug(String.valueOf(ACCORDION)+"&&"+String.valueOf(table.getAccordid()));
			                catschild.add(table);
			    		}else if(children.get(j) instanceof WebAction){
			        		if(includeAction){	
				    			action=(WebAction)children.get(j);
				        		if(action.canDisplay(webActionEnv)&&(action.getAcordionId()==ACCORDION))
				        			logger.debug("add action"+String.valueOf(ACCORDION));
				        			catschild.add(action);
			        		}
			    		}else{
			    			throw new NDSRuntimeException("Unsupported element in TableCategory children:"+ 
			    					children.get(j).getClass());
			    			
			    		}
			    	}
		    	}catch(Throwable t){
		        	logger.error("Fail to load subsystem tree", t);
		        }finally{
		        	try{if(conn!=null)conn.close();}catch(Throwable e){}
		        }
	        	if(catschild.size()>0){
	        		// show this category
	        		ArrayList row= new ArrayList();
	                row.add(ACCORDION_name);
	                row.add(catschild);
	                cats.add(row);
	        	}
			}
			return cats;
    	}else{
    		ArrayList catschild1= new ArrayList();
    	try{
            if(includeAction){
	    		conn=QueryEngine.getInstance().getConnection();
	        	webActionEnv=new HashMap();
	        	webActionEnv.put("connection",conn);
	        	webActionEnv.put("httpservletrequest",request);
	        	webActionEnv.put("userweb",userWeb);
            }
	    	for(int j=0;j< children.size();j++){
	    		if(children.get(j) instanceof Table){
	    			table=(Table)children.get(j);
	    			if(!table.isMenuObject()){
	                	continue;
	                }
	    			try{
	                    WebUtils.checkTableQueryPermission(table.getName(),request );
	                }catch(NDSSecurityException e){
	                    continue;
	                }
	                // table is ok for current user to list
	                catschild1.add(table);
	    		}else if(children.get(j) instanceof WebAction){
	        		if(includeAction){	
		    			action=(WebAction)children.get(j);
		        		if(action.canDisplay(webActionEnv))
		        			catschild1.add(action);
	        		}
	    		}else{
	    			throw new NDSRuntimeException("Unsupported element in TableCategory children:"+ 
	    					children.get(j).getClass());
	    			
	    		}
	    	}
    	}catch(Throwable t){
        	logger.error("Fail to load subsystem tree", t);
        }finally{
        	try{if(conn!=null)conn.close();}catch(Throwable e){}
        }
    	if(catschild1.size()>0){
    		// show this category
    		ArrayList row= new ArrayList();
            row.add(tc.getName());
            row.add(catschild1);
            cats.add(row);
    	}
        }
    	return cats;
	}

	
	/**MU_FAVORITE
	 * @throws Exception  cyl
	 * @param request
	 * @return elements are Table or WebAction and menu list
	 * @paqram includeAction if true?not now
	 */
	
	public List getSubSystemsOfmufavorite(HttpServletRequest request) throws Exception{
		ArrayList mufavorite = new ArrayList();
		TableManager manager=TableManager.getInstance();
		//Table table;
		try{
		UserWebImpl userWeb= ((UserWebImpl)WebUtils.getSessionContextManager(request.getSession()).getActor(nds.util.WebKeys.USER));
		int userid=userWeb.getUserId();
		List al=QueryEngine.getInstance().doQueryList("select t.ad_table_id,t.menu_no,t.fa_menu,t.menu_re from MU_FAVORITE t where t.ownerid="+String.valueOf(userid)+" group by t.ad_table_id,t.menu_no,t.fa_menu,t.menu_re order by t.menu_no");		
		logger.debug("MU_FAVORITE size is "+String.valueOf(al.size()));
		if(al.size()>0){
			for(int i=0;i<al.size();i++){
				ArrayList catschild= new ArrayList();
				List als= (List)al.get(i);
				String	fa_menu=(String)als.get(1);
				String	menu_re=(String)als.get(2);
				int  table_id=Tools.getInt( als.get(0), -1);
				Table table=manager.getTable(table_id);
				logger.debug(table.getName());
				 /*
				if(!table.isMenuObject()){
                	continue;
                	//because many table is webaction not ismenuobject
                }*/
				try{
                    WebUtils.checkTableQueryPermission(table.getName(),request );
                  }catch(NDSSecurityException e){
                    continue;
                }

				catschild.add(table);
				logger.debug("add_table    ->"+table.getName());
		 
        	if(catschild.size()>0){
        		// show this category
        		ArrayList row= new ArrayList();
                row.add(fa_menu);
                row.add(menu_re);
                row.add(catschild);
                logger.debug("fa_menu    ->"+fa_menu);
                mufavorite.add(row);
        	    }
		      }
			}
        }catch(Throwable t){
        	logger.error("Fail to load mufavorite", t);
        }
		return mufavorite;
	}
	
	
	
	/**
	 * Return table categories and table that user has view permission
	 * @param request
	 * @param subSystemId
	 * @return never null, elements are List, containing 2 elements: 
	 * 		1)when first element is nds.schema.TableCategory, then second will be
	 * 		java.util.List (nds.schema.Table or nds.schema.WebAction)
	 * 		2) when first element is nds.schema.WebAction, then second is null 
	 */
	public List getTableCategories(HttpServletRequest request, int subSystemId){
		return getTableCategories(request, subSystemId, true);
	}
	/**
	 * Return table categories and table that user has view permission
	 * @param request
	 * @param subSystemId
	 * @return never null, elements are List, containing 2 elements: 
	 * 		1)when first element is nds.schema.TableCategory, then second will be
	 * 		java.util.List (nds.schema.Table or nds.schema.WebAction)
	 * 		2) when first element is nds.schema.WebAction, then second is null 
	 */
	public List getTableCategories(HttpServletRequest request, int subSystemId, boolean includeActions){
		// Create categories and their tables in hashtable
		TableManager manager=TableManager.getInstance();
        //Iterator tables = manager.getAllTables().iterator();
        //Hashtable categories = new Hashtable(50,20); // key:Integer(category id), values :List of table
        SubSystem ss;Integer tableCategoryId;Table table ;
        WebAction action;
        ArrayList cats = new ArrayList();
        Connection conn= null;
        HashMap webActionEnv=null;
        try{
        	UserWebImpl userWeb= ((UserWebImpl)WebUtils.getSessionContextManager(request.getSession()).getActor(nds.util.WebKeys.USER));
        	if(includeActions){
	        	conn=QueryEngine.getInstance().getConnection();
	        	webActionEnv=new HashMap();
	        	webActionEnv.put("connection",conn);
	        	webActionEnv.put("httpservletrequest",request);
	        	webActionEnv.put("userweb",userWeb);
        	}
	        List categories= manager.getSubSystem(subSystemId).children();
	        for(int i=0;i<categories.size();i++ ){
	        	Object o= categories.get(i); // TableCategory or WebAction
	        	if(o instanceof TableCategory){
		        	TableCategory tc= (TableCategory)o;
		        	List children= tc.children();
		        	ArrayList catschild= new ArrayList();
		        	for(int j=0;j< children.size();j++){
		        		if(children.get(j) instanceof Table){
		        			table=(Table)children.get(j);
		        			if(!table.isMenuObject()){
		                    	continue;
		                    }
		        			try{
		                        WebUtils.checkTableQueryPermission(table.getName(),request );
		                    }catch(NDSSecurityException e){
		                        continue;
		                    }
		                    // table is ok for current user to list
		                    catschild.add(table);
		        		}else if(children.get(j) instanceof WebAction){
		        			if(includeActions){
			        			action=(WebAction)children.get(j);
			        			if(action.canDisplay(webActionEnv))
			        				catschild.add(action);
		        			}
		        		}else{
		        			throw new NDSRuntimeException("Unsupported element in TableCategory children:"+ 
		        					children.get(j).getClass());
		        			
		        		}
		        	}
		        	if(catschild.size()>0){
		        		// show this category
		        		ArrayList row= new ArrayList();
		                row.add(tc);
		                row.add(catschild);
		                cats.add(row);
		        	}
	        	}else if(o instanceof WebAction){
	        		if(includeActions && ((WebAction)o).canDisplay(webActionEnv)){
		        		ArrayList row= new ArrayList();
		                row.add(o);
		                row.add(Collections.EMPTY_LIST );
		                cats.add(row);
	        		}
	        	}else{
	        		throw new NDSException("Unexpected class in subsystem (id="+ subSystemId+"), class is "+ o.getClass());
	        	}
	        	
	        }
        }catch(Throwable t){
        	logger.error("Fail to load subsystem tree", t);
        }finally{
        	try{if(conn!=null)conn.close();}catch(Throwable e){}
        }
        
        
        return cats;        
    }	
}
