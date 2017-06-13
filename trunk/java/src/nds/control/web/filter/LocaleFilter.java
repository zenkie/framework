package nds.control.web.filter;

import org.apache.struts.Globals;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.util.CookieKeys;
import com.liferay.util.CookieUtil;

import nds.control.web.ServletContextManager;
import nds.control.web.WebUtils;
import nds.control.web.reqhandler.LocaleRequestWrapper;
import nds.util.LicenseMake;
import nds.util.LicenseWrapper;
import nds.util.ParamUtils;
import nds.util.Validator;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Iterator;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.servlet.http.Cookie;

public class LocaleFilter extends OncePerRequestFilter {

//	public static final String CHINESELANGUAGE = "中文(简体)";
//	public static final String ENGLISHLANGUAGE = "English";
	
    @SuppressWarnings("unchecked")
	public void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                 FilterChain chain)
            throws IOException, ServletException {
		
		/*过滤器实现思路： 
		（1）判断cookies中是否有值，有值就将cookies中的值得到一个Locale
		（2）cookies没值就根据浏览器默认的语言来显示*/
    
    	Locale currentLocale = request.getLocale(); //定义语言地区信息 
    	Locale preferredLocale = request.getLocale();
		String language = ParamUtils.getAttributeOrParameter(request, "lang");//客户端页面写的语言cookies
	    String langCookie=CookieUtil.get(request.getCookies(),"lang");
	    logger.debug("currentLocale ->"+currentLocale.toString());
	    logger.debug("language ->"+language);
	    
		ServletContextManager scm= WebUtils.getServletContextManager();
      	    
  	    LicenseMake licmark=(LicenseMake)scm.getActor(nds.util.WebKeys.LIC_MANAGER);
  	    
  		Iterator b=licmark.getLicenses();
  		
  		Boolean enablelanguage=false;
  		
  		 while (b.hasNext()) {
		    	LicenseWrapper o = (LicenseWrapper)b.next();
		    	enablelanguage=o.getEnablelanguage();
		    }
		
//		if (Validator.isNotNull(langCookie)&&langCookie.contains("_")) {
//			language=langCookie;
//		}
		try {
			
			/**
			 * 1.如果cookies中是有值的（前提条件是从页面传过来的语言为空才进下面的逻辑）
			 */
			 if(language != null&&enablelanguage){
				 String[] clang=language.split("_");
				    Locale defaultLocale=new Locale(clang[0],clang[1]); 
		        	String lang=defaultLocale.getLanguage();
		            String country=defaultLocale.getCountry();
		            String variant=defaultLocale.getVariant();
		        	Cookie langck = new Cookie("lang", defaultLocale.toString());
		        	langck.setPath(StringPool.SLASH);
					CookieKeys.addCookie(response, langck);
		            //RequestUtil.setCookie(response, "lang", defaultLocale.toString(), "/", null);
		            preferredLocale=new Locale(lang,country,variant);
		            request = new LocaleRequestWrapper(request, preferredLocale);
		            LocaleContextHolder.setLocale(preferredLocale);
			}else if(Validator.isNotNull(langCookie)&&langCookie.contains("_")&&enablelanguage){
				logger.debug("cookie lang->"+langCookie);
				 	String[] clang=langCookie.split("_");
				    Locale defaultLocale=new Locale(clang[0],clang[1]); 
		        	String lang=defaultLocale.getLanguage();
		            String country=defaultLocale.getCountry();
		            String variant=defaultLocale.getVariant();
		        	Cookie langck = new Cookie("lang", defaultLocale.toString());
		        	langck.setPath(StringPool.SLASH);
					CookieKeys.addCookie(response, langck);
		            //RequestUtil.setCookie(response, "lang", defaultLocale.toString(), "/", null);
		            preferredLocale=new Locale(lang,country,variant);
		            request = new LocaleRequestWrapper(request, preferredLocale);
		            LocaleContextHolder.setLocale(preferredLocale);
			}
			/**
			 * 传过来的语言：如果既没有传过来的语言和cookies里面的值就根据浏览器默认的语言来显示
			 */
			else {
//				String defaultLanguage = currentLocale.toString(); //浏览器默认的语言
					Locale defaultLocale=new Locale("zh","CN"); 
	        		String lang=defaultLocale.getLanguage();
	            	String country=defaultLocale.getCountry();
	            	String variant=defaultLocale.getVariant();
				    //request.getSession().setAttribute("lan", ENGLISHLANGUAGE);
		            preferredLocale=new Locale(lang,country,variant);
		            request = new LocaleRequestWrapper(request, preferredLocale);
		            LocaleContextHolder.setLocale(preferredLocale);
				
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			///最后将设置好的环境类放到session中去
			request.getSession().removeAttribute(Globals.LOCALE_KEY);
			request.getSession().setAttribute(Globals.LOCALE_KEY, preferredLocale);
		}
        chain.doFilter(request, response);
    }
}
