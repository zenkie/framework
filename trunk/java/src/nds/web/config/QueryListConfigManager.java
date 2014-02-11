package nds.web.config;

import java.util.Hashtable;

import javax.servlet.ServletContext;

import java.sql.*;
import java.util.*;

import org.json.JSONArray;
import org.json.JSONObject;
import org.shiftone.cache.Cache;

import nds.query.*;
import nds.schema.*;
import nds.control.web.WebUtils;
import nds.log.Logger;
import nds.log.LoggerManager;
import nds.util.*;

/**
 * Query list ui configuration, it's the reconstruction of select clause, containing
 * select from where orderby.  
 * 
 * @author yfzhu@agilecontrol.com
 *
 */
public class QueryListConfigManager{
    private static final int DEFAULT_CACHE_TIME_OUT= 60 *300;// default to 300 minutes
    private static final int DEFAULT_CACHE_MAXIMUM_SIZE=300;//
	
    private Logger logger= LoggerManager.getInstance().getLogger(QueryListConfigManager.class.getName());
    private Cache cache; // key: Object, value: Expression (user's filter on directory, or preferences)
    private Hashtable<Integer, PairTable> tableConfigs; //key: tableid, value:PairTable
    
    private Hashtable<Integer, QueryListConfig> metaConfig;
    
    private static QueryListConfigManager instance=null;
    
