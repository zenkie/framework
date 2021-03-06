package nds.model.base;

import java.io.Serializable;


/**
 * This class has been automatically generated by Hibernate Synchronizer.
 * For more information or documentation, visit The Hibernate Synchronizer page
 * at http://www.binamics.com/hibernatesync or contact Joe Hudson at joe@binamics.com.
 *
 * This is an object that contains data related to the AD_REFBYTABLE table.
 * Do not modify this class because it will be overwritten if the configuration file
 * related to this class is modified.
 *
 * @hibernate.class
 *  table="AD_REFBYTABLE"
 */
public abstract class BaseAdRefbytable  implements Serializable {

	public static String PROP_AD_REFBY_TABLE_ID = "AdRefbyTableId";
	public static String PROP_FILTER = "Filter";
	public static String PROP_IS_ACTIVE = "IsActive";
	public static String PROP_ASSOC_TYPE = "AssocType";
	public static String PROP_MODIFIER_ID = "ModifierId";
	public static String PROP_AD_REFBY_COLUMN_ID = "AdRefbyColumnId";
	public static String PROP_OWNER_ID = "OwnerId";
	public static String PROP_AD_REFBY_COLUMN = "AdRefbyColumn";
	public static String PROP_AD_TABLE = "AdTable";
	public static String PROP_DESCRIPTION = "Description";
	public static String PROP_AD_CLIENT_ID = "AdClientId";
	public static String PROP_ORDERNO = "Orderno";
	public static String PROP_AD_REFBY_TABLE = "AdRefbyTable";
	public static String PROP_AD_TABLE_ID = "AdTableId";
	public static String PROP_MODIFIED_DATE = "ModifiedDate";
	public static String PROP_ID = "Id";
	public static String PROP_CREATION_DATE = "CreationDate";
	public static String PROP_INLINE_MODE = "InlineMode";


	private int hashCode = Integer.MIN_VALUE;

	// primary key
	private java.lang.Integer _id;

	// fields
	private java.lang.Integer _adClientId;
	private java.lang.Integer _adTableId;
	private java.lang.Integer _orderno;
	private java.lang.String _description;
	private java.lang.Integer _adRefbyTableId;
	private java.lang.Integer _adRefbyColumnId;
	private java.lang.String _filter;
	private java.lang.String _assocType;
	private java.lang.Integer _ownerId;
	private java.lang.Integer _modifierId;
	private java.util.Date _creationDate;
	private java.util.Date _modifiedDate;
	private java.lang.String _isActive;
	private java.lang.String _inlineMode;
	
	// many to one
	private nds.model.AdTable _adTable;
	private nds.model.AdTable _adRefbyTable;
	private nds.model.AdColumn _adRefbyColumn;


	// constructors
	public BaseAdRefbytable () {
		initialize();
	}

	/**
	 * Constructor for primary key
	 */
	public BaseAdRefbytable (java.lang.Integer _id) {
		this.setId(_id);
		initialize();
	}

	/**
	 * Constructor for required fields
	 */
	public BaseAdRefbytable (
		java.lang.Integer _id,
		nds.model.AdTable _adTable,
		nds.model.AdTable _adRefbyTable,
		nds.model.AdColumn _adRefbyColumn,
		java.lang.Integer _adTableId,
		java.lang.Integer _adRefbyTableId,
		java.lang.Integer _adRefbyColumnId,
		java.lang.String _assocType,
		java.lang.String _isActive) {

		this.setId(_id);
		this.setAdTable(_adTable);
		this.setAdRefbyTable(_adRefbyTable);
		this.setAdRefbyColumn(_adRefbyColumn);
		this.setAdTableId(_adTableId);
		this.setAdRefbyTableId(_adRefbyTableId);
		this.setAdRefbyColumnId(_adRefbyColumnId);
		this.setAssocType(_assocType);
		this.setIsActive(_isActive);
		initialize();
	}

	protected void initialize () {}



	/**
	 * Return the unique identifier of this class
     * @hibernate.id
     *  generator-class="assigned"
     *  column="ID"
     */
	public java.lang.Integer getId () {
		return _id;
	}

