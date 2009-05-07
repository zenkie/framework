package nds.connection;

import nds.util.NDSException;
/** exception thrown when anything goes wrong with connection-related stuff. */
public class ConnectionException extends NDSException {
    public ConnectionException() {
        super();
    }

    public ConnectionException(String par1) {
        super(par1);
    }
    public ConnectionException(String par1,Exception e) {
        super(par1,e);
    }

}
