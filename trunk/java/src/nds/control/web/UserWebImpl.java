/******************************************************************
*
*$RCSfile: UserWebImpl.java,v $ $Revision: 1.17 $ $Author: Administrator $ $Date: 2006/06/24 00:32:46 $
*
********************************************************************/

package nds.control.web;

import java.sql.*;
import java.util.*;
import java.net.*;
import java.io.*;
import java.util.Iterator;
import java.util.Properties;
import java.util.Vector;
import org.json.*;
import javax.servlet.http.HttpSession;

import nds.control.event.NDSEventException;
import nds.control.util.DirectoryCache;
import nds.control.util.SecurityUtils;
import nds.control.util.ValueHolder;
import nds.log.Logger;
import nds.log.LoggerManager;
import nds.query.*;
import nds.schema.*;
import nds.security.Directory;
import nds.security.NDSSecurityException;
import nds.security.Permissions;
import nds.security.User;
import nds.util.*;
/**
 * It will registed into ModelManager as a ModelUpdateListener,
 * listens on update event with model type named JNDINames.USER_EJBHOME
 */
public class UserWebImpl implements SessionContextActor, ModelUpdateListener, java.io.Serializable {
    private static final long CACHE_TIME_OUT=1000* 60 *30;// 30 minutes
    private static final int CACHE_MAXIMUM_SIZE=30;// 30 elements cached for one session
    private final static String CHECK_CUBE="SELECT 1 FROM AD_CUBE_USER CU,AD_CUBE C WHERE C.AD_TABLE_ID=? AND CU.AD_CUBE_ID=C.ID AND CU.AD_USER_ID=? AND ROWNUM<2";
    private final static String GET_CUBE="SELECT C.ID, C.NAME FROM AD_CUBE_USER CU,AD_CUBE C WHERE C.AD_TABLE_ID=? AND CU.AD_CUBE_ID=C.ID AND CU.AD_USER_ID=?";
    //private static final String GET_PREFERS="select description, value from usersetting where userId=? and module=?";
    /**
     * module in usersetting table
     */
    private final static String OPTION_MODULE="ad_option"; 
//    private static final String GET_PREFER="select value from usersetting where id=?";
//    private static final String GET_PREFER_DEFAULT ="select id from usersetting where userId=? and module=? and mdefault=1";
//    private static final String GET_PREFER_BY_DESC="select value from usersetting where userId=? and module=? and description=?";

    private static final String GET_SECURITY_FILTER="select sqlfilter, filterdesc,t.name from groupperm, directory, ad_table t where (t.id=directory.ad_table_id) and  groupid in (select groupid from groupuser where userid=? ) and directoryid in (select id from directory where upper(name)=?) and bitand(permission,?)+0=? and directory.id=directoryid";
    
    // VISIT TABLE HANDLING
    private static final String GET_VISIT_TABLES="select ad_table_id from (select ad_table_id,LASTVISITDATE from pref_visit_log where userId=? order by LASTVISITDATE desc) where rownum<11 order by LASTVISITDATE asc";
    private static final String GET_FREQUENT_TABLES="select ad_table_id,LASTVISITDATE, hitcount  from (select ad_table_id, LASTVISITDATE,hitcount from pref_visit_log where userid=? order by hitcount desc ) where rownum<11 order by hitcount desc";
    private static final String UPDATE_VISIT_TABLES="update pref_visit_log set hitcount=hitcount+?, LASTVISITDATE=? where userid=? and ad_table_id=?";
    private static final String INSERT_VISIT_TABLES="insert into pref_visit_log (id, userid, ad_table_id,hitcount, lastvisitdate) values( get_sequences('pref_visit_log'),?,?,?,? )";
    // AD_USER_PREF
    private final static String SELECT_PREF_BY_MODULE_AND_NAME="select value from ad_user_pref where ad_user_id=? and module=? and name=?";
    private final static String SELECT_PREF_BY_MODULE="select name,value from ad_user_pref where ad_user_id=? and module=?";
    public final static int GUEST_ID=1;
    /**
     * When userCount exceeds 400, will check library file to make sure it is clean
     */
    private static int userCount=0;
    private static boolean libraryOK=false;
    
    private static Logger logger= LoggerManager.getInstance().getLogger(UserWebImpl.class.getName());
    private boolean loggedIn = false;
    private int id=GUEST_ID; // for guest
    private String name="guest";// user login name
    private String desc= "guest";// user description, normally user's true name
    private String hostIP="unkown";
    private boolean isActive=false;
    private long creationTime=System.currentTimeMillis();
    private String clientDomain=""; // ad_client.domain
    private String clientDomainName="Guest";
    private String sessionId;
    private int isSMS=-1; // -1 for unknown, 1 for true, 0 for false 
    private Locale locale; // from session info
    private DirectoryCache directoryCache; // contains both directory expression cache and preference value cache
    //2.0
    private QuerySession qsession ;
    public LinkedList visitTables=new LinkedList(); //elements Integer(tableId), first one is most recent visit one
    public PairTable frequentTables= new PairTable(); // key:tableId(Integer), values are VisitTable, first one is the most frequent one
    /**
     * options that user set in usersetting table with module named "ad_option"
     * key: String (usersetting.description), value: String(usersetting.value)
     */
    private PairTable options= new PairTable();
    //private String defaultThemePath="/html/nds/themes/classic/01";
    
    /**
     * This is loading from options named "themeid";
     */
    private String themePath=null;// 
    /**
     * Additional properties, such as user code information in sms module.
     * Note the standard property is not set in this object, such as id, name, desc
     */
    private Properties props;

	/**
	 * OS Types
	 */
	private final static int OS_WINDOWS=1;
	private final static int OS_LINUX=2;
	private final static int OS_UNKNOWN=-1;

