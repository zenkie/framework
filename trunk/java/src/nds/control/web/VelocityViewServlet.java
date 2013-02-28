package nds.control.web;

import nds.velocity.*;
import nds.log.Logger;
import nds.log.LoggerManager;
import nds.util.*;

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.Properties;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.*;

import org.apache.commons.collections.ExtendedProperties;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.context.Context;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.io.VelocityWriter;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.util.SimplePool;

/*
 * This is copied from org.apache.velocity.tools.view.servlet.VelocityViewServlet yest simplified
 * 
 * @author yfzhu
 */

/**
 * <p>A servlet to process Velocity templates. This is comparable to the
 * the JspServlet for JSP-based applications.</p>
 *
 * <p>The servlet provides the following features:</p>
 * <ul>
 *   <li>renders Velocity templates</li>
 *   <li>provides transparent access to the servlet request attributes,
 *       servlet session attributes and servlet context attributes by
 *       auto-searching them</li>
 *   <li>logs to the logging facility of the servlet API</li>
 * </ul>
 *
 */

public class VelocityViewServlet extends HttpServlet
{
    private static Logger logger= LoggerManager.getInstance().getLogger(VelocityViewServlet.class.getName());

    /**
	 * Property name set in UserWebImpl.props for VelocityContext;
	 */
	private final static String PROPS_VELOCITY_CONTEXT="nds_velocity_context";
	/**
	 * value has format like "/html/nds/website/xxx"
	 */
	//private final static String PROPS_VELOCITY_WEBSITE_PATH="nds_velocity_website_path";
	/**
	 * Root for vm site
	 */
	private final static String VELOCITY_WEB_ROOT="/html/nds/website";
    /** The HTTP content type context key. */
    public static final String CONTENT_TYPE = "default.contentType";

    /** The default content type for the response */
    public static final String DEFAULT_CONTENT_TYPE = "text/html";

    /** Default encoding for the output stream */
    public static final String DEFAULT_OUTPUT_ENCODING = "UTF-8";

    /**
     * Key used to access the ServletContext in
     * the Velocity application attributes.
     */
    public static final String SERVLET_CONTEXT_KEY =ServletContext.class.getName();


    /**
     * This is the string that is looked for when getInitParameter is
     * called ("org.apache.velocity.properties").
     */
    protected static final String INIT_PROPS_KEY =
        "org.apache.velocity.properties";


    /**
     * Default velocity properties file path. If no alternate value for
     * this is specified, the servlet will look here.
     */
    protected static final String DEFAULT_PROPERTIES_PATH =
        "/WEB-INF/velocity.properties";



    /** Cache of writers */
    private static SimplePool writerPool = new SimplePool(40);

    /* The engine used to process templates. */
    private VelocityEngine velocity = null;
    
    /**
     * from portal.properties#server.url
     */
    private String defaultServerURL;
    /**
     * from portal.properties#webclient.multiple
     */
    private boolean isMultipleClientEnabled=true;
    /**
     * from portal.properties#webclient.default.webdomain
     */
    private String defaultClientWebDomain;
    /**
     * from portal.properties#webclient.default.clientid
     */
    private int defaultClientID;
    /**
     * The default content type.  When necessary, includes the
     * character set to use when encoding textual output.
     */
    private String defaultContentType;

    /**
     * Whether we've logged a deprecation warning for
     * ServletResponse's <code>getOutputStream()</code>.
     * @since VelocityTools 1.1
     */
    private boolean warnOfOutputStreamDeprecation = true;

