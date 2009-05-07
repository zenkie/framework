package nds.schema;

import java.io.FileOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.*;
import java.util.Vector;
import nds.log.*;
import nds.util.CommandExecuter;
import nds.util.StringBeautifier;
import nds.util.StringBufferWriter;
import nds.util.StringHashtable;
import nds.util.StringUtils;
import nds.util.Tools;
import nds.util.*;


public class HibernateXMLGenerator {
	private Logger logger=LoggerManager.getInstance().getLogger(HibernateXMLGenerator.class.getName()); 
    
	private static String dir="/work6/portal/src/nds/model";
    private static String srcDir= "file:///E:/act/table.portal";
    private boolean lowerCase= true; // on unix platform all columns and table name should be lower case
    // Table
    private ArrayList tables; // if null, will generate all tables,
    private HashMap escapeTables=new HashMap();
    private TableManager manager;
    public HibernateXMLGenerator() throws Exception{
    	manager= TableManager.getInstance();
        Properties props= new Properties();
        props.setProperty("directory", "file:/act/tables.portal");
        props.setProperty("store", "file");
        props.setProperty("defaultTypeConverter","nds.schema.OracleTypeConverter");
        manager.init(props);
        
    }
    public void setEscapeTables(String es){
    	StringTokenizer st= new StringTokenizer(es, ",");
    	
    	while( st.hasMoreTokens()){
    		String tb= st.nextToken();
    		Table t=manager.getTable(tb); 
    		if( t !=null ) {
    			escapeTables.put( new Integer(t.getId()),new Integer(t.getId())); 
    		}else{
    			logger.warning(" Table "+ tb+ " not found.");
    		}
    	}
    	
    }
    /**
     * 
     * @param tables "table1,table2"
     */
    public void setTables(String ts){
    	StringTokenizer st= new StringTokenizer(ts, ",");
    	tables=new ArrayList();
    	while( st.hasMoreTokens()){
    		String tb= st.nextToken();
    		Table t=manager.getTable(tb); 
    		if( t !=null ) {
    			tables.add(t);
    		}else{
    			logger.warning(" Table "+ tb+ " not found.");
    		}
    	}
    }
    public void gen()throws Exception{
    	Iterator it;
    	if( tables!=null) it= tables.iterator();
    	else it=manager.getAllTables().iterator();
    	int i=0;
    	StringBuffer sb=new StringBuffer();
    	StringBufferWriter b=new StringBufferWriter(sb);
        b.print("<?xml version=\"1.0\"?>");
        b.println("<!DOCTYPE hibernate-mapping PUBLIC");
        b.println("\"-//Hibernate/Hibernate Mapping DTD//EN\"");
        b.println("\"http://hibernate.sourceforge.net/hibernate-mapping-2.0.dtd\" >");
        b.println("<hibernate-mapping package=\"nds.model\">");
    	b.pushIndent();
        while(it.hasNext()){
        	Table tb=(Table) it.next();
        	i++;
        	if( this.escapeTables.get(new Integer(tb.getId()))==null && ! tb.isView() && tb.getRealTableName().equalsIgnoreCase(tb.getName()))
        		b.println(createTableXML((TableImpl) tb));
        }
        b.popIndent();
        b.println("</hibernate-mapping>");

        FileOutputStream file= new FileOutputStream(dir+"/"+"nds.hbm");
        file.write(sb.toString().getBytes());
        file.close();
        
        logger.debug("Total " + i+ " tables generated.");
    }
    private String createTableXML(TableImpl tb) throws Exception{
        logger.debug( tb.getName());
        String s=tb.toHibernateXML(false);
        return s;
    }    
    /*public void gen()throws Exception{
    	Iterator it;
    	if( tables!=null) it= tables.iterator();
    	else it=manager.getAllTables().iterator();
    	int i=0;
        while(it.hasNext()){
        	Table tb=(Table) it.next();
        	i++;
        	if( this.escapeTables.get(new Integer(tb.getId()))==null && ! tb.isView() && tb.getRealTableName().equalsIgnoreCase(tb.getName()))
        		createTableXML((TableImpl) tb);
        }
        logger.debug("Total " + i+ " tables generated.");
    }
    private void createTableXML(TableImpl tb) throws Exception{
        logger.debug( tb.getName());
        String s=tb.toHibernateXML();

        FileOutputStream file= new FileOutputStream(dir+"/"+StringBeautifier.beautify( tb.getName()) +".hbm");
        file.write(s.getBytes());
        file.close();
    }*/
    public static void main(String[] args) throws Exception{
    	String tables=null;
    	tables="ad_table";//,ad_column,ad_tablecategory,ad_limitvalue,ad_limitvalue_group,ad_aliastable,ad_refbytable";
    	
    	nds.log.LoggerManager.getInstance().init("/act/conf/nds.properties");    	
        HibernateXMLGenerator g = new HibernateXMLGenerator();
        if(tables !=null)g.setTables(tables);
        g.setEscapeTables("u_clob"); // do not generate xml for these tables
        g.gen();
        // touch all files in the dir and sub dirs
        SimpleDateFormat fm=new SimpleDateFormat("yyyyMMddHHmm"); 
        String now= fm.format(new Date()); 
        String outFile="/hibernatexmlgen.out";
        CommandExecuter exc=new CommandExecuter(outFile);
        exc.run("cmd /c find "+dir + " | xargs touch -t " + now );
        String out=Tools.readFile(outFile);
        if( Validator.isNull(out))
        	System.out.println( "OK" );
        else
        	System.out.println( out );
        
    }
}