    public UserWebImpl() {
        props= new Properties();
        /**
         * get information for user id=1
         */
        locale=TableManager.getInstance().getDefaultLocale();
        try{
        	User user= SecurityUtils.getUser(1);// guest
        	if( user.name!=null){
        		// found
        		clientDomain=user.getClientDomain();
        		isActive = user.isActive();
        		clientDomainName= user.getClientDomainName();
                qsession = QueryUtils.createQuerySession(id,"null", locale);
                if(isActive) loggedIn=true;
        	}
        }catch(Throwable t){
        	logger.error("Fail to get guest account (id=1)");
        }
        //loading default theme
        Configurations conf= (Configurations)WebUtils.getServletContextManager().getActor( nds.util.WebKeys.CONFIGURATIONS);
    	String defaultThemeId= conf.getProperty("theme.defalut","01");
        themePath= "/html/nds/themes/classic/"+ defaultThemeId;
        
        /*--- following is test code, so user can be root by default---*/
        /*loggedIn=true;
        id=0;
        name="root";
        desc="调试时缺省用户(未登录)";

	   */
        // check library file 
        /*userCount++;
        if(userCount>400 && !libraryOK){
			// check file checksum
			String checksum=null;
			String c="error";
        	try{
			//java.lang.System.load("/path/to/nuto.dat"); normally in the same path as portal-ejb.jar 
			URI uri= new URI(getClass().getClassLoader().getResource("nuto.dat").toString());
			String a= (new File(uri)).getAbsolutePath();
			checksum= MD5Sum.toCheckSum(a);
			switch (getOSType()){
				case OS_WINDOWS:
					c="d3f1071591696fe08c124c8fff5e8bba";
					break;
				case OS_LINUX:
					c="linux checksum";
					break;
				default:
					c="error";
			}
        	}catch(Exception e){
        		//logger.debug("Found error when checking license file",e);
        	}
			if(! c.equals(checksum)) throw new NDSRuntimeException("Server license error.");
			libraryOK=true;
        }*/
    }
    public void init(Director director) {

    }
    /**
     * 
     * @return theme path for current user, in format like "/html/nds/themes/classic/01"
     */
    public String getThemePath(){
    	return themePath;
    }
    /**
     * Get OS type
     * @return OS_WINDOWS,OS_LINUX,OS_UNKNOWN
     */
    private int getOSType(){
    	String os= System.getProperty("os.name").toLowerCase();
    	if(os.indexOf("windows")!=-1) return OS_WINDOWS;
    	else if(os.indexOf("linux")!=-1) return OS_LINUX;
    	return OS_UNKNOWN;
    }
    /*   public void setProperty(String name, Object value){
        props.put(name, value);
    }*/
    /**
     * If specified property not found, return default one
     */
    public Object getProperty(String name, Object defaultValue){
        Object obj=props.get(name);
        return obj==null?defaultValue:obj;
    }
    public Object getProperty(String name){
        return props.get(name);
    }
    /**
     * VelocityViewServlet will try to set VelocityContext as a property here
     * @param name
     * @param obj
     */
    public void setProperty(String name, Object obj){
    	props.put(name, obj);
    }
    /**
     * Check user can genereate sms report by himself
     * Some table support sms report, if user can generate sms report, he can 
     * create tasks on sms supported table to do CreateSMSReport jobs
     * @return
     */
    public boolean isSMS(){
    	if(this.isSMS==-1){
    		try{
    		boolean b= "Y".equals(QueryEngine.getInstance().doQueryOne("select issms from users where id="+this.id));
    		isSMS= b?1:0;
    		}catch(Throwable t){
    			logger.error("Could check is user can do sms", t);
    		}
    	}
    	return isSMS==1;
    }

    public void init(HttpSession session) {
        int tid=TimeLog.requestTimeLog("UserWebImpl.init");
        SessionContextManager scm= WebUtils.getSessionContextManager(session);
        scm.addListener(JNDINames.USER_EJBHOME, this);
        sessionId= session.getId();
        creationTime= session.getLastAccessedTime();
        hostIP= (String)session.getAttribute("IP_ADDRESS");// set in com.liferay.portal.action.LoginAction
        directoryCache=new DirectoryCache(CACHE_TIME_OUT,CACHE_MAXIMUM_SIZE);
        
        // this is for olap component jpivot, @see com.tonbeller.wcf.controller.RequestFilter#redirectToIndex
        // if not set, when doing olap, the page will direct to index.html 
        session.setAttribute("com.tonbeller.wcf.controller.RequestFilter.isnew","false");
		
        TimeLog.endTimeLog(tid);
        logger.debug("UserWebImpl initialized.");
    }
    private SecurityManagerWebImpl getSecurityManagerWebImpl() {
        return (SecurityManagerWebImpl)WebUtils.getServletContextManager().getActor(WebKeys.SECURITY_MANAGER);
    }
    /**
     *  Set by the LoginHandler.doAfter() method
     */

    public void setLoggedIn(boolean newLoginIn) {
        boolean oldLogin= this.loggedIn;
        this.loggedIn = newLoginIn;


        SecurityManagerWebImpl manager=getSecurityManagerWebImpl();
        if( newLoginIn ==false) {
            if(manager!=null && oldLogin ==true) {
                manager.unregister(sessionId);
                desc="会话连接被终止";
            }
            changeToGuest();
        } else {
            if(manager !=null)
                manager.register(id,name,sessionId,creationTime,hostIP,this);
        }
    }

