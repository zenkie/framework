package nds.model;

import nds.model.base.BaseCBpartner;

/**
 * This is the object class that relates to the C_BPARTNER table.
 * Any customizations belong here.
 */
public class CBpartner extends BaseCBpartner {

/*[CONSTRUCTOR MARKER BEGIN]*/
	public CBpartner () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public CBpartner (java.lang.Integer _id) {
		super(_id);
	}

	/**
	 * Constructor for required fields
	 */
	public CBpartner (
		java.lang.Integer _id,
		java.lang.String _name,
		java.lang.String _description,
		java.lang.String _isSummary,
		java.lang.String _isVendor,
		java.lang.String _isCustomer,
		java.lang.String _isEmployee,
		java.lang.String _isActive) {

		super (
			_id,
			_name,
			_description,
			_isSummary,
			_isVendor,
			_isCustomer,
			_isEmployee,
			_isActive);
	}

/*[CONSTRUCTOR MARKER END]*/
}