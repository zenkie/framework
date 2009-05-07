/******************************************************************
*
*$RCSfile: User.java,v $ $Revision: 1.4 $ $Author: Administrator $ $Date: 2006/03/13 01:13:23 $
*
*$Log: User.java,v $
*Revision 1.4  2006/03/13 01:13:23  Administrator
*no message
*
*Revision 1.3  2005/08/28 00:27:05  Administrator
*no message
*
*Revision 1.2  2005/04/27 03:25:35  Administrator
*no message
*
*Revision 1.1.1.1  2005/03/15 11:23:24  Administrator
*init
*
*Revision 1.1  2004/02/02 10:42:19  yfzhu
*<No Comment Entered>
*
*Revision 1.2  2002/12/17 08:45:37  yfzhu
*no message
*
*Revision 1.2  2001/11/07 20:58:47  yfzhu
*no message
*
*
********************************************************************/
package nds.security;


public class User {

  public Integer id;
  public String name;
  public String passwordHash;
  public int isEnabled;
  public boolean isActive;
  public int isEmployee;
  public int isAdmin;
  public String description;
  public String clientDomain; // ad_client_id.domain
  public int adClientId;
  public int adOrgId;
  public java.util.Locale locale;
  public String clientDomainName;
  public String email; // 
  public Integer getId() {
    return id;
  }
  public void setId(Integer id) {
    this.id = id;
  }
  public String getNameWithDomain(){
  	return name +"@"+ clientDomain;
  }
  public String getClientDomainName(){
	  return clientDomainName;
  }
/**
 * @return Returns the clientDomain.
 */
public String getClientDomain() {
	return clientDomain;
}
/**
 * @param clientDomain The clientDomain to set.
 */
public void setClientDomain(String clientDomain) {
	this.clientDomain = clientDomain;
}
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }
  public String getPasswordHash() {
    return passwordHash;
  }
  public void setPasswordHash(String passwordHash) {
    this.passwordHash = passwordHash;
  }
  public int getIsEnabled() {
    return isEnabled;
  }
  public void setIsEnabled(int isEnabled) {
    this.isEnabled = isEnabled;
  }
  public int getIsEmployee() {
    return isEmployee;
  }
  public void setIsEmployee(int isEmployee) {
    this.isEmployee = isEmployee;
  }
  public int getIsAdmin() {
    return isAdmin;
  }
  public void setIsAdmin(int isAdmin) {
    this.isAdmin = isAdmin;
  }
  public String getDescription() {
    return description;
  }
  public void setDescription(String description) {
    this.description = description;
  }
  public String toString(){
	  return (this.id==null? "": id.toString());
  }
/**
 * @return Returns the isActive.
 */
public boolean isActive() {
	return isActive;
}
/**
 * @param isActive The isActive to set.
 */
public void setActive(boolean isActive) {
	this.isActive = isActive;
}

}