package nds.model;

import nds.model.base.BaseMProductBom;

/**
 * This is the object class that relates to the M_PRODUCT_BOM table.
 * Any customizations belong here.
 */
public class MProductBom extends BaseMProductBom {

/*[CONSTRUCTOR MARKER BEGIN]*/
	public MProductBom () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public MProductBom (java.lang.Integer _id) {
		super(_id);
	}

	/**
	 * Constructor for required fields
	 */
	public MProductBom (
		java.lang.Integer _id,
		java.lang.String _bomType,
		java.lang.String _isActive) {

		super (
			_id,
			_bomType,
			_isActive);
	}

/*[CONSTRUCTOR MARKER END]*/
}