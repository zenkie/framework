/******************************************************************
*
*$RCSfile: SQLType.java,v $ $Revision: 1.1.1.1 $ $Author: Administrator $ $Date: 2005/03/15 11:23:24 $
*
*$Log: SQLType.java,v $
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
package nds.schema;

/**
 * Wrapper that converted from string definition
 * @see TypeConverter
 */
public class SQLType {
    private int type=-1, length=-1, precision=-1,scale=0;
    /**
     * @param type can be those have specific length, such as:<br>
     *  SQLTypes.DATE, SQLType.TINYINT
     */
    public SQLType(int type) {
        this.type=type;
        length=SQLTypes.getDefaultLength(type);
    }
    /**
     * @param type normally for chars or no precision number
     * @param length the length of type
     */
    public SQLType(int  type, int length){
        this.type=type;
        this.length=length;
    }
    /**
     * @param type normally for number type
     * @param precision the number of digits to the left of decimal point
     * @param scale the number of digits to the right of decimal point
     * <p>
     * See respective database document for more detailed informaion
     */
    public SQLType(int type, int precision,int scale ){
        this.type= type;
        this.length=scale > 0? precision+1+scale: precision;
        this.precision=precision;
        this.scale=scale;
    }
    public int getType(){
        return type;
    }
    public int getLength(){
        return length;
    }
    public int getPrecision(){
        return precision;
    }
    public int getScale(){
        return scale;
    }
    public String toString(){
        return "[type="+ SQLTypes.Types[type-1]+",length="+
                length+",precision="+precision+",scale="+scale+"]";
    }
}