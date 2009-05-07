/*
 * Agile Control Technologies Ltd,. CO.
 * http://www.agileControl.com
 */
package nds.web.bean;

import nds.util.StringUtils;
import nds.web.LangUtil;


import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.jsp.*;

/**
 * 扩展 /objext/toolbar.jsp ，增加列表界面下的菜单项目和工具栏上的按钮
 * 
 * @author yfzhu@agilecontrol.com
 */

public class MenuItem{
	private boolean isShownOnToolBar=false;
	public String name;
	public String function;
	public String image;
	
	public  MenuItem(boolean isShownOnToolBar){
		this.isShownOnToolBar=isShownOnToolBar;
	}
	public boolean isShownOnToolBar(){
		return this.isShownOnToolBar;
	}
	//new MenuItem( "<%= PortletUtils.getMessage(pageContext, "menu.new",null)%>",function(){createobj();} ,"<%=NDS_PATH%>/images/tb_new.gif")
	public String toMenuHTML(PageContext pageContext ){
    	StringBuffer sb=new StringBuffer();
    	sb.append("new MenuItem(\"").append(LangUtil.get(pageContext, name ))
		.append("\", function(){").append(function).append(";},\"")
		.append(image).append("\")");
    	return sb.toString();
		
	}
	
}
