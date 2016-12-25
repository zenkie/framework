/*
 * Agile Control Technologies Ltd,. CO.
 * http://www.agileControl.com
 */
package nds.util;



import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.util.*;

import nds.schema.TableManager;

import java.util.*;
import java.util.regex.*;


/**
 * Holds messages of various locale
 * 
 * @author yfzhu@agilecontrol.com
 * @see org.apache.struts.util.MessageResources
 */ 

public class MessagesHolder { 
	private static final Log logger = LogFactory.getLog(MessagesHolder.class);
	/**
	 * Hold for only one instance, so the whole application should have only one instance as static
	 * If you want to have more than one MessagesHolders, record the other ones in specific objects
	 */
	private static MessagesHolder instance=null;
	
	private MessageResources resources=null;
	
	//sample message from db: @this-is-error@:2993
	private static Pattern p=Pattern.compile("(@([\\p{Alpha}\\p{Digit}-_\\.]+)@)");
	
	private boolean isInitialized=false;
	public MessagesHolder(){}

	public synchronized void init(String config){
		MessageResourcesFactory  factory= new PropertyMessageResourcesFactory();
		resources=factory.createResources(config);
		resources.setReturnNull(true); // we will control null ourselves
		isInitialized=true;
		logger.debug("MessagesHolder initialized using "+ config);
	}
	public boolean isInitialized(){
		return isInitialized;
	}
	/**
	 * Tranlate message which contains key string wrappered by "@"
	 * such as "@your-mobile@:13939239", should be translate to "ÊÖ»ú:13939239" when using CHINA locale
	 * if @xxx@ xxx is not key in locale, then should remain original one
	 * @param msg 
	 * @param locale
	 * @return 
	 */
	public String translateMessage( String msg,Locale locale){
		if(msg==null) return null;
		Matcher m=p.matcher(msg);
		StringBuffer sb = new StringBuffer();
		int group=1;
		String r;
		while ( m.find()) {
			r=m.group(group);
			r=r.substring(1,r.length()-1);
			m.appendReplacement(sb, getMessage(locale, r, r));
		}
		m.appendTail(sb);
		return sb.toString();		 
	}
	
	public String getMessage(Locale locale, String key){
		String m=resources.getMessage(locale,key);
		logger.debug("getMessage m->"+m);
		if(m==null) m=key;//m= ("?" + messageKey(locale, key) + "?");
		return m;
	}
	
	
	public String getMessage4(Locale locale, String desc){
		logger.debug("getMessage4 locale->"+locale);
		logger.debug("getMessage4 desc->"+desc);
		if(TableManager.getInstance().getDefaultLocale().hashCode()==locale.hashCode())
	    	return desc;
		else
			return getMessage(locale,desc);
	}
	
	public String getMessage3(Locale locale, String key,String desc){
		logger.debug("getMessage3 locale->"+locale);
		logger.debug("getMessage3 desc->"+desc);
		String m=null;
		if(desc!=null)
			m=resources.getMessage(locale,desc);
		else if(m==null)
			m=resources.getMessage(locale,key);
		if(m==null&&desc!=null) 
			m= ("?" + messageKey(locale, desc) + "?");
		else if(m==null)
			m= ("?" + messageKey(locale, key) + "?");
		logger.debug("getMessage3 m->"+m);
		return m;
	}
	/**
	 * If message for key not found, will try key2
	 * @param locale
	 * @param key
	 * @param key2
	 * @return
	 */
	public String getMessage2(Locale locale, String key, String key2,String desc){
		logger.debug("getMessage2 locale->"+locale);
		logger.debug("getMessage2 desc->"+desc);
//		String m=resources.getMessage(locale,key);
//		if(m==null)m=resources.getMessage(locale,key2);
//		if(m==null&&desc!=null)m=resources.getMessage(locale,desc);
//		if(m==null) m= ("?" + messageKey(locale, desc) + "?");
		String m=null;
		
		if(desc!=null)
			m=resources.getMessage(locale,desc);
		else if(m==null&&key2!=null)
			m=resources.getMessage(locale,key2);
		else if(m==null)
			m=resources.getMessage(locale,key);
		
		if(m==null&&desc!=null) 
			m= ("?" + messageKey(locale, desc) + "?");
		else if(m==null&&key2!=null)
			m= ("?" + messageKey(locale, key2) + "?");
		else if(m==null)
			m= ("?" + messageKey(locale, key) + "?");
		
		logger.debug("getMessage2 m->"+m);
		return m;
	}
	
	public String getMessage2(Locale locale, String key, String key2){
		
		return getMessage2( locale, key,key2,null);
	}
	/**
	 * If message for key not found, defaultValue will be used
	 * @param locale
	 * @param key
	 * @param defaultValue
	 * @return
	 */
	public String getMessage(Locale locale, String key, String defaultValue){
		String m=resources.getMessage(locale,key);
		if(m==null) m= defaultValue;
		return m;
	}
    /**
     * Compute and return a key to be used in caching information by a Locale.
     * <strong>NOTE</strong> - The locale key for the default Locale in our
     * environment is a zero length String.
     *
     * @param locale The locale for which a key is desired
     */
    protected String localeKey(Locale locale) {
        return (locale == null) ? "" : locale.toString();
    }

    /**
     * Compute and return a key to be used in caching information
     * by Locale and message key.
     *
     * @param locale The Locale for which this format key is calculated
     * @param key The message key for which this format key is calculated
     */
    protected String messageKey(Locale locale, String key) {

        return (localeKey(locale) + "." + key);

    }

    /**
     * Compute and return a key to be used in caching information
     * by locale key and message key.
     *
     * @param localeKey The locale key for which this cache key is calculated
     * @param key The message key for which this cache key is calculated
     */
    protected String messageKey(String localeKey, String key) {

        return (localeKey + "." + key);

    }
	
	public static MessagesHolder getInstance(){
		if( instance==null){
			instance=new MessagesHolder();
		}
		return instance;
	}
	
}
