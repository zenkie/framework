package nds.control.ejb.command;
import java.rmi.RemoteException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Vector;

import nds.control.ejb.Command;
import nds.control.event.DefaultWebEvent;
import nds.control.event.NDSEventException;
import nds.control.util.ValueHolder;
import nds.query.QueryEngine;
import nds.util.NDSException;
import nds.util.StringUtils;
import nds.util.Tools;
/**
 * 单品折扣促销单的提交动作，完成以下内容：
 *    验证ProductSet 的合法性
 *    将PromotionASht 插入PosExpData 表，准备下发
 *
 */
public class PromotionAShtSubmit extends Command{
    /* DateFormatter for display in html, normally it's very easy to read
    */
   public final static DateFormat dateFormatter =new SimpleDateFormat("yyyy-MM-dd");

    public PromotionAShtSubmit() {

    }
    public ValueHolder execute(DefaultWebEvent event) throws NDSException ,RemoteException{

        Integer pid = new Integer(Tools.getInt(event.getParameterValue("id") ,-1));
        String tableName ="PromotionASht";
        QueryEngine engine2 = QueryEngine.getInstance() ;
        String no, remark,productSet,sqlDesc;
        int fillerId,shopgroupid,policySort, status=-1;
        java.sql.Date beginDate, endDate;
        double disValue;
        ResultSet rs=null;
        try{
        rs= engine2.doQuery("select NO,REMARK,FILLERID,BEGINDATE,ENDDATE,SHOPGROUPID,PRODUCTSET,SQLDESC,POLICYSORT,DISCOUNTVALUE,STATUS from promotionASht where id="+pid.intValue() ,false);
        if (rs.next()==true){
            no= rs.getString("NO");
            remark=rs.getString("REMARK");
            fillerId= rs.getInt("FILLERID");
            beginDate= rs.getDate("BEGINDATE");
            endDate=rs.getDate("ENDDATE");
            shopgroupid=rs.getInt("SHOPGROUPID");
            productSet=rs.getString("PRODUCTSET");
            sqlDesc=rs.getString("SQLDESC");
            policySort= rs.getInt("POLICYSORT");
            disValue=rs.getDouble("DISCOUNTVALUE");
            status= rs.getInt("STATUS");
        }else
            throw new NDSException("指定的单品折扣促销单未找到，请刷新页面重试！");
        if(status!=1){
            throw new NDSEventException("该请求已经被提交过了！" );
        }
        // check validity
        checkProductSet(productSet);
        String sql=null, props="";
        if (productSet != null){
            productSet=StringUtils.removeChar(productSet,'\t');
            props="PRODUCTSET="+ productSet +
                     '\t'+"POLICYSORT="+policySort+'\t'+"DISCOUNTVALUE="+ disValue;
        }
            // create insert sql to expdata
         sql="insert into promotionsht (id,no,promotionSort,remark,beginDate,endDate,props,ishandled) values("+
                   pid+",'"+no+"',1,'"+remark+"','"+dateFormatter.format(beginDate)+"','"+dateFormatter.format(endDate)+
                   "','"+props+"',1)";
//        logger.debug();
        Vector v= new Vector();
        v.addElement(nds.control.ejb.command.pub.Pub.getExpDataRecord(shopgroupid,sql));

        /* yfzhu 2003-07-20 add support for single product changment in item table */
        sql= "insert into posexpdata (id, shopID, shopgroupid, sqlText) select  seq_expdata.nextval, null," +
        	shopgroupid + ",'insert into promotionshtitem(id, productid, promotionprice, promotionshtid) values ('|| id || ',' || productid || ',' || promotionprice || ',' || promotionAshtID || ');'" +
        	" from promotionAshtItem where promotionAshtID=" + pid;
        logger.debug("sql for PromotionAShtItem: " + sql);
        v.addElement(sql);

        v.addElement("update promotionASht set status=2 where id="+ pid);
        engine2.doUpdate(v);
        }catch(SQLException e){
            throw new NDSEventException(e.getMessage(), e);
        }finally{
            if( rs !=null)try{rs.close();}catch(Exception e3){}
        }
        ValueHolder v = new ValueHolder();
        v.put("message","单品折扣优惠单"+no+"提交成功" ) ;
        return v;
    }
    /**
     *验证ProductSet 的合法性, 如果非法，将抛出错误
     * ProductSet 字段的值形式为：{columnName:=value}...,
     * @see nds.query.QuerySQLHandler#SQLBuilder
     */
    private void checkProductSet(String data)throws NDSEventException{

    }
}