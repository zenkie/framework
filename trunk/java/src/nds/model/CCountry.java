package nds.model;

import nds.model.base.BaseCCountry;

/**
 * This is the object class that relates to the C_COUNTRY table.
 * Any customizations belong here.
 */
public class CCountry extends BaseCCountry {

/*[CONSTRUCTOR MARKER BEGIN]*/
	public CCountry () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public CCountry (java.lang.Integer _id) {
		super(_id);
	}

	/**
	 * Constructor for required fields
	 */
	public CCountry (
		java.lang.Integer _id,
		java.lang.String _name,
		java.lang.String _countrycode,
		java.lang.String _isActive) {

		super (
			_id,
			_name,
			_countrycode,
			_isActive);
	}

/*[CONSTRUCTOR MARKER END]*/
}