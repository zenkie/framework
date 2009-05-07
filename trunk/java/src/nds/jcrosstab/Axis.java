package nds.jcrosstab;

import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
 

/** A generic Axis, could be either horizontal or vertical.  It contains
		one or more slices.
*/

public class Axis 
{
	private static final Log logger = LogFactory.getLog(Axis.class);
	
	ArrayList slices = new ArrayList();//<SliceDefinition>
	Slice first_slice = new Slice();
	Map map = null; //<String,Integer>

	private boolean is_horizontal = true;  //If false, then this must be the vertical axis.

	String[][] axis_grid = null;

	public Axis (boolean t)
	{
		is_horizontal = t;
	}

	/** Since this is a one value param, it has to go to slice 0.
		A two-value param means param 2 is a sub-value of param 1.... TODO...
	*/
	public void addValue(String s)
	{
		first_slice.addValue(s);
	}

	public void addValue(String s, String s2)
	{
		for (int i = 0; i< first_slice.sub_slice.size(); i++)
		{
			Slice slice = (Slice)first_slice.sub_slice.get(i);
			if (slice.getValue().equals(s))
			{
				slice.addValue(s2);
			}
		}
	}

	public void addValue(ArrayList value_list) //<String>
	{
		//System.out.println("Axis.java, 46: " + "Entered addValue value_list is " + value_list);
		first_slice.addValue(value_list);
		//System.out.println("Axis.java, 48: " + "Exiting addValue value_list is " + value_list);
	} 

	public void sort(int slice_idx)
	{
		first_slice.sort(slice_idx);
	}

	public int getSliceDefinitionCount()
	{
		return slices.size();
	}

	public int size ()
	{
		return map.size();
	}

	public int getHash(String s)
	{
		//System.out.println("Axis.java, 68: " + "Getting hash value for key " + s);
		return ((Integer) map.get(s)).intValue();
	}

	public int getColumnCount()
	{
		return first_slice.getColumnCount();
	}

	public void setMap()
	{
		//System.out.println("Axis.java, 70: " + "Entered setMap");
		map = new HashMap(getColumnCount()); //<String,Integer>

		int current_val = 0;

		for (int i = 0; i< first_slice.sub_slice.size(); i++)
		{
			Slice slice =(Slice) first_slice.sub_slice.get(i);
			String map_string = "map-";

			//System.out.println("Axis.java, 80: " + "going to slice " + i + " type is " + slice.type);
			current_val = slice.getMapSlice(map_string, map, current_val);
			//System.out.println("Axis.java, 82: " + "current val is " + current_val);
		}

		//System.out.println("Axis.java, 85: " + "Exiting setMap, map is " + map);
	}

	public void setGrid()
	{
		//System.out.println("Axis.java, 90: " + "Entered setGrid");
		// [-long-axis-] [-short-axis-]  of the grid.
		axis_grid = new String[slices.size()][getColumnCount()];
		//System.out.println("Axis.java, 93: " + "Set grid to be " + slices.size() + " by " + getColumnCount());

		int current_slice = 0;

		for (int i = 0; i< first_slice.sub_slice.size(); i++)
		{
			Slice slice =(Slice) first_slice.sub_slice.get(i);
			ArrayList grid_slice_values = new ArrayList(); //<String>

			current_slice = slice.getGridSlice(grid_slice_values, current_slice, axis_grid);
		}
		
		if (!is_horizontal)
		{
			//"rotate" the grid;
			String[][] rotated_grid = new String[getColumnCount()][slices.size()];  //LONG and THIN
			for (int i=0; i<axis_grid.length; i++)
			{
				for (int j=0; j<axis_grid[i].length; j++)
				{
					rotated_grid[j][i] = axis_grid[i][j];
				}
			}
			axis_grid=rotated_grid;
		}
	}

	public ArrayList getSliceValues(int slice_idx) //<String>
	{
		return first_slice.getSliceValues(slice_idx);
	}

	public ArrayList getSliceElements(int slice_idx) //<Slice>
	{
		return first_slice.getSliceElements(slice_idx);
	}

	public String toString ()
	{
		StringBuffer str = new StringBuffer("\n ---------- Axis Values ---------------- \n");

		str.append("\tSlice Definitions:\n");
		for (int i = 0; i< slices.size(); i++)
		{
			SliceDefinition defn =(SliceDefinition) slices.get(i);
			str.append(defn.toString());
		}
		str.append("\n");

		str.append("map, size is " + map.size() + ": " + map.toString()).append(":\n");
		str.append(nds.util.Tools.toString(map));
		str.append("\n First slice:");
		str.append(first_slice.toString(1));

		return str.toString();
	}

	public void clear()
	{
		System.out.println("Axis.java, 162: " + "Clearing axis");
		first_slice = new Slice();
		if (slices.size() > 0)
			first_slice.setSliceType(0, ((SliceDefinition)slices.get(0)).type);
		map = null;
	}

	public void addSliceByTableColumnIndex (int i, MemberFormatter ft, String desc)
	{
		slices.add(new SliceDefinition(i,ft, desc));
	}


	public SliceDefinition getSliceDefinition(int i)
	{
		return (SliceDefinition)slices.get(i);
	}

	public void addSliceIntegerByTableColumnIndex (int i, MemberFormatter ft,String desc)
	{
		slices.add(new SliceDefinition(i,ft,desc));
	}

 
	public void setSliceType (int slice_idx, String t)
	{
		SliceDefinition slice_defn = (SliceDefinition)slices.get(slice_idx);
		slice_defn.setType(t);

		first_slice.setSliceType(slice_idx, t);
	}

	public int getSliceCount()
	{
		return slices.size();
	}

	public int getSpan ()
	{
		return first_slice.getSpan();
	}

}
