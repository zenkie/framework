package nds.control.ejb.command;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Vector;

import nds.control.ejb.Command;
import nds.control.event.DefaultWebEvent;
import nds.control.event.NDSEventException;
import nds.control.util.SecurityUtils;
import nds.control.util.ValueHolder;
import nds.query.QueryEngine;
import nds.query.SPResult;
import nds.schema.*;
import nds.security.Directory;
import nds.security.User;
import nds.util.JNDINames;
import nds.util.NDSException;
import nds.util.*;
/**
 Save preference info of object creation form, allow for anything in preferece value.
 So will not check validation of the default value.
 This template values should not be cached in UserWebImpl (not necessary) 
*/

public class ObjectTemplate extends Command{
	
    /**
     * 获取每一个新增时允许输入的字段，获取字段的值，不检查正确性，保存到AD_USER_PREF 表
     * Checkbox/Select 控件例外，对于非法值，将不作保存（界面上可以使用0，表示不填写)
     * 模块名称为当前表的template+"."+tablename.tolowercase
     * name= column.getName();
     * value= input from screen.
     */
    public ValueHolder execute(DefaultWebEvent event) throws NDSException ,RemoteException{
        logger.debug(event.toDetailString());
    	TableManager manager = helper.getTableManager();
    	Table table = manager.findTable(event.getParameterValue("table"));
        int tableId = table.getId();
        Integer userId= helper.getOperator(event).id;
        String tableName = table.getName();          // 得到表的名字
        
        String module= "template."+ tableName.toLowerCase();
        DefaultWebEvent e2=new DefaultWebEvent("CommandEvent");
        e2.setParameter("command","SavePreference");
        
        //e2.setParameter("userid", userId.toString());
        e2.setParameter("operatorid",userId.toString() );
        //logger.debug("userid="+ userId.toString()+" e2.userid="+ e2.getParameterValue("userid"));
        
        e2.setParameter("module", module);
        StringBuffer sb=new StringBuffer();
        ArrayList columns= table.getModifiableColumns(Column.ADD);
        for(int i=0;i<columns.size();i++){
        	Column column= (Column)columns.get(i);
        	String name = column.getName().toLowerCase() ;
        	if( column.getReferenceTable()!=null){
        		name= name +"__"+ column.getReferenceTable().getAlternateKey().getName().toLowerCase();
        	}
        	String value=(String)event.getParameterValue(name);
        	if(value==null) value=""; 
        	if (column.isValueLimited() && "0".equals(value)) value="";
        	e2.setParameter(column.getName(), value);
        	sb.append(column.getName()).append(",");
        	logger.debug("name="+ column.getName()+", value="+ value );
        }
        String prefNames= sb.toString();
        e2.setParameter("preferences", prefNames);
        logger.debug(e2.toDetailString());
        return helper.handleEvent(e2);
		

    }
}