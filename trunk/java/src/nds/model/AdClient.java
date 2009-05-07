package nds.model;

import nds.model.base.BaseAdClient;

/**
 * This is the object class that relates to the AD_CLIENT table.
 * Any customizations belong here.
 */
public class AdClient extends BaseAdClient {

/*[CONSTRUCTOR MARKER BEGIN]*/
	public AdClient () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public AdClient (java.lang.Integer _id) {
		super(_id);
	}

	/**
	 * Constructor for required fields
	 */
	public AdClient (
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