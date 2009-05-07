package nds.model;

import nds.model.base.BaseCBpGroup;

/**
 * This is the object class that relates to the C_BP_GROUP table.
 * Any customizations belong here.
 */
public class CBpGroup extends BaseCBpGroup {

/*[CONSTRUCTOR MARKER BEGIN]*/
	public CBpGroup () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public CBpGroup (java.lang.Integer _id) {
		super(_id);
	}

	/**
	 * Constructor for required fields
	 */
	public CBpGroup (
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