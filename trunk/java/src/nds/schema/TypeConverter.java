/******************************************************************
*
*$RCSfile: TypeConverter.java,v $ $Revision: 1.1.1.1 $ $Author: Administrator $ $Date: 2005/03/15 11:23:24 $
*
*$Log: TypeConverter.java,v $
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
 * Convert string to SQLType, with type, and length wrappered in
 */
public interface TypeConverter {
    /**
     * @param type exactly identitcal to which shown on ERWin, such as:<br>
     *  "number(10,2), varchar2(255), blob
     * @return wrappers the type defined in SQLTypes and length if the string specifies
     * @see SQLType
     * @throw ConverException when convertion not successful.
     */
    public SQLType convert(String type) throws ConvertException;
}