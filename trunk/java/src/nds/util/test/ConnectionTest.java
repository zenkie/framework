/******************************************************************
*
*$RCSfile: ConnectionTest.java,v $ $Revision: 1.1.1.1 $ $Author: Administrator $ $Date: 2005/03/15 11:23:27 $
*
*$Log: ConnectionTest.java,v $
*Revision 1.1.1.1  2005/03/15 11:23:27  Administrator
*init
*
*Revision 1.1.1.1  2002/11/29 02:50:35  yfzhu
*Active POS
*
*Revision 1.2  2001/11/07 20:58:48  yfzhu
*no message
*
*
********************************************************************/
package nds.util.test;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Title:        NDS Project
 * Description:  San gao shui yuan, mu xiang ren jia
 * Copyright:    Copyright (c) 2001
 * Company:      aic
 * @author yfzhu
 * @version 1.0
 */
/**
 * Test whether connection can execute statement concurrently or not.
 * Result:
 *   One connection  can only execute on statment once
 *
 */
public class ConnectionTest {
    public static void main(String[] args)throws Exception{
        ConnectionTest test=new ConnectionTest();
        test.testConcurrentConnection();
    }

    public ConnectionTest() {

    }
    private Connection getConnection() throws Exception{
        Class.forName("oracle.jdbc.driver.OracleDriver");
        return DriverManager.getConnection("jdbc:oracle:thin:@172.16.0.1:1521:test","aaa","abc123");
    }
    public void testConcurrentConnection()throws Exception{
        Connection con= getConnection();
        Call call=new Call("Dead procedure", con);
        Thread callThread= new Thread(call);
        callThread.start();
        Thread.sleep(1*1000);
        System.out.println("A new statement send out...");
        Statement stmt=con.createStatement();
        stmt.execute("select * from tabs");
        System.out.println("Finished.");
    }

    private class Call implements Runnable{
        private String info;
        private Connection con;
        public Call(String info,Connection con){
            this.info=info;
            this.con=con;
        }
        public void run(){
            try{
            System.out.println("[begin] "+info);
            // this is a dead proc
            /*
            Create or Replace Procedure AAA.sp_select(abcd varchar2) as
            begin
                WHILE 1= 1 LOOP
                dbms_output.put_line(abcd);
            END LOOP;
            end;
            */
            CallableStatement cs =con.prepareCall("{call sp_select(?)}");
            cs.setString(1,"afasf");
            ResultSet rs=cs.executeQuery();
            System.out.println("[end] "+info);
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }
}