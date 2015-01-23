/******************************************************************
*
*$RCSfile: StartupEngine.java,v $ $Revision: 1.3 $ $Author: Administrator $ $Date: 2006/03/13 01:15:49 $
*
*$Log: StartupEngine.java,v $
*Revision 1.3  2006/03/13 01:15:49  Administrator
*no message
*
*Revision 1.2  2006/01/31 02:58:03  Administrator
*no message
*
*Revision 1.1.1.1  2005/03/15 11:23:14  Administrator
*init
*
*Revision 1.1.1.1  2002/11/29 02:50:34  yfzhu
*Active POS
*
*Revision 1.3  2001/11/20 22:36:09  yfzhu
*no message
*
*Revision 1.2  2001/11/07 20:58:46  yfzhu
*no message
*
*
********************************************************************/
package nds.control;

import java.beans.Beans;
import java.io.File;
import java.net.URI;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;

import com.liferay.util.servlet.SessionErrors;

import nds.control.web.WebUtils;
import nds.log.Logger;
import nds.log.LoggerManager;

import nds.util.Configurations;
import nds.util.DestroyListener;
import nds.util.LicenseManager;
import nds.util.MD5Sum;
import nds.util.NDSRuntimeException;
import nds.util.ServletContextActor;
import nds.util.StringUtils;
import nds.util.Tools;
import nds.util.WebKeys;

/**
 * Need Servlet 2.3 Support
 */
