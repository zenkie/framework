/******************************************************************
*
*$RCSfile: SPResult.java,v $ $Revision: 1.2 $ $Author: Administrator $ $Date: 2006/07/12 10:11:00 $
*
*$Log: SPResult.java,v $
*Revision 1.2  2006/07/12 10:11:00  Administrator
*add audit control
*
*Revision 1.1.1.1  2005/03/15 11:23:23  Administrator
*init
*
*Revision 1.1.1.1  2002/11/29 02:50:34  yfzhu
*Active POS
*
*Revision 1.2  2002/01/04 01:43:22  yfzhu
*no message
*
*Revision 1.1  2001/12/09 03:48:07  yfzhu
*no message
*
*Revision 1.3  2001/11/13 07:19:00  yfzhu
*no message
*
*Revision 1.2  2001/11/07 20:58:47  yfzhu
*no message
*
*
********************************************************************/

package nds.query;

import java.sql.CallableStatement;
import java.sql.SQLException;
import org.json.*;
/**
 *  
 */
public class SPResult implements JSONString {
    private int code;
    private String msg="";
    private Object tag;
    
    public SPResult(CallableStatement stmt, int paramIndex) throws SQLException{
        code = stmt.getInt( paramIndex);
        String returnMsg = stmt.getString( paramIndex + 1);
        msg = returnMsg;
    }
    public SPResult( int code, String msg){
        this.code=code;
        this.msg= msg;
    }
    public SPResult() {
    	code=0;
    }
     /**
     * 直接在其他程序中创建SPResult时调用此方法
     */
    public SPResult(String message){
        code=0;
        msg=message;
    }
    public boolean isSuccessful(){
        return code==0;
    }
    public void setCode(int c){
    	this.code = c;
    }
    public int getCode(){
        return code;
    }
    public void setMessage(String s){
    	this.msg=s;
    }
    public String getMessage(){
        return msg;
    }
    public String getDebugMessage(){
        return msg;
    }
    public String toString(){
        return "["+msg+"]";
    }
    public String toJSONString(){
    	try{
	    	JSONObject jo=new JSONObject();
	    	jo.put("code", code);
	    	jo.put("message",msg);
	    	return jo.toString();
    	}catch(JSONException je){
    		return  JSONObject.NULL.toString();
    	}
    }

	public Object getTag() {
		return this.tag;
	}

	public void setTag(Object tag) {
		this.tag = tag;
	}
}