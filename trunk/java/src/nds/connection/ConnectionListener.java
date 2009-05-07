//Source file: F:\\work\\sms\\src\\nds\\connection\\ConnectionListener.java

package nds.connection;

import java.util.EventListener;

/** listener that should be implemented to listen for any connection events */
public interface ConnectionListener extends EventListener
{

    /**
     * This event is fired when a connection is fully established.  If a connection is not established due to some sort of
     * error (ie. unknown host name or just unable to connect), then this event will NOT be fired.
     */
    public void connectionEstablished(ConnectionEvent e);

    /**
     * <p>The event is fired when the connection is closed normally or abnormally.</p>
     * <p>The status code will let you know how the connection is closed. If the
     * connection is closed abnormally, a error message will go along with it</p>
     */
    public void connectionClosed(ConnectionEvent e);
}
