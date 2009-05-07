/*
 * Agile Control Technologies Ltd,. CO.
 * http://www.agileControl.com
 */
package nds.portlet.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.TimeZone;

import javax.portlet.PortletMode;
import javax.portlet.PortletURL;
import javax.portlet.WindowState;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.Globals;

import com.liferay.portal.LayoutPermissionException;
import com.liferay.portal.NoSuchLayoutSetException;
import com.liferay.portal.PortalException;
import com.liferay.portal.SystemException;
import com.liferay.portal.kernel.security.permission.PermissionChecker;
import com.liferay.portal.kernel.servlet.PortletSessionTracker;
import com.liferay.portal.kernel.util.StackTraceUtil;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.language.LanguageUtil;
import com.liferay.portal.model.ColorScheme;
import com.liferay.portal.model.Company;
import com.liferay.portal.model.Group;
import com.liferay.portal.model.Layout;
import com.liferay.portal.model.LayoutSet;
import com.liferay.portal.model.LayoutTypePortlet;
import com.liferay.portal.model.Theme;
import com.liferay.portal.model.User;
import com.liferay.portal.model.impl.ColorSchemeImpl;
import com.liferay.portal.model.impl.GroupImpl;
import com.liferay.portal.model.impl.LayoutImpl;
import com.liferay.portal.model.impl.LayoutTypePortletImpl;
import com.liferay.portal.model.impl.ThemeImpl;
import com.liferay.portal.security.auth.CompanyThreadLocal;
import com.liferay.portal.security.permission.ActionKeys;
import com.liferay.portal.security.permission.PermissionCheckerFactory;
import com.liferay.portal.security.permission.PermissionCheckerImpl;
import com.liferay.portal.security.permission.PermissionThreadLocal;
import com.liferay.portal.service.CompanyLocalServiceUtil;
import com.liferay.portal.service.GroupLocalServiceUtil;
import com.liferay.portal.service.LayoutLocalServiceUtil;
import com.liferay.portal.service.LayoutSetLocalServiceUtil;
import com.liferay.portal.service.UserLocalServiceUtil;
import com.liferay.portal.service.impl.ThemeLocalUtil;
import com.liferay.portal.service.permission.GroupPermission;
import com.liferay.portal.service.permission.LayoutPermission;
import com.liferay.portal.theme.ThemeDisplay;
import com.liferay.portal.theme.ThemeDisplayFactory;
import com.liferay.portal.util.CookieKeys;
import com.liferay.portal.util.LayoutClone;
import com.liferay.portal.util.LayoutCloneFactory;
import com.liferay.portal.util.PortalUtil;
import com.liferay.portal.util.PortletKeys;
import com.liferay.portal.util.PrefsPropsUtil;
import com.liferay.portal.util.PropsUtil;
import com.liferay.portal.util.Resolution;
import com.liferay.portal.util.WebKeys;
import com.liferay.portlet.LiferayWindowState;
import com.liferay.portlet.PortletURLImpl;
import com.liferay.util.CookieUtil;
import com.liferay.util.GetterUtil;
import com.liferay.util.Http;
import com.liferay.util.HttpHeaders;
import com.liferay.util.ListUtil;
import com.liferay.util.LocaleUtil;
import com.liferay.util.NullSafeProperties;
import com.liferay.util.ParamUtil;
import com.liferay.util.PropertiesUtil;
import com.liferay.util.Validator;
import com.liferay.util.dao.hibernate.QueryUtil;
import com.liferay.util.servlet.SessionErrors;
/**
 * 
 * @author yfzhu@agilecontrol.com
 */

public class UserUtils {
    private final static String GET_USER_LOCALE="select languageid  from lportal.user_ where userid=?";
    private final static String GET_USER_SKIN="select skinid  from lportal.user_ where userid=?";
    private static ThemeDisplay defaultThemeDisplay=null;

    private static Log _log = LogFactory.getLog(UserUtils.class);

