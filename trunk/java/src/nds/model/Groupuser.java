package nds.model;

import nds.model.base.BaseGroupuser;

/**
 * This is the object class that relates to the GROUPUSER table.
 * Any customizations belong here.
 */
public class Groupuser extends BaseGroupuser {

/*[CONSTRUCTOR MARKER BEGIN]*/
	public Groupuser () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public Groupuser (java.lang.Integer _id) {
		super(_id);
	}

	/**
	 * Constructor for required fields
	 */
	public Groupuser (
		java.lang.Integer _id,
		nds.model.Users _user,
		nds.model.Groups _group,
		java.lang.Integer _userId,
		java.lang.Integer _groupId) {

		super (
			_id,
			_user,
			_group,
			_userId,
			_groupId);
	}

/*[CONSTRUCTOR MARKER END]*/
}