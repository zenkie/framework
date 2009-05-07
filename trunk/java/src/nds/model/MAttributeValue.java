package nds.model;

import nds.model.base.BaseMAttributeValue;

/**
 * This is the object class that relates to the M_ATTRIBUTEVALUE table.
 * Any customizations belong here.
 */
public class MAttributeValue extends BaseMAttributeValue {

/*[CONSTRUCTOR MARKER BEGIN]*/
	public MAttributeValue () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public MAttributeValue (java.lang.Integer _id) {
		super(_id);
	}

	/**
	 * Constructor for required fields
	 */
	public MAttributeValue (
		java.lang.Integer _id,
		java.lang.String _isActive) {

		super (
			_id,
			_isActive);
	}

/*[CONSTRUCTOR MARKER END]*/
}