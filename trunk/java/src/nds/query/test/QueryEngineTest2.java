/******************************************************************
*
*$RCSfile: QueryEngineTest2.java,v $ $Revision: 1.1.1.1 $ $Author: Administrator $ $Date: 2005/03/15 11:23:24 $
*
*$Log: QueryEngineTest2.java,v $
*Revision 1.1.1.1  2005/03/15 11:23:24  Administrator
*init
*
*Revision 1.2  2003/03/30 08:11:35  yfzhu
*Updated before subtotal added
*
*Revision 1.1.1.1  2002/11/29 02:50:34  yfzhu
*Active POS
*
*Revision 1.3  2001/11/11 12:45:39  yfzhu
*no message
*
*Revision 1.2  2001/11/07 20:58:47  yfzhu
*no message
*
*
********************************************************************/
package nds.query.test;

import nds.query.QueryException;
import nds.query.QueryResult;

public class QueryEngineTest2  {
    public QueryEngineTest2() {

    }
/*	protected void setUp() throws Exception {
                java.util.Properties props= new java.util.Properties();
                props.setProperty("directory",SchemaStructure.DEFAULT_SCHEMA_PATH);
                props.setProperty("defaultTypeConverter","nds.schema.OracleTypeConverter");
                nds.schema.TableManager.getInstance().init(props);
	}

    public void testDirectSQL() throws QueryException, SQLException{
        QueryEngineExt engine=new QueryEngineExt("aaa");
        ResultSet rs=engine.doQuery("select id from orders");
        while( rs.next()){
            int a=rs.getInt(1);
            break;
        }
    }
    public void tttestQueryRequest1() throws Exception{
        QueryEngineExt engine=new QueryEngineExt("aaa");
        QueryRequestImpl req=new QueryRequestImpl();
        TableManager manager= TableManager.getInstance();
        req.setMainTable(manager.CHEQTYERRADJSHT);
        req.addParam(manager.CHEQTYERRADJSHT_NO,"t" );
        //req.addParam(manager.CHEQTYERRADJSHT_APPLIERID,manager.EMPLOYEE_NAME,"d");
        req.addSelection(manager.CHEQTYERRADJSHT_ID);
        req.addSelection(manager.CHEQTYERRADJSHT_FILLERID,manager.EMPLOYEE_NAME,true);
        req.addSelection(manager.CHEQTYERRADJSHT_FILLERID,manager.EMPLOYEE_NO,true);
        int[] ids=new int[]{
            manager.CHEQTYERRADJSHT_FILLERID, manager.EMPLOYEE_DEPARTMENTID, manager.DEPARTMENT_NAME
        };
        req.addSelection(ids,true);
        req.addSelection(manager.CHEQTYERRADJSHT_AUDITORID,manager.EMPLOYEE_NAME, true);
        req.addSelection(manager.CHEQTYERRADJSHT_AUDITORID,manager.EMPLOYEE_NO,true);
        ids=new int[]{
            manager.CHEQTYERRADJSHT_AUDITORID, manager.EMPLOYEE_DEPARTMENTID, manager.DEPARTMENT_NAME
        };
        req.addSelection(ids,true);

//        System.out.println("UPSpeed :"+(System.currentTimeMillis() -start)+" milliseconds");

        req.setRange(0,2);
//        System.out.println(req.toSQL());
        QueryResult result=engine.doQuery(req);
        outputResult(result);
    }
    public void testQueryRequest2() throws Exception{
        QueryEngineExt engine=new QueryEngineExt("aaa");
        QueryRequestImpl req=new QueryRequestImpl();
        TableManager manager= TableManager.getInstance();

        req.setMainTable(manager.CHEQTYERRADJSHT);
        req.addParam(manager.CHEQTYERRADJSHT_NO,"2" );
        req.addSelection(manager.CHEQTYERRADJSHT_ID);
        req.addSelection(manager.CHEQTYERRADJSHT_FILLERID,manager.EMPLOYEE_NAME,true);
        req.addSelection(manager.CHEQTYERRADJSHT_FILLERID,manager.EMPLOYEE_NO,false);
        int[] ids=new int[]{
            manager.CHEQTYERRADJSHT_FILLERID, manager.EMPLOYEE_DEPARTMENTID, manager.DEPARTMENT_NAME
        };
        req.addSelection(ids,true);
        req.addSelection(manager.CHEQTYERRADJSHT_AUDITORID,manager.EMPLOYEE_NAME,true);
        req.addSelection(manager.CHEQTYERRADJSHT_AUDITORID,manager.EMPLOYEE_NO,false);
        ids=new int[]{
            manager.CHEQTYERRADJSHT_AUDITORID, manager.EMPLOYEE_DEPARTMENTID, manager.DEPARTMENT_NAME
        };
        req.addSelection(ids,true);
        ids=new int[]{
            manager.CHEQTYERRADJSHT_AUDITORID, manager.EMPLOYEE_DEPARTMENTID
//            manager.DEPARTMENT_COMPANYID, manager.COMPANY_NAME
        };
        req.addSelection(ids,true);
        ids=new int[]{
            manager.CHEQTYERRADJSHT_AUDITORID, manager.EMPLOYEE_DEPARTMENTID
   //         manager.DEPARTMENT_COMPANYID, manager.COMPANY_ADDRESS
        };
        req.addSelection(ids,false);


//        System.out.println(req.toSQL());
        QueryResult result=engine.doQuery(req);
        outputResult(result);

    }
    public void testQueryRequestBoundTest() throws Exception{
        QueryEngineExt engine=new QueryEngineExt("aaa");
        QueryRequestImpl req=new QueryRequestImpl();
        TableManager manager= TableManager.getInstance();

        req.setMainTable(manager.CHEQTYERRADJSHT);
        req.addParam(manager.CHEQTYERRADJSHT_NO,"2" );
        req.addSelection(manager.CHEQTYERRADJSHT_ID);
        req.addSelection(manager.CHEQTYERRADJSHT_FILLERID,manager.EMPLOYEE_NAME,false);
        req.addSelection(manager.CHEQTYERRADJSHT_FILLERID,manager.EMPLOYEE_NO,true);
        int[] ids=new int[]{
            manager.CHEQTYERRADJSHT_FILLERID, manager.EMPLOYEE_DEPARTMENTID, manager.DEPARTMENT_NAME
        };
        req.addSelection(ids,true);
        req.addSelection(manager.CHEQTYERRADJSHT_AUDITORID,manager.EMPLOYEE_NAME,true);
        req.addSelection(manager.CHEQTYERRADJSHT_AUDITORID,manager.EMPLOYEE_NO,false);
        ids=new int[]{
            manager.CHEQTYERRADJSHT_AUDITORID, manager.EMPLOYEE_DEPARTMENTID, manager.DEPARTMENT_NAME
        };
        req.addSelection(ids,true);
        ids=new int[]{
            manager.CHEQTYERRADJSHT_AUDITORID, manager.EMPLOYEE_DEPARTMENTID
            //manager.DEPARTMENT_COMPANYID, manager.COMPANY_NAME
        };
        req.addSelection(ids,true);
        ids=new int[]{
            manager.CHEQTYERRADJSHT_AUDITORID, manager.EMPLOYEE_DEPARTMENTID
            //manager.DEPARTMENT_COMPANYID, manager.COMPANY_ADDRESS
        };
        req.addSelection(ids,true);// this is the only difference compare to testQueryRequest2
        req.setRange(1,1);

//        System.out.println(req.toSQL());
        QueryResult result=engine.doQuery(req);
        outputResult(result);
    }*/
    private static String toString(int[] cs){
        String s="[";
        for(int i=0;i< cs.length;i++){
            if( i==0) s += ""+cs[i];
            else s +=","+cs[i];
        }
        return s+"]";
     }

    public void testDirectQueryRequestSQL() throws Exception{
    }
    private void outputResult(QueryResult result)throws QueryException{
        System.out.println("Metadata: "+ result.getMetaData());
        System.out.println("---------data---------");
        while( result.next()){
            String rs="";
            for(int i=0;i< result.getMetaData().getColumnCount();i++){
                String s= result.getString(i+1);
                int id= result.getObjectID(i+1);
                rs +=s+ " "+(id == -1? "":"("+id+")")+",";
            }
            System.out.println(rs);
        }

    }
}