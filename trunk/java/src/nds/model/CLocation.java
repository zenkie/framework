package nds.model;

import nds.model.base.BaseCLocation;

/**
 * This is the object class that relates to the C_LOCATION table.
 * Any customizations belong here.
 */
public class CLocation extends BaseCLocation {

/*[CONSTRUCTOR MARKER BEGIN]*/
	public CLocation () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public CLocation (java.lang.Integer _id) {
		super(_id);
	}

	/**
	 * Constructor for required fields
	 */
	public CLocation (
		java.lang.Integer _id,
		java.lang.String _address1,
		java.lang.String _address2,
		java.lang.String _address3,
		java.lang.String _address4,
		java.lang.String _city,
		java.lang.String _regionName,
		java.lang.String _postal,
		java.lang.String _isActive) {

		super (
			_id,
			_address1,
			_address2,
			_address3,
			_address4,
			_city,
			_regionName,
			_postal,
			_isActive);
	}

/*[CONSTRUCTOR MARKER END]*/
}