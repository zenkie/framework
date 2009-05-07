package nds.model;

import nds.model.base.BaseAdSubSystem;

/**
 * This is the object class that relates to the AD_TABLECATEGORY table.
 * Any customizations belong here.
 */
public class AdSubSystem extends BaseAdSubSystem {

/*[CONSTRUCTOR MARKER BEGIN]*/
	public AdSubSystem () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public AdSubSystem (java.lang.Integer _id) {
		super(_id);
	}

	/**
	 * Constructor for required fields
	 */
	public AdSubSystem (
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