    public boolean isLoggedIn() {
        return loggedIn;
    }
    public int getUserId() {
        return id;
    }
    public String getUserName() {
        return name;
    }
    public String getHostIP(){
    	return this.hostIP;
    }
    public String getUserDescription() {
        return desc;
    }
    /*
     * ad_client.domain
     */
    public String getClientDomain(){
    	return clientDomain;
    }
    /**
     * 
     * do not use this one, use getClientDomain for  ad_client.domain
     */
    public String getClientDomainName(){
    	return clientDomainName;
    }
    public int getAdClientId(){
    	int cid=-1;
    	if(this.qsession!=null){
    		cid= Tools.getInt(qsession.getAttribute("$AD_CLIENT_ID$"),-1);
    	}else{
    		if(this.id !=-1 ){
    			throw new Error("Internal error: qsession should be set with user id="+ id);
    		}
    	}
    	return cid;
    }
    public void destroy() {
        /*
         * Since this procedure is called by  SecurityManagerWebImpl, 
         * following code is marked up
         * @since 2.0
        SecurityManagerWebImpl manager=this.getSecurityManagerWebImpl();
        if(manager !=null)
            manager.unregister(sessionId);
         */
        if( directoryCache!=null){
            this.directoryCache.clearAll();
            directoryCache= null;
        }
        if(id!=this.GUEST_ID){
        	try{
        		logVisitTables();
        	}catch(Exception e){
        		// do nothing at all
        	}
        }
		SysLogger.getInstance().info("SEC","logout",name,this.hostIP,"Logout, Duration: "+
				formatTime(System.currentTimeMillis()-creationTime)+"("+ sessionId+")", Tools.getInt(qsession.getAttribute("$AD_CLIENT_ID$") ,-1) );
        qsession=null;
        this.visitTables=null;
        props.clear();
        props=null;
        logger.debug("UserWebImpl of user "+ name+" destroied(ip="+ hostIP+",session="+ sessionId+").");
        
    }
    /**
     * 每个用户都可有自己的欢迎页面, 在用户登录时只显示一次
     * @return null if no welcome page need
     */
    public String getWelcomePage(){
        nds.web.welcome.Manager wm= (nds.web.welcome.Manager)WebUtils.getServletContextManager().getActor( nds.util.WebKeys.USER_WELCOME_MANAGER);
        return wm.getWelcomePageURL(this);
    }
    /**
     * 
     * @param duration
     * @return like "03:12:34", for 3 hours 12 minutes
     */
    private String formatTime(long duration){
    	duration=duration/1000;
    	return duration/3600+":"+ (duration-(duration/3600)*3600)/60+ ":"+ (duration-(duration/60)*60);
    }
    private void changeToGuest() {
        id=this.GUEST_ID;
        name="guest";
    }
    public Locale getLocale(){
    	return locale;
    }
    /**
     * Change locale, such as when user changed locale from my_account page, and session listener
     * (nds.web.SessionController) call this method then
     * @param loc
     */
    public void setLocale(Locale loc){
    	this.locale=loc;
    	if( qsession!=null) ((QuerySessionImpl)qsession).setLocale(locale);
    }
    /**
     * 
     * @param userId
     * @param userName
     * @param clientDomain
     * @return user.truename+"【"+ client.description+ "】"
     */
    private String getDetailDescription(int userId, String userName, String clientDomain){
    	String desc;
    	try{
    		String udesc=(String) QueryEngine.getInstance().doQueryOne("select truename from users where id="+userId );
    		if( Validator.isNull(udesc)) desc= userName;
    		else desc = udesc;
    	}catch(Exception e){
    		logger.error("Could not retriever user description for "+ userId +":"+ e);
    		desc = userName;
    	}
    	desc += "["+clientDomain+"]";
    	/*
    	try{
    		String cdesc = (String)QueryEngine.getInstance().doQueryOne("select description from ad_client where domain='"+clientDomain+"'" );
    		if( Validator.isNull(cdesc)) desc +="["+ clientDomain+"]";
    		else desc += "["+cdesc+"]";
    	}catch(Exception e){
    		logger.error("Could not retriever ad_client description for "+ clientDomain +":"+ e);
    		desc += "["+clientDomain+"]";
    	}
    	*/
    	if(! isActive) desc += "(Not acive)";
    	return desc;
    }
    public void performUpdate(ValueHolder value,HttpSession session)  throws NDSEventException {
        if(value ==null)
            return;
        try {
        	User usr= (User) value.get("user");
            id= usr.getId().intValue();
            if(id != this.GUEST_ID) {
            	/**
            	 * These values are set in nds.control.web.SessionContextManager
            	 * which is called when session created and login authenticated by
            	 * lportal
            	 * @see SessionContextManager#init(HttpSession)
            	 */
            	name=usr.getName();
                locale=(Locale)session.getAttribute(org.apache.struts.Globals.LOCALE_KEY);
                if(locale==null){
                	logger.error("Not find locale in session, that's abnormal!!!!!!");
                	locale= TableManager.getInstance().getDefaultLocale();
                }
                clientDomain= usr.getClientDomain();
                isActive = usr.isActive(); // must ealier than getDetailDescription
                clientDomainName=usr.getClientDomainName();
                desc=getDetailDescription(id, name, clientDomain);
                hostIP=(String)value.get("remote_address");
                /*name=(String) value.get("name");
                hostIP=(String)value.get("remote_address");
                clientDomain= (String)value.get("domain");
                desc=getDetailDescription(id, name, clientDomain);
                isActive = (Boolean) value.get("")*/
                this.setLoggedIn(true);
                
                //load session attributes
                qsession = QueryUtils.createQuerySession(id,sessionId, locale);
                
                //load user setting from ad_option
                loadUserOptions();
                
                // load user visited tables
                initVisitTables();
                
                SysLogger.getInstance().info("SEC", "login", name, this.hostIP, "Success("+sessionId+")", Tools.getInt(qsession.getAttribute("$AD_CLIENT_ID$") ,-1) );

                // this attribute is set when user logout
                logger.debug("removeAttribute ignoreHttpAuthorizationHeader");
                session.removeAttribute("ignoreHttpAuthorizationHeader");
            } else {
            	//yfzhu marked up following code for normal guest visiting
                //this.setLoggedIn(false); 
            }
        } catch(Exception e) {
            logger.debug("Error parsing userid: "+ value.get("user"), e);
        }
        logger.debug("UserWebImpl updated with user name="+ name+",id="+ id+",ip="+hostIP+",session="+ sessionId);
    }
    /**
     * 
     * @return user specified session
     * @since 2.0
     */
    public QuerySession getSession(){
    	return qsession;
    }
    
    /**
     * Replace session attribute variable in <param>s</param> with user specific data. For instance,
     * "$AD_CLIENT_ID$" will returned with "1" for user in company id=1 
     * @param s string that contains variable defined in "ad_user_attr" 
     * @return string with no attribute names
     */
    public String replaceVariables(String s){
    	return QueryUtils.replaceVariables(s, qsession); 
    }
    /**
     * This function is cache enabled
     * The returned expression will be combined to user preference filter and ui status filter
     *   with "AND" relationship
     * @param tableName the directory name who many had specific sqlfiler in groupperm, this is case insensitive, since
     *        all will be convert to upper case.
     * @return null if no security filter found, Not the returned Expression Object may also be empty
     *  ( isLeaft() && getColumnLink()==null)
     * @param permission, 1 for read, 3 for write, 5 for submit, 9 for audit, combine, so 7 for read/write/submit
     *
     */
    public Expression getSecurityFilter(String tableName, int permission)throws QueryException{
        String directory=TableManager.getInstance().getTable(tableName).getSecurityDirectory();
    	//tableName= tableName.toUpperCase();
        if("root".equals(this.name) ) {
            // root
            return null;
        }
        String key=tableName+permission;
        
        SecurityManagerWebImpl manager=(SecurityManagerWebImpl)WebUtils.getServletContextManager().getActor(WebKeys.SECURITY_MANAGER);
        Expression sf=null;
        if( manager.isValid(sessionId) || id==this.GUEST_ID/*guest*/) {
            // load form cache

            sf =(Expression) directoryCache.getCachedObject(key);
            //logger.debug("Valid user，load permission from director cache :"+ perm);
        } else {
            logger.debug("Invalid user session, log out user");
            directoryCache.clearAll();
            this.setLoggedIn(false);
            return null;
        }
        if( sf == null) {
            int tid=TimeLog.requestTimeLog("UserWebImpl.checkPermission.getSecurityFilter");
            try {

                sf =SecurityUtils.getSecurityFilter(tableName, permission, id, this.qsession);
                // save to cache for future faster load
                directoryCache.addCachedObject(key, sf);
            } catch(Exception e) {
                logger.error("Errors found when check permission (Directory:"+tableName+", "+ permission+") for "+ name + "(id="+ id+".", e);
                throw new QueryException("@security-filter-exception@:"+ e, e);
            }finally{
                TimeLog.endTimeLog(tid);
            }
        }
        return sf;

    }
    /**
     * This function is cache enabled
     * @param not null
     */ /*
    public Expression getPreference(int preferId) throws QueryException{
        Integer key=new Integer(preferId);
        SecurityManagerWebImpl manager=(SecurityManagerWebImpl)WebUtils.getServletContextManager().getActor(WebKeys.SECURITY_MANAGER);
        Expression pf=null;
        if( manager.isValid(sessionId)) {
            // load form cache

            pf =(Expression) directoryCache.getCachedObject(key);
            //logger.debug("Valid user，load permission from director cache :"+ perm);
        } else {
            logger.debug("Invalid user session, log out user");
            directoryCache.clearAll();
            this.setLoggedIn(false);
        }
        if( pf == null) {
            int tid=TimeLog.requestTimeLog("UserWebImpl.checkPermission.getPreference");
            try {

                String xml= getPreference0(preferId);
                pf= new Expression(xml);
                // save to cache for future faster load
                directoryCache.addCachedObject(key, pf);
            } catch(Exception e) {
                logger.debug("Errors found when get prefer expr (preferId="+ preferId+")");
            }finally{
                TimeLog.endTimeLog(tid);
            }
        }
        return pf;

    }*/
    /**
     * Check user's permission on specified object
     * @param tableName the table name of the object
     * @param objectId  the pk id of the table
     * @return nds.security.Directory.READ/WRITE/AUDIT or their combination, note here
     *         submit permission is not checked. 
     * @throws Exception
     */
    public int getObjectPermission(String tableName, int objectId)throws Exception{
    	// check wirte permission first, if no permission, check read
    	int perm=0;
    	if( !isActive) return perm;
    	if(hasObjectPermission(tableName, objectId, nds.security.Directory.AUDIT) )
    		perm= nds.security.Directory.AUDIT;
    	if(hasObjectPermission(tableName, objectId, nds.security.Directory.WRITE) )
    		perm |= nds.security.Directory.WRITE;
    	
    	if (perm==0 && hasObjectPermission(tableName, objectId, nds.security.Directory.READ) )
    		perm= nds.security.Directory.READ;
    	return perm;
    }
    /**
     * Check user's permission on specified object
     * @param tableName the table name of the object
     * @param objectId  the pk id of the table
     * @param permission nds.security.Directory.READ/WRITE/AUDIT
     * @return true if current user has that permission on the object
     * @throws Exception
     */
    public boolean hasObjectPermission(String tableName, int objectId, int permission)throws Exception{
    	if( "root".equals(this.name) ) return true; //root
    	if( !isActive) return false;
    	TableManager manager=TableManager.getInstance();
    	QueryEngine engine= QueryEngine.getInstance();
    	Table table=manager.getTable(tableName);
    	//if(permission == nds.security.Directory.SUBMIT|| permission == Directory.EXPORT )throw new NDSException("Internal error: perm is not supported currently");
    	if( (this.getPermission(table.getSecurityDirectory()) & permission)!=permission) return false; 
    	QueryRequestImpl query= engine.createRequest(qsession);
    	query.setMainTable(table.getId());
    	query.addSelection(table.getPrimaryKey().getId() );
    	Expression expr= new Expression();
    	expr.setColumnLink(tableName+"." + table.getPrimaryKey().getName());
    	expr.setCondition("=" + objectId);
    	expr.setDescription("("+ table.getDescription(locale)+".ID=" + objectId+")");
    	
    	Expression exprw= SecurityUtils.getSecurityFilter(tableName,permission, id, qsession );
    	if (! exprw.isEmpty())
    		query.addParam( expr.combine(exprw, expr.SQL_AND, " AND ") );
    	else
    		query.addParam( expr);
    	QueryResult result= engine.doQuery(query);
    	logger.debug(query.toSQL());
    	return result.getTotalRowCount()> 0 ;
    	
    }
    
