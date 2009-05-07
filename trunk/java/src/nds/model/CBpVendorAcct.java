package nds.model;

import nds.model.base.BaseCBpVendorAcct;

/**
 * This is the object class that relates to the C_BP_VENDOR_ACCT table.
 * Any customizations belong here.
 */
public class CBpVendorAcct extends BaseCBpVendorAcct {

/*[CONSTRUCTOR MARKER BEGIN]*/
	public CBpVendorAcct () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public CBpVendorAcct (java.lang.Integer _id) {
		super(_id);
	}

	/**
	 * Constructor for required fields
	 */
	public CBpVendorAcct (
		java.lang.Integer _id,
		java.lang.String _isActive) {

		super (
			_id,
			_isActive);
	}

/*[CONSTRUCTOR MARKER END]*/
}