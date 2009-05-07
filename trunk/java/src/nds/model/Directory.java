package nds.model;

import nds.model.base.BaseDirectory;

/**
 * This is the object class that relates to the DIRECTORY table.
 * Any customizations belong here.
 */
public class Directory extends BaseDirectory {

/*[CONSTRUCTOR MARKER BEGIN]*/
	public Directory () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public Directory (java.lang.Integer _id) {
		super(_id);
	}

	/**
	 * Constructor for required fields
	 */
	public Directory (
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