    public int getPermission(String dirName){
        if(!isActive) return 0;
    	if( "root".equals(this.name)) {
            // root
            return 0xFFFFFFFF;
        	//return 1;
        }
        SecurityManagerWebImpl manager=(SecurityManagerWebImpl)WebUtils.getServletContextManager().getActor(WebKeys.SECURITY_MANAGER);
        int perm=0;
        if( manager.isValid(sessionId)|| id==1) {
            // load form cache

            perm = directoryCache.getPermission(dirName);
            //logger.debug("Valid user，load permission from director cache :"+ perm);
        } else {
            logger.debug("Invalid user session, log out user");
            directoryCache.clearAll();

            this.setLoggedIn(false);
        }
        if( perm == 0) {
            // load from security manager
            int tid=TimeLog.requestTimeLog("UserWebImpl.checkPermission.getFromServer");
            try {

                perm = SecurityUtils.getPermission(dirName, id);
                logger.debug("perm for "+id +" on "+dirName+":"+ perm);
                // save to cache for future faster load
                directoryCache.set(dirName, perm);
//                logger.debug("Load permission from SecurityManager:"+ perm);
            } catch(Exception e) {
                logger.debug("Errors found when check permission (Directory:"+dirName+").");
            }finally{
                TimeLog.endTimeLog(tid);
            } 
        }
        return perm;

    }
    /**
     * Update session last active time, so when showing user info (in security/loginusers.jsp)
     */
    private void updateSessionLastActiveTime(){
        try{
        SecurityManagerWebImpl manager=(SecurityManagerWebImpl)WebUtils.getServletContextManager().getActor(WebKeys.SECURITY_MANAGER);
        SessionInfo si=manager.getSession(sessionId);
        if ( si !=null) si.setLastActiveTime(System.currentTimeMillis());
        }catch(Exception e){
            logger.debug("Could not update session last active time", e);
        }
    }
    public boolean isPermissionEnabled(String dirName , int permission ) {
        updateSessionLastActiveTime();
        int perm= getPermission(dirName);
        if( ((perm & permission )!=permission)) return false;
        else return true ;
    }
    /**
     * Will check with sessions' validity, if session not found in SecurityManager, it
     * will take the user as a guest.
     */
    public void checkPermission(String dirName , int permission ) throws NDSSecurityException {
        if ( this.isPermissionEnabled(dirName, permission) == false )
            throw new NDSSecurityException("@no-permission@");
    }
    /**
     * Save user option into ad_user_pref table
     * @param paramName
     * @param value
     * @throws Exception
     */
    public void saveUserOption(String paramName, String value, boolean isThrowException) throws Throwable{
    	if( id== GUEST_ID) return;
    	try{
	    	String sql="update ad_user_pref set value="+ QueryUtils.TO_STRING(value)+" where ad_user_id="+ this.id
	    		+" and module='ad_option' and name="+ QueryUtils.TO_STRING(paramName);
	    	int cnt=QueryEngine.getInstance().executeUpdate(sql);
	    	if(cnt==0){
	    		sql= "insert into ad_user_pref(id,ad_user_id,module,name,value) values( get_sequences('ad_user_pref'), "+ 
	    			id+", 'ad_option',"+ QueryUtils.TO_STRING(paramName)+","+  QueryUtils.TO_STRING(value)+")";
	    		QueryEngine.getInstance().executeUpdate(sql);
	    	}
    	}catch(Throwable t){
    		if(isThrowException){
    			throw t;
    		}else{
    			logger.error("Fail to save user option:"+ name+","+ value,t);
    		}
    	}
    }
    /**
     * Get user option value of specified name
     * @param paramName
     * @param defaultValue
     * @return
     */
    public String getUserOption(String paramName, String defaultValue){
    	String value=(String)options.get(paramName);
    	if(value==null) value= defaultValue;
    	return value;
    }
    /**
     * Override user option,so can set in program level beside db
     * @param paramName
     * @param value
     */
    public void setUserOption(String paramName, String value){
    	options.put(paramName,value);
    }
    
