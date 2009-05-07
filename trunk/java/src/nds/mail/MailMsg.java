package nds.mail;
import java.util.ArrayList;
/**
 * Holding mail msg to be sent. Especially useful when records to be deleted
 */
public class MailMsg implements java.io.Serializable {
    private ArrayList mailingList;
    private StringBuffer subject;
    private StringBuffer body;
    private String reference;
    public MailMsg() {
    }
    public MailMsg(ArrayList mailingList,StringBuffer subject,StringBuffer body,String reference){
        this.mailingList= mailingList;
        this.subject=subject;
        this.body=body;
        this.reference= reference;
    }
    public void setMailingList(ArrayList mailingList){
        this.mailingList= mailingList;
    }
    public void setSubject(StringBuffer subject){
        this.subject=subject;
    }
    public void setBody(StringBuffer body){
        this.body=body;
    }
    public void setReference(String ref){
        this.reference= ref;
    }
    public ArrayList getMailingList(){ return mailingList;}
    public StringBuffer getBody(){ return body;}
    /**
     * @param toHtml if true, body will be wrappered in HTML format
     * @return String as body
     */
    public StringBuffer getBody(boolean toHtml){
        StringBuffer sb= new StringBuffer(body.toString() );

        return sb;
    }
    public StringBuffer getSubject(){ return subject;}
    public String getReference(){ return reference;}
    public String toString(){
        return subject.toString() ;
    }
}