    public static void initPageContext(HttpServletRequest req, HttpServletResponse res) throws Exception{
    	if(req.getAttribute(WebKeys.THEME_DISPLAY)!=null) return;
    	
    	ServletContext ctx = req.getSession().getServletContext();
    	HttpSession ses = req.getSession();
    	ServletContext portalCtx = null;
    	String _companyId = ctx.getInitParameter("company_id");
		try {
			portalCtx = ctx.getContext(
				PrefsPropsUtil.getString(_companyId, PropsUtil.PORTAL_CTX));
		}
		catch (Exception e) {
			_log.error(StackTraceUtil.getStackTrace(e));
		}

		if (portalCtx == null) {
			portalCtx = ctx;
		}

		req.setAttribute(WebKeys.CTX, portalCtx);
//		 Portlet session tracker

		if (ses.getAttribute(WebKeys.PORTLET_SESSION_TRACKER) == null ) {
			ses.setAttribute(
				WebKeys.PORTLET_SESSION_TRACKER,
				PortletSessionTracker.getInstance());
		}

		// WebKeys.COMPANY_ID variable

		String companyId = (String)ctx.getAttribute(WebKeys.COMPANY_ID);

		if (portalCtx.getAttribute(WebKeys.COMPANY_ID) == null) {
			portalCtx.setAttribute(WebKeys.COMPANY_ID, companyId);
		}

		if (ses.getAttribute(WebKeys.COMPANY_ID) == null) {
			ses.setAttribute(WebKeys.COMPANY_ID, companyId);
		}

		req.setAttribute(WebKeys.COMPANY_ID, companyId);

		CompanyThreadLocal.setCompanyId(companyId);

		// ROOT_PATH variable

		String rootPath = (String)ctx.getAttribute(WebKeys.ROOT_PATH);

		if (portalCtx.getAttribute(WebKeys.ROOT_PATH) == null) {
			portalCtx.setAttribute(WebKeys.ROOT_PATH, rootPath);
		}

		if (ses.getAttribute(WebKeys.ROOT_PATH) == null) {
			ses.setAttribute(WebKeys.ROOT_PATH, rootPath);
		}

		req.setAttribute(WebKeys.ROOT_PATH, rootPath);

		// MAIN_PATH variable

		String mainPath = (String)ctx.getAttribute(WebKeys.MAIN_PATH);

		if (portalCtx.getAttribute(WebKeys.MAIN_PATH) == null) {
			portalCtx.setAttribute(WebKeys.MAIN_PATH, mainPath);
		}

		if (ses.getAttribute(WebKeys.MAIN_PATH) == null) {
			ses.setAttribute(WebKeys.MAIN_PATH, mainPath);
		}

		req.setAttribute(WebKeys.MAIN_PATH, mainPath);

		// FRIENDLY_URL_PRIVATE_PATH variable

		String friendlyURLPrivatePath =
			(String)ctx.getAttribute(WebKeys.FRIENDLY_URL_PRIVATE_PATH);

		if (portalCtx.getAttribute(WebKeys.FRIENDLY_URL_PRIVATE_PATH) == null) {
			portalCtx.setAttribute(
				WebKeys.FRIENDLY_URL_PRIVATE_PATH, friendlyURLPrivatePath);
		}

		if (ses.getAttribute(WebKeys.FRIENDLY_URL_PRIVATE_PATH) == null) {
			ses.setAttribute(
				WebKeys.FRIENDLY_URL_PRIVATE_PATH, friendlyURLPrivatePath);
		}

		req.setAttribute(
			WebKeys.FRIENDLY_URL_PRIVATE_PATH, friendlyURLPrivatePath);

		// FRIENDLY_URL_PUBLIC_PATH variable

		String friendlyURLPublicPath =
			(String)ctx.getAttribute(WebKeys.FRIENDLY_URL_PUBLIC_PATH);

		if (portalCtx.getAttribute(WebKeys.FRIENDLY_URL_PUBLIC_PATH) == null) {
			portalCtx.setAttribute(
				WebKeys.FRIENDLY_URL_PUBLIC_PATH, friendlyURLPublicPath);
		}

		if (ses.getAttribute(WebKeys.FRIENDLY_URL_PUBLIC_PATH) == null) {
			ses.setAttribute(
				WebKeys.FRIENDLY_URL_PUBLIC_PATH, friendlyURLPublicPath);
		}

		req.setAttribute(
			WebKeys.FRIENDLY_URL_PUBLIC_PATH, friendlyURLPublicPath);

		// IMAGE_PATH variable

		String imagePath = (String)ctx.getAttribute(WebKeys.IMAGE_PATH);

		if (portalCtx.getAttribute(WebKeys.IMAGE_PATH) == null) {
			portalCtx.setAttribute(WebKeys.IMAGE_PATH, imagePath);
		}

		if (ses.getAttribute(WebKeys.IMAGE_PATH) == null) {
			ses.setAttribute(WebKeys.IMAGE_PATH, imagePath);
		}

		req.setAttribute(WebKeys.IMAGE_PATH, imagePath);

		// Portlet Request Processor
/*
		PortletRequestProcessor portletReqProcessor =
			(PortletRequestProcessor)portalCtx.getAttribute(
				WebKeys.PORTLET_STRUTS_PROCESSOR);

		if (portletReqProcessor == null) {
			portletReqProcessor =
				PortletRequestProcessor.getInstance(this, moduleConfig);

			portalCtx.setAttribute(
				WebKeys.PORTLET_STRUTS_PROCESSOR, portletReqProcessor);
		}

		// Tiles definitions factory

		if (portalCtx.getAttribute(TilesUtilImpl.DEFINITIONS_FACTORY) == null) {
			portalCtx.setAttribute(
				TilesUtilImpl.DEFINITIONS_FACTORY,
				ctx.getAttribute(TilesUtilImpl.DEFINITIONS_FACTORY));
		}
*/
		Object applicationAssociate = ctx.getAttribute(WebKeys.ASSOCIATE_KEY);

		if (portalCtx.getAttribute(WebKeys.ASSOCIATE_KEY) == null) {
			portalCtx.setAttribute(WebKeys.ASSOCIATE_KEY, applicationAssociate);
		}

		// Set character encoding

		String strutsCharEncoding =
			PropsUtil.get(PropsUtil.STRUTS_CHAR_ENCODING);

		//req.setCharacterEncoding(strutsCharEncoding);

		/*if (!BrowserSniffer.is_wml(req)) {
		res.setContentType(
				Constants.TEXT_HTML + "; charset=" + strutsCharEncoding);
		}*/

		// Determine content type

		String contentType = req.getHeader(HttpHeaders.CONTENT_TYPE);
		// Current URL
		String completeURL = Http.getCompleteURL(req);

		if ((Validator.isNotNull(completeURL)) &&
			(completeURL.indexOf("j_security_check") == -1)) {

			completeURL = completeURL.substring(
				completeURL.indexOf("://") + 3, completeURL.length());

			completeURL = completeURL.substring(
				completeURL.indexOf("/"), completeURL.length());
		}

		if (Validator.isNull(completeURL)) {
			completeURL = mainPath;
		}

		req.setAttribute(WebKeys.CURRENT_URL, completeURL);

		// Login

		String userId = PortalUtil.getUserId(req);
		String remoteUser = req.getRemoteUser();
/*
		// Is JAAS enabled?

		if (!GetterUtil.getBoolean(
				PropsUtil.get(PropsUtil.PORTAL_JAAS_ENABLE))) {

			String jRemoteUser = (String)ses.getAttribute("j_remoteuser");

			if (jRemoteUser != null) {
				remoteUser = jRemoteUser;

				ses.removeAttribute("j_remoteuser");
			}
		}

		if ((userId != null) && (remoteUser == null)) {
			remoteUser = userId;
		}

		// WebSphere will not return the remote user unless you are
		// authenticated AND accessing a protected path. Other servers will
		// return the remote user for all threads associated with an
		// authenticated user. We use ProtectedServletRequest to ensure we get
		// similar behavior across all servers.

		req = new ProtectedServletRequest(req, remoteUser);

		if ((userId != null) || (remoteUser != null)) {

			// Set the principal associated with this thread

			String name = userId;

			if (remoteUser != null) {
				name = remoteUser;
			}

			PrincipalThreadLocal.setName(name);
		}
*/		
		Company company = PortalUtil.getCompany(req);
		String contextPath = PrefsPropsUtil.getString(
				companyId, PropsUtil.PORTAL_CTX);

			if (contextPath.equals(StringPool.SLASH)) {
				contextPath = StringPool.BLANK;
			}

/*			String rootPath = (String)req.getAttribute(WebKeys.ROOT_PATH);
			String mainPath = (String)req.getAttribute(WebKeys.MAIN_PATH);
			String friendlyURLPrivatePath =
				(String)req.getAttribute(WebKeys.FRIENDLY_URL_PRIVATE_PATH);
			String friendlyURLPublicPath =
				(String)req.getAttribute(WebKeys.FRIENDLY_URL_PUBLIC_PATH);
			String imagePath = (String)req.getAttribute(WebKeys.IMAGE_PATH);
*/
			// Company logo

		String companyLogo =
			imagePath + "/company_logo?img_id=" + companyId;
		User user = PortalUtil.getUser(req);

		boolean signedIn = false;

		if (user == null) {
			user = company.getDefaultUser();
		}
		else if (!user.isDefaultUser()) {
			signedIn = true;
		}
		User realUser = user;

		String realUserId = (String)ses.getAttribute(WebKeys.USER_ID);

		if (realUserId != null) {
			if (!user.getUserId().equals(realUserId)) {
				realUser = UserLocalServiceUtil.getUserById(realUserId);
			}
		}

		Locale locale = (Locale)ses.getAttribute(Globals.LOCALE_KEY);

		if (locale == null) {
			if (signedIn) {
				locale = user.getLocale();
			}
			else {

				// User previously set their preferred language

				String languageId = CookieUtil.get(
					req.getCookies(), CookieKeys.GUEST_LANGUAGE_ID);

				if (Validator.isNotNull(languageId)) {
					locale = LocaleUtil.fromLanguageId(languageId);
				}

				// Get locale from the request

				if ((locale == null) &&
					GetterUtil.getBoolean(
						PropsUtil.get(PropsUtil.LOCALE_DEFAULT_REQUEST))) {

					locale = req.getLocale();
				}

				// Get locale from the default user

				if (locale == null) {
					locale = user.getLocale();
				}

				if (Validator.isNull(locale.getCountry())) {

					// Locales must contain the country code

					locale = LanguageUtil.getLocale(locale.getLanguage());
				}

				List availableLocales = ListUtil.fromArray(
					LanguageUtil.getAvailableLocales());

				if (!availableLocales.contains(locale)) {
					locale = user.getLocale();
				}
			}

			ses.setAttribute(Globals.LOCALE_KEY, locale);

			LanguageUtil.updateCookie(res, locale);
		}
		// Cookie support

		CookieKeys.addSupportCookie(res);

		// Time zone

		TimeZone timeZone = user.getTimeZone();

		if (timeZone == null) {
			timeZone = company.getTimeZone();
		}
/*
		String doAsUserId = ParamUtil.getString(req, "doAsUserId");
		
		// Permission checker

		PermissionCheckerImpl permissionChecker =
			PermissionCheckerFactory.create(user, signedIn, true);

		PermissionThreadLocal.setPermissionChecker(permissionChecker);
		
		// Layout
		Layout layout = null;
		List layouts = null;
		String plid =null;
		String layoutId =null;
		String ownerId =null;

		Object[] defaultLayout = getDefaultLayout(req, user, signedIn);
		
		layout = (Layout)defaultLayout[0];
		layouts = (List)defaultLayout[1];

		req.setAttribute(WebKeys.LAYOUT_DEFAULT, Boolean.TRUE);
		Object[] viewableLayouts = getViewableLayouts(
				layout, layouts, permissionChecker, req);

			layout = (Layout)viewableLayouts[0];
			layouts = (List)viewableLayouts[1];

			if (layout != null) {
				plid = layout.getPlid();

				layoutId = layout.getLayoutId();
				ownerId = layout.getOwnerId();
			}

			if ((layout != null) && layout.isShared()) {

				// Updates to shared layouts are not reflected until the next
				// time the user logs in because group layouts are cached in the
				// session

				layout = (Layout)((LayoutImpl)layout).clone();

				LayoutClone layoutClone = LayoutCloneFactory.getInstance();

				if (layoutClone != null) {
					String typeSettings =
						layoutClone.get(req, layout.getPlid());

					if (typeSettings != null) {
						Properties props = new NullSafeProperties();

						PropertiesUtil.load(props, typeSettings);

						String stateMax = props.getProperty(
							LayoutTypePortletImpl.STATE_MAX);
						String stateMin = props.getProperty(
							LayoutTypePortletImpl.STATE_MIN);

						LayoutTypePortlet layoutTypePortlet =
							(LayoutTypePortlet)layout.getLayoutType();

						layoutTypePortlet.setStateMax(stateMax);
						layoutTypePortlet.setStateMin(stateMin);
					}
				}
			}

			LayoutTypePortlet layoutTypePortlet = null;

			if (layout != null) {
				req.setAttribute(WebKeys.LAYOUT, layout);
				req.setAttribute(WebKeys.LAYOUTS, layouts);

				layoutTypePortlet = (LayoutTypePortlet)layout.getLayoutType();

				if (layout.isPrivateLayout()) {
					permissionChecker.setCheckGuest(false);
				}
			}

			String portletGroupId = PortalUtil.getPortletGroupId(plid);

			// Theme and color scheme
*/
			Theme theme = null;
			ColorScheme colorScheme = null;

			/*if (layout != null) {
				theme = layout.getTheme();
				colorScheme = layout.getColorScheme();
			}
			else {*/
				theme = ThemeLocalUtil.getTheme(
					companyId, ThemeImpl.getDefaultThemeId());
				colorScheme = ThemeLocalUtil.getColorScheme(
					companyId, theme.getThemeId(),
					ColorSchemeImpl.getDefaultColorSchemeId());
			/*}*/

			req.setAttribute(WebKeys.THEME, theme);
			req.setAttribute(WebKeys.COLOR_SCHEME, colorScheme);

			// Resolution

			int resolution = Resolution.FULL_RESOLUTION;

			String resolutionKey = user.getResolution();

			if (resolutionKey.equals(Resolution.S1024X768_KEY)) {
				resolution = Resolution.S1024X768_RESOLUTION;
			}
			else if (resolutionKey.equals(Resolution.S800X600_KEY)) {
				resolution = Resolution.S800X600_RESOLUTION;
			}

			// Theme display

			String protocol = Http.getProtocol(req) + "://";

			ThemeDisplay themeDisplay = ThemeDisplayFactory.create();

			themeDisplay.setCompany(company);
			themeDisplay.setCompanyLogo(companyLogo);
			themeDisplay.setUser(user);
			themeDisplay.setRealUser(realUser);
			//themeDisplay.setDoAsUserId(doAsUserId);
			//themeDisplay.setLayout(layout);
			//themeDisplay.setLayouts(layouts);
			//themeDisplay.setPlid(plid);
			//themeDisplay.setLayoutTypePortlet(layoutTypePortlet);
			//themeDisplay.setPortletGroupId(portletGroupId);
			themeDisplay.setSignedIn(signedIn);
			//themeDisplay.setPermissionChecker(permissionChecker);
			themeDisplay.setLocale(locale);
			themeDisplay.setTimeZone(timeZone);
			themeDisplay.setLookAndFeel(contextPath, theme, colorScheme);
			themeDisplay.setServerPort(req.getServerPort());
			themeDisplay.setSecure(req.isSecure());
			themeDisplay.setResolution(resolution);
			themeDisplay.setStateExclusive(LiferayWindowState.isExclusive(req));
			themeDisplay.setStatePopUp(LiferayWindowState.isPopUp(req));
			themeDisplay.setPathApplet(contextPath + "/applets");
			themeDisplay.setPathCms(rootPath + "/cms");
			themeDisplay.setPathContext(contextPath);
			themeDisplay.setPathFlash(contextPath + "/flash");
			themeDisplay.setPathFriendlyURLPrivate(friendlyURLPrivatePath);
			themeDisplay.setPathFriendlyURLPublic(friendlyURLPublicPath);
			themeDisplay.setPathImage(imagePath);
			themeDisplay.setPathJavaScript(contextPath + "/html/js");
			themeDisplay.setPathMain(mainPath);
			themeDisplay.setPathRoot(rootPath);
			themeDisplay.setPathSound(contextPath + "/html/sound");

			// URLs

			themeDisplay.setShowAddContentIcon(false);
			themeDisplay.setShowHomeIcon(true);
			themeDisplay.setShowMyAccountIcon(signedIn);
			themeDisplay.setShowPageSettingsIcon(false);
			themeDisplay.setShowPortalIcon(true);
			themeDisplay.setShowSignInIcon(!signedIn);
			themeDisplay.setShowSignOutIcon(signedIn);

			/*PortletURL createAccountURL = new PortletURLImpl(
				req, PortletKeys.MY_ACCOUNT, plid, true);

			createAccountURL.setWindowState(WindowState.MAXIMIZED);
			createAccountURL.setPortletMode(PortletMode.VIEW);

			createAccountURL.setParameter(
				"struts_action", "/my_account/create_account");

			themeDisplay.setURLCreateAccount(createAccountURL);
			*/
			themeDisplay.setURLHome(protocol + company.getHomeURL());

			/*if (layout != null) {
				if (layout.getType().equals(LayoutImpl.TYPE_PORTLET)) {
					boolean hasUpdateLayoutPermission =
						LayoutPermission.contains(
							permissionChecker, layout, ActionKeys.UPDATE);

					if (hasUpdateLayoutPermission) {
						themeDisplay.setShowAddContentIcon(true);
						themeDisplay.setShowLayoutTemplatesIcon(true);

						themeDisplay.setURLAddContent(
							"LayoutConfiguration.toggle('" + plid + "', '" +
								PortletKeys.LAYOUT_CONFIGURATION + "', '" +
									doAsUserId + "');");

						themeDisplay.setURLLayoutTemplates(
							"showLayoutTemplates();");
					}
				}

				boolean hasManageLayoutsPermission =
					GroupPermission.contains(
						permissionChecker, portletGroupId,
						ActionKeys.MANAGE_LAYOUTS);

				if (hasManageLayoutsPermission) {
					themeDisplay.setShowPageSettingsIcon(true);

					PortletURL pageSettingsURL = new PortletURLImpl(
						req, PortletKeys.LAYOUT_MANAGEMENT, plid, false);

					pageSettingsURL.setWindowState(WindowState.MAXIMIZED);
					pageSettingsURL.setPortletMode(PortletMode.VIEW);

					pageSettingsURL.setParameter(
						"struts_action", "/layout_management/edit_pages");

					if (layout.isPrivateLayout()) {
						pageSettingsURL.setParameter("tabs2", "private");
					}
					else {
						pageSettingsURL.setParameter("tabs2", "public");
					}

					pageSettingsURL.setParameter("groupId", portletGroupId);
					pageSettingsURL.setParameter("selPlid", plid);

					themeDisplay.setURLPageSettings(pageSettingsURL);
				}
				
				PortletURL myAccountURL = new PortletURLImpl(
					req, PortletKeys.MY_ACCOUNT, plid, false);

				myAccountURL.setWindowState(WindowState.MAXIMIZED);
				myAccountURL.setPortletMode(PortletMode.VIEW);

				myAccountURL.setParameter(
					"struts_action", "/my_account/edit_user");

				themeDisplay.setURLMyAccount(myAccountURL);
				
			}*/

			boolean termsOfUseRequired = GetterUtil.getBoolean(
				PropsUtil.get(PropsUtil.TERMS_OF_USE_REQUIRED), true);

			if (!user.isActive() ||
				(termsOfUseRequired && !user.isAgreedToTermsOfUse())) {

				themeDisplay.setShowAddContentIcon(false);
				themeDisplay.setShowMyAccountIcon(false);
				themeDisplay.setShowPageSettingsIcon(false);
			}

			themeDisplay.setURLPortal(protocol + company.getPortalURL());
			themeDisplay.setURLSignIn(mainPath + "/portal/login");
			themeDisplay.setURLSignOut(mainPath + "/portal/logout");

			req.setAttribute(WebKeys.THEME_DISPLAY, themeDisplay);

			// Parallel render

			req.setAttribute(
				WebKeys.PORTLET_PARALLEL_RENDER,
				new Boolean(ParamUtil.getBoolean(req, "p_p_parallel", true)));
				
			
    }
    private static Object[] getDefaultLayout(
			HttpServletRequest req, User user, boolean signedIn)
		throws PortalException, SystemException {

		// Check the virtual host

		LayoutSet layoutSet = null;

		String host = PortalUtil.getHost(req);

		try {
			if (isValidHost(user.getActualCompanyId(), host)) {
				layoutSet = LayoutSetLocalServiceUtil.getLayoutSet(
					user.getActualCompanyId(), host);

				List layouts = LayoutLocalServiceUtil.getLayouts(
					layoutSet.getOwnerId(),
					LayoutImpl.DEFAULT_PARENT_LAYOUT_ID);

				if (layouts.size() > 0) {
					Layout layout = (Layout)layouts.get(0);

					return new Object[] {layout, layouts};
				}
			}
		}
		catch (NoSuchLayoutSetException nslse) {
		}

		Layout layout = null;
		List layouts = null;

		if (signedIn) {

			// Check the user's personal layouts

			Group userGroup = user.getGroup();

			layouts = LayoutLocalServiceUtil.getLayouts(
				LayoutImpl.PRIVATE + userGroup.getGroupId(),
				LayoutImpl.DEFAULT_PARENT_LAYOUT_ID);

			if (layouts.size() == 0) {
				layouts = LayoutLocalServiceUtil.getLayouts(
					LayoutImpl.PUBLIC + userGroup.getGroupId(),
					LayoutImpl.DEFAULT_PARENT_LAYOUT_ID);
			}

			if (layouts.size() > 0) {
				layout = (Layout)layouts.get(0);
			}

			// Check the user's communities

			if (layout == null) {
				LinkedHashMap groupParams = new LinkedHashMap();

				groupParams.put("usersGroups", user.getUserId());

				List groups = GroupLocalServiceUtil.search(
					user.getCompanyId(), null, null, groupParams,
					QueryUtil.ALL_POS, QueryUtil.ALL_POS);

				for (int i = 0; i < groups.size(); i++) {
					Group group = (Group)groups.get(i);

					layouts = LayoutLocalServiceUtil.getLayouts(
						LayoutImpl.PRIVATE + group.getGroupId(),
						LayoutImpl.DEFAULT_PARENT_LAYOUT_ID);

					if (layouts.size() == 0) {
						layouts = LayoutLocalServiceUtil.getLayouts(
							LayoutImpl.PUBLIC + group.getGroupId(),
							LayoutImpl.DEFAULT_PARENT_LAYOUT_ID);
					}

					if (layouts.size() > 0) {
						layout = (Layout)layouts.get(0);

						break;
					}
				}
			}
		}
		else {

			// Check the guest community

			Group guestGroup = GroupLocalServiceUtil.getGroup(
				user.getActualCompanyId(), GroupImpl.GUEST);

			layouts = LayoutLocalServiceUtil.getLayouts(
				LayoutImpl.PUBLIC + guestGroup.getGroupId(),
				LayoutImpl.DEFAULT_PARENT_LAYOUT_ID);

			if (layouts.size() > 0) {
				layout = (Layout)layouts.get(0);
			}
		}

		return new Object[] {layout, layouts};
	}

