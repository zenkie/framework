package nds.model;

import nds.model.base.BaseCInvoiceItem;

/**
 * This is the object class that relates to the C_INVOICEITEM table.
 * Any customizations belong here.
 */
public class CInvoiceItem extends BaseCInvoiceItem {

/*[CONSTRUCTOR MARKER BEGIN]*/
	public CInvoiceItem () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public CInvoiceItem (java.lang.Integer _id) {
		super(_id);
	}

	/**
	 * Constructor for required fields
	 */
	public CInvoiceItem (
		java.lang.Integer _id,
		java.lang.String _isActive) {

		super (
			_id,
			_isActive);
	}

/*[CONSTRUCTOR MARKER END]*/
}