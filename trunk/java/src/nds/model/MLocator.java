package nds.model;

import nds.model.base.BaseMLocator;

/**
 * This is the object class that relates to the M_LOCATOR table.
 * Any customizations belong here.
 */
public class MLocator extends BaseMLocator {

/*[CONSTRUCTOR MARKER BEGIN]*/
	public MLocator () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public MLocator (java.lang.Integer _id) {
		super(_id);
	}

	/**
	 * Constructor for required fields
	 */
	public MLocator (
		java.lang.Integer _id,
		java.lang.String _isActive) {

		super (
			_id,
			_isActive);
	}

/*[CONSTRUCTOR MARKER END]*/
}