	private static Object[] getViewableLayouts(
			Layout layout, List layouts, PermissionChecker permissionChecker,
			HttpServletRequest req)
		throws PortalException, SystemException {

		if ((layouts != null) && (layouts.size() > 0)) {
			boolean replaceLayout = true;

			if (LayoutPermission.contains(
					permissionChecker, layout, ActionKeys.VIEW)) {

				replaceLayout = false;
			}

			List accessibleLayouts = new ArrayList();

			for (int i = 0; i < layouts.size(); i++) {
				Layout curLayout = (Layout)layouts.get(i);

				if (LayoutPermission.contains(
						permissionChecker, curLayout, ActionKeys.VIEW)) {

					if ((accessibleLayouts.size() == 0) && replaceLayout) {
						layout = curLayout;
					}

					accessibleLayouts.add(curLayout);
				}
			}

			if (accessibleLayouts.size() == 0) {
				layouts = null;

				SessionErrors.add(
					req, LayoutPermissionException.class.getName());
			}
			else {
				layouts = accessibleLayouts;
			}
		}

		return new Object[] {layout, layouts};
	}

	private static boolean isValidHost(String companyId, String host)
		throws PortalException, SystemException {

		if (Validator.isNotNull(host)) {
			Company company = CompanyLocalServiceUtil.getCompany(companyId);

			if (company.getPortalURL().indexOf(host) == -1) {
				return true;
			}
		}

		return false;
	}

