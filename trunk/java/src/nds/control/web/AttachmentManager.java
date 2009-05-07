/******************************************************************
*
*$RCSfile: AttachmentManager.java,v $ $Revision: 1.2 $ $Author: Administrator $ $Date: 2005/08/28 00:27:02 $
*
********************************************************************/
package nds.control.web;

import java.util.*;
import java.io.*;
import java.net.*;
import javax.servlet.ServletContext;

import nds.log.*;
import nds.util.*;
/**
 *  Provides basic, versioning attachments.
 *
 *  <PRE>
 *   Structure is as follows:
 *      upload.root/$client_domain/tablename/columnname
 *         object1_Id/
 *            attachment.properties
 *               1.doc
 *               2.doc
 *               3.doc
 *         object2_Id/
 *             attachment.properties
 *               1.png
 * 		         2.png
 *             
 *  </PRE>
 *  Attachment's parentdir should be $client_domain/tablename/columnname
 *  Attachment's name will be like 1.ext, 2.ext 
 * 
 *  The names of the directories will be URLencoded.
 *  <p>
 *  "attachment.properties" consists of the following items:
 *  <UL>
 *   <LI>1.author = author name for version 1 (etc)
 *  </UL>

*/
public class AttachmentManager implements nds.util.ServletContextActor,java.io.Serializable {
    private Logger logger= LoggerManager.getInstance().getLogger(AttachmentManager.class.getName());
    private String m_storageDir;
    public static final String PROP_STORAGEDIR = "upload.root";

    private static final String PROPERTY_FILE   = "attachment.properties";

    private static final String ATTDIR_EXTENSION = "-dir";

    public void destroy() {
    }
    public void init(Director director) {
    }
    public void init(ServletContext context) {
        Configurations conf=(Configurations)WebUtils.getServletContextManager().getActor(WebKeys.CONFIGURATIONS);
        m_storageDir = conf.getProperty(PROP_STORAGEDIR, "/upload");
        //
        //  Check if the directory exists - if it doesn't, create it.
        //
        File f = new File( m_storageDir );

        if( !f.exists() )
        {
            f.mkdirs();
        }
        
    	logger.debug("AttachmentManager initialized.");

    }
    /**
     * 
     * @return root path for upload files
     */
    public String getRootPath(){
    	return m_storageDir;
    }
    /**
     *  Finds storage dir, and if it exists, makes sure that it is valid.
     *
     *  @param wikipage Page to which this attachment is attached.
     */
    private File findPageDir( String parent )
        throws NDSException
    {

        File f = new File( m_storageDir, parent);

        if( f.exists() && !f.isDirectory() )
        {
            throw new NDSException("Storage dir '"+f.getAbsolutePath()+"' is not a directory!");
        }

        return f;
    }
    /**
     *  Finds the dir in which the attachment lives.
     */
    private File findAttachmentDir( Attachment att )
        throws NDSException
    {
        File f = new File( findPageDir(att.getParentName()), 
                           (att.getFileName()+ATTDIR_EXTENSION) );
        return f;
    }

    /**
     *  Goes through the repository and decides which version is
     *  the newest one in that directory.
     *
     *  @return Latest version number in the repository, or 0, if
     *          there is no page in the repository.
     */
    private int findLatestVersion( Attachment att )
        throws NDSException
    {
        // File pageDir = findPageDir( att.getName() );
        File attDir  = findAttachmentDir( att );

        // log.debug("Finding pages in "+attDir.getAbsolutePath());
        String[] pages = attDir.list( new AttachmentVersionFilter() );

        if( pages == null )
        {
            return 0; // No such thing found.
        }

        int version = 0;

        for( int i = 0; i < pages.length; i++ )
        {
            // log.debug("Checking: "+pages[i]);
            int cutpoint = pages[i].indexOf( '.' );
                String pageNum = ( cutpoint > 0 ) ? pages[i].substring( 0, cutpoint ) : pages[i] ;

                try
                {
                    int res = Integer.parseInt( pageNum );

                    if( res > version )
                    {
                        version = res;
                    }
                }
                catch( NumberFormatException e ) {} // It's okay to skip these.
            }

        return version;
    }

    /**
     *  Returns the file extension.  For example "test.png" returns "png".
     *  <p>
     *  If file has no extension, will return "bin"
     */
    public static String getFileExtension( String filename )
    {
        String fileExt = "bin";

        int dot = filename.lastIndexOf('.');
        if( dot >= 0 && dot < filename.length()-1 )
        {
            fileExt = ( filename.substring( dot+1 ) );
        }

        return fileExt;
    }

    /**
     *  Writes the page properties back to the file system.
     *  Note that it WILL overwrite any previous properties.
     */
    private void putAttachmentProperties( Attachment att, Properties properties )
        throws IOException,
               NDSException
    {
        File attDir = findAttachmentDir( att );
        File propertyFile = new File( attDir, PROPERTY_FILE );

        OutputStream out = new FileOutputStream( propertyFile );

        properties.store( out, 
                          " JSPWiki page properties for "+
                          att.getName()+
                          ". DO NOT MODIFY!" );

        out.close();
    }

    /**
     *  Reads page properties from the file system.
     */
    private Properties getAttachmentProperties( Attachment att )
        throws IOException,
               NDSException
    {
        Properties props = new Properties();

        File propertyFile = new File( findAttachmentDir(att), PROPERTY_FILE );

        if( propertyFile != null && propertyFile.exists() )
        {
            InputStream in = new FileInputStream( propertyFile );

            props.load(in);

            in.close();
        }
        
        return props;
    }

