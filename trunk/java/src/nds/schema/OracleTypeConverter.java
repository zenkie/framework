/******************************************************************
*
*$RCSfile: OracleTypeConverter.java,v $ $Revision: 1.3 $ $Author: Administrator $ $Date: 2005/11/16 02:57:22 $
*
*$Log: OracleTypeConverter.java,v $
*Revision 1.3  2005/11/16 02:57:22  Administrator
*no message
*
*Revision 1.2  2005/05/27 05:01:50  Administrator
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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Hashtable;

import nds.log.Logger;
import nds.log.LoggerManager;
/**
 * Convert oracle types
 * Support special type : DATENUMBER which store as number(8) while only used for storing date type 'YYYYMMDD'
 * 
 */
public class OracleTypeConverter implements TypeConverter{
    private static Logger logger=LoggerManager.getInstance().getLogger(OracleTypeConverter.class.getName());
    private static Hashtable methods;
    static{
        methods=new Hashtable();
        addMethodByType("number");
        addMethodByType("varchar2");
        addMethodByType("smallint");
        addMethodByType("date");
        addMethodByType("varchar");
        addMethodByType("clob");
        addMethodByType("char");
        addMethodByType("datetime");
        addMethodByType("datenumber"); // store as number(8) while only used for storing date type 'YYYYMMDD', since 3.0
    }
    private static void addMethodByType(String type){
        // if type is "abc", then method name should be "convertAbc";
        String methodName= "convert"+ type.substring(0,1).toUpperCase()+ type.substring(1);
        try{
            methods.put(type, OracleTypeConverter.class.getMethod(methodName,new Class [] {java.lang.String.class}));
        }catch(Exception e){
            logger.debug("Method not found: public SQLType "+ methodName+"(String) for type "+ type, e);
            throw new RuntimeException("Method not found: public SQLType "+ methodName+"(String) in nds.schema.OracleTypeConverter");
        }
    }
    public OracleTypeConverter() {
    }
    public SQLType convert(String type)throws ConvertException{
    	//logger.debug("convert("+ type+")");
        type=type.trim();
        int q= type.indexOf('(');
        String grossType=null;
        if( q != -1){
            grossType=type.substring(0,q);
        }else grossType=type;
        Method method=(Method) methods.get(grossType);
        if( method ==null) throw new ConvertException(" Type \""+type+"\" not recognized by TypeConverter");
        Object obj=null;
        try{
            obj=method.invoke(this, new Object[]{type});
        }catch( InvocationTargetException e){
            logger.debug("Error found for type "+ type, e);
            throw new ConvertException(e.getMessage());
        }catch(IllegalAccessException er){
            logger.debug("Error found for type"+type, er);
            throw new ConvertException(er.getMessage());
        }
        return (SQLType )obj;
    }
    /**
     * @param type format number(p,s)
     */
    public SQLType convertNumber(String type) throws ConvertException{
        int ps= type.indexOf('(')+1;// precision start
        int comma= type.indexOf(',');// comman pos
        int se=type.indexOf(')');// scale end
        String p,s;
        SQLType t=null;
        if( comma != -1){
            p= type.substring(ps,comma);
            s= type.substring(comma+1,se);
            t=new SQLType(SQLTypes.DOUBLE,(new Integer(p)).intValue(), (new Integer(s)).intValue());
        }else{
            p= type.substring(ps, se);
            t=new SQLType(SQLTypes.BIGINT, (new Integer(p)).intValue(), 0);
        }
        return t;
    }
    public SQLType convertClob(String type) throws ConvertException{
        return new SQLType(SQLTypes.CLOB);
    }
    public SQLType convertVarchar2(String type) throws ConvertException{
        int ps= type.indexOf('(')+1;
        int se=type.indexOf(')');
        int length=( new Integer(type.substring(ps, se))).intValue();
        return new SQLType(SQLTypes.VARCHAR, length);
    }
    public SQLType convertVarchar(String type) throws ConvertException{
        return convertVarchar2(type);
    }
    public SQLType convertChar(String type) throws ConvertException{
        return convertVarchar2(type);
    }
    public SQLType convertSmallint(String type) throws ConvertException{
        return new SQLType(SQLTypes.SMALLINT);
    }
    public SQLType convertDate(String type) throws ConvertException{
        return new SQLType(SQLTypes.DATE, 10);
    }
    public SQLType convertDatetime(String type) throws ConvertException{
    	//'2007/08/23 HH:MM:SS'
        return new SQLType(SQLTypes.TIMESTAMP,19);
    }
    /**
     * DateNumber is number, while display and accept input as Date format
     * @param type DATENUMBER
     * @return 
     * @throws ConvertException
     */
    public SQLType convertDatenumber(String type) throws ConvertException{
    	// will allow input as '2007/08/23' 10 bits total
        return new SQLType(SQLTypes.DATENUMBER,10);
    }
}