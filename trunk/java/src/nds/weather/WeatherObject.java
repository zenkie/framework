package nds.weather;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import org.mozilla.javascript.Scriptable;
/**
 * Wrapper weather information
 */
public class WeatherObject {
    private final static SimpleDateFormat dateTimeSecondsFormatter=new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
    private final static String LINE_SEPARATOR= ",";
    private Properties props;
    public WeatherObject() {
        props=new Properties();
    }
    public void setProperty(String name, Object value){
        props.put(name,value);
    }
    public String getProperty(String name){
        Object o= props.get(name);
        return (o==Scriptable.NOT_FOUND?"":o+"");
    }
    public String getProperty(String name, String defaultValue){
        Object o= props.get(name);
        return ((o==Scriptable.NOT_FOUND || o==null)?defaultValue:o+"");

    }
    public int getPropertyInt(String name, int defaultInt){
        String s=getProperty(name,"");
        if ("".equals(s) )return defaultInt;
        return   (int)Double.parseDouble(s);
    }
    public java.sql.Date getProperyDate(String name, java.sql.Date defaultDate){
        try{
            String s=getProperty(name,"");
            if("".equals(s)) return defaultDate;
            java.sql.Date d=
            new java.sql.Date(dateTimeSecondsFormatter.parse(s).getTime());
            return d;
        }catch(Exception e){
            return null;
        }
    }
    public String toString(){
        return getDetailInfo(props);
    }
    private String getDetailInfo(Map content){

        StringBuffer s=new StringBuffer("["+ LINE_SEPARATOR);
        if( content==null)
            return "Empty";
        for( Iterator it= content.keySet().iterator(); it.hasNext();) {
            Object key= it.next();
            Object value= content.get(key);
            if ( value.getClass().isArray() ){
                try{
                value= nds.util.Tools.toString( (Object[])value);
                }catch(Exception e){
                    value="array(???)";
                }
            }
            s.append(key+" = "+ value+ LINE_SEPARATOR);
        }
        s.append("]");
        return s.toString();
    }
}