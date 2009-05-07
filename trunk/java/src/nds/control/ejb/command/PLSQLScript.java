package nds.control.ejb.command;
import java.rmi.RemoteException;
import java.sql.Connection;

import nds.control.ejb.Command;
import nds.control.event.DefaultWebEvent;
import nds.control.event.NDSEventException;
import nds.control.util.ValueHolder;
import nds.query.QueryEngine;
import nds.query.oracle.PLSQLBlock;
import nds.util.NDSException;

/**
 * Execute bean shell script, script result will be put in
 * ValueHolder's "result"
 */
public class PLSQLScript extends Command{

    /**
     *
     * @param event contains
     *           sql - pl/sql block
     * @return
     * @throws NDSException
     * @throws RemoteException
     */
    public ValueHolder execute(DefaultWebEvent event) throws NDSException ,RemoteException{
        Connection conn=null;
        try{
            conn= QueryEngine.getInstance().getConnection();
            ValueHolder vh= new ValueHolder();
            PLSQLBlock sqlblock= new PLSQLBlock();
            String sql=(String) event.getParameterValue("sql");
            sqlblock.enableDBMS_OUTPUT(1000000);
            sqlblock.execute(conn, sql);
            vh.put("message", sqlblock.getOutput());
            vh.put("code", "0");
            return vh;
        }catch(Exception e){
            throw new NDSEventException(e.getMessage(),e );
        }
    }

}