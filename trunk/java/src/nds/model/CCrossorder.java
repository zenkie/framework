package nds.model;

import nds.model.base.BaseCCrossorder;

/**
 * This is the object class that relates to the C_CROSSORDER table.
 * Any customizations belong here.
 */
public class CCrossorder extends BaseCCrossorder {

/*[CONSTRUCTOR MARKER BEGIN]*/
	public CCrossorder () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public CCrossorder (java.lang.Integer _id) {
		super(_id);
	}

	/**
	 * Constructor for required fields
	 */
	public CCrossorder (
		java.lang.Integer _id,
		java.lang.String _docno,
		java.lang.String _docType,
		java.lang.String _targetdocType,
		java.lang.String _paymentRule,
		java.lang.String _invoiceRule,
		java.lang.String _deliveryRule,
		java.lang.String _freightCostRule,
		java.lang.String _deliveryviaRule,
		java.lang.String _priorityRule,
		java.lang.String _isTaxincluded,
		java.lang.String _sendemail,
		java.lang.String _vPaymentRule,
		java.lang.String _vInvoiceRule,
		java.lang.String _vDeliveryRule,
		java.lang.String _vFreightCostRule,
		java.lang.String _vDeliveryviaRule,
		java.lang.String _vPriorityRule,
		java.lang.String _vIstaxincluded,
		java.lang.String _vSendemail,
		java.lang.String _isActive) {

		super (
			_id,
			_docno,
			_docType,
			_targetdocType,
			_paymentRule,
			_invoiceRule,
			_deliveryRule,
			_freightCostRule,
			_deliveryviaRule,
			_priorityRule,
			_isTaxincluded,
			_sendemail,
			_vPaymentRule,
			_vInvoiceRule,
			_vDeliveryRule,
			_vFreightCostRule,
			_vDeliveryviaRule,
			_vPriorityRule,
			_vIstaxincluded,
			_vSendemail,
			_isActive);
	}

/*[CONSTRUCTOR MARKER END]*/
}