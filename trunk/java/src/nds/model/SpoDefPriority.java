package nds.model;

import nds.model.base.BaseSpoDefPriority;

/**
 * This is the object class that relates to the SPO_DEF_PRIORITY table.
 * Any customizations belong here.
 */
public class SpoDefPriority extends BaseSpoDefPriority {

/*[CONSTRUCTOR MARKER BEGIN]*/
	public SpoDefPriority () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public SpoDefPriority (java.lang.Integer _id) {
		super(_id);
	}

	/**
	 * Constructor for required fields
	 */
	public SpoDefPriority (
		java.lang.Integer _id,
		nds.model.Users _user,
		java.lang.Integer _userId,
		java.lang.Integer _priority) {

		super (
			_id,
			_user,
			_userId,
			_priority);
	}

/*[CONSTRUCTOR MARKER END]*/
}