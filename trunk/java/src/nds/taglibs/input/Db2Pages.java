package nds.taglibs.input;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import nds.query.QueryEngine;
import nds.query.QueryException;
import nds.util.StringUtils;

/**
 * <p>Title: NDS Project</p>
 * <p>Description: San gao shui yuan, mu xiang ren jia</p>
 * <p>Copyright: Copyright (c) 2001</p>
 * <p>Company: aic</p>
 * @author yfzhu
 * @version 1.0
 */

public final class Db2Pages {

    public static ArrayList getValues(String usr,String ctrl) throws SQLException,QueryException{
        String SQL = "select convalue from inputdata where username = '"+ usr +"' and control = '"+ ctrl +"' order by moddate desc";
        ResultSet rs = QueryEngine.getInstance().doQuery(SQL);
        ArrayList al = new ArrayList();
        String tmp = null;
        while(rs.next()){
            tmp = rs.getString(1);
            tmp = StringUtils.replace(tmp,"\"","\\\"");
            al.add(StringUtils.replace(tmp,"'","\\'"));
        }
        return al;
    }
}