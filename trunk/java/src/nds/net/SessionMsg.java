package nds.net;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import nds.util.Sequences;

import com.echomine.jabber.JabberChatMessage;
/**
 * 会话中传递的消息
 */
public class SessionMsg implements java.io.Serializable {
    private Properties params;
    private static String LINE_SEPARATOR=" ";
    private transient JabberChatMessage origionalJabberMessage=null;// them msg that this session msg is created from
    /**
     * @param prefix will be used as message id' prefix, such as "NDSJava_"
     */
    public SessionMsg(String prefix) {
        params= new Properties();
        int id;
        id= Sequences.getNextID("SessionMsg");
        params.setProperty("ID",prefix+id); //"NDSJava_" + id);
    }
    public void addParam(String name, String value){
        if ( value!=null ) params.setProperty(name,value);
    }
    public void setType(String type){
        params.put("CommandType", type);
    }
    public String getType(){
        return params.getProperty("CommandType", "");
    }
    public Enumeration getParamNames(){
        return params.keys() ;
    }
    public String getSubject(){
        return params.getProperty("Subject", params.getProperty("CommandType", ""));
    }
    public String getBody(){
        return params.getProperty("Body", getID());//toString());
    }
    public String getParentMsgID(){
        return params.getProperty("ParentMsgID", "");
    }
    public String getID(){
        return params.getProperty("ID");
    }
    public void setBody(String body){
        addParam("Body", body);
    }
    public void setParentMsgID(String pid){
        addParam("ParentMsgID", pid);
    }
    public void setSubject(String subject){
        addParam("Subject", subject);
    }
    public void setThreadID(String threadID){
        addParam("ThreadID", threadID);
    }
    public String getThreadID(){
        return params.getProperty("ThreadID");
    }
    public void setTo( String to){
        addParam("To", to);
    }
    public String getTo(){
        return params.getProperty("To");
    }
    public String getParam(String name){
        return params.getProperty(name);
    }
    public String toString(){
        return getDetailInfo(params);
    }

    public void setOrigionalJabberMessage(JabberChatMessage jcm){
        origionalJabberMessage= jcm;
    }
    public JabberChatMessage getOrigionalJabberMessage(){
        return origionalJabberMessage;
    }
    /**
     * Get each property value in Map, and concatenate to a readable string
     */
    public static String getDetailInfo(Map content) {
        StringBuffer s=new StringBuffer("["+ LINE_SEPARATOR);
        if( content==null)
            return "Empty";
        for( Iterator it= content.keySet().iterator(); it.hasNext();) {
            Object key= it.next();
            Object value= content.get(key);
            s.append(key+" = "+ value+ LINE_SEPARATOR);
        }
        s.append("]");
        return s.toString();
    }

}