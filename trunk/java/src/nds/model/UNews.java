package nds.model;

import nds.model.base.BaseUNews;

/**
 * This is the object class that relates to the U_NEWS table.
 * Any customizations belong here.
 */
public class UNews extends BaseUNews {

/*[CONSTRUCTOR MARKER BEGIN]*/
	public UNews () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public UNews (java.lang.Integer _id) {
		super(_id);
	}

	/**
	 * Constructor for required fields
	 */
	public UNews (
		java.lang.Integer _id,
		java.lang.String _no,
		java.lang.String _subject,
		java.lang.String _isPublic,
		java.lang.String _isActive) {

		super (
			_id,
			_no,
			_subject,
			_isPublic,
			_isActive);
	}
/*[CONSTRUCTOR MARKER END]*/
}