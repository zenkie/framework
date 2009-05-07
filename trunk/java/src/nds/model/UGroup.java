package nds.model;

import nds.model.base.BaseUGroup;

/**
 * This is the object class that relates to the U_GROUP table.
 * Any customizations belong here.
 */
public class UGroup extends BaseUGroup {

/*[CONSTRUCTOR MARKER BEGIN]*/
	public UGroup () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public UGroup (java.lang.Integer _id) {
		super(_id);
	}

	/**
	 * Constructor for required fields
	 */
	public UGroup (
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