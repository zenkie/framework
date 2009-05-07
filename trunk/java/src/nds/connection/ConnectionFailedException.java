package nds.connection;


/** exception thrown when anything goes wrong with connection-related stuff. */
public class ConnectionFailedException extends ConnectionException {
    public ConnectionFailedException() {
        super();
    }

    public ConnectionFailedException(String par1) {
        super(par1);
    }
    public ConnectionFailedException(String par1,Exception e) {
        super(par1,e);
    }

}
