/*
 * Agile Control Technologies Ltd,. CO.
 * http://www.agileControl.com
 */
package nds.web.alert;

import nds.query.QueryResult;
import nds.schema.*;
import nds.util.Tools;

import java.util.*;
import java.math.*;

/**
 * When value is positive, load class "positive-row"
 * @author yfzhu@agilecontrol.com
 */

public class AuditStateAlerter extends ColumnAlerterSupport{
	private final static String RequestAlerterCss="request-row";
	private final static String RejectAlerterCss="reject-row";
	private final static String AcceptAlerterCss="accept-row";
	private final static String AuditingAlerterCss="auditing-row";
	
	Legend legend;
	public AuditStateAlerter(){
		legend=new Legend();
		legend.addItem(RequestAlerterCss, "request");
		legend.addItem(RejectAlerterCss, "reject");
		legend.addItem(AcceptAlerterCss, "accept");
		legend.addItem(AuditingAlerterCss, "auditing");
	}
	/**
	 * How to describe this alerter
	 * @return Legend which contains items that description each style of alert
	 */
	public Legend getLegend(Column column){return legend;}
	/**
	 * Get css class of current row in result set, current column
	 * is specified, start from 1. Normally this is used in object list table
	 * @param rs
	 * @param column start from 1
	 * @return such as "red-row" or "bold-row"
	 */
	public String getRowCssClass(QueryResult rs, int column, Column col){
			int audit_state=Tools.getInt(rs.getObject(column), -1);
			String s;
			if(audit_state<0){
				s=null;
			}else{
			if(audit_state%2==1){
				if(Tools.getInt(col.getDefaultValue(), 1)== audit_state){
					s= RequestAlerterCss;
				}else s= RejectAlerterCss;
			}else{
				if(audit_state==0) s=AcceptAlerterCss;
				else s=AuditingAlerterCss;
			}}
			return s;
	}
	
	private static  AuditStateAlerter instance;
	public static AuditStateAlerter getInstance(){
		if(instance==null){
			instance= new AuditStateAlerter();
		}
		return instance;
	}
	
}
