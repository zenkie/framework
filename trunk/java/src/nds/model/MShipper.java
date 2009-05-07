package nds.model;

import nds.model.base.BaseMShipper;

/**
 * This is the object class that relates to the M_SHIPPER table.
 * Any customizations belong here.
 */
public class MShipper extends BaseMShipper {

/*[CONSTRUCTOR MARKER BEGIN]*/
	public MShipper () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public MShipper (java.lang.Integer _id) {
		super(_id);
	}

	/**
	 * Constructor for required fields
	 */
	public MShipper (
		java.lang.Integer _id,
		java.lang.String _name,
		java.lang.String _trackingurl,
		java.lang.String _isActive) {

		super (
			_id,
			_name,
			_trackingurl,
			_isActive);
	}

/*[CONSTRUCTOR MARKER END]*/
}