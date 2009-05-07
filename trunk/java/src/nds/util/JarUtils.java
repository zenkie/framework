
package nds.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public final class JarUtils
{

    private static void ensureDirectory(File file)
        throws IOException
    {
        if(file.exists())
        {
            if(!file.isDirectory())
                throw new IOException("Couldn't unpack dir " + file.getCanonicalPath() + "; already exists but not a directory.");
        } else
        if(!file.mkdirs())
            if(!file.exists())
                new IOException("Couldn't make directory " + file.getCanonicalPath());
            else
                log("Huh? Couldnn't mkdirs " + file + " but it exists.");
    }

    public static boolean isJar(File file)
    {
        FileInputStream fileinputstream = null;
        try
        {
            boolean flag3;
            try
            {
                fileinputstream = new FileInputStream(file);
                boolean flag4 = false;
                byte abyte0[] = new byte[4];
                int i;
                int j;
                for(i = 0; i < 4; i += j)
                {
                    j = fileinputstream.read(abyte0, i, 4 - i);
                    if(j == -1)
                        break;
                }

                if(i < 4)
                {
                    boolean flag = false;
                    return flag;
                }
                for(int k = 0; k < 4; k++)
                    if(abyte0[k] != jarMagicNumber[k])
                    {
                        boolean flag1 = false;
                        return flag1;
                    }

                boolean flag2 = true;
                return flag2;
            }
            catch(IOException _ex)
            {
                flag3 = false;
            }
            return flag3;
        }
        finally
        {
            if(fileinputstream != null)
                try
                {
                    fileinputstream.close();
                }
                catch(IOException _ex) { }
        }
    }

    public static void unpackJar(File file, File file1, String pattern)
        throws IOException
    {
        unpackJar(((InputStream) (new FileInputStream(file))), file1, pattern);
    }
    /**
     * Find entries in specified path with specified extension in jar file
     * @return Vector of entry data, in byte[].
     */
    public static Vector findEntries(File jarFile, String path, String fileExt) throws IOException{
        Vector v=new Vector();
        ZipInputStream zipinputstream = new ZipInputStream(new FileInputStream(jarFile));
        BufferedInputStream bufferedinputstream = new BufferedInputStream(zipinputstream);
        ZipEntry zipentry;
        ByteArrayOutputStream bos=null;
        while((zipentry = zipinputstream.getNextEntry()) != null)
        {
            String entryName=zipentry.getName();

            if( entryName.startsWith(path) && entryName.endsWith(fileExt)){
//                v.addElement(entryName.substring(path.length()+1));
                int l =(int) zipentry.getSize();// maybe -1, means unknown
                bos=new ByteArrayOutputStream();
                int l1 = 0;
                int i;
                try{
                    while((i = bufferedinputstream.read()) != -1){
                        l1++;
                        bos.write(i);
                    }
                }catch(EOFException eofexception){
                    if(l1 != l)
                        throw eofexception;
                }
                zipinputstream.closeEntry();
                v.addElement(bos.toByteArray());
            }
            zipinputstream.closeEntry();
        }
        bufferedinputstream.close();
        return v;
    }
    public static void unpackJar(InputStream inputstream, File file, String pattern)
        throws IOException
    {
        ZipInputStream zipinputstream = new ZipInputStream(inputstream);
        BufferedInputStream bufferedinputstream = new BufferedInputStream(zipinputstream);
        ZipEntry zipentry;
        while((zipentry = zipinputstream.getNextEntry()) != null)
        {
            if(! zipentry.getName().startsWith(pattern)) continue;
            String s = zipentry.getName().replace('/', File.separatorChar);
            File file1 = new File(file, s);
            if(zipentry.isDirectory())
            {
                ensureDirectory(file1);
            } else
            {
                ensureDirectory(new File(file1.getParent()));
                FileOutputStream fileoutputstream = new FileOutputStream(file1);
                BufferedOutputStream bufferedoutputstream = new BufferedOutputStream(fileoutputstream);
                long l = zipentry.getSize();
                long l1 = 0L;
                int i;
                try
                {
                    while((i = bufferedinputstream.read()) != -1)
                    {
                        l1++;
                        bufferedoutputstream.write(i);
                    }
                }
                catch(EOFException eofexception)
                {
                    if(l1 != l)
                        throw eofexception;
                }
                zipinputstream.closeEntry();
                bufferedoutputstream.close();
            }
        }
        bufferedinputstream.close();
    }

    public JarUtils()
    {
    }
    public static void log(String s) {
        if(debug)
            System.out.println("[nds.util.JarUtils] "+s);
    }

    private static final boolean debug = true;
    private static final boolean verbose = true;
    public static final byte jarMagicNumber[] = {
        80, 75, 3, 4
    };

}
