package nds.control.ejb.command;

import java.util.Vector;

import nds.control.ejb.Command;
import nds.control.event.DefaultWebEvent;
import nds.control.util.ValueHolder;
import nds.query.QueryEngine;
import nds.schema.TableManager;
import nds.util.NDSException;

public class SetNotifyParams extends Command {
    public SetNotifyParams() {
    }
    public ValueHolder execute(DefaultWebEvent event) throws java.rmi.RemoteException, nds.util.NDSException {
        QueryEngine engine= QueryEngine.getInstance();
        int userId= helper.getOperator(event).getId().intValue() ;
        String tableName= (String)event.getParameterValue("table");
        String email= (String)event.getParameterValue("email");
        if( email =="" ) throw new NDSException("必须设置邮箱地址！");
        TableManager manager= TableManager.getInstance();
        Vector sqls=new Vector(); String sql;
        sqls.addElement("update employee set email='"+ email+"' where userid="+ userId);
        sqls.addElement("delete from notifyparams where ownerid="+ userId+" and tablename='"+ tableName+"'");
        String[] params=event.getRawParameterValues("param");
        if( params !=null) for (int i=0;i< params.length;i++){
            String param=params[i];
            sql="insert into notifyparams(ownerid, tablename, sqlcondition,tableaction, robotaction,robotparam,remark) values("+
                userId+",'"+ tableName + "',";

            if(param.equals("OnMyObjectPermit")){
                sql += "'select count(*) from " + tableName + " where id=$objectid and ownerid="+
                       userId+"','permit','mail','"+ email+ "', 'OnMyObjectPermit'";
            }else if (param.equals("OnMyObjectRollback")){
                sql +="'select count(*) from " + tableName + " where id=$objectid and ownerid="+
                       userId+"','rollback','mail','"+ email+ "','OnMyObjectRollback'";
            }else if (param.equals("OnObjectAudit")){
                sql +="'select count(*) from " + tableName + " where id=$objectid',"+
                       "'audit','mail','"+ email+ "','OnObjectAudit'";
            }else if (param.equals("OnObjectPermit")){
                sql +="'select count(*) from " + tableName + " where id=$objectid',"+
                       "'permit','mail','"+ email+ "','OnObjectPermit'";
            }else{
                throw new NDSException("未知参数:" + param);
            }
            sql +=")";
            sqls.addElement(sql);
        }
        engine.doUpdate(sqls);
        ValueHolder holder=new ValueHolder();
        holder.put("message", "参数设置完成！");
        return holder;
    }
}