    private QueryListConfigManager() {
        logger.debug("QueryListConfigManager initialized.");
        Configurations conf=(Configurations)WebUtils.getServletContextManager().getActor(WebKeys.CONFIGURATIONS);
        long timeOut=DEFAULT_CACHE_TIME_OUT;
        try{
           timeOut=( new Integer(conf.getProperty("querylistconf.cache.timeout"))).longValue()  ; // seconds
        }catch(Exception e){}
         int size=DEFAULT_CACHE_MAXIMUM_SIZE;
        try{
           size=( new Integer( conf.getProperty("querylistconf.cache.size"))).intValue() ; // size
        }catch(Exception e2){}

        cache=org.shiftone.cache.CacheManager.getInstance().newCache(timeOut*1000, size);
        tableConfigs=new Hashtable();
        metaConfig=new Hashtable();
        logger.debug("Create Cache, timeout="+ timeOut+" seconds, size="+ size);
    	
    }
    public void deleteQueryListConfig(int id)throws Exception{
    	QueryListConfig pc= (QueryListConfig)cache.remove(id);
    	PairTable pt= tableConfigs.get(pc.getTableId());
    	if(pt!=null) pt.remove(id);
    	QueryEngine.getInstance().executeUpdate("delete from ad_querylist where id="+id);
    }
    /**
     * Remove from cache, so can reload configuration next time request
     * @param id
     */
    public QueryListConfig removeQueryListConfig(int id){
    	
    	QueryListConfig pc= (QueryListConfig)cache.remove(id);
    	
    	return pc;
    }
    public int getCacheSize(){
    	return cache.size();
    }
    /**
     * Query list configuration of meta data, retrieved definition from ad_table/ad_column
     * 
     * @param tableId
       @param sgrade column with sgrade higher then this code, will not be selected
     * @return
     */
    public QueryListConfig getMetaDefault(int tableId, int sgrade) throws NDSException{
    	
    	QueryListConfig qlc= metaConfig.get(tableId);
    	if(qlc!=null) return qlc;
    	
    	qlc=getModalDefaultQueryListConfig(tableId,sgrade);
    	if(qlc!=null) return qlc;
    	
    	TableManager manager=TableManager.getInstance();
    	Table table=manager.getTable(tableId);
    	
    	qlc=new QueryListConfig();
    	qlc.setId(-1); // not in db
    	qlc.setName("DEFAULT");
    	qlc.setTableId(tableId);
    	qlc.setDefault(false);
    	//conditions
    	ArrayList<ColumnLink> cls=new ArrayList<ColumnLink>();
    	ArrayList al=table.getIndexedColumns();
    	for(int i=0;i<al.size();i++){
    		Column col=(Column) al.get(i);
    		if(col.getSecurityGrade()> sgrade) continue;
    		ColumnLink cl=new ColumnLink(new int[]{col.getId()});
    		cls.add(cl);
    	}
    	qlc.setConditions(cls);
    	//selections
    	cls=new ArrayList<ColumnLink>();
    	al=table.getColumns(new int[]{Column.MASK_QUERY_LIST},false, sgrade ); //default sgrade
    	for(int i=0;i<al.size();i++){
    		Column col=(Column) al.get(i);
    		ColumnLink cl=new ColumnLink(new int[]{col.getId()});
    		cls.add(cl);
    	}
    	qlc.setSelections(cls);
    	
    	//orderbys
    	cls=new ArrayList<ColumnLink>();
    	JSONArray orderby=null;
    	if( table.getJSONProps()!=null) orderby=table.getJSONProps().optJSONArray("orderby");
    	if(orderby!=null){
    		for(int i=0;i<orderby.length();i++){
    			try{
        			JSONObject od= orderby.getJSONObject(i);
    				ColumnLink cl= new ColumnLink(table.getName()+"."+ od.getString("column"));
    				cl.setTag(od.optBoolean("asc",true));
    				cls.add(cl);
    			}catch(Throwable t){
    				logger.error("fail to load order by of "+ table.getName()+", pos="+i , t);
    				//throw new NDSException("order by column error:"+ od.optString("column"));
    			}
    			
    		}
        	
    		
    	}else if( table.getColumn("orderno")!=null){
    		ColumnLink cl= new ColumnLink(new int[]{table.getColumn("orderno").getId()});
    		cl.setTag(true);
    		cls.add(cl);
    	}
    	qlc.setOrderBys(cls);
    	
    	metaConfig.put(tableId, qlc);
    	return qlc;
    }
    /**
     * 
     * @param id - record id of ad_querylist table
     * @return null if id is -1 
     * @throws ObjectNotFoundException if id is not -1 and not found in db
     */
    public QueryListConfig getQueryListConfig(int id ) throws NDSRuntimeException{
    	if(id==-1) return null;
    	try{
	    	QueryListConfig pc=(QueryListConfig)cache.getObject(id);
	    	if(pc==null){
	    		//load from db
	    		Object props=QueryEngine.getInstance().doQueryOne("select props from ad_querylist where id="+id);
	    		if(props instanceof java.sql.Clob) {
					try{
						props=((java.sql.Clob)props).getSubString(1, (int) ((java.sql.Clob)props).length());
					}catch(Throwable t){
						throw new NDSException(t.getMessage(),t);
					}
	        	}else if(!(props instanceof java.lang.String)){
	        		throw new NDSException("Internal error: found props neither Clob nor String");
	        	}
	    		if(props==null) throw new NDSRuntimeException("query list config not found in db id="+id);
	    		pc=new QueryListConfig((String)props);
	    		pc.setId(id);// id in props is always -1
    			cache.addObject(id, pc);
    			
	    	}
	    	return pc;
    	}catch(Throwable t){
    		logger.error("Fail to get QueryListConfig id="+id, t);
    		if(t instanceof NDSRuntimeException) throw (NDSRuntimeException)t;
    		return null;
    	}
    }
    /**
     * Load id and name of configures of specified table, not including meta default
     * @param tableId
     * @param forceReload if true, will relaod from db forcefully
     * @return PairTable key: id of QueryListConfig, value:Object[2] 1 is name of QueryListConfig, 2 is isDefault(boolean)
     */
    public PairTable getQueryListConfigs(int tableId, boolean forceReload){
    	PairTable pt=null;
    	try{
	    	pt=(PairTable) this.tableConfigs.get(tableId);
	    	if(pt==null || forceReload ){
	    		//load from db
	    		List al=QueryEngine.getInstance().doQueryList("select id,name,isdefault from ad_querylist where ad_table_id="+tableId+" order by isdefault desc");
	    		if(al.size()>0){
	    			pt=new PairTable();
		    		for(int i=0;i< al.size();i++){
		    			List one=(List)al.get(i);
		    			pt.put( Tools.getInt(one.get(0),-1), 
	    					new Object[]{ (String)one.get(1), 
		    					Tools.getYesNo((String)one.get(2), false)});
		    		}
	    		}else{
	    			pt=PairTable.EMPTY_PAIRTABLE;
	    		}
	    		tableConfigs.put(tableId, pt);
	    	}
    	}catch(Throwable t){
    		logger.error("Fail to get QueryListConfig tableid="+tableId, t);
    	}
    	return pt;
    }
    /**
     * If id =-1, this is new one, else will do update
     * if isdefault set to true, will update previous one to not default one 
     * @param qlc note if qlc.id=-1, it'll get updated
     */
    public void saveQueryListConfig(QueryListConfig qlc ) throws Exception{
    	//meta configuration
    	if(qlc.getId()==0) throw new Error(" id= 0 is meta configuration, not to be saved");
    	if(qlc.getName().equals("DEFAULT")) throw new NDSException("DEFAULT is reserved");
		Table table=TableManager.getInstance().getTable(qlc.getTableId());
		QueryEngine engine=QueryEngine.getInstance();
    	if(qlc.getId()==-1){
    		// insert db
    		int nid=engine.getSequence(table.getName());

    		Connection con=engine.getConnection();
            PreparedStatement stmt=null;
            try{
            	stmt = con.prepareStatement("insert into ad_querylist(id,name,ad_table_id,props,isdefault) values(?,?,?,?,?)");
            	stmt.setInt(1,nid);
            	stmt.setString(2,qlc.getName());
            	stmt.setInt(3, qlc.getTableId());
            	stmt.setString(4, qlc.toJSONString()  );
            	stmt.setString(5,qlc.isDefault()?"Y":"N");
                stmt.executeUpdate();
                
            }finally{
            	try{stmt.close();}catch(Exception e){}
                try{con.close();}catch(Exception e){}
            }    		
    		
    		// update qlc in mem
    		qlc.setId(nid);
    		
    	}else{
    		// update db
    		Connection con=engine.getConnection();
            PreparedStatement stmt=null;
            try{
            	stmt = con.prepareStatement("update ad_querylist set name=?, props=?,isdefault=? where id=?");
            	stmt.setString(1,qlc.getName());
            	stmt.setString(2, qlc.toJSONString()  );
            	stmt.setString(3,qlc.isDefault()?"Y":"N");
            	stmt.setInt(4, qlc.getId());
                stmt.executeUpdate();
                
            }finally{
            	try{stmt.close();}catch(Exception e){}
                try{con.close();}catch(Exception e){}
            }    		
    		//remove from cache so as to reload
    		this.removeQueryListConfig(qlc.getId());
    	}
    	if(qlc.isDefault()){
    		//set all others of the same table to none-default
    		engine.executeUpdate("update ad_querylist set isdefault='N' where isdefault='Y' and ad_table_id="+ table.getId()+" and id<>"+qlc.getId());
    	}
		//clear list cache of the table 
		this.tableConfigs.remove(table.getId());
    	
    }
    /**
     * When none is default, will return null;
     * @param tableId
     * @return null if no one is default
     */
    public QueryListConfig getDefaultQueryListConfig(int tableId){
    	PairTable pt=this.getQueryListConfigs(tableId, false);
    	QueryListConfig c=null;
    	for(int i=0;i<pt.size();i++){
    		Object[] v=(Object[])pt.getValue(i);
    		if( ((Boolean)v[1]).booleanValue()){
    			// this one is default
    			int cid=((Integer)pt.getKey(i)).intValue();
    			c= this.getQueryListConfig(cid);
    			break;
    		}
    	}
    	return c;
    	
    }
    
