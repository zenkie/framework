package nds.model;

import nds.model.base.BaseCSalesRegion;

/**
 * This is the object class that relates to the C_SALESREGION table.
 * Any customizations belong here.
 */
public class CSalesRegion extends BaseCSalesRegion {

/*[CONSTRUCTOR MARKER BEGIN]*/
	public CSalesRegion () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public CSalesRegion (java.lang.Integer _id) {
		super(_id);
	}

	/**
	 * Constructor for required fields
	 */
	public CSalesRegion (
		java.lang.Integer _id,
		java.lang.String _name,
		java.lang.String _description,
		java.lang.String _isSummary,
		java.lang.String _isActive) {

		super (
			_id,
			_name,
			_description,
			_isSummary,
			_isActive);
	}

/*[CONSTRUCTOR MARKER END]*/
}