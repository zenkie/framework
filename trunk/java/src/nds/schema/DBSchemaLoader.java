/*
 * Agile Control Technologies Ltd,. CO.
 * http://www.agileControl.com
 */
package nds.schema;

import java.util.Hashtable;
import java.util.Iterator;
import java.sql.*;
import java.util.*;
import nds.util.*;
import nds.query.*;
import nds.control.event.NDSEventException;
import nds.model.dao.*;
import nds.model.*;
import org.hibernate.*;
import nds.log.*;
import java.sql.*;
/**
 * Load AD_Table/AD_Column definitions from db, column and table should be active.
 * 
 * @author yfzhu@agilecontrol.com
 */

public class DBSchemaLoader {
	private Logger logger= LoggerManager.getInstance().getLogger(DBSchemaLoader.class.getName());
	private Hashtable adTables=new Hashtable();// key:table id(Integer) value: AdTable
	private Hashtable tables= new Hashtable();// key:table id, value TableImpl
	private HashMap categories= new HashMap(); // key: category id (Integer) values are TableCategory
	private HashMap subsystems= new HashMap(); // key: category id (Integer) values are SubSystem
	/**
	 * By default, comments of column and table will not be loaded into memory
	 * this can be changed using portal.properties#schema.loadcomments 
	 */
	private boolean loadingComments=false;
	
/*	private static String[] apTables={"AD_SYSTEM","AD_TABLE","AD_COLUMN", 
			"AD_ALIASTABLE","AD_REFBYTABLE","AD_TABLECATEGORY","AD_TABLESQL","DIRECTORY", "AD_LIMITVALUE","AD_LIMITVALUE_GROUP",};
*/			 
	public void destroy(){
		tables.clear();
		adTables.clear();
		categories.clear();
		subsystems.clear();
		adTables=null;
		tables=null;
		categories=null;
		subsystems=null;
	}
	
	/**
	 * Currenty ad_client_id is ignored
	 * @param ad_client_id
	 * @param isDebugMode when true, the ap tables such ad_table,ad_column will be set to "isMenuObj" 
	 * and will be readonly
	 */
	public void setup(int ad_client_id) throws Exception{
		loadingComments="true".equalsIgnoreCase(nds.control.util.EJBUtils.getApplicationConfigurations().getProperty("schema.loadcomments", "false"));

		Session session= null; 
    	AdTableDAO adTableDAO=new AdTableDAO();
    	try{
    		session=adTableDAO.createSession();
    		long t=System.currentTimeMillis();
    		//List colList= session.createSQLQuery("select {c.*} from ad_column {c} where {c}.IsActive='Y' and exists (select 1 from ad_table a, ad_tablecategory g where a.id={c}.ad_table_id and g.id=a.ad_tablecategory_id and g.isactive='Y' and a.isactive='Y') order by {c}.Orderno", "c", AdColumn.class).list();
    		List colList= session.createSQLQuery("select * from ad_column where ad_column.IsActive='Y' and exists (select 1 from ad_table a, ad_tablecategory g, ad_subsystem s where a.id=ad_column.ad_table_id and g.id=a.ad_tablecategory_id and s.id=g.ad_subsystem_id and s.isactive='Y' and g.isactive='Y' and a.isactive='Y') order by ad_column.Orderno").addEntity(AdColumn.class).list();
    		
    		for(Iterator it=colList.iterator();it.hasNext();){
    			AdColumn col= (AdColumn) it.next();
    			createColumns(col);
    		}
    		for(Iterator it=adTables.values().iterator();it.hasNext();){
    			AdTable tb= (AdTable) it.next();
   				createRefByTables(tb);
    		}
    		
    		//add subsystems who has no tablecategory but do have webaction as child
    		List ss= session.createSQLQuery("select * from ad_subsystem where ad_subsystem.IsActive='Y' and exists (select 1 from ad_action a where a.ad_subsystem_id=ad_subsystem.id and a.isactive='Y') and not exists (select 1 from ad_tablecategory c where c.ad_subsystem_id=ad_subsystem.id and c.isactive='Y') order by ad_subsystem.Orderno")
    			.addEntity(AdSubSystem.class).list();
    		for(Iterator it=ss.iterator();it.hasNext();){
    			AdSubSystem s= (AdSubSystem) it.next();
    			createSubSystem(s);
    		}
    		
    		logger.debug("Total meta loading time :" +(System.currentTimeMillis()-t)/1000.0);
    	}finally{
    		if(session !=null){
    			try{adTableDAO.closeSession();}catch(Exception e2){}
    		}
    	}
    	// unload adtable in cache 2009-7-5
    	adTables.clear();
	}
	
	
	private TableImpl getTableImpl(AdTable tb){
		TableImpl table= (TableImpl)tables.get(tb.getId()) ;
		if(table ==null)
			table= createTable(tb);
		return table;
	}
	private SubSystem getSubSystem(AdSubSystem ss){
		if(ss==null) return null;
		SubSystem system= (SubSystem)this.subsystems.get(ss.getId());
		if( system==null) system=createSubSystem(ss);
		return system;
		
	}
	private SubSystem createSubSystem(AdSubSystem ass){
		SubSystem ss= new SubSystem( );
		ss.setId(ass.getId().intValue());
		ss.setName(ass.getName());
		ss.setOrderno(ass.getOrderno());
		ss.setPageURL(ass.getUrl());
		ss.setIconURL(ass.getIconUrl());
		//tc.setParent
		subsystems.put(ass.getId(), ss);
		return ss;
	}
	
