/******************************************************************
*
*$RCSfile: Tools.java,v $ $Revision: 1.13 $ $Author: Administrator $ $Date: 2006/03/13 01:12:11 $
********************************************************************/
package nds.util;

import java.io.*;
import java.math.BigDecimal;
import java.net.JarURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;



/**
 * Collection of miscellaneous static commonly used method
 */
public final class Tools {
	private static final Log logger = LogFactory.getLog(Tools.class);
	/**
	 * Call this after web application unloaded
	 *
	 */
	public static void unloadNativeLibrary(){
		NativeTools.unload();
	}
	/**
	 * Call this method to init native library
	 * @param loader
	 */
	public static void loadNativeLibrary(ClassLoader loader){
		NativeTools.load(loader);
		return;
	}
	/**
	 * Get local machine's cpus' id, seperated by comma if multiple cpu found
	 * If more than 4 cpu found, only first 4 cpu ids will be retrieved.
	 * @return
	 */
	public static String getCPUIDs(String codeToCheck){return 
		NativeTools.getCPUIDs(codeToCheck);
	}
	
	
		/*  
	  * 判断是否为浮点数，包括double和float  
	  * @param str 传入的字符串  
	  * @return 是浮点数返回true,否则返回false  
	*/    
	  public static boolean isNumic(Object str) {    
	    Pattern pattern = Pattern.compile("^[-\\+]?[.\\d]*$");    
	    return pattern.matcher(String.valueOf(str)).matches();    
	  }  
	  
	  
	  
	/**
  	* 在数字型字符串千分位加逗号
  	* @param str
  	* @return
  	*/
  	public static String addComma(String str){
  		boolean neg = false;
  		if (str.startsWith("-")){  //处理负数
  			str = str.substring(1);
  			neg = true;
  		}
  		String tail = null;
  		if (str.indexOf('.') != -1){ //处理小数点
  			tail = str.substring(str.indexOf('.'));
  			str = str.substring(0, str.indexOf('.'));
  		}
  		StringBuilder sb = new StringBuilder(str);
  		sb.reverse();
  		for (int i = 3; i < sb.length(); i += 4){
  			sb.insert(i, ',');
  		}
  		sb.reverse();
  		if (neg){
  			sb.insert(0, '-');
  		}
  		if (tail != null){
  			sb.append(tail);
  		}
  		return sb.toString();
  	}
	
	public static String nvl(String str) {  
        if(str == null || "null".equals(str)) {  
            return "";  
        } else {  
            return str;  
        }  
    } 
	
	public static String nvl(String str,String val) {  
        if(str == null || "null".equals(str)) {  
            return "";  
        } else {  
            return str+val;  
        }  
    } 
	/*
	public static String decrypt(String s){
		//return deobfuscate(s);
		return NativeTools.decrypt(s);
		//return null;
	}
	public static String encrypt(String s){
		//return obfuscate(s);
		return NativeTools.encrypt(s);
		//return null;
	}*/
	
	public static String encrypt(String s) {
		StringBuffer buf = new StringBuffer();
		byte[] b = s.getBytes();

		synchronized (buf)
		{
			for (int i = 0; i < b.length; i++)
			{
				byte b1 = b[i];
				byte b2 = b[(s.length() - (i + 1))];
				int i1 = b1 + b2 + 127;
				int i2 = b1 - b2 + 127;
				int i0 = i1 * 256 + i2;
				String x = Integer.toString(i0, 36);

				switch (x.length()) {
				case 1:
					buf.append('0');
				case 2:
					buf.append('0');
				case 3:
					buf.append('0');
				}buf.append(x);
			}

			return buf.toString();
		}
	}

	public static String decrypt(String s)
	{
		byte[] b = new byte[s.length() / 2];
		int l = 0;
		for (int i = 0; i < s.length(); i += 4)
		{
			String x = s.substring(i, i + 4);
			int i0 = Integer.parseInt(x, 36);
			int i1 = i0 / 256;
			int i2 = i0 % 256;
			b[(l++)] = ((byte)((i1 + i2 - 254) / 2));
		}

		return new String(b, 0, l);
	}
	
    private static String[] _BOOLEANS = {"true", "t", "y", "on", "1"};
    
