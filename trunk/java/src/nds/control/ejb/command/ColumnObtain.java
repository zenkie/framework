package nds.control.ejb.command;
import java.rmi.RemoteException;
import java.sql.*;
import java.util.*;

import nds.control.ejb.DefaultWebEventHelper;
import nds.control.event.DefaultWebEvent;
import nds.log.Logger;
import nds.log.LoggerManager;
import nds.schema.Column;
import nds.schema.Table;
import nds.util.NDSException;
/**
 * Title:        NDS Project
 * Description:  San gao shui yuan, mu xiang ren jia
 * Copyright:    Copyright (c) 2001
 * Company:      aic
 * @author yfzhu
 * @version 1.0
 */

public abstract class ColumnObtain {
    protected Logger logger;
    DefaultWebEventHelper webHelper;
    protected boolean isBestEffort ;
    protected HashMap invalidRows;// elements Integer(value=key) (对于不符合条件的值，将行号（from 0)记录在invalidRows中)
    protected Connection conn;
    public ColumnObtain(){
        webHelper = new DefaultWebEventHelper();
        logger = LoggerManager.getInstance().getLogger(getClass().getName());

    }
    public void setConnection(Connection conn){
    	this.conn=conn;
    }
    public Connection getConnection(){
    	return conn;
    }
    /**
     * @param row 0 is the first row
     */
    protected boolean isInvalidRow(int row){
        return (invalidRows !=null)  && invalidRows.containsKey(new Integer(row));
    }
    /**
     * @param row 0 is the first row
     * @param msg why the row is invalid
     */
    protected void setRowInvalid(int row, String msg){
        if(invalidRows !=null) invalidRows.put(new Integer(row), msg);
    }
    public void enableBestEffort(boolean b){
        isBestEffort=b;
    }
    /**
     *  尽量获得该列的所有值，对于不符合条件的值，将行号（from 0)记录在invalidRows中，
     *  注意invalidRows 可能已经包含有一些不符合条件的行了。
     *
     */
    public void setInvalidRows(HashMap rows){
        invalidRows= rows;
    }
    /**
    * 返回某一列的值，这个值可能会有多个，所以Vector中包含数组, 在此方法中，有错就抛
    */
   public abstract Vector getColumnValue(DefaultWebEvent event,Table table,Column col,int length) throws NDSException,RemoteException;

}