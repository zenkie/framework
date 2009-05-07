/******************************************************************
*
*$RCSfile: ColumnData.java,v $ $Revision: 1.1.1.1 $ $Author: Administrator $ $Date: 2005/03/15 11:23:24 $
*
*$Log: ColumnData.java,v $
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
/**
  * Container for details about a table column.
  *
  * @author: J.A.Carter
  * Release 1.1
  *
  * <c) Joe Carter 1998
  * Released under GPL. See LICENSE for full details
  */
package nds.schema.test;
public
class ColumnData
{
  // NB "TEXT" is my own add on!
  static String[] sqlTypes = { "CHAR","TINYINT","BIGINT","INT",
                               "SMALLINT","FLOAT","REAL","DOUBLE",
                               "NUMERIC","DECIMAL","DATE","VARCHAR",
                               "LONGVARCHAR","TIMESTAMP","TIME","BIT",
                               "BINARY","VARBINARY","LONGVARBINARY","NULL",
                               "OTHER","TEXT" };

  int type;
  int columns;
  String name;

/**
  * Standard constructor.
  * Requires name, XOPEN type, number of columns.
  * The last is only used for CHAR(x) and DATE fields.
  */
  public ColumnData (String name,int origType, int columns)
  {
    this.name = name;
    this.type = origType;
    this.columns = columns;

// Uncomment this is you want to see the raw types used
// for the columns. Usefully for debugging the inconsistent
// use in between drivers/databases.
//
//System.out.println("name : "+name+" type : "+origType);

    // sheesh, suppose I should change this to
    // a switch statement. TODO!!!
    //
    switch (type)
    {
      case -1:
        // Sybase
        // TEXT
        type = 22;
        break;

      case -2:
        //MySQL
        //TINYBLOB - use VARBINARY for now. Cobble alert!
        type = 18;
        break;

      case -3:
        // MySQL
        // medium blob / blob to LONGVARBINARY
        type = 19;

// Arse -> Some serious type conflicts here
// This issue needs sorting out. Need a heavy duty
// dig down the JDBC specs. Sigh...

// OOOPS - conflict. MySQL gets priority for now.
        // PostGres
        // abstime / timestamp
//        type = 14;  // use timestamp

// OOOPS - conflict. Postgres gets priority for now.
        //Oracle
        //RAW which maps to VARBINARY
//        type = 18;
        break;


      case -4:
        //Oracle
        //LONG RAW which maps to LONGVARBINARY
        type = 19;
        break;

      case -5:
        // MySQL
        // bigint
        type = 3;
        break;

      case -6:
        // Sybase
        // TINYINT
        type = 2;
        break;

      case -7:
        // Sybase
        // BIT
        type = 16;
        break;

      case 1111:
        // PostGres
        // bytea / int28 / reltime / tinterval
        type = 3;  // use bigint
        break;

      case 91:
        // PostGres
        // date
        type = 11;
        break;

      case 92:
        // PostGres
        // timestamp
        type = 14;
        break;

      case 93:
        // MySQL
        // timestamp
        type = 14;
        break;

      default:
        break;
    }

    if ((type < 1) || (type > 24))
    {
      System.err.println("Warning! - column name : "+name+
         " is of a type not recognised. Value : "+type);
      System.err.println("Defaulting to string");
      type = 12;
    }
  }

/**
  * Constructor with column type as a String.
  * Requires name, type, number of columns.
  * The last is only used for CHAR(x) and DATE fields.
  */
  public ColumnData (String name, String coltype, int columns)
  {
    this.name = name;
    this.columns = columns;

    int i=0;
    boolean quit = false;
    this.type = -1;  // invalid
    while (!quit)
    {
      if (coltype.toUpperCase().compareTo(sqlTypes[i]) == 0)
      {
        this.type = i+1;  // starts at 1
        quit = true;
      }

      i++;
      if (i>=sqlTypes.length)
        quit = true;
    }

    if (this.type == -1)
      System.out.println("Column name : "+name+" Type : "+coltype+" is unknown");
  }

/**
  * Returns the name of the column.
  */
  public String getName()
  {
    return name;
  }

/**
  * Converts to entry to a readable form.
  */
  public String toString()
  {
    String res = "";
    String digits = "";

	if ((type == 1) || (type == 11))
	  digits = "("+columns+")";
	else
	  digits = "";

    if ((type > sqlTypes.length) || (type < 0))
      res = "Type : "+type+"  Name : "+name;
    else
      res = "Type : "+sqlTypes[type-1]+digits+" Name : "+name;

	return res;
  }
  public int getColumnSize(){
    return columns;
  }
  /**
  * If you want to crate a new Java Object by column type, this will
  * be helpful
  */
  public String getJavaObjectType(){

    String jType = null;

    switch (type)
    {
      case 1:
      case 12:
      case 13:
      case 22:
        jType = "String";
        break;

      case 2:
        jType = "Byte";
        break;

      case 3:
        jType = "Long";
        break;

      case 4:
        jType = "Integer";
        break;

      case 5:
        jType = "Short";
        break;

      case 6:
      case 8:
        jType = "Double";
        break;

      case 9:
      case 10:
        jType = "BigDecimal";
        break;

      case 7:
        jType = "Float";
        break;

      case 11:
        jType = "java.util.Date";
        // always return Timestamp as it's more accurate
        // and is a superset of Date
        // NOPE - lets follow Suns recommendations, so...
        //jType = "Timestamp";
        break;

      case 14:
        jType = "Timestamp";
        break;

      case 15:
        jType = "Time";
        break;

      case 16:
        jType = "Boolean";
        break;

      case 17:
      case 18:
      case 19:
        jType = "ByteArrayObject"; // refer to DBTable.ByteArrayObject
        break;

      case 20:
        jType = "NullObject";//refer to DBTable.NullObject
        break;

      default:
        System.out.println("Warning - col type : "+type+" is unknown");
        jType = "unknown";
        break;
    }

    return jType;

  }
  /**
  * if the column's value is packed in a java Object, how could we extract
  * that value out of the object? This method will be helpful
  *
  * For instance, if we have a Long, to retrieve long value, we will call
  *
  *		(Long(obj)).longValue()
  *					^^^^^^^^^
  * This method will return "longValue" as above
  *
  * If there's no need for changing, such as Date,String, then return null;
  *
  */
  public String getMethodRetrievingValue(){
    String jType = null;

    switch (type)
    {
      case 1:
      case 12:
      case 13:
      case 22:
        jType =null ;//"toString";
        break;

      case 2:
        jType = "byteValue";
        break;

      case 3:
        jType = "longValue";
        break;

      case 4:
        jType = "intValue";
        break;

      case 5:
        jType = "shortValue";
        break;

      case 6:
      case 8:
        jType = "doubleValue";
        break;

      case 9:
      case 10:
        jType = null;// no need to change, it's BigDecimal
        break;

      case 7:
        jType = "floatValue";
        break;

      case 11:
        jType = null;// no need to change, it's Date
        // always return Timestamp as it's more accurate
        // and is a superset of Date
        // NOPE - lets follow Suns recommendations, so...
        //jType = "Timestamp";
        break;

      case 14:
        jType =null;// "Timestamp";
        break;

      case 15:
        jType = null;//"Time";
        break;

      case 16:
        jType = "booleanValue";
        break;

      case 17:
      case 18:
      case 19:
        jType = "getBytes"; // refer to DBTable.ByteArrayObject
        break;

      case 20:
        jType = "getNull";//refer to DBTable.NullObject
        break;

      default:
        System.out.println("Warning - col type : "+type+" is unknown");
        jType = "unknown";
        break;
    }

    return jType;

  }
  /**
   * size in byte unit, according to sql type, for instance, integer size is 4
   */
  public int getSize(){
    int size;

    switch (type)
    {
      case 1:
      case 12:
      case 13:
      case 22:
        size= columns;//jType = "String"; in fact each char in java is 2 byte
                      // we consider that the string field will be half-full at average
        break;

      case 2:
        size=1;//jType = "Byte";
        break;

      case 3:
        size=8;//jType = "Long";
        break;

      case 4:
        size=4;//jType = "Int";
        break;

      case 5:
        size=2;//jType = "Short";
        break;

      case 6:
      case 8:
        size=8;//jType = "Double";
        break;

      case 9:
      case 10:
        size=16;//jType = "BigDecimal";
        break;

      case 7:
        size=4;//jType = "Float";
        break;

      case 11:
        size=12;//jType = "Date";
        // always return Timestamp as it's more accurate
        // and is a superset of Date
        // NOPE - lets follow Suns recommendations, so...
        //jType = "Timestamp";
        break;

      case 14:
        size=12;//jType = "Timestamp";
        break;

      case 15:
        size=12;//jType = "Time";
        break;

      case 16:
        size=1;//jType = "Boolean";
        break;

      case 17:
      case 18:
      case 19:
        size=65535;//jType = "Bytes"; average 64K
        break;

      case 20:
        size=0;//jType = "Null";
        break;

      default:
        System.out.println("Warning - col type : "+type+" is unknown");
        size=4;//jType = "unknown";
        break;
    }

    return size;

  }
  /**
  * value of column in resultset can be retrieved using
  *
  *      rs.getXXXX(columnName) { XXX can be String, Int...}
  * 		   ^^^^
  *	thie method returns the XXXX
  *
  */
  public String getNameInResultSet(){
    String jType = null;

    switch (type)
    {
      case 1:
      case 12:
      case 13:
      case 22:
        jType = "String";
        break;

      case 2:
        jType = "Byte";
        break;

      case 3:
        jType = "Long";
        break;

      case 4:
        jType = "Int";
        break;

      case 5:
        jType = "Short";
        break;

      case 6:
      case 8:
        jType = "Double";
        break;

      case 9:
      case 10:
        jType = "BigDecimal";
        break;

      case 7:
        jType = "Float";
        break;

      case 11:
        jType = "Date";
        // always return Timestamp as it's more accurate
        // and is a superset of Date
        // NOPE - lets follow Suns recommendations, so...
        //jType = "Timestamp";
        break;

      case 14:
        jType = "Timestamp";
        break;

      case 15:
        jType = "Time";
        break;

      case 16:
        jType = "Boolean";
        break;

      case 17:
      case 18:
      case 19:
        jType = "Bytes";
        break;

      case 20:
        jType = "Null";
        break;

      default:
        System.out.println("Warning - col type : "+type+" is unknown");
        jType = "unknown";
        break;
    }

    return jType;

  }
/**
  * Writes out the equivalent java type of the column sql type.
  */
  public String getJavaType()
  {
    String jType = null;

    switch (type)
    {
      case 1:
      case 12:
      case 13:
      case 22:
        jType = "String";
        break;

      case 2:
        jType = "byte";
        break;

      case 3:
        jType = "long";
        break;

      case 4:
        jType = "int";
        break;

      case 5:
        jType = "short";
        break;

      case 6:
      case 8:
        jType = "double";
        break;

      case 9:
      case 10:
        jType = "java.math.BigDecimal";
        break;

      case 7:
        jType = "float";
        break;

      case 11:
        jType = "Date";
        // always return Timestamp as it's more accurate
        // and is a superset of Date
        // NOPE - lets follow Suns recommendations, so...
        //jType = "Timestamp";
        break;

      case 14:
        jType = "Timestamp";
        break;

      case 15:
        jType = "Time";
        break;

      case 16:
        jType = "boolean";
        break;

      case 17:
      case 18:
      case 19:
        jType = "byte[]";
        break;

      case 20:
        jType = "null";
        break;

      default:
        System.out.println("Warning - col type : "+type+" is unknown");
        jType = "unknown";
        break;
    }

    return jType;
  }
}
