package nds.model;

import nds.model.base.BaseCRegion;

/**
 * This is the object class that relates to the C_REGION table.
 * Any customizations belong here.
 */
public class CRegion extends BaseCRegion {

/*[CONSTRUCTOR MARKER BEGIN]*/
	public CRegion () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public CRegion (java.lang.Integer _id) {
		super(_id);
	}

	/**
	 * Constructor for required fields
	 */
	public CRegion (
		java.lang.Integer _id,
		java.lang.String _name,
		java.lang.String _description,
		java.lang.String _isActive) {

		super (
			_id,
			_name,
			_description,
			_isActive);
	}

/*[CONSTRUCTOR MARKER END]*/
}