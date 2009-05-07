package nds.model;

import nds.model.base.BaseUGroupuser;

/**
 * This is the object class that relates to the U_GROUPUSER table.
 * Any customizations belong here.
 */
public class UGroupuser extends BaseUGroupuser {

/*[CONSTRUCTOR MARKER BEGIN]*/
	public UGroupuser () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public UGroupuser (java.lang.Integer _id) {
		super(_id);
	}

	/**
	 * Constructor for required fields
	 */
	public UGroupuser (
		java.lang.Integer _id,
		nds.model.Users _user,
		nds.model.UGroup _uGroup,
		java.lang.Integer _userId,
		java.lang.Integer _uGroupId,
		java.lang.String _isActive) {

		super (
			_id,
			_user,
			_uGroup,
			_userId,
			_uGroupId,
			_isActive);
	}

/*[CONSTRUCTOR MARKER END]*/
}