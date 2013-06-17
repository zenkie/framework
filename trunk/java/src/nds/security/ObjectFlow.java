package nds.security;

import java.sql.Connection;
import java.util.*;

import nds.control.util.AuditUtils;
import nds.db.oracle.DatabaseManager;
import nds.log.Logger;
import nds.log.LoggerManager;
import nds.query.QueryEngine;
import nds.schema.Filter;
import nds.schema.TableManager;
import nds.util.NDSRuntimeException;
import nds.util.Validator;
import org.json.JSONArray;
import org.json.JSONObject;

public class ObjectFlow {
	
	        private int tableid;
	        private int objectid;
	        private ArrayList<Workflow> flows;
	        private HashMap<Integer, Process> au_process;
	        private HashMap<Integer, Phase> au_phase;
	        private String docno;
	        private static Logger logger= LoggerManager.getInstance().getLogger(DatabaseManager.class.getName());
	        
	        public ObjectFlow(int tableid, int objectid, int userid)
	        		throws Exception{
	        	this.tableid = tableid;
	        	this.objectid = objectid;
	        	this.flows = new ArrayList();
	        	this.au_process = new HashMap();
	        	this.au_phase = new HashMap();
	        	init();
	        	}
	        		      
	        private void init() throws Exception {
	        	QueryEngine engin= QueryEngine.getInstance();
	        	Connection conn=engin.getConnection();
	        	JSONArray jsonarray;
	        	int lpinsid, lphaseid, lprocessid;
	        	String lstate;
	        	Workflow workflow = null;
	        	jsonarray = engin.doQueryObjectArray(
	        					"select id, au_phase_id, au_process_id, state, to_char(modifieddate,'YYYY/MM/DD HH24:MI') "
	        							+ "modifieddate from au_phaseinstance where ad_table_id=? and record_id=? order by id asc",
	        							new Object[] {Integer.valueOf(tableid),
	        									Integer.valueOf(objectid) }, conn, false);
	        
	        	lpinsid = -1;
	        	lphaseid = -1;
	        	lprocessid = -1;
	        	lstate = "R";

	        	for (int l = 0; l < jsonarray.length(); l++) {
	        		ObjectFlow objectflow = this;
	        		Process process;
	        		JSONObject jor;
	        		jor = jsonarray.getJSONObject(l);
	        		int pinsid = jor.getInt("id");
	        		int phaseid = jor.getInt("au_phase_id");
	        		int processid = jor.getInt("au_process_id");
	        		String state = jor.getString("state");
	        		process = (Process) au_process.get(processid);
	        		if (process == null) {
	        			logger.debug(" first null process");
	        			JSONObject jo = QueryEngine
	        					.getInstance()
	        					.doQueryObject(
	        							"select name, description, filterobj from au_process where id=?",
	        							new Object[] { Integer.valueOf(processid) },
	        							conn, false);
	        			logger.debug(String.valueOf(processid));
	        			logger.debug(jo.getString("name"));
	        			
	        			logger.debug("filter desc"+(new Filter(jo.getString("filterobj"))).getDescription());
	        			String desc=jo.getString("description")==null?jo.getString("description"):jo.getString("name");
	        			logger.debug(desc);
	        			process = new Process(processid,
	        					jo.getString("name"),desc,
	        					(new Filter(jo.getString("filterobj"))).getDescription());

	        			JSONArray au_pharray = QueryEngine
	        					.getInstance()
	        					.doQueryObjectArray(
	        							"select id, name, description, permit_num, REJECT_NUM, filterobj from au_phase "
	        									+ "where au_process_id=? order by orderno asc",
	        									new Object[] { Integer.valueOf(processid) },
	        									false);

	        			for (int i = 0; i < au_pharray.length(); i++) {

	        				JSONObject au_jor = au_pharray.getJSONObject(i);
	        				int phid = au_jor.getInt("id");
	        				Phase au_phase = objectflow. new Phase();
	        				au_phase.id = phid;
	        				au_phase.filter = (new Filter(au_jor.getString("filterobj")))
	        						.getDescription();
	        				au_phase.name = au_jor.getString("name");
	        				if (Validator.isNull(au_phase.name))
	        					au_phase.name = "";
	        				au_phase.permitNum = au_jor.optInt("permit_num", 0);
	        				au_phase.rejectNum = au_jor.optInt("reject_num", 0);
	        				au_phase.process = process;
	        				au_phase.auditors = AuditUtils.getPhaseAuditUsers(phid,
	        						TableManager.getInstance().getTable(objectflow.tableid),objectflow.objectid);
	        				process.phases.add(au_phase);
	        				objectflow.au_phase.put(phid, au_phase);
	        			}

	        			objectflow.au_process.put(processid, process);
	        		}
	        			Phase phase;
	        			phase = (Phase) au_phase.get(Integer.valueOf(phaseid));
	        			PhaseInstance phains = new PhaseInstance(pinsid, phase, state,jor.getString("modifieddate"), conn);
	        			
	        			Iterator it = process.phases.iterator();
        				if (pinsid != lpinsid && "R".equals(lstate)) {
        					logger.debug(state+String.valueOf(phains.id)+"add flow"+l);
        					(workflow = new Workflow()).process = process;
        					objectflow.flows.add(workflow);
              				workflow.pis.add(phains);
            	        	lpinsid =pinsid;
            	        	lphaseid = phase.id;
            	        	lprocessid = processid;
            	        	lstate = state;
	        			 } else{
	        				if (!it.hasNext()) {
	        					throw new NDSRuntimeException((new StringBuilder())
	        							.append("neither phase id=").append(phaseid)
	        							.append(" nor phase id=").append(phaseid)
	        							.append(" is found in process id=")
	        							.append(process.id).toString());
	        				}
	        				for (; it.hasNext();) {
	        					Phase phase1;
	        					if((phase1 = (Phase)((Iterator) (it)).next()).id != phaseid){
	        						continue;
	        					}
	        					
	        					logger.debug(state+String.valueOf(phains.id)+"add pis"+l);
	              				workflow.pis.add(phains);
	            	        	lpinsid =pinsid;
	            	        	lphaseid = phase.id;
	            	        	lprocessid = processid;
	            	        	lstate = state;
	        				}
        		

	        			 }
        	        	
	        	}
	        	if (lpinsid > -1) {
	        		docno = (String) engin.doQueryOne(
	        				"select record_docno from au_phaseinstance where id=?",
	        				new Object[] { Integer.valueOf(lpinsid) }, conn);
	        	}

	        	if (conn != null) {
	        		try {
	        			conn.close();
	        			return;
	        		} catch (Throwable e) {
	        			return;
	        		}

	        	}
	        	

	        }

