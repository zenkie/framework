/******************************************************************
*
*$RCSfile: SumFieldInterpreter.java,v $ $Revision: 1.2 $ $Author: Administrator $ $Date: 2005/12/18 14:06:16 $
*
********************************************************************/
package nds.query.web;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;

import nds.log.Logger;
import nds.log.LoggerManager;
import nds.query.QueryEngine;
import nds.query.QueryException;
import nds.query.QueryUtils;
import nds.schema.SQLTypes;
import nds.util.ColumnInterpretException;
import nds.util.ColumnInterpreter;
import nds.util.StringUtils;
/**
 * 计算table 的SumField 伪列，一般情况下，由constructor 的参数(sql)指明这个sum是如何计算的。
 * sql的参数是table record的id (在sql中用$0表示）, 通过parseValue的参数指明。
 * @see nds.schema.Table#getSumFields
 */
public class SumFieldInterpreter implements /*ColumnInterpreter,*/java.io.Serializable {
    private static Logger logger= LoggerManager.getInstance().getLogger(SumFieldInterpreter.class.getName());
    private int type ;
    private String sql;
    private String colname;
    /**
     * @param colname, the colname on which the computation should be sumed on, normally is the
     *      sheet table id column in sheet item table
     * @param type, get from Query#getSQLType
     * @param sql without object id( which will be set by parseValue() parameter
     */
    public SumFieldInterpreter(String colname, int type, String sql) {
        this.colname=colname;
        this.type=type;
        this.sql=sql;
    }
    /**
     * param value String indeed of ( sheet table object Id)
     */
    public String parseValue(Object value) throws ColumnInterpretException{
      try{
        String rsql= StringUtils.replace(sql,"$0",""+value);// sql has a string '$0' signs for sheet table id
        ResultSet rs=QueryEngine.getInstance().doQuery(rsql);
        String nbsp="";
        String s="";
        if( rs.next()){
            int i=1;
            switch(type){
                    case SQLTypes.DECIMAL:
                    case SQLTypes.NUMERIC:
                    BigDecimal vb=rs.getBigDecimal(i);
                    if( vb==null) s="";
                    else {
                        s= vb+"";
                    }
                    break;
                    case SQLTypes.BIGINT:
                    case SQLTypes.INT:
                    case SQLTypes.SMALLINT:
                    case SQLTypes.TINYINT:
                    int intValue=rs.getInt(i);
                    // check for Range limited column,*defer to when getString(column)
                    // @see Column.isValueLimited(), Column.getValueInterpeter()
                    s=""+intValue;
                    break;
                    case SQLTypes.FLOAT:
                    try {
                        s=((java.text.DecimalFormat)QueryUtils.floatFormatter.get()).format(rs.getFloat(i));
                        //logger.debug("getting float: "+rs.getFloat(i)+",converting to :"+s );
                    } catch(Exception e) {
                        s=nbsp;
                    }
                    break;
                    case SQLTypes.REAL:
                    case SQLTypes.DOUBLE:
                    try {
                        s=((java.text.DecimalFormat)QueryUtils.floatFormatter.get()).format(rs.getDouble(i));
                        //logger.debug("getting double: "+rs.getDouble(i)+",converting to :"+s );
                    } catch(Exception e) {
                        s=nbsp;
                    }

                    break;
                    default:
                    throw new ColumnInterpretException("Unexpected supported type:"+type);
            }//end switch
        }
        return s;
      }catch(SQLException er){
        logger.debug("error",er);
        throw new ColumnInterpretException("Error", er);
      }catch(QueryException qe){
        logger.debug("error",qe);
        throw new ColumnInterpretException("Error", qe);
      }


    }
    public Object getValue(String str) {
        throw new IllegalArgumentException("Method not support.");
    }
}
