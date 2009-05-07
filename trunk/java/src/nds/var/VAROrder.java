package nds.var;

public class VAROrder {
  private int id;
  private String docno;
  private double amt;
  private int c_bpartner_id;
  private String c_bpartner_name;
  private int payer_id;
  private String pname;
  private String pemail;
  private String ptruename;
  private int status;
  private String state; 
  private String doctype;

public double getAmt() {
	return amt;
}
public void setAmt(double amt) {
	this.amt = amt;
}
public int getC_bpartner_id() {
	return c_bpartner_id;
}
public void setC_bpartner_id(int c_bpartner_id) {
	this.c_bpartner_id = c_bpartner_id;
}
public String getC_bpartner_name() {
	return c_bpartner_name;
}
public void setC_bpartner_name(String c_bpartner_name) {
	this.c_bpartner_name = c_bpartner_name;
}
public String getDocno() {
	return docno;
}
public void setDocno(String docno) {
	this.docno = docno;
}
public int getId() {
	return id;
}
public void setId(int id) {
	this.id = id;
}
public int getPayer_id() {
	return payer_id;
}
public void setPayer_id(int payer_id) {
	this.payer_id = payer_id;
}
public String getPemail() {
	return pemail;
}
public void setPemail(String pemail) {
	this.pemail = pemail;
}
public String getPname() {
	return pname;
}
public void setPname(String pname) {
	this.pname = pname;
}
public String getPtruename() {
	return ptruename;
}
public void setPtruename(String ptruename) {
	this.ptruename = ptruename;
}
public String getState() {
	return state;
}
public void setState(String state) {
	this.state = state;
}
public int getStatus() {
	return status;
}
public void setStatus(int status) {
	this.status = status;
}
public String getDoctype() {
	return doctype;
}
public void setDoctype(String doctype) {
	this.doctype = doctype;
}

}
