package nds.db.hsql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

public class Tools {
    private static Connection conn;

    public final static SimpleDateFormat dayFormatter=new SimpleDateFormat("yyMMdd");
    public final static DecimalFormat numFormatter=new DecimalFormat("#0000");

    private final static String GET_USER_PERMISSION_BY_DIR=
            "SELECT distinct permission FROM groupperm, groupuser "+
            "where directoryid = ? and groupuser.userId=? "+
            "and groupperm.groupid=groupuser.groupid";
    /**
     * This must be called before all functions
     */
    public static void init(Connection connection) {
        conn=connection;
    }
    public static void destroy(){
        try{
            conn.close();
        }catch(Exception e){

        }
        conn=null;
    }
    /**
     * Get filter column value from tmp_groupperm, used for Command GroupSetPermission
     */
    public static String sec_get_filter(int groupId, int dirId){
        String ret=null;
                ResultSet rs=null;
                Statement stmt=null;
                try{
                    stmt= conn.createStatement();
                    rs= stmt.executeQuery("select sqlfilter from tmp_groupperm  where groupid="+ groupId + " and directoryid="+dirId);
                    if( rs.next()){
                        ret = rs.getString(1);
                    }
                }catch(SQLException e){
                    throw new Error("Can not sec_get_filter:"+ e.getLocalizedMessage());
                }finally{
                    if( rs !=null) try{rs.close();}catch(Exception e){}
                    if( stmt !=null)try{stmt.close();}catch(Exception e){}
                }
        return ret;
    }
    /**
     * Get filterdesc column value from tmp_groupperm, used for Command GroupSetPermission
     */
    public static String sec_get_filterdesc(int groupId, int dirId){
        String ret=null;
                ResultSet rs=null;
                Statement stmt=null;
                try{
                    stmt= conn.createStatement();
                    rs= stmt.executeQuery("select filterdesc from tmp_groupperm  where groupid="+ groupId + " and directoryid="+dirId);
                    if( rs.next()){
                        ret = rs.getString(1);
                    }
                }catch(SQLException e){
                    throw new Error("Can not sec_get_filterdesc:"+ e.getLocalizedMessage());
                }finally{
                    if( rs !=null) try{rs.close();}catch(Exception e){}
                    if( stmt !=null)try{stmt.close();}catch(Exception e){}
                }
        return ret;
    }
    public static  int getEmployeeId(int userId){
        int ret=-1;
        ResultSet rs=null;
        Statement stmt=null;
        try{
            stmt= conn.createStatement();
            rs= stmt.executeQuery("select id from employee  where userid="+ userId);
            if( rs.next()){
                ret = rs.getInt(1);
            }
        }catch(SQLException e){
            throw new Error("Can not getEmployeeId:"+ e.getLocalizedMessage());
        }finally{
            if( rs !=null) try{rs.close();}catch(Exception e){}
            if( stmt !=null)try{stmt.close();}catch(Exception e){}
        }
        return ret;

    }

    public static  int getSheetStatus(String tableName, int id){
            int ret=-1;
            ResultSet rs=null;
            Statement stmt=null;
            try{
                stmt= conn.createStatement();
                rs= stmt.executeQuery("select status from "+ tableName+"  where id="+id);
                if( rs.next()){
                    ret = rs.getInt(1);
                }
            }catch(SQLException e){
                throw new Error("Can not getSheetStatus:"+ e.getLocalizedMessage());
            }finally{
                if( rs !=null) try{rs.close();}catch(Exception e){}
                if( stmt !=null)try{stmt.close();}catch(Exception e){}
            }
            return ret;

    }


