package nds.model;

import nds.model.base.BaseMLotctl;

/**
 * This is the object class that relates to the M_LOTCTL table.
 * Any customizations belong here.
 */
public class MLotctl extends BaseMLotctl {

/*[CONSTRUCTOR MARKER BEGIN]*/
	public MLotctl () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public MLotctl (java.lang.Integer _id) {
		super(_id);
	}

	/**
	 * Constructor for required fields
	 */
	public MLotctl (
		java.lang.Integer _id,
		java.lang.String _isActive) {

		super (
			_id,
			_isActive);
	}

/*[CONSTRUCTOR MARKER END]*/
}