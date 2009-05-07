/******************************************************************
*
*$RCSfile: JNDINames.java,v $ $Revision: 1.1.1.1 $ $Author: Administrator $ $Date: 2005/03/15 11:23:26 $
*
*$Log: JNDINames.java,v $
*Revision 1.1.1.1  2005/03/15 11:23:26  Administrator
*init
*
*Revision 1.4  2004/02/02 10:42:45  yfzhu
*<No Comment Entered>
*
*Revision 1.3  2003/03/30 08:11:40  yfzhu
*Updated before subtotal added
*
*Revision 1.2  2002/12/17 08:45:38  yfzhu
*no message
*
*Revision 1.1.1.1  2002/11/29 02:50:35  yfzhu
*Active POS
*
*Revision 1.12  2001/12/28 14:20:02  yfzhu
*no message
*
*Revision 1.11  2002/01/04 01:43:22  yfzhu
*no message
*
*Revision 1.10  2001/12/09 03:43:32  yfzhu
*no message
*
*Revision 1.9  2001/11/29 00:48:49  yfzhu
*no message
*
*Revision 1.8  2001/11/22 06:28:18  yfzhu
*no message
*
*Revision 1.7  2001/11/20 22:36:10  yfzhu
*no message
*
*Revision 1.6  2001/11/14 23:31:01  yfzhu
*no message
*
*Revision 1.5  2001/11/10 04:12:33  yfzhu
*no message
*
*Revision 1.3  2001/11/08 15:10:51  yfzhu
*First time compile OK
*
*Revision 1.2  2001/11/07 20:58:47  yfzhu
*no message
*
*
********************************************************************/

package nds.util;

/**
 * This class is the central location to store the internal
 * JNDI names of various entities. Any change here should
 * also be reflected in the deployment descriptors.
 */
public interface JNDINames {
    public static final String WEB_ROOT="/nds";
    //
    // JNDI names of EJB home objects
    //
    public static final String CLIENTCONTROLLER_EJBHOME =
        "nds/ejb/ClientController";

//    public static final String LOGGER = "/nds/Logger";

    public static final String MAILER_EJBHOME =
        "nds/ejb/mail/Mailer";

    public final static String MAILSESSION = "nds/mail";


/*    public static final String DATASOURCE =
        "/nds/jdbc/DataSource";
*/
    public static final String CONTEXTMANAGER_WEB =
        "/nds/web/ContextManager";
    // following is only a markup, not really an ejb home
    public final static String USER_EJBHOME = "nds/ejb/security/User";



    public final static String SERVER_URL="172.16.0.1:7001";
    public final static int TRUE = 1;
    public final static int FALSE = 2;

    //nmdemo added for sheet status
    public final static int STATUS_DRAFT = 1;
    public final static int STATUS_AUDITING = 3; //…Û∫À÷–
    public final static int STATUS_ROLLBACK = 5;
    public final static int STATUS_PERMIT = 4;// equals to STATUS_SUBMIT
    public final static int STATUS_SUBMIT = 2;// equals to STATUS_PERMIT
    public final static int STATUS_DELETE = 6;// equals to STATUS_PERMIT

}
