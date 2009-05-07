package nds.model;

import nds.model.base.BaseMWarehouse;

/**
 * This is the object class that relates to the M_WAREHOUSE table.
 * Any customizations belong here.
 */
public class MWarehouse extends BaseMWarehouse {

/*[CONSTRUCTOR MARKER BEGIN]*/
	public MWarehouse () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public MWarehouse (java.lang.Integer _id) {
		super(_id);
	}

	/**
	 * Constructor for required fields
	 */
	public MWarehouse (
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