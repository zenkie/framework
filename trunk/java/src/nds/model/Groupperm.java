package nds.model;

import nds.model.base.BaseGroupperm;

/**
 * This is the object class that relates to the GROUPPERM table.
 * Any customizations belong here.
 */
public class Groupperm extends BaseGroupperm {

/*[CONSTRUCTOR MARKER BEGIN]*/
	public Groupperm () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public Groupperm (java.lang.Integer _id) {
		super(_id);
	}

	/**
	 * Constructor for required fields
	 */
	public Groupperm (
		java.lang.Integer _id,
		nds.model.Groups _group,
		nds.model.Directory _directory,
		java.lang.Integer _groupId,
		java.lang.Integer _directoryId,
		java.lang.Integer _permission) {

		super (
			_id,
			_group,
			_directory,
			_groupId,
			_directoryId,
			_permission);
	}

/*[CONSTRUCTOR MARKER END]*/
}