    public static String getSheetNO( String tableName, int clientId){
        if(true){
        	throw new Error("Can not getSheetNO: Not supported yet since add clientId param");
        }
    	tableName= tableName.toUpperCase();
        String head="N/A";
        int no=0;
        java.util.Date d;
        ResultSet rs=null;
        Statement stmt=null;
        try{
            stmt= conn.createStatement();
            rs= stmt.executeQuery("select head,lastdate,no  from SheetNoSeq where upper(name)="+tableName);
            if( rs.next()){
                head = rs.getString(1);
                d= rs.getDate(2);
                no= rs.getInt(3);
                if ( dayFormatter.format(d).equals(dayFormatter.format(new java.util.Date()))){
                    // same day, increment no
                    no++;
                }else{
                    no=1;// initial for the first day
                    d= new java.util.Date();
                }
                // update
                stmt.executeUpdate("update SheetNoSeq set lastdate=CURDATE(), NO="+no+ " where upper(name)="+tableName);
            }
        }catch(SQLException e){
            throw new Error("Can not getAKNo:"+ e.getLocalizedMessage());
        }finally{
            if( rs !=null) try{rs.close();}catch(Exception e){}
            if( stmt !=null)try{stmt.close();}catch(Exception e){}
        }
        return head+dayFormatter.format(new java.util.Date())+numFormatter.format(no);

    }

    public static String getAKNo(int id, String tableName){
        String ret=null;
        ResultSet rs=null;
        Statement stmt=null;
        try{
            stmt= conn.createStatement();
            rs= stmt.executeQuery("select no from "+ tableName +" where id="+ id);
            if( rs.next()){
                ret = rs.getString(1);
            }
        }catch(SQLException e){
            throw new Error("Can not getAKNo:"+ e.getLocalizedMessage());
        }finally{
            if( rs !=null) try{rs.close();}catch(Exception e){}
            if( stmt !=null)try{stmt.close();}catch(Exception e){}
        }
        return ret;

    }
/*
  -- Get maximum permission of specified user on specified directory.
  -- User may belong to various groups, and each has special permission on the directory,
  -- according to table 'groupperm', and this funtion returns the 'or' union of all permissions
*/
    public static int getUserPermissionByDirId(int dirId, int userId){
        int ret=0;
        ResultSet rs=null;
        PreparedStatement stmt=null;
        try{
            stmt= conn.prepareStatement(GET_USER_PERMISSION_BY_DIR);
            stmt.setInt(1, dirId);
            stmt.setInt(2, userId);
            rs= stmt.executeQuery();
            while( rs.next()){
                int perm= rs.getInt(1);
                ret += perm - ret & perm;
            }
        }catch(SQLException e){
            throw new Error("Can not get user permission:"+ e.getLocalizedMessage());
        }finally{
            if( rs !=null) try{rs.close();}catch(Exception e){}
            if( stmt !=null)try{stmt.close();}catch(Exception e){}
        }
        return ret;

    }
    public static int getSequence(String tableName) throws RuntimeException {
        if ( conn==null) throw new Error("Connection not found");
        int id=-1;
        String seq= "SEQ_"+ tableName.toUpperCase();
        ResultSet rs=null;
        Statement stmt=null;
        try{
            stmt= conn.createStatement();
        }catch(Exception e){
            throw new Error("Could not create statement:"+ e.getLocalizedMessage(), e);
        }
        try{
            rs=stmt.executeQuery("select next value for "+ seq + " from dual");
            rs.next();
            id=rs.getInt(1);
            return id;
        }catch(SQLException e){
            if ( e.getErrorCode()==-191){
                // sequence not found, so create one
                try{
                    stmt.execute("CREATE SEQUENCE "+ seq + " AS INTEGER START WITH 1");
                }catch(Exception ee){
                    if( stmt !=null){try{ stmt.close();}catch(Exception eee){}}
                    throw new Error("Could not create sequence "+ seq +":"+ ee.getLocalizedMessage(), ee);
                }
                try{
                    rs=stmt.executeQuery("select next value for "+ seq + " from dual");
                    rs.next();
                    id=rs.getInt(1);
                    return id;
                }catch(SQLException e2){
                    throw new Error("Error when getting sequence from "+ seq +":"+ e2.getLocalizedMessage(), e2);
                }finally{
                    if( stmt !=null){try{ stmt.close();}catch(Exception e3){}}
                    if( rs !=null){try{ rs.close();}catch(Exception e3){}}
                }
            }
        }finally{
            if( stmt !=null){try{ stmt.close();}catch(Exception e){}}
            if( rs !=null){try{ rs.close();}catch(Exception e){}}
        }
        return id;
    }
}