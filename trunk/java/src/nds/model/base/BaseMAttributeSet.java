package nds.model.base;

import java.io.Serializable;


/**
 * This class has been automatically generated by Hibernate Synchronizer.
 * For more information or documentation, visit The Hibernate Synchronizer page
 * at http://www.binamics.com/hibernatesync or contact Joe Hudson at joe@binamics.com.
 *
 * This is an object that contains data related to the M_ATTRIBUTESET table.
 * Do not modify this class because it will be overwritten if the configuration file
 * related to this class is modified.
 *
 * @hibernate.class
 *  table="M_ATTRIBUTESET"
 */
public abstract class BaseMAttributeSet  implements Serializable {

	public static String PROP_MANDATORY_TYPE = "MandatoryType";
	public static String PROP_IS_ACTIVE = "IsActive";
	public static String PROP_IS_GUARANTEE_DATE_MANDATORY = "IsGuaranteeDateMandatory";
	public static String PROP_AD_ORG = "AdOrg";
	public static String PROP_IS_INSTANCE_ATTRIBUTE = "IsInstanceAttribute";
	public static String PROP_MODIFIER_ID = "ModifierId";
	public static String PROP_IS_SERNOMANDATORY = "IsSernomandatory";
	public static String PROP_IS_LOT = "IsLot";
	public static String PROP_IS_LOTMANDATORY = "IsLotmandatory";
	public static String PROP_M_LOTCTL = "MLotctl";
	public static String PROP_M_LOTCTL_ID = "MLotctlId";
	public static String PROP_OWNER_ID = "OwnerId";
	public static String PROP_IS_GUARANTEE_DATE = "IsGuaranteeDate";
	public static String PROP_GUARANTEEDAYS = "Guaranteedays";
	public static String PROP_M_SERNOCTL_ID = "MSernoctlId";
	public static String PROP_AD_ORG_ID = "AdOrgId";
	public static String PROP_DESCRIPTION = "Description";
	public static String PROP_M_SERNOCTL = "MSernoctl";
	public static String PROP_AD_CLIENT_ID = "AdClientId";
	public static String PROP_NAME = "Name";
	public static String PROP_MODIFIED_DATE = "ModifiedDate";
	public static String PROP_IS_SERNO = "IsSerno";
	public static String PROP_ID = "Id";
	public static String PROP_CREATION_DATE = "CreationDate";


	private int hashCode = Integer.MIN_VALUE;

	// primary key
	private java.lang.Integer _id;

	// fields
	private java.lang.Integer _adClientId;
	private java.lang.Integer _adOrgId;
	private java.lang.String _name;
	private java.lang.String _description;
	private java.lang.String _isSerno;
	private java.lang.Integer _mSernoctlId;
	private java.lang.String _isLot;
	private java.lang.Integer _mLotctlId;
	private java.lang.String _isGuaranteeDate;
	private java.lang.Integer _guaranteedays;
	private java.lang.String _isInstanceAttribute;
	private java.lang.String _mandatoryType;
	private java.lang.String _isGuaranteeDateMandatory;
	private java.lang.String _isLotmandatory;
	private java.lang.String _isSernomandatory;
	private java.lang.Integer _ownerId;
	private java.lang.Integer _modifierId;
	private java.util.Date _creationDate;
	private java.util.Date _modifiedDate;
	private java.lang.String _isActive;

	// many to one
	private nds.model.AdOrg _adOrg;
	private nds.model.MSernoctl _mSernoctl;
	private nds.model.MLotctl _mLotctl;


	// constructors
	public BaseMAttributeSet () {
		initialize();
	}

	/**
	 * Constructor for primary key
	 */
	public BaseMAttributeSet (java.lang.Integer _id) {
		this.setId(_id);
		initialize();
	}

