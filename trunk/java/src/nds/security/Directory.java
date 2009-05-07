/******************************************************************
*
*$RCSfile: Directory.java,v $ $Revision: 1.2 $ $Author: Administrator $ $Date: 2005/08/28 00:27:05 $
*
*$Log: Directory.java,v $
*Revision 1.2  2005/08/28 00:27:05  Administrator
*no message
*
*Revision 1.1.1.1  2005/03/15 11:23:24  Administrator
*init
*
*Revision 1.1  2004/02/02 10:42:17  yfzhu
*<No Comment Entered>
*
*Revision 1.3  2003/03/30 08:11:46  yfzhu
*Updated before subtotal added
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


public interface Directory {
  public final int READ=1;
  // write including read
  public final int WRITE=3;
  // submit including read, but not write
  public final int SUBMIT=5;
  // audit including read, but not write submit
  public final int AUDIT=9;
  //BATCH EXPORT 
  public final int EXPORT=17;
}