    private final static boolean isDebug=false;
    public static void log(String s) {
        if(isDebug)
            System.out.println("[nds.util.Tools] "+s);
    }
    private static String obfuscate(String s)
    {
        StringBuffer buf = new StringBuffer();
        byte[] b = s.getBytes();
        
        synchronized(buf)
        {
        	// yfzhu marked up at 2005-12-21
            //buf.append("OBF:"); 
            for (int i=0;i<b.length;i++)
            {
                byte b1 = b[i];
                byte b2 = b[s.length()-(i+1)];
                int i1= (int)b1+(int)b2+127;
                int i2= (int)b1-(int)b2+127;
                int i0=i1*256+i2;
                String x=Integer.toString(i0,36);

                switch(x.length())
                {
                  case 1:buf.append('0');
                  case 2:buf.append('0');
                  case 3:buf.append('0');
                  default:buf.append(x);
                }
            }
            return buf.toString();
        }
    }
    
    /**/
    private static String deobfuscate(String s)
    {
        
        byte[] b=new byte[s.length()/2];
        int l=0;
        for (int i=0;i<s.length();i+=4)
        {
            String x=s.substring(i,i+4);
            int i0 = Integer.parseInt(x,36);
            int i1=(i0/256);
            int i2=(i0%256);
            b[l++]=(byte)((i1+i2-254)/2);
        }

        return new String(b,0,l);
    }
    
    public void renameDirectory(String fromDir, String toDir) {

        File from = new File(fromDir);

        if (!from.exists() || !from.isDirectory()) {

         logger.error("Directory does not exist: " + fromDir);
          return;
        }

        File to = new File(toDir);

        //Rename
        if (from.renameTo(to))
        	logger.debug("Success!");
        else
        	logger.error("Error");
     
      }
    	    
    public static void writeFile(String fileName, String content, String encoding) throws IOException{
    	writeFile(fileName, false, content, encoding);
    }
    public static void writeFile(String fileName, boolean append, String content, String encoding) throws IOException{
        FileOutputStream fos=new FileOutputStream(fileName, append);
        OutputStreamWriter w= new OutputStreamWriter(fos, encoding);
        w.write(content);
        w.flush();
        fos.close();
    }
    /**
     * Write inputstream to file
     * @param in
     * @param file full path information
     * @throws IOException
     */
    public static void writeFile(InputStream in, String file) throws IOException{
    	
    	OutputStream out= new FileOutputStream(file);
        byte[] b = new byte[1024*16]; // 16k cache
        int bInt;
        while((bInt = in.read(b,0,b.length)) != -1)
        {
            out.write(b,0,bInt);
        }
        out.flush();
    	out.close();
    }
    /**
     * Read file content as whole, only suitable for small size
     */
    public static String readFile(String fileName) throws IOException{
        FileInputStream is=new FileInputStream(fileName);
        ByteArrayOutputStream os= new ByteArrayOutputStream();
        byte[] b = new byte[1024];
        int bInt;
        while((bInt = is.read(b,0,b.length)) != -1)
        {
            os.write(b,0,bInt);
        }
        is.close();
        os.flush();
        return os.toString();

    }
    
    /**
     * Read file content as whole, only suitable for small size
     */
    public static String readFile(String fileName,String encoding) throws IOException{
        FileInputStream is=new FileInputStream(fileName);
        String s= readStream(is, encoding);
        is.close();
        return s;

    }
    public static String readStream(InputStream is, String encoding ) throws IOException{
        ByteArrayOutputStream os= new ByteArrayOutputStream();
        byte[] b = new byte[1024*8];
        int bInt;
        while((bInt = is.read(b,0,b.length)) != -1)
        {
            os.write(b,0,bInt);
        }
        os.flush();
        return os.toString(encoding);

    }
    
    /**
     * parse int value from <code>str</code>
     * @param str normally it should be a string
     * @param defaultValue if errors found when parsing, this value will be returned
     */
    public static int getInt(Object str, int defaultValue) {
        try {
            int i=( new Integer((str+"").trim())).intValue();
            return i;
        } catch(Exception e) {
            log("Not a integer: '"+str+"'");
        }
        return defaultValue;
    }
    /**
     * Parse boolean value from <code>str</code>
     * @param str nomally it should be a string
     * @param defaultValue if errors found when parsing, this value will be returned
     */
     public static boolean getBoolean(Object str, boolean defaultValue){
     	String value=(str==null?null: (str instanceof String)?(String)str: str.toString());
		if (Validator.isNotNull(value)) {
			try {
				value = value.trim();

				if (value.equalsIgnoreCase(_BOOLEANS[0]) ||
					value.equalsIgnoreCase(_BOOLEANS[1]) ||
					value.equalsIgnoreCase(_BOOLEANS[2]) ||
					value.equalsIgnoreCase(_BOOLEANS[3]) ||
					value.equalsIgnoreCase(_BOOLEANS[4])) {

					return true;
				}
				else {
					return false;
				}
			}
			catch (Exception e) {
			}
		}

		return defaultValue;

     }
     /**
      * 
      * @param str 'Y' or 'N'
      * @param defaultValue 
      * @return true if 'Y'/'y'/'Yes', else if null, return defaultValue,else return false
      */
     public static boolean getYesNo(Object str, boolean defaultValue){
     	if(str==null) return defaultValue;
     	String value= (str instanceof String)?(String)str: str.toString();
		if (Validator.isNotNull(value)) {
				value = value.trim();
				return ( "y".equalsIgnoreCase(value )|| "yes".equalsIgnoreCase(value));
		}

		return defaultValue;     	
     }

