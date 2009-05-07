package nds.model;

import nds.model.base.BaseSpoExportLog;

/**
 * This is the object class that relates to the SPO_EXPORT_LOG table.
 * Any customizations belong here.
 */
public class SpoExportLog extends BaseSpoExportLog {

/*[CONSTRUCTOR MARKER BEGIN]*/
	public SpoExportLog () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public SpoExportLog (java.lang.Integer _id) {
		super(_id);
	}

	/**
	 * Constructor for required fields
	 */
	public SpoExportLog (
		java.lang.Integer _id,
		nds.model.Users _user,
		java.lang.String _no,
		java.lang.Integer _userId) {

		super (
			_id,
			_user,
			_no,
			_userId);
	}

/*[CONSTRUCTOR MARKER END]*/
}