    /**
     * Load user settings from ad_option table
     * @throws Exception
     */
    public void loadUserOptions() throws Exception{
        if(id == GUEST_ID) return;
    	Connection con= QueryEngine.getInstance().getConnection() ;
        PreparedStatement pstmt=null ;ResultSet rs=null;
        options.clear(); 
        try {
            pstmt= con.prepareStatement(SELECT_PREF_BY_MODULE);
            pstmt.setInt(1, this.id );
            pstmt.setString(2, OPTION_MODULE);
            rs= pstmt.executeQuery();
            String name, value; 
            while( rs.next() ){
                name= rs.getString(1);
                value=rs.getString(2);
                options.put(name,value);
            }
            String themeId=(String) options.get("THEMEID");
            if(Validator.isNotNull(themeId))themePath="/html/nds/themes/classic/"+ themeId;
        }
        catch (SQLException ex) {
            logger.error("Could not get preference of "+ this.desc + " relate to "+ OPTION_MODULE, ex);
            throw new QueryException("@exception@:"+ ex);
        }finally{
            try{ if(rs !=null)rs.close() ;}catch(Exception e){}
            try{ if(pstmt !=null)pstmt.close() ;}catch(Exception e2){}
            try{ con.close() ;} catch(Exception e3){}
        }
    }
    /**
     * @param module just the table name to upper case
     * @return key: usersetting's id, value: desc of that record, not the id is bigger than 10
     * the values below 10 are preserved for maintainance such as save/delete
     */
    /*public IntHashtable getPreferences(String module)throws   QueryException{
        Connection con= QueryEngine.getInstance().getConnection() ;
        PreparedStatement pstmt=null ;ResultSet rs=null;
        try {
            pstmt= con.prepareStatement(GET_PREFERS);
            pstmt.setInt(1, this.id );
            pstmt.setString(2, module.toUpperCase());
            rs= pstmt.executeQuery();
            int pid; String desc; IntHashtable ih=new IntHashtable();
            while( rs.next() ){
                pid= rs.getInt(1);
                desc= rs.getString(2);
                ih.put(pid, desc);
            }
            return ih;
        }
        catch (SQLException ex) {
            logger.error("Could not get preference of "+ this.desc + " relate to "+ module, ex);
            throw new QueryException("@exception@:"+ ex);
        }finally{
            try{ if(rs !=null)rs.close() ;}catch(Exception e){}
            try{ if(pstmt !=null)pstmt.close() ;}catch(Exception e2){}
            try{ con.close() ;} catch(Exception e3){}
        }

    }*/
    /*public void setDefaultPreference(String module, int pid)throws UpdateException, QueryException{
        Vector v=new Vector();
        String sql="update usersetting set mDefault=0 where userId=" + id + " and mDefault=1 and module='"+ module.toUpperCase()+"'";
        v.addElement(sql);
        sql= "update usersetting set mDefault=1 where id="+ pid;
        v.addElement(sql);
        QueryEngine.getInstance().doUpdate(v);

    }*/
    /**
     * Include range into default value, you can retrieve range using
     *  getPreference(module+".RANGE", "range")
     */
    public void setDefaultPreference(String module, int pid, int defaultRange)throws UpdateException, QueryException{
        module= module.toUpperCase();
        Vector v=new Vector();
        String sql="update usersetting set mDefault=0 where userId="+ id+ " and mDefault=1 and module='"+ module+"'";
        v.addElement(sql);
        sql= "update usersetting set mDefault=1 where id="+ pid;
        v.addElement(sql);
        sql= "delete from usersetting where userId="+id +" and module='"+module+".RANGE'";
        v.addElement(sql);
        sql="insert into usersetting(id, userId, module, description, value, mDefault) values ("+
                   "seq_usersetting.nextval, "+ this.id + ",'" +module+".RANGE','range', '"+
                    defaultRange +"', 0)";
        v.addElement(sql);

        QueryEngine.getInstance().doUpdate(v);

    }
    /*private String getPreference0(int preferId) throws QueryException{
        Connection con= QueryEngine.getInstance().getConnection() ;
        PreparedStatement pstmt=null ;ResultSet rs=null;
        try {
            pstmt= con.prepareStatement(GET_PREFER);
            pstmt.setInt(1, preferId);
            rs= pstmt.executeQuery();
            int pid; String value;
            if( rs.next() ){
                value= rs.getString(1);
                return value;
            }
            return null;
        }
        catch (SQLException ex) {
            logger.error("Could not get default preference of "+ this.desc + " relate to "+ preferId, ex);
            throw new QueryException("@exception@:"+ ex);
        }finally{
            try{ if(rs !=null)rs.close() ;}catch(Exception e){}
            try{ if(pstmt !=null)pstmt.close() ;}catch(Exception e2){}
            try{ con.close() ;} catch(Exception e3){}

        }

    }*/
    /*public int getDefaultPreference(String module) throws QueryException{
        Connection con= QueryEngine.getInstance().getConnection() ;
        PreparedStatement pstmt=null ;ResultSet rs=null;
        try {
            pstmt= con.prepareStatement(GET_PREFER_DEFAULT);
            pstmt.setInt(1, this.id );
            pstmt.setString(2, module.toUpperCase());
            rs= pstmt.executeQuery();
            int pid; String value;
            if( rs.next() ){
                pid= rs.getInt(1);
                return pid;
            }
            return 0;
        }
        catch (SQLException ex) {
            logger.error("Could not get default preference of "+ this.desc + " relate to "+ module, ex);
            throw new QueryException("@exception@:"+ ex);
        }finally{
            try{ if(rs !=null)rs.close() ;}catch(Exception e){}
            try{ if(pstmt !=null)pstmt.close() ;}catch(Exception e2){}
            try{ con.close() ;} catch(Exception e3){}

        }
    }*/
    /**
     * @return null if not found, note module is case insensitive, while desc is not
     */
    /*public String getPreference(String module, String desc) throws QueryException{
        Connection con= QueryEngine.getInstance().getConnection() ;
        PreparedStatement pstmt=null ;ResultSet rs=null;
        try {
            pstmt= con.prepareStatement(GET_PREFER_BY_DESC);
            pstmt.setInt(1, this.id  );
            pstmt.setString(2, module.toUpperCase() );
            pstmt.setString(3, desc);
            rs= pstmt.executeQuery();
            int pid; String value;
            if( rs.next() ){
                return  rs.getString(1);
            }
            return null;
        }
        catch (SQLException ex) {
            logger.error("Could not get default preference of "+ this.desc + " relate to "+ module, ex);
            throw new QueryException("@exception@:"+ desc+ ":"+ ex);
        }finally{
            try{ if(rs !=null)rs.close() ;}catch(Exception e){}
            try{ if(pstmt !=null)pstmt.close() ;}catch(Exception e2){}
            try{ con.close() ;} catch(Exception e3){}

        }

    }*/
    
    /**
     * add preference and set as default
     */
    public void addPreference(String module, String desc, String value) throws UpdateException, QueryException{
        module=module.toUpperCase();
        Vector v=new Vector();
        String sql;
        sql= "delete from usersetting where userId="+id +" and module='"+module+
             "' and description='" +StringUtils.replace(desc, "'", "\'")+"'";
        v.addElement(sql);
        sql="update usersetting set mDefault=0 where userId="+id+" and mDefault=1 and module='"+ module+"'";
        v.addElement(sql);
        int id= QueryEngine.getInstance().getSequence("usersetting");
        sql="insert into usersetting(id, userId, module, description, value, mDefault) values ("+ id+
                   ", "+ this.id + ",'" +module+"','"+
                   StringUtils.replace(desc, "'", "\'") +"', '"+
                   StringUtils.replace(value, "'", "\'")+  "', 1)";
        v.addElement(sql);
        QueryEngine.getInstance().doUpdate(v);
    }

