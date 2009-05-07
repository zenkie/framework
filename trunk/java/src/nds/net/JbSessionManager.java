package nds.net;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Vector;

import nds.log.Logger;
import nds.log.LoggerManager;
import nds.query.QueryEngine;
import nds.util.Tools;

/**
* Contained connected jbsessions to ndsjava (mit3)
* There's 2 condition that information will be written to database:
*  the vpn ip disconnected,
*  the jabber session closed and after 30 minute interval 'if during that interval,
*             same jb session connected again, will take as not disconnected, and not
*             log to db.
*/
public class JbSessionManager{
    private static Logger logger= LoggerManager.getInstance().getLogger(JbSessionManager.class.getName());
	private Hashtable ips=new Hashtable();//key:ip, value Date(connected)
	private Vector sessions=new Vector(); //elements are JbSessionInfo
    private final static long LONG_DYING_INTERVAL=1000*60*15 ;// after 30 minutes,the died session will be removed
    private final static String INSERT_POSLOG= "insert into poslog(id,CUSTOMERID,IPADDRESS,LOGINTIME,DURATION,APPVERSION,REMARK) values "+
                              "( get_sequences('poslog'),get_customer_by_jbname(?), ?,to_date(?,'YYYY-MM-DD-HH24-MI-SS'),?,?,?)";
    private final static SimpleDateFormat dateTimeSecondsFormatter=new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
    private Runnable r= new Runnable(){
        public void run(){
            // sleep every LONG_DYING_INTERVAL milliseconds to check deleted sessions to log to db
            try{
                Thread.sleep(LONG_DYING_INTERVAL);
                removeClosedSessions();
            }catch(Exception e){
                logger.error("Found error"+ e);
            }
        }
    };
    public JbSessionManager(){
        try{
            Thread t=new Thread(r);
            t.setDaemon(true);
            t.setName("JbSessionWatcher");
            t.start();
            logger.info("Jabber Session Manager started, will check close session every " + (LONG_DYING_INTERVAL/60000)+" seconds.");
        }catch(Exception e){
            logger.error("Could not start watcher:"+ e);
        }
    }
    /**
	* @return elements are JbSessionInfo, include those has statu=2
	*/
	public Vector getAliveSessions(String ip){
    	Vector v=new Vector();
        for(int i=sessions.size()-1;i>=0 ;i--){
            JbSessionInfo si= (JbSessionInfo)sessions.elementAt(i);
            if (si.status !=3 && si.ip.equals(ip)){
            	v.addElement(si);
            }
        }
		return v;
	}
	/**
	* ip will be equal or more than session ips ( some longly connected ip, such as in lan)
	* @return key:ip, value Date(connected)
	*/
	public Hashtable getAliveIPs(){
		return ips;
	}
	public void VPNConnected(String ip, Date time){
		ips.put(ip,time);
	}
    /**
     * Get and remove close sessions, elements are JbSessionInfo
     * will also check whether jb session disconnected a long time(30minutes)
     */
    public Vector removeClosedSessions(){
    	Vector v=new Vector();
        for(int i=sessions.size()-1;i>=0 ;i--){
            JbSessionInfo si= (JbSessionInfo)sessions.elementAt(i);
            if (si.status==3 ){
            	v.addElement(si);
            	sessions.remove(i);
            }else if(si.status ==2 && System.currentTimeMillis() -si.jbLogoutTime.getTime()>LONG_DYING_INTERVAL  ){
                si.status=3;
                logSessionInfo(si);
                v.addElement(si);
                sessions.remove(i);
            }
        }
		return v;
    }
    /**
     * Write to db
     */
    private void logSessionInfo(JbSessionInfo si){
        if( si.status !=3) return;
/*        private final static String INSERT_POSLOG= "insert into poslog(id,CUSTOMERID,IPADDRESS,LOGINTIME,DURATION,APPVERSION,REMARK) values "+
                                  "( get_sequences('poslog'),get_customer_by_jbname(?), ?,?,?,?,?)";
*/
        logger.debug("logSessionInfo: "+ si);

        PreparedStatement pstmt=null;
        Connection conn=null;
        try{
            QueryEngine engine=QueryEngine.getInstance();
            conn=engine.getConnection();
            pstmt=conn.prepareStatement(INSERT_POSLOG);
            pstmt.setString(1, si.jbUserName );
            pstmt.setString(2,si.ip );
            System.out.println("JbSessionManager.logSessioInof:"+ si.jbLoginTime.getTime() +",sqlDate="+(new java.sql.Date( si.jbLoginTime.getTime() )));
            java.sql.Date d=new java.sql.Date(si.jbLoginTime.getTime());
            System.out.println("JbSessionManager.logSessioInof:sqlTime=" +d.getTime()+", format="+dateTimeSecondsFormatter.format(d));

            pstmt.setString(3, dateTimeSecondsFormatter.format(si.jbLoginTime) );

            pstmt.setLong(4,(si.jbLogoutTime.getTime() -si.jbLoginTime.getTime() )/1000);
            String appv=null, prop;
            if( si.props !=null){
                appv= si.props.getProperty("LocalVersion");
                prop=Tools.toString(si.props);
            }
            else {
                appv="";
                prop="";
            }
            pstmt.setString(5, appv);
            pstmt.setString(6, prop);
            pstmt.execute();
        }catch(Exception e){
            logger.error("Could not save Jabber session infomation:"+si, e);
        }finally{
            if(pstmt!=null){try{pstmt.close();}catch(Exception e2){}}
            if(conn!=null){try{conn.close();}catch(Exception e2){}}
        }

    }
	public void VPNDisconnected(String ip, Date time){
        // find all jbSession info in sessions, and mark there status
        for(int i=0;i< sessions.size();i++){
            JbSessionInfo si= (JbSessionInfo)sessions.elementAt(i);
            if (si.ip.equals(ip)) {
                si.status=3;
                si.jbLogoutTime = time;
                logSessionInfo(si);

            }
        }
		ips.remove(ip);
	}
	public void jbSessionStart(String jbUser , String ip , Date t){
		if( ips.get(ip)==null) ips.put(ip,t);
		// if sessions already contained jbUser/ip, then do nothing

        logger.debug("jbSessionStart: jbUser="+ jbUser+",ip="+ip+",date="+ t);

        JbSessionInfo si;
		for(int i=0;i< sessions.size();i++){
			si=(JbSessionInfo)sessions.elementAt(i);
			if (si.ip.equals(ip) && si.jbUserName.equalsIgnoreCase(jbUser)){
				si.status=1;
                si.jbLogoutTime = null;
				return;
			}
		}
		si=new JbSessionInfo();
		si.ip=ip;
		si.jbUserName=jbUser;
		si.jbLoginTime=t;
		sessions.addElement(si);

	}
	public void jbSessionEnd(String jbUser ,Date t){
	/*收到jabber  session end , 在jabber list中记录通讯结束时间（标记jabber 为准备结束状态），
		*/
		JbSessionInfo si=getSessionInfo(jbUser);
		if (si !=null){
			si.status=2;
			si.jbLogoutTime=t;
		}
        logger.debug("jbSessionEnd: use="+ jbUser+",date="+ t);

	}
	private JbSessionInfo getSessionInfo(String jbUser){
		JbSessionInfo si;
		for(int i=0;i< sessions.size();i++){
			si=(JbSessionInfo)sessions.elementAt(i);
			if (si.jbUserName.equalsIgnoreCase(jbUser)){
				return si;
			}
		}
		return null;
	}
	public void jbMessage(String jbUser, String name, String value){
		JbSessionInfo si=getSessionInfo(jbUser);
		if (si !=null){
			if(si.props==null) si.props=new Properties();
			si.props.setProperty(name, value);
		}
        logger.debug("jbMessage: use="+ jbUser+","+ name+"="+ value);
	}
}