package nds.jcrosstab;

import java.util.*;
import org.apache.commons.collections.ComparatorUtils;
/** A Slice is a lengthwise cross section of an axis.  For example,
	if you have the horizontal axis set to Year, Quarter, then that
	axis has 2 slices, the first by Year, then a second by Quarter.
*/
 
public class Slice
{
	String value = null;  //These will be null on the first slice, slice index 0? Is that right?
	Integer i_value = null;

	ArrayList values = new ArrayList();//<String>
	ArrayList i_values = new ArrayList(); //<Integer>

	ArrayList sub_slice = new ArrayList();//<Slice>

	String type = "String"; 

	public Slice () {}

	public Slice (String s)
	{
		value = s;
	}

	public Slice (Integer i)
	{
		i_value =i;// new Integer(i);
		type = "Integer";
	}

	public String toString (int tabs)
	{
		String spacer = "\t";

		for (int i = 0; i<tabs; i++)
		{
			spacer = spacer + "\t";
		}

		StringBuffer str = new StringBuffer("\n" + spacer + " --- Slice --- " + value + " " + i_value + " -------------------- \n");
		str.append(spacer + "Slice type: " + type + " value: " + value + " i_value: " + i_value + "\n");

		str.append(spacer + "String values: " + values + "\n");
		str.append(spacer + "Integer values: " + i_values + "\n");

		if (sub_slice.size() > 0)
		{
			str.append(spacer + "... sub_slice: \n");
			for (int i = 0; i<sub_slice.size(); i++)
			{
				Slice slice =(Slice) sub_slice.get(i);
				str.append(slice.toString(tabs + 1));
			}
		}

		str.append(spacer + "--- End Slice ------------------------------------------ \n");

		return str.toString();
	}

	public int getColumnCount()
	{
		//System.out.println("Slice.java, 63: " + "Entered column count for slice: " + getValue());
		if (sub_slice.size()>0)
		{
			int col_count = 0;
			for (int i = 0; i<sub_slice.size(); i++)
			{
				Slice slice = (Slice)sub_slice.get(i);
				col_count = col_count + slice.getColumnCount();
			}
			//System.out.println("Slice.java, 73: " + "returning column count of " + col_count);
			return col_count;
		}
		else
		{
			//System.out.println("Slice.java, 78: " + "sub slice is size 0, returning 0");
			return 1;
		}
	}

	public String getValue ()
	{
		if (type.equals("Integer"))
		{
			return i_value.toString();
		}
		else
		{
			return value;
		}
	}

	public int getMapSlice(String map_string, Map/*<String,Integer>*/ map, int current_index)
	{
		map_string = map_string + getValue();

		if (sub_slice.size() > 0)
		{
			for (int i = 0; i< sub_slice.size(); i++)
			{
				Slice slice =(Slice) sub_slice.get(i);
				//System.out.println("Slice.java, 108: " + "Going to sub-slice");
				current_index = slice.getMapSlice(map_string + "-", map, current_index);
			}
		}
		else
		{
			//bottom of tree, time to set map key
			//System.out.println("Slice.java, 115: " + "At bottom of map slice, map_string is " + map_string + " current index is " + current_index);
			map.put(map_string,new Integer( current_index));
			current_index++;
		}

		//For thought:  set span here??
		return current_index;
	}

	public int getGridSlice(ArrayList/*<String>*/ grid_slice_values, int current_slice, String[][] axis_grid)
	{
		//System.out.println("Slice.java, 128: " + "Entered getGridSlice, values are " + grid_slice_values);
		grid_slice_values.add(getValue());
		if (sub_slice.size() > 0)
		{
			for (int i = 0; i< sub_slice.size(); i++)
			{
				Slice slice = (Slice)sub_slice.get(i);
				current_slice = slice.getGridSlice(grid_slice_values, current_slice, axis_grid);
				//Following is bug found by yfzhu, may delete other element with same value
				//Just delete the last object
				//grid_slice_values.remove(grid_slice_values.get(grid_slice_values.size()-1));
				grid_slice_values.remove(grid_slice_values.size()-1);
			}
		}
		else
		{
			for (int i = 0; i< grid_slice_values.size(); i++)
			{
				//System.out.println("Slice.java, 143: " + "Going to add values to grid " + grid_slice_values);
				axis_grid[i][current_slice] =(String) grid_slice_values.get(i);
			}
			current_slice++;
		}

		return current_slice;
	}

