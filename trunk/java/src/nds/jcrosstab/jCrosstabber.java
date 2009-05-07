package nds.jcrosstab;

import java.io.*;
import java.util.*;
import java.net.*;
import java.sql.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class jCrosstabber
{
	private static final Log logger = LogFactory.getLog(jCrosstabber.class);	
	
	jCrosstabResultSet jxtab = new jCrosstabResultSet();

	/** The primary method for this class, given a ResultSet object, it reuturns a jCrosstabResultSet data structure.
		Note: the ResultSet object passed in has to be created by a Statement object, NOT a PreparedStatement object.
		PreparedStatements do not put column names and such in the same places as Statement.  See TestHarness.java for
		an example.
	*/
	public jCrosstabResultSet getCrosstabResultSet (ResultSet rs) throws InadequateColumnCountException
	{
		java.util.Date start_time = new java.util.Date();

		ResultSetMetaData rsmd = null;
 
		try
		{
			rsmd = rs.getMetaData();
			logger.debug("jCrosstabber.java, 21: " + "ResultSet column count is " + rsmd.getColumnCount());
			for (int i=0; i<rsmd.getColumnCount(); i++)
			{
				logger.debug("jCrosstabber.java, 24: " + "column name is " + rsmd.getColumnName(i+1) + " and label is " + rsmd.getColumnLabel(i+1));
			}
 
			if (rsmd.getColumnCount() < 3)
			{
				throw new InadequateColumnCountException("Less than 3 columns present in ResultSet object.");
			}
		}
		catch (SQLException sex)
		{
			logger.debug("jCrosstabber.java, 34: " + "SQLException: " + sex.getMessage());
			logger.debug("jCrosstabber.java, 35: " + "SQLState: " + sex.getSQLState());
			logger.debug("jCrosstabber.java, 36: " + "VendorError: " + sex.getErrorCode());
		}

		//Clear out anything as needed before running the new data.
		jxtab.clear();

		//jxtab.setDefaultsIfNeeded();

		logger.debug("jCrosstabber.java, 44: " + "Going to set Axes values");
		jxtab.setAxesValues(rs);

		//logger.debug("jCrosstabber.java, 24: " + "horizontal axis is " + jxtab.horizontal_axis);
		//logger.debug("jCrosstabber.java, 25: " + "vertical axis is " + jxtab.vertical_axis);

		//logger.debug("jCrosstabber.java, 22: " + "Going to sort.");
		//jxtab.sortAxes();

		//logger.debug("jCrosstabber.java, 30: " + "Going to setMaps.");
		jxtab.setMaps();
		jxtab.setGrids();

		//logger.debug("jCrosstabber.java, 34: " + "v size is " + jxtab.vertical_axis.size());
		//.out.println("jCrosstabber.java, 35: " + "h size is " + jxtab.horizontal_axis.size());
 
		logger.debug("jCrosstabber.java, 60: " + "Going to setRows.");
		jxtab.setRows(rs);

		java.util.Date end_time = new java.util.Date();
		jxtab.setTime(end_time.getTime() - start_time.getTime());

		logger.debug("jCrosstabber.java, 66: " + "Exiting getCrosstabResultSet.");

		return jxtab;
	}

	public void setHorizontalAxisType (int slice_idx, String s)
	{
		jxtab.setHorizontalAxisType(slice_idx, s);
	}

	public void setVerticalAxisType (int slice_idx, String s)
	{
		jxtab.setVerticalAxisType(slice_idx, s);
	} 

	

	public void setHorizontalAxisByTableColumnIndex (int i)
	{
		jxtab.horizontal_axis.getSliceDefinition(0).setDatabaseColumnByIndex(i);
	}


	public void setVerticalAxisByTableColumnIndex (int i)
	{
		jxtab.vertical_axis.getSliceDefinition(0).setDatabaseColumnByIndex(i);
	}
	// ------ end single axis slice methods --------------------------------------

	public void addHorizontalSliceByTableColumnIndex(int i, MemberFormatter ft, String desc)
	{
		jxtab.horizontal_axis.addSliceByTableColumnIndex(i,ft, desc);
	}
	
	public void addVerticalSliceByTableColumnIndex(int i, MemberFormatter ft, String desc)
	{
		jxtab.vertical_axis.addSliceByTableColumnIndex(i,ft,desc);
	}
 
	
	public void addDataRowsColumnByIndex (int i, String functionName, HashMap params, String desc, 
			String format, String formula, String valueName) throws ClassNotFoundException
	{
		jxtab.addDataRowsColumnByIndex(i, functionName, params, desc,  format, formula, valueName);
	}
}
