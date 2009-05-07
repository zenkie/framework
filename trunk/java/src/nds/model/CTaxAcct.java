package nds.model;

import nds.model.base.BaseCTaxAcct;

/**
 * This is the object class that relates to the C_TAX_ACCT table.
 * Any customizations belong here.
 */
public class CTaxAcct extends BaseCTaxAcct {

/*[CONSTRUCTOR MARKER BEGIN]*/
	public CTaxAcct () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public CTaxAcct (java.lang.Integer _id) {
		super(_id);
	}

	/**
	 * Constructor for required fields
	 */
	public CTaxAcct (
		java.lang.Integer _id,
		java.lang.String _isActive) {

		super (
			_id,
			_isActive);
	}

/*[CONSTRUCTOR MARKER END]*/
}