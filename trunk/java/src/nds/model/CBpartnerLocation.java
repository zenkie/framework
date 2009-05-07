package nds.model;

import nds.model.base.BaseCBpartnerLocation;

/**
 * This is the object class that relates to the C_BPARTNER_LOCATION table.
 * Any customizations belong here.
 */
public class CBpartnerLocation extends BaseCBpartnerLocation {

/*[CONSTRUCTOR MARKER BEGIN]*/
	public CBpartnerLocation () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public CBpartnerLocation (java.lang.Integer _id) {
		super(_id);
	}

	/**
	 * Constructor for required fields
	 */
	public CBpartnerLocation (
		java.lang.Integer _id,
		java.lang.String _name,
		java.lang.String _isBillto,
		java.lang.String _isShipto,
		java.lang.String _isPayfrom,
		java.lang.String _isRemitto,
		java.lang.String _isActive) {

		super (
			_id,
			_name,
			_isBillto,
			_isShipto,
			_isPayfrom,
			_isRemitto,
			_isActive);
	}

/*[CONSTRUCTOR MARKER END]*/
}