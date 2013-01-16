package nds.control.web;

import nds.util.Configurations;
import nds.util.Tools;
import nds.util.Validator;

public class ConfigValues
{
	public static final String AUTH_SECRET = get("auth.secret");

	public static final int LISTEN_PORT = Tools.getInt(System.getProperty("http.port", "80"), 80);

	public static final String LICENSE_PATH = get("license.path");

	public static final boolean JAVASCRIPT_FAST_LOAD = get("javascript.fast.load", true);

	public static final boolean SECURITY_REMEMBER_ME = get("security.rememberme", false);

	public static final int SECURITY_REMEMBER_ME_MAX_AGE = get("security.rememberme.max.age", 365);

	public static final String THEME_DEFAULT = get("theme.default", "01");

	public static final String WIKI_HELP_PATH = get("wiki.help.path", "/nea/ext/help/index.jsp");

	public static final String HOME_XML = get("home.xml", "/nea/ext/home.xml.jsp");

	public static final boolean HOME_JSP = get("home.jsp", true);

	public static final boolean LIST_EDITABLE = get("list.editable", true);

	public static final String SSV_NAVIGATION = get("ssview.navigation");

	public static final String SSV_BOTTOM_FILE = get("ssview.bottom");

	public static final boolean PORTAL_SSVIEW = get("portal.ssview", false);

	public static final String WEB_ROOT = get("web.root");

	public static final String DIR_NEA_ROOT = get("dir.nea.root");

	public static final int CXTAB_DIMENSION_THRESHOLD = get("cxtab.dimension.threshold", 5);

	public static final boolean CXTAB_TYPE_HTML = get("cxtab.type.html", false);

	public static final boolean TABLE_ACTION_UNVOID = get("table.action.unvoid", true);

	public static final int OBJECT_TAB_STARTIDX = get("object.tab.startidx", 0);

	public static final int IMPORT_EXCEL_MAXSIZE = get("import.excel.maxsize", 1);

	public static final int CONTROLLER_COPY_MAX = get("controller.copy.max", 20);

	public static final int QUERY_DROPDOWN_MAX = get("query.dropdown.max", 20);

	public static final int QUERY_DYNAMICQUERY_MAX = get("query.dynamicquery.max", 7);

	public static final String SECURITY_PASSWORD_FILE = get("security.password.file");

	public static final boolean SCHEMA_MODIFY = get("schema.modify", true);

	public static final int REST_QUERY_MAX_RANGE = get("rest.query.max.range", 10000);

	public static final String LDAP_FACTORY_INITIAL = get("ldap.factory.initial", "com.sun.jndi.ldap.LdapCtxFactory");
	public static final String LDAP_BASE_PROVIDER_URL = get("ldap.base.provider.url", "ldap://localhost:10389");
	public static final String LDAP_BASE_DN = get("ldap.base.dn", "dc=lifecycle,dc=cn");
	public static final String LDAP_SECURITY_PRINCIPAL = get("ldap.security.principal", "uid");
	public static final String LDAP_SECURITY_CREDENTIALS = get("ldap.security.credentials", "secret");
	public static final String LDAP_REFERRAL = get("ldap.referral", "follow");
	public static final boolean LDAP_AUTH_ENABLED = get("ldap.auth.enabled", false);
	public static final boolean LDAP_AUTH_REQUIRED = get("ldap.auth.required", false);
	public static final int LDAP_PAGE_SIZE = get("ldap.page.size", 1000);
	public static final int LDAP_RANGE_SIZE = get("ldap.range.size", 1000);
	public static final String LDAP_AUTH_METHOD = get("ldap.auth.method", "bind");
	public static final String LDAP_AUTH_PASSWORD_ENCRYPTION_ALGORITHM = get("ldap.auth.password.encryption.algorithm", "");
	public static final String LDAP_AUTH_PASSWORD_ENCRYPTION_ALGORITHM_TYPES = get("ldap.auth.password.encryption.algorithm.types", "MD5,SHA");

