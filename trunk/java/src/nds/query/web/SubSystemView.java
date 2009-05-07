package nds.query.web;

import java.util.*;

import javax.servlet.http.HttpServletRequest;

import nds.control.web.*;
import nds.schema.*;
import nds.security.NDSSecurityException;
import nds.util.Validator;

/**
 * Create Sub System view for user
 * @author yfzhu
 *
 */
public class SubSystemView {
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
				if(containsViewableTableCategories(request, ss.getId())){
					al.add(new Integer(ss.getId()));
					subs.add(ss);
				}
			}
			userWeb.setProperty("subsystems",al);
		}
		return subs;
	}
	/**
	 * if contains one category, will return true;
	 * @param request
	 * @param subSystemId
	 * @return
	 */
	private boolean containsViewableTableCategories(HttpServletRequest request, int subSystemId){
		
		// Create categories and their tables in hashtable
		TableManager manager=TableManager.getInstance();
        Iterator tables = manager.getAllTables().iterator();
        Hashtable categories = new Hashtable(50,20); // key:Integer(category id), values :List of table
        SubSystem ss;Integer tableCategoryId;Table table ;
        while(tables.hasNext()) {
            table= (Table)tables.next();
            tableCategoryId=new Integer(table.getCategory().getId());
            ss=table.getCategory().getSubSystem();
            if(ss ==null || ss.getId() !=subSystemId )continue;
            //skip item table
            if(!table.isMenuObject()){
            	continue;
            }
            // check for current user permission
            try{
                WebUtils.checkTableQueryPermission(table.getName(),request );
            }catch(NDSSecurityException e){
                continue;
            }
            // user has table view permission and that table belongs to current system
            return true;
        }
        return false;        
	}
	/**
	 * Return table categories and table that user has view permission
	 * @param request
	 * @param subSystemId
	 * @return never null, elements are List, contains 2 elements: 
	 * 		nds.schema.TableCategory,
	 * 		java.util.List (nds.schema.Table) 
	 */
	public List getTableCategories(HttpServletRequest request, int subSystemId){
		// Create categories and their tables in hashtable
		TableManager manager=TableManager.getInstance();
        Iterator tables = manager.getAllTables().iterator();
        Hashtable categories = new Hashtable(50,20); // key:Integer(category id), values :List of table
        SubSystem ss;Integer tableCategoryId;Table table ;
        while(tables.hasNext()) {
            table= (Table)tables.next();
            tableCategoryId=new Integer(table.getCategory().getId());
            ss=table.getCategory().getSubSystem();
            if(ss ==null || ss.getId() !=subSystemId )continue;
            //skip item table
            if(!table.isMenuObject()){
            	continue;
            }
            // check for current user permission
            try{
                WebUtils.checkTableQueryPermission(table.getName(),request );
            }catch(NDSSecurityException e){
                continue;
            }

            if(categories.containsKey(tableCategoryId)) { //has thisCategory
                List oldCatedGroup = (List)categories.get(tableCategoryId);
                oldCatedGroup.add(table);
            } else { //First Init this Category
            	List catedGroup = new ArrayList();
                catedGroup.add(table);
                categories.put(tableCategoryId,catedGroup);
            }
        }
        List v;
        for ( Iterator it= categories.keySet().iterator();it.hasNext();){
            v= (List) categories.get( it.next() );
            nds.util.ListSort.sort(v,"Order");
        }
        
        // So categories contains category which has internal table ordered
        
        // Order table categories in this sub system 
        ArrayList rtn = new ArrayList();
        ArrayList row;ArrayList catedTableGroup;
        Integer categoryId;
        ArrayList tcs = TableManager.getInstance().getTableCategories(); // elements are TableCategory
        for ( int i=0;i< tcs.size();i++){
        	TableCategory tc=  (TableCategory)tcs.get(i);
        	categoryId=new Integer(tc.getId());
            catedTableGroup = (ArrayList)categories.get(categoryId);
            if( catedTableGroup !=null){
                row= new ArrayList();
                row.add(tc);
                row.add(catedTableGroup);
                rtn.add(row);
                categories.remove(categoryId);
            }
        }
        return rtn;        
    }	
}
