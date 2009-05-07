package nds.model;

import nds.model.base.BaseMLot;

/**
 * This is the object class that relates to the M_LOT table.
 * Any customizations belong here.
 */
public class MLot extends BaseMLot {

/*[CONSTRUCTOR MARKER BEGIN]*/
	public MLot () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public MLot (java.lang.Integer _id) {
		super(_id);
	}

	/**
	 * Constructor for required fields
	 */
	public MLot (
		java.lang.Integer _id,
		java.lang.String _isActive) {

		super (
			_id,
			_isActive);
	}

/*[CONSTRUCTOR MARKER END]*/
}