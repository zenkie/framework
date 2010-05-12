package nds.web.action;

import java.util.ArrayList;
import java.util.List;

import nds.query.QueryEngine;
import nds.schema.SchemaUtils;
import nds.schema.*;
import nds.util.*;

public class WebActionUtils {
	/**
	 * Load action Action 状态为可用，且对应table/category必须都可用
	 * 如果是category, 要求必须含有ad_table
	 * @return
	 * @throws Exception
	 */
	public static List<WebAction> loadActions() throws Exception{
		List actions=QueryEngine.getInstance().doQueryList(
				"select id, displaytype,name, description,iconurl, ad_table_id,ad_tablecategory_id, priority, filter, actiontype, content, scripts,urltarget, saveobj, comments,ad_subsystem_id from ad_action where isactive='Y' and ( exists (select 1 from ad_tablecategory c, ad_subsystem s where c.id=ad_action.ad_tablecategory_id and s.id=c.ad_subsystem_id and c.isactive='Y' and s.isactive='Y' and exists(select 1 from ad_table t where t.ad_tablecategory_id=c.id and t.isactive='Y')) or exists (select 1 from ad_table t,ad_tablecategory c, ad_subsystem s where t.id=ad_action.ad_table_id and c.id=t.ad_tablecategory_id and s.id=c.ad_subsystem_id and t.isactive='Y' and c.isactive='Y' and s.isactive='Y')or exists(select 1 from ad_subsystem s where s.id=ad_action.ad_subsystem_id and s.isactive='Y')) order by priority"
		);
		
		ArrayList<WebAction> wactions=new ArrayList();
		for(int i=0;i< actions.size();i++){
			List act=(List)actions.get(i);
			int j=0;
			int id=Tools.getInt( act.get(j++), -1);
			String displayType= (String)act.get(j++);
			String name= (String)act.get(j++);
			String description= (String)act.get(j++);
			String iconURL= (String)act.get(j++);
			int tableId=Tools.getInt( act.get(j++), -1);
			int tableCategoryId=Tools.getInt( act.get(j++), -1);
			int order=Tools.getInt( act.get(j++), -1);
			String filter= (String)act.get(j++);
			String actionType= (String)act.get(j++);
			String content= (String)act.get(j++);
			
			String scripts=null;
			Object sc=act.get(j++);
			//support clob
			if(sc instanceof java.sql.Clob) {
				scripts=((java.sql.Clob)sc).getSubString(1, (int) ((java.sql.Clob)sc).length());
        	}else{
        		scripts=(String)sc;
        	}
			//String scripts= (String)act.get(j++);
			String urlTarget= (String)act.get(j++);
			String saveObjType= (String)act.get(j++);
			String comments= (String)act.get(j++);
			int subSystemId=  Tools.getInt( act.get(j++), -1);
			
			WebAction wa=createAction(id, displayType, name, description, iconURL, tableId, tableCategoryId, 
					subSystemId,order, filter, actionType, content, scripts, urlTarget, saveObjType, comments);
			
			wactions.add(wa);
		}
		return wactions;
	} 
	public static WebAction createAction(int id, String displayType,String name,
			String description,String iconURL,int tableId, int tableCategoryId,int subSystemId,
			int order, String filter, String actionType,String content,String scripts,
			String urlTarget,String saveObjType,String comments){
		WebAction.DisplayTypeEnum dte=WebAction.DisplayTypeEnum.parse(displayType);
		WebActionImpl wa;
		switch(dte){
			case ListButton:
				wa=new ListButtonAction();
				break;
			case ListMenuItem:
				wa=new ListMenuItemAction();
				break;
			case ObjButton:
				wa=new ObjButtonAction();
				break;
			case ObjMenuItem:
				wa=new ObjMenuItemAction();
				break;
			case TabButton:
				wa=new TabButtonAction();
				break;
			case TreeNode:
				wa=new TreeNodeAction();
				break;
			default:
				throw new IllegalArgumentException("display type:"+ displayType +" not defined as WebAction");
			
		}
		wa.setId(id);
		updateWebAction(wa,dte, name, description, iconURL, tableId, tableCategoryId, 
				subSystemId,order, filter, actionType, content, scripts, urlTarget, saveObjType, comments);
		return wa;
	}
	private static void updateWebAction(WebActionImpl wa, WebAction.DisplayTypeEnum displayType,String name,
			String description,String iconURL,int tableId, int tableCategoryId,int subSystemId,
			int order, String filter, String actionType,String content,String scripts,
			String urlTarget,String saveObjType,String comments){
		wa.setDisplayType(displayType);
		wa.setName(name);
		wa.setActionType(WebAction.ActionTypeEnum.parse(actionType));
		wa.setComments(comments);
		wa.setDescription(description);
		wa.setFilter(filter);
		wa.setIconURL(iconURL);
		wa.setOrder(order);
		wa.setSaveObjType(WebAction.SaveObjectEnum.parse(saveObjType));
		wa.setScript(nds.util.Validator.isNotNull(content)? content: scripts);
		//wa.setShouldConfirm(shouldConfirm)
		wa.setTableCategoryId(tableCategoryId);
		wa.setTableId(tableId);
		wa.setSubSystemId(subSystemId);
		wa.setUrlTarget(urlTarget);
	}
	/**
	 * Reload action and update tablemanager cache
	 * @param id ad_action.id
	 * @throws Exception
	 */
	public static void reloadWebAction(int id) throws Exception{
		List actions=QueryEngine.getInstance().doQueryList(
				"select id, displaytype,name, description,iconurl, ad_table_id,"+
				"ad_tablecategory_id, priority, filter, actiontype, content, scripts,"+
				"urltarget, saveobj, comments,ad_subsystem_id from ad_action where id="+ id +" and isactive='Y' and "+
				"( exists (select 1 from ad_tablecategory c, ad_subsystem s where c.id=ad_action.ad_tablecategory_id and s.id=c.ad_subsystem_id "+
				"and c.isactive='Y' and s.isactive='Y' ) or "+
				"exists (select 1 from ad_table t,ad_tablecategory c, ad_subsystem s where "+ 
				"t.id=ad_action.ad_table_id and c.id=t.ad_tablecategory_id and s.id=c.ad_subsystem_id "+
				"and t.isactive='Y' and c.isactive='Y' and s.isactive='Y') or exists(select 1 from ad_subsystem s where s.id=ad_action.ad_subsystem_id and s.isactive='Y'))");
		
		if(actions==null || actions.size()==0){
			throw  new NDSException("This action is not going to be loaded in memory currently");
		}
		List act= (List) actions.get(0);
		
		int j=0;
		int id2=Tools.getInt( act.get(j++), -1);
		String displayType= (String)act.get(j++);
		String name= (String)act.get(j++);
		String description= (String)act.get(j++);
		String iconURL= (String)act.get(j++);
		int tableId=Tools.getInt( act.get(j++), -1);
		int tableCategoryId=Tools.getInt( act.get(j++), -1);
		int order=Tools.getInt( act.get(j++), -1);
		String filter= (String)act.get(j++);
		String actionType= (String)act.get(j++);
		String content= (String)act.get(j++);
		
		String scripts=null;
		Object sc=act.get(j++);
		//support clob
		if(sc instanceof java.sql.Clob) {
			scripts=((java.sql.Clob)sc).getSubString(1, (int) ((java.sql.Clob)sc).length());
    	}else{
    		scripts=(String)sc;
    	}
		
		String urlTarget= (String)act.get(j++);
		String saveObjType= (String)act.get(j++);
		String comments= (String)act.get(j++);
		int subsystemId=Tools.getInt( act.get(j++), -1);
		WebAction.DisplayTypeEnum dte=WebAction.DisplayTypeEnum.parse(displayType);
		
		/**
		 * 如果是新增的ACTION，将添加到当前表或表类别定义的动作列表中，否则更新现有Action实例
		 * 更新或者新增后，将要求刷新在列表里的先后顺序。
		 */
		if(tableId!=-1){
			TableImpl tb=(TableImpl) TableManager.getInstance().getTable(tableId);
			if(tb==null) throw new NDSException("Could not locate table id="+ tableId+" in memory");
			
			List<WebAction> list= tb.getWebActions(dte);
			boolean bUpdate=false;
			if(list.isEmpty()){
				WebAction wa=createAction(id, displayType, name, description, iconURL, tableId, tableCategoryId, 
						subsystemId,order, filter, actionType, content, scripts, urlTarget, saveObjType, comments);
				tb.addWebAction(wa);
			}else{
				for(int i=0;i< list.size();i++){
					WebActionImpl a= (WebActionImpl)list.get(i);
					if(a.getId()== id){
						// do update
						updateWebAction(a,WebAction.DisplayTypeEnum.parse(displayType), name, description, iconURL, tableId, tableCategoryId, 
								subsystemId,order, filter, actionType, content, scripts, urlTarget, saveObjType, comments);
						bUpdate=true;
						break;
					}
				}
				if(!bUpdate){
					// do insert
					WebAction wa=createAction(id, displayType, name, description, iconURL, tableId, tableCategoryId, 
							subsystemId,order, filter, actionType, content, scripts, urlTarget, saveObjType, comments);
					list.add(wa);
				}
				nds.util.ListSort.sort(list, "Order");
			
			}
		}
		if(tableCategoryId!=-1){
			TableCategory c=  TableManager.getInstance().getTableCategory(tableCategoryId);
			if(c==null) throw new NDSException("Could not locate tablecategory id="+ tableCategoryId+" in memory");
			
			List<WebAction> list= c.getWebActions();
			
			boolean bUpdate=false;
			if(list.isEmpty()){
				WebAction wa=createAction(id, displayType, name, description, iconURL, tableId, tableCategoryId, 
						subsystemId,order, filter, actionType, content, scripts, urlTarget, saveObjType, comments);
				c.addWebAction(wa);
			}else{
				for(int i=0;i< list.size();i++){
					WebActionImpl a= (WebActionImpl)list.get(i);
					if(a.getId()== id){
						// do update
						updateWebAction(a,WebAction.DisplayTypeEnum.parse(displayType), name, description, iconURL, tableId, tableCategoryId, 
								subsystemId,order, filter, actionType, content, scripts, urlTarget, saveObjType, comments);
						bUpdate=true;
						break;
					}
				}
				if(!bUpdate){
					// do insert
					WebAction wa=createAction(id, displayType, name, description, iconURL, tableId, tableCategoryId, 
							subsystemId,order, filter, actionType, content, scripts, urlTarget, saveObjType, comments);
					list.add(wa);
				}
				nds.util.ListSort.sort(list, "Order");
			
			}
		}
	}
}
