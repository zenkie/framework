package nds.model;

import nds.model.base.BaseAdRefbytable;

/**
 * This is the object class that relates to the AD_REFBYTABLE table.
 * Any customizations belong here.
 */
public class AdRefbytable extends BaseAdRefbytable {

/*[CONSTRUCTOR MARKER BEGIN]*/
	public AdRefbytable () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public AdRefbytable (java.lang.Integer _id) {
		super(_id);
	}

	/**
	 * Constructor for required fields
	 */
	public AdRefbytable (
		java.lang.Integer _id,
		nds.model.AdTable _adTable,
		nds.model.AdTable _adRefbyTable,
		nds.model.AdColumn _adRefbyColumn,
		java.lang.Integer _adTableId,
		java.lang.Integer _adRefbyTableId,
		java.lang.Integer _adRefbyColumnId,
		java.lang.String _assocType,
		java.lang.String _isActive) {

		super (
			_id,
			_adTable,
			_adRefbyTable,
			_adRefbyColumn,
			_adTableId,
			_adRefbyTableId,
			_adRefbyColumnId,
			_assocType,
			_isActive);
	}

/*[CONSTRUCTOR MARKER END]*/
}