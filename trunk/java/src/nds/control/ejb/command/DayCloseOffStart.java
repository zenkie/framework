package nds.control.ejb.command;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Calendar;

import nds.control.ejb.Command;
import nds.control.event.DefaultWebEvent;
import nds.control.util.ValueHolder;
import nds.query.SPResult;
import nds.security.User;
import nds.util.NDSException;

/**
 * Title:        NDS Project
 * Description:  San gao shui yuan, mu xiang ren jia
 * Copyright:    Copyright (c) 2001
 * Company:      aic
 * @author Apollo, Mark
 * @version 1.0
 */

public class DayCloseOffStart extends Command {

    public ValueHolder execute(DefaultWebEvent event) throws NDSException, RemoteException {

        User commander = helper.getOperator(event);

        // 得到要进行日结的时间
        String year = (String)event.getParameterValue("year",true);
        String month = (String)event.getParameterValue("month",true);
        String day = (String)event.getParameterValue("day",true);

        Calendar calendar = Calendar.getInstance();
        calendar.set(
                Integer.parseInt(year),
                Integer.valueOf(month).intValue() - 1,
                Integer.parseInt(day)
            );
        java.sql.Date dayCloseDate = new java.sql.Date(calendar.getTime().getTime());
        String s_DayCloseDate = dayCloseDate.toString();

        ArrayList para = new ArrayList();
        para.add(s_DayCloseDate);
        SPResult res = helper.executeStoredProcedure("Check_DayCloseOff", para, true);
        if ( ! res.isSuccessful() ) {
            throw new NDSException(res.getDebugMessage());
        }

        ArrayList params = new ArrayList();
        params.add(s_DayCloseDate);
        res = helper.executeStoredProcedureBackground(
            "DayCloseOffStart", params, true, commander);
        ValueHolder v = new ValueHolder();
        if ( res.isSuccessful() ) {
            v.put("message", res.getMessage());
        } else {
            logger.debug(res.toString());
            throw new NDSException(res.getDebugMessage());
        }
        return v;
    }

    /**
     * 根据给定的时间获得该时间的年月
     */
    public Integer getYM(java.sql.Date date) {
        Calendar c = Calendar.getInstance() ;
        c.setTime(date);
        int year = c.get(c.YEAR );

        String yearStr = new Integer(year).toString() ;
        int month = c.get(c.MONTH );
        logger.debug("The year of year from getYM(java.sql.Date) is:"+month) ;
        String monthStr = null;
        if(month<10){
            monthStr = "0"+new Integer(month+1).toString() ;
        }else{
            monthStr = new Integer(month+1).toString() ;
        }
        String yearMonth = yearStr+monthStr;
        Integer ymInt = new Integer(yearMonth);
        return ymInt;
    }

    // 返回该日期的整数值
    public Integer getYMD(java.sql.Date date1) {
        Calendar c = Calendar.getInstance() ;
        c.setTime(date1);
        int year = c.get(c.YEAR );
        String yearStr = new Integer(year).toString() ;
        int month = c.get(c.MONTH );
        System.out.println("The value of month is:"+month) ;
        int date = c.get(c.DATE ) ;
        String dateStr =null;
        dateStr = date<10 ?"0"+new Integer(date).toString():new Integer(date).toString();

        String monthStr = month<9?"0"+new Integer(month+1).toString():new Integer(month+1).toString();
        String yearMD = yearStr+monthStr+dateStr;
        System.out.println("The return value is:"+yearMD) ;
        Integer vv = new Integer(yearMD);
        return vv;
    }

    // 返回上个月的月末日期
    public java.sql.Date  getLastYM(java.sql.Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date) ;
        Calendar c2 = Calendar.getInstance();
        c2.set(c.get(Calendar.YEAR),c.get(Calendar.MONTH ),1  ) ;
        c2.add(Calendar.DATE ,-1) ;
        return new java.sql.Date(c2.getTime().getTime() ) ;
    }

    private java.sql.Date getFirstDayOfMonth(java.sql.Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.set(c.get(Calendar.YEAR), c.get(Calendar.MONTH), 1);

        return new java.sql.Date(c.getTime().getTime());
    }

    private java.sql.Date getLastDayOfLastMonth(java.sql.Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.set(c.get(Calendar.YEAR), c.get(Calendar.MONTH), 1);
        c.add(Calendar.DATE, -1);

        return new java.sql.Date(c.getTime().getTime());
    }

}