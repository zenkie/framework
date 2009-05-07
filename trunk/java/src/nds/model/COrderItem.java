package nds.model;

import nds.model.base.BaseCOrderItem;

/**
 * This is the object class that relates to the C_ORDERITEM table.
 * Any customizations belong here.
 */
public class COrderItem extends BaseCOrderItem {

/*[CONSTRUCTOR MARKER BEGIN]*/
	public COrderItem () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public COrderItem (java.lang.Integer _id) {
		super(_id);
	}

	/**
	 * Constructor for required fields
	 */
	public COrderItem (
		java.lang.Integer _id,
		java.lang.String _isActive) {

		super (
			_id,
			_isActive);
	}

/*[CONSTRUCTOR MARKER END]*/
}