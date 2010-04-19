package nds.velocity;
import nds.control.event.*;

import nds.schema.*;
import nds.query.*;
import nds.util.*;

import java.sql.*;
import java.text.*;
import java.util.*;
/**
 * For column whose obtain manner is set as "sheetNo", and has ad_sequence.seqtype set as "vtl" 
 * the content of "format" column of "ad_sequence" table will be vtl then. SequenceController will
 * be the most frequently used object in vtl content
 * 
我们用Velocity语法来构造序号模板，主要用于与nds交互的类为nds.velocity.SequenceController，
在模板中通过seq来引用。关于SequenceController提供的方法见后文详述。

实现的过程是：首先使用当前记录的id作为编号内容参与当前记录的INSERT,UPDATE语句（称为伪编号）。
在记录保存完毕（AC/AM过程执行完毕）后，根据保存内容进行编号更新（称为实际编号），为独立的UPDATE语句。

在格式中，通过seq.val(ColumnLink) 的方式获取当前记录的指定的字段的值，ColumnLink必须是以当前表字段起始，
通过外键关联关系指向的其他关联表的字段，例如：M_DIM1_ID;CODE 表示获取当前表的M_DIM1_ID字段对应的
M_DIM1表上的CODE字段内容，首字段无需写当前表的名称。关联级数不限。

在格式中支持常用的函数，如seq.nextval表示按指定循环方式定义的数字，详细见下文描述。
这里有个特殊的定义：@serial(length) 用于表示前序相同的编号下的流水。截取@serial出现前的格式定义语句，
替换为实际内容作为分类内容，寻找当前表相同分类下的最大序列值。参数length，表示补0位数，
例如：@serial (4) 表示流水号4位，如 0012。
 
 * @author yfzhu
 *
 */
public class SequenceController {
	
	private static String GET_NETVAL="select id, currentnext,lastdate, 'YYYYMMDD'), cycletype from ad_sequence where name = ? and ad_client_id = ? for update";
	private static String UPDATE_NEXTVAL="update ad_sequence SET lastdate=?,currentnext=? where id=?";
	
	private DefaultWebEvent event;
	/**
	 * Main table that sequence will set on
	 */
	private Table mainTable ;
	/**
	 * Then object id of main table requestes for sequence value
	 */
	private int objectId; 
	/**
	 * Only in this connection, the newly created/modified record (specified by objectId and mainTable)
	 * will be found
	 */
	private Connection conn;
	/**
	 * Column that wants the sequence as value
	 */
	private Column column;
	public SequenceController(Column c, DefaultWebEvent de,Connection cn ){
		this.column=c;
		this.mainTable=c.getTable();
		event= de;
		conn=cn;
	}
	/**
	 * Get value of last column in column clink
	 * 
	 * @param clink, column link starts from main table of current object, in format like:
	 * 	"column1;column2;column3", column1 should not set table name, since it must start 
	 * from the main table of event object current working on
	 * @return data of last column in the link
	 * @throws Exception
	 */
	public String val(String clink) throws Exception{
		ColumnLink cl=new ColumnLink(mainTable.getName()+"."+ clink);
		QueryRequestImpl req=QueryEngine.getInstance().createRequest(event.getQuerySession());
		req.setMainTable(mainTable.getId());
		req.addSelection(cl.getColumnIDs(),false,null);
		req.addParam(mainTable.getPrimaryKey().getId(), "="+objectId);
		Object s=QueryEngine.getInstance().doQueryOne(req.toSQL(),conn);
		return s==null?"":s.toString();
	}
	/**
	 * Get next value specified in ad_sequence of current Column
	 * @param length 
	 * @throws Exception
	 */
	public String nextval(int length) throws Exception{
		// must lock current record of ad_sequence first
		PreparedStatement pstmt=null,pstmt2=null;
		ResultSet rs=null;
		int ad_sequence_id;
		int lastDate;
		String cycletype;
		int currentNext;
		int today=Integer.parseInt( ((SimpleDateFormat)QueryUtils.dateNumberFormatter.get()).format(new java.util.Date()));
		try{
			int clientId= Tools.getInt(event.getStringWithNoVariables("$AD_CLIENT_ID$"), -1);
			pstmt=conn.prepareStatement(GET_NETVAL);
			pstmt.setString(1, column.getSequenceHead());
			pstmt.setInt(2,clientId);
			rs=pstmt.executeQuery();
			if(rs.next()){
				ad_sequence_id= rs.getInt(1);
				currentNext = rs.getInt(2);
				lastDate= rs.getInt(3);
				cycletype=rs.getString(4);
				
				if("D".equals(cycletype)){
					if(today!=lastDate)currentNext=0;
				}else if("M".equals(cycletype)){
					if( today / 100 != lastDate/100)currentNext=0;
					
				}else if("Y".equals(cycletype)){
					if( today / 10000 != lastDate/10000)currentNext=0;
				}
				currentNext++;
				pstmt2= conn.prepareStatement(UPDATE_NEXTVAL);
				pstmt2.setInt(1, today);
				pstmt2.setInt(2, currentNext);
				pstmt2.setInt(3,ad_sequence_id);
				pstmt2.executeUpdate();		
				
				return (new DecimalFormat("0000000000000000000".substring(0,length))).format(currentNext);
				
			}else throw new NDSException("data not found for ad_sequence with name "+ column.getSequenceHead());
		}finally{
			if(pstmt!=null)try{ pstmt.close();}catch(Throwable t){};
			if(rs!=null)try{ rs.close();}catch(Throwable t){};
			if(pstmt2!=null)try{ pstmt2.close();}catch(Throwable t){};
		}
		
	}
	
	/**
	 * random int value as output
	 * @param length should be less then 10
	 * @return
	 * @throws Exception
	 */
	public String rand(int length) throws Exception{
		if(length>9) throw new NDSException("rand("+ length+") out of range: length should not greater than 9");
		Random g=new Random();
		return (new DecimalFormat("0000000000000000000".substring(0,length))).format(g.nextInt());		
	}
	/**
	 * random value as output
	 * @param length
	 * @param seed
	 * @return
	 * @throws Exception
	 */
	public String rand(int length, long seed) throws Exception{
		if(length>9) throw new NDSException("rand("+ length+") out of range: length should not greater than 9");
		Random g=new Random(seed);
		return (new DecimalFormat("0000000000000000000".substring(0,length))).format(g.nextInt());	
	}
	/**
	 * Working time that this method is requested
	 * @param format java date format 
	 * @return
	 * @throws Exception
	 */
	public String now(String format)throws Exception{
		return (new SimpleDateFormat(format)).format(new java.util.Date());
		
	}
	/**
	 * Find and replace @serial(length) tag in sequence no result string   
	 * @param value sequence no result string containing @serial(length)
	 * @return sequence no result string without @serial(length)
	 * @throws Exception
	 */
	public String replaceSerialTag(String value) throws Exception{
		return null;
	}
}
