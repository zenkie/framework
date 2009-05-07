/******************************************************************
*
*$RCSfile: Permissions.java,v $ $Revision: 1.2 $ $Author: Administrator $ $Date: 2005/12/18 14:06:17 $
*
*$Log: Permissions.java,v $
*Revision 1.2  2005/12/18 14:06:17  Administrator
*no message
*
*Revision 1.1.1.1  2005/03/15 11:23:24  Administrator
*init
*
*Revision 1.1.1.1  2002/11/29 02:50:35  yfzhu
*Active POS
*
*Revision 1.4  2002/01/04 01:43:22  yfzhu
*no message
*
*Revision 1.3  2001/11/14 23:30:30  yfzhu
*no message
*
*Revision 1.2  2001/11/07 20:58:47  yfzhu
*no message
*
*
********************************************************************/
package nds.security;
import nds.util.ColumnInterpretException;
import nds.util.ColumnInterpreter;

/**
 * The permission filed on a secured object is an integer, this class represents the meaning of
 * the integer.
 * <p>
 * The permission integer has 7 bits, as following:<br>
 * 6 5 4 3 2 1 0<br>
 * f r w d r w d<br>
 *<p>
 * The 6th bit means Frozen, if set, there's no permission for anyone to modify or delete, except
 * sys and admin user(s). 5th 4th and 3rd bits are for owner of the object, means Read, Write,
 * and Delete. While 2nd 1st and 0th are for other users else, means Read, Write, and Delete
 * repectively.
 *<p>
 * If user has no Write permission on the directory to which this secured object belongs, then any
 * modification on the object is disallowed, even if the object has 1st and 0th bits set.
 *<p>
 * For instance, 1101100b means object is frozen, all users can read, no one can write and delete
 * ( even though owner has delete bit set), 0111100b means owner has full control over object,
 * while others( at least have read permission on the very directory) can only read. 0111110b means
 * owner has full permission, others can read and modify( if he has also write permission on the
 * directory)
 *
 * @see SecuredObject#getPermissions
 */
public class Permissions  implements  java.io.Serializable {
    //内部变量
    private int permission;
    //内部常量
    public static final int FROZEN  =      64;//0100,0000=0x40
    public static final int OWNER_READ   = 32;//0010,0000=0x20
    public static final int OWNER_WRITE  = 16;//0001,0000=0x10
    public static final int OWNER_DELETE = 8; //0000,1000=0x08
    public static final int OTHER_READ   = 4; //0000,0100=0x04
    public static final int OTHER_WRITE  = 2; //0000,0010=0x02
    public static final int OTHER_DELETE = 1; //0000,0001=0x01


    public Permissions() {
        permission = 0;
    }
    public Permissions(int initPermission) {
        permission = initPermission;
    }
    public Permissions(Integer initPermission) {
        if( initPermission ==null) permission = 0;
        else permission = initPermission.intValue();
    }
    public Permissions(boolean frozen,boolean owner_read ,boolean owner_write, boolean owner_delete,boolean other_read,boolean other_write , boolean other_delete) {
        permission = 0;
        if(other_delete)
            this.setPermission(OTHER_DELETE);
        if(other_write)
            this.setPermission(OTHER_WRITE);
        if(other_read)
            this.setPermission(OTHER_READ);
        if(owner_delete)
            this.setPermission(OWNER_DELETE);
        if(owner_write)
            this.setPermission(OWNER_WRITE);
        if(owner_read)
            this.setPermission(OWNER_READ);
        if(frozen)
            this.setPermission(FROZEN);

    }

