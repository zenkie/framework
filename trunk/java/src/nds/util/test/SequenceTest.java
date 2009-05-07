/******************************************************************
*
*$RCSfile: SequenceTest.java,v $ $Revision: 1.1.1.1 $ $Author: Administrator $ $Date: 2005/03/15 11:23:27 $
*
*$Log: SequenceTest.java,v $
*Revision 1.1.1.1  2005/03/15 11:23:27  Administrator
*init
*
*Revision 1.1.1.1  2002/11/29 02:50:35  yfzhu
*Active POS
*
*Revision 1.2  2001/11/07 20:58:48  yfzhu
*no message
*
*
********************************************************************/
package nds.util.test;

import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;

import nds.util.ejb.SbSequenceRemote;
import nds.util.ejb.SbSequenceRemoteHome;

public class SequenceTest {
    private static final String ERROR_NULL_REMOTE = "Remote interface reference is null.  It must be created by calling one of the Home interface methods first.";
    private static final int MAX_OUTPUT_LINE_LENGTH = 100;
    private boolean logging = true;
    private SbSequenceRemoteHome sequenceHome = null;
    private SbSequenceRemote sequence = null;

    /**Construct the EJB test client*/
    public SequenceTest() {
        long startTime = 0;
        if (logging) {
            log("Initializing bean access.");
            startTime = System.currentTimeMillis();
        }

        try {
            //get naming context
            Context ctx = getInitialContext();

            //look up jndi name
            Object ref = ctx.lookup("nds/ejb/SbSequenceRemote");

            //cast to Home interface
            sequenceHome = (SbSequenceRemoteHome) PortableRemoteObject.narrow(ref, SbSequenceRemoteHome.class);
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

    public SbSequenceRemote create() {
        long startTime = 0;
        if (logging) {
            log("Calling create()");
            startTime = System.currentTimeMillis();
        }
        try {
            sequence = sequenceHome.create();
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
            log("Return value from create(): " + sequence + ".");
        }
        return sequence;
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
    /**Main method*/

    public static void main(String[] args) throws Exception{
        SequenceTest tester = new SequenceTest();
        SbSequenceRemote client=tester.create();
        int s=client.getNextNumberInSequence("bbbbbbbbbbbbbbbbbbbb");
/*        int k=client.getNextNumberInSequence("tt2");
        int dif= k -s;
        for( int i=0;i< 0;i++){
            s=client.getNextNumberInSequence("test");
            k=client.getNextNumberInSequence("test2");
            if( k-s != dif) {
                System.out.println("k="+k+",s="+s+", curdif="+ (k-s)+" ,while the orgdif="+ dif);
                throw new Error( "Error in testing");
            }
        }*/
    }
}