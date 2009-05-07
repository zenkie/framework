package nds.model;

import nds.model.base.BaseCTax;

/**
 * This is the object class that relates to the C_TAX table.
 * Any customizations belong here.
 */
public class CTax extends BaseCTax {

/*[CONSTRUCTOR MARKER BEGIN]*/
	public CTax () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public CTax (java.lang.Integer _id) {
		super(_id);
	}

	/**
	 * Constructor for required fields
	 */
	public CTax (
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