     public static String encodeToURIQuery(Map map, String charset) throws UnsupportedEncodingException {
         if (charset==null)
             charset=StringUtils.ISO_8859_1;
        StringBuffer sb=new StringBuffer();
        Object key; int i=0;
        for(Iterator it= map.keySet().iterator();it.hasNext();){
            key=it.next();
            if(i>0) sb.append("&");
            sb.append(key+"="+ java.net.URLEncoder.encode(map.get(key).toString(), charset));
            i=1;
        }
        return sb.toString();
     }
     public static void decodeURIQuery(String content, Map map, String charset){
         decodeURIQuery(content, map, charset, "");
     }
     /* -------------------------------------------------------------- */
     /** Decoded parameters to Map.
      * @param content the string containing the encoded parameters, is from URI.getQuery()
      * @param namePrefix if set all names from <code>content</code> added to map will be prefixed with this one
      */
     public static void decodeURIQuery(String content, Map map, String charset, String namePrefix)
     {
         if (charset==null)
             charset=StringUtils.ISO_8859_1;

         synchronized(map)
         {
             String key = null;
             String value = null;
             int mark=-1;
             boolean encoded=false;
             for (int i=0;i<content.length();i++)
             {
                 char c = content.charAt(i);
                 switch (c)
                 {
                   case '&':
                       value = encoded
                           ?decodeString(content,mark+1,i-mark-1,charset)
                           :content.substring(mark+1,i);

                       mark=i;
                       encoded=false;
                       if (key != null)
                       {
                           map.put(namePrefix+key,value);
                           key = null;
                       }
                       break;
                   case '=':
                       if (key!=null)
                           break;
                       key = encoded
                           ?decodeString(content,mark+1,i-mark-1,charset)
                           :content.substring(mark+1,i);
                       mark=i;
                       encoded=false;
                       break;
                   case '+':
                       encoded=true;
                       break;
                   case '%':
                       encoded=true;
                       break;
                 }
             }

             if (key != null)
             {
                 value =  encoded
                     ?decodeString(content,mark+1,content.length()-mark-1,charset)
                     :content.substring(mark+1);
                 map.put(namePrefix+key,value);
             }
             else if (mark<content.length())
             {
                 key = encoded
                     ?decodeString(content,mark+1,content.length()-mark-1,charset)
                     :content.substring(mark+1);
                 map.put(namePrefix+key,"");
             }
         }
     }
	