    public void clearAll() {
    	if(cache!=null) cache.clear();
    	tableConfigs.clear();
    	this.metaConfig.clear();
        logger.debug("QueryListConfigManager cleared.");
    }
    public static QueryListConfigManager getInstance(){
    	if(instance==null) instance=new QueryListConfigManager();
    	return instance;
    }
    
    /**
     * 
     * @param tableId
     * @param securityGrade
     * @return
     * paco add
     */
    public QueryListConfig getModalDefaultQueryListConfig(int tableId, int sgrade) throws NDSException{
     	
    	QueryListConfig qlc=new QueryListConfig();
    	qlc.setId(-1); // not in db
    	qlc.setName("DEFAULT");
    	qlc.setTableId(tableId);
    	qlc.setDefault(false);

    	
    	Column column=null;
    	JSONArray showJson=null;
    	ArrayList<ColumnLink> cls=new ArrayList<ColumnLink>();
    	TableManager tManager=TableManager.getInstance();
    	Table table=tManager.getTable(tableId);
    	
    	
    	//conditions
    	ArrayList al=table.getIndexedColumns();
    	for(int i=0;i<al.size();i++){
    		Column col=(Column) al.get(i);
    		if(col.getSecurityGrade()> sgrade) continue;
    		ColumnLink cl=new ColumnLink(new int[]{col.getId()});
    		cls.add(cl);
    	}
    	qlc.setConditions(cls);
    	
    	//selections
    	cls=new ArrayList<ColumnLink>();
    	JSONObject tExtendPro=table.getJSONProps();
    	if(tExtendPro==null || !tExtendPro.has("showColumns")){
    		logger.debug("tExtendPro is null");
    		return null;
    		}
    	if(tExtendPro!=null && tExtendPro.has("showColumns")){
    		showJson=tExtendPro.optJSONArray("showColumns");
    		for(int i=0;i<showJson.length();i++){
    			logger.debug("showColumns is append"); 
    			logger.debug("showColumns is :"+showJson.optString(i)); 
    			column=table.getColumn(showJson.optString(i));
    			if(column==null)logger.debug("cloumn is null"); 
    			if(column.getSecurityGrade()> sgrade) continue;
    			ColumnLink cl=new ColumnLink(new int[]{column.getId()});
        		cls.add(cl);
    		}
    	}	
    	
    	if(tExtendPro!=null && tExtendPro.has("moveShowColumns")){
    		showJson=tExtendPro.optJSONArray("moveShowColumns");
    		for(int i=0;i<showJson.length();i++){
    			logger.debug("moveShowColumns is append"); 
    			column=table.getColumn(showJson.optString(i));
    			logger.debug("showColumns is :"+showJson.optString(i)); 
    			if(column.getSecurityGrade()> sgrade) continue;
    			ColumnLink cl=new ColumnLink(new int[]{column.getId()});
    			if(!cls.contains(cl))cls.add(cl);
    		}
    	}
    	
    	if(cls!=null){qlc.setSelections(cls);};
    	
    	//orderbys
    	cls=new ArrayList<ColumnLink>();
    	JSONArray orderby=null;
    	if( table.getJSONProps()!=null) orderby=table.getJSONProps().optJSONArray("orderby");
    	if(orderby!=null){
    		for(int i=0;i<orderby.length();i++){
    			try{
        			JSONObject od= orderby.getJSONObject(i);
        			ColumnLink cl= new ColumnLink(table.getName()+"."+ od.getString("column"));
    				cl.setTag(od.optBoolean("asc",true));
    				cls.add(cl);
    			}catch(Throwable t){
    				logger.error("fail to load order by of "+ table.getName()+", pos="+i , t);
    				//throw new NDSException("order by column error:"+ od.optString("column"));
    			}
    			
    		}
        	
    		
    	}else if( table.getColumn("orderno")!=null){
    		ColumnLink cl= new ColumnLink(new int[]{table.getColumn("orderno").getId()});
    		cl.setTag(true);
    		cls.add(cl);
    	}
    	qlc.setOrderBys(cls);
    	
    	if(qlc!=null){metaConfig.put(tableId, qlc);}else{logger.debug("qlc is null");}
    	return qlc;
    }

}