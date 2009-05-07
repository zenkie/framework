/******************************************************************
*
*$RCSfile: DirectoryNotFoundException.java,v $ $Revision: 1.1.1.1 $ $Author: Administrator $ $Date: 2005/03/15 11:23:24 $
*
*$Log: DirectoryNotFoundException.java,v $
*Revision 1.1.1.1  2005/03/15 11:23:24  Administrator
*init
*
*Revision 1.1.1.1  2002/11/29 02:50:35  yfzhu
*Active POS
*
*Revision 1.2  2001/11/07 20:58:47  yfzhu
*no message
*
*
********************************************************************/
package nds.security;

/**
 * Title:        进销存系统
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      GiniusGift
 * @author
 * @version 1.0
 */

public class UserNotFoundException extends nds.util.NDSException {
  public UserNotFoundException() {
    super();
  }
  public UserNotFoundException(String errorMesg) {
    //this.errMesg = errorMesg;
    super(errorMesg);
  }
    public UserNotFoundException(String s, Exception ex) {
    //this.errMesg = errorMesg;
    super(s, ex);
  }
}