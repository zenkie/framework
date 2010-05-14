/******************************************************************
*
*$RCSfile: Attachment.java,v $ $Revision: 1.1 $ $Author: Administrator $ $Date: 2005/06/16 10:19:42 $
*
********************************************************************/
package nds.util;

import java.util.*;

/**
 Verioned attachment with author specified. 
*/
public class Attachment {
    private String m_fileName; //this will be file name is filesystem 
    private String m_parentName;
    private int    m_status = CREATED;
    private String       m_name;
    private Date         m_lastModified;
    private long         m_fileSize = -1;
    private int          m_version = -1;
    private HashMap      m_attributes = new HashMap();
    private String m_author;
    private String m_ext;
    public static final int CREATED       = 0;
    public static final int UPLOADING     = 1;
    public static final int COMPLETE      = 2;
    
    private String origFileName; // uploaded file name
    
    public Attachment( String parentDir, String fileName)
    {
        m_name= parentDir+"/"+fileName ;
        m_parentName = parentDir;
        m_fileName   = fileName;
    }

    public String toString()
    {
        return "Attachment ["+getName()+";mod="+getLastModified()+
               ";status="+m_status+"]";
    }
    public String getExtension(){
    	return m_ext;
    }
    public void setExtension(String ext){
    	this.m_ext=ext;
    }
    public String getFileName()
    {
        return( m_fileName );
    }

    public void setFileName( String name )
    {
        m_fileName = name;
    }

    public int getStatus()
    {
        return m_status;
    }

    public void setStatus( int status )
    {
        m_status = status;
    }

    public String getParentName()
    {
        return m_parentName;
    }

    public String getName()
    {
        return m_name;
    }
    
    /**
     *  A WikiPage may have a number of attributes, which might or might not be 
     *  available.  Typically attributes are things that do not need to be stored
     *  with the wiki page to the page repository, but are generated
     *  on-the-fly.  A provider is not required to save them, but they
     *  can do that if they really want.
     *
     *  @param key The key using which the attribute is fetched
     *  @return The attribute.  If the attribute has not been set, returns null.
     */
    public Object getAttribute( String key )
    {
        return m_attributes.get( key );
    }

    /**
     *  Sets an metadata attribute.
     */
    public void setAttribute( String key, Object attribute )
    {
        m_attributes.put( key, attribute );
    }

    /**
     *  Removes an attribute from the page, if it exists.
     *  @return If the attribute existed, returns the object.
     *  @since 2.1.111
     */
    public Object removeAttribute( String key )
    {
        return m_attributes.remove( key );
    }

    /**
     *  Returns the date when this page was last modified.
     */
    public Date getLastModified()
    {
        return m_lastModified;
    }

    public void setLastModified( Date date )
    {
        m_lastModified = date;
    }

    public void setVersion( int version )
    {
        m_version = version;
    }

    /**
     *  Returns the version that this WikiPage instance represents.
     */
    public int getVersion()
    {
        return m_version;
    }

    /**
     *  @since 2.1.109
     */
    public long getSize()
    {
        return( m_fileSize );
    }

    /**
     *  @since 2.1.109
     */
    public void setSize( long size )
    {
        m_fileSize = size;
    }
    public void setAuthor( String author )
    {
        m_author = author;
    }

    /**
     *  Returns author name, or null, if no author has been defined.
     */
    public String getAuthor()
    {
        return m_author;
    }

	public String getOrigFileName() {
		return origFileName;
	}

	public void setOrigFileName(String origFileName) {
		this.origFileName = origFileName;
	}
	
}
