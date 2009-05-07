package nds.model;

import nds.model.base.BaseMWarehouseAcct;

/**
 * This is the object class that relates to the M_WAREHOUSE_ACCT table.
 * Any customizations belong here.
 */
public class MWarehouseAcct extends BaseMWarehouseAcct {

/*[CONSTRUCTOR MARKER BEGIN]*/
	public MWarehouseAcct () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public MWarehouseAcct (java.lang.Integer _id) {
		super(_id);
	}

	/**
	 * Constructor for required fields
	 */
	public MWarehouseAcct (
		java.lang.Integer _id,
		java.lang.String _isActive) {

		super (
			_id,
			_isActive);
	}

/*[CONSTRUCTOR MARKER END]*/
}