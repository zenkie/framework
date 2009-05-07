/******************************************************************
*
*$RCSfile: DataGenerator.java,v $ $Revision: 1.1.1.1 $ $Author: Administrator $ $Date: 2005/03/15 11:23:23 $
*
*$Log: DataGenerator.java,v $
*Revision 1.1.1.1  2005/03/15 11:23:23  Administrator
*init
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

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

import nds.schema.Column;
import nds.schema.SQLTypes;
import nds.schema.Table;
import nds.schema.TableManager;

public class DataGenerator {

    private DbConnectionDefaultPool pool;
    private TableManager manager;

    public DataGenerator(int rowsPerTable)throws SQLException{
        pool= DbConnectionDefaultPool.getInstance();
        manager=TableManager.getInstance();
        tables=new Hashtable();
        Iterator it= manager.getAllTables().iterator();
        while(it.hasNext()){
            Table table=(Table) it.next();
            tables.put(table,new Integer(20));
        }
        createData(rowsPerTable);
    }
    protected Connection getConnection() throws SQLException {
        Connection dbConnection = pool.getConnection();
        return dbConnection;
    }
    private void createData(int rowsPerTable)throws SQLException{
        Connection con=getConnection();
        Iterator it= manager.getAllTables().iterator();
        while(it.hasNext()){
            Table table=(Table) it.next();
            long start= System.currentTimeMillis();
            String tableName=table.getName();
            createTableData(con, table, rowsPerTable);
            System.out.println("Table:"+ table+ ", time="+ ((System.currentTimeMillis() -start)/1000));
        }
    }
    /**
     * @param count how many rows to be inserted
     */
    private void createTableData(Connection con,Table table, int count){
            ArrayList cols=table.getAllColumns();
            String allColumns=null;
            String questionMarks=null;
            for( int i=0;i< cols.size();i++){
                Column col= (Column) cols.get(i);
                if(allColumns==null) {
                    allColumns = col.getName();
                    questionMarks="? ";
                }else {
                    allColumns +=","+col.getName();
                    questionMarks +=",? ";
                }

            }
            try{

            String sql="insert into "+ table.getRealTableName()+"(" +allColumns+") values ("+ questionMarks+")";
//            System.out.println(sql);
//            PreparedStatement pstmt= con.prepareStatement(sql);
            Statement pstmt2= con.createStatement();
            pstmt2.executeUpdate("delete from "+ table.getName());
            for( int i=0;i< count;i++){
                String sql2="insert into "+ table.getRealTableName()+"(" +allColumns+") values (";
                for( int j=0;j< cols.size();j++){
                    if( ((Column)cols.get(j)).equals( table.getPrimaryKey())){
                        //pstmt.setInt(j+1, nextID(table));
                        sql2 += nextID(table)+",";
                    }else{
                        //setStatmentValue(pstmt,j+1,(Column)cols.get(j));
                        sql2 += getStatmentValue((Column)cols.get(j))+",";
                    }
                }
                try{
                    //pstmt.executeUpdate();
                     sql2 =sql2.substring(0,sql2.length()-1)+")";
//                     System.out.println(sql2);
                    pstmt2.execute(sql2);
                }catch(Exception e){
                    System.out.println(sql2);
                     System.out.println(e);
                }
            }
            }catch(Exception ee){
                ee.printStackTrace();
            }
    }
    /**
     * set statement value at pos
     */
    private void setStatmentValue(PreparedStatement pstmt, int pos, Column col) throws Exception{
        switch( col.getSQLType()){
            case SQLTypes.BIGINT:
            case SQLTypes.DECIMAL:
                pstmt.setBigDecimal(pos, randomDecimal());
                break;
            case SQLTypes.INT:
            case SQLTypes.SMALLINT:
            case SQLTypes.TINYINT:
                pstmt.setInt(pos, randomInt(col.getLength()));
                break;
            case SQLTypes.FLOAT:
                pstmt.setFloat(pos, randomInt(6));
                break;
            case SQLTypes.DOUBLE:
                pstmt.setDouble(pos, randomInt(6));
                break;
            case SQLTypes.REAL:
                pstmt.setDouble(pos, randomInt(6));
                break;
            case SQLTypes.TIME:
            case SQLTypes.TIMESTAMP:
            case SQLTypes.DATE:
                pstmt.setDate(pos, randomDate());
                break;
            case SQLTypes.VARCHAR:
            case SQLTypes.LONGVARCHAR:
            case SQLTypes.CHAR:
                pstmt.setString(pos, randomString(col.getLength()));
                break;
            default:
                pstmt.setObject(pos, randomString(col.getLength()));
        }
    }
    private BigDecimal randomDecimal(){
        return BigDecimal.valueOf((long)randomInt(10));
    }
    private java.sql.Date randomDate(){
        return new java.sql.Date( RandomGen.getDate(new java.util.Date(),100).getTime());
    }
    private int randomInt(int size){
        return RandomGen.getInt(0, 1000);//(int)(Math.pow(10,size)-1));
    }
    private String randomString(int size){
        String s= RandomGen.getRandomString(size);
        if( s.equals("")) s="a";
        return s;
    }
    /**
     * Primary key
     */
    private int nextID(Table table){
        Integer i=(Integer) tables.get(table);
        int v=i.intValue();
        tables.put(table, new Integer(v+1));
        return v;
    }
     private String getStatmentValue(Column col) throws Exception{
        String s=null;
        switch( col.getSQLType()){
            case SQLTypes.BIGINT:
            case SQLTypes.DECIMAL:
                if( col.isValueLimited()){
                    s=RandomGen.getInt(0, 9)+"";
                }else{
                   s= randomDecimal()+"";
                }
                break;
            case SQLTypes.INT:
            case SQLTypes.SMALLINT:
            case SQLTypes.TINYINT:
                if( col.isValueLimited()){
                    s=RandomGen.getInt(0, 9)+"";
                }else{
                    s=randomInt(col.getLength())+"";
                }
                break;
            case SQLTypes.FLOAT:
                s=randomInt(6)+"";
                break;
            case SQLTypes.DOUBLE:
                 s=randomInt(6)+"";
                break;
            case SQLTypes.REAL:
                 s=randomInt(6)+"";
                break;
            case SQLTypes.TIME:
            case SQLTypes.TIMESTAMP:
            case SQLTypes.DATE:
                 s="to_date('"+dateFormatter.format(randomDate())+"', 'yyyy-MM-dd')";
                break;
            case SQLTypes.VARCHAR:
            case SQLTypes.LONGVARCHAR:
            case SQLTypes.CHAR:
                s="'"+randomString(col.getLength())+"'";
                break;
            default:
                throw new Exception("Error , unknow type "+col.getSQLType()+"  for Column "+ col );
        }
        return s;
    }

    private Hashtable tables;//key table value:Integer(pk id)
     public final static DateFormat dateFormatter
		= new SimpleDateFormat("yyyy'-'MM'-'dd");
    public static void main(String[] args) throws SQLException{
        DataGenerator dataGenerator1 = new DataGenerator(100);
    }

}