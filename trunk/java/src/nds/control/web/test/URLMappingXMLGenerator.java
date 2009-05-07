/******************************************************************
*
*$RCSfile: URLMappingXMLGenerator.java,v $ $Revision: 1.1.1.1 $ $Author: Administrator $ $Date: 2005/03/15 11:23:16 $
*
*$Log: URLMappingXMLGenerator.java,v $
*Revision 1.1.1.1  2005/03/15 11:23:16  Administrator
*init
*
*Revision 1.1.1.1  2002/11/29 02:50:34  yfzhu
*Active POS
*
*Revision 1.2  2001/11/07 20:58:46  yfzhu
*no message
*
*
********************************************************************/
package nds.control.web.test;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.HashMap;

import nds.control.web.URLMapping;

public class URLMappingXMLGenerator {

    public URLMappingXMLGenerator(String file,int urlCount, int screenCount) throws Exception{
        StringBuffer buf;
        buf=new StringBuffer(100000);
        buf.append(catHeader());
        for( int i=0;i< urlCount;i++)
            buf.append("\n"+catMappingItemURL(i+1));
        for( int i=0;i< screenCount;i++)
            buf.append("\n"+catMappingItemScreen(i+1));
        buf.append(catFooter());
        FileOutputStream fos=new FileOutputStream(file);
        OutputStreamWriter w= new OutputStreamWriter(fos);
        w.write(buf.toString());
        w.flush();
        fos.close();
        System.out.println("Successfully written to "+ file+ ", with URLMapping (SCREEN[1-"+screenCount+"] and URL[1-"+urlCount+"]");

    }
    private String catHeader(){
        String s="<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?> \n<mappings>\n";
        return s;
    }
    private String catFooter(){
        String s="\n</mappings>\n";
        return s;
    }
    private String catMappingItemURL(int num){
        String url,screen,req,flow;
        url="URL"+num;
        screen=(""+RandomGen.getRandomString(20)).toUpperCase();
        return newMappingItem(url,screen);
    }
    private String newMappingItem(String url,String screen){
        String req=null,flow=null,event=null,nextscreen=null;
        boolean sec;
        HashMap flowItems=null;
        int i=RandomGen.getInt(1,2);
        switch(i){
            case 1:  req= RandomGen.getRandomString(50); break;
            case 2:  event=RandomGen.getRandomString(50); break;
        }
        i=RandomGen.getInt(1,3);
        switch(i){
            case 1:  flow=RandomGen.getRandomString(50);break;
            case 2:  nextscreen=RandomGen.getRandomString(20).toUpperCase(); break;
            case 3:
                flowItems=new HashMap();
                int j= RandomGen.getInt(2,5);
                for(int k=0;k<j;k++){
                    flowItems.put(RandomGen.getRandomString(20).toUpperCase(),RandomGen.getRandomString(20));
                }
                break;
        }
        sec= RandomGen.getRandomBoolean();
        URLMapping map=new URLMapping(url,screen,req,flow,event,nextscreen,flowItems,sec);
        return map.toString();

    }
    private String catMappingItemScreen(int num){
        String url,screen,req,flow;
        url=RandomGen.getRandomString(50);
        screen="SCREEN"+ num;//""+RandomGen.getRandomString(20)).toUpperCase();
        return newMappingItem(url,screen);
    }

    public static void main(String args[])throws Exception{
       new URLMappingXMLGenerator("f:/urlmappings.xml", 10,10);
    }


}