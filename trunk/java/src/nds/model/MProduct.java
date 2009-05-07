package nds.model;

import nds.model.base.BaseMProduct;

/**
 * This is the object class that relates to the M_PRODUCT table.
 * Any customizations belong here.
 */
public class MProduct extends BaseMProduct {

/*[CONSTRUCTOR MARKER BEGIN]*/
	public MProduct () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public MProduct (java.lang.Integer _id) {
		super(_id);
	}

	/**
	 * Constructor for required fields
	 */
	public MProduct (
		java.lang.Integer _id,
		java.lang.String _name,
		java.lang.String _isSummary,
		java.lang.String _isStocked,
		java.lang.String _isPurchased,
		java.lang.String _isSold,
		java.lang.String _isActive) {

		super (
			_id,
			_name,
			_isSummary,
			_isStocked,
			_isPurchased,
			_isSold,
			_isActive);
	}

/*[CONSTRUCTOR MARKER END]*/
}