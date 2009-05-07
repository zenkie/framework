package nds.model;

import nds.model.base.BaseCValidCombination;

/**
 * This is the object class that relates to the C_VALIDCOMBINATION table.
 * Any customizations belong here.
 */
public class CValidCombination extends BaseCValidCombination {

/*[CONSTRUCTOR MARKER BEGIN]*/
	public CValidCombination () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public CValidCombination (java.lang.Integer _id) {
		super(_id);
	}

	/**
	 * Constructor for required fields
	 */
	public CValidCombination (
		java.lang.Integer _id,
		java.lang.String _combination,
		java.lang.String _description,
		java.lang.String _isActive) {

		super (
			_id,
			_combination,
			_description,
			_isActive);
	}

/*[CONSTRUCTOR MARKER END]*/
}