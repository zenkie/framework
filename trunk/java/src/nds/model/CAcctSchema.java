package nds.model;

import nds.model.base.BaseCAcctSchema;

/**
 * This is the object class that relates to the C_ACCTSCHEMA table.
 * Any customizations belong here.
 */
public class CAcctSchema extends BaseCAcctSchema {

/*[CONSTRUCTOR MARKER BEGIN]*/
	public CAcctSchema () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public CAcctSchema (java.lang.Integer _id) {
		super(_id);
	}

	/**
	 * Constructor for required fields
	 */
	public CAcctSchema (
		java.lang.Integer _id,
		nds.model.CCurrency _cCurrency,
		java.lang.String _name,
		java.lang.Integer _cCurrencyId,
		java.lang.String _isActive) {

		super (
			_id,
			_cCurrency,
			_name,
			_cCurrencyId,
			_isActive);
	}

/*[CONSTRUCTOR MARKER END]*/
}