package nds.model;

import nds.model.base.BaseAdReport;

/**
 * This is the object class that relates to the AD_REPORT table.
 * Any customizations belong here.
 */
public class AdReport extends BaseAdReport {

/*[CONSTRUCTOR MARKER BEGIN]*/
	public AdReport () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public AdReport (java.lang.Integer _id) {
		super(_id);
	}

	/**
	 * Constructor for required fields
	 */
	public AdReport (
		java.lang.Integer _id,
		java.lang.String _isMaster,
		java.lang.String _reportType,
		java.lang.String _allowFg,
		java.lang.String _isActive,
		java.lang.String _name) {

		super (
			_id,
			_isMaster,
			_reportType,
			_allowFg,
			_isActive,
			_name);
	}

/*[CONSTRUCTOR MARKER END]*/
}