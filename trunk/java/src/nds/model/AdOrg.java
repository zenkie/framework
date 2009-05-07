package nds.model;

import nds.model.base.BaseAdOrg;

/**
 * This is the object class that relates to the AD_ORG table.
 * Any customizations belong here.
 */
public class AdOrg extends BaseAdOrg {

/*[CONSTRUCTOR MARKER BEGIN]*/
	public AdOrg () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public AdOrg (java.lang.Integer _id) {
		super(_id);
	}

	/**
	 * Constructor for required fields
	 */
	public AdOrg (
		java.lang.Integer _id,
		java.lang.String _name,
		java.lang.String _description,
		java.lang.String _isActive,
		java.lang.String _isSummary) {

		super (
			_id,
			_name,
			_description,
			_isActive,
			_isSummary);
	}

/*[CONSTRUCTOR MARKER END]*/
}