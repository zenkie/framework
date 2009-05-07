package nds.model;

import nds.model.base.BaseMAttributeUse;

/**
 * This is the object class that relates to the M_ATTRIBUTEUSE table.
 * Any customizations belong here.
 */
public class MAttributeUse extends BaseMAttributeUse {

/*[CONSTRUCTOR MARKER BEGIN]*/
	public MAttributeUse () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public MAttributeUse (java.lang.Integer _id) {
		super(_id);
	}

	/**
	 * Constructor for required fields
	 */
	public MAttributeUse (
		java.lang.Integer _id,
		java.lang.String _isActive) {

		super (
			_id,
			_isActive);
	}

/*[CONSTRUCTOR MARKER END]*/
}