	        public String getDocNO()
	        {
	        	return this.docno; 
	        } 

	        
	        public List<Workflow> getWorkflows() { 
	        	return this.flows; 
	        	}
	        
	        
	        public int getWorkflowTimes()
	        {
	          return this.flows.size();
	        }
	      
	        public class PhaseInstance{
	          public int id;
	          public ObjectFlow.Phase phase;
	          public String state;
	          public JSONArray aas;
	          public String modifiedDate;
	      
	          PhaseInstance(int Phaseid, ObjectFlow.Phase phase, String state, String modate, Connection con)
	            throws Exception
	          {
	            this.id = Phaseid;
	            this.phase = phase;
	            this.state = state;
	            this.modifiedDate = modate;
	            this.aas = QueryEngine.getInstance().doQueryObjectArray("select p.ad_user_id, p.state, p.comments, to_char(p.modifieddate,'YYYY/MM/DD HH24:MI') modifieddate ,p.assignee_id, u.truename uname, u2.truename u2name from au_pi_user p, users u, users u2 where p.au_pi_id=?  and u.id(+)=p.ad_user_id and u2.id(+)=p.assignee_id order by p.modifieddate desc", 
	            		new Object[] { Integer.valueOf(this.id) }, false); if (this.aas.length() > 0) this.modifiedDate = this.aas.getJSONObject(0).getString("modifieddate");
	          }
	        }
	      
