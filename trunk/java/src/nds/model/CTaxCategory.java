package nds.model;

import nds.model.base.BaseCTaxCategory;

/**
 * This is the object class that relates to the C_TAXCATEGORY table.
 * Any customizations belong here.
 */
public class CTaxCategory extends BaseCTaxCategory {

/*[CONSTRUCTOR MARKER BEGIN]*/
	public CTaxCategory () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public CTaxCategory (java.lang.Integer _id) {
		super(_id);
	}

	/**
	 * Constructor for required fields
	 */
	public CTaxCategory (
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