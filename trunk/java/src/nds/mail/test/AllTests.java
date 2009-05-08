package nds.mail.test;

import java.util.Properties;

import javax.mail.Session;
import javax.naming.Context;
import javax.naming.InitialContext;

import junit.framework.Test;
import junit.framework.TestSuite;
import nds.mail.NotificationManager;
import nds.schema.TableManager;
public class AllTests {
	public static void main(String[] args) throws Exception {
		junit.textui.TestRunner.run(suite());
	}

	public static Test suite() throws Exception {
        //nds.query.test.QueryEngineExt qe= new nds.query.test.QueryEngineExt("aa");
        Properties props= new Properties();
        //props.put("Connection", qe.getConnection()  );
        props.setProperty("mail.host", "localhost");
        props.setProperty("mail.transport.protocol", "smtp");
        props.setProperty("mail.user", "root@aic.com");
        props.setProperty("mail.smtp.password", "abc123");
        props.setProperty("mail.from", "root@aic.com");
        props.setProperty("mail.debug", "true");
        props.setProperty("weburl", "http://localhost:7001/nds");

        props.setProperty("shouldNotify", "true");
        props.put("MailSession", Session.getDefaultInstance(props,null));
        NotificationManager.getInstance().init(props);

        props=new Properties();
        props.setProperty("directory","file:/e:/aic/tables/test");
        props.setProperty("defaultTypeConverter","nds.schema.OracleTypeConverter" );
        TableManager.getInstance().init(props);



		TestSuite suite= new TestSuite("NDS util tests");
		suite.addTestSuite(nds.mail.test.MailRobotSessionTest.class);
		return suite;
	}
    private static Context getInitialContext() throws Exception {
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
            System.err.println("Unable to connect to WebLogic server at " + url);
            System.err.println("Please make sure that the server is running.");
            throw e;
        }
    }

}