	public static final String LDAP_AUTH_SEARCH_FILTER = get("ldap.auth.search.filter", "(mail=@email_address@)");
	public static final String LDAP_ATTRS_TRANSFORMER_IMPL = get("ldap.attrs.transformer.impl", "com.agilecontrol.nea.core.security.ldap.DefaultAttributesTransformer");
	public static final String LDAP_CONTACT_MAPPINGS = get("ldap.contact.mappings", "");
	public static final String LDAP_CONTACT_CUSTOM_MAPPINGS = get("ldap.contact.custom.mappings", "");
	public static final String LDAP_USER_DEFAULT_OBJECT_CLASSES = get("ldap.user.default.object.classes", "top,person,inetOrgPerson,organizationalPerson");
	public static final String LDAP_USER_MAPPINGS = get("ldap.user.mappings", "uuid");
	public static final String LDAP_USER_CUSTOM_MAPPINGS = get("ldap.user.custom.mappings", "");
	public static final String LDAP_GROUP_DEFAULT_OBJECT_CLASSES = get("ldap.group.default.object.classes", "top,groupOfUniqueNames");
	public static final String LDAP_GROUP_MAPPINGS = get("ldap.group.mappings", "groupName=cn\ndescription=description\nuser=uniqueMember");
	public static final boolean LDAP_IMPORT_ENABLED = get("ldap.import.enabled", false);
	public static final boolean LDAP_IMPORT_ON_STARTUP = get("ldap.import.on.startup", false);
	public static final int LDAP_IMPORT_INTERVAL = get("ldap.import.interval", 10);
	public static final String LDAP_IMPORT_USER_SEARCH_FILTER = get("ldap.import.user.search.filter", "(objectClass=inetOrgPerson)");
	public static final String LDAP_IMPORT_GROUP_SEARCH_FILTER = get("ldap.import.group.search.filter", "(objectClass=groupOfUniqueNames)");
	public static final String LDAP_IMPORT_METHOD = get("ldap.import.method", "user");
	public static final boolean LDAP_IMPORT_CREATE_ROLE_PER_GROUP = get("ldap.import.create.role.per.group", false);
	public static final boolean LDAP_EXPORT_ENABLED = get("ldap.export.enabled", true);
	public static final String LDAP_USERS_DN = get("ldap.users.dn", "ou=users,dc=example,dc=com");
	public static final String LDAP_GROUPS_DN = get("ldap.groups.dn", "ou=groups,dc=example,dc=com");
	public static final boolean LDAP_PASSWORD_POLICY_ENABLED = get("ldap.password.policy.enabled", false);
	public static final String LDAP_ERROR_PASSWORD_AGE = get("ldap.error.password.age", "age");
	public static final String LDAP_ERROR_PASSWORD_EXPIRED = get("ldap.error.password.expired", "expired");
	public static final String LDAP_ERROR_PASSWORD_HISTORY = get("ldap.error.password.history", "history");
	public static final String LDAP_ERROR_PASSWORD_NOT_CHANGEABLE = get("ldap.error.password.not.changeable", "not allowed to change");
	public static final String LDAP_ERROR_PASSWORD_SYNTAX = get("ldap.error.password.syntax", "syntax");
	public static final String LDAP_ERROR_PASSWORD_TRIVIAL = get("ldap.error.password.trivial", "trivial");
	public static final String LDAP_ERROR_USER_LOCKOUT = get("ldap.error.user.lockout", "retry limit");

	public static final boolean NTLM_AUTH_ENABLED = get("ntlm.auth.enabled", false);
	public static final String NTLM_AUTH_DOMAIN_CONTROLLER = get("ntlm.auth.domain.controller", "127.0.0.1");
	public static final String NTLM_AUTH_DOMAIN_CONTROLLER_NAME = get("ntlm.auth.domain.controller.name", "lifecycle");
	public static final String NTLM_AUTH_DOMAIN = get("ntlm.auth.domain", "lifecycle");
	public static final String NTLM_AUTH_SERVICE_ACCOUNT = get("ntlm.auth.service.account", "linkpoint@lifecycle.com");
	public static final String NTLM_AUTH_SERVICE_PASSWORD = get("ntlm.auth.service.password", "test");

	public static final String PASSWORDS_ENCRYPTION_ALGORITHM = get("passwords.encryption.algorithm", "SHA");

	public static final int REST_CLIENT_CONNTIMEOUT = get("rest.client.conntimeout", 120);
	public static final int REST_CLIENT_READTIMEOUT = get("rest.client.readtimeout", 120);

	public static final String LIFECYCLE_CA_USER = get("lifecycle.ca.user", "firsttime");
	public static final String LIFECYCLE_CA_PASSWORD = get("lifecycle.ca.password");

	public static final String APP_TABLEACTION_LISTENERS = get("app.tableaction.listeners");

	public static final boolean QUERY_FIRST_DATE_RANGE = get("query.first.date.range", true);

	public static String get(String paramString)
	{
		return paramString = getConfigurations().getProperty(paramString);
	}

	public static String get(String paramString1, String paramString2)
	{
		if (Validator.isNull(paramString1 = getConfigurations().getProperty(paramString1)))
			return paramString2;
		return paramString1;
	}

	public static boolean get(String paramString, boolean paramBoolean) {
		if (Validator.isNull(paramString = getConfigurations().getProperty(paramString)))
			return paramBoolean;
		try {
			return Boolean.parseBoolean(paramString); } catch (Throwable localThrowable) {
			}
		return paramBoolean;
	}

	public static int get(String paramString, int paramInt)
	{
		if (Validator.isNull(paramString = getConfigurations().getProperty(paramString)))
			return paramInt;
		try {
			return Integer.parseInt(paramString); } catch (Throwable localThrowable) {
			}
		return paramInt;
	}

	public static String[] getArray(String paramString)
	{
		if (Validator.isNull(paramString = getConfigurations().getProperty(paramString))) {
			return null;
		}
		return paramString.split(",");
	}

	public static int[] getIntArray(String paramString, int paramInt)
	{
		String[] par_arry=getArray(paramString);
		if ( par_arry == null)
			return null;
		int[] arrayOfInt = new int[par_arry.length];
		for (int i = 0; i < arrayOfInt.length; i++) arrayOfInt[i] = Tools.getInt(par_arry[i], paramInt);
		return arrayOfInt;
	}

	private static Configurations getConfigurations()
	{
		Configurations localConfigurations;
		return localConfigurations = (Configurations)WebUtils.getServletContextManager().getActor("nds.web.configs");
	}
}


