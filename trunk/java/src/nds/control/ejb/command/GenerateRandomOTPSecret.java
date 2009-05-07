package nds.control.ejb.command;

import java.rmi.RemoteException;
import java.text.DecimalFormat;
import java.util.ArrayList;

import org.apache.commons.lang.RandomStringUtils;

import nds.control.ejb.Command;
import nds.control.event.DefaultWebEvent;
import nds.control.event.NDSEventException;
import nds.control.util.ValueHolder;
import nds.control.web.WebUtils;
import nds.query.*;
import nds.util.*;




/*import com.liferay.portal.ejb.UserManagerUtil;
import com.liferay.portal.ejb.UserManager;
import com.liferay.portal.ejb.UserManagerFactory;

*/
import nds.schedule.JobManager;
import nds.security.User;

/**
 * Generate random OTP Secret
 *
 */

public class GenerateRandomOTPSecret extends Command {
	/**
	 *  objectid will be  ad_trigger.id
	 */
  public ValueHolder execute(DefaultWebEvent event) throws RemoteException, NDSException {
  	User usr=helper.getOperator(event);
  	
  	int columnId =Tools.getInt( event.getParameterValue("columnid"), -1);
  	int objectId= Tools.getInt( event.getParameterValue("objectid"), -1);
  	String secret;
  	String counter;
  	
  	try{
  	  	int secretLength=Tools.getInt( QueryEngine.getInstance().doQueryOne("select otp_length from users where id="+ objectId), 8);
  	  	secret= RandomStringUtils.randomAlphanumeric(secretLength);
  		counter= RandomStringUtils.randomNumeric(18);

  		QueryEngine.getInstance().executeUpdate("update users set otp_secret='"+
  				obfuscate(secret)+"', otp_counter='"+ counter+"',  otp_cdate = sysdate where id="+ objectId);
	  	ValueHolder holder= new ValueHolder();
	  	
		
	  	holder.put("message","@pls-record-otp-info-with-secret@:"+secret+
				", @otp-counter@:"+ ((java.text.DecimalFormat)QueryUtils.intPrintFormatter.get()).format(Long.parseLong(counter)) 
				+"</PRE>");
		holder.put("code","0");
		
		return holder;
  	}catch(Exception e){
  		logger.error("Error generate secret for user id="+ objectId ,e);
  		throw new NDSException("@exception@:"+e);
  	}
  }
  private static String obfuscate(String s)
  {
      StringBuffer buf = new StringBuffer();
      byte[] b = s.getBytes();
      
      synchronized(buf)
      {
      	// yfzhu marked up at 2005-12-21
          //buf.append("OBF:"); 
          for (int i=0;i<b.length;i++)
          {
              byte b1 = b[i];
              byte b2 = b[s.length()-(i+1)];
              int i1= (int)b1+(int)b2+127;
              int i2= (int)b1-(int)b2+127;
              int i0=i1*256+i2;
              String x=Integer.toString(i0,36);

              switch(x.length())
              {
                case 1:buf.append('0');
                case 2:buf.append('0');
                case 3:buf.append('0');
                default:buf.append(x);
              }
          }
          return buf.toString();
      }
  }  
}