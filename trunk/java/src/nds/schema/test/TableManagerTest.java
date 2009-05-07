/******************************************************************
*
*$RCSfile: TableManagerTest.java,v $ $Revision: 1.4 $ $Author: Administrator $ $Date: 2005/08/28 00:27:05 $
*
*$Log: TableManagerTest.java,v $
*Revision 1.4  2005/08/28 00:27:05  Administrator
*no message
*
*Revision 1.3  2005/04/18 03:28:20  Administrator
*no message
*
*Revision 1.2  2005/03/16 09:05:15  Administrator
*no message
*
*Revision 1.1.1.1  2005/03/15 11:23:24  Administrator
*init
*
*Revision 1.1.1.1  2002/11/29 02:50:35  yfzhu
*Active POS
*
*Revision 1.2  2001/11/07 20:58:47  yfzhu
*no message
*
*
********************************************************************/
package nds.schema.test;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.*;
import java.util.*;
import javax.sql.*;
import junit.framework.TestCase;
import nds.schema.Column;
import nds.schema.Table;
import nds.schema.*;
import nds.log.*;
import nds.query.*;
public class TableManagerTest  extends TestCase {
	private Logger logger=LoggerManager.getInstance().getLogger(TableManagerTest.class.getName());
    TableManager manager;
    
//    DbConnectionDefaultPool pool;
    public TableManagerTest(String name) {
          super(name);
    }
    protected void setUp(){
     try{
         manager= TableManager.getInstance();
         Properties props= new Properties();
         props.setProperty("directory", "file:/act/tables.portal");
         props.setProperty("defaultTypeConverter","nds.schema.OracleTypeConverter");
         manager.init(props);
         props.setProperty("dbms.type", "oracle");
         QueryEngine.getInstance((DataSource)null).init(props);
         
     }catch(Exception e){
          e.printStackTrace();
      }

    }
    public void testCount(){
    	logger.debug("Total tables:"+manager.getAllTables().size());
    }
    
  /**
    * Connects to the database a get the MetaData.
    */
  public Connection connect() throws SQLException,ClassNotFoundException
  {
    Class.forName("oracle.jdbc.driver.OracleDriver");
    String url="jdbc:oracle:thin:@localhost:1521:test";
    String username="nds2";
    String password="abc123";
    Connection connection = DriverManager.getConnection(url,username,password);
    return connection;
  }
  public void testDB() throws Exception{
  	Connection conn= connect();
	  for(Iterator it=manager.getAllTables().iterator();it.hasNext();){
	  		Table tb=(Table)it.next();
	  		checkTable(tb, conn);
	  }
  	
  }
  private void checkTable(Table table,Connection conn ) throws Exception{
  	QueryRequestImpl query;
    query=QueryEngine.getInstance().createRequest(null);
    query.setMainTable(table.getId());
    query.addSelection(table.getPrimaryKey().getId());
    query.addAllShowableColumnsToSelection(Column.QUERY_LIST);
  	String sql= query.toSQL();
  	try{
  		conn.createStatement().executeQuery(sql);
  	}catch(Exception e){
  		logger.error(table.getName()+ ":: has error for sql:"+ e);
  		System.out.println(sql);
  	}
  }
  public void testTables(){
  	  logger.debug("Show ref-by tables ------------------");
  	  for(Iterator it=manager.getAllTables().iterator();it.hasNext();){
  	  		Table tb=(Table)it.next();
  	  		for(Iterator it2=tb.getRefByTables().iterator();it2.hasNext();){
  	  			RefByTable rbt= (RefByTable) it2.next();
  	  			logger.debug(tb.getName()+ ":: " + rbt);
  	  		}
  	  		//logger.debug(tb.getName()+ ":: " + tb.getDescription());
  	  }
  }

}