    /**
     * <p>Initializes servlet, toolbox and Velocity template engine.
     * Called by the servlet container on loading.</p>
     *
     * <p>NOTE: If no charset is specified in the default.contentType
     * property (in your velocity.properties) and you have specified
     * an output.encoding property, then that will be used as the
     * charset for the default content-type of pages served by this
     * servlet.</p>
     *
     * @param config servlet configuation
     */
    public void init(ServletConfig config) throws ServletException
    {
        super.init(config);

        // do whatever we have to do to init Velocity
        initVelocity(config);

        // we can get these now that velocity is initialized
        defaultContentType =
            (String)getVelocityProperty(CONTENT_TYPE, DEFAULT_CONTENT_TYPE);

        String encoding =
            (String)getVelocityProperty(RuntimeConstants.OUTPUT_ENCODING,
                                        DEFAULT_OUTPUT_ENCODING);
        defaultContentType += "; charset=" + encoding;
/*        // For non Latin-1 encodings, ensure that the charset is
        // included in the Content-Type header.
        if (!DEFAULT_OUTPUT_ENCODING.equalsIgnoreCase(encoding))
        {
            int index = defaultContentType.lastIndexOf("charset");
            if (index < 0)
            {
                // the charset specifier is not yet present in header.
                // append character encoding to default content-type
                defaultContentType += "; charset=" + encoding;
            }
            else
            {
                // The user may have configuration issues.
            	logger.info("VelocityViewServlet: Charset was already " +
                              "specified in the Content-Type property.  " +
                              "Output encoding property will be ignored.");
            }
        }
*/        
    	// host not valid, redirect to our website homepage
    	Configurations conf= (Configurations)WebUtils.getServletContextManager().getActor(WebKeys.CONFIGURATIONS);
    	defaultServerURL= conf.getProperty("server.url");         
    	isMultipleClientEnabled= "true".equalsIgnoreCase(conf.getProperty("webclient.multiple","true"));
    	defaultClientWebDomain=conf.getProperty("webclient.default.webdomain");
    	defaultClientID= Tools.getInt(conf.getProperty("webclient.default.clientid"), 1);
    	
        logger.info("VelocityViewServlet: Default content-type is: " +
                      defaultContentType);
    }


    /**
     * Looks up an init parameter with the specified key in either the
     * ServletConfig or, failing that, in the ServletContext.
     */
    protected String findInitParameter(ServletConfig config, String key)
    {
        // check the servlet config
        String param = config.getInitParameter(key);

        if (param == null || param.length() == 0)
        {
            // check the servlet context
            ServletContext servletContext = config.getServletContext();
            param = servletContext.getInitParameter(key);
        }
        return param;
    }


    /**
     * Simplifies process of getting a property from VelocityEngine,
     * because the VelocityEngine interface sucks compared to the singleton's.
     * Use of this method assumes that {@link #initVelocity(ServletConfig)}
     * has already been called.
     */
    protected String getVelocityProperty(String key, String alternate)
    {
        String prop = (String)velocity.getProperty(key);
        if (prop == null || prop.length() == 0)
        {
            return alternate;
        }
        return prop;
    }


    /**
     * Returns the underlying VelocityEngine being used.
     */
    protected VelocityEngine getVelocityEngine()
    {
        return velocity;
    }

    /**
     * Sets the underlying VelocityEngine
     */
    protected void setVelocityEngine(VelocityEngine ve)
    {
        if (ve == null)
        {
            throw new NullPointerException("Cannot set the VelocityEngine to null");
        }
        this.velocity = ve;
    }



    /**
     * Initializes the Velocity runtime, first calling
     * loadConfiguration(ServletConfig) to get a
     * org.apache.commons.collections.ExtendedProperties
     * of configuration information
     * and then calling velocityEngine.init().  Override this
     * to do anything to the environment before the
     * initialization of the singleton takes place, or to
     * initialize the singleton in other ways.
     *
     * @param config servlet configuration parameters
     */
    protected void initVelocity(ServletConfig config) throws ServletException
    {
        velocity = new VelocityEngine();
        setVelocityEngine(velocity);

        // register this engine to be the default handler of log messages
        // if the user points commons-logging to the LogSystemCommonsLog
//        LogSystemCommonsLog.setVelocityEngine(velocity);

        velocity.setApplicationAttribute(SERVLET_CONTEXT_KEY, getServletContext());

        // Try reading the VelocityTools default configuration
        /*try
        {
            ExtendedProperties defaultProperties = loadDefaultProperties();
            velocity.setExtendedProperties(defaultProperties);
        }
        catch(Exception e)
        {
            log("VelocityViewServlet: Unable to read Velocity Servlet configuration file: ", e);

            // This is a fatal error...
            throw new ServletException(e);
        }*/

        // Try reading an overriding user Velocity configuration
        try
        {
            ExtendedProperties p = loadConfiguration(config);
            velocity.setExtendedProperties(p);
        }
        catch(Exception e)
        {
            log("VelocityViewServlet: Unable to read Velocity configuration file: ", e);
            log("VelocityViewServlet: Using default Velocity configuration.");
        }

        // now all is ready - init Velocity
        try
        {
            velocity.init();
        }
        catch(Exception e)
        {
            log("VelocityViewServlet: PANIC! unable to init()", e);
            throw new ServletException(e);
        }
    }

