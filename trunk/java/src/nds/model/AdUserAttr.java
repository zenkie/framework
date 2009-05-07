package nds.model;

import nds.model.base.BaseAdUserAttr;

/**
 * This is the object class that relates to the AD_USER_ATTR table.
 * Any customizations belong here.
 */
public class AdUserAttr extends BaseAdUserAttr {

/*[CONSTRUCTOR MARKER BEGIN]*/
	public AdUserAttr () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public AdUserAttr (java.lang.Integer _id) {
		super(_id);
	}

	/**
	 * Constructor for required fields
	 */
	public AdUserAttr (
		java.lang.Integer _id,
		java.lang.String _name,
		java.lang.String _value,
		java.lang.String _description,
		java.lang.String _isActive) {

		super (
			_id,
			_name,
			_value,
			_description,
			_isActive);
	}

/*[CONSTRUCTOR MARKER END]*/
}