/******************************************************************
*
*$RCSfile: TableMappingManager.java,v $ $Revision: 1.2 $ $Author: Administrator $ $Date: 2005/03/16 09:05:14 $
*
*$Log: TableMappingManager.java,v $
*Revision 1.2  2005/03/16 09:05:14  Administrator
*no message
*
*Revision 1.1.1.1  2005/03/15 11:23:24  Administrator
*init
*
*Revision 1.6  2003/05/29 19:40:00  yfzhu
*<No Comment Entered>
*
*Revision 1.5  2003/04/03 09:28:15  yfzhu
*<No Comment Entered>
*
*Revision 1.4  2003/03/30 08:11:37  yfzhu
*Updated before subtotal added
*
*Revision 1.3  2002/12/17 09:09:17  yfzhu
*no message
*
*Revision 1.2  2002/12/17 05:54:17  yfzhu
*no message
*
*Revision 1.1.1.1  2002/11/29 02:50:35  yfzhu
*Active POS
*
*Revision 1.7  2001/12/28 14:20:02  yfzhu
*no message
*
*Revision 1.6  2001/12/09 03:43:32  yfzhu
*no message
*
*Revision 1.5  2001/11/29 13:13:14  yfzhu
*no message
*
*Revision 1.4  2001/11/29 00:49:40  yfzhu
*no message
*
*Revision 1.3  2001/11/22 06:28:18  yfzhu
*no message
*
*Revision 1.2  2001/11/07 20:58:47  yfzhu
*no message
*
*
********************************************************************/
package nds.schema;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Vector;

import nds.log.Logger;
import nds.log.LoggerManager;
import nds.util.JarUtils;
import nds.util.NDSException;
import nds.util.StringHashtable;
import nds.util.xml.XmlMapper;
/**
 * Table information loader
 */
public class TableMappingManager implements java.io.Serializable {
    /**
     * Extension of file containing table info
     */
    public final static String TABLE_EXTENSION=".table";

    private static Logger logger= LoggerManager.getInstance().getLogger(TableMappingManager.class.getName());

    private StringHashtable mappings;
    /**
     * @param directory Absolute path of directory which contains "*.table"
     */
    public TableMappingManager(String directory) throws NDSException {
        mappings=new StringHashtable(30);
        loadTables(directory);
    }
    private void loadTables(String directory) throws NDSException {
        try {
            logger.debug("Directory is:"+ directory);
            URL url=new URL(directory);

            File dir= new File(url.getFile());

            if( !dir.isDirectory()) {
                // take as input stream, and check whether it is a jar/war file

                if( !url.getProtocol().equals("zip"))
                    throw new NDSException(" Directory ("+directory+") is not valid, only directory or jar file allowed.");
                String rootFileName= url.getFile().substring(0,url.getFile().indexOf("!"));
                logger.debug("Jar file is:"+ rootFileName);

                File  rootFile=new File(rootFileName);
                Vector tables= JarUtils.findEntries(rootFile, "WEB-INF/tables",".table");
                for( int i=0;i< tables.size();i++) {
                    InputStream tableStream=new ByteArrayInputStream((byte[]) tables.elementAt(i));
                    try{
//                        logger.debug("init No."+i);
                        init(tableStream);
                    }catch(Exception e){
                        logger.debug("Error found when init No."+i+"(start from 0) table in Jar file" );
                    }
                }
                return;
                /*                File  parentDir= createTempDirectory();

                                    logger.debug("Temp directory created:"+ parentDir.toString());
                                    JarUtils.unpackJar(rootFile, parentDir, "WEB-INF/tables");
                                    String tableDir= parentDir.getPath()+"/WEB-INF/tables";
                                    logger.debug("Search table description files in "+tableDir);
                                    dir=new File(tableDir);*/
            }
            //dir= new File( url.getFile());


            // list only file with specified extension
            FilenameFilter filter=new FileSeacher(dir,TABLE_EXTENSION);
            File[] files=dir.listFiles(filter);
            if( files ==null || files.length==0) {
                logger.debug("Could not find any tables in directory:"+ dir);
                return;
            }
            //files=sort( files); // no need anymore, SchemaStrcuture will do that
            for( int i=0;i< files.length;i++) {
                try{
//                    logger.debug("Begin init:"+files[i]);
                    init( files[i].toURL().openStream());
                }catch(Exception e){
                    logger.debug("Error found when init file:"+ files[i], e);
                }
            }
        } catch(IOException e) {
            logger.error("error.",e);
            throw new NDSException("Exception found.", e);
        }

    }
    /**
     * Create temporary directory in tmpdir, named <code>myTempDirName</code>
     * if the directory already exists in tmpdir, remove it recursivly
     */
    private File createTempDirectory(String myTempDirName) throws IOException {
        String tempDir= System.getProperty("java.io.tmpdir","/");
        logger.debug("Temp dir is :"+ tempDir);
        File dir= new File(tempDir+myTempDirName);
        if( dir.isFile())
            dir.delete();
        else if( dir.isDirectory()) {
            removeDir(dir);
            return dir;
        }
        boolean result = dir.mkdirs();
        if (result == false) {
            String msg = "Directory " + dir.getAbsolutePath() + " creation was not " +
                         "successful for an unknown reason";
            throw new IOException(msg);
        }
        return dir;
    }

