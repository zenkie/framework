package nds.web.interpreter;

import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import nds.util.ColumnInterpretException;
import nds.util.ColumnInterpreter;

public class Foldinterpreter implements ColumnInterpreter,java.io.Serializable{

	public String parseValue(Object value, Locale locale){
		if(value==null) {return "";}
		String result=null;
		String m=(String)value;
		JSONArray ja=null;
		JSONObject jo=null;
		StringBuffer jsb=new StringBuffer();
		try {
			ja=new JSONArray(m);
		}catch(Exception e) {
			e.printStackTrace();
		}
		if(ja!=null) {
			for(int i=0;i<ja.length();i++) {
				jo=ja.optJSONObject(i);
				if(jo.has("name")) {
					jsb.append(jo.optString("name"));
					jsb.append("<br>");
				}
			}
		}
		result=(jsb.length()==0?"":jsb.toString())+"<ori>"+value+"</ori>";

		//System.out.print("past->"+result);
		return result;
	}

	public Object getValue(String str, Locale locale){
		//System.out.print("get->"+str);
		return str;
	}

	@Override
	public String changeValue(String str, Locale locale)
			throws ColumnInterpretException {
		// TODO Auto-generated method stub
		return null;
	}

}
