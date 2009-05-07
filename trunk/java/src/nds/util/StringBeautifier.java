/*
 * Agile Control Technologies Ltd,. CO.
 * http://www.agileControl.com
 */
package nds.util;

/**
	 * Beautify string, for instance: 
	 * AD_CLIENT_ID -> AdClientId , replace _, and set first char to uppercase
	 * AD_CLIENTID ->  AdClientId , if id is found in the last , will set to uppercase
 * @author yfzhu@agilecontrol.com
 */

public class StringBeautifier {
	// in lower case, $ means start with, ^means end with
	private final static String[] words=new String[]{
	"$id", "^id", "client", "owner","modifier","date", "$is", "$has", "name","rule","type","amt",
	"cost", "status","acct" ,"category","item","bank","value","region","attribute","combination",
	"current","instance"
	
	};
	
	/**
	 * Beautify string, for instance: 
	 * AD_CLIENT_ID -> AdClientId , replace _, and set first char to uppercase
	 * OWNERID    -> OwnerId, owner is a word, so 'O' and 'I' will be upper case
	 * AD_CLIENTID ->  AdClientId , if id is found in the last , will set to uppercase
	 * 
	 * @param s
	 * @return
	 */
	public static String beautify(String s){
		if( s==null) return null;
		s=s.toLowerCase();
		for( int i=0;i< words.length;i++){
			if(words[i].indexOf("$")> -1){
				s= replaceFirst(s, words[i].substring(1));
			}else if(words[i].indexOf("^")> -1){
				s= replaceLast(s, words[i].substring(1));
			}else
			s=StringUtils.replace(s, words[i], "_"+words[i]+"_");
		}
		char[] c=s.toCharArray();
		StringBuffer sb=new StringBuffer();
		boolean needUpperCase=true;
		for(int i=0;i<c.length;i++){
			if(c[i]=='_'){
				needUpperCase=true;
				continue;
			}
			if(needUpperCase) sb.append( String.valueOf(c[i]).toUpperCase());
			else sb.append( String.valueOf(c[i]).toLowerCase());
			needUpperCase =false;
		}
		
		return sb.toString();
	}
	/**
	 * Replace s of the first string to first+"_", for instance
	 * s="isvendor", first="is", then return "is_vendor"
	 * @param s
	 * @param firstWord
	 * @return
	 */
	private static String replaceFirst(String s, String firstWord){
		if( s.startsWith(firstWord)){
			return firstWord+"_" + s.substring(firstWord.length());
		}
		return s;
	}
	private static String replaceLast(String s, String word){
		if( s.endsWith(word)){
			return s.substring(0,s.length() - word.length())+ "_" + word; 
		}
		return s;
	}
	/**
	 * If last 2 chars is "ID", strip them, for instance
	 * AdClientID -> AdClient
	 * @param s
	 * @return
	 */
	public static String stripLastID(String s){
		if( s==null) return null;
		if( s.toLowerCase().endsWith("id")){
			s= s.substring( 0, s.length()-2);
		}
		return s;
	}	
	
	public static void main(String[] args){
		String[] tests=new String[]{
			"AD_CLIENT_ID","clientid","OWNERID","isuser",
			"FREIGHTCOSTRULE","FREIGHTAMT","M_SHIPPER_ID",
			"DOCNO", "supervisorid","ihasone","hasone" ,"CValIdCombination"
		};
		for(int i= 0;i< tests.length;i++){
			System.out.println( tests[i]+ " ->" + beautify(tests[i]) +" ->" + stripLastID(beautify(tests[i])) );
		}
	}
}
