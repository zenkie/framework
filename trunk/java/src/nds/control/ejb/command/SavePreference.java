package nds.control.ejb.command;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.*;

import nds.control.ejb.Command;
import nds.control.event.DefaultWebEvent;
import nds.control.event.NDSEventException;
import nds.control.util.ValueHolder;
import nds.query.*;
import nds.util.*;




/*import com.liferay.portal.ejb.UserManagerUtil;
import com.liferay.portal.ejb.UserManager;
import com.liferay.portal.ejb.UserManagerFactory;

*/
import nds.security.User;

/**
 * Save user preference, UserWebImpl cache should be invalidated before or after calling this class
 */

public class SavePreference extends Command {
    private final static String UPDATE_PREF="update ad_user_pref set value=? , modifieddate=sysdate where ad_user_id=? and module=? and name=?";
    private final static String INSERT_PREF="insert into  ad_user_pref (id, module, name, value, ad_user_id, creationdate, modifieddate) values (get_sequences('ad_user_pref'), ?, ?,?,?,sysdate,sysdate)";
    private final static String DELETE_PREF="delete from  ad_user_pref where ad_user_id=? and module=? and name=?";

	/**
	 * @param event contains "module" and "preferences"
	 * "preferences" contains preference names seperated by comma
	 */
  public ValueHolder execute(DefaultWebEvent event) throws RemoteException, NDSException {
  	User usr=helper.getOperator(event);
  	
  	
	int userId= usr.id.intValue();//Tools.getInt( event.getParameterValue("userid"), usr.id.intValue());

	String module= (String)event.getParameterValue("module");
	String preferences= (String)event.getParameterValue("preferences");
	StringTokenizer st=new StringTokenizer(preferences, ",");
	Properties props=new Properties();
	while(st.hasMoreTokens()){
		String name= st.nextToken();
		String value=(String) event.getParameterValue(name);
		if(Validator.isNotNull(name))props.setProperty(name, value);
	}
	setPreferenceValues(userId, module, props);
	ValueHolder holder= new ValueHolder();
	holder.put("message", "@finished@");
	holder.put("code","0");
	return holder;
  }
  /**
   * Set preferences and store, if cacheable, will also update cache
   * @param module
   * @param values name is preference name, value is preference value
   * @param cacheable
   */
  public static void setPreferenceValues(int userId, String module, Properties values) throws NDSException{
  	Connection con=null;
      PreparedStatement pstmt=null ,pstmt2=null, pstmt3=null;
      String p=null;String name=null, value= null;
      int r;
      try {
  		con=QueryEngine.getInstance().getConnection() ;
          pstmt= con.prepareStatement(UPDATE_PREF);
          pstmt2= con.prepareStatement(INSERT_PREF);
          pstmt3= con.prepareStatement(DELETE_PREF);
          for(Iterator it= values.keySet().iterator();it.hasNext();){
          	name=(String) it.next();
          	value= values.getProperty(name);
          	if(Validator.isNull(value)){
          		// try delete, since no data 
          		//try{
          			pstmt3.setInt(1, userId);
          			pstmt3.setString(2, module);
    	            pstmt3.setString(3, name);
    	            r=pstmt3.executeUpdate();
    	            //logger.debug("Delete pref for user id= "+ userId + " module="+ module+",name="+name +" count="+r);
          		//}catch(Throwable dt){}
          	}else{
	            pstmt.setString(1, value);
	            pstmt.setInt(2, userId);
	            pstmt.setString(3, module);
	            pstmt.setString(4, name);
	            r= pstmt.executeUpdate();
	            if(r<1){
	            	// insert 
	                pstmt2.setString(1, module);
	                pstmt2.setString(2, name);
	                pstmt2.setString(3, value);
	                pstmt2.setInt(4, userId);
	                pstmt2.executeUpdate();
	            }
          	}
          }
  	}catch(Exception e){
  		throw new NDSException("Could not save preferences", e);
  	}finally{
          try{ if(pstmt !=null)pstmt.close() ;}catch(Exception e2){}
          try{ if(pstmt2 !=null)pstmt2.close() ;}catch(Exception e2){}
          try{ if(pstmt3 !=null)pstmt3.close() ;}catch(Exception e2){}
          try{ con.close() ;} catch(Exception e3){}

      }        	    	
  }
  /**
   * Set preference and store, if cacheable, will also update cache
   * @param module
   * @param name
   * @param value
   * @param cacheable
   */
  private void setPreferenceValue(int userId, String module, String name, String value, boolean cacheable) throws Exception{
  	Connection con=null;
      PreparedStatement pstmt=null ;
      String p=null;
      try {
  		con=QueryEngine.getInstance().getConnection() ;
  		if(Validator.isNull(value)){
      		// try delete, since no data 
      		try{
      			pstmt= con.prepareStatement(DELETE_PREF);
      			pstmt.setInt(1, userId);
      			pstmt.setString(2, module);
	            pstmt.setString(3, name);
	            pstmt.executeUpdate();
      		}catch(Throwable dt){}
      	}else{
          pstmt= con.prepareStatement(UPDATE_PREF);
          pstmt.setString(1, value);
          pstmt.setInt(2, userId);
          pstmt.setString(3, module);
          pstmt.setString(4, name);
          int r= pstmt.executeUpdate();
          if(r<1){
          	// insert 
              pstmt= con.prepareStatement(INSERT_PREF);
              pstmt.setString(1, module);
              pstmt.setString(2, name);
              pstmt.setString(3, value);
              pstmt.setInt(4, userId);
              pstmt.executeUpdate();
          }
      	}
  	}finally{
          
          try{ if(pstmt !=null)pstmt.close() ;}catch(Exception e2){}
          try{ con.close() ;} catch(Exception e3){}

      }        	
  }
  
}