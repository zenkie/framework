package nds.web.config;

import java.util.*;
import nds.query.*;
import nds.schema.*;
import nds.util.*;
import nds.log.Logger;
import nds.log.LoggerManager;

/**
 * Create various kinds of PortletConfig
 * @author yfzhu
 *
 */
public class PortletConfigFactory {
    private static Logger logger= LoggerManager.getInstance().getLogger(PortletConfigFactory.class.getName());
	public static PortletConfig loadPortletConfig(String name, int type) throws Exception{
		PortletConfig pc=null;
		switch(type){
		case PortletConfig.TYPE_LIST_DATA:
			pc= loadListDataConfig(name);
			break;
		case PortletConfig.TYPE_LIST_UI:
			pc=loadListUIConfig(name);
			break;
		case PortletConfig.TYPE_OBJECT_UI:
			pc=loadObjectUIConfig(name);
			break;
		default:
			throw new nds.util.ObjectNotFoundException("PortletConfig not found for:type="+type+",name="+ name);
		}
		return pc;
	}
    /**
	 * 
	 * @param id
	 * @param type PortletConfig.TYPE
	 * @return
	 * @throws Exception
	 */
	public static PortletConfig loadPortletConfig(int id, int type) throws Exception{
		PortletConfig pc=null;
		switch(type){
		case PortletConfig.TYPE_LIST_DATA:
			pc= loadListDataConfig(id);
			break;
		case PortletConfig.TYPE_LIST_UI:
			pc=loadListUIConfig(id);
			break;
		case PortletConfig.TYPE_OBJECT_UI:
			pc=loadObjectUIConfig(id);
			break;
		default:
			throw new nds.util.ObjectNotFoundException("PortletConfig not found for:type="+type+",id="+ id);
		}
		return pc;
	}
	private static PortletConfig loadListDataConfig(List al){
		if(al.size()==0)return null;
		al=(List)al.get(0);
		ListDataConfig pc=new ListDataConfig();
		pc.setId( Tools.getInt(al.get(0), -1));
		pc.setName((String)al.get(1));
		pc.setTableId(Tools.getInt(al.get(2), -1));
		pc.setMainURL((String)al.get(3));
		pc.setMainTarget((String)al.get(4));
		pc.setColumnMasks((String)al.get(5));
		pc.setFilter((String)al.get(6));
		pc.setPublic(Tools.getYesNo((String)al.get(7), false));
		pc.setOrderbyColumnId(Tools.getInt(al.get(8), -1));
		pc.setAscending(Tools.getYesNo((String) al.get(9),true));
		return pc;
	}
	private static PortletConfig loadListDataConfig(String name)  throws Exception{
		List al=nds.query.QueryEngine.getInstance().doQueryList("select id, name, ad_table_id,mainurl,maintarget,columnmasks,filter,ispublic,orderbycolumn_id,orderbyasc from ad_listdataconf where name="+QueryUtils.TO_STRING(name));
		return loadListDataConfig(al);
	}
	private static PortletConfig loadListDataConfig(int id)  throws Exception{
		List al=nds.query.QueryEngine.getInstance().doQueryList("select id, name, ad_table_id,mainurl,maintarget,columnmasks,filter,ispublic,orderbycolumn_id,orderbyasc from ad_listdataconf where id="+id);
		return loadListDataConfig(al);
	}
	private static PortletConfig loadListUIConfig(String name)  throws Exception{
		List al=nds.query.QueryEngine.getInstance().doQueryList("select id, name, cssclass,showtitle,morestyle,moreurl,columncount,columnlengths,pagesize ,liststyle, searchbox,titlecss from ad_listuiconf where name="+QueryUtils.TO_STRING(name));
		return loadListUIConfig(al);
	}
	private static PortletConfig loadListUIConfig(int id)  throws Exception{
		List al=nds.query.QueryEngine.getInstance().doQueryList("select id, name, cssclass,showtitle,morestyle,moreurl,columncount,columnlengths,pagesize ,liststyle, searchbox,titlecss from ad_listuiconf where id="+id);
		return loadListUIConfig(al);
	}
	private static PortletConfig loadListUIConfig(List al)  throws Exception{
		if(al.size()==0)return null;
		al=(List)al.get(0);
		ListUIConfig pc=new ListUIConfig();
		pc.setId( Tools.getInt(al.get(0), -1));
		pc.setName((String)al.get(1));
		pc.setCssClass((String)al.get(2));
		pc.setShowTitle(Tools.getYesNo((String)al.get(3), false));
		pc.setMoreStyle((String)al.get(4));
		pc.setMoreURL((String)al.get(5));
		pc.setColumnCount(Tools.getInt(al.get(6), -1));
		pc.setColumnLength(StringUtils.parseIntArray((String)al.get(7),","));
		pc.setPageSize(Tools.getInt(al.get(8), -1));
		pc.setStyle((String)al.get(9));
		pc.setSearchBox((String)al.get(10));
		pc.setTitleCss((String)al.get(11));
		return pc;
	}
	private static PortletConfig loadObjectUIConfig(String name)  throws Exception{
		List al=nds.query.QueryEngine.getInstance().doQueryList("select id, name, cssclass,tableparamname,pkparamname,cols,defaultaction,ispublic from ad_objuiconf where name="+QueryUtils.TO_STRING(name));
		return loadObjectUIConfig(al);
	}
	private static PortletConfig loadObjectUIConfig(int id)  throws Exception{
		List al=nds.query.QueryEngine.getInstance().doQueryList("select id, name, cssclass,tableparamname,pkparamname,cols,defaultaction,ispublic from ad_objuiconf where id="+id);
		return loadObjectUIConfig(al);
	}
	private static PortletConfig loadObjectUIConfig(List al)  throws Exception{
		if(al.size()==0)return null;
		al=(List)al.get(0);
		ObjectUIConfig pc=new ObjectUIConfig();
		pc.setId( Tools.getInt(al.get(0), -1));
		pc.setName((String)al.get(1));
		pc.setCssClass((String)al.get(2));
		pc.setTableParamName((String)al.get(3));
		pc.setIdParamName((String)al.get(4));
		pc.setColsPerRow(Tools.getInt(al.get(5), 2));
		pc.setDefaultAction( "EDIT".equalsIgnoreCase((String)al.get(6))? pc.ACTION_EDIT:pc.ACTION_VIEW);
		pc.setPublic(Tools.getYesNo((String)al.get(7), false));
		return pc;
	}
}
