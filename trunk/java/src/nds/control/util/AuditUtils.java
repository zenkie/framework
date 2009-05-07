/*
 * Agile Control Technologies Ltd,. CO.
 * http://www.agileControl.com
 */
package nds.control.util;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.http.HttpServletRequest;

import java.util.*;

import nds.audit.Program;
import nds.control.ejb.Command;
import nds.control.event.NDSEventException;
import nds.control.web.UserWebImpl;
import nds.control.web.WebUtils;
import nds.log.*;
import nds.query.*;
import nds.util.*;
import nds.schema.*;

import java.sql.*;

/**
 * Handle audit process
 * 
 * @author yfzhu@agilecontrol.com
 */

public class AuditUtils {
	private static Logger logger= LoggerManager.getInstance().getLogger(AuditUtils.class.getName());
	private static int maxAssignCount=-1;
	public static int getMaxAssignCount(){
      	/**
      	 * maxAssign the maximum count of assignment, the count will do increment every time an assignment is done 
    	 * if maximum assignment count is reached, the new assignment will fail
      	 */
		if(maxAssignCount==-1)
			maxAssignCount =Tools.getInt( EJBUtils.getApplicationConfigurations().getProperty("audit.assign.max"),10);
		return maxAssignCount;
		
	}
	/**
	 * 
	 * @param filterObj instanceof java.sql.Clob
	 * @param propName "expr", "sql", "desc" supported 
	 * @return nds.schema.Filter.property specified by propName
	 * @throws NDSException if parse clob to nds.schema.Filter error
	 */
	private static String getFilterProperty(Object filterObj, String propName) throws NDSException{
		String prop=null; // this means filter is empty(all thinks are suitable)
		
		if(filterObj!=null){
			if(filterObj instanceof java.sql.Clob) {
				try{
					filterObj=((java.sql.Clob)filterObj).getSubString(1, (int) ((java.sql.Clob)filterObj).length());
				}catch(Throwable t){
					throw new NDSException(t.getMessage(),t);
				}
        	}else if(!(filterObj instanceof java.lang.String)){
        		throw new NDSException("Internal error: found filterobj neither Clob nor String");
        	}
			nds.schema.Filter f=new nds.schema.Filter();
			f.parse((String)filterObj);
			if("expr".equals(propName))prop= f.getExpression();
			else if("sql".equals(propName))prop= f.getSql();
			else if("desc".equals(propName))prop= f.getDescription();
			else throw new NDSException("Invalid propety:"+ propName);
		}
		logger.debug("getFilterProperty("+(String)filterObj +","+ propName+")="+prop );
		return prop;
	}
	/**
	 * Check if table record specified by objectId belongs to any audit process.
	 * 
	 * If more than one process found, the higher priority one will be returned. 
	 * @param tableId	
	 * @param objectId
	 * @return au_process.id or -1 if not found
	 * @throws Exception
	 */
	public static int getProcess(int tableId, int objectId) throws Exception{
		TableManager manager= TableManager.getInstance();
		Table table= manager.getTable(tableId);
		QueryEngine engine= QueryEngine.getInstance();
		// currently process must be adClientId related.
		if (!table.isAdClientIsolated()) throw new NDSException("Unexpected table type (no ad_client_id column?): tableId="+ tableId);
		int adClientId= Tools.getInt(engine.doQueryOne("select ad_client_id from "+ table.getRealTableName()+" where id="+objectId), -1);
		
		List al=engine.doQueryList("select p.filterobj, p.id from au_process p where p.isactive='Y' "+
				"and p.ad_table_id="+ tableId+" and p.ad_client_id="+ adClientId +" order by p.priority desc");
		
		for(Iterator it=al.iterator();it.hasNext();){
			List p= (List)it.next();
			Object filterObj=p.get(0);
			String exprXML=null;
			try{
				exprXML=getFilterProperty(filterObj,"expr");
			}catch(Throwable t){
				logger.error("fail to get process for table="+table+", id="+ objectId, t);
				continue;
			}
			int processId= Tools.getInt(p.get(1), -1);
			if(Validator.isNull(exprXML)) return processId;
			Expression exp=new Expression(exprXML);
			Expression idExp= new Expression(new ColumnLink(new int[]{table.getPrimaryKey().getId()}),"="+ objectId, null);
			exp= exp.combine(idExp, SQLCombination.SQL_AND, null);
			
			QueryRequestImpl query=engine.createRequest(null);
			query.setMainTable(tableId);
			query.addParam(exp);
			String cntSQL=query.toCountSQL();
			logger.debug(cntSQL);
			int cnt=Tools.getInt(engine.doQueryOne(cntSQL),0);
			if(cnt > 0) return processId;
		}
		return -1;
	}
	/**
	 * Convert object to display format, mainly for date object
	 * @param o
	 * @return
	 */
	private static String convert(Object o){
		if(o==null ) return "";
		if(o instanceof java.util.Date){
			return ((java.text.SimpleDateFormat)QueryUtils.dateFormatter.get()).format((java.util.Date)o);
		}
		return o.toString();
	}
	/**
	 * Create instance for one process instance, and execute the instance
	 * @param tableId
	 * @param objectId
	 * @param processId au_process.id
	 * @param orderno the executed phase should have orderno higher than this one 
	 * @return contains information about result, for value of "state" 
	 * 	 "R"	- Rejected
	 *   "A"	- Accepted
	 * 	 "W"	- Wait
	 * 	 Detailed message will set in value "message"	\
	 *   if "code" is not zero, the status of object will set to 1 to allow for modification(deletion)
	 * @throws Exception
	 */
	public static ValueHolder executeProcess(int tableId, int objectId, int processId, int userId, int orderno, Connection conn) throws Exception{
		QueryEngine engine= QueryEngine.getInstance();
		TableManager manager= TableManager.getInstance();
		Table table= manager.getTable(tableId);
		boolean connFromOut=true;
		if(conn==null){
			conn= QueryEngine.getInstance().getConnection();
			connFromOut=false;
		}
		String processName=(String) engine.doQueryOne("select name from au_process where id="+processId);
		ValueHolder vh=new ValueHolder();
		vh.put("message","@accepted@");
		vh.put("code", "0");
		vh.put("state", "A");
		try{
		List al;
		al=engine.doQueryList("select filterobj, p.id, p.succ_action,p.succ_program,fail_action, fail_program,PERMIT_NUM from  au_phase p where p.au_process_id="+ processId+" and orderno>"+ orderno+" order by p.orderno asc", conn);
		
		for(Iterator it=al.iterator();it.hasNext();){
			List p= (List)it.next();// phase
			int phaseId= Tools.getInt(p.get(1), -1);
			int permitNum=  Tools.getInt(p.get(6), 1);
			Object filterObj=p.get(0);
			String exprXML=null;
			try{
				exprXML=getFilterProperty(filterObj,"expr");
			}catch(Throwable t){
				logger.error("Fail to load filterobj of au_phase id="+ phaseId, t);
				throw new NDSException("Fail to load filterobj of au_phase id="+ phaseId, t);
			}
			
			
			// create brief
			List columns =engine.doQueryList("select ad_column1_id, ad_column2_id, ad_column3_id, name from au_phase where id="+phaseId, conn);
			Column col1= manager.getColumn(Tools.getInt( ((List)columns.get(0)).get(0), -1));
			Column col2= manager.getColumn(Tools.getInt( ((List)columns.get(0)).get(1), -1));
			Column col3= manager.getColumn(Tools.getInt( ((List)columns.get(0)).get(2), -1));
			String phaseName= (String) ((List)columns.get(0)).get(3);
			
			QueryRequestImpl query= engine.createRequest(null);
			query.setMainTable( table.getId() ,false); // table may change status
			query.addSelection(table.getAlternateKey().getId());
			if(col1!=null)
				if(col1.getReferenceTable()!=null) query.addSelection(col1.getId(), col1.getReferenceTable().getAlternateKey().getId(), false);
				else query.addSelection(col1.getId());
			if(col2!=null)
				if(col2.getReferenceTable()!=null) query.addSelection(col2.getId(), col2.getReferenceTable().getAlternateKey().getId(), false);
				else query.addSelection(col2.getId());
			if(col3!=null)
				if(col3.getReferenceTable()!=null) query.addSelection(col3.getId(), col3.getReferenceTable().getAlternateKey().getId(), false);
				else query.addSelection(col3.getId());
			
			query.addParam(new Expression(new ColumnLink(new int[]{table.getPrimaryKey().getId()}) , "="+objectId,null));
			List q=engine.doQueryList(query.toSQL(),conn);
			String docno="";
			StringBuffer brief=new StringBuffer();
			if(q!=null && q.size()>0 ){
				if(q.get(0) instanceof List ){
					List descs= (List)q.get(0);
					docno=descs.get(0).toString();
					if(col1!=null) brief.append(col1.getDescription(manager.getDefaultLocale())).append(":")
						.append(convert(descs.get(1))).append(";");
					if(col2!=null) brief.append(col2.getDescription(manager.getDefaultLocale())).append(":")
						.append(convert(descs.get(2))).append(";");
					if(col3!=null) brief.append(col3.getDescription(manager.getDefaultLocale())).append(":")
						.append(convert(descs.get(3))).append(";");
				}else{
					// q.get(0) is not list, then it must be the docno
					docno= (q.get(0)).toString();
				}
			}
			//create phase instance
			int phaseInstanceId= engine.getSequence(manager.getTable("au_phaseinstance").getName());
			
			String sql2="insert into au_phaseinstance(id, ad_client_id, ad_org_id, isactive, ownerid, modifierid, creationdate, " +
			"modifieddate, au_phase_id,au_process_id,ad_table_id,record_id, record_docno,DESCRIPTION) select "+phaseInstanceId+", ad_client_id," +
			"ad_org_id, 'Y', "+ userId+","+userId+",sysdate,sysdate,"+phaseId+","+ processId+","+tableId+","+ objectId+
			","+ QueryUtils.TO_STRING(docno, 80)+","+  QueryUtils.TO_STRING(brief.toString(),500)+ " from users where id="+ userId;
			logger.debug(sql2);
			
			if (conn.createStatement().executeUpdate(sql2)!=1)
				throw new NDSException("Fail to insert phase instance");
			//phase instance members
			/**
			 * 支持4种方式定义的审核人, 还要防止重复：
			 * au_phase.userfilter: 满足过滤器的所有用户
			 * au_phase_user.ad_user_id : 一个指定的用户
			 * au_phase_user.userlink: 可以设置以待审核单据任何属性开始的columnlink，最终必须指向 users表
				例如，作用于采购单的流程阶段，通过 m_purchase.ownerid;c_department_id;manager_id
				能够构造出由采购单创建人的部门经理审批这样的审核人
			   au_phase_user.groupid: 用户组，属于该组的用户都是审核人

			 */
			StringBuffer sb=new StringBuffer();
			// userfilter
			String ufSQL=getFilterProperty(engine.doQueryOne("select userfilter from au_phase where id="+phaseId, conn),"sql");
			if(Validator.isNotNull(ufSQL)){
				sb.append("select distinct id ad_user_id from users where id "+ ufSQL+" union ");
			}
			//single user au_phase_user.ad_user_id
			sb.append("select distinct ad_user_id from au_phase_user where isactive='Y' and ad_user_id is not null"+
					" and exists(select 1 from users u where u.id=au_phase_user.ad_user_id) and au_phase_id="+phaseId);
			//au_phase_user.groupid
			sb.append(" union select distinct g.userid from au_phase_user p, groupuser g where p.isactive='Y' and p.groupid=g.groupid and "+
					"exists(select 1 from users u where u.id=g.userid) and p.au_phase_id="+phaseId);
			//au_phase_user.userlink
			List userlinks=engine.doQueryList("select userlink from au_phase_user where isactive='Y' and userlink is not null and au_phase_id="+phaseId, conn);
			StringBuffer userIds=new StringBuffer();
			for(int ui=0;ui<userlinks.size();ui++){
				String userLink= (String)userlinks.get(0);
				int linkedUserId= getUserIdByColumnLink(userLink, table, objectId,conn);
				if(linkedUserId!=-1)userIds.append(linkedUserId).append(",");
			}
			if(userIds.length()>0){
				userIds.deleteCharAt(userIds.length()-1);
				sb.append(" union select distinct id from users where id in("+
					userIds.toString()+")");
			}
			sql2="insert into au_pi_user(id,ad_client_id, ad_org_id, isactive, ownerid, modifierid, creationdate, " +
			"modifieddate, au_pi_id,ad_user_id,state) select get_sequences('au_pi_user'), ad_client_id, ad_org_id, 'Y',"+ 
			userId+","+ userId+",sysdate,sysdate, "+ phaseInstanceId+",ad_user_id,'W' from ("+ sb.toString()+") a, users u where u.id="+ userId;
			
			logger.debug(sql2);
			int auditUserCount=conn.createStatement().executeUpdate(sql2);
			
			
			/*sql2="insert into au_pi_user(id,ad_client_id, ad_org_id, isactive, ownerid, modifierid, creationdate, " +
					"modifieddate, au_pi_id,ad_user_id,state) select get_sequences('au_pi_user'), ad_client_id, ad_org_id, 'Y',"+ 
					userId+","+ userId+",sysdate,sysdate, "+ phaseInstanceId+",ad_user_id,'W' from au_phase_user where ad_user_id is not null"+
					" and exists(select 1 from users u where u.id=au_phase_user.ad_user_id) and au_phase_id="+phaseId;
			logger.debug(sql2);
			conn.createStatement().executeUpdate(sql2);
			//au_phase_user.groupid
			sql2="insert into au_pi_user(id,ad_client_id, ad_org_id, isactive, ownerid, modifierid, creationdate, " +
					"modifieddate, au_pi_id,ad_user_id,state) select get_sequences('au_pi_user'), p.ad_client_id, p.ad_org_id, 'Y',"+ 
					userId+","+ userId+",sysdate,sysdate, "+ phaseInstanceId+",g.userid,'W' from au_phase_user p, groupuser g where p.groupid=g.id and "+
					"exists(select 1 from users u where u.id=g.userid) and p.au_phase_id="+phaseId;
			logger.debug(sql2);
			conn.createStatement().executeUpdate(sql2);
			//au_phase_user.userlink
			List userlinks=engine.doQueryList("select userlink from au_phase_user where userlink is not null and au_phase_id="+phaseId, conn);
			StringBuffer userIds=new StringBuffer();
			for(int ui=0;ui<userlinks.size();ui++){
				String userLink= (String)userlinks.get(0);
				int linkedUserId= getUserIdByColumnLink(userLink, table, objectId,conn);
				if(linkedUserId!=-1)userIds.append(linkedUserId).append(",");
			}
			if(userIds.length()>0){
				userIds.deleteCharAt(userIds.length()-1);
				sql2="insert into au_pi_user(id,ad_client_id, ad_org_id, isactive, ownerid, modifierid, creationdate, " +
					"modifieddate, au_pi_id,ad_user_id,state) select get_sequences('au_pi_user'), p.ad_client_id, p.ad_org_id, 'Y',"+ 
					userId+","+ userId+",sysdate,sysdate, "+ phaseInstanceId+",p.id,'W' from users p where p.id in("+
					userIds.toString()+")";
				logger.debug(sql2);
				conn.createStatement().executeUpdate(sql2);
			}*/
			
			// do auto assign, if any pi_user set is_out='Y'
			
			ArrayList params=new ArrayList();
			params.add(new Integer(phaseInstanceId));
			params.add(new Integer(getMaxAssignCount()));
			SPResult res= engine.executeStoredProcedure("au_phaseinstance_auto_assign", params, true , conn);
			if(res.getCode()!=0) throw new NDSException("@exception@:("+docno+")"+res.getMessage());
			
			// check flow condition
			Expression expr= new Expression(new ColumnLink(new int[]{table.getPrimaryKey().getId()}),"="+ objectId, null);
			if (Validator.isNotNull(exprXML)){
				Expression exp=new Expression(exprXML);
				expr= expr.combine(exp, SQLCombination.SQL_AND, null);
			}
			query=engine.createRequest(null);
			query.setMainTable(tableId,false); // do not include filter, since many view table use status as filter (=1) item.
			query.addParam(expr);
			
			int cnt=Tools.getInt(engine.doQueryOne(query.toCountSQL(),conn),0);
			
			String action, program;
			boolean isSuccessAction;
			if(cnt > 0){
				action= (String)p.get(2);
				program=(String)p.get(3);
				isSuccessAction=true;
			}else{
				action= (String)p.get(4);
				program=(String)p.get(5);
				isSuccessAction=false;
			}

			if("P".equals(action)){
				action= executeProgram(program,phaseInstanceId );
			}
			//如果进入待批状态，但待批的审核人数不能达到要求，也应该驳回
			if("W".equals(action) ){
				if(permitNum>auditUserCount){
					logger.debug("permit num ("+permitNum+") cound not be satisfied as audit user count is "+auditUserCount);
					action="R";
				}
			}
			vh.put("state", action);
			sql2="update au_phaseinstance set state='"+action+"' where id="+ phaseInstanceId;
			logger.debug(sql2);
			conn.createStatement().executeUpdate(sql2);
			syncObject(table, objectId, phaseInstanceId, action, conn);
			if("R".equals(action)){
				vh.put("message","@rejected@,@audit-process-name@:"+processName+",@audit-phase-name@:"+ phaseName);
				vh.put("code", "-2"); // so can let object do submit again
				break;
			}else if("A".equals(action)){
				//move to next
				vh.put("message", "@accepted@");
				vh.put("code", "0");
			}else if("W".equals(action)){
				vh.put("message","@wait-for-approve@,@audit-process-name@:"+processName+",@audit-phase-name@:"+ phaseName);
				vh.put("code", "0");
				//stop
				break;
			}else{
				throw new NDSException("Unsupported action:"+ action);
			}				
			
		}//end phases iteration
		}finally{
			try{if(!connFromOut)conn.close();}catch(Throwable t){}
		}
		//update record status
		return vh;
	}
	/**
	 * 获取 tableId,objectId 所指向的表的物理记录上，对应的clink 字段所指向的user
	 * 例如，作用于采购单的流程阶段，通过 m_purchase.ownerid;c_department_id;manager_id
				能够构造出由采购单创建人的部门经理审批这样的审核人
				
	 * @param clink 必须起始于tableId 所在的表，中止于users表
	 * @param table 描述所在表
	 * @param objectId 描述记录id
	 * @return -1 if found error
	 */
	private static int getUserIdByColumnLink(String clink, Table table, int objectId,Connection conn ){
		int userId=-1;
		try{
			ColumnLink cl=new ColumnLink(clink);
			Table userTable= TableManager.getInstance().getTable("users");
			if(!cl.getColumns()[0].getTable().equals(table)|| ! userTable.equals( cl.getLastColumn().getReferenceTable())){
				logger.error("Find an error column link in au_phase_user:"+ clink+", either not start from "+ table+" or not ends in USERS");
				return userId;
			}
			QueryRequestImpl q=QueryEngine.getInstance().createRequest(null);
			q.setMainTable(table.getId());
			q.addSelection(cl.getColumnIDs(), false,null);
			q.addParam(table.getPrimaryKey().getId(),"="+objectId);
			userId= Tools.getInt(QueryEngine.getInstance().doQueryOne(q.toSQL(),conn),-1);
		}catch(Throwable t){
			logger.error("Fail to load user from clink :"+ clink+" of table "+ table +", with id="+ objectId,t);
		}
		return userId;
	}
	/**
	 * Execute program to decide state for the phase instance 
	 * @param name should be instance of nds.audit.Program
	 * @param phaseInstanceId
	 * @return "R", "A", "W" for reject, accept and wait state of phase instance
	 */
	public static String executeProgram(String name, int phaseInstanceId) throws Exception{
		Class c=null;
		Program prg;
            // try figure the special command name, such as PromotionAShtSubmit
       	c= Class.forName(name);
        prg=(Program) c.newInstance();
        return prg.execute(phaseInstanceId);
	}
	
