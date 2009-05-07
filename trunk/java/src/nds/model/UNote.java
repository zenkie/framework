package nds.model;

import nds.model.base.BaseUNote;

/**
 * This is the object class that relates to the U_NOTE table.
 * Any customizations belong here.
 */
public class UNote extends BaseUNote {

/*[CONSTRUCTOR MARKER BEGIN]*/
	public UNote () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public UNote (java.lang.Integer _id) {
		super(_id);
	}

	/**
	 * Constructor for required fields
	 */
	public UNote (
		java.lang.Integer _id,
		java.lang.String _no,
		java.lang.String _priorityRule,
		java.lang.String _title,
		java.lang.String _isActive) {

		super (
			_id,
			_no,
			_priorityRule,
			_title,
			_isActive);
	}
/*[CONSTRUCTOR MARKER END]*/
}