	/**
	 * Constructor for required fields
	 */
	public BaseMAttributeSet (
		java.lang.Integer _id,
		java.lang.String _name,
		java.lang.String _isSerno,
		java.lang.String _isLot,
		java.lang.String _isGuaranteeDate,
		java.lang.Integer _guaranteedays,
		java.lang.String _isInstanceAttribute,
		java.lang.String _mandatoryType,
		java.lang.String _isGuaranteeDateMandatory,
		java.lang.String _isLotmandatory,
		java.lang.String _isSernomandatory,
		java.lang.String _isActive) {

		this.setId(_id);
		this.setName(_name);
		this.setIsSerno(_isSerno);
		this.setIsLot(_isLot);
		this.setIsGuaranteeDate(_isGuaranteeDate);
		this.setGuaranteedays(_guaranteedays);
		this.setIsInstanceAttribute(_isInstanceAttribute);
		this.setMandatoryType(_mandatoryType);
		this.setIsGuaranteeDateMandatory(_isGuaranteeDateMandatory);
		this.setIsLotmandatory(_isLotmandatory);
		this.setIsSernomandatory(_isSernomandatory);
		this.setIsActive(_isActive);
		initialize();
	}

	protected void initialize () {}



	/**
	 * Return the unique identifier of this class
     * @hibernate.id
     *  generator-class="sequence"
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
	 * Return the value associated with the column: AD_ORG_ID
	 */
	public java.lang.Integer getAdOrgId () {
		return _adOrgId;
	}

