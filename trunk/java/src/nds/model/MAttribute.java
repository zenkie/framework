package nds.model;

import nds.model.base.BaseMAttribute;

/**
 * This is the object class that relates to the M_ATTRIBUTE table.
 * Any customizations belong here.
 */
public class MAttribute extends BaseMAttribute {

/*[CONSTRUCTOR MARKER BEGIN]*/
	public MAttribute () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public MAttribute (java.lang.Integer _id) {
		super(_id);
	}

	/**
	 * Constructor for required fields
	 */
	public MAttribute (
		java.lang.Integer _id,
		java.lang.String _name,
		java.lang.String _description,
		java.lang.String _attributeValueType,
		java.lang.String _isMandatory,
		java.lang.String _isInstanceAttribute,
		java.lang.String _isActive) {

		super (
			_id,
			_name,
			_description,
			_attributeValueType,
			_isMandatory,
			_isInstanceAttribute,
			_isActive);
	}

/*[CONSTRUCTOR MARKER END]*/
}