package nds.control.ejb.command;

import java.rmi.RemoteException;
import java.util.ArrayList;

import nds.control.ejb.Command;
import nds.control.ejb.Trigger;
import nds.control.event.DefaultWebEvent;
import nds.control.event.NDSEventException;
import nds.control.util.ValueHolder;
import nds.query.*;
import nds.util.*;
import nds.schema.*;
import nds.security.Directory;
import nds.security.User;

/**
  在Column 定义里的ValueInterpeter 在 DisplayType ='button' 的时候，用于标识构造和响应按钮事件的方法。
  1)
  ValueInterpeter不含"." 或不是 "nds.web.ButtonCommandUI" 接口的实现时，表示显示按钮的控制类为 nds.web.ButtonCommandUI_Impl，
  生成的命令由nds.control.ejb.command.ButtonCommand处理。
  ButtonCommand 首先检查传入的事件中是否含有具体的事件处理类的信息（param='delegator')，如果不存在，将以Column的
  ValueInterpeter 作为delegator。delegator不含"."，具体的处理过程将交给 delegator 所指明的
  存储过程，如果delegator包含"."，具体的处理过程将交给delegator 所指明的类，此类应该继承Command。
  2)
  当ValueInterpeter 为ButtonCommandUI接口的实现时，表示显示按钮的控制类为ValueInterpeter所指明的类。可以考虑
  扩展nds.web.ButtonCommandUI_Impl, 如果仍希望借助ButtonCommand的机制，进入有关存储过程，可以override nds.web.ButtonCommandUI_Impl
  的 getDelegator() 方法，此方法缺省返回 Column.ValueInterpeter
 */

public class ButtonCommand extends Command {
	/**
	 * Security consideration: will use record max action permission(r,w,s) for this command.
	 * 这里存在一个安全bug，未对具体记录进行判断，而只是判断了表的最高权限。（必须与表所能够进行的最高执行权限匹配，
	 * 例如，表定义了读写提交功能，则当前将判断读写提交
	 * 
	 * @param event must contains 
	 *   objectid* - the record to handle.
	 *   columnid* - column id of button object.
	 *   delegator - optional, if not exists, will use column.ValueInterpeter as that one,
	 *   			 delegator can be store procedure name, or class name which implements 
	 * 				 nds.control.ejb.Command interface.
	 *   			 store procedure should have 3 parameter: objectId, r_code, r_message     
	 * 	 operatorid*-for user, set by system.
	 * @return ValueHolder contains code and message, 
	 * 		code: 
	 * 			0 - only show message
	 * 			1 - show message and refresh current page
	 * 			2 - show message and close current page
	 *          <0 - error
	 */
  public ValueHolder execute(DefaultWebEvent event) throws RemoteException, NDSException {
  	ValueHolder v= new ValueHolder();
  	// nerver throw error, always return code
	//try{
	  	User usr=helper.getOperator(event);
	  	int columnId =Tools.getInt( event.getParameterValue("columnid", true), -1);
	  	int objectId= Tools.getInt( event.getParameterValue("objectid",true), -1);
	  	TableManager manager= TableManager.getInstance();
	  	Column column= manager.getColumn(columnId);
	  	Table table =column.getTable();
	  	event.setParameter("directory",  table.getSecurityDirectory() );
	  	
	  	// user permission should has all action permission set on table
	  	int perm= helper.getPermissions(table.getSecurityDirectory(), usr.getId().intValue());
	  	int maxPerm = 0;
	  	if (table.isActionEnabled(Table.SUBMIT)) maxPerm |= Directory.SUBMIT;
	  	if( table.isActionEnabled(Table.MODIFY)|| 
	  			table.isActionEnabled(Table.ADD)||table.isActionEnabled(Table.DELETE)) maxPerm |= Directory.WRITE;
	  	if ( table.isActionEnabled(Table.QUERY)) maxPerm |= Directory.READ;
	  	
	  	if( ((perm & maxPerm) != maxPerm) &&  !"root".equals(usr.getName())) throw new NDSEventException("Permission denied");
	  	
	  	String className=(String)event.getParameterValue("delegator",true);
	  	if(Validator.isNull(className)) className= manager.getColumn(columnId).getValueInterpeter();
	  	if(Validator.isNull(className)) throw new NDSEventException("Delegator of the button command not set(column="+ column+")");
	  	// class or stored procedure
	    if (className.indexOf('.')<0 ){
	    	// stored procedure
	    	// procedure param :  objectId, r_code, r_msg
	      	ArrayList params=new ArrayList();
	      	params.add(new Integer(objectId));
	      	params.add(usr.getId());
	      	SPResult result =helper.executeStoredProcedure(className, params, true);
	        v.put("message",result.getMessage() ) ;
	        v.put("code", new Integer(result.getCode()));
	    }else{
	    	// to avoid recursion, check class name may not be ButtonCommand
	    	if( "nds.control.ejb.command.ButtonCommand".equalsIgnoreCase(className)){
	    		throw new NDSEventException("Column interpreter can not be 'nds.control.ejb.command.ButtonCommand' for 'button' type");
	    	}
	    	DefaultWebEvent e2= (DefaultWebEvent) event.clone();
	    	e2.setParameter("command", className );
	    	v= helper.handleEvent(e2);
	    }
  	
	/* yfzhu modified 20081015 so transaction can be rolled back
	 * catch(Throwable t){
  		logger.error("Fail to execute ", t);
  		v.put("code","0");
  		v.put("message", "@exception@:"+t.getMessage() ) ;
  	}*/
  	return v;
  	
  }
}