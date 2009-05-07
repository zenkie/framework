/******************************************************************
*
*$RCSfile: ToolsTest.java,v $ $Revision: 1.2 $ $Author: Administrator $ $Date: 2006/03/13 01:13:00 $
*
*$Log: ToolsTest.java,v $
*Revision 1.2  2006/03/13 01:13:00  Administrator
*no message
*
*Revision 1.1.1.1  2005/03/15 11:23:27  Administrator
*init
*
*Revision 1.2  2003/03/30 08:12:00  yfzhu
*Updated before subtotal added
*
*Revision 1.1.1.1  2002/11/29 02:50:35  yfzhu
*Active POS
*
*Revision 1.3  2001/11/10 04:12:33  yfzhu
*no message
*
*Revision 1.2  2001/11/07 20:58:48  yfzhu
*no message
*
*
********************************************************************/
package nds.util.test;

import java.util.Hashtable;
import java.util.Map;

import junit.framework.TestCase;
import nds.util.StringUtils;
import nds.util.Tools;
import nds.util.URIBuilder;

public class ToolsTest  extends TestCase {
    public ToolsTest(String name) {
        super(name);
    }
    public void testReplace(){
        String s= "abc abcabc";
        this.assertEquals(StringUtils.replace(s,"abc","yfzhu",-1), "yfzhu yfzhuyfzhu");
        this.assertEquals(StringUtils.replace(s,"abc","yfzhu",1), "yfzhu abcabc");
        this.assertEquals(StringUtils.replace(s,"abc","yfzhu",2), "yfzhu yfzhuabc");
        this.assertEquals(StringUtils.replace(s,"abc","yfzhu",3), "yfzhu yfzhuyfzhu");
        s="aaa";
        this.assertEquals(StringUtils.replace(s,"a","yfzhu",1), "yfzhuaa");
    }
    public void testGetDetailInfo(){
        Map map=System.getProperties();
        System.out.println(nds.util.Tools.getDetailInfo(map));
    }
    public void testGetInt(){
        String s="  121";
        int i= nds.util.Tools.getInt(s, -1);
        this.assertEquals(121, i);
    }
    public void testDecodeURIQuery(){
        String url= "c=1&b=2&s=3";
        Hashtable ht=new Hashtable();
        Tools.decodeURIQuery(url, ht, null);
        this.assertEquals(ht.size(), 3);
        this.assertEquals(ht.get("c"), "1");
        this.assertEquals(ht.get("b"), "2");
        this.assertEquals(ht.get("s"), "3");
    }
    public void testURIBuilder() throws Exception{
        String redirect= "http://localhost/c/portal/layout?p_l_id=1&p_p_id=ncp1&p_p_action=0&p_p_state=maximized&p_p_mode=view&_ncp1_iframe=true";

        URIBuilder ub= new URIBuilder(redirect, StringUtils.ISO_8859_1);
        System.out.println("ub11:"+ub.toDebugString());

        ub.setQuery("menu_root=item1");
        System.out.println("ub1:"+ub.toDebugString());
        System.out.println("redirect to :" + ub.getURI().toString());

    }
    public void testDecodeURIQuery2(){
        String s="l_id=1&p_p_id=10&p_p_action=0&p_p_state=maximized&p_p_mode=view&_10_struts_action=%2Faddress_book%2Fview_all";
        Hashtable ht=new Hashtable();
        Tools.decodeURIQuery(s, ht, null);
        System.out.println(Tools.toString(ht));

    }
    
}