public class StartupEngine extends HttpServlet // implements javax.servlet.ServletContextListener
            //javax.servlet.http.HttpSessionBindingListener
{
	private Logger logger= LoggerManager.getInstance().getLogger(StartupEngine.class.getName());
	/**
     * Constructor
     * 
     */
    public StartupEngine() {
    }
    public void init() {
        // init servlet context manager

        /*System.out.println("\n\r"+
                           "^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n\r"+
                           "     NDS Startup Engine is initializing...     \n\r"+
                           "                                                \n\r"+
                           "^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
        */
        /*
         yfzhu 2004-04-22 add support for multiple nds application in one web server
         the main concern is the configuration file, we set it in web.xml/context-param
         like:
        <web-app>
        ...
         <context-param>
             <param-name>nds.config.path</param-name>
             <param-value>e:/aic/conf/nds.properties</param-value>
        </context-param>
        ...
        </web-app>
         so we can retrieved using

        ServletContext context = this.getServletContext();
        String footer = context.getInitParameter("footerPage");

        */
		//Tools.loadNativeLibrary(Tools.class.getClassLoader());
    	//add check mac

        ServletContext context = this.getServletContext();
        String path = context.getInitParameter("nds.config.path");
        // default path to system property
        if ( path ==null) path= System.getProperty("nds.config.path","/nds.properties");
        //System.out.println("NDS loading configurations from file:"+ path);
        // set it into EJBUtils
        Configurations conf=null;
        try{
            conf=getConfigurations(path);

            nds.control.util.EJBUtils.initConfigurations(conf);

        }catch(Exception e){
            System.out.println("NDS could not load configuration from file:"+ path);
            e.printStackTrace();
        }
        if(conf !=null)nds.log.LoggerManager.getInstance().init(conf.getProperties(), true);

        WebUtils.setServletContext(this.getServletContext());
        WebUtils.getServletContextManager();
    	if(!check_mac()){
    		logger.debug("check_mac");
    		//return mapping.findForward("portal.vaildkey");
    		 throw new NDSRuntimeException("Error initializing context !check_uk error!");
    	}
    }

    
    private Configurations getConfigurations(String filePath)throws Exception {
        Configurations conf = null;
        //java.net.URL url= new java.net.URL(context.getResource("/WEB-INF/nds.properties").toString());

        File file = new File( filePath);
        java.net.URL url = file.toURL();
        conf = new Configurations(url.openStream());
        return conf;
    }

    public void destoy() {
    }
    private  static boolean doLog=true;
    private void debug(String str) {
        if(doLog)
            System.out.println("[StartEngine] "+ str);
    }
    ///------------- implements ServletContextListener
    public void contextDestroyed(ServletContextEvent sce) {
        DestroyListener manager=(DestroyListener)sce.getServletContext().getAttribute(WebKeys.SERVLET_CONTEXT_MANAGER);
        manager.destroy();
        debug("ServletContextManager "+manager+" destroied.");
    }
    ///------------- implements ServletContextListener
    public void contextInitialized(ServletContextEvent sce) {
        try {
            ServletContextActor actor=(ServletContextActor)Beans.instantiate(StartupEngine.class.getClassLoader(), "nds.control.web.ServletContextManager");
            actor.init(sce.getServletContext());
            debug("ServletContextManager "+actor+" created.");
        } catch(Exception e) {
            e.printStackTrace();
            throw new NDSRuntimeException("Error initializing context", e);
        }

    }
 
	/**
	 * return check_mac address
	 * @param req 
	 * 
	 */
	private boolean check_mac(){

		Connection conn = null; 
		Object sc=null;
		String mac =null;
		ResultSet rs = null;
		PreparedStatement pstmt = null;
		//if mac pass exists
		//get mac file to check it
		try{
			conn= nds.query.QueryEngine.getInstance().getConnection();
			pstmt= conn.prepareStatement("select mac from users where id=?");
			pstmt.setInt(1, 893);
			rs= pstmt.executeQuery();
			if(rs.next()){
				sc=rs.getObject(1);
				if(sc instanceof java.sql.Clob) {
					mac=((java.sql.Clob)sc).getSubString(1, (int) ((java.sql.Clob)sc).length());
	        	}else{
	        		mac=(String)sc;
	        	}	
			}
			logger.debug("keyfile :"+mac);
			if(mac==null){
				//SessionErrors.add(req, "VERIFY_KEYFILE_ERROR");
				System.out.print("mac is null");
				return false;
				}
			try{if(conn!=null) conn.close();}catch(Throwable t){}
		}catch(Throwable t){
			System.out.print("query is error");
			return false;
		}finally{
			try{if(rs!=null) rs.close();}catch(Throwable t){}
			try{if(pstmt!=null) pstmt.close();}catch(Throwable t){}
			try{if(conn!=null) conn.close();}catch(Throwable t){}
		}	
		//else
		//nds.util.Configurations conf=(nds.util.Configurations)nds.control.web.WebUtils.getServletContextManager().getActor(nds.util.WebKeys.CONFIGURATIONS);
		try{
			LicenseManager.validateLicense(WebKeys.PRDNAME,"5.0", mac);
			/*
		    Iterator b=LicenseManager.getLicenses();
		    while (b.hasNext()) {
		    LicenseWrapper o = (LicenseWrapper)b.next();
	    	logger.debug("ltype :"+o.getLicenseType().toString());
	   
	    	if(o.getLicenseType()==License.LicenseType.COMMERCIAL){
				//logger.error("The license should contain machine code.");
				//return;
				//if(license.getExpiresDate().getTime() - license.getCreationDate().getTime() > 1L * 100 * 24 * 3600* 1000  ){
					if(System.currentTimeMillis() >o.getExpiresDate().getTime()  ){
					//logger.error("Non-Commercial license valid duration should not be greater than 100 days");
					logger.error("your company service day is old!!!!!!!");
					o.setMms(LicenseManager.sendmss(o.getName(),o.getExpiresDate()));
					//nds.control.web.WebUtils.setMms(o.getMms());
				}
		    }else if(o.getLicenseType()==License.LicenseType.EVALUATION){
				// Evaluation should not have valid duration over 31 days
				logger.debug("Evaluation should not have valid duration over");
				//if(license.getExpiresDate().getTime() - license.getCreationDate().getTime() > 1L * 31 * 24 * 3600* 1000  ){
				if(System.currentTimeMillis() >o.getExpiresDate().getTime()  ){
					logger.error("Evaluation license valid duration should not be greater than 30 days");
					o.setExpdate(true);
			    	//nds.control.web.WebUtils.setLtype(o.getLicenseType());
					//return;
				}
		    }
		    }
		    */
			return true;
		} catch (Exception e) {
			logger.debug("check mackey invaild",e);
			System.out.print("check mackey invaild");
			//SessionErrors.add(req, "VERIFY_KEY_ERROR");
			return false;
		}
	}


}

