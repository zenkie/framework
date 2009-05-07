package nds.model;

import nds.model.base.BaseMInoutItem;

/**
 * This is the object class that relates to the M_INOUTITEM table.
 * Any customizations belong here.
 */
public class MInoutItem extends BaseMInoutItem {

/*[CONSTRUCTOR MARKER BEGIN]*/
	public MInoutItem () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public MInoutItem (java.lang.Integer _id) {
		super(_id);
	}

	/**
	 * Constructor for required fields
	 */
	public MInoutItem (
		java.lang.Integer _id,
		java.lang.String _isActive) {

		super (
			_id,
			_isActive);
	}

/*[CONSTRUCTOR MARKER END]*/
}