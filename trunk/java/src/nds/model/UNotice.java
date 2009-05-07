package nds.model;

import nds.model.base.BaseUNotice;

/**
 * This is the object class that relates to the U_NOTICE table.
 * Any customizations belong here.
 */
public class UNotice extends BaseUNotice {

/*[CONSTRUCTOR MARKER BEGIN]*/
	public UNotice () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public UNotice (java.lang.Integer _id) {
		super(_id);
	}

	/**
	 * Constructor for required fields
	 */
	public UNotice (
		java.lang.Integer _id,
		java.lang.String _no,
		java.lang.String _priorityRule,
		java.lang.String _title,
		java.lang.String _isEvent,
		java.lang.String _remindby,
		java.lang.String _isActive) {

		super (
			_id,
			_no,
			_priorityRule,
			_title,
			_isEvent,
			_remindby,
			_isActive);
	}

/*[CONSTRUCTOR MARKER END]*/
}