/******************************************************************
*
*$RCSfile: SQLTypes.java,v $ $Revision: 1.1.1.1 $ $Author: Administrator $ $Date: 2005/03/15 11:23:24 $
*
*$Log: SQLTypes.java,v $
*Revision 1.1.1.1  2005/03/15 11:23:24  Administrator
*init
*
*Revision 1.1.1.1  2002/11/29 02:50:35  yfzhu
*Active POS
*
*Revision 1.3  2001/12/09 03:43:32  yfzhu
*no message
*
*Revision 1.2  2001/11/07 20:58:47  yfzhu
*no message
*
*
********************************************************************/
package nds.schema;


public final class SQLTypes {

  public final static String[] Types = { "CHAR","TINYINT","BIGINT","INT",
                               "SMALLINT","FLOAT","REAL","DOUBLE",
                               "NUMERIC","DECIMAL","DATE","VARCHAR",
                               "LONGVARCHAR","TIMESTAMP","TIME","BIT",
                               "BINARY","VARBINARY","LONGVARBINARY","NULL",
                               "OTHER","CLOB","DATENUMBER" };
  /**
   * Defalut length of types when shown as String
   */
  private final static int[] lengths={
    255, 4,10, 10,
    8, 20,20,20,
    20,20,40/*"1999/01/01-1999/02/03"*/,255,
    1000,40,40,1,
    65535,65535,65535,0,
    16,65535, 40/*"1999/01/01-1999/02/03"*/
  };
  public final static int  CHAR=1;
  public final static int  TINYINT=2;
  public final static int  BIGINT=3;
  public final static int  INT=4;
  public final static int  SMALLINT=5;
  public final static int  FLOAT=6;
  public final static int  REAL=7;
  public final static int  DOUBLE=8;
  public final static int  NUMERIC=9;
  public final static int  DECIMAL=10;
  public final static int  DATE=11;
  public final static int  VARCHAR=12;
  public final static int  LONGVARCHAR=13;
  public final static int  TIMESTAMP=14;
  public final static int  TIME=15;
  public final static int  BIT=16;
  public final static int  BINARY=17;
  public final static int  VARBINARY=18;
  public final static int  LONGVARBINARY=19;
  public final static int  NULL=20;
  public final static int  OTHER=21;
  public final static int  CLOB=22; // oracle clob
  public final static int DATENUMBER=23;
  /**
   * 
   * @param sqlType types defined in this class 
   * @return type that equals to hibernate type, include following:
   * "integer, long, short, float, double, character, byte, boolean, yes_no, true_false"
   * "string, date, time, timestamp,text,clob"
   */
  public final static String getHibernateType(int sqlType){
  	String type="string "; // default
  	switch(sqlType){
  	case SQLTypes.CHAR:type="string";break;
  	case SQLTypes.TINYINT:type="integer";break;
  	case SQLTypes.BIGINT:type="integer";break;
  	case SQLTypes.INT:type="integer";break;
  	case SQLTypes.SMALLINT:type="integer";break;
  	case SQLTypes.FLOAT:type="double";break;
  	case SQLTypes.REAL:type="double";break;
  	case SQLTypes.DOUBLE:type="double";break;
  	case SQLTypes.NUMERIC:type="double";break;
  	case SQLTypes.DECIMAL:type="double";break;
  	case SQLTypes.DATE:type="date";break;
  	case SQLTypes.VARCHAR:type="string";break;
  	case SQLTypes.LONGVARCHAR:type="string";break;
  	case SQLTypes.TIMESTAMP:type="date";break;
  	case SQLTypes.TIME:type="date";break;
  	case SQLTypes.BIT:type="integer";break;
  	case SQLTypes.BINARY:type="binary";break;
  	case SQLTypes.VARBINARY:type="binary";break;
  	case SQLTypes.LONGVARBINARY:type="binary";break;
  	case SQLTypes.NULL:type="string";break;
  	case SQLTypes.OTHER:type="string";break;
  	case SQLTypes.CLOB:type="string";break;
  	case SQLTypes.DATENUMBER:type="integer";break;
  	}
  	return type;
  }
  /**
   * Convert java.sql.Types to SQLTypes
   * @param javaSQLType defined in java.sql.Types
   * @return int defined in SQLTypes
   * @see java.sql.Types
   */
  public final static int convertToSQLType(int javaSQLType){
    int type=javaSQLType;
    switch (javaSQLType)
    {
	case java.sql.Types.BIT 	:type=BIT;break;
	case java.sql.Types.TINYINT 	:type=TINYINT;break;
	case java.sql.Types.SMALLINT	:type=SMALLINT;break;
	case java.sql.Types.INTEGER 	:type=INT;break;
	case java.sql.Types.BIGINT 	:type=BIGINT;break;
	case java.sql.Types.FLOAT 	:type=FLOAT;break;
	case java.sql.Types.REAL 	:type=REAL;break;
	case java.sql.Types.DOUBLE 	:type=DOUBLE;break;
	case java.sql.Types.NUMERIC 	:type=NUMERIC;break;
	case java.sql.Types.DECIMAL	:type=DECIMAL;break;
	case java.sql.Types.CHAR	:type=CHAR;break;
	case java.sql.Types.VARCHAR 	:type=VARCHAR;break;
	case java.sql.Types.LONGVARCHAR :type=CLOB;break;
	case java.sql.Types.DATE 	:type=DATE;break;
	case java.sql.Types.TIME 	:type=TIMESTAMP;break;
	case java.sql.Types.TIMESTAMP 	:type=TIMESTAMP;break;
	case java.sql.Types.BINARY	:type=VARBINARY;break;
	case java.sql.Types.VARBINARY 	:type=LONGVARBINARY;break;
	case java.sql.Types.LONGVARBINARY :type=LONGVARBINARY;break;
	case java.sql.Types.NULL	 : break;//???
	case java.sql.Types.OTHER	:type=BIGINT;break;
    case java.sql.Types.CLOB       :type=CLOB;break;

/*        case java.sql.Types.JAVA_OBJECT         = 2000;
        case java.sql.Types.DISTINCT            = 2001;
        case java.sql.Types.STRUCT              = 2002;
        case java.sql.Types.ARRAY               = 2003;
        case java.sql.Types.BLOB                = 2004;
        case java.sql.Types.CLOB                = 2005;
        case java.sql.Types.REF                 = 2006;
*/
      default:
        break;
    }

    if ((type < 1) || (type > 24))
    {
      System.err.println("Warning! -  is of a type not recognised. Value : "+type);
      System.err.println("Defaulting to string");
      type = 12;
    }
    return type;
  }
  public static int getDefaultLength(int type){
    return lengths[type-1];
  }
}