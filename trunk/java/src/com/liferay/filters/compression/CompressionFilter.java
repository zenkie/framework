/**
 * Removed UTF-8 Encoding setting
 */
package com.liferay.filters.compression;

import com.liferay.portal.kernel.util.ServerDetector;
import com.liferay.util.BrowserSniffer;
import com.liferay.util.GetterUtil;
import com.liferay.util.Http;
import com.liferay.util.ParamUtil;
import com.liferay.util.SystemProperties;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <a href="CompressionFilter.java.html"><b><i>View Source</i></b></a>
 *
 * @author  Brian Wing Shun Chan
 *
 */
public class CompressionFilter implements Filter {

	static boolean useCompressionFilter = GetterUtil.getBoolean(
		SystemProperties.get(CompressionFilter.class.getName()), true);

	static {

		// The compression filter will work on JBoss, Jetty, JOnAS, OC4J, Orion,
		// and Tomcat, but may break on other servers

		if (useCompressionFilter) {
			if (ServerDetector.isJBoss() || ServerDetector.isJetty() ||
				ServerDetector.isJOnAS() || ServerDetector.isOC4J() ||
				ServerDetector.isOrion() || ServerDetector.isTomcat()) {

				useCompressionFilter = true;
			}
			else {
				useCompressionFilter = false;
			}
		}
	}

	public static final String ENCODING = GetterUtil.getString(
		SystemProperties.get(CompressionFilter.class.getName() + ".encoding"),
		"UTF-8");

	public void init(FilterConfig config) {
	}

	public void doFilter(
			ServletRequest req, ServletResponse res, FilterChain chain)
		throws IOException, ServletException {

		if (_log.isDebugEnabled()) {
			if (useCompressionFilter) {
				_log.debug("Compression is enabled");
			}
			else {
				_log.debug("Compression is disabled");
			}
		}

		HttpServletRequest httpReq = (HttpServletRequest)req;
		HttpServletResponse httpRes = (HttpServletResponse)res;

		//yfzhu marked up following as it will cause error as following: 2007-10-22
/*
java.lang.IllegalStateException: getReader() or getInputStream() called
        at org.mortbay.jetty.servlet.ServletHttpRequest.setCharacterEncoding(ServletHttpRequest.java:602)
        at javax.servlet.ServletRequestWrapper.setCharacterEncoding(ServletRequestWrapper.java:112)
        at com.liferay.filters.compression.CompressionFilter.doFilter(CompressionFilter.java:98)
        at org.mortbay.jetty.servlet.WebApplicationHandler$CachedChain.doFilter(WebApplicationHandler.java:821)
        at com.liferay.portal.servlet.filters.virtualhost.VirtualHostFilter.doFilter(VirtualHostFilter.java:169)
        at org.mortbay.jetty.servlet.WebApplicationHandler$CachedChain.doFilter(WebApplicationHandler.java:821)
        at org.mortbay.jetty.servlet.WebApplicationHandler.dispatch(WebApplicationHandler.java:471)
        at org.mortbay.jetty.servlet.Dispatcher.dispatch(Dispatcher.java:286)
        at org.mortbay.jetty.servlet.Dispatcher.forward(Dispatcher.java:171)
        at org.directwebremoting.impl.DefaultWebContext.forwardToString(DefaultWebContext.java:135)
 */		
		//httpReq.setCharacterEncoding(ENCODING);

		String completeURL = Http.getCompleteURL(httpReq);

		if (useCompressionFilter && isCompress(httpReq) &&
			!isInclude(httpReq) && BrowserSniffer.acceptsGzip(httpReq) &&
			!isAlreadyFiltered(httpReq)) {

			if (_log.isDebugEnabled()) {
				_log.debug("Compressing " + completeURL);
			}

			httpReq.setAttribute(_ALREADY_FILTERED, Boolean.TRUE);

			CompressionResponse compressionResponse =
				new CompressionResponse(httpRes);

			chain.doFilter(req, compressionResponse);

			compressionResponse.finishResponse();
		}
		else {
			if (_log.isDebugEnabled()) {
				_log.debug("Not compressing " + completeURL);
			}

			chain.doFilter(req, res);
		}
	}

	public void destroy() {
	}

	protected boolean isAlreadyFiltered(HttpServletRequest req) {
		if (req.getAttribute(_ALREADY_FILTERED) != null) {
			return true;
		}
		else {
			return false;
		}
	}

	protected boolean isCompress(HttpServletRequest req) {
		if (!ParamUtil.get(req, _COMPRESS, true)) {
			return false;
		}
		else {

			// The exclusive state is used to stream binary content.
			// Compressing binary content through a servlet filter is bad on
			// performance because the user will not start downloading the
			// content until the entire content is compressed.

			String windowState = ParamUtil.getString(req, "p_p_state");

			if (windowState.equals("exclusive")) {
				return false;
			}
			else {
				return true;
			}
		}
	}

	protected boolean isInclude(HttpServletRequest req) {
		String uri = (String)req.getAttribute(_INCLUDE);

		if (uri == null) {
			return false;
		}
		else {
			return true;
		}
	}

	private static final String _ALREADY_FILTERED =
		CompressionFilter.class + "_ALREADY_FILTERED";

	private static final String _COMPRESS = "compress";

	private static final String _INCLUDE = "javax.servlet.include.request_uri";

	private static Log _log = LogFactory.getLog(CompressionFilter.class);

}