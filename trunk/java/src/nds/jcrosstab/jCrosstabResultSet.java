package nds.jcrosstab;

import nds.jcrosstab.fun.*;

import nds.util.BshScriptUtils;
import nds.util.StringUtils;
import nds.util.Tools;
import nds.util.Validator;

import java.io.*;
import java.text.DecimalFormat;
import java.util.*;
import java.net.*;
import java.sql.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import bsh.Interpreter;
 
public class jCrosstabResultSet
{ 
	private static final Log logger = LogFactory.getLog(jCrosstabResultSet.class);
	//String types are the default unless otherwise specified.
	Axis horizontal_axis = new Axis(true);  //Constructor takes a boolean "is_horizontal"
	Axis vertical_axis = new Axis(false);
	ArrayList factColumns=new ArrayList(); // elements are FactColumnInfo
	Object[][][] data;
	float build_time_seconds = -1; 
	long rowsFetched;
	// ---------------------------------------------------------------
	// Constructors
	// ---------------------------------------------------------------
	public jCrosstabResultSet ()
	{}
	/**
	 * Row fetched from db
	 * @return
	 */
	public long getRowsFetched(){
		return rowsFetched;
	}
	/**
	 * 
	 * @param vIdx the slice index in vertical axis, start from 0
	 * @return
	 */
	public MemberFormatter getVerticalFormatter(int vIdx){
		return ((SliceDefinition)vertical_axis.slices.get(vIdx)).getFormatter();
	}
	/**
	 * 
	 * @param hIdx the slice index in horizontal axis, start from 0
	 * @return
	 */
	public MemberFormatter getHorizontalFormatter(int hIdx){
		return ((SliceDefinition)horizontal_axis.slices.get(hIdx)).getFormatter();
	}
	

	// ---------------------------------------------------------------
	// Methods to set PRIOR TO actually building the crosstabs
	// ---------------------------------------------------------------

	public void setHorizontalAxisType (int slice_idx, String s)
	{
		horizontal_axis.setSliceType(slice_idx, s);

		//The default resultset column for the horizontal axis is 1.
		horizontal_axis.getSliceDefinition(slice_idx).setDatabaseColumnByIndex(1);
		//logger.debug("jCrosstabResultSet.java, 18: " + "Set horizontal axis to Integer.");
	}

	public void setVerticalAxisType (int slice_idx, String s)
	{
		vertical_axis.setSliceType(slice_idx, s);

		//The default resultset column for the vertical axis is 2.
		vertical_axis.getSliceDefinition(slice_idx).setDatabaseColumnByIndex(2);
		//logger.debug("jCrosstabResultSet.java, 25: " + "Set vertical axis to Integer.");
	}

	public void clear()
	{
		horizontal_axis.clear();
		vertical_axis.clear();
		//data_rows = null;
	}
	/**
	 * 
	 * @param i position in sql result set, the position in measure array is by order that calls this method
	 * @param functionName
	 * @param params
	 * @param columnDesc
	 * @param format
	 * @throws ClassNotFoundException
	 */
	public void addDataRowsColumnByIndex(int i, String functionName, HashMap params, 
			String columnDesc, String format, String formula, String valueName) throws ClassNotFoundException{
		
		FactColumnInfo fact=new FactColumnInfo();
		fact.columnIndex=i;
		fact.functionName= functionName;
		fact.params=params;
		fact.columnDesc= columnDesc;
		fact.format=format;
		fact.formula=formula;
		fact.valueName= valueName;
		if( fact.isComputeColumn()) fact.createInterpreter();
		else fact.createFunction();
		this.factColumns.add(fact);
	}
	public int getDataColumnCount(){
		return factColumns.size();
	}
	/**
	 * 
	 * @param i position in columns , start from 0, max to getDataColumnCount()-1
	 * @return java.sql.Types.INTEGER or java.sql.Types.DOUBLE
	 */
	public int getDataColumnType(int i){
		if(((FactColumnInfo)factColumns.get(i)).function!=null)  
			return ((FactColumnInfo)factColumns.get(i)).function.getReturnType();
		return java.sql.Types.DOUBLE;
	}
	public String getDataColumnDescription(int i){
		return ((FactColumnInfo)factColumns.get(i)).columnDesc;
	}
	/**
	 * Format return value
	 * @return
	 */ 
	public DecimalFormat getValueFormatter(int i){
		return ((FactColumnInfo)factColumns.get(i)).getValueFormat();
	}

	// --------------------------------------------------------------------------------
	// These are the methods called to actually build the crosstab data
	// --------------------------------------------------------------------------------


	public void addHorizontalValue(String h)
	{
		//logger.debug("jCrosstabResultSet.java, 60: " + "Adding horizontal value " + h);
		horizontal_axis.addValue(h);
	}

