package nds.model;

import nds.model.base.BaseCOrder;

/**
 * This is the object class that relates to the C_ORDER table.
 * Any customizations belong here.
 */
public class COrder extends BaseCOrder {

/*[CONSTRUCTOR MARKER BEGIN]*/
	public COrder () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public COrder (java.lang.Integer _id) {
		super(_id);
	}

	/**
	 * Constructor for required fields
	 */
	public COrder (
		java.lang.Integer _id,
		java.lang.String _docno,
		java.lang.String _docType,
		java.lang.String _targetdocType,
		java.lang.String _description,
		java.lang.String _paymentRule,
		java.lang.String _invoiceRule,
		java.lang.String _deliveryRule,
		java.lang.String _freightCostRule,
		java.lang.String _deliveryviaRule,
		java.lang.String _priorityRule,
		java.lang.String _isTaxincluded,
		java.lang.String _sendemail,
		java.lang.String _isActive) {

		super (
			_id,
			_docno,
			_docType,
			_targetdocType,
			_description,
			_paymentRule,
			_invoiceRule,
			_deliveryRule,
			_freightCostRule,
			_deliveryviaRule,
			_priorityRule,
			_isTaxincluded,
			_sendemail,
			_isActive);
	}

/*[CONSTRUCTOR MARKER END]*/
}