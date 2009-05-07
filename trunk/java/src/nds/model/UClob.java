package nds.model;

import nds.model.base.BaseUClob;
import java.io.*;
/**
 * This is the object class that relates to the U_CLOB table.
 * Any customizations belong here.
 */
public class UClob extends BaseUClob {

/*[CONSTRUCTOR MARKER BEGIN]*/
	public UClob () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public UClob (java.lang.Integer _id) {
		super(_id);
	}
/*[CONSTRUCTOR MARKER END]*/
	/**
	 * Get string of clob content, if null, return ""
	 */
	public String getStringContent(){
		String content="";
		try{
		java.sql.Clob clobContent= this.getContent();
		if(clobContent!=null ){
			Reader clobReader = clobContent.getCharacterStream();
	    	if (clobReader != null) {
	    		StringBuffer buffer=new StringBuffer();
			    BufferedReader bufferedClobReader = new BufferedReader(clobReader);
	      		String line = null;
	      		while( (line = bufferedClobReader.readLine()) != null ){
	    			buffer.append(line);
	      		}
	      		bufferedClobReader.close();
	      		content=buffer.toString();
	    	}
		}
		}catch(Exception e){
			content= "Internal error:"+e.getMessage();
		}
		return content;
	}
}