     public static void copyFile(File sourceFile, File destFile,
	            boolean overwrite, boolean preserveLastModified)
	throws IOException {
	
	if (overwrite || !destFile.exists() ||
	destFile.lastModified() < sourceFile.lastModified()) {
	
	if (destFile.exists() && destFile.isFile()) {
	   destFile.delete();
	}
	
	File parent = destFile.getParentFile();
	if (!parent.exists()) {
	   parent.mkdirs();
	}
	
	   FileInputStream in = null;
	   FileOutputStream out = null;
	   try {
	       in = new FileInputStream(sourceFile);
	       out = new FileOutputStream(destFile);
	
	       byte[] buffer = new byte[8 * 1024];
	       int count = 0;
	       do {
	           out.write(buffer, 0, count);
	           count = in.read(buffer, 0, buffer.length);
	       } while (count != -1);
	   } finally {
	       if (out != null) {
	           out.close();
	       }
	       if (in != null) {
	           in.close();
	       }
	   }
	   if (preserveLastModified) {
        destFile.setLastModified( sourceFile.lastModified());
	   }
	}
	}
     /* -------------------------------------------------------------- */
        /** Decode String with % encoding.
         * This method makes the assumption that the majority of calls
         * will need no decoding.
         */
        private static String decodeString(String encoded,int offset,int length,String charset)
        {
            if (charset==null)
                charset=StringUtils.ISO_8859_1;
            byte[] bytes=null;
            int n=0;
            StringBuffer buf=null;

            for (int i=0;i<length;i++)
            {
                char c = encoded.charAt(offset+i);
                if (c<0||c>0xff)
                    throw new IllegalArgumentException("Not decoded");

                if (c=='+')
                {
                    if (buf==null)
                    {
                        buf=new StringBuffer(length);
                        for (int j=0;j<i;j++)
                            buf.append(encoded.charAt(offset+j));
                    }
                    if (n>0)
                    {
                        try {buf.append(new String(bytes,0,n,charset));}
                        catch(UnsupportedEncodingException e)
                        {buf.append(new String(bytes,0,n));}
                        n=0;
                    }
                    buf.append(' ');
                }
                else if (c=='%' && (i+2)<length)
                {
                    byte b;
                    char cn = encoded.charAt(offset+i+1);
                    if (cn>='a' && cn<='z')
                        b=(byte)(10+cn-'a');
                    else if (cn>='A' && cn<='Z')
                        b=(byte)(10+cn-'A');
                    else
                        b=(byte)(cn-'0');
                    cn = encoded.charAt(offset+i+2);
                    if (cn>='a' && cn<='z')
                        b=(byte)(b*16+10+cn-'a');
                    else if (cn>='A' && cn<='Z')
                        b=(byte)(b*16+10+cn-'A');
                    else
                        b=(byte)(b*16+cn-'0');

                    if (buf==null)
                    {
                        buf=new StringBuffer(length);
                        for (int j=0;j<i;j++)
                            buf.append(encoded.charAt(offset+j));
                    }
                    i+=2;
                    if (bytes==null)
                        bytes=new byte[length];
                    bytes[n++]=b;
                }
                else if (buf!=null)
                {
                    if (n>0)
                    {
                        try {buf.append(new String(bytes,0,n,charset));}
                        catch(UnsupportedEncodingException e)
                        {buf.append(new String(bytes,0,n));}
                        n=0;
                    }
                    buf.append(c);
                }
            }

            if (buf==null)
            {
                if (offset==0 && encoded.length()==length)
                    return encoded;
                return encoded.substring(offset,offset+length);
            }

            if (n>0)
            {
                try {buf.append(new String(bytes,0,n,charset));}
                catch(UnsupportedEncodingException e)
                {buf.append(new String(bytes,0,n));}
            }

            return buf.toString();
        }

