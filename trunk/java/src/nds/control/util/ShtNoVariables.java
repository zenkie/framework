package nds.control.util;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Random;
import java.util.Vector;
import nds.control.event.DefaultWebEvent;
import nds.control.event.NDSEventException;
import nds.log.Logger;
import nds.log.LoggerManager;
import nds.query.ColumnLink;
import nds.query.Expression;
import nds.query.QueryEngine;
import nds.query.QueryException;
import nds.query.QueryRequestImpl;
import nds.query.QueryResult;
import nds.schema.Column;
import nds.schema.Table;
import nds.util.NDSException;
import nds.util.Tools;
import nds.util.Validator;

public class ShtNoVariables {
	private static final Logger logger = LoggerManager.getInstance().getLogger(
			ShtNoVariables.class.getName());
	private DefaultWebEvent event;
	private int index;
	private Connection conn;
	private Column col;
	private int clientId;

	public ShtNoVariables(Column col, DefaultWebEvent event, int index,
			Connection conn) {
		this.event = event;
		this.index = index;
		this.conn = conn;
		this.col = col;
		this.clientId = Tools.getInt(
				event.getStringWithNoVariables("$AD_CLIENT_ID$"), -1);
	}

	public String rand(int index) {
		long l = System.currentTimeMillis();
		this.index = index;
		return rand(
				(int) ((new Random(l)).nextDouble() * Math.pow(10.0D, index)),
				index);
	}

	public String rand(int rand_index, int index) {
		long l = rand_index
				+ (int) Math.round(Math.random() * (index - rand_index));
		StringBuffer rand_str = new StringBuffer();
		for (int v = 0; v < String.valueOf(index).length(); v++)
			rand_str.append("0");
		return new DecimalFormat(rand_str.toString()).format(l);
	}

	public String nextserial(int index) {
		return getSequenceNO("get_NextSerial", index);
	}

	public String nextval(int index) {
		return getSequenceNO("Get_NextVal", index);
	}

	private String getSequenceNO(String paramString, int index) {
		CallableStatement stmt = null;
		if (this.clientId == -1) {
			logger.warning("Found client id is -1");
			return "NO_AD_CLIENT_ID";
		}
		try {
			(stmt = this.conn.prepareCall("{? = call " + paramString + "(?,?)}"))
					.registerOutParameter(1, 2);
			stmt.setString(2, this.col.getSequenceHead());
			stmt.setInt(3, this.clientId);
			stmt.executeUpdate();
			return formatIntByLen(stmt.getInt(1), index);
		} catch (Throwable e) {
			logger.error("Fail to get sequence no with " + paramString
					+ ", for client i=" + this.clientId, e);
			return "ERR";
		} finally {
			try {
				stmt.close();
			} catch (Exception e) {
			}
		}
	}

	private static String formatIntByLen(int paramInt1, int paramInt2) {
		StringBuffer localStringBuffer = new StringBuffer();
		for (int i = 0; i < paramInt2; i++)
			localStringBuffer.append("0");
		return new DecimalFormat(localStringBuffer.toString())
				.format(paramInt1);
	}

	public String get(String paramString) throws Throwable {
		if (Validator.isNull(paramString))return "";
		Object localObject1 = null;
		String str=null;
		Vector v;
		try {

			localObject1 = this.col.getTable();
			Object localObject2;
			Column column;
			localObject2 = new ColumnLink(((Table) localObject1).getName()
					+ "." + paramString);
			
			column = new ColumnLink(((Table) localObject1).getName() + "."
					+ paramString).getColumns()[0];
			if ((!(column).equals(((Table) localObject1).getPrimaryKey()))
					&& (!column.isMaskSet(1))) {
				return paramString + "_ERR_ON_MASK";
			}
    
	 
			 HashMap valuesHashMap=(HashMap)this.event.getParameterValue("ColumnValueHashMap");
			 //logger.debug("valuesHashMap :"+valuesHashMap.toString());
			 logger.debug("columname :"+column.getName());
			 v=(Vector)valuesHashMap.get(column.getName());
			 
			 if(v==null || v.size()==0){
				logger.debug("Not find values of column " + column);
				return "";
			}
			
			Object[] d=(Object[]) v.elementAt(0);
			if (((ColumnLink) localObject2).length() == 1) {
				if (d[this.index] == null)
					return "";
				return d[this.index].toString();
			}
			int[] arrayOfInt = new int[(((ColumnLink) localObject2)
					.getColumnIDs()).length - 1];
			for (int i = 0; i < arrayOfInt.length; i++)
				arrayOfInt[i] = ((ColumnLink) localObject2).getColumnIDs()[(i + 1)];
			QueryRequestImpl query;
			(query = QueryEngine.getInstance().createRequest(
					this.event.getQuerySession())).setMainTable(column
					.getReferenceTable(true).getId());
			query.addSelection(arrayOfInt, false, null);
			localObject1 = new Expression(new ColumnLink(new int[] { column
					.getReferenceTable(true).getPrimaryKey().getId() }), "="
					+ d[this.index].toString(), null);

			query.addParam((Expression) localObject1);
			QueryResult rs = QueryEngine.getInstance()
					.doQuery(query, this.conn);

			if (rs.next())
				str = rs.getString(1);
			else
				str = "";
		} catch (QueryException e) {
			logger.error("fail to parse obj(" + paramString
					+ ") according to object idx=" + this.index + " in event:"
					+ this.event.toDetailString(), e);
			str = "ERR";
		}catch(Throwable e) {
			logger.error("fail to parse obj(" + paramString
					+ ") according to object idx=" + this.index, e);
			throw new NDSEventException("内部错误:请检查序号生成器的VTL模版,无法获取值");
		}
		return str;
	}

	public String replaceFlowNO(String FlowNO) {
		int i=FlowNO.indexOf("@serial(");
		if (i > -1) {
			String str = FlowNO.substring(0, i);
			int j = FlowNO.indexOf(")", i + 8);
			try {
				int k = Integer.parseInt(FlowNO.substring(i + 8, j));
				int pos = Tools.getInt(
						QueryEngine.getInstance().doQueryOne(
								"select max(substr("
										+ col.getName()
										+ ","
										+ (i + 1)
										+ ","
										+ k
										+ ")) from "
										+ col.getTable()
												.getRealTableName() + " "
										+ col.getTable().getName()
										+ " where ad_client_id=? and substr("
										+ col.getName() + ",1," + i
										+ ")=?",
								new Object[] { Integer.valueOf(this.clientId), str },
								this.conn), 0);

				pos++;

				if (pos > (k=(int) (Math.pow(10.0D, k) - 1.0D)))
					pos = 1;

				String randno = rand(pos, k);
				return str + randno + FlowNO.substring(j + 1);
			} catch (QueryException localQueryException) {
				logger.error("Fail to replace @serial in:" + FlowNO,
						localQueryException);
				return str + "ERR" + FlowNO.substring(j + 1);
			}
		}
		return FlowNO;
	}
}