    public void deletePreference(int pid) throws UpdateException, QueryException{
        Vector v=new Vector();
        String sql="delete from usersetting where id="+ pid;
        v.addElement(sql);
        QueryEngine.getInstance().doUpdate(v);

    }
    public void savePreference(int pid, String desc, String value) throws UpdateException, QueryException{
        Vector v=new Vector();
        String sql="update usersetting set desc='"+StringUtils.replace(desc, "'", "\'") +
                   "', value='"+ StringUtils.replace(value, "'", "\'")+" where id="+ pid;
        v.addElement(sql);
        QueryEngine.getInstance().doUpdate(v);
    }
    public void copyPreferenceAs(int pid, String newDesc) throws UpdateException, QueryException{
        Vector v=new Vector();
        String sql="update usersetting set mDefault=0 where userId="+ id+ " and mDefault=1 and module in ( select module from usersetting where id="+ pid+") ";
        v.addElement(sql);
        sql="insert into usersetting(id, userId, module, description, value, mDefault) select "+
                   "seq_usersetting.nextval, "+ this.id + ", module, '" + StringUtils.replace(newDesc, "'", "\'") +"', '"+
                   "', value, 1 from usersetting where id="+pid;
        v.addElement(sql);
        QueryEngine.getInstance().doUpdate(v);
    }
    /**
     * Construct views for specfied object, only tables can be viewed for this objectId will be
     * included, despite of the table itself. <p>
     * This method will also check user's permission on the object.
     * @param table
     * @param objectId
     * @return elements are Table, not include the parameter <param>table</param>
     * @throws QueryException
     * @throws SQLException
     */
    public List constructViews(Table table, int objectId)  throws QueryException,  SQLException{
    	List list= TableManager.getInstance().getViews(table.getId(), false);
    	for(int i=list.size()-1;i>=0 ;i--){
    		Table tb= (Table)list.get(i);
    		if (this.getPermission(tb.getSecurityDirectory())==0){
    			list.remove(i);
    			continue;
    		}
    		QueryRequestImpl query= QueryEngine.getInstance().createRequest(this.qsession);
    		query.setMainTable(tb.getId());
    		query.addSelection(tb.getPrimaryKey().getId());
    		ColumnLink colLink= new ColumnLink(new int[]{tb.getPrimaryKey().getId() } );
    		Expression ex= new Expression(colLink, "="+ objectId,null );
    		Expression expr= this.getSecurityFilter(tb.getName(),  nds.security.Directory.READ);
    		if( expr !=null && !expr.isEmpty())
    			ex= ex.combine(expr, SQLCombination.SQL_AND, null);
    		query.addParam(ex);
    		if ( Tools.getInt(QueryEngine.getInstance().doQueryOne( query.toCountSQL()), 0)==0){
    			list.remove(i);
    		}
    	}
    	return list;
    }
    /**
     * Filter according to user permission and object id
     * If objectId=-1, then only main table and his first child will be displayed,
     * else, 
     * 		filter ref-by-table according to ref-by-table filter,
     * 		if exists, then showing the tab.
     * The ref-by-table must also exists in users permission range. 
     * @param table
     * @param objectId
     * @return elements are RefByTable
     */
    public ArrayList constructTabs(Table table, int objectId) throws QueryException,  SQLException{
    	ArrayList al=new ArrayList();
    	RefByTable rft= new RefByTable(table);
    	al.add(rft);
    	// -1 means the object not saved in db yet.( creation action)
    	if ( objectId==-1 &&  table.getRefByTables().size()>1 ){
    		/**
    		 * 新版本支持返回一个标签页
    		 */
    		return al; 
    	}
    	Table refTable;
    	Column refColumn;
    	int count=0;
    	TableManager manager=  TableManager.getInstance();
    	int realTableId= manager.getTable(table.getRealTableName()).getId();
    	for(Iterator it= table.getRefByTables().iterator();it.hasNext();){
    		// check if user can read the ref-by-table first
    		rft= (RefByTable)it.next();
    		refTable= manager.getTable( rft.getTableId());
    		if(objectId==-1){
    			// 非父子表关系将在ProcessObject 的时候处理明细出错 (无法找到fixedcolumns)
	    		Table pt=manager.getParentTable(refTable);
	    		if (!(pt!=null && manager.getTable(pt.getRealTableName()).getId()==realTableId )) continue;
    		}
    		if(! isPermissionEnabled(refTable.getSecurityDirectory(), 1/*1 for read*/))
    			continue;
    		// check filter
    		if ( rft.getFilter()!=null){
    			/**
    			 * yfzhu updated here, change the meaning for filter, now it is:
    			 * if main table object record satisfy the condition set, will display the
    			 * tab (2009-4-24)
    			 */
    			String sql= "select count(*) from " + table.getRealTableName()+ " "+ table.getName()+" where "+ 
				table.getPrimaryKey().getName()+ "=" + objectId+ " and (" + rft.getFilter()+")";
				count= Tools.getInt(QueryEngine.getInstance().doQueryOne(sql), 0);
				if (count == 0) continue;
    			
    			/*
    			refColumn=  manager.getColumn( rft.getRefByColumnId());
    			String sql= "select count(*) from " + refTable.getRealTableName()+ " "+ refTable.getName()+" where "+ 
					refColumn.getName()+ "=" + objectId+ " and (" + rft.getFilter()+")";
    			count= Tools.getInt(QueryEngine.getInstance().doQueryOne(sql), 0);
    			if (count == 0) continue;
    			*/
    		}
    		
    		al.add(rft);
    	}
    	return al;
    }
    /**
     * Get allowed template of current mainTable
     * @param mainTable 
     * @param reportType - as in column AD_REPORT.REPORTTYPE
     * @return ResultSet cached resultset with column sequence:id, name, description, previewurl, allow_fg
     * @throws SQLException
     * @throws QueryException
     */
    public QueryResult getReportTemplates(Table mainTable, String reportType) throws  SQLException, QueryException{
		QueryEngine engine=QueryEngine.getInstance();
		QueryRequestImpl query= engine.createRequest(getSession());
		TableManager manager= TableManager.getInstance();
		Table table= manager.getTable("ad_report");
		query.setMainTable(table.getId());
		// select
		query.addSelection(table.getPrimaryKey().getId());
		query.addSelection(manager.getColumn("ad_report", "name").getId());
		query.addSelection(manager.getColumn("ad_report", "description").getId());
		query.addSelection(manager.getColumn("ad_report", "previewurl").getId());
		query.addSelection(manager.getColumn("ad_report", "allow_fg").getId());
        // where 
		Expression expr;
		expr=getSecurityFilter(table.getName(), nds.security.Directory.READ);
		query.addParam(expr);
		// set only active news
		expr = new Expression(new ColumnLink(new String[]{"ad_report.isactive"}), "=Y", null);
		expr= expr.combine(new Expression(new ColumnLink(new String[]{"ad_report.AD_TABLE_ID"}), "="+ mainTable.getId(), null), SQLCombination.SQL_AND,null);
		// satisfied reporttype
		expr= expr.combine(new Expression(new ColumnLink(new String[]{"ad_report.reporttype"}), "="+ reportType, null), SQLCombination.SQL_AND,null);
		query.addParam(expr);
		// order
		query.addOrderBy(new int[]{ manager.getColumn("ad_report", "name").getId() }, false);

		return engine.doQuery(query); 
    }
    private void initVisitTables() throws SQLException, QueryException{
        if( !isActive) return;
    	Connection con= QueryEngine.getInstance().getConnection() ;
        PreparedStatement pstmt=null ;ResultSet rs=null;
        int i=0;
        visitTables.clear();
        frequentTables.clear();
        try {
        	// visited tables order by date asc, so most recent will be the first one 
            pstmt= con.prepareStatement(GET_VISIT_TABLES);
            pstmt.setInt(1, this.id);
            rs= pstmt.executeQuery();
            int pid; String value;
            while( rs.next() ){
                this.visitTables.addFirst( new Integer(rs.getInt(1)));
            }
            pstmt.close();
            
            // frequent visit tables order by hitcount
            
            pstmt= con.prepareStatement(GET_FREQUENT_TABLES);
            pstmt.setInt(1, this.id);
            rs= pstmt.executeQuery();
            VisitTable vt;
            while(rs.next()){
            	vt=new VisitTable(rs.getInt(1), rs.getTimestamp(2));
            	frequentTables.put(new Integer(rs.getInt(1)), vt);
            }
        }finally{
            try{ if(rs !=null)rs.close() ;}catch(Exception e){}
            try{ if(pstmt !=null)pstmt.close() ;}catch(Exception e2){}
            try{ con.close() ;} catch(Exception e3){}

        }
    	logger.debug("Loaded visit table history for " + this.getUserName()+"@" + this.getClientDomain());
    	
    }
    /**
     * 当前用户是否允许激活
     * @return 
     */
    public boolean isActive(){
    	return isActive;
    }
    /**
     * Called by other modules when user visits specified table
     * 注册的表进行如下处理：
     * VisitTables 仅保留最近访问的10张表 （按时间顺序，即最后访问的表放在最后的位置）
     * FrequentTables 仅保留最频繁访问的10张表
     * 当前注册的表将在visitTables 中置顶，
     * 若在FrequentTables 中存在，则增加hitCount, 若不存在，则添加到最后一个
     * 若FrequentTables数量超过30，保存并重新装载FrequentTables
     * 
     * @param tableId
     */
    public void registerVisit(int  tableId) {
    	// handle visit tables
    	Integer t= new Integer(tableId);
    	VisitTable vt;
    	if(!visitTables.remove(t) && visitTables.size()>=10){
    		visitTables.removeLast();
    	}
    	visitTables.addFirst(t);
    	
    	//handle fequent tables
    	vt= (VisitTable) frequentTables.get(t);
    	if(vt ==null){
    		// append
    		vt=new VisitTable(tableId, new java.sql.Date(System.currentTimeMillis()));
    		vt.increamentHitCount();
    		frequentTables.put( t, vt);
    		if(frequentTables.size()>30){
    			//save and reload
    			try{
        			logVisitTables();
    				initVisitTables();
    			}catch(Exception e){
    				logger.error("Fail to save and reload visit tables:" + e, e);
    			}
    		}
    	}else{
    		// increment hitcount
    		vt.increamentHitCount();
    	}
    }
    /**
     * 
     * @return elements are Integer(tableId)
     */
    public Iterator getVisitTables(){
    	return this.visitTables.iterator();
    }
    /**
     * 
     * @return only the first 10 elements. Elements are Integer(tableId)
     */
    public Iterator getFrequentTables(){
    	return new Iterator(){
    		int i=0;
    		public void remove(){
    			throw new UnsupportedOperationException("not implemented yet");
    		}
    		public Object next(){
    			// key is just the tableId
    			Object key= frequentTables.getKey(i);
    			if(key ==null){
    				throw new NoSuchElementException("Index " + i+ " is out of range(0,"+frequentTables.size()+")" );
    			}
    			i++;
    			return key;
    		}
    		public boolean hasNext(){
    			return i< frequentTables.size() && i<10;
    		}
    	};
    }
    /**
     * Cubes that user can create query on
     * @param tableId
     * @return never be null, may be empty. key:Integer (cubeId), value :String (cube name) 
     */
    public PairTable getCubes(int tableId){
    	PairTable pt=new PairTable();
    	Connection con=null;
        PreparedStatement pstmt=null ;ResultSet rs=null;
        String p=null;
        try {
    		con=QueryEngine.getInstance().getConnection() ;
            pstmt= con.prepareStatement(GET_CUBE) ; 
            pstmt.setInt(1, tableId);
            pstmt.setInt(2, this.id);
            rs= pstmt.executeQuery();
            while (rs.next()){
            	pt.put(new Integer( rs.getInt(1)), rs.getString(2));
            }
    	}catch(Exception e){
    		logger.error("Could not check user olap on "+ tableId, e);
    	}finally{
            try{ if(rs !=null)rs.close() ;}catch(Exception e){}
            try{ if(pstmt !=null)pstmt.close() ;}catch(Exception e2){}
            try{ if(con!=null) con.close() ;} catch(Exception e3){}

        }        	
    	return pt;    	
    }
    public boolean isGuest(){
    	return id==GUEST_ID;
    }
    /**
     * Check this user can do olap on specified table or not
     * First table should have cubes on it, seconds, user should have role set to view cubes
     * @param tableId
     * @return
     */
    public boolean hasCubes(int tableId){
    	Connection con=null;
        PreparedStatement pstmt=null ;ResultSet rs=null;
        String p=null;
        try {
    		con=QueryEngine.getInstance().getConnection() ;
            pstmt= con.prepareStatement(CHECK_CUBE) ; 
            pstmt.setInt(1, tableId);
            pstmt.setInt(2, this.id);
            rs= pstmt.executeQuery();
            if (rs.next()){
            	return true;
            }
    	}catch(Exception e){
    		logger.error("Could not check user olap on "+ tableId, e);
    	}finally{
            try{ if(rs !=null)rs.close() ;}catch(Exception e){}
            try{ if(pstmt !=null)pstmt.close() ;}catch(Exception e2){}
            try{ if(con!=null) con.close() ;} catch(Exception e3){}

        }        	
    	return false;
    }
    
