package nds.model.base;

import java.io.Serializable;

public abstract class BaseAdSysModel implements Serializable {
	
	public static String PROP_IS_ACTIVE = "IsActive";
	public static String PROP_AD_CLIENT_ID = "AdClientId";
	public static String PROP_MODIFIER_ID = "ModifierId";
	public static String PROP_ORDERNO = "Orderno";
	public static String PROP_MDISP = "Mdisp";
	public static String PROP_MLINK = "Mlink";
	public static String PROP_PICO = "Pico";
	public static String PROP_NAME = "Name";
	public static String PROP_MODIFIED_DATE = "ModifiedDate";
	public static String PROP_ID = "Id";
	public static String PROP_OWNER_ID = "OwnerId";
	public static String PROP_CREATION_DATE = "CreationDate";


	private int hashCode = Integer.MIN_VALUE;

	// primary key
	private java.lang.Integer _id;

	// fields
	private java.lang.Integer _adClientId;
	private java.lang.String _name;
	private java.lang.String _mlink;
	private java.lang.Integer _orderno;
	private java.lang.String _pico;
	private java.lang.String _mdisp;
	private java.lang.Integer _ownerId;
	private java.lang.Integer _modifierId;
	private java.util.Date _creationDate;
	private java.util.Date _modifiedDate;
	private java.lang.String _isActive;

	// constructors
	public BaseAdSysModel () {
		initialize();
	}

	/**
	 * Constructor for primary key
	 */
	public BaseAdSysModel (java.lang.Integer _id) {
		this.setId(_id);
		initialize();
	}

	/**
	 * Constructor for required fields
	 */
	public BaseAdSysModel (
		java.lang.Integer _id,
		java.lang.String _name,
		java.lang.String _isActive) {

		this.setId(_id);
		this.setName(_name);
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


//	/**
//	 * Return the value associated with the column: ORDERNO
//	 */
//	public java.lang.Integer getOrderno () {
//		return _orderno;
//	}
//
//	/**
//	 * Set the value related to the column: ORDERNO
//	 * @param _orderno the ORDERNO value
//	 */
//	public void setOrderno (java.lang.Integer _orderno) {
//		this._orderno = _orderno;
//	}


	/**
	 * Return the value associated with the column: MLINK
	 */
	public java.lang.String getMlink () {
		return _mlink;
	}

	/**
	 * Set the value related to the column: MLINK
	 * @param _mlink the MLINK value
	 */
	public void setMlink (java.lang.String _mlink) {
		this._mlink = _mlink;
	}

	/**
	 * Return the value associated with the column: PICO
	 */
	public java.lang.String getPico () {
		return _pico;
	}

	/**
	 * Set the value related to the column: PICO
	 * @param _pico the URL value
	 */
	public void setPico (java.lang.String _pico) {
		this._pico = _pico;
	}
	/**
	 * Return the value associated with the column: MDISP
	 */
	public java.lang.String getMdisp () {
		return _mdisp;
	}

	/**
	 * Set the value related to the column: MDISP
	 * @param _mdisp the MDISP value
	 */
	public void setMdisp (java.lang.String _mdisp) {
		this._mdisp = _mdisp;
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




	public boolean equals (Object obj) {
		if (null == obj) return false;
		if (!(obj instanceof nds.model.base.BaseAdSysModel)) return false;
		else {
			nds.model.base.BaseAdSysModel mObj = (nds.model.base.BaseAdSysModel) obj;
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

}
