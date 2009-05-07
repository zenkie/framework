package nds.model;

import nds.model.base.BaseCBpEmployeeAcct;

/**
 * This is the object class that relates to the C_BP_EMPLOYEE_ACCT table.
 * Any customizations belong here.
 */
public class CBpEmployeeAcct extends BaseCBpEmployeeAcct {

/*[CONSTRUCTOR MARKER BEGIN]*/
	public CBpEmployeeAcct () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public CBpEmployeeAcct (java.lang.Integer _id) {
		super(_id);
	}

	/**
	 * Constructor for required fields
	 */
	public CBpEmployeeAcct (
		java.lang.Integer _id,
		java.lang.String _isActive) {

		super (
			_id,
			_isActive);
	}

/*[CONSTRUCTOR MARKER END]*/
}