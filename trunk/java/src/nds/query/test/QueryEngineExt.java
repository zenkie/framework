/******************************************************************
*
*$RCSfile: QueryEngineExt.java,v $ $Revision: 1.1.1.1 $ $Author: Administrator $ $Date: 2005/03/15 11:23:24 $
*
*$Log: QueryEngineExt.java,v $
*Revision 1.1.1.1  2005/03/15 11:23:24  Administrator
*init
*
*Revision 1.3  2003/03/30 08:11:35  yfzhu
*Updated before subtotal added
*
*Revision 1.2  2002/12/17 08:45:37  yfzhu
*no message
*
*Revision 1.1.1.1  2002/11/29 02:50:34  yfzhu
*Active POS
*
*Revision 1.2  2001/11/07 20:58:47  yfzhu
*no message
*
*
********************************************************************/
package nds.query.test;

import java.sql.Connection;

import nds.query.QueryEngine;

/**
 * engine not using DataSource, using JDBC instead
 */
public class QueryEngineExt extends QueryEngine{
    private DbConnectionDefaultPool pool;
    public QueryEngineExt(String a){
        super(a);
        pool= DbConnectionDefaultPool.getInstance();
    }
    public Connection getConnection()  {
        Connection dbConnection = pool.getConnection();
        return dbConnection;
    }

}