    /**
     * Remove directory and all its sub directories and files
     */
    protected void removeDir(File d) {
        String[] list = d.list();
        if (list == null)
            list = new String[0];
        for (int i = 0; i < list.length; i++) {
            String s = list[i];
            File f = new File(d, s);
            if (f.isDirectory()) {
                removeDir(f);
            } else {
                //                logger.debug("Deleting " + f.getAbsolutePath());
                if (!f.delete()) {
                    logger.debug("Unable to delete file " + f.getAbsolutePath());
                }
            }
        }
        // remove the directory itself
        //        logger.debug("Deleting directory " + d.getAbsolutePath());
        if (!d.delete()) {
            logger.debug("Unable to delete directory " + d.getAbsolutePath());
        }
    }

    private File[] sort(File[] files) {
        Vector v=new Vector();
        for( int i=0;i< files.length;i++) {
            v.addElement(files[i]);
        }
        nds.util.ListSort.sort(v);
        File[] fs=new File[ v.size()];
        for( int i=0;i< fs.length;i++) {
            fs[i]= (File) v.elementAt(i);
        }
        return fs;
    }
    /**
     * Get one table map
     */
    private void init(InputStream stream) throws Exception{
            TableMapping map=loadMapping(stream);
            mappings.put(map.name, map);
            stream.close();
    }
    private TableMapping loadMapping(InputStream stream) throws Exception {
        TableMapping maps=new TableMapping();
        String dtdURL = "file:" ;
        XmlMapper xh=new XmlMapper();
        xh.setValidating(true);

        // By using dtdURL you brake most buildrs ( at least xerces )
        xh.register("-//Sun Microsystems, Inc.//DTD Web Application 2.2//EN",
                    dtdURL );
        xh.addRule("table/name", xh.methodSetter("setName", 0) );
        xh.addRule("table/description", xh.methodSetter("setDescription", 0) );
        xh.addRule("table/description2", xh.methodSetter("setDescription2", 0) );
        xh.addRule("table/mask", xh.methodSetter("setMask", 0) );
        xh.addRule("table/category", xh.methodSetter("setCategory", 0) );
        xh.addRule("table/order", xh.methodSetter("setOrder", 0) );
        xh.addRule("table/url", xh.methodSetter("setURL", 0) );
        xh.addRule("table/class", xh.methodSetter("setClassName", 0) );
        xh.addRule("table/primary-key", xh.methodSetter("setPrimaryKey", 0) );
        xh.addRule("table/alternate-key", xh.methodSetter("addAlternateKey", 0) );
        xh.addRule("table/alternate-key2", xh.methodSetter("addAlternateKey2", 0) );
        xh.addRule("table/item-table", xh.methodSetter("setItemTable", 0) );
        xh.addRule("table/prefetch-column", xh.methodSetter("setPrefetchColumn", 0) );
        xh.addRule("table/prefetch-sql", xh.methodSetter("setPrefetchSql", 0) );
        // -------- yfzhu added at 2002-12-15 for dispatch-column
        xh.addRule("table/dispatch-column", xh.methodSetter("setDispatchColumn",0));
        // --------- yfzhu added at 2003-05-23 for comment
        xh.addRule("table/comment", xh.methodSetter("setComment",0));
        //2.0 for view construction, the table name will be view name, real table name
        // will be set here.
        xh.addRule("table/security-directory", xh.methodSetter("setSecurityDirectory",0) );
        xh.addRule("table/real-table", xh.methodSetter("setRealTable",0) );
        xh.addRule("table/filter", xh.methodSetter("setFilter",0) );
        xh.addRule("table/ref-by-table", xh.objectCreate("nds.schema.RefByTableHolder") );
        xh.addRule("table/ref-by-table", xh.addChild("addRefByTable", null) ); // remove it from stack when done
        xh.addRule("table/ref-by-table/name", xh.methodSetter("setTableName", 0) );
        xh.addRule("table/ref-by-table/ref-by-column", xh.methodSetter("setColumnName", 0) );
        xh.addRule("table/ref-by-table/filter", xh.methodSetter("setFilter", 0) );
        xh.addRule("table/ref-by-table/association", xh.methodSetter("setAssociation", 0) );
        // above added at  2.0
        

        xh.addRule("table/column", xh.objectCreate("nds.schema.ColumnMapping") );
        xh.addRule("table/column", xh.addChild("addColumnMapping", null) ); // remove it from stack when done
        xh.addRule("table/column/name", xh.methodSetter("setName", 0) );
        xh.addRule("table/column/sum-method", xh.methodSetter("setSumMethod", 0) );
        xh.addRule("table/column/description", xh.methodSetter("setDescription", 0) );
        // --------- yfzhu added at 2003-05-23 for comment
        xh.addRule("table/column/comment", xh.methodSetter("setComment",0));

        xh.addRule("table/column/description2", xh.methodSetter("setDescription2", 0) );
        xh.addRule("table/column/type", xh.methodSetter("setType",0) );
        xh.addRule("table/column/nullable", xh.methodSetter("setNullable",0) );
        xh.addRule("table/column/mask", xh.methodSetter("setMask",0) );
        xh.addRule("table/column/ref-table", xh.methodSetter("setRefTable",0) );
        xh.addRule("table/column/ref-column", xh.methodSetter("setRefColumn",0) );
        xh.addRule("table/column/obtain-manner", xh.methodSetter("setObtainManner",0));
        xh.addRule("table/column/default-value", xh.methodSetter("setDefaultValue",0) );
        xh.addRule("table/column/object-name", xh.methodSetter("setObjectTable",0) );
        xh.addRule("table/column/object-column", xh.methodSetter("setTableColumnName",0) );
        xh.addRule("table/column/modifiable", xh.methodSetter("setModifiable",0) );
        xh.addRule("table/column/reg-expression", xh.methodSetter("setRegExpression",0) );
        xh.addRule("table/column/error-message", xh.methodSetter("setErrorMessage",0) );
        xh.addRule("table/column/interpreter", xh.methodSetter("setInterpreter",0) );
        //2.0
        xh.addRule("table/column/filter", xh.methodSetter("setFilter",0) );
        xh.addRule("table/column/display", xh.methodSetter("setDisplaySetting",0) );
        
        // above added at  2.0
        xh.addRule("table/column/limit-value", xh.methodSetter("addLimitValue",2) );
        xh.addRule("table/column/limit-value/desc", xh.methodParam(0) );
        xh.addRule("table/column/limit-value/value", xh.methodParam(1) );
        xh.addRule("table/column/limit", xh.methodSetter("setLimit",0) );//By hawke
        xh.addRule("table/column/money", xh.methodSetter("setMoney",0) );//By hawke

        xh.addRule("table/sum-field", xh.objectCreate("nds.schema.ColumnMapping") );
        xh.addRule("table/sum-field", xh.addChild("addSumField", null) ); // remove it from stack when done
        xh.addRule("table/sum-field/description", xh.methodSetter("setDescription", 0) );
        xh.addRule("table/sum-field/interpreter", xh.methodSetter("setInterpreter",0) );
        xh.addRule("table/sum-field/type", xh.methodSetter("setType",0) );

        xh.addRule("table/flink", xh.objectCreate("nds.schema.ColumnMapping") );
        xh.addRule("table/flink", xh.addChild("addFlink", null) ); // remove it from stack when done
        xh.addRule("table/flink/name", xh.methodSetter("setName",0) );
        xh.addRule("table/flink/description", xh.methodSetter("setDescription", 0) );
        xh.addRule("table/flink/nullable", xh.methodSetter("setNullable",0) );
        xh.addRule("table/flink/interpreter", xh.methodSetter("setInterpreter",0) );
        xh.addRule("table/flink/type", xh.methodSetter("setType",0) );
        xh.addRule("table/flink/ref-table", xh.methodSetter("setRefTable",0) );
        xh.addRule("table/flink/ref-column", xh.methodSetter("setRefColumn",0) );
        xh.addRule("table/flink/limit", xh.methodSetter("setLimit",0) );

        xh.addRule("table/alias-table", xh.objectCreate("nds.schema.AliasTable") );
        xh.addRule("table/alias-table", xh.addChild("addAliasTable", null) ); // remove it from stack when done
        xh.addRule("table/alias-table/name", xh.methodSetter("setName", 0) );
        xh.addRule("table/alias-table/real-table", xh.methodSetter("setRealTableName",0) );
        xh.addRule("table/alias-table/condition", xh.methodSetter("setCondition",0) );

        xh.addRule("table/trigger", xh.objectCreate("nds.schema.TriggerHolder") );
        xh.addRule("table/trigger", xh.addChild("setTriggers", null) ); // remove it from stack when done
        xh.addRule("table/trigger/before-modify", xh.methodSetter("setBeforeModify", 0) );
        xh.addRule("table/trigger/after-modify", xh.methodSetter("setAfterModify",0) );
        xh.addRule("table/trigger/before-delete", xh.methodSetter("setBeforeDelete",0) );
        xh.addRule("table/trigger/after-create", xh.methodSetter("setAfterCretae",0) );


        xh.readXml(stream, maps);
        return maps;
    }

    /**
     */
    public StringHashtable getMappings() {
        return mappings;
    }
    /**
     * Filter files with only specified extension
     */
    private class FileSeacher implements java.io.FilenameFilter {
        private File dir;
        private String ext;
        public FileSeacher(File dir, String extension) {
            this.dir=dir;
            this.ext=extension;
        }
        public boolean accept(File directory, String name) {
            if( dir.equals(directory) && name.toLowerCase().endsWith(ext))
                return true;
            return false;
        }
    }
}