	private TableCategory getTableCategory(AdTableCategory tc){
		TableCategory category= (TableCategory)this.categories.get(tc.getId());
		if( category==null) category=createTableCategory(tc);
		return category;
	}
	private TableCategory createTableCategory(AdTableCategory atc){
		TableCategory tc= new TableCategory( );
		tc.setId(atc.getId().intValue());
		tc.setName(atc.getName());
		tc.setOrder(Tools.getInt(atc.getOrderno(),-1));
		tc.setPageURL(atc.getUrl());
		tc.setSubSystem(getSubSystem(atc.getAdSubSystem()));
		//tc.setParent
		categories.put(atc.getId(), tc);
		return tc;
	}
	private TableImpl createTable(AdTable tb){
		// try loading class according to table name
		TableImpl table=null;
		if( Validator.isNotNull(tb.getClassName())){
			try{
				Class c= Class.forName(tb.getClassName());
				table= (TableImpl)c.newInstance();
				table.setId(tb.getId().intValue());
			}catch(Exception e){
				logger.info("Could not load "+ tb.getClassName() + " as TableImpl");
			}
		}
		if( table==null) table= new TableImpl(tb.getId().intValue());
		table.setOrder(tb.getOrderno().intValue());
		table.setName(tb.getName());
		table.setDescription(tb.getDescription());
		table.setRowURL(tb.getUrl());
		table.setCategory(getTableCategory( tb.getAdTableCategory()));
		table.setSubmitProcName(tb.getProcSubmit());
		table.setMask(tb.getMask());
		table.setFilter(tb.getFilter());
		table.setModifiedDate(tb.getModifiedDate());
		table.setIsMenuObject(Tools.getYesNo(tb.getIsMenuObj(),true));
		table.setIsBig(Tools.getYesNo(tb.getIsBig(),false));
		table.setIsSMS(Tools.getYesNo(tb.getIsSMS(),false));
		table.setIsDropdown(Tools.getYesNo(tb.getIsDropdown(),false));
		if(tb.getAdObjuiconfId()!=null)
			table.setUIConfigId(tb.getAdObjuiconfId().intValue());
		if(tb.getParentTableId()!=null)
			table.setParentTableId(tb.getParentTableId().intValue());
		if(tb.getRowCnt()!=null)
			table.setRowCount( tb.getRowCnt().intValue() );
		if(tb.getRealtable()!=null)
			table.setRealTableName(tb.getRealtable().getName());
		else
			table.setRealTableName(tb.getName());
		if(tb.getDirectory()!=null)
			table.setSecurityDirectory(tb.getDirectory().getName());

		TriggerHolder th=new TriggerHolder();

		if(Tools.getYesNo(tb.getHasTrigAc(),false))
			th.setAfterCreate(tb.getTrigAc(),tb.getName()+"_AC" );
		if(Tools.getYesNo(tb.getHasTrigAm(),false))
			th.setAfterModify(tb.getTrigAm(),tb.getName()+"_AM" );
		if(Tools.getYesNo(tb.getHasTrigBd(),false))
			th.setBeforeDelete(tb.getTrigBd(),tb.getName()+"_BD" );
		if(Tools.getYesNo(tb.getHasTrigBm(),false))
			th.setBeforeModify(tb.getTrigBm(),tb.getName()+"_BM" );
		table.setTriggers(th);
		
		// alias table
		// deprecated as no longer needed
		/*for(Iterator it =tb.getAdAliastableSet().iterator();it.hasNext();){
			AdAliastable at= (AdAliastable) it.next();
			AliasTable atb=new AliasTable();
			atb.setRealTableName(at.getRealtable().getName());
			atb.setCondition(at.getCondition());
			atb.setName(at.getName());
			table.addAliasTable(atb);
		}*/
		
		// tree type
		if( Tools.getYesNo(tb.getIsTree(),false)==true){
			table.setParentNodeColumnId(tb.getParentColumnId());
			table.setSummaryColumnId(tb.getSummaryColumnId());
		}
		if(loadingComments) table.setComment(tb.getComments());
		
		String p=tb.getProps();
		if(nds.util.Validator.isNotNull(p)){
			/*
			 * accept both xml and json format to specify properties, xml must be readable by JSONObject.parseXML
			 * json is the default format
			 */
			org.json.JSONObject jo=null;
			try{
				jo= new org.json.JSONObject(p);
			}catch(Throwable t){
				
				try{
					jo =org.json.XML.toJSONObject(p);
				}catch(Throwable t2){
					logger.error("Fail to parse to json:"+ p, t2);
					throw new nds.util.NDSRuntimeException("Fail to parse props of table "+ tb.getName()+" to json:"+ t2);
				}
				
			}
			if(jo!=null) table.setJSONProps(jo);
		  	
			
		}
		
		//if(!isDebugMode)checkAPTable(table);
		tables.put(tb.getId(),  table);
		adTables.put(tb.getId(), tb);
		return table;
	}

	
	private void createColumns(AdColumn column) throws Exception{
		AdTable tb= column.getAdTable();
		if( Tools.getYesNo(tb.getIsActive(),true)== false) return;
		// if category is not active , do no create
		if( Tools.getYesNo(tb.getAdTableCategory().getIsActive(),true)== false) return;
		// if is fk, then fk column's table and table category should also be active
		if(column.getRefColumn()!=null &&  (
				Tools.getYesNo(column.getRefColumn().getAdTable().getIsActive(), true)==false ||
				Tools.getYesNo(column.getRefColumn().getAdTable().getAdTableCategory().getIsActive(), true)==false)) return;
		
			TableImpl table= getTableImpl(tb);
			SQLType t= TypeConverterFactory.getInstance().getConverter().convert(column.getColType().toLowerCase());
            int type=t.getType();
            int length=t.getLength();
            int scale=t.getScale()  ;			
			ColumnImpl col=new ColumnImpl(column.getId().intValue(),table,
					column.getDbName(), column.getDescription(),type , Tools.getYesNo(column.getNullable(),true));
			col.setDefaultValue(column.getDefaultValue());
			
			col.setIsAlternateKey(Tools.getYesNo(column.getIsAk(), false));
			col.setIsDisplayKey(Tools.getYesNo(column.getIsDk(), false));
			col.setIsUpperCase(Tools.getYesNo(column.getIsUpperCase(), false));
			col.setIsIndexed(Tools.getYesNo(column.getIsIndexed(), false));
			col.setOnDeleteAction(column.getOnDelete());
            col.setMask(column.getMask());
            if( column.getOrderno()!=null)
            	col.setDisplayOrder(column.getOrderno().intValue());
            col.setModifiable(Tools.getYesNo(column.getModifiable(),true));
            col.setObtainManner(column.getObtainmanner()) ;
            if(column.getStatSize()!=null && column.getStatSize().intValue()>0){
            	col.setStatSize(column.getStatSize().intValue());
            }else{
            	col.setStatSize(-1); // screen will use default length when statsize <=0
            }
            AdColumn refCol= column.getRefColumn();
            if( refCol!=null){
            	col.setReferenceTable( getTableImpl( refCol.getAdTable()) );
            	col.setReferenceColumnName(refCol.getDbName());
            }
           	col.setSequenceHead(column.getSequencename());
            col.setLength(length);
            col.setScale(scale);
            
            String dt= column.getDisplayType();
            if("text(auto)".equalsIgnoreCase(dt)){
            	dt="text";
            	//Add support for auto complete text, only for fk column
            	if( refCol!=null)col.setIsAutoComplete(true);
            }
            String ds=( Validator.isNull(dt)?"text":dt) + "(" + 
				Tools.getInt(column.getDisplaycols(), 1)+","+
				Tools.getInt(column.getDisplayrows(), 1)+")";
            if(Tools.getInt(column.getDisplaywidth(),-1)>0){
            	ds +="["+ column.getDisplaywidth()+"]";
            }
				
            DisplaySetting dso=new DisplaySetting(ds);
            
            col.setDisplaySetting(dso);
            col.setErrorMessage(column.getErrmsg());
            col.setRegExpresion(column.getRegexpression()); // this must be later than setDisplaySetting
            col.setValueInterpreter(column.getInterpreter());
            
            String sum= (String)column.getSummethod();
            if( !Validator.isNull(sum) && !"0".equalsIgnoreCase(sum)){
            	col.setSubTotalMethod(sum);
            }
            PairTable pt=null;
        	AdLimitValueGroup avg= column.getAdLimitValueGroup();
        	if(avg!=null){
            	pt= new PairTable();
            	Set lvs=avg.getAdLimitValueSet();
            	for(Iterator it2=lvs.iterator();it2.hasNext();){
            		AdLimitValue alv= (AdLimitValue) it2.next();
            		//2010.02.21 only isactive one will be loaded yfzhu
            		if("Y".equalsIgnoreCase(alv.getIsActive()))
            				pt.put(alv.getValue(), alv.getDescription());
            	}
			}            
            if(pt!=null){
            	col.setIsValueLimited(true);
            	col.setValues(avg.getName(), pt);
            }
            if(loadingComments) col.setComment(column.getComments());
            //after displaysetting
            col.setFilter(column.getFilter());
            
    		String p=column.getProps();
    		if(nds.util.Validator.isNotNull(p)){
    			/*
    			 * accept both xml and json format to specify properties, xml must be readable by JSONObject.parseXML
    			 * json is the default format
    			 */
    			org.json.JSONObject jo=null;
    			try{
    				jo= new org.json.JSONObject(p);
    			}catch(Throwable t3){
    				
    				try{
    					jo =org.json.XML.toJSONObject(p);
    				}catch(Throwable t2){
    					logger.error("Fail to parse to json:"+ p, t2);
    					throw new nds.util.NDSRuntimeException("Fail to parse props of table "+ column.getName()+" to json:"+ t2);
    				}
    				
    			}
    			if(jo!=null) col.setJSONProps(jo);
    		  	
    			
    		}
            
        	table.addColumn(col);
		
		
	}
	private void createRefByTables(AdTable tb) throws Exception{
	try{
		TableImpl table=(TableImpl) tables.get(tb.getId());
		if(table ==null ){
			throw new NDSException("Unexpected, not found "+ tb.getName() + " in list, but want to create refby for it.");
		}
//		 table's dispatch column
		if(Tools.getYesNo( tb.getIsDispatchable(),false)){
			Integer dcid= tb.getDispColumnId();
			if( dcid==null){
				table.setDispatchColumn(null);
			}else{
				table.setDispatchColumn(table.getColumn(dcid.intValue()));
			} 
		}		
		for(Iterator it=tb.getAdRefbytableSet().iterator();it.hasNext();){
			AdRefbytable afb= (AdRefbytable) it.next();
			// skip those not active
			if(!"Y".equals(afb.getIsActive())) continue;
			TableImpl ti= (TableImpl) tables.get(afb.getAdRefbyTableId());
			// ti may not exists if it's inactive
			if( ti==null){
				logger.debug("Could not load "+ afb.getAdRefbyTable().getName()+" from list as "+ tb.getName()+"'s refby table(exists in db but not memory)");
				continue;
			}
			Column ci= ti.getColumn(afb.getAdRefbyColumnId().intValue());
			if( ci==null ) {
				throw new NDSException("Unexpected, not found column "+ afb.getAdRefbyColumn().getName() + " in memeory table.");
			}
			RefByTable rbt= new RefByTable(afb.getId().intValue(), table, ti,ci, afb.getFilter(),
					afb.getAssocType(), afb.getDescription(), afb.getInlineMode());
			table.addRefByTable(rbt);
		}
	}catch(Exception tt){
		logger.error("Fail to create ref-by table for "+ tb.getName(), tt);
		throw tt;
	}
		
	}
	public ArrayList getSubSystems(){
		
		ArrayList  col=new ArrayList( this.subsystems.values());
		ListSort.sort(col,"Orderno");
		return col;
	}
	/**
	 * By order specified in ad_tablecategory
	 * @return elements are TableCategory ordered by order no 
	 */
	public ArrayList getTableCategories(){
		ArrayList  col=new ArrayList( this.categories.values());
		ListSort.sort(col,"Order");
		return col;
	}
	/**
     * Return elements type of Table
     */
    public Iterator getTables() {
        return tables.values().iterator();
    }
}
