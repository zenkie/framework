package nds.model;

import nds.model.base.BaseAdTableCategory;

/**
 * This is the object class that relates to the AD_TABLECATEGORY table.
 * Any customizations belong here.
 */
public class AdTableCategory extends BaseAdTableCategory {

/*[CONSTRUCTOR MARKER BEGIN]*/
	public AdTableCategory () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public AdTableCategory (java.lang.Integer _id) {
		super(_id);
	}

	/**
	 * Constructor for required fields
	 */
	public AdTableCategory (
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