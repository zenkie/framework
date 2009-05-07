package nds.model;

import nds.model.base.BaseCPaymentItem;

/**
 * This is the object class that relates to the C_PAYMENTITEM table.
 * Any customizations belong here.
 */
public class CPaymentItem extends BaseCPaymentItem {

/*[CONSTRUCTOR MARKER BEGIN]*/
	public CPaymentItem () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public CPaymentItem (java.lang.Integer _id) {
		super(_id);
	}

	/**
	 * Constructor for required fields
	 */
	public CPaymentItem (
		java.lang.Integer _id,
		java.lang.String _isActive) {

		super (
			_id,
			_isActive);
	}

/*[CONSTRUCTOR MARKER END]*/
}