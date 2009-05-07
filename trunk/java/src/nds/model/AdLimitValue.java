package nds.model;

import nds.model.base.BaseAdLimitValue;

/**
 * This is the object class that relates to the AD_LIMITVALUE table.
 * Any customizations belong here.
 */
public class AdLimitValue extends BaseAdLimitValue {

/*[CONSTRUCTOR MARKER BEGIN]*/
	public AdLimitValue () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public AdLimitValue (java.lang.Integer _id) {
		super(_id);
	}

	/**
	 * Constructor for required fields
	 */
	public AdLimitValue (
		java.lang.Integer _id,
		nds.model.AdLimitValueGroup _adLimitValueGroup,
		java.lang.Integer _adLimitValueGroupId,
		java.lang.String _value,
		java.lang.String _description,
		java.lang.String _isActive) {

		super (
			_id,
			_adLimitValueGroup,
			_adLimitValueGroupId,
			_value,
			_description,
			_isActive);
	}

/*[CONSTRUCTOR MARKER END]*/
}