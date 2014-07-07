package nds.control.ejb.command;

import java.rmi.RemoteException;
import java.util.ArrayList;

import nds.control.ejb.Command;
import nds.control.event.DefaultWebEvent;
import nds.control.event.NDSEventException;
import nds.control.util.ValueHolder;
import nds.query.SPResult;
import nds.schema.Column;
import nds.schema.Table;
import nds.schema.TableManager;
import nds.security.Directory;
import nds.security.User;
import nds.util.NDSException;
import nds.util.Tools;
import nds.util.Validator;

public class ButtonCommandforOrder extends Command {
	
	 public ValueHolder execute(DefaultWebEvent event) throws RemoteException, NDSException {
		  	ValueHolder v= new ValueHolder();
		  	// nerver throw error, always return code
			//try{
			  	User usr=helper.getOperator(event);
			  	int columnId =Tools.getInt( event.getParameterValue("columnid", true), -1);
			  	int objectId= Tools.getInt( event.getParameterValue("objectid",true), -1);
			  	String active=(String) event.getParameterValue("active");
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
				String[] vals=((String) className).split("@");
				if(vals.length==2){
					className=vals[0];
					active=vals[1];
				}
			  	if(Validator.isNull(className)) className= manager.getColumn(columnId).getValueInterpeter();
			  	if(Validator.isNull(className)) throw new NDSEventException("Delegator of the button command not set(column="+ column+")");
			  	// class or stored procedure
			    if (className.indexOf('.')<0 ){
			    	// stored procedure
			    	// procedure param :  objectId, r_code, r_msg
			    	System.out.print("className ->"+className);
			    	System.out.print("objectId ->"+objectId);
			    	System.out.print("table ->"+table.getName());
			    	System.out.print("active ->"+active);
			    	System.out.print("usr ->"+usr.getId());
			      	ArrayList params=new ArrayList();
			      	params.add(new Integer(objectId));
			      	params.add(table.getName());
			      	params.add(active);
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
