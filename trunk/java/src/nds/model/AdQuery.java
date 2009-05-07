package nds.model;

import nds.model.base.BaseAdQuery;

/**
 * This is the object class that relates to the AD_Query table.
 * Any customizations belong here.
 */
public class AdQuery extends BaseAdQuery {

/*[CONSTRUCTOR MARKER BEGIN]*/
	public AdQuery () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public AdQuery (java.lang.Integer _id) {
		super(_id);
	}

	/**
	 * Constructor for required fields
	 */
	public AdQuery (
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