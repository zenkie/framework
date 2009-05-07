package nds.model;

import nds.model.base.BaseCCurrency;

/**
 * This is the object class that relates to the C_CURRENCY table.
 * Any customizations belong here.
 */
public class CCurrency extends BaseCCurrency {

/*[CONSTRUCTOR MARKER BEGIN]*/
	public CCurrency () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public CCurrency (java.lang.Integer _id) {
		super(_id);
	}

	/**
	 * Constructor for required fields
	 */
	public CCurrency (
		java.lang.Integer _id,
		java.lang.String _isOCode,
		java.lang.String _description,
		java.lang.String _isActive) {

		super (
			_id,
			_isOCode,
			_description,
			_isActive);
	}

/*[CONSTRUCTOR MARKER END]*/
}