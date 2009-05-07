package nds.model;

import nds.model.base.BaseMProductCategory;

/**
 * This is the object class that relates to the M_PRODUCT_CATEGORY table.
 * Any customizations belong here.
 */
public class MProductCategory extends BaseMProductCategory {

/*[CONSTRUCTOR MARKER BEGIN]*/
	public MProductCategory () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public MProductCategory (java.lang.Integer _id) {
		super(_id);
	}

	/**
	 * Constructor for required fields
	 */
	public MProductCategory (
		java.lang.Integer _id,
		java.lang.String _isDefault,
		java.lang.String _isActive) {

		super (
			_id,
			_isDefault,
			_isActive);
	}

/*[CONSTRUCTOR MARKER END]*/
}