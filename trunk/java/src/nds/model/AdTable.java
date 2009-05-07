package nds.model;

import nds.model.base.BaseAdTable;

/**
 * This is the object class that relates to the AD_TABLE table.
 * Any customizations belong here.
 */
public class AdTable extends BaseAdTable {

/*[CONSTRUCTOR MARKER BEGIN]*/
	public AdTable () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public AdTable (java.lang.Integer _id) {
		super(_id);
	}

	/**
	 * Constructor for required fields
	 */
	public AdTable (
		java.lang.Integer _id,
		nds.model.AdTableCategory _adTableCategory,
		java.lang.String _name,
		java.lang.String _description,
		java.lang.String _mask,
		java.lang.Integer _adTableCategoryId,
		java.lang.String _hasTrigAc,
		java.lang.String _hasTrigAm,
		java.lang.String _hasTrigBm,
		java.lang.String _hasTrigBd,
		java.lang.String _isDispatchable,
		java.lang.String _isTree,
		java.lang.String _isMenuObj,
		java.lang.String _isSMS,
		java.lang.String _isDropdown,
		java.lang.String _isActive) {

		super (
			_id,
			_adTableCategory,
			_name,
			_description,
			_mask,
			_adTableCategoryId,
			_hasTrigAc,
			_hasTrigAm,
			_hasTrigBm,
			_hasTrigBd,
			_isDispatchable,
			_isTree,
			_isMenuObj,
			_isSMS,
			_isDropdown,
			_isActive);
	}
/*[CONSTRUCTOR MARKER END]*/
}