    /**
     * Save att.in into a new file
     * @param att
     * @param data
     * @return the newly created file
     * @throws NDSException
     * @throws IOException
     */
    public File putAttachmentData( Attachment att, InputStream data )
        throws NDSException,
               IOException
    {
        OutputStream out = null;
        File attDir = findAttachmentDir( att );

        if(!attDir.exists())
        {
            attDir.mkdirs();
        }

        int latestVersion = findLatestVersion( att );

        // System.out.println("Latest version is "+latestVersion);
        
        try
        {
            int versionNumber = latestVersion+1;
            Properties props = getAttachmentProperties( att );
            
            File newfile = new File( attDir, versionNumber+"."+
                                     props.getProperty("ext",att.getExtension()) );

            logger.info("Uploading attachment "+att.getFileName()+" to page "+att.getParentName());
            logger.info("Saving attachment contents to "+newfile.getAbsolutePath());
            out = new FileOutputStream(newfile);

            copyContents( data, out );

            out.close();


            String author = att.getAuthor();

            if( author == null )
            {
                author = "unknown";
            }

            props.setProperty( versionNumber+".author", author );
            if( att.getExtension() !=null) props.setProperty("ext", props.getProperty("ext",att.getExtension()) );
            putAttachmentProperties( att, props );
            return newfile;
        }
        catch( IOException e )
        {
            logger.error( "Could not save attachment data: ", e );
            throw (IOException) e.fillInStackTrace();
        }
        finally
        {
                if( out != null ) out.close();
        }
    }

   

    private File findFile( File dir, Attachment att )
        throws FileNotFoundException,
               NDSException,IOException
    {
        int version = att.getVersion();

        if( version == -1 )
        {
            version = findLatestVersion( att );
        }
        Properties props=  getAttachmentProperties( att);
        String ext = props.getProperty("ext",  att.getExtension());
        File f = new File( dir, version+"."+ext );

        if( !f.exists() )
        {
            if ("bin".equals(ext))
            {
                File fOld = new File( dir, version+"." );
                if (fOld.exists())
                    f = fOld;
            }
            if( !f.exists() )
            {
                throw new FileNotFoundException("No such file: "+f.getAbsolutePath()+" exists.");
            }
        }

        return f;
    }

    public InputStream getAttachmentData( Attachment att )
        throws IOException,
               NDSException
    {
        File attDir = findAttachmentDir( att );

        File f = findFile( attDir, att );

        return new FileInputStream( f );
    }
    public File getAttachmentFile(Attachment att )
        throws IOException,
               NDSException
    {
        File attDir = findAttachmentDir( att );

        File f = findFile( attDir, att );

        return f;
    }
    
    
    /**
     * 
     * @param parentPath
     * @param name
     * @param version -1 if to get latest version
     * @return
     * @throws NDSException
     */
    public Attachment getAttachmentInfo( String parentPath, String name, int version )
        throws NDSException
    {
        Attachment att = new Attachment( parentPath, name );
        File dir = findAttachmentDir( att );

        if( !dir.exists() )
        {
            // log.debug("Attachment dir not found - thus no attachment can exist.");
            return null;
        }
        
        if( version ==-1 )
        {
            version = findLatestVersion(att);
        }

        att.setVersion( version );
        

        // System.out.println("Fetching info on version "+version);
        try
        {
            Properties props = getAttachmentProperties(att);

            att.setAuthor( props.getProperty( version+".author" ) );
            att.setExtension(props.getProperty("ext"));
            File f = findFile( dir, att );

            att.setSize( f.length() );
            att.setLastModified( new Date(f.lastModified()) );
        }catch(java.io.FileNotFoundException fne){
        	logger.error(fne.getMessage());
        	return null;
        }catch( IOException e )
        {
            logger.error("Can't read page properties", e );
            throw new NDSException("Cannot read page properties: "+e.getMessage());
        }
        return att;
    }

    public List getVersionHistory( Attachment att )
    {
        ArrayList list = new ArrayList();

        try
        {
            int latest = findLatestVersion( att );

            for( int i = latest; i >= 1; i-- )
            {
                Attachment a = getAttachmentInfo( att.getParentName(), 
                                                  att.getFileName(), i );

                if( a != null )
                {
                    list.add( a );
                }
            }
        }
        catch( NDSException e )
        {
            logger.error("Getting version history failed for page: "+att,e);
        }

        return list;
    }


    

    public void deleteAttachment( Attachment att )
        throws NDSException
    {
        File dir = findAttachmentDir( att );
        String[] files = dir.list();

        for( int i = 0; i < files.length; i++ )
        {
            File file = new File( dir.getAbsolutePath() + "/" + files[i] );
            file.delete();
        }
        dir.delete();
    }


 

    /**
     *  Just copies all characters from <I>in</I> to <I>out</I>.
     *
     *  @since 1.9.31
     */
    private void copyContents( InputStream in, OutputStream out )
        throws IOException
    {
        byte[] b = new byte[1024*16]; // 16k cache
        int bInt;
        while((bInt = in.read(b,0,b.length)) != -1)
        {
            out.write(b,0,bInt);
        }
        out.flush();
    }
    
    /**
     *  Accepts only files that are actual versions, no control files.
     */
    public class AttachmentVersionFilter
        implements FilenameFilter
    {
        public boolean accept( File dir, String name )
        {
            return !name.equals( PROPERTY_FILE );
        }
    }
}
