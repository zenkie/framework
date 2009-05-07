package nds.model;

import nds.model.base.BaseCCharge;

/**
 * This is the object class that relates to the C_CHARGE table.
 * Any customizations belong here.
 */
public class CCharge extends BaseCCharge {

/*[CONSTRUCTOR MARKER BEGIN]*/
	public CCharge () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public CCharge (java.lang.Integer _id) {
		super(_id);
	}

	/**
	 * Constructor for required fields
	 */
	public CCharge (
		java.lang.Integer _id,
		java.lang.String _name,
		java.lang.String _isActive) {

		super (
			_id,
			_name,
			_isActive);
	}

/*[CONSTRUCTOR MARKER END]*/
}