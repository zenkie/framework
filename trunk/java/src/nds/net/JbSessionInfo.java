package nds.net;

import java.util.Date;
import java.util.Properties;

import nds.util.Tools;
/**
* Contained connected jbsession to ndsjava (mit3)
*/
public class JbSessionInfo{
	public String ip;
	public String jbUserName;
	public Date jbLoginTime=null;
	public Date jbLogoutTime=null;
	public Properties props;
    public int status=1; // 1 for alive,2 for session end 3 for ip end
    public String toString(){
        return "JbSessionInfo(ip="+ip+",user="+jbUserName+",login="+jbLoginTime+
                ",logout="+jbLogoutTime+(props==null?"":",props="+Tools.toString(props));
    }
}