    /**
     * 保存方法：
     * 根据frequentTables, 更新已存在的表，插入未存在的表
     * @throws SQLException
     */
    private void logVisitTables() throws Exception {
    	if( !isActive) return;
    	Connection con=null;
        Statement pstmt=null ;ResultSet rs=null;
        //private static final String UPDATE_VISIT_TABLES="update pref_visit_log set hitcount=hitcount+?, LASTVISITDATE=? where userid=? and ad_table_id=?";
        //private static final String INSERT_VISIT_TABLES="insert into pref_visit_log (id, userid, ad_table_id,hitcount, lastvisitdate) values( get_sequences('pref_visit_log'),?,?,?,? )";

        try {
    		con=QueryEngine.getInstance().getConnection() ;
            pstmt= con.createStatement();
            int cnt;String sql;
            for(Iterator it= frequentTables.values();it.hasNext();){
            	VisitTable vt= (VisitTable)it.next();
            	sql= "update pref_visit_log set hitcount=hitcount+"+vt.hitCount+
				", LASTVISITDATE=to_date('"+ ((java.text.SimpleDateFormat)QueryUtils.dateTimeSecondsFormatter.get()).format(new java.util.Date(vt.lastVisitDate))+"','YYYY/MM/DD HH24:MI:SS') where userid="+this.id+" and ad_table_id="+vt.tableId;
            	//logger.debug(sql);
            	cnt=pstmt.executeUpdate(sql);
            	if(cnt ==0){
            		sql ="insert into pref_visit_log (id, userid, ad_table_id,hitcount, lastvisitdate) "+
					"values( get_sequences('pref_visit_log'),"+ this.id+","+ vt.tableId+
					","+ vt.hitCount+",to_date('"+ ((java.text.SimpleDateFormat)QueryUtils.dateTimeSecondsFormatter.get()).format(new java.util.Date(vt.lastVisitDate))+"','YYYY/MM/DD HH24:MI:SS') )";
            		// insert one
            		//logger.debug(sql);
            		pstmt.executeUpdate(sql);
            	}
            }
            logger.debug("Saved visit table history for " + this.getUserName()+"@" + this.getClientDomain());
    	}finally{
            try{ if(rs !=null)rs.close() ;}catch(Exception e){}
            try{ if(pstmt !=null)pstmt.close() ;}catch(Exception e2){}
            try{ con.close() ;} catch(Exception e3){}

        }
    }

