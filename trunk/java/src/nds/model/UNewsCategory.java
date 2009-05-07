package nds.model;

import nds.model.base.BaseUNewsCategory;

/**
 * This is the object class that relates to the U_NEWSCATEGORY table.
 * Any customizations belong here.
 */
public class UNewsCategory extends BaseUNewsCategory {

/*[CONSTRUCTOR MARKER BEGIN]*/
	public UNewsCategory () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public UNewsCategory (java.lang.Integer _id) {
		super(_id);
	}

	/**
	 * Constructor for required fields
	 */
	public UNewsCategory (
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