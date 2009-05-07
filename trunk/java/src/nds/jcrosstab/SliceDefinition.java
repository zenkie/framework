package nds.jcrosstab;

import java.util.*;

/** This object is set BEFORE the jCrosstabResultSet is built.
	It allows the user to define axis slices prior to launching the build,
	otherwise it would have to be all done in the Axis constructor.
*/
 
public class SliceDefinition 
{
	int    database_column_index = 1; //Number must be 1 or greater.

	MemberFormatter formatter=null;
	String description;
	String type = "String";  //The other option is Integer

	public SliceDefinition () {}

	public SliceDefinition (int i,MemberFormatter ft , String desc)
	{
		database_column_index = i;
		formatter=ft;
		description=desc;
	}
	/**
	 * Slice description
	 * @return
	 */
	public String getDescription(){
		return description;
	}
	public MemberFormatter getFormatter() {
		return formatter;
	}
	public void setFormatter(MemberFormatter formatter) {
		this.formatter = formatter;
	}


	public void setDatabaseColumnByIndex(int i)
	{
		database_column_index = i;
	}





	public int getDatabaseColumnIndex ()
	{
		return database_column_index;
	}

	public String toString ()
	{
		return "SliceDefinition: "+ description+":type: " + type + ", " +
			"index: " + database_column_index ;
	}

	public void setType (String t)
	{
		type = t;
	}

	public String getType ()
	{
		return type;
	}

}
