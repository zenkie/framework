package nds.model.base;

import java.io.Serializable;


/**
 * This class has been automatically generated by Hibernate Synchronizer.
 * For more information or documentation, visit The Hibernate Synchronizer page
 * at http://www.binamics.com/hibernatesync or contact Joe Hudson at joe@binamics.com.
 *
 * This is an object that contains data related to the AD_COLUMN table.
 * Do not modify this class because it will be overwritten if the configuration file
 * related to this class is modified.
 *
 * @hibernate.class
 *  table="AD_COLUMN"
 */
public abstract class BaseAdColumn  implements Serializable {

	public static String PROP_IS_AK = "IsAk";
	public static String PROP_IS_ACTIVE = "IsActive";
	public static String PROP_MASK = "Mask";
	public static String PROP_FILTER = "Filter";
	public static String PROP_MODIFIER_ID = "ModifierId";
	public static String PROP_SUMMETHOD = "Summethod";
	public static String PROP_AD_LIMIT_VALUE_GROUP_ID = "AdLimitValueGroupId";
	public static String PROP_DISPLAYCOLS = "Displaycols";
	public static String PROP_OWNER_ID = "OwnerId";
	public static String PROP_AD_TABLE = "AdTable";
	public static String PROP_DB_NAME = "DbName";
	public static String PROP_DESCRIPTION = "Description";
	public static String PROP_COL_TYPE = "ColType";
	public static String PROP_DISPLAY_TYPE = "DisplayType";
	public static String PROP_AD_CLIENT_ID = "AdClientId";
	public static String PROP_INTERPRETER = "Interpreter";
	public static String PROP_ORDERNO = "Orderno";
	public static String PROP_DISPLAYROWS = "Displayrows";
	public static String PROP_NULLABLE = "Nullable";
	public static String PROP_REF_COLUMN_ID = "RefColumnId";
	public static String PROP_AD_TABLE_ID = "AdTableId";
	public static String PROP_NAME = "Name";
	public static String PROP_MODIFIED_DATE = "ModifiedDate";
	public static String PROP_IS_UPPER_CASE = "IsUpperCase";
	public static String PROP_OBTAINMANNER = "Obtainmanner";
	public static String PROP_MODIFIABLE = "Modifiable";
	public static String PROP_AD_LIMIT_VALUE_GROUP = "AdLimitValueGroup";
	public static String PROP_U_CLOB_ID = "UClobId";
	public static String PROP_DEFAULT_VALUE = "DefaultValue";
	public static String PROP_REF_COLUMN = "RefColumn";
	public static String PROP_STAT_SIZE = "StatSize";
	public static String PROP_REGEXPRESSION = "Regexpression";
	public static String PROP_IS_DK = "IsDk";
	public static String PROP_ERRMSG = "Errmsg";
	public static String PROP_COMMENTS = "Comments";
	public static String PROP_PROPS = "Props";
	// show comments
	public static String PROP_SHOW_COMMENTS = "Showcomment";
	public static String PROP_SHOW_TITLE = "Showtitle";
	public static String PROP_ROWSPAN = "Rowspan";
	public static String PROP_SEQUENCENAME = "Sequencename";
	public static String PROP_ID = "Id";
	public static String PROP_CREATION_DATE = "CreationDate";
	public static String PROP_DISPLAYWIDTH = "Displaywidth";
	public static String PROP_IS_INDEXED = "IsIndexed";
	public static String PROP_ON_DELETE = "OnDelete";

	private int hashCode = Integer.MIN_VALUE;

	// primary key
	private java.lang.Integer _id;

	// fields
	private java.lang.Integer _adClientId;
	private java.lang.Integer _adTableId;
	private java.lang.String _name;
	private java.lang.String _dbName;
	private java.lang.String _colType;
	private java.lang.String _isAk;
	private java.lang.String _isDk;
	private java.lang.String _nullable;
	private java.lang.String _summethod;
	private java.lang.String _description;
	private java.lang.Integer _orderno;
	private java.lang.String _modifiable;
	private java.lang.String _mask;
	private java.lang.String _obtainmanner;
	private java.lang.String _sequencename;
	private java.lang.String _comments;
	private java.lang.String _props;
	private java.lang.Integer _refColumnId;
	private java.lang.Integer _adLimitValueGroupId;
	private java.lang.String _defaultValue;
	private java.lang.String _regexpression;
	private java.lang.String _errmsg;
	private java.lang.String _interpreter;
	private java.lang.String _filter;
	private java.lang.String _displayType;
	private java.lang.Integer _displayrows;
	private java.lang.Integer _displaycols;
	private java.lang.Integer _displaywidth;
	private java.lang.Integer _uClobId;
	private java.lang.Integer _ownerId;
	private java.lang.Integer _modifierId;
	private java.util.Date _creationDate;
	private java.util.Date _modifiedDate;
	private java.lang.String _isActive;
	private java.lang.String _isUpperCase;
	private java.lang.Integer _statSize;
	private java.lang.String _isIndexed;
	private java.lang.String _onDelete;
	private java.lang.String _Showcomment;
	private java.lang.String _Showtitle;
	private java.lang.String _Rowspan;
	