	/**
	 * Set the unique identifier of this class
	 * @param _id the new ID
	 */
	public void setId (java.lang.Integer _id) {
		this._id = _id;
		this.hashCode = Integer.MIN_VALUE;
	}


	/**
	 * Return the value associated with the column: AD_CLIENT_ID
	 */
	public java.lang.Integer getAdClientId () {
		return _adClientId;
	}

	/**
	 * Set the value related to the column: AD_CLIENT_ID
	 * @param _adClientId the AD_CLIENT_ID value
	 */
	public void setAdClientId (java.lang.Integer _adClientId) {
		this._adClientId = _adClientId;
	}


	/**
	 * Return the value associated with the column: AD_TABLE_ID
	 */
	public java.lang.Integer getAdTableId () {
		return _adTableId;
	}

	/**
	 * Set the value related to the column: AD_TABLE_ID
	 * @param _adTableId the AD_TABLE_ID value
	 */
	public void setAdTableId (java.lang.Integer _adTableId) {
		this._adTableId = _adTableId;
	}


	/**
	 * Return the value associated with the column: ORDERNO
	 */
	public java.lang.Integer getOrderno () {
		return _orderno;
	}

	/**
	 * Set the value related to the column: ORDERNO
	 * @param _orderno the ORDERNO value
	 */
	public void setOrderno (java.lang.Integer _orderno) {
		this._orderno = _orderno;
	}


	/**
	 * Return the value associated with the column: DESCRIPTION
	 */
	public java.lang.String getDescription () {
		return _description;
	}

	/**
	 * Set the value related to the column: DESCRIPTION
	 * @param _description the DESCRIPTION value
	 */
	public void setDescription (java.lang.String _description) {
		this._description = _description;
	}


	/**
	 * Return the value associated with the column: AD_REFBY_TABLE_ID
	 */
	public java.lang.Integer getAdRefbyTableId () {
		return _adRefbyTableId;
	}

	/**
	 * Set the value related to the column: AD_REFBY_TABLE_ID
	 * @param _adRefbyTableId the AD_REFBY_TABLE_ID value
	 */
	public void setAdRefbyTableId (java.lang.Integer _adRefbyTableId) {
		this._adRefbyTableId = _adRefbyTableId;
	}


	/**
	 * Return the value associated with the column: AD_REFBY_COLUMN_ID
	 */
	public java.lang.Integer getAdRefbyColumnId () {
		return _adRefbyColumnId;
	}

	/**
	 * Set the value related to the column: AD_REFBY_COLUMN_ID
	 * @param _adRefbyColumnId the AD_REFBY_COLUMN_ID value
	 */
	public void setAdRefbyColumnId (java.lang.Integer _adRefbyColumnId) {
		this._adRefbyColumnId = _adRefbyColumnId;
	}


	/**
	 * Return the value associated with the column: FILTER
	 */
	public java.lang.String getFilter () {
		return _filter;
	}

	/**
	 * Set the value related to the column: FILTER
	 * @param _filter the FILTER value
	 */
	public void setFilter (java.lang.String _filter) {
		this._filter = _filter;
	}


	/**
	 * Return the value associated with the column: ASSOCTYPE
	 */
	public java.lang.String getAssocType () {
		return _assocType;
	}

	/**
	 * Set the value related to the column: ASSOCTYPE
	 * @param _assocType the ASSOCTYPE value
	 */
	public void setAssocType (java.lang.String _assocType) {
		this._assocType = _assocType;
	}


	/**
	 * Return the value associated with the column: OWNERID
	 */
	public java.lang.Integer getOwnerId () {
		return _ownerId;
	}

	/**
	 * Set the value related to the column: OWNERID
	 * @param _ownerId the OWNERID value
	 */
	public void setOwnerId (java.lang.Integer _ownerId) {
		this._ownerId = _ownerId;
	}


	/**
	 * Return the value associated with the column: MODIFIERID
	 */
	public java.lang.Integer getModifierId () {
		return _modifierId;
	}

