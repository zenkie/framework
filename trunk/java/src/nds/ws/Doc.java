/*
 * Agile Control Technologies Ltd,. CO.
 * http://www.agileControl.com
 */
package  nds.ws;

import java.util.*;
 
/**
 * 
 * @author yfzhu@agilecontrol.com
 */

public class Doc {  
	
	public final static int CODE_OK=0;
	//Code for warning 
	public final static int CODE_DOC_DIFF_SESSION=102;

	//Code for error
	public final static int CODE_INVALID_SESSION=401;
	public final static int CODE_DOC_NOT_FOUND=402;
	public final static int CODE_INSUFFICIENT_PERMISSION=401;
	public final static int CODE_DOC_INTERNAL_ERROR=500;
	
	//Doc type
	public final static String DOCTYPE_REQUEST="request";
	public final static String DOCTYPE_RESPONSE="response";
	
	private int id;
	private String no;
	private String desc;
	private String docType;
	
	private int returnCode=0; // 0 for OK, less than 400 for warning, others for error
	private String returnMessage;
	
	private String[] paramNames;
	private String[] paramValues;

	public Doc(){}
	public Doc(int id,String no, String desc, String doctype, int retCode, String retMsg){
		this.id=id;
		this.no=no;
		this.desc=desc;
		this.docType= doctype;
		this.returnCode=retCode;
		
		this.returnMessage= retMsg;
	}

	public String getDesc() {
		return desc;
	}
	public int getId() {
		return id;
	}
	public String getNo() {
		return no;
	}
	public void setDesc(String desc) {
		this.desc = desc;
	}
	public void setId(int id) {
		this.id = id;
	}
	public void setNo(String no) {
		this.no = no;
	}
	public String getDocType() {
		return docType;
	}
	public void setDocType(String docType) {
		this.docType = docType;
	}
	public String toString(){
		return "docType:"+ docType+",id="+id+",no="+no+",desc="+desc;
	}

	public String[] getParamNames() {
		return paramNames;
	}
	public String[] getParamValues() {
		return paramValues;
	}
	public void setParamNames(String[] paramNames) {
		this.paramNames = paramNames;
	}
	public void setParamValues(String[] paramValues) {
		this.paramValues = paramValues;
	}
	public int getReturnCode() {
		return returnCode;
	}
	public String getReturnMessage() {
		return returnMessage;
	}
	public void setReturnCode(int returnCode) {
		this.returnCode = returnCode;
	}
	public void setReturnMessage(String returnMessage) {
		this.returnMessage = returnMessage;
	}
}