	public void addValue(String s)
	{
		if (type.equals("Integer"))
		{
			if (!i_values.contains(new Integer(Integer.parseInt(s))))
				i_values.add(new Integer(Integer.parseInt(s)));
		}
		else //Default String type
		{
			if (!values.contains(s))
				values.add(s);
		}
	}

	public void addValue(ArrayList/*<String>*/ value_list){
		if (value_list == null){
			System.out.println("Slice.java, 168: " + "Error: value list is null");
		}else if (value_list.size() == 0){
			System.out.println("Slice.java, 172: " + "Error: value list size is 0");
		}else if (value_list.size() == 1){
			addValue((String)value_list.get(0));
		}else{
			//Go thru sub_slice, see which one to send the value list to.
			for (int i=0; i<sub_slice.size(); i++){
				Slice slice = (Slice)sub_slice.get(i);
				if((value_list.get(0)==null &&  slice.getValue()==null) || (slice.getValue()!=null && slice.getValue().equals(value_list.get(0)))){
					value_list.remove(0);
					slice.addValue(value_list);
					return;
				}
			}
		}
	}

	public void sort(int slice_idx)
	{
		//System.out.println("Slice.java, 168: " + "Entered slice sort for slice_idx " + slice_idx);
		if (slice_idx == 0)
		{
			if (type.equals("Integer"))
			{
				Collections.sort(i_values, ComparatorUtils.nullLowComparator(null));
		
				for (int i = 0; i< i_values.size(); i++)
				{
					sub_slice.add(new Slice( (Integer) i_values.get(i)));
				}
			}
			else
			{
				//Default type String
				Collections.sort(values, ComparatorUtils.nullLowComparator(null));
		
				for (int i = 0; i< values.size(); i++)
				{
					sub_slice.add(new Slice((String)values.get(i)));
				}
			}
		}
		else
		{
			slice_idx--;
			//Now descend into the tree and set/sort each sub-slice.
			for (int i = 0; i<sub_slice.size(); i++)
			{
				Slice slice =(Slice) sub_slice.get(i);
				slice.sort(slice_idx);
			}
		}
		//System.out.println("Slice.java, 201: " + "Exiting slice sort");
	}

	public void setSliceType (int slice_idx, String t)
	{
		type = t;
		//System.out.println("Slice.java, 207: " + "current slice is " + this.toString(2));
		for (int i = 0; i<sub_slice.size(); i++)
		{
			Slice slice =(Slice) sub_slice.get(i);
			slice.setSliceType(slice_idx, t);
		}
		//System.out.println("Slice.java, 213: " + "current slice is " + this.toString(2));

	}

	public ArrayList/*<String>*/ getSliceValues (int slice_idx)
	{
		if (slice_idx == 0)
		{
			if (type.equals("Integer"))
			{
				ArrayList/*<String>*/ new_v = new ArrayList();
				for (int i = 0; i < i_values.size(); i++)
				{
					new_v.add(i_values.get(i).toString());
				}
				return new_v;
			}
			else
			{
				//Default type String.
				return values;
			}
		}
		else
		{
			//Descend into each slice and get values, then append.
			ArrayList/*<String>*/ slice_vals_as_row = new ArrayList();

			for (int i = 0; i<sub_slice.size(); i++)
			{
				Slice slice = (Slice)sub_slice.get(i);
				slice_vals_as_row.addAll(slice.getSliceValues(slice_idx-1));
			}
			return slice_vals_as_row;
		}
	}

	/** This method returns the slice element parts, giving access to the span info etc.
	*/
	public ArrayList getSliceElements (int slice_idx) /*<Slice>*/
	{
		if (slice_idx == 0)
		{
			return sub_slice;
		}
		else
		{
			//Descend into each slice and get values, then append.
			ArrayList slice_vals_as_row = new ArrayList();//<Slice>

			for (int i = 0; i<sub_slice.size(); i++)
			{
				Slice slice =(Slice) sub_slice.get(i);
				slice_vals_as_row.addAll(slice.getSliceElements(slice_idx-1));
			}
			return slice_vals_as_row;
		}
	}

	public int getSpan ()
	{
		if (sub_slice.size() == 0)
		{
			return 1;
		}
		else
		{
			int slices_span = 0;
			for (int i = 0; i<sub_slice.size(); i++)
			{
				Slice slice =(Slice) sub_slice.get(i);
				slices_span = slices_span + slice.getSpan();
			}
			return slices_span;
		}
	}

}