	/**
	 * Cancel phase instance specified
	 * @param phaseInstanceId au_phaseinstance id to be handled
	 * @param userId the user that request assignment, internal process should make sure that the user
	 *  has the right to do this cancellation
	 * @return message of assignment, code should always be 0, since any error should be thrown out
	 * @throws Exception if any error occurs
	 */
	public static ValueHolder doCancelAssign(int phaseInstanceId,int userId) throws Exception{
		ValueHolder vh=new ValueHolder();
		QueryEngine engine= QueryEngine.getInstance();
		// 	check instance state, the permit num
		ArrayList params=new ArrayList();
		params.add(new Integer(phaseInstanceId));
		params.add(new Integer(userId));
		SPResult res= engine.executeStoredProcedure("au_phaseinstance_cancel_assign", params, true );
		if(res.getCode()!=0) throw new NDSException("@exception@:"+ res.getMessage());
		vh.put("message", res.getMessage());
		vh.put("code", "0");
		return vh;
		
	}
	/**
	 * Assign phase instance specified to the user of <param>assigneeId</param>
	 * @param phaseInstanceId au_phaseinstance id to be handled
	 * @param assigneeId  assignee user id
	 * @param userId the user that request assignment, internal process should make sure that the user
	 *  has the right to do this assignment
	 * @return message of assignment, code should always be 0, since any error should be thrown out
	 * @throws Exception if any error occurs
	 */
	public static ValueHolder doAssign(int phaseInstanceId,int userId, int assigneeId) throws Exception{
		ValueHolder vh=new ValueHolder();
		QueryEngine engine= QueryEngine.getInstance();
		// 	check instance state, the permit num
		ArrayList params=new ArrayList();
		params.add(new Integer(phaseInstanceId));
		params.add(new Integer(userId));
		params.add(new Integer(assigneeId));
		params.add(new Integer(getMaxAssignCount()));
		SPResult res= engine.executeStoredProcedure("au_phaseinstance_assign", params, true );
		if(res.getCode()!=0) throw new NDSException("@exception@:"+ res.getMessage());
		vh.put("message", res.getMessage());
		vh.put("code", "0");
		return vh;
		
	}
	/**
	 * After phase instance status updated, proceed the porcess
	 * @param phaseInstanceId
	 * @param userId the executer
	 * @return contains information about result, for value of "state" 
	 * 	 "R"	- Rejected
	 *   "A"	- Accepted
	 * 	 "W"	- Wait
	 * 	 Detailed message will set in value "message"	\
	 *   "code" 
	 * 	 0 		-	no error found
	 *   <>0	-	there's error
	 	 "docno" document no that handled
	 * @throws Exception
	 */
	public static ValueHolder executePhaseInstance(int phaseInstanceId, int userId) throws Exception{
		ValueHolder vh=new ValueHolder();
		QueryEngine engine= QueryEngine.getInstance();
		Connection conn= engine.getConnection();
		try{
		Statement stmt= conn.createStatement();

		List piInfo= engine.doQueryList("select pi.ad_table_id, pi.record_id,pi.AU_PROCESS_ID,p.orderno,pi.RECORD_DOCNO, pi.state from au_phaseinstance pi, au_phase p where p.id=pi.au_phase_id and pi.id="+phaseInstanceId,conn);
		int tableId= Tools.getInt(((List)piInfo.get(0)).get(0)  ,-1);
		int objectId= Tools.getInt(((List)piInfo.get(0)).get(1)  ,-1);
		int processId=Tools.getInt(((List)piInfo.get(0)).get(2)  ,-1);
		int orderno=Tools.getInt(((List)piInfo.get(0)).get(3)  ,-1);
		String docno= (String) ((List)piInfo.get(0)).get(4);
		String state= (String)((List)piInfo.get(0)).get(5);
		
			vh.put("code", "0");
			
			Table table= TableManager.getInstance().getTable(tableId);
			
			if("R".equals(state)){
				// whole instance rejected
				syncObject(table, objectId, phaseInstanceId, "R", conn);
	        	// set status of object to 1 if that column exists
	        	if(table.getColumn("status")!=null)
	        		stmt.executeUpdate("update "+ table.getRealTableName()+" set status=1 where id="+ objectId);
				
				vh.put("message", docno+" rejected.");
				vh.put("state", "R");
			}else if("W".equals(state)){
				// still wait
				vh.put("message", docno+" still wait.");
				vh.put("state", "W");
			}else if("A".equals(state)){
				syncObject(table, objectId, phaseInstanceId, "A", conn);
				// accepted, so do next phase, if exists. if not exists, whole process ends
				vh=executeProcess(tableId, objectId,processId, userId,orderno,conn );
				vh.put("table", table); // this value will be used by "ExecuteAudit" Command or "ExecuteAuditTimeout" Command
				vh.put("objectid", new Integer(objectId));// this value will be used by "ExecuteAudit" Command or "ExecuteAuditTimeout" Command
			}else{
				throw new NDSException("Unexpected state("+ state+") of phase instance id="+phaseInstanceId);
			}
		
		vh.put("docno", docno);
		return vh;
		}finally{
			try{conn.close();}catch(Throwable t){}
		}		
	}
	/**
	 * Reject or accpet on specified instance
	 * Check if the rejection of one user will trigger the process instance to move to another phase.
	 * If do, proceed.
	 * @param phaseInstanceId
	 * @param userId
	 * @param accept true for acceptance, false for rejection
	 * @param comments
	 * @return contains information about result, for value of "state" 
	 * 	 "R"	- Rejected
	 *   "A"	- Accepted
	 * 	 "W"	- Wait
	 * 	 Detailed message will set in value "message"	\
	 *   "code" 
	 * 	 0 		-	no error found
	 *   <>0	-	there's error
	 	 "docno" document no that handled
	 * @throws Exception
	 */
	public static ValueHolder doAudit(int phaseInstanceId, int userId,boolean accept, String comments) throws Exception{
		ValueHolder vh=new ValueHolder();
		QueryEngine engine= QueryEngine.getInstance();
		Connection conn= engine.getConnection();
		try{
		Statement stmt= conn.createStatement();
		// this update may affect more than one records
		// 在以下情况下，执行审核意见将修改一条以上记录：
		// 本人是原始审核人之一，而同时又被其他原始审核人赋予了代办人的权利。
		// 此更新语句将仅仅修改状态为待办的审核明细，而对已经修改的保持不动
		int cnt=stmt.executeUpdate("update au_pi_user set modifieddate=sysdate,state='"+(accept?"A":"R") +"', comments="+ QueryUtils.TO_STRING(comments, 255)+
				" where au_pi_id="+ phaseInstanceId+" and ((ad_user_id="+userId+" and assignee_id is null ) or (assignee_id="+ userId+")) and state='W'");

		List piInfo= engine.doQueryList("select pi.ad_table_id, pi.record_id,pi.AU_PROCESS_ID,p.orderno,pi.RECORD_DOCNO from au_phaseinstance pi, au_phase p where p.id=pi.au_phase_id and pi.id="+phaseInstanceId,conn);
		int tableId= Tools.getInt(((List)piInfo.get(0)).get(0)  ,-1);
		int objectId= Tools.getInt(((List)piInfo.get(0)).get(1)  ,-1);
		int processId=Tools.getInt(((List)piInfo.get(0)).get(2)  ,-1);
		int orderno=Tools.getInt(((List)piInfo.get(0)).get(3)  ,-1);
		String docno= (String) ((List)piInfo.get(0)).get(4);
		
		if(cnt ==0){
			// do nothing, just return
			vh.put("code", "-1");
			vh.put("message", "@no-record-changed@");
			vh.put("state", "W");
		}else{
			vh.put("code", "0");
			
			Table table= TableManager.getInstance().getTable(tableId);
			
			
			// 	check instance state, the permit num
			ArrayList params=new ArrayList();
			params.add(new Integer(phaseInstanceId));
			SPResult res= engine.executeStoredProcedure("au_phaseinstance_update_state", params, true ,conn);
			

			if(res.getCode()==1){
				// whole instance rejected
				syncObject(table, objectId, phaseInstanceId, "R", conn);
	        	// set status of object to 1 if that column exists
	        	if(table.getColumn("status")!=null)
	        		stmt.executeUpdate("update "+ table.getRealTableName()+" set status=1 where id="+ objectId);
				
				vh.put("message", res.getMessage());
				vh.put("state", "R");
			}else if(res.getCode()==2){
				// still wait
				vh.put("message", res.getMessage());
				vh.put("state", "W");
			}else if(res.getCode()==3){
				syncObject(table, objectId, phaseInstanceId, "A", conn);
				// accepted, so do next phase, if exists. if not exists, whole process ends
				vh=executeProcess(tableId, objectId,processId, userId,orderno,conn );
				vh.put("table", table); // this value will be used by "ExecuteAudit" Command or "ExecuteAuditTimeout" Command
				vh.put("objectid", new Integer(objectId));// this value will be used by "ExecuteAudit" Command or "ExecuteAuditTimeout" Command
			}else{
				throw new NDSException("("+ res.getCode()+")"+ res.getMessage());
			}
		}
		vh.put("docno", docno);
		return vh;
		}finally{
			try{conn.close();}catch(Throwable t){}
		}
	}
	
