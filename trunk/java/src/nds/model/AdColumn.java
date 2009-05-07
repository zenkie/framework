package nds.model;

import nds.model.base.BaseAdColumn;

/**
 * This is the object class that relates to the AD_COLUMN table.
 * Any customizations belong here.
 */
public class AdColumn extends BaseAdColumn {

/*[CONSTRUCTOR MARKER BEGIN]*/
	public AdColumn () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public AdColumn (java.lang.Integer _id) {
		super(_id);
	}

	/**
	 * Constructor for required fields
	 */
	public AdColumn (
		java.lang.Integer _id,
		nds.model.AdTable _adTable,
		java.lang.Integer _adTableId,
		java.lang.String _dbName,
		java.lang.String _colType,
		java.lang.String _description,
		java.lang.String _modifiable,
		java.lang.String _mask,
		java.lang.String _obtainmanner,
		java.lang.String _isActive) {

		super (
			_id,
			_adTable,
			_adTableId,
			_dbName,
			_colType,
			_description,
			_modifiable,
			_mask,
			_obtainmanner,
			_isActive);
	}
/*[CONSTRUCTOR MARKER END]*/
	public String toString(){
		return this.getAdTable().getName()+"."+ this.getDbName() + "#"+ this.getId();
	}
}