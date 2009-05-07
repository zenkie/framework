package nds.model;

import nds.model.base.BaseCElement;

/**
 * This is the object class that relates to the C_ELEMENT table.
 * Any customizations belong here.
 */
public class CElement extends BaseCElement {

/*[CONSTRUCTOR MARKER BEGIN]*/
	public CElement () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public CElement (java.lang.Integer _id) {
		super(_id);
	}

	/**
	 * Constructor for required fields
	 */
	public CElement (
		java.lang.Integer _id,
		java.lang.String _name,
		java.lang.String _description,
		java.lang.String _elementType,
		java.lang.String _isActive) {

		super (
			_id,
			_name,
			_description,
			_elementType,
			_isActive);
	}

/*[CONSTRUCTOR MARKER END]*/
}