	/**
	 * Set the value related to the column: AD_ORG_ID
	 * @param _adOrgId the AD_ORG_ID value
	 */
	public void setAdOrgId (java.lang.Integer _adOrgId) {
		this._adOrgId = _adOrgId;
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
	 * Return the value associated with the column: ISSERNO
	 */
	public java.lang.String getIsSerno () {
		return _isSerno;
	}

	/**
	 * Set the value related to the column: ISSERNO
	 * @param _isSerno the ISSERNO value
	 */
	public void setIsSerno (java.lang.String _isSerno) {
		this._isSerno = _isSerno;
	}


	/**
	 * Return the value associated with the column: M_SERNOCTL_ID
	 */
	public java.lang.Integer getMSernoctlId () {
		return _mSernoctlId;
	}

	/**
	 * Set the value related to the column: M_SERNOCTL_ID
	 * @param _mSernoctlId the M_SERNOCTL_ID value
	 */
	public void setMSernoctlId (java.lang.Integer _mSernoctlId) {
		this._mSernoctlId = _mSernoctlId;
	}


	/**
	 * Return the value associated with the column: ISLOT
	 */
	public java.lang.String getIsLot () {
		return _isLot;
	}

	/**
	 * Set the value related to the column: ISLOT
	 * @param _isLot the ISLOT value
	 */
	public void setIsLot (java.lang.String _isLot) {
		this._isLot = _isLot;
	}


	/**
	 * Return the value associated with the column: M_LOTCTL_ID
	 */
	public java.lang.Integer getMLotctlId () {
		return _mLotctlId;
	}

	/**
	 * Set the value related to the column: M_LOTCTL_ID
	 * @param _mLotctlId the M_LOTCTL_ID value
	 */
	public void setMLotctlId (java.lang.Integer _mLotctlId) {
		this._mLotctlId = _mLotctlId;
	}


	/**
	 * Return the value associated with the column: ISGUARANTEEDATE
	 */
	public java.lang.String getIsGuaranteeDate () {
		return _isGuaranteeDate;
	}

	/**
	 * Set the value related to the column: ISGUARANTEEDATE
	 * @param _isGuaranteeDate the ISGUARANTEEDATE value
	 */
	public void setIsGuaranteeDate (java.lang.String _isGuaranteeDate) {
		this._isGuaranteeDate = _isGuaranteeDate;
	}


	/**
	 * Return the value associated with the column: GUARANTEEDAYS
	 */
	public java.lang.Integer getGuaranteedays () {
		return _guaranteedays;
	}

	/**
	 * Set the value related to the column: GUARANTEEDAYS
	 * @param _guaranteedays the GUARANTEEDAYS value
	 */
	public void setGuaranteedays (java.lang.Integer _guaranteedays) {
		this._guaranteedays = _guaranteedays;
	}


	/**
	 * Return the value associated with the column: ISINSTANCEATTRIBUTE
	 */
	public java.lang.String getIsInstanceAttribute () {
		return _isInstanceAttribute;
	}

	/**
	 * Set the value related to the column: ISINSTANCEATTRIBUTE
	 * @param _isInstanceAttribute the ISINSTANCEATTRIBUTE value
	 */
	public void setIsInstanceAttribute (java.lang.String _isInstanceAttribute) {
		this._isInstanceAttribute = _isInstanceAttribute;
	}


	/**
	 * Return the value associated with the column: MANDATORYTYPE
	 */
	public java.lang.String getMandatoryType () {
		return _mandatoryType;
	}

	/**
	 * Set the value related to the column: MANDATORYTYPE
	 * @param _mandatoryType the MANDATORYTYPE value
	 */
	public void setMandatoryType (java.lang.String _mandatoryType) {
		this._mandatoryType = _mandatoryType;
	}


	/**
	 * Return the value associated with the column: ISGUARANTEEDATEMANDATORY
	 */
	public java.lang.String getIsGuaranteeDateMandatory () {
		return _isGuaranteeDateMandatory;
	}

	/**
	 * Set the value related to the column: ISGUARANTEEDATEMANDATORY
	 * @param _isGuaranteeDateMandatory the ISGUARANTEEDATEMANDATORY value
	 */
	public void setIsGuaranteeDateMandatory (java.lang.String _isGuaranteeDateMandatory) {
		this._isGuaranteeDateMandatory = _isGuaranteeDateMandatory;
	}


	/**
	 * Return the value associated with the column: ISLOTMANDATORY
	 */
	public java.lang.String getIsLotmandatory () {
		return _isLotmandatory;
	}

	/**
	 * Set the value related to the column: ISLOTMANDATORY
	 * @param _isLotmandatory the ISLOTMANDATORY value
	 */
	public void setIsLotmandatory (java.lang.String _isLotmandatory) {
		this._isLotmandatory = _isLotmandatory;
	}


	/**
	 * Return the value associated with the column: ISSERNOMANDATORY
	 */
	public java.lang.String getIsSernomandatory () {
		return _isSernomandatory;
	}

	/**
	 * Set the value related to the column: ISSERNOMANDATORY
	 * @param _isSernomandatory the ISSERNOMANDATORY value
	 */
	public void setIsSernomandatory (java.lang.String _isSernomandatory) {
		this._isSernomandatory = _isSernomandatory;
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
     * @hibernate.property
     *  column=AD_ORG_ID
	 */
	public nds.model.AdOrg getAdOrg () {
		return this._adOrg;
	}

	/**
	 * Set the value related to the column: AD_ORG_ID
	 * @param _adOrg the AD_ORG_ID value
	 */
	public void setAdOrg (nds.model.AdOrg _adOrg) {
		this._adOrg = _adOrg;
	}


	/**
     * @hibernate.property
     *  column=M_SERNOCTL_ID
	 */
	public nds.model.MSernoctl getMSernoctl () {
		return this._mSernoctl;
	}

	/**
	 * Set the value related to the column: M_SERNOCTL_ID
	 * @param _mSernoctl the M_SERNOCTL_ID value
	 */
	public void setMSernoctl (nds.model.MSernoctl _mSernoctl) {
		this._mSernoctl = _mSernoctl;
	}


	/**
     * @hibernate.property
     *  column=M_LOTCTL_ID
	 */
	public nds.model.MLotctl getMLotctl () {
		return this._mLotctl;
	}

	/**
	 * Set the value related to the column: M_LOTCTL_ID
	 * @param _mLotctl the M_LOTCTL_ID value
	 */
	public void setMLotctl (nds.model.MLotctl _mLotctl) {
		this._mLotctl = _mLotctl;
	}


	public boolean equals (Object obj) {
		if (null == obj) return false;
		if (!(obj instanceof nds.model.base.BaseMAttributeSet)) return false;
		else {
			nds.model.base.BaseMAttributeSet mObj = (nds.model.base.BaseMAttributeSet) obj;
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