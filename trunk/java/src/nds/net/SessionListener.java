package nds.net;

/**
 * 在SessionController 中侦听网络请求的处理器
 */
public interface SessionListener {
    /**
     * @return Listener ID that to identify each listener instance, even
     * they are of same class
     */
    public String getID();
    /**
     * Stop the listener
     */
    public void kill();
    /**
     * Start listener on SessionController, normal steps will include registering
     * in SessionController
     */
    public void start();
    /**
     * Handle message from SessionController.
     * @param msg the msg that will be handle on
     */
    public void onMessage(SessionMsg msg);
    public void setController( SessionController controller);
    /**
     * @return the SessionMsg type that will be handle on.
     * SessionMsg type is set as a parameter named "CommandType"
     */
    public String getType();

}