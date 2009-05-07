package nds.control.ejb.command;
import java.rmi.RemoteException;
import java.util.ArrayList;

import nds.control.ejb.Command;
import nds.control.event.DefaultWebEvent;
import nds.control.util.ValueHolder;
import nds.query.QueryEngine;
import nds.query.SPResult;
import nds.util.NDSException;




public class MergeCostUpShtStart extends Command{
    public ValueHolder execute(DefaultWebEvent event) throws NDSException, RemoteException {


        String year = (String)event.getParameterValue("year");
        String month = (String)event.getParameterValue("month");
        int monthInt = new Integer(month).intValue();
        if(monthInt<10){
           month = "0"+month;
        }
        String yearMonthStr = year.trim() +month.trim() ;
        // 获取这个月的年月值
        Integer yearMonth = new Integer(yearMonthStr);
        logger.debug("The value of yearmonth is:"+yearMonth) ;


        QueryEngine engine = QueryEngine.getInstance() ;
        ArrayList list = new ArrayList();
        list.add(yearMonth) ;
        list.add(helper.getOperator(event).getId());
        SPResult result = helper.executeStoredProcedure("MergeCostUpShtStart",list,true);

        ValueHolder v = new ValueHolder();
        if(result.isSuccessful()){
           v.put("message",result.getMessage() ) ;
        }else{
           v.put("message",result.getDebugMessage()  ) ;
        }
        return v;
    }
}