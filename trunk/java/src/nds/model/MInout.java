package nds.model;

import nds.model.base.BaseMInout;

/**
 * This is the object class that relates to the M_INOUT table.
 * Any customizations belong here.
 */
public class MInout extends BaseMInout {

/*[CONSTRUCTOR MARKER BEGIN]*/
	public MInout () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public MInout (java.lang.Integer _id) {
		super(_id);
	}

	/**
	 * Constructor for required fields
	 */
	public MInout (
		java.lang.Integer _id,
		java.lang.String _docno,
		java.lang.String _docType,
		java.lang.String _targetdocType,
		java.lang.String _movementType,
		java.lang.String _description,
		java.lang.String _deliveryRule,
		java.lang.String _freightCostRule,
		java.lang.String _deliveryviaRule,
		java.lang.String _priorityRule,
		java.lang.String _isActive) {

		super (
			_id,
			_docno,
			_docType,
			_targetdocType,
			_movementType,
			_description,
			_deliveryRule,
			_freightCostRule,
			_deliveryviaRule,
			_priorityRule,
			_isActive);
	}

/*[CONSTRUCTOR MARKER END]*/
}