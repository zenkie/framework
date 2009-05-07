package nds.model;

import nds.model.base.BaseCUom;

/**
 * This is the object class that relates to the C_UOM table.
 * Any customizations belong here.
 */
public class CUom extends BaseCUom {

/*[CONSTRUCTOR MARKER BEGIN]*/
	public CUom () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public CUom (java.lang.Integer _id) {
		super(_id);
	}

	/**
	 * Constructor for required fields
	 */
	public CUom (
		java.lang.Integer _id,
		java.lang.String _name,
		java.lang.String _description,
		java.lang.String _isActive) {

		super (
			_id,
			_name,
			_description,
			_isActive);
	}

/*[CONSTRUCTOR MARKER END]*/
}