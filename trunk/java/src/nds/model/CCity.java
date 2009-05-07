package nds.model;

import nds.model.base.BaseCCity;

/**
 * This is the object class that relates to the C_CITY table.
 * Any customizations belong here.
 */
public class CCity extends BaseCCity {

/*[CONSTRUCTOR MARKER BEGIN]*/
	public CCity () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public CCity (java.lang.Integer _id) {
		super(_id);
	}

	/**
	 * Constructor for required fields
	 */
	public CCity (
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