/******************************************************************
*
*$RCSfile: AllTests.java,v $ $Revision: 1.2 $ $Author: Administrator $ $Date: 2005/03/16 09:05:14 $
*
*$Log: AllTests.java,v $
*Revision 1.2  2005/03/16 09:05:14  Administrator
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

import java.util.Properties;

import javax.sql.DataSource;

import nds.model.dao._RootDAO;
import nds.query.QueryEngine;
import nds.schema.TableManager;
import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests implements java.io.Serializable{
	public static void main(String[] args)throws Exception {
            System.out.println("Begin test");
            nds.log.LoggerManager.getInstance().init("/act/conf/nds.properties");
            long t=System.currentTimeMillis();
            nds.model.dao._RootDAO.initialize();
            System.out.println("Totoal time :" +(System.currentTimeMillis()-t)/1000.0);
            setup();
            //junit.textui.TestRunner.run(suite());
            System.out.println("End test");
	}
	protected static void setup(){
	     try{
	     	 long t=System.currentTimeMillis();
	         TableManager manager= TableManager.getInstance();
	         Properties props= new Properties();
	         props.setProperty("directory", "file:/act/tables.portal");
	         props.setProperty("defaultTypeConverter","nds.schema.OracleTypeConverter");
	         manager.init(props);
	         System.out.println("Totoal time :" +(System.currentTimeMillis()-t)/1000.0);

	         props.setProperty("dbms.type", "oracle");
	         
	         QueryEngine.getInstance((DataSource)null).init(props);
	         
	     }catch(Exception e){
	          e.printStackTrace();
	      }

	    }	
	public static Test suite() {
		TestSuite suite= new TestSuite("NDS schema tests");
//		suite.addTestSuite(nds.schema.test.DisplaySettingTest.class);                
		suite.addTestSuite(nds.schema.test.TableManagerTest.class);
//                suite.addTestSuite(nds.schema.test.OracleTypeConverterTest.class);
//                suite.addTestSuite(nds.schema.test.SchemaStructureTest.class);
//                suite.addTestSuite(nds.schema.test.TableManagerTest.class);

		return suite;
	}
}