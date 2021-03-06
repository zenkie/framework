package nds.model.base;

import java.io.Serializable;


/**
 * This class has been automatically generated by Hibernate Synchronizer.
 * For more information or documentation, visit The Hibernate Synchronizer page
 * at http://www.binamics.com/hibernatesync or contact Joe Hudson at joe@binamics.com.
 *
 * This is an object that contains data related to the M_ATTRIBUTEUSE table.
 * Do not modify this class because it will be overwritten if the configuration file
 * related to this class is modified.
 *
 * @hibernate.class
 *  table="M_ATTRIBUTEUSE"
 */
public abstract class BaseMAttributeUse  implements Serializable {

	public static String PROP_IS_ACTIVE = "IsActive";
	public static String PROP_AD_ORG = "AdOrg";
	public static String PROP_SEQNO = "Seqno";
	public static String PROP_MODIFIER_ID = "ModifierId";
	public static String PROP_M_ATTRIBUTE_ID = "MAttributeId";
	public static String PROP_OWNER_ID = "OwnerId";
	public static String PROP_M_ATTRIBUTE = "MAttribute";
	public static String PROP_M_ATTRIBUTE_SET_ID = "MAttributeSetId";
	public static String PROP_AD_ORG_ID = "AdOrgId";
	public static String PROP_AD_CLIENT_ID = "AdClientId";
	public static String PROP_M_ATTRIBUTE_SET = "MAttributeSet";
	public static String PROP_MODIFIED_DATE = "ModifiedDate";
	public static String PROP_ID = "Id";
	public static String PROP_CREATION_DATE = "CreationDate";


	private int hashCode = Integer.MIN_VALUE;

	// primary key
	private java.lang.Integer _id;

	// fields
	private java.lang.Integer _adClientId;
	private java.lang.Integer _adOrgId;
	private java.lang.Integer _seqno;
	private java.lang.Integer _mAttributeId;
	private java.lang.Integer _mAttributeSetId;
	private java.lang.Integer _ownerId;
	private java.lang.Integer _modifierId;
	private java.util.Date _creationDate;
	private java.util.Date _modifiedDate;
	private java.lang.String _isActive;

	// many to one
	private nds.model.AdOrg _adOrg;
	private nds.model.MAttribute _mAttribute;
	private nds.model.MAttributeSet _mAttributeSet;


	// constructors
	public BaseMAttributeUse () {
		initialize();
	}

	/**
	 * Constructor for primary key
	 */
	public BaseMAttributeUse (java.lang.Integer _id) {
		this.setId(_id);
		initialize();
	}

	/**
	 * Constructor for required fields
	 */
	public BaseMAttributeUse (
		java.lang.Integer _id,
		java.lang.String _isActive) {

		this.setId(_id);
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
	 * Return the value associated with the column: SEQNO
	 */
	public java.lang.Integer getSeqno () {
		return _seqno;
	}

	/**
	 * Set the value related to the column: SEQNO
	 * @param _seqno the SEQNO value
	 */
	public void setSeqno (java.lang.Integer _seqno) {
		this._seqno = _seqno;
	}


	/**
	 * Return the value associated with the column: M_ATTRIBUTE_ID
	 */
	public java.lang.Integer getMAttributeId () {
		return _mAttributeId;
	}

	/**
	 * Set the value related to the column: M_ATTRIBUTE_ID
	 * @param _mAttributeId the M_ATTRIBUTE_ID value
	 */
	public void setMAttributeId (java.lang.Integer _mAttributeId) {
		this._mAttributeId = _mAttributeId;
	}


	/**
	 * Return the value associated with the column: M_ATTRIBUTESET_ID
	 */
	public java.lang.Integer getMAttributeSetId () {
		return _mAttributeSetId;
	}

	/**
	 * Set the value related to the column: M_ATTRIBUTESET_ID
	 * @param _mAttributeSetId the M_ATTRIBUTESET_ID value
	 */
	public void setMAttributeSetId (java.lang.Integer _mAttributeSetId) {
		this._mAttributeSetId = _mAttributeSetId;
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
     *  column=M_ATTRIBUTE_ID
	 */
	public nds.model.MAttribute getMAttribute () {
		return this._mAttribute;
	}

	/**
	 * Set the value related to the column: M_ATTRIBUTE_ID
	 * @param _mAttribute the M_ATTRIBUTE_ID value
	 */
	public void setMAttribute (nds.model.MAttribute _mAttribute) {
		this._mAttribute = _mAttribute;
	}


	/**
     * @hibernate.property
     *  column=M_ATTRIBUTESET_ID
	 */
	public nds.model.MAttributeSet getMAttributeSet () {
		return this._mAttributeSet;
	}

	/**
	 * Set the value related to the column: M_ATTRIBUTESET_ID
	 * @param _mAttributeSet the M_ATTRIBUTESET_ID value
	 */
	public void setMAttributeSet (nds.model.MAttributeSet _mAttributeSet) {
		this._mAttributeSet = _mAttributeSet;
	}


	public boolean equals (Object obj) {
		if (null == obj) return false;
		if (!(obj instanceof nds.model.base.BaseMAttributeUse)) return false;
		else {
			nds.model.base.BaseMAttributeUse mObj = (nds.model.base.BaseMAttributeUse) obj;
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