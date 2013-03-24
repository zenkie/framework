package nds.test;

import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;

import nds.control.ejb.ClientController;
import nds.control.ejb.ClientControllerHome;
import nds.control.event.DefaultWebEvent;
import nds.control.event.NDSEvent;
import nds.control.util.ValueHolder;

public class ClientControllerTestClient1 {
    private static final String ERROR_NULL_REMOTE = "Remote interface reference is null.  It must be created by calling one of the Home interface methods first.";
    private static final int MAX_OUTPUT_LINE_LENGTH = 100;
    private boolean logging = true;
    private ClientControllerHome clientControllerHome = null;
    private ClientController clientController = null;

    //Construct the EJB test client
    public ClientControllerTestClient1() {
        long startTime = 0;
        if (logging) {
            log("Initializing bean access.");
            startTime = System.currentTimeMillis();
        }

        try {
            //get naming context
            Context ctx = getInitialContext();

            //look up jndi name
            Object ref = ctx.lookup("/nds/ejb/ClientController");

            //cast to Home interface
            clientControllerHome = (ClientControllerHome) PortableRemoteObject.narrow(ref, ClientControllerHome.class);
            create();
            if (logging) {
                long endTime = System.currentTimeMillis();
                log("Succeeded initializing bean access.");
                log("Execution time: " + (endTime - startTime) + " ms.");
            }
        }
        catch(Exception e) {
            if (logging) {
                log("Failed initializing bean access.");
            }
            e.printStackTrace();
        }
    }

    private Context getInitialContext() throws Exception {
        String url = "t3://localhost:7001";
        String user = null;
        String password = null;
        Properties properties = null;
        try {
            properties = new Properties();
            properties.put(Context.INITIAL_CONTEXT_FACTORY, "weblogic.jndi.WLInitialContextFactory");
            properties.put(Context.PROVIDER_URL, url);
            if (user != null) {
                properties.put(Context.SECURITY_PRINCIPAL, user);
                properties.put(Context.SECURITY_CREDENTIALS, password == null ? "" : password);
            }

            return new InitialContext(properties);
        }
        catch(Exception e) {
            log("Unable to connect to WebLogic server at " + url);
            log("Please make sure that the server is running.");
            throw e;
        }
    }

    //----------------------------------------------------------------------------
    // Methods that use Home interface methods to generate a Remote interface reference
    //----------------------------------------------------------------------------

    public ClientController create() {
        long startTime = 0;
        if (logging) {
            log("Calling create()");
            startTime = System.currentTimeMillis();
        }
        try {
            clientController = clientControllerHome.create();
            if (logging) {
                long endTime = System.currentTimeMillis();
                log("Succeeded: create()");
                log("Execution time: " + (endTime - startTime) + " ms.");
            }
        }
        catch(Exception e) {
            if (logging) {
                log("Failed: create()");
            }
            e.printStackTrace();
        }

        if (logging) {
            log("Return value from create(): " + clientController + ".");
        }
        return clientController;
    }

    //----------------------------------------------------------------------------
    // Methods that use Remote interface methods to access data through the bean
    //----------------------------------------------------------------------------

    public ValueHolder handleEvent(DefaultWebEvent ese) {
        ValueHolder returnValue = null;
        if (clientController == null) {
            System.out.println("Error in handleEvent(): " + ERROR_NULL_REMOTE);
            return returnValue;
        }
        long startTime = 0;
        if (logging) {
            log("Calling handleEvent(" + ese + ")");
            startTime = System.currentTimeMillis();
        }

        try {
            returnValue = clientController.handleEvent(ese);
            if (logging) {
                long endTime = System.currentTimeMillis();
                log("Succeeded: handleEvent(" + ese + ")");
                log("Execution time: " + (endTime - startTime) + " ms.");
            }
        }
        catch(Exception e) {
            if (logging) {
                log("Failed: handleEvent(" + ese + ")");
            }
            e.printStackTrace();
        }

        if (logging) {
            log("Return value from handleEvent(" + ese + "): " + returnValue + ".");
        }
        return returnValue;
    }

    //----------------------------------------------------------------------------
    // Utility Methods
    //----------------------------------------------------------------------------

    private void log(String message) {
        if (message == null) {
            System.out.println("-- null");
            return ;
        }
        if (message.length() > MAX_OUTPUT_LINE_LENGTH) {
            System.out.println("-- " + message.substring(0, MAX_OUTPUT_LINE_LENGTH) + " ...");
        }
        else {
            System.out.println("-- " + message);
        }
    }
    //Main method

    public static void main(String[] args) {
        ClientControllerTestClient1 client = new ClientControllerTestClient1();
        // Use the client object to call one of the Home interface wrappers
        // above, to create a Remote interface reference to the bean.
        // If the return value is of the Remote interface type, you can use it
        // to access the remote interface methods.  You can also just use the
        // client object to call the Remote interface wrappers.
    }
}