	public void addHorizontalValue(String h, String h2)
	{
		horizontal_axis.addValue(h, h2);
	}

	public void addVerticalValue(String v)
	{
		//logger.debug("jCrosstabResultSet.java, 66: " + "Adding vertical value " + v);
		vertical_axis.addValue(v);
	}

	public void addVerticalValue(String h, String h2)
	{
		vertical_axis.addValue(h, h2);
	}

	public void setMaps()
	{
		horizontal_axis.setMap();
		vertical_axis.setMap();
	}

	public void setTime(float i)
	{
		build_time_seconds = i/1000;
	}

	/** This method traverses the input ResultSet object and sends values to the 
		horizontal and vertical axes to add to thier values list(s).
	*/
	public void setAxesValues (ResultSet rs)
	{
 
		try
		{ 
			ArrayList slice_value_list = new ArrayList();//<String>

			logger.debug("jCrosstabResultSet.java, 166: " + "horizontal axis slice count is " + horizontal_axis.getSliceCount());
			for (int i = 0; i<horizontal_axis.getSliceCount(); i++)
			{
				rs.beforeFirst();
				while(rs.next())
				{
					slice_value_list.clear();
					for (int j=0; j<=i; j++)
					{
						slice_value_list.add(rs.getString(horizontal_axis.getSliceDefinition(j).getDatabaseColumnIndex()));
					}
					//logger.debug("jCrosstabResultSet.java, 181: " + "going to axis.addValue slice_value_list is " + slice_value_list);
					horizontal_axis.addValue(slice_value_list);
					//logger.debug("jCrosstabResultSet.java, 183: " + "Returned from axis.addValue, slice_value_list is " + slice_value_list);
				}
				horizontal_axis.sort(i);
			}

			logger.debug("jCrosstabResultSet.java, 187: " + "vertial axis slice count is " + vertical_axis.getSliceCount());
			for (int i = 0; i<vertical_axis.getSliceCount(); i++)
			{
				rs.beforeFirst();
				while(rs.next())
				{
					slice_value_list.clear();
					for (int j=0; j<=i; j++)
					{
						slice_value_list.add(rs.getString(vertical_axis.getSliceDefinition(j).getDatabaseColumnIndex()));
					}
					vertical_axis.addValue(slice_value_list);
				}
				vertical_axis.sort(i);
			}
		}
		catch (SQLException sex)
		{
			logger.debug("jCrosstabResultSet.java, 208: " + "SQLException: " + sex.getMessage());
			logger.debug("jCrosstabResultSet.java, 209: " + "SQLState: " + sex.getSQLState());
			logger.debug("jCrosstabResultSet.java, 210: " + "VendorError: " + sex.getErrorCode());
		}
	}

