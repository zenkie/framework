package nds.model;

import nds.model.base.BaseAdSequence;

/**
 * This is the object class that relates to the AD_SEQUENCE table.
 * Any customizations belong here.
 */
public class AdSequence extends BaseAdSequence {

/*[CONSTRUCTOR MARKER BEGIN]*/
	public AdSequence () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public AdSequence (java.lang.Integer _id) {
		super(_id);
	}

	/**
	 * Constructor for required fields
	 */
	public AdSequence (
		java.lang.Integer _id,
		java.lang.String _name,
		java.lang.String _description,
		java.lang.String _vformat,
		java.lang.String _isDayCycle,
		java.lang.String _isActive) {

		super (
			_id,
			_name,
			_description,
			_vformat,
			_isDayCycle,
			_isActive);
	}
/*[CONSTRUCTOR MARKER END]*/
}