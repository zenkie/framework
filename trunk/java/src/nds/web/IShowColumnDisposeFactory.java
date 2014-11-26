package nds.web;

import java.util.Locale;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.PageContext;

import org.json.JSONArray;

import nds.control.web.UserWebImpl;
import nds.query.QueryResult;
import nds.query.web.FKObjectQueryModel;
import nds.query.web.TableQueryModel;
import nds.schema.Column;
import nds.schema.DisplaySetting;
import nds.schema.Table;
import nds.util.PairTable;

public interface IShowColumnDisposeFactory {
	public void setTableIndex(int index);			//需要初始化时设置参数
	public void setNamespace(String ns);			//需要初始化时设置参数
	
	public void getHtml(Locale lc,Column column,HttpServletRequest request,int objid,QueryResult result,int currentindex,PageContext pc);
	
	public abstract FKObjectQueryModel  getFKqm();
	
	public abstract PairTable getpt();
	
	public abstract PairTable getvalues();
	
	public abstract String getfcm();
	
	public abstract boolean getISFc();

	public abstract Locale getLocale();
	
	public abstract UserWebImpl getUW();
	
	public abstract Table getTable();
	
	public abstract int getObjid();
	
	public abstract Column getColumn();
	
	public abstract boolean getIsVoid();
	
	public abstract int getAactionType();
	
	public abstract boolean getCanEdit();
	
	public abstract String getInputName();

	public abstract String getCi();

	public abstract boolean getbrv();

	public abstract HttpServletRequest getRequest();

	public abstract Properties getos();
	
	public abstract DisplaySetting getDS();
	
	public abstract TableQueryModel getModel();
	
	public abstract JSONArray getDL();
	
	public abstract int getCurrentIndex();
	
	public abstract String getData();
	
	public abstract void setDWNBSP(String dwnbsp);
	
	public abstract String getDWNBSP();
	
	public abstract int getCoid();
	
	public abstract Object getDDB();
	
	public abstract String getABT();
	
	public abstract PageContext getpc();
	
	public abstract String getFKUT();
	
	public abstract String getTableIndex();
	
	public abstract int getShowCC();
	
	public abstract String getNS();
	
	public abstract org.apache.jasper.runtime.TagHandlerPool getCLibs();
}
