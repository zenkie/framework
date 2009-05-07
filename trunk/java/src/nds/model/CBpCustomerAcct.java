package nds.model;

import nds.model.base.BaseCBpCustomerAcct;

/**
 * This is the object class that relates to the C_BP_CUSTOMER_ACCT table.
 * Any customizations belong here.
 */
public class CBpCustomerAcct extends BaseCBpCustomerAcct {

/*[CONSTRUCTOR MARKER BEGIN]*/
	public CBpCustomerAcct () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public CBpCustomerAcct (java.lang.Integer _id) {
		super(_id);
	}

	/**
	 * Constructor for required fields
	 */
	public CBpCustomerAcct (
		java.lang.Integer _id,
		java.lang.String _isActive) {

		super (
			_id,
			_isActive);
	}

/*[CONSTRUCTOR MARKER END]*/
}