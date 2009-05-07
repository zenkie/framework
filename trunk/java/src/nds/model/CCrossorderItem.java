package nds.model;

import nds.model.base.BaseCCrossorderItem;

/**
 * This is the object class that relates to the C_CROSSORDERITEM table.
 * Any customizations belong here.
 */
public class CCrossorderItem extends BaseCCrossorderItem {

/*[CONSTRUCTOR MARKER BEGIN]*/
	public CCrossorderItem () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public CCrossorderItem (java.lang.Integer _id) {
		super(_id);
	}

	/**
	 * Constructor for required fields
	 */
	public CCrossorderItem (
		java.lang.Integer _id,
		java.lang.Integer _qty,
		java.lang.String _isActive) {

		super (
			_id,
			_qty,
			_isActive);
	}

/*[CONSTRUCTOR MARKER END]*/
}