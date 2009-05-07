/******************************************************************
*
*$RCSfile: DisplaySettingTest.java,v $ $Revision: 1.2 $ $Author: Administrator $ $Date: 2005/12/18 14:06:17 $
*
*$Log: DisplaySettingTest.java,v $
*Revision 1.2  2005/12/18 14:06:17  Administrator
*no message
*
*Revision 1.1.1.1  2005/03/15 11:23:24  Administrator
*init
*
*Revision 1.1.1.1  2002/11/29 02:50:35  yfzhu
*Active POS
*
*Revision 1.4  2001/11/22 06:28:18  yfzhu
*no message
*
*Revision 1.3  2001/11/11 12:45:39  yfzhu
*no message
*
*Revision 1.2  2001/11/07 20:58:47  yfzhu
*no message
*
*
********************************************************************/
package nds.schema.test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;

import junit.framework.TestCase;
import nds.log.Logger;
import nds.log.LoggerManager;
import nds.schema.Column;

import nds.schema.*;

public class DisplaySettingTest  extends TestCase {
    private Logger logger=LoggerManager.getInstance().getLogger(DisplaySettingTest.class.getName());
    
    public DisplaySettingTest(String name) {
        super(name);
    }
    
    public void testText(){
    	
    	String[] s=new String[]{
    	"text(1,2)","radio(3)","check","textarea",
		"hr","blank","textarea(2,3)", "select"
    	};
    	String[] s2=new String[]{
    	"text(1,2)","radio(3,1)","check(1,1)","textarea(1,1)",
		"hr(1,1)","blank(1,1)","textarea(2,3)", "select(1,1)"
    	};
    	for(int i=0;i< s.length; i++){
    		//this.assertEquals((new DisplaySetting(s[i])).toString(),s2[i]);
    		//if (!(new DisplaySetting(s[i])).toString().equals(s2[i]))
    			//logger.error( "Found error when convert :"+ s[i]);
    	}
    }
   

}