	/** This method traverses the input ResultSet and correctly sets values into the data rows.
		The horizontal and vertical axes must be correctly set and fully configured prior to calling this method.
	*/
	public void setRows(ResultSet rs)
	{
		
		SetWrapper[][][] data_rows;// v_index, h_index, column_index

		data_rows = new SetWrapper[vertical_axis.size()][horizontal_axis.size()][ this.factColumns.size() ];
		data = new Number[vertical_axis.size()][horizontal_axis.size()][ this.factColumns.size() ];
		for(int i=0;i< factColumns.size();i++){
			for(int j=0;j<vertical_axis.size();j++ )
				for(int k=0;k<horizontal_axis.size();k++ )data_rows[j][k][i]=new SetWrapper();
		}
		logger.debug("jCrosstabResultSet.java, 224: " + "set data_rows arrays of size v " + vertical_axis.size() + " h " + horizontal_axis.size());
		try
		{
			rs.beforeFirst();
			rowsFetched=0;
			//logger.debug("jCrosstabResultSet.java, 61: " + "Reading rows.");
			while(rs.next())
			{
				rowsFetched++;
				//logger.debug("jCrosstabResultSet.java, 232: " + "On resultset row " + rs.getRow());
				String h = "map-";
				for (int i = 0; i<horizontal_axis.slices.size(); i++)
				{
					h = h + rs.getString(horizontal_axis.getSliceDefinition(i).getDatabaseColumnIndex());
					if (i <  (horizontal_axis.slices.size()-1))
						h = h + "-";
				}

				String v = "map-";
				for (int i = 0; i<vertical_axis.slices.size(); i++)
				{
					v = v + rs.getString(vertical_axis.getSliceDefinition(i).getDatabaseColumnIndex());
					if (i <  (vertical_axis.slices.size()-1))
						v = v + "-";

					//logger.debug("jCrosstabResultSet.java, 256: " + "v is now " + v);
				}

				//logger.debug("jCrosstabResultSet.java, 259: " + "Getting hashes for " + h + " and " + v);
				int h_idx = horizontal_axis.getHash(h);
				int v_idx = vertical_axis.getHash(v);
				//logger.debug("jCrosstabResultSet.java, 262: " + "indices are h=" + h_idx + " and v=" + v_idx);

				//logger.debug("jCrosstabResultSet.java, 297: " + "Setting data values");
				//logger.debug("jCrosstabResultSet.java, 257: " + "horizontal axis is " + horizontal_axis);
				//logger.debug("jCrosstabResultSet.java, 266: " + "vertical axis map size is " + vertical_axis.map.size());
				//logger.debug("jCrosstabResultSet.java, 267: " + "vertical axis map is " + vertical_axis.map.toString());

				//logger.debug("jCrosstabResultSet.java, 268: " + rs.getString(data_rows_column_index));
				for(int i=0;i< this.factColumns.size();i++){
					try{
						data_rows[v_idx][h_idx][i].addValue(rs.getObject( ((FactColumnInfo)factColumns.get(i)).columnIndex));
					}catch(Throwable t){
						data_rows[v_idx][h_idx][i].addValue(t);
					}
				}
			}
			// normal measure
			
			for(int i=0;i< this.factColumns.size();i++){
				FactColumnInfo fci=(FactColumnInfo)factColumns.get(i);
				if(fci.isComputeColumn()){
					continue;
				}
				for(int j=0;j<vertical_axis.size();j++){
					for(int k=0;k<horizontal_axis.size();k++){
						data[j][k][i]= fci.executeFunction(data_rows[j][k][i]);
					}
				}
			}
			// compute measure
			for(int i=0;i< this.factColumns.size();i++){
				FactColumnInfo fci=(FactColumnInfo)factColumns.get(i);
				if(!fci.isComputeColumn()){
					continue;
				}
				for(int j=0;j<vertical_axis.size();j++){
					for(int k=0;k<horizontal_axis.size();k++){
						HashMap map=new HashMap();
						for(int m=0;m<this.factColumns.size();m++ ){
							FactColumnInfo v=(FactColumnInfo)factColumns.get(m);
							if(v.valueName!=null){
								map.put(v.valueName, FunUtil.nullValue.equals(data[j][k][m])? null:data[j][k][m]);
							}
						}
						data[j][k][i]= fci.evalFormula(map);
					}
				}
			}
			
		}
		catch (SQLException sex)
		{
			logger.debug("jCrosstabResultSet.java, 283: " + "SQLException: " + sex.getMessage());
			logger.debug("jCrosstabResultSet.java, 284: " + "SQLState: " + sex.getSQLState());
			logger.debug("jCrosstabResultSet.java, 285: " + "VendorError: " + sex.getErrorCode());

		}

		//logger.debug("jCrosstabResultSet.java, 85: " + "Exiting setRows");
	}

	public void setGrids()
	{
		horizontal_axis.setGrid();
		vertical_axis.setGrid();
	}

	public String[][] getHorizontalGrid()
	{
		return horizontal_axis.axis_grid;
	}

	public String[][] getVerticalGrid()
	{
		return vertical_axis.axis_grid;
	}

	public Object[][][] getDataGrid ()
	{
		return data;
	}
	/**
	 * Dump detail information
	 * @return
	 */
	public String toDetailString(){
		StringBuffer sb=new StringBuffer();
		sb.append("HorizontalGrid:\n");
		for(int i=0;i<horizontal_axis.axis_grid.length;i++ ){
			sb.append("[");
			for(int j=0;j<horizontal_axis.axis_grid[i].length;j++){
				sb.append(horizontal_axis.axis_grid[i][j]).append(",");
			}
			sb.append("]\n");
		}
		sb.append("VerticalGrid:\n");
		for(int i=0;i<vertical_axis.axis_grid.length;i++ ){
			sb.append("[");
			for(int j=0;j<vertical_axis.axis_grid[i].length;j++){
				sb.append(vertical_axis.axis_grid[i][j]).append(",");
			}
			sb.append("]\n");
		}
		sb.append("Data:\n");
		for(int i=0;i<data.length;i++ ){
			sb.append("[");
			for(int j=0;j<data[i].length;j++){
				sb.append(data[i][j][0]).append(",");
			}
			sb.append("]\n");
		}
		sb.append("HorizontalAxis:\n");
		sb.append(horizontal_axis.toString());
		sb.append("VerticalAxis:\n");
		sb.append(vertical_axis.toString());
		return sb.toString();
	}
	public int getVerticalAxisSliceCount ()
	{
		return vertical_axis.getSliceCount();
	}

	public int getHorizontalAxisSliceCount ()
	{
		return horizontal_axis.getSliceCount();
	}

