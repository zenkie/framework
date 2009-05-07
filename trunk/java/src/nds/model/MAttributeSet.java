package nds.model;

import nds.model.base.BaseMAttributeSet;

/**
 * This is the object class that relates to the M_ATTRIBUTESET table.
 * Any customizations belong here.
 */
public class MAttributeSet extends BaseMAttributeSet {

/*[CONSTRUCTOR MARKER BEGIN]*/
	public MAttributeSet () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public MAttributeSet (java.lang.Integer _id) {
		super(_id);
	}

	/**
	 * Constructor for required fields
	 */
	public MAttributeSet (
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

		super (
			_id,
			_name,
			_isSerno,
			_isLot,
			_isGuaranteeDate,
			_guaranteedays,
			_isInstanceAttribute,
			_mandatoryType,
			_isGuaranteeDateMandatory,
			_isLotmandatory,
			_isSernomandatory,
			_isActive);
	}

/*[CONSTRUCTOR MARKER END]*/
}