package nds.monitor;

import java.util.EventObject;
import nds.security.User;
import org.json.JSONObject;

public class ObjectActionEvent extends EventObject
{
	private int tableId;
	private int adClientId;
	private int objId;
	private int webActionId;
	private User operator;
	private ActionType actionType;
	private JSONObject context;

	public ObjectActionEvent(int tableid, int objid, int clientid, ActionType actype, User user, JSONObject jor)
			throws Exception
			{
		super(jor.get("source"));
		this.tableId = tableid;
		this.objId = objid;
		this.adClientId = clientid;
		this.actionType = actype;
		this.operator = user;
		this.context = jor;
			}

	public ObjectActionEvent(int tableid, int objid, int clientid, int actionid, User user, JSONObject jor)
			throws Exception
			{
		super(jor.get("source"));
		this.tableId = tableid;
		this.objId = objid;
		this.adClientId = clientid;
		this.webActionId = actionid;
		this.actionType = ActionType.WEBACTION;
		this.operator = user;
		this.context = jor;
			}

	public int getTableId()
	{
		return this.tableId;
	}
	public int getAdClientId() {
		return this.adClientId;
	}
	public int getObjId() {
		return this.objId;
	}
	public int getWebActionId() {
		return this.webActionId;
	}
	public User getOperator() {
		return this.operator;
	}
	public ActionType getActionType() {
		return this.actionType;
	}
	public JSONObject getContext() {
		return this.context;
	}

	public String toString() {
		StringBuffer param;
		param = new StringBuffer();
		param.append("table=").append(tableId)
		.append(",objectid=").append(objId)
		.append(",webactionid=").append(webActionId)
		.append(",action=").append(actionType.getType())
		.append(",user=").append(operator.getId());

		return param.toString();
	}

	public static enum ActionType{
		
		AC("ac",0), AM("am",1), BD("bd",2),
		BC("bc",3),BM("bm",4),SUBMIT("submit",5),UNSUBMIT("unsubmit",6),
		WEBACTION("action",7),TASK("task",8),AUDITING("auditing",9),REJECT("reject",10);
		
		public static final int MAX_ORDINAL = 10;
		private String actionType;
		private int ordinal;

		private ActionType(String type, int ordinal)
		{
			this.actionType = type;
			this.ordinal = ordinal;
		}
		public final int getOrdinal() {
			return ordinal;
		}
		public final String getType() {
			return actionType;
		}
		public static ActionType parse(String type) {
			if ("ac".equals(type)) return AC;
			else if ("am".equals(type)) return AM;
			else if ("bd".equals(type)) return BD;
			else if ("bc".equals(type)) return BC;
			else if ("bm".equals(type)) return BM;
			else if ("submit".equals(type)) return SUBMIT;
			else if ("unsubmit".equals(type)) return UNSUBMIT;
			else if ("action".equals(type)) return WEBACTION;
			else if ("task".equals(type)) return TASK;
			else if ("auditing".equals(type)) return AUDITING;
			else if ("reject".equals(type)) return REJECT;
			else
			throw new IllegalArgumentException("type " + type + " is not supported");
		}
	};

}