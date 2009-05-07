package nds.model;

import nds.model.base.BaseMAttributeSetInstance;

/**
 * This is the object class that relates to the M_ATTRIBUTESETINSTANCE table.
 * Any customizations belong here.
 */
public class MAttributeSetInstance extends BaseMAttributeSetInstance {

/*[CONSTRUCTOR MARKER BEGIN]*/
	public MAttributeSetInstance () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public MAttributeSetInstance (java.lang.Integer _id) {
		super(_id);
	}

	/**
	 * Constructor for required fields
	 */
	public MAttributeSetInstance (
		java.lang.Integer _id,
		java.lang.String _serno,
		java.lang.String _isActive) {

		super (
			_id,
			_serno,
			_isActive);
	}

/*[CONSTRUCTOR MARKER END]*/
}