/******************************************************************
*
*$RCSfile: TypeConverterFactory.java,v $ $Revision: 1.2 $ $Author: Administrator $ $Date: 2005/03/16 09:05:14 $
*
*$Log: TypeConverterFactory.java,v $
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
package nds.schema;

import java.util.Properties;

import nds.log.Logger;
import nds.log.LoggerManager;

/**
 * Get converter that convert String type defined in ERWin to nds.schema.SQLTypes
 */
public class TypeConverterFactory {
    private static Logger logger=LoggerManager.getInstance().getLogger(TypeConverterFactory.class.getName());

    private static TypeConverterFactory instance=null;
    private TypeConverter defaultConverter=null;
    private TypeConverterFactory() {

    }
    /**
     * @param props Current needed properties are:<br>
     *      "defaultTypeConverter" value: should be a class name with full package path
     */
    public void init(Properties props){
        String dc= props.getProperty("defaultTypeConverter");
        if( dc ==null) defaultConverter=new OracleTypeConverter();
        else{
            try{
               defaultConverter=(TypeConverter) Class.forName(dc).newInstance();
            }catch(Exception er){
                logger.error("Could not get default converter: "+dc,er);
            }
        }
    }
    public TypeConverter getConverter(){
        if( defaultConverter == null){
            logger.debug("TypeConverterFactory not initialized, using default configuration.");
            defaultConverter=new OracleTypeConverter();
        }
        return defaultConverter;
    }
    public static TypeConverterFactory getInstance(){
        if( instance ==null){
            instance=new TypeConverterFactory();
        }
        return instance;
    }
}