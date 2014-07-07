package nds.model;

import nds.model.base.BaseAdSysModel;

public class AdSysModel  extends BaseAdSysModel{
	
	
	/*[CONSTRUCTOR MARKER BEGIN]*/
	public AdSysModel () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public AdSysModel (java.lang.Integer _id) {
		super(_id);
	}

	/**
	 * Constructor for required fields
	 */
	public AdSysModel (
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
