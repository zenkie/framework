package nds.audit;

import java.sql.Connection;
import java.util.ArrayList;
import nds.query.QueryEngine;
import nds.query.SPResult;
import nds.util.NDSException;

public  class DBAuditProcedure implements Program {
	private String procName;

	public void init(String procedureName) {
		this.procName = procedureName;
	}

	public String execute(int phaseInstanceId)//, Connection conn)
			throws Exception {
		ArrayList params = new ArrayList();
		params.add(Integer.valueOf(phaseInstanceId));
		SPResult spr = QueryEngine.getInstance().executeStoredProcedure(
				this.procName, params, true);
		String ret = null;
		switch (spr.getCode()) {
		case 1:
			ret = "R";
			break;
		case 2:
			ret = "A";
			break;
		case 3:
			ret = "W";
			break;
		default:
			throw new NDSException("Not valid return code(=" + spr.getCode()
					+ ") from " + this.procName);
		}

		return ret;
	}


}