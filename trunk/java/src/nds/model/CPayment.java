package nds.model;

import nds.model.base.BaseCPayment;

/**
 * This is the object class that relates to the C_PAYMENT table.
 * Any customizations belong here.
 */
public class CPayment extends BaseCPayment {

/*[CONSTRUCTOR MARKER BEGIN]*/
	public CPayment () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public CPayment (java.lang.Integer _id) {
		super(_id);
	}

	/**
	 * Constructor for required fields
	 */
	public CPayment (
		java.lang.Integer _id,
		java.lang.String _docno,
		java.lang.String _docType,
		java.lang.String _targetdocType,
		java.lang.String _description,
		java.lang.String _isActive) {

		super (
			_id,
			_docno,
			_docType,
			_targetdocType,
			_description,
			_isActive);
	}

/*[CONSTRUCTOR MARKER END]*/
}