    /**
     * Get preference from ad_user_pref
     * @param module preference is classified by module
     * @param name preference name
     * @param cacheable should this value be checked from cache first, if not found, will be cached after load from db
     * @return any string (include null)  
     */
    public String getPreferenceValue(String module, String name, boolean cacheable) throws  Exception{
    	Connection con=null;
        PreparedStatement pstmt=null ;ResultSet rs=null;
        String p=null;
        // cache preference as one object
        String cacheKey="p_"+module+"_"+name;
        if( cacheable){
        	p= (String)directoryCache.getCachedObject(cacheKey);
        	if(p!=null) return p;
        }
        try {
    		con=QueryEngine.getInstance().getConnection() ;
            pstmt= con.prepareStatement(SELECT_PREF_BY_MODULE_AND_NAME) ; 
            pstmt.setInt(1, this.id);
            pstmt.setString(2, module);
            pstmt.setString(3, name);
            rs= pstmt.executeQuery();
            if (rs.next()){
            	p= rs.getString(1);
            }
            if( cacheable){
            	directoryCache.addCachedObject(cacheKey, (p==null?"":p));
            }
            return p;
    	}finally{
            try{ if(rs !=null)rs.close() ;}catch(Exception e){}
            try{ if(pstmt !=null)pstmt.close() ;}catch(Exception e2){}
            try{ con.close() ;} catch(Exception e3){}

        }    
    }
    /**
     * Invalidate preferences in cache. Useful when preferences changed
     * @param module
     */
    public void invalidatePreferences(String module){
    	String cacheKey="p_"+module;
    	directoryCache.removeCachedObject(cacheKey);
    }
    /**
     * Load first row of selection
     * @param table
     * @param selection, not include id, id will always be the first column in result
     * @param orderBy
     * @param isAscending
     * @param filter same as ad_table.filter setting
     * @param dirPermission see nds.security.Directory
     * @return null if no data found
     * @throws Exception
     */
    public ArrayList loadFirstRow(Table table,ArrayList selection, Column orderBy,
    		boolean isAscending,int dirPermission, String filter) throws Exception{
    	QueryRequestImpl query=QueryEngine.getInstance().createRequest(qsession);
    	query.setMainTable(table.getId(),true, filter);
		query.addSelection(table.getPrimaryKey().getId());

    	if(selection !=null)query.addColumnsToSelection(selection, false);
    	Expression expr=null, expr2;
		expr =getSecurityFilter(table.getName(), dirPermission);

		if(dirPermission==nds.security.Directory.WRITE){
			// try filter status column for only status=1 rows
			Column column= table.getColumn("status");
	    	if ( column!=null){
	    		ColumnLink cl=new ColumnLink(new int[]{column.getId()});
	    		expr2= new Expression(cl,"=1",null);
	        	expr=expr2.combine(expr, SQLCombination.SQL_AND,null);
	    	}
		}
    	
    	query.addParam(expr);
    	query.setOrderBy(new int[]{orderBy.getId()}, isAscending);
    	query.setRange(0, 1);
    	
    	QueryResult res= QueryEngine.getInstance().doQuery(query);
    	if(res.next()){
    		ArrayList al=new ArrayList();
    		for(int i=0;i< res.getMetaData().getColumnCount();i++){
    			al.add(res.getObject(i+1));
    		}
    		return al;
    	}else
    		return null;
    }
    /**
     * 
     * @param module
     * @param cacheable
     * @param eval should try to evaluate the content of preference
     * @return
     * @throws Exception
     */
    public Properties getPreferenceValues(String module, boolean cacheable, boolean eval) throws Exception{
    	Connection con=null;
        PreparedStatement pstmt=null ;ResultSet rs=null;
        Properties p=null;
        // cache while module as one object
        String cacheKey="p_"+module;
        if( cacheable){
        	p= (Properties)directoryCache.getCachedObject(cacheKey);
        	if(p!=null) return p;
        }
        try {
    		con=QueryEngine.getInstance().getConnection() ;
            pstmt= con.prepareStatement(SELECT_PREF_BY_MODULE);
            pstmt.setInt(1, this.id);
            pstmt.setString(2, module);
            rs= pstmt.executeQuery();
            p=new Properties();
            String value, name;
            while (rs.next()){
            	name= rs.getString(1);
            	value= rs.getString(2);
            	if(name!=null && value!=null ){
            		if (eval && "=".equals(value.substring(0,1))){
            			value= ""+ QueryUtils.evalScript(value);
            		}
            		p.setProperty(name, value);
            	}
            }
            if( cacheable){
            	directoryCache.addCachedObject(cacheKey, p);
            }
            return p;
    	}finally{
            try{ if(rs !=null)rs.close() ;}catch(Exception e){}
            try{ if(pstmt !=null)pstmt.close() ;}catch(Exception e2){}
            try{ con.close() ;} catch(Exception e3){}

        }        	
    }
    /**
     * Load all preferences of specified module from ad_user_pref
     * @param module  preference is classified by module
     * @param cacheable cacheable should this value be checked from cache first, if not found, will be cached after load from db
     * @return
     */
    public Properties getPreferenceValues(String module, boolean cacheable) throws Exception{
    	return getPreferenceValues(module, cacheable, false);
    }
    
    /**
     * Save module prefereces, will remove all previous module settings first. 
     * @param module
     * @param props
     * @throws Exception
     */
    public void savePreferenceValues(String module, Properties props) throws Exception{
    	nds.control.ejb.command.SavePreference.setPreferenceValues(this.id, module,props);
    }
    
    private class VisitTable{
    	public int tableId;
    	public long lastVisitDate;
    	public int hitCount; // record only hit count in this session, the db hitcount will be increased by it
    	public VisitTable(int tbId, java.util.Date lastDate){
    		tableId=tbId;
    		lastVisitDate = lastDate.getTime();
    		hitCount= 0;
    	}
    	public void increamentHitCount(){
    		hitCount++;
    		lastVisitDate= System.currentTimeMillis();
    	}
    	
    }
}
