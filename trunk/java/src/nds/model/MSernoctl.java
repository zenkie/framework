package nds.model;

import nds.model.base.BaseMSernoctl;

/**
 * This is the object class that relates to the M_SERNOCTL table.
 * Any customizations belong here.
 */
public class MSernoctl extends BaseMSernoctl {

/*[CONSTRUCTOR MARKER BEGIN]*/
	public MSernoctl () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public MSernoctl (java.lang.Integer _id) {
		super(_id);
	}

	/**
	 * Constructor for required fields
	 */
	public MSernoctl (
		java.lang.Integer _id,
		java.lang.String _isActive) {

		super (
			_id,
			_isActive);
	}

/*[CONSTRUCTOR MARKER END]*/
}