package nds.control.ejb.command;

import java.rmi.RemoteException;
import java.util.ArrayList;

import nds.control.ejb.Command;
import nds.control.event.DefaultWebEvent;
import nds.control.event.NDSEventException;
import nds.control.util.ValueHolder;
import nds.control.web.UserWebImpl;
import nds.control.web.WebUtils;
import nds.query.*;
import nds.util.*;




/*import com.liferay.portal.ejb.UserManagerUtil;
import com.liferay.portal.ejb.UserManager;
import com.liferay.portal.ejb.UserManagerFactory;

*/
import nds.security.Directory;
import nds.security.User;

/**
 * Create order according to picking list
 * All items in picking list will set as order items
 * Order information will be copied from m_inout's order
 * if m_inout not exists in picking list, will try to fetch from parent of picking list
 */

public class CreateOrderByPKL extends Command {
	/**
	 *  
	 */
  public ValueHolder execute(DefaultWebEvent event) throws RemoteException, NDSException {
  	User usr=helper.getOperator(event);
  	
  	int columnId =Tools.getInt( event.getParameterValue("columnid",true), -1);
  	int objectId= Tools.getInt( event.getParameterValue("objectid",true), -1);
	String docBaseType= (String )QueryEngine.getInstance().doQueryOne("select docbasetype from c_doctype where id=(select C_ORDER_DOCTYPE_ID from m_pick where id="+ objectId+")");
	if(docBaseType!=null && docBaseType.length()>1){
		String firstChar= docBaseType.substring(0,1);
		String dir=null;
		if(firstChar.equals("S")){
			// sale
			dir="C_V_SO_ORDER_LIST";
		}else if(firstChar.equals("P")){
			// purchase
			dir="C_V_PO_ORDER_LIST";
		}else{
			//unknown
			
		}
		if( dir!=null && (nds.control.util.SecurityUtils.getPermission(dir, usr.getId().intValue())&Directory.WRITE)==Directory.WRITE){
			if(!"root".equals(usr.getName()))throw new NDSException("@no-permission@");	
		}
	}else{
		throw new NDSException("@unknown-doctype@");	
	}
	
  	
  	ArrayList al=new ArrayList();
  	al.add(new Integer(objectId));
  	al.add(usr.getId());
  	SPResult result= helper.executeStoredProcedure("M_PICK_CREATE_ORDER", al, true  );
  	
  	ValueHolder holder= new ValueHolder();
  	if(result.isSuccessful() ){
  		holder.put("message",result.getMessage() ) ;
  		holder.put("next-screen", "/html/nds/info.jsp");
        
    }else{
        throw new NDSEventException(result.getDebugMessage());
    }  	
	return holder;
  }
}