	// many to one
	private nds.model.AdTable _adTable;
	private nds.model.AdColumn _refColumn;
	private nds.model.AdLimitValueGroup _adLimitValueGroup;


	// constructors
	public BaseAdColumn () {
		initialize();
	}

	/**
	 * Constructor for primary key
	 */
	public BaseAdColumn (java.lang.Integer _id) {
		this.setId(_id);
		initialize();
	}

	/**
	 * Constructor for required fields
	 */
	public BaseAdColumn (
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

		this.setId(_id);
		this.setAdTable(_adTable);
		this.setAdTableId(_adTableId);
		this.setDbName(_dbName);
		this.setColType(_colType);
		this.setDescription(_description);
		this.setModifiable(_modifiable);
		this.setMask(_mask);
		this.setObtainmanner(_obtainmanner);
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
	 * Return the value associated with the column: NAME
	 */
	public java.lang.String getName () {
		return _name;
	}

	/**
	 * Set the value related to the column: NAME
	 * @param _name the NAME value
	 */
	public void setName (java.lang.String _name) {
		this._name = _name;
	}


	/**
	 * Return the value associated with the column: DBNAME
	 */
	public java.lang.String getDbName () {
		return _dbName;
	}

	/**
	 * Set the value related to the column: DBNAME
	 * @param _dbName the DBNAME value
	 */
	public void setDbName (java.lang.String _dbName) {
		this._dbName = _dbName;
	}


	/**
	 * Return the value associated with the column: COLTYPE
	 */
	public java.lang.String getColType () {
		return _colType;
	}

	/**
	 * Set the value related to the column: COLTYPE
	 * @param _colType the COLTYPE value
	 */
	public void setColType (java.lang.String _colType) {
		this._colType = _colType;
	}


	/**
	 * Return the value associated with the column: ISAK
	 */
	public java.lang.String getIsAk () {
		return _isAk;
	}

	/**
	 * Set the value related to the column: ISAK
	 * @param _isAk the ISAK value
	 */
	public void setIsAk (java.lang.String _isAk) {
		this._isAk = _isAk;
	}


	/**
	 * Return the value associated with the column: ISDK
	 */
	public java.lang.String getIsIndexed () {
		return _isIndexed;
	}

	/**
	 * Set the value related to the column: ISDK
	 * @param _isDk the ISDK value
	 */
	public void setIsIndexed (java.lang.String _isIndexed) {
		this._isIndexed = _isIndexed;
	}
	/**
	 * Return the value associated with the column: ISDK
	 */
	public java.lang.String getOnDelete () {
		return _onDelete;
	}

	/**
	 * Set the value related to the column: ISDK
	 * @param _isDk the ISDK value
	 */
	public void setOnDelete (java.lang.String _onDelete) {
		this._onDelete = _onDelete;
	}

	/**
	 * Return the value associated with the column: ISDK
	 */
	public java.lang.String getIsDk () {
		return _isDk;
	}

	/**
	 * Set the value related to the column: ISDK
	 * @param _isDk the ISDK value
	 */
	public void setIsDk (java.lang.String _isDk) {
		this._isDk = _isDk;
	}


	/**
	 * Return the value associated with the column: NULLABLE
	 */
	public java.lang.String getNullable () {
		return _nullable;
	}

	/**
	 * Set the value related to the column: NULLABLE
	 * @param _nullable the NULLABLE value
	 */
	public void setNullable (java.lang.String _nullable) {
		this._nullable = _nullable;
	}


	/**
	 * Return the value associated with the column: SUMMETHOD
	 */
	public java.lang.String getSummethod () {
		return _summethod;
	}

	/**
	 * Set the value related to the column: SUMMETHOD
	 * @param _summethod the SUMMETHOD value
	 */
	public void setSummethod (java.lang.String _summethod) {
		this._summethod = _summethod;
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
	 * Return the value associated with the column: MODIFIABLE
	 */
	public java.lang.String getModifiable () {
		return _modifiable;
	}

	/**
	 * Set the value related to the column: MODIFIABLE
	 * @param _modifiable the MODIFIABLE value
	 */
	public void setModifiable (java.lang.String _modifiable) {
		this._modifiable = _modifiable;
	}


	/**
	 * Return the value associated with the column: MASK
	 */
	public java.lang.String getMask () {
		return _mask;
	}

	/**
	 * Set the value related to the column: MASK
	 * @param _mask the MASK value
	 */
	public void setMask (java.lang.String _mask) {
		this._mask = _mask;
	}


	/**
	 * Return the value associated with the column: OBTAINMANNER
	 */
	public java.lang.String getObtainmanner () {
		return _obtainmanner;
	}

	/**
	 * Set the value related to the column: OBTAINMANNER
	 * @param _obtainmanner the OBTAINMANNER value
	 */
	public void setObtainmanner (java.lang.String _obtainmanner) {
		this._obtainmanner = _obtainmanner;
	}


	/**
	 * Return the value associated with the column: SEQUENCENAME
	 */
	public java.lang.String getSequencename () {
		return _sequencename;
	}

	/**
	 * Set the value related to the column: SEQUENCENAME
	 * @param _sequencename the SEQUENCENAME value
	 */
	public void setSequencename (java.lang.String _sequencename) {
		this._sequencename = _sequencename;
	}


	/**
	 * Return the value associated with the column: COMMENTS
	 */
	public java.lang.String getComments () {
		return _comments;
	}

	/**
	 * Set the value related to the column: COMMENTS
	 * @param _comments the COMMENTS value
	 */
	public void setComments (java.lang.String _comments) {
		this._comments = _comments;
	}
	
	/**
	 * Return the value associated with the column: comment
	 */
	public java.lang.String getShowcomment () {
		return _Showcomment;
	}

	/**
	 * Set the value related to the column: _Showcomment
	 * @param _comments the COMMENTS value
	 */
	public void setShowcomment (java.lang.String _Showcomment) {
		this._Showcomment = _Showcomment;
	}	
	
	/**
	 * Return the value associated with the column: title
	 */
	public java.lang.String getShowtitle () {
		return _Showtitle;
	}

	/**
	 * Set the value related to the column: _Showtitle
	 * @param _comments the COMMENTS value
	 */
	public void setShowtitle (java.lang.String _Showtitle) {
		this._Showtitle = _Showtitle;
	}	
	
	
	/**
	 * Return the value associated with the column: title
	 */
	public java.lang.String getRowspan () {
		return _Rowspan;
	}

	/**
	 * Set the value related to the column: _Showtitle
	 * @param _comments the COMMENTS value
	 */
	public void setRowspan (java.lang.String _Rowspan) {
		this._Rowspan = _Rowspan;
	}	
	
	
	/**
	 * Return the value associated with the column: Props
	 */
	public java.lang.String getProps () {
		return _props;
	}

	/**
	 * Set the value related to the column: Props
	 * @param _comments the COMMENTS value
	 */
	public void setProps (java.lang.String _props) {
		this._props = _props;
	}


	/**
	 * Return the value associated with the column: REF_COLUMN_ID
	 */
	public java.lang.Integer getRefColumnId () {
		return _refColumnId;
	}

	/**
	 * Set the value related to the column: REF_COLUMN_ID
	 * @param _refColumnId the REF_COLUMN_ID value
	 */
	public void setRefColumnId (java.lang.Integer _refColumnId) {
		this._refColumnId = _refColumnId;
	}


	/**
	 * Return the value associated with the column: AD_LIMITVALUE_GROUP_ID
	 */
	public java.lang.Integer getAdLimitValueGroupId () {
		return _adLimitValueGroupId;
	}

	/**
	 * Set the value related to the column: AD_LIMITVALUE_GROUP_ID
	 * @param _adLimitValueGroupId the AD_LIMITVALUE_GROUP_ID value
	 */
	public void setAdLimitValueGroupId (java.lang.Integer _adLimitValueGroupId) {
		this._adLimitValueGroupId = _adLimitValueGroupId;
	}


	/**
	 * Return the value associated with the column: DEFAULTVALUE
	 */
	public java.lang.String getDefaultValue () {
		return _defaultValue;
	}

	/**
	 * Set the value related to the column: DEFAULTVALUE
	 * @param _defaultValue the DEFAULTVALUE value
	 */
	public void setDefaultValue (java.lang.String _defaultValue) {
		this._defaultValue = _defaultValue;
	}


	/**
	 * Return the value associated with the column: REGEXPRESSION
	 */
	public java.lang.String getRegexpression () {
		return _regexpression;
	}

	/**
	 * Set the value related to the column: REGEXPRESSION
	 * @param _regexpression the REGEXPRESSION value
	 */
	public void setRegexpression (java.lang.String _regexpression) {
		this._regexpression = _regexpression;
	}


	/**
	 * Return the value associated with the column: ERRMSG
	 */
	public java.lang.String getErrmsg () {
		return _errmsg;
	}

	/**
	 * Set the value related to the column: ERRMSG
	 * @param _errmsg the ERRMSG value
	 */
	public void setErrmsg (java.lang.String _errmsg) {
		this._errmsg = _errmsg;
	}


	/**
	 * Return the value associated with the column: INTERPRETER
	 */
	public java.lang.String getInterpreter () {
		return _interpreter;
	}

	/**
	 * Set the value related to the column: INTERPRETER
	 * @param _interpreter the INTERPRETER value
	 */
	public void setInterpreter (java.lang.String _interpreter) {
		this._interpreter = _interpreter;
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
	 * Return the value associated with the column: DISPLAYTYPE
	 */
	public java.lang.String getDisplayType () {
		return _displayType;
	}

	/**
	 * Set the value related to the column: DISPLAYTYPE
	 * @param _displayType the DISPLAYTYPE value
	 */
	public void setDisplayType (java.lang.String _displayType) {
		this._displayType = _displayType;
	}


	/**
	 * Return the value associated with the column: DISPLAYROWS
	 */
	public java.lang.Integer getDisplayrows () {
		return _displayrows;
	}

	/**
	 * Set the value related to the column: DISPLAYROWS
	 * @param _displayrows the DISPLAYROWS value
	 */
	public void setDisplayrows (java.lang.Integer _displayrows) {
		this._displayrows = _displayrows;
	}


	/**
	 * Return the value associated with the column: DISPLAYCOLS
	 */
	public java.lang.Integer getDisplaycols () {
		return _displaycols;
	}

	/**
	 * Set the value related to the column: DISPLAYCOLS
	 * @param _displaycols the DISPLAYCOLS value
	 */
	public void setDisplaycols (java.lang.Integer _displaycols) {
		this._displaycols = _displaycols;
	}


	/**
	 * Return the value associated with the column: DISPLAYWIDTH
	 */
	public java.lang.Integer getDisplaywidth () {
		return _displaywidth;
	}

	/**
	 * Set the value related to the column: DISPLAYWIDTH
	 * @param _displaywidth the DISPLAYWIDTH value
	 */
	public void setDisplaywidth (java.lang.Integer _displaywidth) {
		this._displaywidth = _displaywidth;
	}


	/**
	 * Return the value associated with the column: U_CLOB_ID
	 */
	public java.lang.Integer getUClobId () {
		return _uClobId;
	}

	/**
	 * Set the value related to the column: U_CLOB_ID
	 * @param _uClobId the U_CLOB_ID value
	 */
	public void setUClobId (java.lang.Integer _uClobId) {
		this._uClobId = _uClobId;
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
	 * Return the value associated with the column: ISUPPERCASE
	 */
	public java.lang.String getIsUpperCase () {
		return _isUpperCase;
	}

	/**
	 * Set the value related to the column: ISUPPERCASE
	 * @param _isUpperCase the ISUPPERCASE value
	 */
	public void setIsUpperCase (java.lang.String _isUpperCase) {
		this._isUpperCase = _isUpperCase;
	}


	/**
	 * Return the value associated with the column: STATSIZE
	 */
	public java.lang.Integer getStatSize () {
		return _statSize;
	}

	/**
	 * Set the value related to the column: STATSIZE
	 * @param _statSize the STATSIZE value
	 */
	public void setStatSize (java.lang.Integer _statSize) {
		this._statSize = _statSize;
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
     *  column=REF_COLUMN_ID
	 */
	public nds.model.AdColumn getRefColumn () {
		return this._refColumn;
	}

	/**
	 * Set the value related to the column: REF_COLUMN_ID
	 * @param _refColumn the REF_COLUMN_ID value
	 */
	public void setRefColumn (nds.model.AdColumn _refColumn) {
		this._refColumn = _refColumn;
	}


	/**
     * @hibernate.property
     *  column=AD_LIMITVALUE_GROUP_ID
	 */
	public nds.model.AdLimitValueGroup getAdLimitValueGroup () {
		return this._adLimitValueGroup;
	}

	/**
	 * Set the value related to the column: AD_LIMITVALUE_GROUP_ID
	 * @param _adLimitValueGroup the AD_LIMITVALUE_GROUP_ID value
	 */
	public void setAdLimitValueGroup (nds.model.AdLimitValueGroup _adLimitValueGroup) {
		this._adLimitValueGroup = _adLimitValueGroup;
	}


	public boolean equals (Object obj) {
		if (null == obj) return false;
		if (!(obj instanceof nds.model.base.BaseAdColumn)) return false;
		else {
			nds.model.base.BaseAdColumn mObj = (nds.model.base.BaseAdColumn) obj;
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