    /*private ExtendedProperties loadDefaultProperties()
    {
        InputStream inputStream = null;
        ExtendedProperties defaultProperties = new ExtendedProperties();

        try
        {
            inputStream = getClass()
                    .getResourceAsStream(DEFAULT_TOOLS_PROPERTIES);
            if (inputStream != null)
            {
                defaultProperties.load(inputStream);
            }
        }
        catch (IOException ioe)
        {
            log("Cannot load default extendedProperties!", ioe);
        }
        finally
        {
            try
            {
                if (inputStream != null)
                {
                    inputStream.close();
                }
            }
            catch (IOException ioe)
            {
                log("Cannot close default extendedProperties!", ioe);
            }
        }
        return defaultProperties;
    }*/


    /**
     *  Loads the configuration information and returns that
     *  information as an ExtendedProperties, which will be used to
     *  initialize the Velocity runtime.
     *  <br><br>
     *  Currently, this method gets the initialization parameter
     *  VelocityServlet.INIT_PROPS_KEY, which should be a file containing
     *  the configuration information.
     *  <br><br>
     *  To configure your Servlet Spec 2.2 compliant servlet runner to pass
     *  this to you, put the following in your WEB-INF/web.xml file
     *  <br>
     *  <pre>
     *    &lt;servlet&gt;
     *      &lt;servlet-name&gt; YourServlet &lt/servlet-name&gt;
     *      &lt;servlet-class&gt; your.package.YourServlet &lt;/servlet-class&gt;
     *      &lt;init-param&gt;
     *         &lt;param-name&gt; org.apache.velocity.properties &lt;/param-name&gt;
     *         &lt;param-value&gt; velocity.properties &lt;/param-value&gt;
     *      &lt;/init-param&gt;
     *    &lt;/servlet&gt;
     *   </pre>
     *
     * Alternately, if you wish to configure an entire context in this
     * fashion, you may use the following:
     *  <br>
     *  <pre>
     *    &lt;context-param&gt;
     *       &lt;param-name&gt; org.apache.velocity.properties &lt;/param-name&gt;
     *       &lt;param-value&gt; velocity.properties &lt;/param-value&gt;
     *       &lt;description&gt; Path to Velocity configuration &lt;/description&gt;
     *    &lt;/context-param&gt;
     *   </pre>
     *
     *  Derived classes may do the same, or take advantage of this code to do the loading for them via :
     *   <pre>
     *      ExtendedProperties p = super.loadConfiguration(config);
     *   </pre>
     *  and then add or modify the configuration values from the file.
     *  <br>
     *
     *  @param config ServletConfig passed to the servlets init() function
     *                Can be used to access the real path via ServletContext (hint)
     *  @return ExtendedProperties loaded with configuration values to be used
     *          to initialize the Velocity runtime.
     *  @throws IOException I/O problem accessing the specified file, if specified.
     */
    protected ExtendedProperties loadConfiguration(ServletConfig config)
        throws IOException
    {
        // grab the path to the custom props file (if any)
        String propsFile = findInitParameter(config, INIT_PROPS_KEY);
        if (propsFile == null)
        {
            // ok, look in the default location for custom props
            propsFile = DEFAULT_PROPERTIES_PATH;
            logger.debug("VelocityViewServlet: Looking for custom properties at '"
                           + DEFAULT_PROPERTIES_PATH + "'");
        }

        ExtendedProperties p = new ExtendedProperties();
        InputStream is = getServletContext().getResourceAsStream(propsFile);
        if (is != null)
        {
            // load the properties from the input stream
            p.load(is);
            logger.info("VelocityViewServlet: Using custom properties at '"
                          + propsFile + "'");
        }
        else
        {
            logger.debug("VelocityViewServlet: No custom properties found. " +
                           "Using default Velocity configuration.");
        }
        return p;
    }


