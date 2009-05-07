package nds.model;

import nds.model.base.BaseCUomConversion;

/**
 * This is the object class that relates to the C_UOM_CONVERSION table.
 * Any customizations belong here.
 */
public class CUomConversion extends BaseCUomConversion {

/*[CONSTRUCTOR MARKER BEGIN]*/
	public CUomConversion () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public CUomConversion (java.lang.Integer _id) {
		super(_id);
	}

	/**
	 * Constructor for required fields
	 */
	public CUomConversion (
		java.lang.Integer _id,
		double _multiplyrate,
		java.lang.String _isActive) {

		super (
			_id,
			_multiplyrate,
			_isActive);
	}

/*[CONSTRUCTOR MARKER END]*/
}