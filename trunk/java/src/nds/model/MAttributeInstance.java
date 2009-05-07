package nds.model;

import nds.model.base.BaseMAttributeInstance;

/**
 * This is the object class that relates to the M_ATTRIBUTEINSTANCE table.
 * Any customizations belong here.
 */
public class MAttributeInstance extends BaseMAttributeInstance {

/*[CONSTRUCTOR MARKER BEGIN]*/
	public MAttributeInstance () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public MAttributeInstance (java.lang.Integer _id) {
		super(_id);
	}

	/**
	 * Constructor for required fields
	 */
	public MAttributeInstance (
		java.lang.Integer _id,
		java.lang.String _value,
		java.lang.Integer _valueNumber,
		java.lang.String _isActive) {

		super (
			_id,
			_value,
			_valueNumber,
			_isActive);
	}

/*[CONSTRUCTOR MARKER END]*/
}