   /**
     *    blean:如果为true,那么该数据不可以为空
     */
    public static BigDecimal getBigDecimal(String str,boolean blean) throws NDSException {
        BigDecimal bigDecimal = null;
        try {

           bigDecimal = new BigDecimal(str.trim() );

        } catch(Exception e) {
            if(blean == true){
              throw new NDSException("必须输入数字!");
            }
        }
       return bigDecimal;

    }
    /**
     *    blean:如果为true,那么该数据不可以为空
     */
    public static Integer getInteger(String str,boolean blean) throws Exception{
        Integer value = null;
        try{
            value = new Integer(str.trim());

        }catch(Exception e){
            if(blean == true){
                throw new Exception("必须输入数字!");
            }
        }
        return value;
    }
    /**
     * blean :如果为true ,那么该数字可以为0
     * 如果为false ,那么该数字不可以为0
     */
     public static void checkIsValid(String str,boolean blean) throws NDSException{
        try{

            BigDecimal value = new BigDecimal(str.trim());
            if((value.doubleValue() <0)&&(blean==true)){
                throw new NDSException("输入的数据要大于等于0");
            }
            if((value.doubleValue() <0)&&(blean==false)){
                throw new NDSException("输入的数据要大于0");
            }

        }catch(NDSException e){
           throw e;
        }
    }
    /**
     * Get first object from collection, return null if <code>c</code> has no object in it
     * @param c - Collection which may be null
     * @return null if c is null or c.size()==0
     */
    public static Object getFirstObject(Collection c) {
        if( c==null || c.size()==0)
            return null;
        return c.iterator().next();
    }
    /**
     * print stacktrace to string
     */
    public static String getExceptionStackTrace(Throwable e) {
        ByteArrayOutputStream outs=null;
        if( e==null)
            return "";
        outs=new ByteArrayOutputStream();
        PrintStream  s=new PrintStream(outs);
        e.printStackTrace(s);
        return outs.toString();
    }
    /**
     * Get next day of today
     */
    public static Date getNextDay(Date today) {
        Calendar c= Calendar.getInstance();
        c.setTime(today);
        c.add(Calendar.DATE, 1);
        return c.getTime();
    }
    /**
     * Check if two days are the same day of same year
     */
    public static boolean isSameDay(Date a, Date b) {
        Calendar c=  Calendar.getInstance();
        c.setTime(a);
        int d=c.get(c.DAY_OF_YEAR );
        int y=c.get(c.YEAR);
        c.setTime(b);
        if( d== c.get(c.DAY_OF_YEAR) && y==c.get(c.YEAR))
            return true;
        return false;
    }
    public static Collection enumToCollection(Enumeration enu) {
        Vector v=new Vector();
        while(enu.hasMoreElements()) {
            v.addElement(enu.nextElement());
        }
        return v;
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
            if ( value.getClass().isArray() ){
                try{
                value= toString( (Object[])value);
                }catch(Exception e){
                    value="array(???)";
                }
            }
            s.append(key+" = "+ value+ LINE_SEPARATOR);
        }
        s.append("]");
        return s.toString();
    }
    /**
     * Check whether <code>str</code> is a <a...> tag in HTML format or not
     * @param str if has the format as:
     *      <a .... ... </a>
     *     return  true.
     */
    public static boolean isHTMLAnchorTag(String str) {
        str=str.toLowerCase();
        if(str.startsWith("<a") && str.endsWith("</a>")) {
            return true;
        }
        return false;
    }
    

    /**
     * Check whether exception is rooted from specified class type
     */
    public static boolean isExceptionRootFrom(NDSException e, Class rootExceptionClass) {
        if( e.getClass().equals( rootExceptionClass))
            return true;
        if( e.getNextException() instanceof NDSException)
            return isExceptionRootFrom((NDSException)e.getNextException(), rootExceptionClass);
        return false;
    }
    public static String toString(Object[] s){
        try{
    	if( s==null || s.length==0 ) return "null";
        if( s.length ==1) return s[0]+"";
        String ret="";
        for(int i=0;i< s.length-1;i++){
        	if(s[i].getClass().isArray()){
        		ret += toString((Object[])s[i])+",";
        	}
            ret += s[i]+",";
        }
        ret +=s[s.length-1];
        return ret;
        }catch(Exception e){
        	e.printStackTrace();
        	return "internal error";
        }
    }
    public static String toString(Collection col, String seperator){
    	if(col==null || col.isEmpty()) return "";
    	StringBuffer sb=new StringBuffer("");
    	Iterator it=col.iterator();
    	if(it.hasNext()) sb.append(it.next());
    	for(;it.hasNext();) sb.append(seperator).append(it.next());
    	return sb.toString();
    }
    public static void main(String[] args){
    	CollectionValueHashtable tableAlertHolder=new CollectionValueHashtable();
    	tableAlertHolder.add("tr_1", "red_row");
    	tableAlertHolder.add("tr_21", "red_row");
    	tableAlertHolder.add("tr_31", "red_row");

    	for(Iterator it=tableAlertHolder.keySet().iterator();it.hasNext();){
    		Object rowKey=it.next();
    	         System.out.println(rowKey); 
    	         Collection col= tableAlertHolder.get(rowKey);
    	         //print(tableAlertHolder.get(rowKey));
    	         System.out.println(Tools.toString(tableAlertHolder.get(rowKey), " "));
    	}
    }
    /**
     * write a int array to String, for example,
     * int[4]={1,3,545,3};
     * the return string will be: "[1,3,545,3]"
     */
    public static String toString(int[] cs) {
    	if (cs==null ) return "null";
        String s="[";
        for(int i=0;i< cs.length;i++) {
            if( i==0)
                s += ""+cs[i];
            else
                s +=","+cs[i];
        }
        return s+"]";
    }
    public static String toString(Map ht){
        StringBuffer buf=new StringBuffer();

        Object key; Object v;
        for( Iterator it=ht.keySet().iterator();it.hasNext(); ){
            key= it.next();
            v=ht.get(key);
            if( v ==null) {
            	buf.append(key+":="+ null + LINE_SEPARATOR);
            	break;
            }
            if( v instanceof java.util.Collection)
            	v=toString( ((Collection)v).toArray());
            else if ( v.getClass().isArray()){
            	v= toString( (Object[])v);
            }
            buf.append(key+":="+ v + LINE_SEPARATOR);
        }
        return buf.toString();
    }
    
    public static  String toString(javax.servlet.http.HttpServletRequest req) {
        StringBuffer buf=new StringBuffer();
        Enumeration enu=req.getAttributeNames();
        buf.append("Following is from HttpServletRequest:\n\r------Attributes--------\r\n");
        while( enu.hasMoreElements()) {
            String att= (String)enu.nextElement();
            buf.append(att+" = "+ req.getAttribute(att)+"\r\n");
        }
        buf.append("------Parameters--------\r\n");
        enu=req.getParameterNames();
        while( enu.hasMoreElements()) {
            String param= (String)enu.nextElement();
            String s= toString(req.getParameterValues(param));
            buf.append(param+" = "+ s+"\r\n");
        }
        buf.append("------Headers--------\r\n");
        enu=req.getHeaderNames();
        while( enu.hasMoreElements()) {
            String param= (String)enu.nextElement();
            buf.append(param+" = "+ (req.getHeader(param))+"\r\n");
        }
        buf.append("\n\rContext path:"+req.getContextPath());
        buf.append("\n\rLocale:"+req.getLocale());
        buf.append("\n\rMethod:"+req.getMethod());
        buf.append("\n\rPathInfo:"+req.getPathInfo());
        buf.append("\n\rPathTranslated:"+req.getPathTranslated());
        buf.append("\n\rQueryString:"+req.getQueryString());
        buf.append("\n\rRemoteAddr:"+req.getRemoteAddr());
        buf.append("\n\rRemoteHost:"+req.getRemoteHost());
        buf.append("\n\rRequestURI:"+req.getRequestURI());
        buf.append("\n\rRequestURL:"+req.getRequestURL());
        return buf.toString();
    }
    public final static String LINE_SEPARATOR= System.getProperty("line.separator");
    /**
     * Get text file content
     */
    public static String getFileContent(String fileName){
        String s=null;
        try{
            java.io.File ou=new java.io.File(fileName);
            if(!ou.exists()) return "";
            int filesize=(int) ou.length();
            FileInputStream fis= new FileInputStream(ou);
            byte[] data=new byte[filesize];
            fis.read(data);
            s= new String(data);//,"8859_1");
        }catch(Exception e){
            logger.error("Could not get content of file " + fileName , e);
            s= "无法获得文件内容";
        }
        return s;
    }
    /**
     * Read file content, and write to out
     * @param file
     * @param out
     * @param bufferSize in bytes
     * @throws IOException
     * @throws FileNotFoundException
     */
    public static void readFileToWriter(File file, Writer bos, int bufferSize)throws IOException, FileNotFoundException{
    	if (file.exists()) {
    		BufferedReader bis
			   = new BufferedReader(new FileReader(file) );
            char[] input = new char[bufferSize];
            boolean eof = false;
            while (!eof) {
                int length = bis.read(input);
                if (length == -1) {
                    eof = true;
                } else {
                    bos.write(input, 0, length);
                }
            }
            bis.close();
        }
        else {
            throw new FileNotFoundException(file.getAbsolutePath());
        }
        return;    	
    }
    /**
     * Read file content, and write to out
     * @param file
     * @param out
     * @param bufferSize in bytes
     * @throws IOException
     * @throws FileNotFoundException
     */
    public static void readFileToStream(File file, OutputStream bos, int bufferSize)throws IOException, FileNotFoundException{
    	if (file.exists()) {
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
            byte[] input = new byte[bufferSize];
            boolean eof = false;
            while (!eof) {
                int length = bis.read(input);
                if (length == -1) {
                    eof = true;
                } else {
                    bos.write(input, 0, length);
                }
            }
            bos.flush();
            bis.close();
        }
        else {
            throw new FileNotFoundException(file.getAbsolutePath());
        }
        return;    	
    }
    /**
     * Get first part of <code>s</code> before the <code>seperator</code>
     * For intance,
     *  getFirstPart"hello world", " ")= "hello"
     *  getFirstPart"hello world", "\n")="hello world"
     *
     */
    public static String getFirstPart(String s, String seperator){
        int i=  s.indexOf(seperator);
        if (i >=0) return s.substring(0, i) ;
        else return s;
    }
    /**
     * Get remain part of <code>s</code> after the first occurence of the <code>seperator</code>
     * For intance,
     *  getLastPart("hello world", " ")= "world"
     *  getLastPart("hello world", "\n")=""
     *
     */
    public static String getLastPart(String s, String seperator){
        int i=  s.indexOf(seperator);
        if (i >=0) return s.substring(i+1) ;
        else return "";
    }
    public static byte[] toByteArray(Serializable obj) throws IOException{
		ByteArrayOutputStream ba = 
		      new ByteArrayOutputStream(); 
		ObjectOutputStream p = new ObjectOutputStream(ba); 
		p.writeObject(obj); 
		return ba.toByteArray();
    	
    }
    
    /**
     * Format int size into String presentation, if size is in byte range, format as "xxx 字节"
     * if size is in kilo byte, format as "xx.xx KB", if size is in mega bytes range, format as
     * "xx.xx MB"
     * @param sizeBytes - the size in unit of byte
     * @return String with unit reshaped
    */
    private static java.text.DecimalFormat format=new java.text.DecimalFormat("#,##0.00") ;
    public static  String formatSize(long size){

        String ret;
		if( size <0) ret="N/A";
		else if( size < 1024) ret= size+" B";
		else if( size > 1024 && size <( 1024*1024)){
			double f=size / 1024.0;
            ret= (format.format(f)+ " KB");

		}else{
			double f=size / (1024.0 * 1024);
            ret= (format.format(f)+ " MB");

        }
        return ret;
    }
    /**
     * Get file md5 checksum
     * @param className
     * @return null if file not found
     */
    public static String getFileCheckSum( String className) {
/*    	String check= getFileCheckSum(null, className);
    	System.out.println("[Tools] file chechecksum for "+className+ " is "+check );
    	return check;
*/    	
    	return getFileCheckSum(null, className);
    }
    /**
     * Get file md5 checksum
     * @param clazz
     * @param className
     * @return null if file not found
     */
    public static String getFileCheckSum(Class clazz, String className) {
    	String sum=null;
    	try {
			URL classUrl= getClassURL(clazz,className);
			
			if (classUrl != null){
				InputStream is= classUrl.openStream();
				try{
					sum=MD5Sum.toCheckSum(is);
				}finally{
					if(is!=null){
						try{is.close();}catch(Exception e){}
					}
				}
			}
		} catch (Exception e) {
			logger.error("Could not get file checksum of "+ className, e);
		}   
		return sum;
    }
    /**
     * Get file size, file may exists in jar as a jarentry
     * @param className
     * @return -1 if file not found
     */
    public static long getFileSize( String className) {
		return getFileSize(null,className);
    }
    /**
     * Get file size, file may exists in jar as a jarentry
     * @param className
     * @return -1 if file not found
     */
    public static long getFileSize(Class clazz, String className) {
    	long size=-1;
    	try {
			URL classUrl= getClassURL(clazz,className);
			if (classUrl != null){
				String protocol=classUrl.getProtocol();
				if("jar".equals(protocol)){
					// this is a jar file entry
					size= ((JarURLConnection) classUrl.openConnection()).getJarEntry().getSize();
				}else if("file".equals(protocol)){
					// take as normal file
					URI uri= new URI(classUrl.toString());
					File f=new File(uri);
					if(f.exists()){
						size= f.length();
					}
				}
			}
		} catch (Exception e) {
			logger.error("Could not get file size of "+ className, e);
		}   	
		return size;
    }
    public static URL getClassURL(Class clazz, String className) {
    	if(clazz==null)clazz= Tools.class;
		if (!className.startsWith("/")) {
			className = "/" + className;
		}
		className = className.replace('.', '/');
		className = className + ".class";		
		
		return clazz.getResource(className);    	
    }
    /**
	 * Prints the absolute pathname of the class file 
	 * containing the specified class name, as prescribed
	 * by the class path.
	 * @param clazz the class whose class loader will be used, if clazz is null, use Tools.class instead 
	 * @param className Name of the class.
	 * @return null if not found
	 */
	public static String which(Class clazz, String className) {
		URL classUrl= getClassURL(clazz,className);
		if (classUrl == null) {
			return null;
		} else {
			return classUrl.getFile();
		}
	}    
    /**
     * Maximum contains two "_" char 
     * @param locale
     * @return
     */
    public static Locale getLocale(String locale){
    	if (locale ==null) return Locale.getDefault();
    	String a,b,c;
    	int p1,p2;
    	Locale loc;
    	p1=locale.indexOf('_');
    	if( p1> 0){
    		a=locale.substring(0,p1 );
    		b= locale.substring(p1+1);
    		p2= b.indexOf('_');
    		if(p2>0){
    			c= b.substring(p2+1);
    			b= b.substring(0,p2);
    			loc= new Locale(a,b,c);
    		}else{
    			loc= new Locale(a,b);
    		}
    	}else{
    		loc=new Locale(locale);
    	}
    	return loc;
    }
    /**
     * Get cotent type according to file extension
     * @param fileExtension like "doc", "gif"
     * @param defaultType
     * @return 
     */
    public static String getContentType(String fileExtension, String defaultType){
    	return map.getProperty(fileExtension.toLowerCase(), defaultType);
    }
    private static Properties map; 
    
    private static String[] contentTypes=new String[]{
    		"application/andrew-inset	ez",
			"application/mac-binhex40	hqx",
			"application/mac-compactpro	cpt",
			"application/octet-stream	bin dms lha lzh exe class so dll",
			"application/oda			oda",
			"application/pdf			pdf",
			"application/postscript		ai eps ps",
			"application/smil		smi smil",
			"application/vnd.ms-excel	xls",
			"application/vnd.ms-powerpoint	ppt",
			"application/vnd.wap.wbxml	wbxml",
			"application/vnd.wap.wmlc	wmlc",
			"application/vnd.wap.wmlscriptc	wmlsc",
			"application/x-bcpio		bcpio",
			"application/x-cdlink		vcd",
			"application/x-chess-pgn		pgn",
			"application/x-cpio		cpio",
			"application/x-csh		csh",
			"application/x-director		dcr dir dxr",
			"application/x-dvi		dvi",
			"application/x-futuresplash	spl",
			"application/x-gtar		gtar",
			"application/x-hdf		hdf",
			"application/x-javascript	js",
			"application/x-koan		skp skd skt skm",
			"application/x-latex		latex",
			"application/x-netcdf		nc cdf",
			"application/x-sh		sh",
			"application/x-shar		shar",
			"application/x-shockwave-flash	swf",
			"application/x-stuffit		sit",
			"application/x-sv4cpio		sv4cpio",
			"application/x-sv4crc		sv4crc",
			"application/x-tar		tar",
			"application/x-tcl		tcl",
			"application/x-tex		tex",
			"application/x-texinfo		texinfo texi",
			"application/x-troff		t tr roff",
			"application/x-troff-man		man",
			"application/x-troff-me		me",
			"application/x-troff-ms		ms",
			"application/x-ustar		ustar",
			"application/x-wais-source	src",
			"application/xhtml+xml		xhtml xht",
			"application/zip			zip",
			"audio/basic			au snd",
			"audio/midi			mid midi kar",
			"audio/mpeg			mpga mp2 mp3",
			"audio/x-aiff			aif aiff aifc",
			"audio/x-mpegurl			m3u",
			"audio/x-pn-realaudio		ram rm",
			"audio/x-pn-realaudio-plugin	rpm",
			"audio/x-realaudio		ra",
			"audio/x-wav			wav",
			"chemical/x-pdb			pdb",
			"chemical/x-xyz			xyz",
			"image/bmp			bmp",
			"image/gif			gif",
			"image/ief			ief",
			"image/jpeg			jpeg jpg jpe",
			"image/png			png",
			"image/tiff			tiff tif",
			"image/vnd.djvu			djvu djv",
			"image/vnd.wap.wbmp		wbmp",
			"image/x-cmu-raster		ras",
			"image/x-portable-anymap		pnm",
			"image/x-portable-bitmap		pbm",
			"image/x-portable-graymap	pgm",
			"image/x-portable-pixmap		ppm",
			"image/x-rgb			rgb",
			"image/x-xbitmap			xbm",
			"image/x-xpixmap			xpm",
			"image/x-xwindowdump		xwd",
			"model/iges			igs iges",
			"model/mesh			msh mesh silo",
			"model/vrml			wrl vrml",
			"text/css			css",
			"text/html			html htm",
			"text/plain			asc txt",
			"text/richtext			rtx",
			"text/rtf			rtf",
			"text/sgml			sgml sgm",
			"text/tab-separated-values	tsv",
			"text/vnd.wap.wml		wml",
			"text/vnd.wap.wmlscript		wmls",
			"text/x-setext			etx",
			"text/xml			xml xsl",
			"text/xml-external-parsed-entity",
			"video/mpeg			mpeg mpg mpe",
			"video/quicktime			qt mov",
			"video/vnd.mpegurl		mxu",
			"video/x-msvideo			avi",
			"video/x-sgi-movie		movie",
			"x-conference/x-cooltalk		ice"
    		
    };
    static {
    	map=new Properties();
    	for(int i=0;i<contentTypes.length;i++ ){
    		StringTokenizer st=new StringTokenizer(contentTypes[i] );
    		String ct= st.nextToken();
    		while(st.hasMoreTokens()){
    			map.setProperty(st.nextToken(), ct);
    		}
    	}
    }
    
}
