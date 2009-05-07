package nds.model;

import nds.model.base.BaseAdCube;

/**
 * This is the object class that relates to the AD_Cube table.
 * Any customizations belong here.
 */
public class AdCube extends BaseAdCube {

/*[CONSTRUCTOR MARKER BEGIN]*/
	public AdCube () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public AdCube (java.lang.Integer _id) {
		super(_id);
	}

	/**
	 * Constructor for required fields
	 */
	public AdCube (
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