	private static boolean isViewableCommunity(User user, String ownerId)
		throws PortalException, SystemException {

		// Public layouts are always viewable

		if (!LayoutImpl.isPrivateLayout(ownerId)) {
			return true;
		}

		// Users can only see their own private layouts

		String groupId = LayoutImpl.getGroupId(ownerId);

		Group group = GroupLocalServiceUtil.getGroup(groupId);

		if (group.isUser()) {
			if (group.getClassPK().equals(user.getUserId())) {
				return true;
			}
			else {
				return false;
			}
		}

		// Authenticated users can only see group layouts if they belong to the
		// group

		if (GroupLocalServiceUtil.hasUserGroup(user.getUserId(), groupId)) {
			return true;
		}
		else {
			return false;
		}
	}
    
    public static ThemeDisplay getDefaultThemeDisplay() throws Exception{
    	if( defaultThemeDisplay!=null) return defaultThemeDisplay;

		ThemeDisplay themeDisplay =new ThemeDisplay();
		String contextPath="";
		String companyId= "liferay.com";
		Theme theme = ThemeLocalUtil.getTheme(companyId, ThemeImpl.getDefaultThemeId());
		ColorScheme	colorScheme = ThemeLocalUtil.getColorScheme(companyId, theme.getThemeId(),
				ColorSchemeImpl.getDefaultColorSchemeId());
			
		themeDisplay.setLookAndFeel(contextPath, theme, colorScheme);
		
/*		themeDisplay.setCompany(company);
		themeDisplay.setCompanyLogo(companyLogo);
		themeDisplay.setUser(user);
		themeDisplay.setRealUser(realUser);
		themeDisplay.setDoAsUserId(doAsUserId);
		themeDisplay.setLayout(layout);
		themeDisplay.setLayouts(layouts);
		themeDisplay.setPlid(plid);
		themeDisplay.setLayoutTypePortlet(layoutTypePortlet);
		themeDisplay.setPortletGroupId(portletGroupId);
		themeDisplay.setSignedIn(signedIn);
		themeDisplay.setPermissionChecker(permissionChecker);
		themeDisplay.setLocale(locale);
		themeDisplay.setTimeZone(timeZone);
		themeDisplay.setServerPort(req.getServerPort());
		themeDisplay.setSecure(req.isSecure());
		themeDisplay.setResolution(resolution);
		themeDisplay.setStateExclusive(LiferayWindowState.isExclusive(req));
		themeDisplay.setStatePopUp(LiferayWindowState.isPopUp(req));
		themeDisplay.setPathApplet(contextPath + "/applets");
		themeDisplay.setPathCms(rootPath + "/cms");
		themeDisplay.setPathContext(contextPath);
		themeDisplay.setPathFlash(contextPath + "/flash");
		themeDisplay.setPathFriendlyURLPrivate(friendlyURLPrivatePath);
		themeDisplay.setPathFriendlyURLPublic(friendlyURLPublicPath);
*/		
		
		themeDisplay.setPathImage("/image");
		themeDisplay.setPathJavaScript( "/html/js");
		themeDisplay.setPathMain("/c");
		themeDisplay.setPathRoot("");
		themeDisplay.setPathSound(contextPath + "/html/sound");

		defaultThemeDisplay=themeDisplay;
    	return defaultThemeDisplay;
    }
    public static ColorScheme getDefaultColorScheme()throws Exception{
    	return com.liferay.portal.service.impl.ThemeLocalUtil.getColorScheme("liferay.com","classic", "01");
    }
    /**
     * Return default color scheme currently, will allow user to select scheme in later implementation
     * @param userName
     * @param clientDomain
     * @return
     * @throws Exception
     */
    public static ColorScheme getColorScheme(String userName, String clientDomain)throws Exception{
    	return com.liferay.portal.service.impl.ThemeLocalUtil.getColorScheme("liferay.com","classic", "01");
    	/*String skin=null;
    	Connection con= null;;
    	ResultSet rs=null;
    	PreparedStatement pstmt=null;
    	try{
    		con=nds.query.QueryEngine.getInstance().getConnection();
    		pstmt=con.prepareStatement(GET_USER_SKIN);
	    	pstmt.setString(1,userName +"@"+ clientDomain );
	    	rs=pstmt.executeQuery();
	    	if(rs.next()){
	    		skin= rs.getString(1);
	    	}
    	}finally{
    		if(rs!=null)try{rs.close();}catch(Exception e2){}
    		if(pstmt!=null)try{pstmt.close();}catch(Exception e2){}
    		if(con!=null)try{con.close();}catch(Exception e2){}
    	}
    	if(skin ==null)skin="30";// 30 is the orange, we select it as default
    	return ColorSchemeManagerUtil.getColorScheme(skin);*/
    	
    }
	/**
     * Get locale of the user
     * @param userId
     * @return
     * @throws Exception
     */
    public static Locale getLocale(String userName, String clientDomain)throws Exception{
    	String locale=null;
    	Locale loc;
    	Connection con= null;;
    	ResultSet rs=null;
    	PreparedStatement pstmt=null;
    	try{
    		con=nds.query.QueryEngine.getInstance().getConnection();
    		pstmt=con.prepareStatement(GET_USER_LOCALE);
	    	pstmt.setString(1,userName +"@"+ clientDomain );
	    	rs=pstmt.executeQuery();
	    	if(rs.next()){
	    		locale= rs.getString(1);
	    	}
    	}/*catch(Exception e){
    		logger.error("Could not fetch user according to user id="+ userId, e);
    	}*/finally{
    		if(rs!=null)try{rs.close();}catch(Exception e2){}
    		if(pstmt!=null)try{pstmt.close();}catch(Exception e2){}
    		if(con!=null)try{con.close();}catch(Exception e2){}
    	}
    	if(locale !=null){
			int x = locale.indexOf("_");

			String language = locale.substring(0, x);
			String country = locale.substring(x + 1, locale.length());

    		loc= new Locale(language, country);
    	}
    	else loc= Locale.getDefault();
    	return loc;
    	
    }
}
