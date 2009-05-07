package nds.control.ejb.command;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;

import nds.control.ejb.Command;
import nds.control.event.DefaultWebEvent;
import nds.control.event.NDSEventException;
import nds.control.util.SecurityUtils;
import nds.control.util.ValueHolder;
import nds.query.*;
import nds.schema.*;
import nds.security.User;
import nds.util.NDSException;
import nds.util.StringUtils;
import nds.util.Tools;
import nds.util.Validator;


/**
 * Create SMS report task that will run by scheduler
 *
 */

public class CreateSMSReport extends Command {
	/**
	 * 
	 * @param event params:
	 * 	request - QueryRequest the query that generate report on. Will retrieve filter expression only.
	 *  The select clause will reconstructed with only columns to be printed in sublist(no. 9 in mask)(xxxxxxxx?x) 
	 * 
	 *  recordno - when creating process instance, system will first check is there records of the 
	 *    same recordno exists, if exists, will delete before create new one.
	 * 
	 *  reportname - contains variable $D/Y/M/m/H for day,year,month,minute,hour
	 *    for instance $D will show as "14d" when day is 14
	 *  groupname- groups ak.  Only root can set this param
	 *  lines   - max lines that the query will return
	 *  priority- priorty, the higher the number, the higher the priority. Only root can set this param
	 *  ad_processqueue_name - queue name 
	 */
  public ValueHolder execute(DefaultWebEvent event) throws RemoteException, NDSException {
  	Connection conn=null;
  	PreparedStatement pstmt=null;
  	
  	
    try{
    	conn= QueryEngine.getInstance().getConnection();
    	// check user sms creation permission
    	User user=helper.getOperator(event);
    	int userId=user.getId().intValue();
    	if(!"Y".equals( QueryEngine.getInstance().doQueryOne("select issms from users where id="+ userId, conn))){
    		throw new NDSEventException("@no-permission@");
    	}
	    QueryRequestImpl request=(QueryRequestImpl) event.getParameterValue("request",true);
	    int mainTableId =request.getMainTable().getId();
	    Expression whereExpr= request.getParamExpression();
	    int[] orderLink=request.getOrderColumnLink();
	    boolean asc=request.isAscendingOrder();

	    // The select clause will reconstructed with only columns to be printed in list(no. 8 in mask)(xxxxxxx?xxx) 
	    request= QueryEngine.getInstance().createRequest(event.getQuerySession());
	    request.setMainTable(mainTableId );
	    request.addAllShowableColumnsToSelection(Column.PRINT_SUBLIST, false);
	    //Here has bugs, since query may contain other conditions that not use addParam(Expression)
	    // method in QueryRequestImpl, such as addParam(int, boolean)
	    request.addParam(whereExpr);
	    if(orderLink!=null)request.setOrderBy(orderLink, asc);
	    
	    String ad_processqueue_name=(String)event.getParameterValue("ad_processqueue_name",true);
	    int lines=Tools.getInt(event.getParameterValue("lines",true),20);

	    request.setRange(0, lines);

	    String groupName=null;
	    int priority=1;
	    if(user.getName().equals("root")){
	    	priority=Tools.getInt(event.getParameterValue("priority",true),1);
	    	groupName=(String)event.getParameterValue("groupname",true);
	    }

	    //	  create ad_pinstance into specified queue
	    String recordno=(String) event.getParameterValue("recordno",true);
	    if(Validator.isNull(recordno)) recordno="";
	    
        ArrayList list =new ArrayList();
        list.add(user.getId());
        list.add(ad_processqueue_name);
        //if record no is same, will delete the old one first
        list.add(request.getMainTable().getName()+ "_"+recordno);
        list.add("nds.process.CreateSMSReport");
        // report name can not have any of ";="
        String reportName=(String)event.getParameterValue("reportname",true);
        reportName= StringUtils.replace(reportName, ";",",");
        reportName= StringUtils.replace(reportName, "=","-");
        // pinstance_para
        String param= "priority="+priority+";reportname="+reportName ;
        if(groupName!=null)param+=";group="+groupName;
        
        list.add(param);
        
        ArrayList res=new ArrayList();
        res.add(Integer.class);
        java.util.Collection result=QueryEngine.getInstance().executeFunction("ad_pinstance_create", list,res,conn );
        
        // sp returned value contains create pinstance id
        int pId=Tools.getInt(result.toArray()[0],-1);
        if(pId!=-1){
        	// insert query to para named "query"
        	pstmt= conn.prepareStatement("insert into ad_pinstance_para(id, ad_client_id,ad_org_id,ad_pinstance_id,name,info,isactive) values (get_sequences('ad_pinstance_para'),?,?,?,?,?,'Y')");
        	pstmt.setInt(1, user.adClientId);
        	pstmt.setInt(2, user.adOrgId);
        	pstmt.setInt(3, pId);
        	pstmt.setString(4, "query");
        	String sql= request.getSQLForReportWithRange(false,true);
        	if(sql.length()>4000) throw new NDSEventException("@fail-to-create-task@:query too long(>4000)");
        	pstmt.setString(5, sql);
        	int cnt=pstmt.executeUpdate();
        	if(cnt<1) throw new NDSEventException("@fail-to-create-task@:param 'query' could not insert");
        }else{
        	throw new NDSEventException("@fail-to-create-task@:pinstanceid=-1");
        }
        
	    ValueHolder v=new ValueHolder();
	    v.put("message", "@task-generated@");
	    return v;
	 }catch(Throwable e){
	 	logger.error("", e);
	 	if(!(e instanceof NDSException ))throw new NDSEventException("Òì³£", e);
	 	else throw (NDSException)e;
    }finally{
    	if(pstmt!=null){
    		try{pstmt.close();}catch(Throwable t){}
    	}
    	if(conn!=null){
    		try{conn.close();}catch(Throwable t){}
    	}
    }
  }
}