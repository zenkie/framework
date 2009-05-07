package nds.velocity;

import nds.util.*;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.io.*;

import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.taglib.tiles.ComponentConstants;
import org.apache.struts.tiles.ComponentContext;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.*;


import com.liferay.portal.language.LanguageUtil_IW;
import com.liferay.portal.language.UnicodeLanguageUtil_IW;
import com.liferay.portal.model.Theme;
import com.liferay.portal.service.permission.AccountPermission_IW;
import com.liferay.portal.service.permission.CommonPermission_IW;
import com.liferay.portal.service.permission.GroupPermission_IW;
import com.liferay.portal.service.permission.LayoutPermission_IW;
import com.liferay.portal.service.permission.LocationPermission_IW;
import com.liferay.portal.service.permission.OrganizationPermission_IW;
import com.liferay.portal.service.permission.PortalPermission_IW;
import com.liferay.portal.service.permission.PortletPermission_IW;
import com.liferay.portal.service.permission.RolePermission_IW;
import com.liferay.portal.service.permission.UserGroupPermission_IW;
import com.liferay.portal.service.permission.UserPermission_IW;
import com.liferay.portal.theme.ThemeDisplay;
import com.liferay.portal.util.PortalUtil_IW;
import com.liferay.portal.util.PropsUtil_IW;
import com.liferay.portal.util.ServiceLocator;
import com.liferay.portal.util.SessionClicks_IW;
import com.liferay.portal.util.WebKeys;
import com.liferay.portlet.PortletConfigImpl;
import com.liferay.portlet.PortletURLFactory;
import com.liferay.util.ArrayUtil_IW;
import com.liferay.util.BrowserSniffer_IW;
import com.liferay.util.GetterUtil;
import com.liferay.util.GetterUtil_IW;
import com.liferay.util.Randomizer_IW;
import com.liferay.util.StaticFieldGetter;
import com.liferay.util.StringUtil_IW;
import com.liferay.util.UnicodeFormatter_IW;
import com.liferay.util.Validator;
import com.liferay.util.velocity.VelocityResourceListener;

/**
 * Initialize variables used in velocity templates
 * @author yfzhu
 */
public class VelocityUtils {
	
	 
	
	
	public static String evaluate(String input) throws Exception {
		Velocity.init();

		VelocityContext context = new VelocityContext();

		StringWriter output = new StringWriter();

		Velocity.evaluate(context, output, VelocityUtils.class.getName(), input);

		return output.toString();
	}
	
	/*public static void insertVariables(
			VelocityContext vc, int adClientId, String serverRootURL) {
			//vc.put("webroot", arg1)
			// Request
			//vc.put("myweb", new WebClient(adClientId, serverRootURL, ) );
			
			// Helper utilities

			insertHelperUtilities(vc);

			// Insert custom vm variables

	}*/
	
	public static void insertHelperUtilities(VelocityContext vc) {

		// Array util

		vc.put("arrayUtil", ArrayUtil_IW.getInstance());

		// Browser sniffer

		vc.put(
			"browserSniffer", BrowserSniffer_IW.getInstance());

		// Getter util

		vc.put("getterUtil", GetterUtil_IW.getInstance());

		// Language

		vc.put("languageUtil", LanguageUtil_IW.getInstance());
		vc.put("unicodeLanguageUtil", UnicodeLanguageUtil_IW.getInstance());

		// Portal util

		vc.put("portalUtil", PortalUtil_IW.getInstance());

		// Props util

		vc.put("propsUtil", PropsUtil_IW.getInstance());

		// Portlet URL factory

		vc.put("portletURLFactory", PortletURLFactory.getInstance());

		// Randomizer

		vc.put("randomizer", Randomizer_IW.getInstance().getInstance());

		// Service locator

		vc.put("serviceLocator", ServiceLocator.getInstance());

		// Session clicks

		vc.put("sessionClicks", SessionClicks_IW.getInstance());

		// Static field getter

		vc.put("staticFieldGetter", StaticFieldGetter.getInstance());

		// String util

		vc.put("stringUtil", StringUtil_IW.getInstance());

		// Unicode formatter

		vc.put("unicodeFormatter", UnicodeFormatter_IW.getInstance());

	}
	
}