	public String toString ()
	{
		StringBuffer str = new StringBuffer ("jCrosstabResultSet Dump\n");
		str.append("--------------------------\n");

		str.append(horizontal_axis.toString());
		str.append(vertical_axis.toString());
	
		str.append("data_rows_columns=" + Tools.toString(this.factColumns.toArray()));
	
		return str.toString();
	}

	public void sortOnColumn (int col_idx)
	{
		sortOnColumn(true, col_idx);
	}

	public void sortOnColumn (boolean sort_ascending, int col_idx)
	{
		/*boolean made_change = true;
		String[][] vert = vertical_axis.axis_grid;

		while (made_change)
		{
			made_change = false;
			for (int i=0; i<data_rows.length-1; i++)
			{
				if ( (sort_ascending && (data_rows[i][col_idx] > data_rows[i+1][col_idx])) ||
					 (!sort_ascending && (data_rows[i][col_idx] < data_rows[i+1][col_idx])) )
				{
					int[] temp = data_rows[i];
					data_rows[i] = data_rows[i+1];
					data_rows[i+1] = temp;
	
					//Have to sort the vertical Axis too.
					String[] stemp = vert[i];
					vert[i] = vert[i+1];
					vert[i+1] = stemp;

					made_change = true;
				}
			}
		}*/
	}

	public void sortOnRow (int row_idx)
	{
		sortOnRow(true, row_idx);
	}

	public void sortOnRow (boolean sort_ascending, int row_idx)
	{
		/*
		boolean made_change = true;
		String[][] hor = horizontal_axis.axis_grid;

		while (made_change)
		{
			made_change = false;

			for (int j=0; j<data_rows[row_idx].length-1; j++)
			{
				if ( (sort_ascending && (data_rows[row_idx][j] > data_rows[row_idx][j+1]) ) ||
					 (!sort_ascending && (data_rows[row_idx][j] < data_rows[row_idx][j+1]) )     )
				{
					//Flip the horizontal axis values
					for (int ii=0; ii<hor.length; ii++)
					{
							String stemp = hor[ii][j];
							hor[ii][j] = hor[ii][j+1];
							hor[ii][j+1] = stemp;
					}

					for (int ii=0; ii<data_rows.length; ii++)
					{
							int temp = data_rows[ii][j];
							data_rows[ii][j] = data_rows[ii][j+1];
							data_rows[ii][j+1] = temp;
					}

					//Flip the data grid values

					made_change = true;
				}
			}

		}*/
	}
}
/**
 * Fact column information
 * 
 * @author yfzhu@agilecontrol.com
 */
class FactColumnInfo{
	private static final Log logger = LogFactory.getLog(FactColumnInfo.class);	
	int columnIndex;// in sql selection, so can fetch data from ResultSet by this index
	String functionName;
	HashMap params;
	String columnDesc;
	String format;
	String formula;// compute column formula
	String valueName;
	Fun function=null;
	Interpreter interpreter=null;
	private DecimalFormat valueFormat= null; // used for comput 
	public DecimalFormat getValueFormat(){
		if(function!=null) return function.getValueFormatter();
		return valueFormat;
	}
	/**
	 * Create interpreter.
	 *    
	 * Note interpreter is reused for whole column data set
	 */
	public void createInterpreter(){
		interpreter= BshScriptUtils.createInterpreter();
		if(Validator.isNotNull(format))
			valueFormat = new DecimalFormat(format);
	}
	/**
	 * Return FunUtil.nullValue if error found    
	 * @param params
	 * @return
	 */
	public Object evalFormula(Map params){
		try{
			Object o=BshScriptUtils.evalScript(interpreter, formula, params);
			//logger.debug("formula="+ formula+",params="+ Tools.toString(params)+", result="+ o);
			return o;
		}catch(Throwable t){
			//logger.debug("bsh error for script:"+ formula+":"+ t);
			return FunUtil.nullValue;
		}
	}
	/**
	 * Compute column start with "="
	 * @return
	 */
	public boolean isComputeColumn(){
		return formula!=null;
	}
	/**
	 * Should be called when function created
	 * @param vs
	 * @return
	 */
	public Object executeFunction(SetWrapper vs){
		return function.execute(vs);
	}
	public Fun createFunction() throws ClassNotFoundException{
		if(function !=null) return function;
		
		function=FunFactory.createFunction(functionName);
		if(params!=null){
			for(Iterator it=params.keySet().iterator();it.hasNext();){
				Object key= it.next();
				function.addParameter((String)key,(String) params.get(key));
			}
		}
		if(Validator.isNotNull(format)){
			try{
				function.setValueFormatter(new DecimalFormat(format));
			}catch(Throwable t){
				logger.error("Invalid pattern:"+ format);
			}
		}
		return function;
	}
	public String toString(){
		return "index="+ columnIndex+",fun="+functionName+",param="+ Tools.toString(params)+",desc="+ columnDesc;
	}
}

