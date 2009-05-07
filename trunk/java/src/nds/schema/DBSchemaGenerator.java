/*
 * Agile Control Technologies Ltd,. CO.
 * http://www.agileControl.com
 */
package nds.schema;

import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.StringTokenizer;

import nds.log.Logger;
import nds.log.LoggerManager;
import nds.util.CommandExecuter;
import nds.util.StringBeautifier;
import nds.util.Tools;
import nds.util.Validator;
import nds.util.*;
import java.util.*;

/**
 * Generate schema meta data( ad_table, ad_column) according to xml
 * @author yfzhu@agilecontrol.com
 */
public class DBSchemaGenerator {
	private Logger logger=LoggerManager.getInstance().getLogger(HibernateXMLGenerator.class.getName()); 
    
	private static String dir="/work6/portal/src/nds/model";
    private static String srcDir= "file:///E:/act/table.portal";
    private boolean lowerCase= true; // on unix platform all columns and table name should be lower case
    private HashMap tables=null; //key:tablename, value:null, if null, will generate all tables
    public DBSchemaGenerator() throws Exception{
    	
    	TableManager manager= TableManager.getInstance();
        Properties props= new Properties();
        props.setProperty("directory", "file:/act/tables.portal");
        props.setProperty("defaultTypeConverter","nds.schema.OracleTypeConverter");
        props.setProperty("store","file");
        manager.init(props);
        
    }
    /**
     * 
     * @param tables "table1,table2"
     */
    public void setTables(String ts){
    	TableManager manager= TableManager.getInstance();
    	StringTokenizer st= new StringTokenizer(ts, ",");
    	tables=new HashMap();
    	while( st.hasMoreTokens()){
    		String tb= st.nextToken();
    		Table t=manager.getTable(tb); 
    		if( t !=null ) {
    			tables.put(t.getName().toUpperCase(),t.getName().toUpperCase());
    		}else{
    			logger.warning(" Table "+ tb+ " not found.");
    		}
    	}
    }
    public void gen(String filePath)throws Exception{
    	TableMappingManager manager= new TableMappingManager( "file:/act/tables.portal");
        StringHashtable tableMaps=manager.getMappings();
        Vector c= new Vector(tableMaps.values());
    	StringBuffer sb=new StringBuffer();
    	StringBufferWriter b= new StringBufferWriter(sb);
    	b.println("variable AD_CLIENT_ID NUMBER;");
    	b.println("EXEC :AD_CLIENT_ID:=<YOU CLIENT ID HERE>;");
    	b.println();
    	b.println();
    	b.println("spool ddl.log;");
    	b.println("set feedback off;");
    	b.println("alter trigger tri_ad_table_bi disable;");
    	b.println("alter trigger tri_ad_column_bi disable;");
    	b.println("alter trigger tri_ad_column_bd disable;");
    	b.println("alter trigger tri_ad_table_bd disable;");
    	b.println("alter trigger tri_ad_column_bu disable;");
    	b.println();
    	// create ad_tablecategory first
    	createADTableCategoryRecords(b);
    	createDirectoryRecords(b);
    	createADLimitValueGroupRecords(b);
    	
    	Iterator it=c.iterator();
    	
    	while(it.hasNext()){
            TableMapping map=(TableMapping)it.next();
            if(tables!=null &&  null== tables.get(map.name.toUpperCase()) )continue;
       		b.println(map.toDDL());
       		b.println("prompt Inserted "+map.name+" ...");
        }
    	b.println();
    	b.println("exec gen_u_clobs;");
    	b.println("alter trigger tri_ad_table_bi enable;");
    	b.println("alter trigger tri_ad_column_bi enable;");
    	b.println("alter trigger tri_ad_column_bd enable;");
    	b.println("alter trigger tri_ad_table_bd enable;");
    	b.println("alter trigger tri_ad_column_bu enable;");
    	
    	b.println("spool off;");
        FileOutputStream file= new FileOutputStream(filePath);
        file.write(sb.toString().getBytes());
        file.close();
        logger.info(filePath + " generated.");
    }
    private void createDirectoryRecords(StringBufferWriter b){
    	TableManager manager= TableManager.getInstance();
    	HashMap map= new HashMap();
    	for(Iterator it=manager.getAllTables().iterator();it.hasNext();){
    		Table tb= (Table)it.next();
    		if(map.get(tb.getSecurityDirectory())==null){
    			map.put(tb.getSecurityDirectory(), tb.getSecurityDirectory());
    			b.println("insert into DIRECTORY(ID,AD_CLIENT_ID,ISACTIVE,NAME,DESCRIPTION,AD_TABLECATEGORY_ID ,URL,AD_TABLE_ID,CREATIONDATE,MODIFIEDDATE,OWNERID,MODIFIERID)VALUES("+
    					"get_sequences('DIRECTORY'),:AD_CLIENT_ID,'Y','"+tb.getSecurityDirectory()+"','"+
						tb.getDescription(manager.getDefaultLocale())+"',GET_ID_BY_NAME(null, 'ad_tablecategory','"+ tb.getCategory()+"'),'/html/nds/objext/listframeset.jsp?table="+
						tb.getName()+"','"+ tb.getId()+"',sysdate,sysdate,0,0);");
    		}
    		
    	}
    	b.println("prompt Inserted Directory...");
    }
    private void createADTableCategoryRecords(StringBufferWriter b){
    	HashMap map= new HashMap();//key: string category name, value: null
    	TableManager manager= TableManager.getInstance();
    	for(Iterator it=manager.getAllTables().iterator();it.hasNext();){
    		Table tb= (Table)it.next();
    		map.put(tb.getCategory(), tb.getCategory());
    	}
    	int i=0;
    	for( Iterator it= map.keySet().iterator();it.hasNext();){
    		String cat= (String ) it.next();
    		i++;
    		b.println("insert into AD_TABLECATEGORY(ID,AD_CLIENT_ID,ISACTIVE,NAME,ORDERNO,URL,COMMENTS,CREATIONDATE,MODIFIEDDATE,OWNERID,MODIFIERID) VALUES("+
    				"get_sequences('AD_TABLECATEGORY'), :AD_CLIENT_ID, 'Y','"+ cat+"',"+ (10*i)+",null,null, sysdate,sysdate,0,0);" );
    	}
    	b.println("prompt Inserted AD_TABLECATEGORY...");
    }
    private void createADLimitValueGroupRecords(StringBufferWriter b){
    	HashMap map=new HashMap();//key column name, value null;
    	TableManager manager= TableManager.getInstance();
    	for(Iterator it= manager.getAllTables().iterator();it.hasNext();){
    		for(Iterator it2= ((Table)it.next()).getAllColumns().iterator();it2.hasNext();){
    			Column col= (Column)it2.next();
    			if( col.isValueLimited()){
    				if( map.get( col.getName().toUpperCase())==null){
    					map.put( col.getName().toUpperCase(),col.getName().toUpperCase());
    					b.println("insert into AD_LIMITVALUE_GROUP(ID,AD_CLIENT_ID,ISACTIVE,NAME,VALUETYPE,COMMENTS,CREATIONDATE,MODIFIEDDATE,OWNERID,MODIFIERID) VALUES("+
    							"get_sequences('AD_LIMITVALUE_GROUP'), :AD_CLIENT_ID, 'Y','"+ col.getName().toUpperCase() +"','"+ SQLTypes.getHibernateType( col.getSQLType()) +"',null, sysdate,sysdate,0,0);" );
    					PairTable pt=col.getValues(Locale.getDefault()); // key:data, value: description
    					int i=0;
    					for(Iterator it3= pt.keys();it3.hasNext();){
    						String key= (String)it3.next();
    						i++;
    						b.println("insert into AD_LIMITVALUE(ID,AD_CLIENT_ID,ISACTIVE,AD_LIMITVALUE_GROUP_ID,VALUE,"+
    								"DESCRIPTION, ORDERNO,VALUETYPE,COMMENTS,CREATIONDATE,MODIFIEDDATE,OWNERID,MODIFIERID)  VALUES("+
    								"get_sequences('AD_LIMITVALUE'), :AD_CLIENT_ID, 'Y',get_id_by_name(:AD_CLIENT_ID,'AD_LIMITVALUE_GROUP','"+col.getName().toUpperCase()+"'),'"+ key +
									"','"+ pt.get(key)+"',"+ (i*10)+",'"+ SQLTypes.getHibernateType( col.getSQLType()) +"',null, sysdate,sysdate,0,0);" );
    					}
    					b.println();
    					
    				}
    			}
    		}
    	}
    	b.println("prompt Inserted LimitValueGroup...");
    	b.println();
    }
    public static void main(String[] args) throws Exception{
    	String tables=null;
    	//tables="ad_table,ad_column,ad_tablecategory,ad_limitvalue,ad_limitvalue_group,ad_aliastable,ad_refbytable";
    	
    	nds.log.LoggerManager.getInstance().init("/act/conf/nds.properties");    	
    	DBSchemaGenerator g = new DBSchemaGenerator();
    	//g.setTables("C_BPartner,C_BPartner_Location,C_V_E_BPartner");
        g.gen("/ddl.sql");
        
    }	
}