    public boolean isFrozen() {

        return (permission&FROZEN)==FROZEN;
    }
    public boolean canOwnerRead() {
        return (permission&OWNER_READ)==OWNER_READ;
    }
    public boolean canOwnerWrite() {
        return (permission&OWNER_WRITE)==OWNER_WRITE;
    }
    public boolean canOwnerDelete() {
        return (permission&OWNER_DELETE)==OWNER_DELETE;
    }
    public boolean canOtherRead() {
        return (permission&OTHER_READ)==OTHER_READ;
    }
    public boolean canOtherWrite() {
        return (permission&OTHER_WRITE)==OTHER_WRITE;
    }
    public boolean canOtherDelete() {
        return (permission&OTHER_DELETE)==OTHER_DELETE;
    }
    public int getPermissionValue() {
        return this.permission;
    }
    /**
     * Check whether this Permissions has specified permission bits set
     * @param perm the permission to be compared, can be one or combination of following values:
     *    Permissions.OTHER_READ, Permisisons.FROZEN, ...
     * @return if only one bit is not set, or some permission the <code>perm</code>
     *    has while this Permission has no, return false
     *    如果<code>perm</code>具有本Permission所没有的为1的bit,就返回false
     */
    public boolean hasPermissions(int perm) {
        return (permission | perm)==permission;
    }

    /**
     * Set one permission bit on original permission, other bits are preserved
     */
    public void setPermission(int perm) {
        permission |=perm ;
    }
    /**
     * Set one permission bit on original permission, other bits are preserved
     */
    public void clearPermission(int perm) {
        permission &= ~perm;
    }
    /**
     * @roseuid 3B7CD5E9019F
     */
    public int intValue() {
        return permission;
    }

    /**
     * @return String as "-rwdr-d", the chars in order are: frozen, owner read,
     * owner write, owner delete, other read, other write, other delete.
     * @roseuid 3B7CD582022D
     */
    public String toString() {
        char[] s= new char[7];
        s[0]=isFrozen()? 'f':'-';
        s[1]=canOwnerRead()?'r':'-';
        s[2]=canOwnerWrite()?'w':'-';
        s[3]=canOwnerDelete()?'d':'-';
        s[4]=canOtherRead()?'r':'-';
        s[5]=canOtherWrite()?'w':'-';
        s[6]=canOtherDelete()?'d':'-';
        return new String(s);
    }
    public boolean equals(Object obj) {
        if(( obj instanceof Permissions ) && ((Permissions)obj).permission== this.permission)
            return true;
        return false;
    }
    ////////////////////////implements ColumnInterpreter////////////////////////

    /**parse specified value(String or Integer) to string that can be easily interpreted by users
     * @throws ColumnInterpretException if input value is not valid
     */
    public String parseValue(Object value) throws ColumnInterpretException {
        int i= nds.util.Tools.getInt(value, -1);
        if( i == -1)
            throw new ColumnInterpretException("Invalid argument:"+value+", only String or Integer supported.");
        return (new Permissions(i)).toString();
    }

    /**
    * parse input string to column accepted int value
    * @param str like "frwdrwd"
    * @return Integer type
    * @throws ColumnInterpretException if input string is not valid
    */
    public Object getValue(String str) throws ColumnInterpretException {
        if( str.length() !=7)
            throw new ColumnInterpretException("Not a valid perimssion string");
        char[] s= str.toCharArray();
        int p=0;
        boolean hasError=false;
        for( int i=0;i< 7;i++) {
            switch(s[i]) {
                    case 'f':
                    if( i !=0)
                        hasError=true;
                    else
                        p += FROZEN;
                    break;
                    case 'r':
                    if( i == 1 )
                        p += OWNER_READ;
                    else if( i==4)
                        p+=OTHER_READ;
                    else
                        hasError=true;
                    break;
                    case 'w':
                    if( i==2)
                        p+=OWNER_WRITE;
                    else if(i==5)
                        p+=OTHER_WRITE;
                    else
                        hasError=true;
                    break;
                    case 'd':
                    if( i==3 )
                        p+=OWNER_DELETE;
                    else if( i==6)
                        p+=OTHER_DELETE;
                    else
                        hasError=true;
                    break;
            }
            if( hasError)
                throw new  ColumnInterpretException("Not a valid perimssion string: "+ str);
        }
        return new Integer(p);
    }

}
