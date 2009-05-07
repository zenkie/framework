package nds.model;

import nds.model.base.BaseAdLimitValueGroup;

/**
 * This is the object class that relates to the AD_LIMITVALUE_GROUP table.
 * Any customizations belong here.
 */
public class AdLimitValueGroup extends BaseAdLimitValueGroup {

/*[CONSTRUCTOR MARKER BEGIN]*/
	public AdLimitValueGroup () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public AdLimitValueGroup (java.lang.Integer _id) {
		super(_id);
	}

	/**
	 * Constructor for required fields
	 */
	public AdLimitValueGroup (
		java.lang.Integer _id,
		java.lang.String _name,
		java.lang.String _isActive) {

		super (
			_id,
			_name,
			_isActive);
	}

/*[CONSTRUCTOR MARKER END]*/
}