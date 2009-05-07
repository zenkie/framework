package nds.model;

import nds.model.base.BaseCBankAccount;

/**
 * This is the object class that relates to the C_BANKACCOUNT table.
 * Any customizations belong here.
 */
public class CBankAccount extends BaseCBankAccount {

/*[CONSTRUCTOR MARKER BEGIN]*/
	public CBankAccount () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public CBankAccount (java.lang.Integer _id) {
		super(_id);
	}

	/**
	 * Constructor for required fields
	 */
	public CBankAccount (
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