    /**
     * Handles GET - calls doRequest()
     */
    public void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException
    {
       
    	doRequest(request, response);
    }


    /**
     * Handle a POST request - calls doRequest()
     */
    public void doPost(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException
    {
        doRequest(request, response);
    }


    /**
     *  Handles with both GET and POST requests
     *
     *  @param request  HttpServletRequest object containing client request
     *  @param response HttpServletResponse object for the response
     */
    protected void doRequest(HttpServletRequest request,
                             HttpServletResponse response)
         throws ServletException, IOException
    {
    	VelocityContext context = null;
        try
        {
            // first, get a context
            context = createContext(request, response);
            if(context==null){
            	// host not valid, redirect to our website homepage
            	response.sendRedirect(defaultServerURL);
            	return;
            }
            
            String serverRootPath=((nds.velocity.WebClient)context.get("myweb")).getServerRootURL();
            String tpath=getVelocityTemplatePath(request, context);
            if(tpath==null || tpath.indexOf(serverRootPath)<0){
//            	 host not valid, redirect to our website homepage
            	response.sendRedirect(serverRootPath);
            	return;
            }
            
            fillContext(context, request);

            // set the content type
            setContentType(request, response);

            // get the template
            Template template = handleRequest(request, response, context);

            // bail if we can't find the template
            if (template == null)
            {
                velocity.warn("VelocityViewServlet: couldn't find template to match request.");
                return;
            }

            // merge the template and context
            mergeTemplate(template, context, response);
        }
        catch (Exception e)
        {
            // log the exception
            velocity.error("VelocityViewServlet: Exception processing the template: "+e);

            // call the error handler to let the derived class
            // do something useful with this failure.
            error(request, response, e);
        }
        finally
        {
            // call cleanup routine to let a derived class do some cleanup
            requestCleanup(request, response, context);
        }
    }


    /**
     * <p>This was a common extension point, but has been deprecated.
     * It has no single replacement.  Instead, you should override 
     * {@link #fillContext} to add custom things to the {@link Context}
     * or override a {@link #getTemplate} method to change how
     * {@link Template}s are retrieved</p>
     *
     * @param request client request
     * @param response client response
     * @param ctx  VelocityContext to fill
     * @return Velocity Template object or null
     */
    protected Template handleRequest(HttpServletRequest request,
                                     HttpServletResponse response,
                                     Context ctx)
        throws Exception
    {
        return getTemplate(request, response, ctx);
    }


    /**
     * Create a context or retrieve context from session attribute
     * When context not found in session attribute, will first try loading adClient infor from
     * request parameter named "client", if still not found, try loading UserWebImpl from session,
     * if user not found or find is guest, throws Exception
     *  
     * When handling automatic task (nds.process.CreateWebSite) webzipping whole web site, client parameter
     * is preferred way as there's no one login. When user preview website, UserWebImpl is preferred way.So
     * whether preview or not depends on this.
     *  
     *  
     * @param request servlet request from client
     * @param response servlet reponse to client
     * @return null if not a valid request ( host name not find in web_client)
     */
    protected VelocityContext createContext(HttpServletRequest request,
                                    HttpServletResponse response)
    {
    	VelocityContext context= null;
    	
    	HttpSession session=request.getSession(true);
    	
    	context=(VelocityContext)session.getAttribute(PROPS_VELOCITY_CONTEXT);
    	if(context==null){
    		boolean preview=false;// default to webzip
    		int clientId=-1;
    		String webDomain=null;

        	if(isMultipleClientEnabled){
        		/**
        		 * for yunbao
        		 */
	    		try{
	    			java.net.URL url = new java.net.URL(request.getRequestURL().toString());
	    			webDomain= url.getHost();
	    			clientId=WebUtils.getAdClientId(webDomain);
	    		}catch(Throwable t){
	    			logger.error("fail to parse host from "+request.getRequestURL() +":"+t);
	    		}
	    		// client id=-1 means not found, when not found, should return null;
	    		if(clientId==-1) return null;
	    		
	    		context= new VelocityContext();
	    		//VelocityUtils.insertVariables(context, clientId, "/");
	    		context.put("myweb", new WebClient(clientId, "/",  webDomain, true) );
        	}else{
        		/**
        		 * For ahyyzb only, which use vml as website language
        		 */
        		webDomain= defaultClientWebDomain;
        		clientId=defaultClientID;
        		
        		context= new VelocityContext();
        		//VelocityUtils.insertVariables(context, clientId, "/");
        		context.put("myweb", new WebClient(clientId, "",  webDomain,false) );
        	}
    		
			try {
				VelocityUtils.insertHelperUtilities(context);
			} catch (Throwable e) {
				throw new NDSRuntimeException("Fail to init velocity", e);
			}
/*			UserWebImpl userWeb =null;
	    	try{
	    		userWeb= ((UserWebImpl)WebUtils.getSessionContextManager(request.getSession()).getActor(nds.util.WebKeys.USER));	
	    	}catch(Throwable userWebException){
	    		
	    	}
	    	if(userWeb==null || userWeb.getUserId()==UserWebImpl.GUEST_ID ){
	    	}else{
	    		clientId= userWeb.getAdClientId();
	    		preview=true;
        		context= new VelocityContext();
        		VelocityUtils.insertVariables(context, clientId, preview?VELOCITY_WEB_ROOT:"/");
	    	}*/
    		session.setAttribute(PROPS_VELOCITY_CONTEXT,context);
    		
    	}
    	//set working request and response
    	context.put("request", request);
    	context.put("response", response);
    	
    	return context;
        
    }


    /**
     * This is an extension hook for users who subclass this servlet to
     * make their own modifications to the {@link Context}. It is a partial
     * replacement of the deprecated {@link #handleRequest} method. This
     * implementation does nothing.
     */
    protected void fillContext(Context context, HttpServletRequest request)
    {
        // this implementation does nothing
    }
        

    /**
     * Sets the content type of the response.  This is available to be overriden
     * by a derived class.
     *
     * <p>The default implementation is :
     * <pre>
     *    response.setContentType(defaultContentType);
     * </pre>
     * where defaultContentType is set to the value of the default.contentType
     * property, or "text/html" if that is not set.</p>
     *
     * @param request servlet request from client
     * @param response servlet reponse to client
     */
    protected void setContentType(HttpServletRequest request,
                                  HttpServletResponse response)
    {
        response.setContentType(defaultContentType);
    }


   
    /**
     * <p>Gets the requested template.</p>
     *
     * @param request client request
     * @param response client response (whose character encoding we'll use)
     * @return Velocity Template object or null
     */
    protected Template getTemplate(HttpServletRequest request,
                                   HttpServletResponse response, Context cxt)
        throws ResourceNotFoundException, ParseErrorException, Exception
    {
        String path = getVelocityTemplatePath(request, (VelocityContext)cxt);
        
        if (response == null)
        {
            return getTemplate(path, this.DEFAULT_OUTPUT_ENCODING);
        }
        else
        {
            return getTemplate(path, response.getCharacterEncoding());
        }
    }


    /**
     * Retrieves the requested template.
     *
     * @param name The file name of the template to retrieve relative to the
     *             template root.
     * @return The requested template.
     * @throws ResourceNotFoundException if template not found
     *          from any available source.
     * @throws ParseErrorException if template cannot be parsed due
     *          to syntax (or other) error.
     * @throws Exception if an error occurs in template initialization
     */
    public Template getTemplate(String name)
        throws ResourceNotFoundException, ParseErrorException, Exception
    {
        return getTemplate(name, null);
    }


    /**
     * Retrieves the requested template with the specified character encoding.
     *
     * @param name The file name of the template to retrieve relative to the
     *             template root.
     * @param encoding the character encoding of the template
     * @return The requested template.
     * @throws ResourceNotFoundException if template not found
     *          from any available source.
     * @throws ParseErrorException if template cannot be parsed due
     *          to syntax (or other) error.
     * @throws Exception if an error occurs in template initialization
     */
    public Template getTemplate(String name, String encoding)
        throws ResourceNotFoundException, ParseErrorException, Exception
    {
        if (encoding == null)
        {
            return getVelocityEngine().getTemplate(name,this.DEFAULT_OUTPUT_ENCODING);
        }
        else
        {
            return getVelocityEngine().getTemplate(name, encoding);
        }
    }


    /**
     * Merges the template with the context.  Only override this if you really, really
     * really need to. (And don't call us with questions if it breaks :)
     *
     * @param template template object returned by the handleRequest() method
     * @param context Context created by the {@link #createContext}
     * @param response servlet reponse (used to get a Writer)
     */
    protected void mergeTemplate(Template template,
                                 Context context,
                                 HttpServletResponse response)
        throws ResourceNotFoundException, ParseErrorException,
               MethodInvocationException, IOException,
               UnsupportedEncodingException, Exception
    {
        VelocityWriter vw = null;
        Writer writer = getResponseWriter(response);
        try
        {
            vw = (VelocityWriter)writerPool.get();
            if (vw == null)
            {
                vw = new VelocityWriter(writer, 4 * 1024, true);
            }
            else
            {
                vw.recycle(writer);
            }
            performMerge(template, context, vw);
        }
        finally
        {
            if (vw != null)
            {
                try
                {
                    // flush and put back into the pool
                    // don't close to allow us to play
                    // nicely with others.
                    vw.flush();
                    /* This hack sets the VelocityWriter's internal ref to the
                     * PrintWriter to null to keep memory free while
                     * the writer is pooled. See bug report #18951 */
                    vw.recycle(null);
                    writerPool.put(vw);
                }
                catch (Exception e)
                {
                    logger.debug("VelocityViewServlet: " +
                                   "Trouble releasing VelocityWriter: " +
                                   e.getMessage());
                }
            }
        }
    }


    /**
     * This is here so developers may override it and gain access to the
     * Writer which the template will be merged into.  See
     * <a href="http://issues.apache.org/jira/browse/VELTOOLS-7">VELTOOLS-7</a>
     * for discussion of this.
     *
     * @param template template object returned by the handleRequest() method
     * @param context Context created by the {@link #createContext}
     * @param writer a VelocityWriter that the template is merged into
     */
    protected void performMerge(Template template, Context context, Writer writer)
        throws ResourceNotFoundException, ParseErrorException,
               MethodInvocationException, Exception
    {
        template.merge(context, writer);
    }


    /**
     * Invoked when there is an error thrown in any part of doRequest() processing.
     * <br><br>
     * Default will send a simple HTML response indicating there was a problem.
     *
     * @param request original HttpServletRequest from servlet container.
     * @param response HttpServletResponse object from servlet container.
     * @param e  Exception that was thrown by some other part of process.
     */
    protected void error(HttpServletRequest request,
                         HttpServletResponse response,
                         Exception e)
        throws ServletException
    {
    	VelocityContext context= null;
    	
    	HttpSession session=request.getSession();
    	
    	context=(VelocityContext)session.getAttribute(PROPS_VELOCITY_CONTEXT);

    	try
        {
            StringBuffer html = new StringBuffer();
            html.append("<html>\n");
            html.append("<head><title>Error</title></head>\n");
            html.append("<body>\n");
            html.append("<h2>VelocityViewServlet : Error processing a template for path '");
            html.append(getVelocityTemplatePath(request,context));
            html.append("'</h2>\n");

            Throwable cause = e;

            String why = cause.getMessage();
            if (why != null && why.trim().length() > 0)
            {
                html.append(StringEscapeUtils.escapeHtml(why));
                html.append("\n<br>\n");
            }

            // if it's an MIE, i want the real stack trace!
            if (cause instanceof MethodInvocationException)
            {
                // get the real cause
                cause = ((MethodInvocationException)cause).getWrappedThrowable();
            }

            StringWriter sw = new StringWriter();
            cause.printStackTrace(new PrintWriter(sw));

            html.append("<pre>\n");
            html.append(StringEscapeUtils.escapeHtml(sw.toString()));
            html.append("</pre>\n");
            html.append("</body>\n");
            html.append("</html>");
            getResponseWriter(response).write(html.toString());
        }
        catch (Exception e2)
        {
            // clearly something is quite wrong.
            // let's log the new exception then give up and
            // throw a servlet exception that wraps the first one
            velocity.error("VelocityViewServlet: Exception while printing error screen: "+e2);
            throw new ServletException(e);
        }
    }

    /**
     * <p>Procure a Writer with correct encoding which can be used
     * even if HttpServletResponse's <code>getOutputStream()</code> method
     * has already been called.</p>
     *
     * <p>This is a transitional method which will be removed in a
     * future version of Velocity.  It is not recommended that you
     * override this method.</p>
     *
     * @param response The response.
     * @return A <code>Writer</code>, possibly created using the
     *        <code>getOutputStream()</code>.
     */
    protected Writer getResponseWriter(HttpServletResponse response)
        throws UnsupportedEncodingException, IOException
    {
        Writer writer = null;
        try
        {
            writer = response.getWriter();
        }
        catch (IllegalStateException e)
        {
            // ASSUMPTION: We already called getOutputStream(), so
            // calls to getWriter() fail.  Use of OutputStreamWriter
            // assures our desired character set
            if (this.warnOfOutputStreamDeprecation)
            {
                this.warnOfOutputStreamDeprecation = false;
                velocity.warn("VelocityViewServlet: " +
                              "Use of ServletResponse's getOutputStream() " +
                              "method with VelocityViewServlet is " +
                              "deprecated -- support will be removed in " +
                              "an upcoming release");
            }
            // Assume the encoding has been set via setContentType().
            String encoding = response.getCharacterEncoding();
            if (encoding == null)
            {
                encoding = DEFAULT_OUTPUT_ENCODING;
            }
            writer = new OutputStreamWriter(response.getOutputStream(),
                                            encoding);
        }
        return writer;
    }


    /**
     * Cleanup routine called at the end of the request processing sequence
     * allows a derived class to do resource cleanup or other end of
     * process cycle tasks.  This default implementation does nothing.
     *
     * @param request servlet request from client
     * @param response servlet reponse
     * @param context Context created by the {@link #createContext}
     */
    protected void requestCleanup(HttpServletRequest request,
                                  HttpServletResponse response,
                                  Context context)
    {
    }
    /**
     * Retrieves the path for the specified request regardless of
     * whether this is a direct request or an include by the
     * RequestDispatcher.
     */
    private String getVelocityTemplatePath(HttpServletRequest request, VelocityContext vc)
    {
        // If we get here from RequestDispatcher.include(), getServletPath()
        // will return the original (wrong) URI requested.  The following special
        // attribute holds the correct path.  See section 8.3 of the Servlet
        // 2.3 specification.
        String path = (String)request.getAttribute("javax.servlet.include.servlet_path");
        // also take into account the PathInfo stated on SRV.4.4 Request Path Elements
        String info = (String)request.getAttribute("javax.servlet.include.path_info");
        if (path == null)
        {
            path = request.getServletPath();
            info = request.getPathInfo();
        }
        if (info != null)
        {
            path += info;
        }
        //when url path contains /html/nds/website/, it must have same folder name as specified in webclient 
		/* try{
			 String serverRootPath=((nds.velocity.WebClient)vc.get("myweb")).getServerRootURL();
			 if(path!=null &&  path.indexOf(serverRootPath)<0){
				 //forcefully direct path to serverRootPath
				 path=serverRootPath+"/index.vml";
			 }
		 }catch(Throwable t){
			 logger.error("Fail to fetch myweb from VelocityContext", t);
		 }
        */
        
        return path;
        /*if(vc!=null){
        	WebClient wc= (WebClient)vc.get("myweb");
            return wc.getServerRootURL()+path;
        }else
        	return path;
        */
    }
}
