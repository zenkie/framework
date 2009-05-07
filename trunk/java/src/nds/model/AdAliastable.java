package nds.model;

import nds.model.base.BaseAdAliastable;

/**
 * This is the object class that relates to the AD_ALIASTABLE table.
 * Any customizations belong here.
 */
public class AdAliastable extends BaseAdAliastable {

/*[CONSTRUCTOR MARKER BEGIN]*/
	public AdAliastable () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public AdAliastable (java.lang.Integer _id) {
		super(_id);
	}

	/**
	 * Constructor for required fields
	 */
	public AdAliastable (
		java.lang.Integer _id,
		nds.model.AdTable _adTable,
		nds.model.AdTable _realtable,
		java.lang.Integer _adTableId,
		java.lang.Integer _realtableId,
		java.lang.String _condition,
		java.lang.String _isActive) {

		super (
			_id,
			_adTable,
			_realtable,
			_adTableId,
			_realtableId,
			_condition,
			_isActive);
	}
/*[CONSTRUCTOR MARKER END]*/
}