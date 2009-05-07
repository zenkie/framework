package nds.model;

import nds.model.base.BaseMStorage;

/**
 * This is the object class that relates to the M_STORAGE table.
 * Any customizations belong here.
 */
public class MStorage extends BaseMStorage {

/*[CONSTRUCTOR MARKER BEGIN]*/
	public MStorage () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public MStorage (java.lang.Integer _id) {
		super(_id);
	}

	/**
	 * Constructor for required fields
	 */
	public MStorage (
		java.lang.Integer _id,
		java.lang.String _isActive) {

		super (
			_id,
			_isActive);
	}

/*[CONSTRUCTOR MARKER END]*/
}