	        public class Workflow{
	        	
	          public ObjectFlow.Process process;
	          
	          public List<ObjectFlow.PhaseInstance> pis = new ArrayList();
	      
	          public Workflow() {
	          } 
	          
	          public ObjectFlow.PhaseInstance getPhaseInstance(int phaseid) { 
	        	for(Iterator it=this.pis.iterator();it.hasNext();){  
	              ObjectFlow.PhaseInstance pInstance;
	              if ((pInstance = (ObjectFlow.PhaseInstance)it.next()).phase.id == phaseid) return pInstance;
	            }
	            return null; 
	            }
	      
	          public String getState() {
	            if (this.pis.size() > 0) {
	              return ((ObjectFlow.PhaseInstance)this.pis.get(this.pis.size() - 1)).state;
	            }
	            return "";
	          }
	          
	          public int getLastPhaseInstanceId() {
	            if (this.pis.size() > 0)
	              return ((ObjectFlow.PhaseInstance)this.pis.get(this.pis.size() - 1)).id;
	            return -1;
	          }
	          
	          public String getLastModifiedDate() {
	            if (this.pis.size() > 0) {
	              return ((ObjectFlow.PhaseInstance)this.pis.get(this.pis.size() - 1)).modifiedDate;
	            }
	            return "";
	          }
	      
	          public List<ObjectFlow.PhaseOfWorkflow> getPhases(boolean paramBoolean)
	          {
	            ArrayList localArrayList = new ArrayList();
	            ObjectFlow.Phase phase;
	            ObjectFlow.PhaseOfWorkflow PhaseOfWorkflow;
	            ObjectFlow.PhaseInstance phinstance;
	            if (paramBoolean){
	            	  for(Iterator it=this.process.phases.iterator();it.hasNext();){
	            		  phase = (ObjectFlow.Phase)it.next();
	            		  PhaseOfWorkflow = new ObjectFlow.PhaseOfWorkflow();
	            		  PhaseOfWorkflow.phase=phase;
	            		  PhaseOfWorkflow.pi = getPhaseInstance(phase.id);
	            		  localArrayList.add(PhaseOfWorkflow);
	              }
	            }else {
	            	for(Iterator it=this.pis.iterator();it.hasNext();){
	            	  phinstance = (ObjectFlow.PhaseInstance)it.next();
	            	  PhaseOfWorkflow = new ObjectFlow.PhaseOfWorkflow();
	            	  PhaseOfWorkflow.phase=((ObjectFlow.PhaseInstance)phinstance).phase;
	            	  PhaseOfWorkflow.pi = ((ObjectFlow.PhaseInstance)phinstance);
	            	  localArrayList.add(PhaseOfWorkflow);
	              }
	            }
	            return localArrayList;
	          }
	        }
	      
	        public class PhaseOfWorkflow
	        {
	          public ObjectFlow.Phase phase;
	          public ObjectFlow.PhaseInstance pi;
	          public PhaseOfWorkflow()
	          {
	          }
	        }
	      
	        public class Phase
	        {
	          public int id;
	          public String name;
	          public String filter;
	          public int permitNum;
	          public int rejectNum;
	          public JSONArray auditors;
	          public ObjectFlow.Process process;
	          public Phase()
	          {
	          }
	        }
	      
	        public class Process
	        {
	          public int id;
	          public String name;
	          public String description;
	          public String filter;
	          public List<ObjectFlow.Phase> phases = new ArrayList();
	      
	          Process(int id, String name, String desc, String filter) {  
	        	this.id = id;
	            this.name = name;
	            this.description = desc;
	            this.filter = filter;
	          }
	        }
	      
}


