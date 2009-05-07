package nds.model;

import nds.model.base.BaseEmployee;

/**
 * This is the object class that relates to the EMPLOYEE table.
 * Any customizations belong here.
 */
public class Employee extends BaseEmployee {

/*[CONSTRUCTOR MARKER BEGIN]*/
	public Employee () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public Employee (java.lang.Integer _id) {
		super(_id);
	}

	/**
	 * Constructor for required fields
	 */
	public Employee (
		java.lang.Integer _id,
		java.lang.Integer _departmentid,
		java.lang.Integer _userid,
		java.lang.String _no,
		java.lang.Integer _gender,
		java.lang.String _name) {

		super (
			_id,
			_departmentid,
			_userid,
			_no,
			_gender,
			_name);
	}

/*[CONSTRUCTOR MARKER END]*/
}