	/**
	 * Set the value related to the column: MODIFIERID
	 * @param _modifierId the MODIFIERID value
	 */
	public void setModifierId (java.lang.Integer _modifierId) {
		this._modifierId = _modifierId;
	}


	/**
	 * Return the value associated with the column: CREATIONDATE
	 */
	public java.util.Date getCreationDate () {
		return _creationDate;
	}

	/**
	 * Set the value related to the column: CREATIONDATE
	 * @param _creationDate the CREATIONDATE value
	 */
	public void setCreationDate (java.util.Date _creationDate) {
		this._creationDate = _creationDate;
	}


	/**
	 * Return the value associated with the column: MODIFIEDDATE
	 */
	public java.util.Date getModifiedDate () {
		return _modifiedDate;
	}

	/**
	 * Set the value related to the column: MODIFIEDDATE
	 * @param _modifiedDate the MODIFIEDDATE value
	 */
	public void setModifiedDate (java.util.Date _modifiedDate) {
		this._modifiedDate = _modifiedDate;
	}


	/**
	 * Return the value associated with the column: ISACTIVE
	 */
	public java.lang.String getIsActive () {
		return _isActive;
	}

	/**
	 * Set the value related to the column: ISACTIVE
	 * @param _isActive the ISACTIVE value
	 */
	public void setIsActive (java.lang.String _isActive) {
		this._isActive = _isActive;
	}
	/**
	 * Return the value associated with the column: ISACTIVE
	 */
	public java.lang.String getInlineMode () {
		return this._inlineMode;
	}

	/**
	 * Set the value related to the column: _inlineMode
	 * @param _isActive the _inlineMode value
	 */
	public void setInlineMode (java.lang.String _inlineMode) {
		this._inlineMode = _inlineMode;
	}

	/**
     * @hibernate.property
     *  column=AD_TABLE_ID
	 * not-null=true
	 */
	public nds.model.AdTable getAdTable () {
		return this._adTable;
	}

	/**
	 * Set the value related to the column: AD_TABLE_ID
	 * @param _adTable the AD_TABLE_ID value
	 */
	public void setAdTable (nds.model.AdTable _adTable) {
		this._adTable = _adTable;
	}


	/**
     * @hibernate.property
     *  column=AD_REFBY_TABLE_ID
	 * not-null=true
	 */
	public nds.model.AdTable getAdRefbyTable () {
		return this._adRefbyTable;
	}

	/**
	 * Set the value related to the column: AD_REFBY_TABLE_ID
	 * @param _adRefbyTable the AD_REFBY_TABLE_ID value
	 */
	public void setAdRefbyTable (nds.model.AdTable _adRefbyTable) {
		this._adRefbyTable = _adRefbyTable;
	}


	/**
     * @hibernate.property
     *  column=AD_REFBY_COLUMN_ID
	 * not-null=true
	 */
	public nds.model.AdColumn getAdRefbyColumn () {
		return this._adRefbyColumn;
	}

	/**
	 * Set the value related to the column: AD_REFBY_COLUMN_ID
	 * @param _adRefbyColumn the AD_REFBY_COLUMN_ID value
	 */
	public void setAdRefbyColumn (nds.model.AdColumn _adRefbyColumn) {
		this._adRefbyColumn = _adRefbyColumn;
	}


	public boolean equals (Object obj) {
		if (null == obj) return false;
		if (!(obj instanceof nds.model.base.BaseAdRefbytable)) return false;
		else {
			nds.model.base.BaseAdRefbytable mObj = (nds.model.base.BaseAdRefbytable) obj;
			if (null == this.getId() || null == mObj.getId()) return false;
			else return (this.getId().equals(mObj.getId()));
		}
	}


	public int hashCode () {
		if (Integer.MIN_VALUE == this.hashCode) {
			if (null == this.getId()) return super.hashCode();
			else {
				String hashStr = this.getClass().getName() + ":" + this.getId().hashCode();
				this.hashCode = hashStr.hashCode();
			}
		}
		return this.hashCode;
	}


	public String toString () {
		return super.toString();
	}

}