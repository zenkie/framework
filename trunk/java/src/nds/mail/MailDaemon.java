
package nds.mail;

import java.util.ArrayList;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.URLName;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import nds.log.Logger;
import nds.log.LoggerManager;
import nds.util.ObjectQueue;

public class MailDaemon implements Runnable{
    private static Logger logger= LoggerManager.getInstance().getLogger(MailDaemon.class.getName());
    private ObjectQueue queue;
    private Properties props;
    private  Transport transport = null;
    private  Session session =null;// Session.getDefaultInstance(props, null);

    public MailDaemon(ObjectQueue queue, Properties props){
        this.props= props;
		this.queue=queue;
        if (session==null) session=Session.getDefaultInstance(props);
    }
    /**
     * @param mailingList elements of String (email address such as "eforum@sina.com")
     * @param briefMsg as subject
     * @param detailMsg as body
     * @param referecne will insert into mail referecne header so can link to each other
     */
    private void MailTo(ArrayList mailingList, StringBuffer briefMsg, StringBuffer detailMsg, String referecne) throws Exception{
        if( mailingList.size() ==0){
            logger.debug("Can not find any reciever for mail subject:"+ briefMsg+", not mail sent") ;
            return;
        }
        //InitialContext ic = new InitialContext();
        checkMailSession();
        //(Session)PortableRemoteObject.narrow( context.lookup(JNDINames.MAILSESSION), javax.mail.Session.class);
        MimeMessage msg = new MimeMessage(session);
        msg.setFrom();
        String contentType = "text/plain;;charset=GB2312";
        msg.setRecipients(Message.RecipientType.BCC,
                                            InternetAddress.parse(getToAddresses(mailingList), false));
        msg.setSubject(briefMsg.toString(), "GB2312");
        msg.setHeader("X-Mailer", "ActiveMailer 1.0");
        msg.setHeader("References",referecne );

        msg.setSentDate(new java.util.Date());
        MimeBodyPart mbp = new MimeBodyPart();
        mbp.setText(detailMsg.toString(), "GB2312");

        Multipart mp = new MimeMultipart();
        mp.addBodyPart(mbp);
        msg.setContent(mp);


        Transport.send(msg);
    }
    private String getToAddresses(ArrayList mailingList){
        StringBuffer sb= new StringBuffer();
        for( int i=0; i< mailingList.size();i++){
            sb.append(mailingList.get(i)+",");
        }
        return sb.toString();
    }
    private void checkMailSession() throws Exception{
        if(transport == null || !transport.isConnected())
        {
            URLName transportURLName = getServiceURLName(props.getProperty("mail.transport.protocol", "smtp"), props);
            logger.debug("TransportURLName:"+ transportURLName);
            try{
                transport = session.getTransport(transportURLName);
            }catch(Exception e){
                // reconnect using current props
                session=Session.getDefaultInstance(props);
                transport = session.getTransport(transportURLName);
            }
            transport.connect(transportURLName.getHost(), transportURLName.getPort(), transportURLName.getUsername(), transportURLName.getPassword());
            logger.debug("Mail transport connected to " +transportURLName.getHost()+ " port "+
                         transportURLName.getPort() + " user "+ transportURLName.getUsername()+ " password "+transportURLName.getPassword() );
        }
    }
    private URLName getServiceURLName(String serviceName, Properties props)throws Exception
    {
        int port = -1;
        try
        {
            String portString = props.getProperty(String.valueOf(String.valueOf((new StringBuffer("mail.")).append(serviceName).append(".port"))), "-1");
            port = Integer.parseInt(portString);
        }
        catch(Exception exception) { }
        URLName url = new URLName(serviceName, props.getProperty(String.valueOf(String.valueOf((new StringBuffer("mail.")).append(serviceName).append(".host")))), port, props.getProperty(String.valueOf(String.valueOf((new StringBuffer("mail.")).append(serviceName).append(".folder")))), props.getProperty(String.valueOf(String.valueOf((new StringBuffer("mail.")).append(serviceName).append(".user")))), props.getProperty(String.valueOf(String.valueOf((new StringBuffer("mail.")).append(serviceName).append(".password")))));
        return url;
    }

    public void run(){
        logger.debug("Start Mail Daemon");
           while ( queue.hasMoreElements() ){
                MailMsg msg=(MailMsg)queue.nextElement() ;
                try{
                    MailTo(msg.getMailingList(), msg.getSubject(),msg.getBody(),msg.getReference() );
                }catch(Exception e){
                    logger.debug("Error mail " + msg +":"+ e);
                }
            }
        logger.debug("Mail Daemon stopped");
    }
}
