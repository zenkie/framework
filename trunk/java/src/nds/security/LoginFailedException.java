/******************************************************************
*
*$RCSfile: LoginFailedException.java,v $ $Revision: 1.1.1.1 $ $Author: Administrator $ $Date: 2005/03/15 11:23:24 $
*
*$Log: LoginFailedException.java,v $
*Revision 1.1.1.1  2005/03/15 11:23:24  Administrator
*init
*
*Revision 1.1.1.1  2002/11/29 02:50:35  yfzhu
*Active POS
*
*Revision 1.1  2001/12/28 14:20:50  yfzhu
*no message
*
*Revision 1.2  2001/11/07 20:58:47  yfzhu
*no message
*
*
********************************************************************/
package nds.security;

/**
 * Title:        NDS Project
 * Description:  San gao shui yuan, mu xiang ren jia
 * Copyright:    Copyright (c) 2001
 * Company:      aic
 * @author yfzhu
 * @version 1.0
 */

public class LoginFailedException  extends NDSSecurityException{

  public LoginFailedException() {
    super();
  }
  public LoginFailedException(String errorMesg) {
    //this.errMesg = errorMesg;
    super(errorMesg);
  }
    public LoginFailedException(String s, Exception ex) {
    //this.errMesg = errorMesg;
    super(s, ex);
  }

}
