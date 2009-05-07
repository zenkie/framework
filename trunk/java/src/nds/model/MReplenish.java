package nds.model;

import nds.model.base.BaseMReplenish;

/**
 * This is the object class that relates to the M_REPLENISH table.
 * Any customizations belong here.
 */
public class MReplenish extends BaseMReplenish {

/*[CONSTRUCTOR MARKER BEGIN]*/
	public MReplenish () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public MReplenish (java.lang.Integer _id) {
		super(_id);
	}

	/**
	 * Constructor for required fields
	 */
	public MReplenish (
		java.lang.Integer _id,
		java.lang.String _replenishType,
		java.lang.String _isActive) {

		super (
			_id,
			_replenishType,
			_isActive);
	}

/*[CONSTRUCTOR MARKER END]*/
}