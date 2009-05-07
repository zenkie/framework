/******************************************************************
*
*$RCSfile: CopyItemForMM.java,v $ $Revision: 1.1 $ $Author: Administrator $ $Date: 2006/03/28 02:27:11 $
*

********************************************************************/
package nds.control.ejb.command;

import java.rmi.RemoteException;
import java.util.*;
import java.sql.*;
import nds.schema.*;

import nds.control.ejb.Command;
import nds.control.event.DefaultWebEvent;
import nds.control.util.ValueHolder;
import nds.query.*;
import nds.util.*;
import nds.control.util.*;
import nds.security.*;
import nds.util.Tools;

/**
 * Copy line with different material attributes
 * 
 * 
 * @author yfzhu@agilecontrol.com
 */
public class CopyItemForMM extends Command{


    /**
     * @param event - special parameters:
     *  id 			- reorod id
     *  table		- table of the record (may be virtual)
     *  fillcolumn	- the column that will accept input data
     *  defaultvalue- default value of the attribute instance that has no data set 
     *  notnull		- if set, will not create records that has no data set (not fill with defaultvalue)
     *  Axxxx       - xxx is for m_attributesetinstance id
     *  fixedcolumn - fixed columns (optional)
     *  mainobjecttableid - parent table's id (optional)
     * 
     */
    public ValueHolder execute(DefaultWebEvent event) throws NDSException, RemoteException{
    	//logger.debug(event.toDetailString());
    	TableManager manager= TableManager.getInstance();
    	Table table= manager.getTable(Tools.getInt(event.getParameterValue("table",true),-1));
    	int objectId= Tools.getInt(event.getParameterValue("id",true),-1);
    	
    	// do user has write permission on the selected table?
    	String dir= table.getSecurityDirectory();
    	event.setParameter("directory",  dir);
		User usr =helper.getOperator(event);
    	helper.checkDirectoryWritePermission(event, usr);

    	int clientId= usr.adClientId;
		int orgId= usr.adOrgId;
		int uId= usr.id.intValue();
		QueryEngine engine=QueryEngine.getInstance();
		
		Column fillColumn=manager.getColumn( Tools.getInt((String)event.getParameterValue("fillcolumn"),-1));
		
		boolean bNotDefault= event.getParameterValue("notnull")!=null;
		String defaultValue=(String) event.getParameterValue("defaultvalue");
		int siId;String name,value;
		PairTable values=new PairTable(); // key: instance description (AK), value: input value
		// get all instances and their values
		for(Iterator it=event.getParameterNames();it.hasNext();){
			name= (String)it.next();
			if(name.startsWith("A")){
				siId= Tools.getInt( name.substring(1) , -1);
				if(siId!=-1){
					value=(String) event.getParameterValue(name);
					if(Validator.isNull(value) && !bNotDefault) value= defaultValue;
					if(Validator.isNotNull(value)){
						values.put( engine.doQueryOne("select description from M_ATTRIBUTESETINSTANCE where id="+ siId), value);
					}
				}
			}
		}
		logger.debug( values.toString()) ;
    	PairTable fixedColumns=PairTable.parseIntTable((String)event.getParameterValue("fixedcolumns"), null);
    	String mainobjecttableid= (String)event.getParameterValue("mainobjecttableid");
    	QueryRequestImpl query=CopyTo.getQuery(table,fixedColumns);
    	query.addParam( table.getPrimaryKey().getId(), "="+objectId);
    	logger.debug(query.toSQL());
    	QueryResult result= engine.doQuery(query);
    	if(!result.next()) throw new NDSException("record to be copied not found :table="+ table.getName()+", id="+ objectId );
    	
    	String nextScreen;
    	if(values.size()< 2){
        	nextScreen=WebKeys.NDS_URI+"/object/object.jsp?mainobjecttableid="+mainobjecttableid+"&table="+table.getId()+"&action=input&fixedcolumns="+java.net.URLEncoder.encode(fixedColumns.toURLQueryString(""));
    	}else{
    		nextScreen=WebKeys.NDS_URI+"/objext/object_batchadd.jsp?mainobjecttableid="+mainobjecttableid+"&table="+table.getId()+"&fixedcolumns="+java.net.URLEncoder.encode(fixedColumns.toURLQueryString(""));
    	}
    	ValueHolder vh=new ValueHolder();
    	vh.put("next-screen",nextScreen);
    	if (values.size()==0) {
    		vh.put("message","数据未设置！");
    		return vh;
    	}
    	CollectionValueHashtable ht=new CollectionValueHashtable();
    	
    	int cl=result.getMetaData().getColumnCount();
    	int attributeInstanceIdColumnPos =-1;
    	int fillColumnPos =-1;
    	// only attributesetinstance.description will be shown in result
    	int attributeInstanceIdColumnId=manager.getTable("m_attributesetinstance").getAlternateKey().getId();
    	
    	for(int i=1;i<cl+1;i++){
    		if (result.getMetaData().getColumnId(i) == attributeInstanceIdColumnId) attributeInstanceIdColumnPos=i;
    		else if (result.getMetaData().getColumnId(i)== fillColumn.getId()) fillColumnPos=i;
    	}
    	logger.debug("attributeInstanceIdColumnPos="+ attributeInstanceIdColumnPos+", fillColumnPos="+fillColumnPos);
    	
    	//logger.debug("column count="+ cl);
    	Object v;
    	for(int j=0;j< values.size();j++){
    		for(int i=1 ;i< cl+1;i++){
    			if(i==attributeInstanceIdColumnPos) v = values.getKey(j);
    			else if(i== fillColumnPos) v= values.get( values.getKey(j));
    			else{
	    			v=result.getObject(i);
	    			if(v==null)v="";
	    			if(v instanceof java.util.Date) v= ((java.text.SimpleDateFormat)QueryUtils.inputDateFormatter.get()).format((java.util.Date)v);
    			}
    			ht.add(new Integer(i), v);
    		}
    	}
    	ArrayList scl=query.getAllSelectionColumnLinks();
    	//logger.debug("getAllSelectionColumnLinks count="+ scl.size());
    	DefaultWebEvent e=new DefaultWebEvent("copyitemformm"); 
    	for(int i=0;i< scl.size();i++){
    		Column[] clink=((ColumnLink)scl.get(i)).getColumns();
    		name=clink[0].getName().toLowerCase();
    		for(int j=1;j<clink.length;j++){
    			name += "__" + clink[j].getName().toLowerCase();
    		}
    		//logger.debug("name="+ name);
    		Collection col= ht.get( new Integer(i+1));
    		Object[] ss=(Object[]) col.toArray();
    		
    		e.setParameter(name,ss);
    	}
    	// for check box to be selected
    	String[] sidx= new String[values.size()];
    	for(int i=0;i<values.size();i++){
    		sidx[i]=""+i;
    	}
    	e.setParameter("selectedItemIdx",sidx);
    	e.setParameter(DefaultWebEvent.SELECTER_NAME,"selectedItemIdx" );
    	//logger.debug("generated event:"+ e.toDetailString());
    	vh.put(WebKeys.DEFAULT_WEB_EVENT, e);
    	vh.put("message","数据生成但尚未保存.");
    	vh.put("linecount", String.valueOf(values.size()));// will be used by batch add jsp
    	return vh;
    	

    }
}