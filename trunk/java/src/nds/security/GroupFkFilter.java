package nds.security;
import nds.log.*;
import nds.query.*;
import nds.util.Tools;
import nds.util.Validator;

import java.util.*;


/**
 * Bean for all fkfilter objs in one group
 * @author yfzhu
 *
 */
public class GroupFkFilter {
    private static Logger logger= LoggerManager.getInstance().getLogger((GroupFkFilter.class.getName()));
    
	
	private Map<Integer,FkFilter> fkFilters;
	private int groupId;
	
	
	/**
	 * Load all FkFilters of one group, just description, no sqlfilter
	 * @param groupId id of table groups
	 * @throws QueryException
	 */
	public GroupFkFilter(int groupId) throws QueryException{
		fkFilters=new HashMap();
		List al=QueryEngine.getInstance().doQueryList("select id, ad_table_id,description from sec_fkfilter where groupid="+groupId);
		for(int i=0;i<al.size();i++){
			List o=(List)al.get(i);
			FkFilter ff=new FkFilter();
			ff.setId(Tools.getInt(o.get(0),-1) );
			ff.setGroupId(groupId);
			ff.setTableId(Tools.getInt(o.get(1),-1) );
			ff.setDescription((String) o.get(2));
			fkFilters.put(ff.getId(), ff);
		}
	}
	
	/**
	 * Get FkFilters that ids contains
	 * @param filterIds in format like "id1,id2,..," id is pk of table sec_fkfilter
	 * @return never be null
	 */
	public List<FkFilter> list(String filterIds){
		if(Validator.isNull(filterIds)) return Collections.EMPTY_LIST;
		ArrayList<FkFilter> al=new ArrayList();
		StringTokenizer st=new StringTokenizer(filterIds,",");
		while(st.hasMoreTokens()){
			int id= Tools.getInt(st.nextToken(),-1);
			if(id>-1){
				FkFilter ff= fkFilters.get(id);
				if(ff!=null) al.add(ff);
				else logger.warning("not find fkfilter with id="+ id + " for group id="+ groupId+" with ids="+filterIds);
			}
		}
		return al;
	}

	public Collection<FkFilter> getFkFilters() {
		return fkFilters.values();
	}


	public int getGroupId() {
		return groupId;
	}
	
	
}
