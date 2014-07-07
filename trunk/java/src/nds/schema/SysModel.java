package nds.schema;


import java.util.*;

import javax.servlet.http.HttpServletRequest;

import nds.control.web.UserWebImpl;
import nds.control.web.WebUtils;
import nds.log.Logger;
import nds.log.LoggerManager;
import nds.query.QueryEngine;
import nds.query.QueryException;
import nds.query.web.SubSystemView;
import nds.util.MessagesHolder;
import nds.util.Tools;
import nds.util.Validator;
/**
 * Sub sysmodel of table, current has only one level
 * @author jackrain
 */


public class SysModel {
	
	
	private int id;
	private String name;
	private Integer orderno;
	private String mlink;
	private String pico;
	private String mdisp;
	private List<TableCategory> tcs;
	private List<WebAction> actions;
	private List<SubSystem> sub;
	private List tcacts; // WebAction and TableCategory in order
	private static Logger logger=LoggerManager.getInstance().getLogger(SysModel.class.getName());
	/**
	 * People can view the specified subsystem
	 */
	public final static int PERMISSION_VIEWABLE=0; 
	
	public SysModel(){
		tcs=new ArrayList<TableCategory>();
		actions=new ArrayList<WebAction>();
		tcacts=new ArrayList();
		sub =new ArrayList<SubSystem>();
		
	}

	/**
	 * @return Returns the id.
	 */
	public int getId() {
		return id;
	}

	public void setId(int id) {
		// TODO Auto-generated method stub
		this.id=id;
		
	}

	/**
	 * @return Returns the name.
	 */
	public String getName() {
		return name;
	}
	public void setName(String name) {
		// TODO Auto-generated method stub
		this.name=name;
	}

	/**
	 * @return Returns the mlink.
	 */
	public String getMlink() {
		return mlink;
	}
	public void setMlink(String mlink) {
		// TODO Auto-generated method stub
		this.mlink=mlink;
	}

	/**
	 * @return Returns the pico.
	 */
	public String getPico() {
		return pico;
	}
	public void setPico(String pico) {
		// TODO Auto-generated method stub
		this.pico=pico;
	}
	/**
	 * @return Returns the mdisp.
	 */
	public String getMdisp() {
		return mdisp;
	}
	public void setMdisp(String mdisp) {
		// TODO Auto-generated method stub
		this.mdisp=mdisp;
	}
	
	public void addTableCategory(TableCategory tc){
		tcs.add(tc);
		tcacts.add(tc);
	}
	
	
	public void addSubSystem(SubSystem s){
		sub.add(s);
	}
	
	public String getDescription(Locale locale){
		if( TableManager.getInstance().getDefaultLocale().hashCode()==locale.hashCode())
			return name;
		return MessagesHolder.getInstance().getMessage(locale, "SysModel_"+id);
	}
	
	public void sortTableCategoryAndActions(){
		//nds.util.ListSort.sort(tcs, "getOrder");
		//nds.util.ListSort.sort(actions, "getOrder");
		nds.util.ListSort.sort(sub, "getOrderno");
		System.out.print("≈≈–Ú¡À≈∂£°£°£°");
		Collections.sort(tcacts, new Comparator(){
			/**
			 * a negative integer, zero, or a positive integer as the
     * 	       first argument is less than, equal to, or greater than the
     *	       second. 
			 */
			public int compare(Object o1,Object o2){
				int o1Order,o2Order;
				if(o1 instanceof TableCategory) o1Order= ((TableCategory)o1).getOrder();
				else o1Order=  ((WebAction)o1).getOrder();
				
				if(o2 instanceof TableCategory) o2Order= ((TableCategory)o2).getOrder();
				else o2Order=  ((WebAction)o2).getOrder();
				
				return o1Order-o2Order;
				
			}
		});
	}
	/**
	 * 
	 * @return not null,elements can be subsystem in order
	 */
	public List children(){
		return sub;
	}
	/**
	 * @return Returns the orderno.
	 */
	public Integer getOrderno() {
		return orderno;
	}
	/**
	 * @param orderno The orderno to set.
	 */
	public void setOrderno(Integer orderno) {
		this.orderno = orderno;
	}
	
	
	/**
	 * 
	 * @param request
	 * @param permissionType PERMISSION_VIEWABLE, PERMISSION_NO_PERM or PERMISSION_NO_LICENSE
	 * @return never null, elements are nds.schema.SubSystem
	 */
	public List getSubSystems(HttpServletRequest request, int permissionType) throws Exception{
		
		if(permissionType==PERMISSION_VIEWABLE) return getSubSystems(request,sub);
		return null;
		
	}
	
	
	/**
	 * Get viewable subsystem list
	 * @param request
	 * @return never null, elements are nds.schema.SubSystem
	 */
	public List getSubSystems(HttpServletRequest request,List sub){
		UserWebImpl userWeb= ((UserWebImpl)WebUtils.getSessionContextManager(request.getSession()).getActor(nds.util.WebKeys.USER));
		ArrayList subs=new ArrayList();
		if(userWeb.getUserId()==userWeb.GUEST_ID){
			return subs;
		}
		List al= (List)userWeb.getProperty("subsystems");// elements are subystem.id
		TableManager manager=TableManager.getInstance();
		if(al!=null){
			
			for(int i=0;i< al.size();i++){
				int sid= ((Integer)al.get(i)).intValue();
				SubSystem ss= manager.getSubSystem(sid );
				if(sub.contains(ss)){
					subs.add(ss);
				}
			}
		}else{
			// search all tablecategoris for subsystem
			// add users subsystems param
			al=new ArrayList();
			String[] sub_list;
			try
			{
				String subsystems=(String)QueryEngine.getInstance().doQueryOne("SELECT subsystems from users where id="+userWeb.getUserId());
				if (Validator.isNotNull(subsystems)){
					sub_list = subsystems.split(",");
					for (int m = 0; m < sub_list.length; m++)
					{
						SubSystem usersub = manager.getSubSystem(sub_list[m].trim());
						if (usersub != null&& sub.contains(usersub))
						{
							  al.add(new Integer(usersub.getId()));
							  subs.add(usersub);
						}
					}
					userWeb.setProperty("subsystems", al);
					return subs;
				}
			}
			catch (QueryException e) {
				logger.error("Fail to load subsystems from users", e);
			}			
		}
		return subs;
	}
}
