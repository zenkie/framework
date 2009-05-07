package nds.model;

import nds.model.base.BaseCGreeting;

/**
 * This is the object class that relates to the C_GREETING table.
 * Any customizations belong here.
 */
public class CGreeting extends BaseCGreeting {

/*[CONSTRUCTOR MARKER BEGIN]*/
	public CGreeting () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public CGreeting (java.lang.Integer _id) {
		super(_id);
	}

	/**
	 * Constructor for required fields
	 */
	public CGreeting (
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