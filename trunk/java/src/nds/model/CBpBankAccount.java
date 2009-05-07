package nds.model;

import nds.model.base.BaseCBpBankAccount;

/**
 * This is the object class that relates to the C_BP_BANKACCOUNT table.
 * Any customizations belong here.
 */
public class CBpBankAccount extends BaseCBpBankAccount {

/*[CONSTRUCTOR MARKER BEGIN]*/
	public CBpBankAccount () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public CBpBankAccount (java.lang.Integer _id) {
		super(_id);
	}

	/**
	 * Constructor for required fields
	 */
	public CBpBankAccount (
		java.lang.Integer _id,
		java.lang.String _accountno,
		java.lang.String _isActive) {

		super (
			_id,
			_accountno,
			_isActive);
	}

/*[CONSTRUCTOR MARKER END]*/
}