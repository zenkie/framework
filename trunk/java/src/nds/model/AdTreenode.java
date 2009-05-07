package nds.model;

import nds.model.base.BaseAdTreenode;

/**
 * This is the object class that relates to the AD_TREENODE table.
 * Any customizations belong here.
 */
public class AdTreenode extends BaseAdTreenode {

/*[CONSTRUCTOR MARKER BEGIN]*/
	public AdTreenode () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public AdTreenode (java.lang.Integer _id) {
		super(_id);
	}

	/**
	 * Constructor for required fields
	 */
	public AdTreenode (
		java.lang.Integer _id,
		java.lang.String _isActive) {

		super (
			_id,
			_isActive);
	}

/*[CONSTRUCTOR MARKER END]*/
}