package nds.model;

import nds.model.base.BaseGroups;

/**
 * This is the object class that relates to the GROUPS table.
 * Any customizations belong here.
 */
public class Groups extends BaseGroups {

/*[CONSTRUCTOR MARKER BEGIN]*/
	public Groups () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public Groups (java.lang.Integer _id) {
		super(_id);
	}

	/**
	 * Constructor for required fields
	 */
	public Groups (
		java.lang.Integer _id,
		java.lang.String _name) {

		super (
			_id,
			_name);
	}
/*[CONSTRUCTOR MARKER END]*/
}