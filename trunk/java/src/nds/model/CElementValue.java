package nds.model;

import nds.model.base.BaseCElementValue;

/**
 * This is the object class that relates to the C_ELEMENTVALUE table.
 * Any customizations belong here.
 */
public class CElementValue extends BaseCElementValue {

/*[CONSTRUCTOR MARKER BEGIN]*/
	public CElementValue () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public CElementValue (java.lang.Integer _id) {
		super(_id);
	}

	/**
	 * Constructor for required fields
	 */
	public CElementValue (
		java.lang.Integer _id,
		java.lang.String _name,
		java.lang.String _description,
		java.lang.String _accountType,
		java.lang.String _accountsign,
		java.lang.String _isSummary,
		java.lang.String _isBankAccount,
		java.lang.String _isActive) {

		super (
			_id,
			_name,
			_description,
			_accountType,
			_accountsign,
			_isSummary,
			_isBankAccount,
			_isActive);
	}

/*[CONSTRUCTOR MARKER END]*/
}