	/**
	 * Update state of object according to phase instance state
	 * @param phaseInstanceId
	 * @throws Exception
	 */
	private static void syncObject(Table tb, int objectId ,int phaseInstanceId, String state, Connection conn) throws Exception{
		TableManager manager=TableManager.getInstance();
		Table table =manager.getTable(tb.getRealTableName());
		Column col=table.getColumn("au_pi_id");
		String sql=null;
			
		if (col!=null && col.getReferenceColumn()!=null && col.getReferenceColumn().getId()== manager.getTable("au_phaseinstance").getPrimaryKey().getId()){
			sql= " au_pi_id="+ phaseInstanceId;
		}
		col= table.getColumn("au_state");
		if(col!=null && col.isValueLimited()){
			if(sql==null)sql = " au_state='"+ state+"'";
			else sql += ",au_state='"+ state+"'";
		}
		col= table.getColumn("status");
		if(("W".equals(state)|| "R".equals(state)) && col!=null){
			if(sql!=null)sql+=",status=" + ("W".equals(state)?3:1);
			else sql="status=" + ("W".equals(state)?3:1);
		}

		if(sql!=null){
			sql = "update " + table.getRealTableName()+ " set "+sql + " where id="+ objectId;
			logger.debug(sql);
			conn.createStatement().executeUpdate(sql);
		}
	}
	private static QueryRequestImpl createRequest(HttpServletRequest request, boolean showAssignment) throws Exception{
		UserWebImpl userWeb = ((UserWebImpl)WebUtils.getSessionContextManager(request.getSession(true)).getActor(nds.util.WebKeys.USER));	
		QueryEngine engine=QueryEngine.getInstance();
		QueryRequestImpl query= engine.createRequest( userWeb.getSession());
		TableManager manager= TableManager.getInstance();
		Table table= manager.getTable("au_phaseinstance");
		query.setMainTable(table.getId());
		// select
		query.addSelection(table.getPrimaryKey().getId());
		query.addSelection(manager.getColumn("au_phaseinstance", "AU_PROCESS_ID").getId(),
				manager.getColumn("au_process", "name").getId(), false);
		query.addSelection(manager.getColumn("au_phaseinstance", "ad_table_id").getId());
		query.addSelection(manager.getColumn("au_phaseinstance", "record_docno").getId());
		query.addSelection(manager.getColumn("au_phaseinstance", "record_id").getId());
		query.addSelection(manager.getColumn("au_phaseinstance", "DESCRIPTION").getId());
		query.addSelection(manager.getColumn("au_phaseinstance", "creationdate").getId());
		
		
        // where 
		Expression expr, expr2;
		if(!showAssignment){
			// include my jobs that not assign to other one, and jobs that assigned to me
			expr=new Expression(null, "exists(select 1 from au_pi_user u where u.au_pi_id=AU_PHASEINSTANCE.ID "+
				"and u.state='W' and ((u.ad_user_id="+ userWeb.getUserId()+ " and u.assignee_id is null ) " +
				" or u.assignee_id="+ userWeb.getUserId()+"))", null);
		}else{
			// jobs that i assigned to others
			expr=new Expression(null, "exists(select 1 from au_pi_user u where u.au_pi_id=AU_PHASEINSTANCE.ID "+
					"and u.state='W' and u.ad_user_id="+ userWeb.getUserId()+ " and u.assignee_id is not null)", null);
		}	
		// limit to state W
		expr2=new Expression(new ColumnLink(new int[]{manager.getColumn("au_phaseinstance", "state").getId()}),
				"=W", " is Wait");
		
		expr2= expr.combine(expr2,SQLCombination.SQL_AND,null);
		query.addParam(expr2);
		query.setOrderBy(new int[]{manager.getColumn("au_phaseinstance", "creationdate").getId()}, true);
		return query;		
	}
	/**
	 * 
	 * @param request
	 * @param maxRecords
	 * @param startIdx
	 * @param showAssignment if true, will only show instances that created by current user, but assigned to other user
	 * @return
	 * @throws Exception
	 */
	public static QueryResult findWaitingInstances(HttpServletRequest request, int maxRecords, int startIdx , boolean showAssignment) throws Exception{
		QueryEngine engine=QueryEngine.getInstance();
		
		QueryRequestImpl query=createRequest(request, showAssignment);
		// range
		query.setRange( startIdx, maxRecords);
		
		logger.debug(query.toSQL());
		return engine.doQuery( query);
	}
	public static int getTotalCount(HttpServletRequest request, boolean showAssignment) throws Exception{
		QueryEngine engine=QueryEngine.getInstance();
		QueryRequestImpl query=createRequest(request,showAssignment);
		// range
		logger.debug(query.toCountSQL());
		return engine.getTotalResultRowCount( query);
		
	}	
}

