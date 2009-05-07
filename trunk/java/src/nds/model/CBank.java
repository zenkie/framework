package nds.model;

import nds.model.base.BaseCBank;

/**
 * This is the object class that relates to the C_BANK table.
 * Any customizations belong here.
 */
public class CBank extends BaseCBank {

/*[CONSTRUCTOR MARKER BEGIN]*/
	public CBank () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public CBank (java.lang.Integer _id) {
		super(_id);
	}

	/**
	 * Constructor for required fields
	 */
	public CBank (
		java.lang.Integer _id,
		java.lang.String _name,
		java.lang.String _isActive) {

		super (
			_id,
			_name,
			_isActive);
	}

/*[CONSTRUCTOR MARKER END]*/
}