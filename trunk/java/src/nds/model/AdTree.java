package nds.model;

import nds.model.base.BaseAdTree;

/**
 * This is the object class that relates to the AD_TREE table.
 * Any customizations belong here.
 */
public class AdTree extends BaseAdTree {

/*[CONSTRUCTOR MARKER BEGIN]*/
	public AdTree () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public AdTree (java.lang.Integer _id) {
		super(_id);
	}

	/**
	 * Constructor for required fields
	 */
	public AdTree (
		java.lang.Integer _id,
		java.lang.String _name,
		java.lang.String _description,
		java.lang.String _treeType,
		java.lang.String _isActive) {

		super (
			_id,
			_name,
			_description,
			_treeType,
			_isActive);
	}

/*[CONSTRUCTOR MARKER END]*/
}