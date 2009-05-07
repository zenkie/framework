package nds.schema;

import java.io.FileOutputStream;
import java.util.Iterator;
import java.util.Vector;

import nds.util.StringHashtable;
import nds.util.StringUtils;
import nds.util.*;

public class VBXMLGenerator {
    private String dir="F:/work5/vb/pos/dist/conf/ora.all";
    private String srcDir= "file:///E:/aic/tables.mit4";
    private boolean lowerCase= true; // on unix platform all columns and table name should be lower case
    public VBXMLGenerator() throws Exception{
        gen();
    }
    private void gen()throws Exception{
        TableMappingManager manager= new TableMappingManager(srcDir);
        StringHashtable tableMaps=manager.getMappings();
        Vector c= new Vector(tableMaps.values());

        //sort by table name,that is, the toString() of map
        ListSort.sort(c);

        for(Iterator it=c.iterator();it.hasNext();) {

            TableMapping map=(TableMapping)it.next();
            createTableXML(map);
        }

    }
    private void createTableXML(TableMapping map) throws Exception{
        System.out.println("---"+ map.name+"----");
        String s= additionalHandle(map.toXML());

        FileOutputStream file= new FileOutputStream(dir+"/"+ (lowerCase?map.name.toLowerCase():map.name) +".table");
        file.write(s.getBytes());
        file.close();
    }
    /**
     Add addtion handling on table xml string
     */
    private String additionalHandle(String s){
        // replace "nds.security.PasswordInterpreter" with "Schema.PasswordInterpreter"
        s= StringUtils.replace(s,"nds.security.PasswordInterpreter","Schema.PasswordInterpreter");
        // replace "nds.security.Permissions" with "NopInterpreter"
        s= StringUtils.replace(s,"nds.security.Permissions","Schema.NopInterpreter");
        return s;
    }
    public static void main(String[] args) throws Exception{
        VBXMLGenerator VBXMLGenerator1 = new VBXMLGenerator();
    }
}