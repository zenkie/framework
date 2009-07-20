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
	
	public SubSystemView(){}
	/**
	 * 
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
			al=new ArrayList();
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
	 * if contains one category, will return true;
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
	 * Return table categories and table that user has view permission
	 * @param request
	 * @param subSystemId
	 * @return never null, elements are List, containing 2 elements: 
	 * 		1)when first element is nds.schema.TableCategory, then second will be
	 * 		java.util.List (nds.schema.Table or nds.schema.WebAction)
	 * 		2) when first element is nds.schema.WebAction, then second is null 
	 */
	public List getTableCategories(HttpServletRequest request, int subSystemId){
		// Create categories and their tables in hashtable
		TableManager manager=TableManager.getInstance();
        //Iterator tables = manager.getAllTables().iterator();
        //Hashtable categories = new Hashtable(50,20); // key:Integer(category id), values :List of table
        SubSystem ss;Integer tableCategoryId;Table table ;
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
		        			action=(WebAction)children.get(j);
		        			if(action.canDisplay(webActionEnv))
		        				catschild.add(action);
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
	        		if(((WebAction)o).canDisplay(webActionEnv)){
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
