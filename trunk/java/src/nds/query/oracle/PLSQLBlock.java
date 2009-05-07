package nds.query.oracle;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Allow catching DBMS_OUTPUT output from oracle pl/sql call
 */
public class PLSQLBlock {
//    private static Logger logger= LoggerManager.getInstance().getLogger(PLSQLBlock.class.getName());

    private boolean isOutputEnabled= false;
    private int outputSize;
    StringBuffer output; // contains output data;
/*
    * our instance variables. It is always best to
    * use callable or prepared statements and prepare (parse)
    * them once per program execution, rather then one per
    * execution in the program.  The cost of reparsing is
    * very high.  Also -- make sure to use BIND VARIABLES!
    *
    * we use three statments in this class. One to enable
    * dbms_output - equivalent to SET SERVEROUTPUT on in SQL*PLUS.
    * another to disable it -- like SET SERVEROUTPUT OFF.
    * the last is to "dump" or display the results from dbms_output
    * using system.out
    *
 */

    public PLSQLBlock() {
    }
    /**
     * if this method not called, no output will be returned
     * @param outputSize, that maximum output chars we will get at most.
     */
    public void enableDBMS_OUTPUT(int outputSize){
        this.outputSize=outputSize;
        isOutputEnabled=true;
    }
    /*
    * show does most of the work.  It loops over
    * all of the dbms_output data, fetching it in this
    * case 32,000 bytes at a time (give or take 255 bytes).
    * It will print this output on stdout by default (just
    * reset what System.out is to change or redirect this
    * output).
    */
    private StringBuffer retrieveOuput(Connection conn) throws SQLException
    {
        StringBuffer sb=new StringBuffer();
        CallableStatement show_stmt = conn.prepareCall(
              "declare " +
              "    l_line varchar2(255); " +
              "    l_done number; " +
              "    l_buffer long; " +
              "begin " +
              "  loop " +
              "    exit when length(l_buffer)+255 > :maxbytes OR l_done = 1; " +
              "    dbms_output.get_line( l_line, l_done ); " +
              "    l_buffer := l_buffer || l_line || chr(10); " +
              "  end loop; " +
              " :done := l_done; " +
              " :buffer := l_buffer; " +
              "end;" );
        show_stmt.registerOutParameter( 2, java.sql.Types.INTEGER );
        show_stmt.registerOutParameter( 3, java.sql.Types.VARCHAR );
        int  done = 0;

        show_stmt.registerOutParameter( 2, java.sql.Types.INTEGER );
        show_stmt.registerOutParameter( 3, java.sql.Types.VARCHAR );

        for(;;)
        {
            show_stmt.setInt( 1, 32000 );
            show_stmt.executeUpdate();
            sb.append( show_stmt.getString(3) );
            if ( (done = show_stmt.getInt(2)) == 1 ) break;
        }
        return sb;
    }
    /**
     * @return true if ok
     */
    private void enableOutput(Connection conn) throws SQLException{
        CallableStatement enable_stmt  = null;
        try{
        enable_stmt=conn.prepareCall( "begin dbms_output.enable(:1); end;" );
        enable_stmt.setInt( 1, outputSize );
        enable_stmt.executeUpdate();
        }finally{
            if( enable_stmt!=null)try{ enable_stmt.close();}catch(Exception e){}
        }

    }
    /*
    * disable only has to execute the dbms_output.disable call
    */
    private void disableOutput(Connection conn)throws SQLException{
        CallableStatement disable_stmt =null;
        try{
            disable_stmt=conn.prepareCall( "begin dbms_output.disable; end;" );
            disable_stmt.executeUpdate();
            disable_stmt.close();
        }finally{
            if( disable_stmt!=null)try{ disable_stmt.close();}catch(Exception e){}
        }

    }

    /**
     * Note: since this methon execute many call to oracle sequencely, set
     * connections' automit to false is prefered.
     * "If using jta enabled pool, that's already done"
     */
    public void execute(Connection conn,String plsql) throws SQLException{
        CallableStatement stmt=null;
        try{
            if( isOutputEnabled) this.enableOutput(conn);
            stmt= conn.prepareCall(plsql);
            stmt.executeUpdate();
            output= this.retrieveOuput(conn);
            if( isOutputEnabled) this.disableOutput(conn);
        }finally{
            try{ if(stmt!=null) stmt.close();}catch(Exception e){}
        }

    }
    public String getOutput(){
        return output==null?"":output.toString();
    }
    public static void main(String[] args)  throws SQLException{
        PLSQLBlock PLSQLBlock1 = new PLSQLBlock();
        DriverManager.registerDriver
          (new oracle.jdbc.driver.OracleDriver());

        Connection conn = DriverManager.getConnection
             ("jdbc:oracle:thin:@localhost:1521:test",
              "nds5", "abc123");
        conn.setAutoCommit (false);
        PLSQLBlock1.enableDBMS_OUTPUT(1000000);
        PLSQLBlock1.execute(conn,"begin create table aa(id number(10));end;");
        System.out.println(PLSQLBlock1.getOutput());

    }
}