/**
 * 支持Inactive , 支持OTP
 */
package com.liferay.portal.service.impl;

import com.liferay.mail.service.MailServiceUtil;
import com.liferay.portal.ContactBirthdayException;
import com.liferay.portal.ContactFirstNameException;
import com.liferay.portal.ContactLastNameException;
import com.liferay.portal.DuplicateUserEmailAddressException;
import com.liferay.portal.DuplicateUserIdException;
import com.liferay.portal.NoSuchContactException;
import com.liferay.portal.NoSuchGroupException;
import com.liferay.portal.NoSuchRoleException;
import com.liferay.portal.NoSuchUserException;
import com.liferay.portal.NoSuchUserGroupException;
import com.liferay.portal.OrganizationParentException;
import com.liferay.portal.PortalException;
import com.liferay.portal.RequiredUserException;
import com.liferay.portal.ReservedUserEmailAddressException;
import com.liferay.portal.ReservedUserIdException;
import com.liferay.portal.SystemException;
import com.liferay.portal.UserEmailAddressException;
import com.liferay.portal.UserIdException;
import com.liferay.portal.UserPasswordException;
import com.liferay.portal.UserPortraitException;
import com.liferay.portal.UserSmsException;
import com.liferay.portal.kernel.mail.MailMessage;
import com.liferay.portal.kernel.util.Base64;
import com.liferay.portal.kernel.util.KeyValuePair;
import com.liferay.portal.kernel.util.OrderByComparator;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.language.LanguageException;
import com.liferay.portal.language.LanguageUtil;
import com.liferay.portal.model.Company;
import com.liferay.portal.model.Contact;
import com.liferay.portal.model.Group;
import com.liferay.portal.model.Organization;
import com.liferay.portal.model.Role;
import com.liferay.portal.model.User;
import com.liferay.portal.model.UserGroup;
import com.liferay.portal.model.impl.ContactImpl;
import com.liferay.portal.model.impl.ResourceImpl;
import com.liferay.portal.model.impl.UserImpl;
import com.liferay.portal.security.auth.AuthPipeline;
import com.liferay.portal.security.auth.Authenticator;
import com.liferay.portal.security.auth.PrincipalException;
import com.liferay.portal.security.auth.PrincipalFinder;
import com.liferay.portal.security.auth.UserIdGenerator;
import com.liferay.portal.security.auth.UserIdValidator;
import com.liferay.portal.security.pwd.PwdEncryptor;
import com.liferay.portal.security.pwd.PwdToolkitUtil;
import com.liferay.portal.service.ContactLocalServiceUtil;
import com.liferay.portal.service.GroupLocalServiceUtil;
import com.liferay.portal.service.PasswordTrackerLocalServiceUtil;
import com.liferay.portal.service.ResourceLocalServiceUtil;
import com.liferay.portal.service.UserIdMapperLocalServiceUtil;
import com.liferay.portal.service.UserLocalService;
import com.liferay.portal.service.persistence.CompanyUtil;
import com.liferay.portal.service.persistence.ContactUtil;
import com.liferay.portal.service.persistence.GroupFinder;
import com.liferay.portal.service.persistence.GroupUtil;
import com.liferay.portal.service.persistence.OrganizationUtil;
import com.liferay.portal.service.persistence.PermissionUserFinder;
import com.liferay.portal.service.persistence.RoleFinder;
import com.liferay.portal.service.persistence.RoleUtil;
import com.liferay.portal.service.persistence.UserFinder;
import com.liferay.portal.service.persistence.UserGroupFinder;
import com.liferay.portal.service.persistence.UserGroupUtil;
import com.liferay.portal.service.persistence.UserUtil;
import com.liferay.portal.util.PortalInstances;
import com.liferay.portal.util.PortalUtil;
import com.liferay.portal.util.PrefsPropsUtil;
import com.liferay.portal.util.PropsUtil;
import com.liferay.portlet.documentlibrary.service.DLFileRankLocalServiceUtil;
import com.liferay.portlet.messageboards.service.MBMessageFlagLocalServiceUtil;
import com.liferay.portlet.messageboards.service.MBStatsUserLocalServiceUtil;
import com.liferay.portlet.shopping.service.ShoppingCartLocalServiceUtil;
import com.liferay.util.Encryptor;
import com.liferay.util.EncryptorException;
import com.liferay.util.GetterUtil;
import com.liferay.util.InstancePool;
import com.liferay.util.StringUtil;
import com.liferay.util.Time;
import com.liferay.util.Validator;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import java.rmi.RemoteException;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.mail.internet.InternetAddress;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nds.portal.auth.InactiveUserException;
import nds.portal.auth.UserManager;

/**
 * <a href="UserLocalServiceImpl.java.html"><b><i>View Source</i></b></a>
 *
 * @author  Brian Wing Shun Chan
 *
 */
public class UserLocalServiceImpl implements UserLocalService {

	public void addGroupUsers(String groupId, String[] userIds)
		throws PortalException, SystemException {

		GroupUtil.addUsers(groupId, userIds);
	}

	public void addRoleUsers(String roleId, String[] userIds)
		throws PortalException, SystemException {

		RoleUtil.addUsers(roleId, userIds);
	}

	public void addUserGroupUsers(String userGroupId, String[] userIds)
		throws PortalException, SystemException {

		UserGroupUtil.addUsers(userGroupId, userIds);
	}

	public User addUser(
			String creatorUserId, String companyId, boolean autoUserId,
			String userId, boolean autoPassword, String password1,
			String password2, boolean passwordReset, String emailAddress,
			Locale locale, String firstName, String middleName, String lastName,
			String nickName, String prefixId, String suffixId, boolean male,
			int birthdayMonth, int birthdayDay, int birthdayYear,
			String jobTitle, String organizationId, String locationId)
		throws PortalException, SystemException {

		return addUser(
			creatorUserId, companyId, autoUserId, userId, autoPassword,
			password1, password2, passwordReset, emailAddress, locale,
			firstName, middleName, lastName, nickName, prefixId, suffixId, male,
			birthdayMonth, birthdayDay, birthdayYear, jobTitle, organizationId,
			locationId, true);
	}

	public User addUser(
			String creatorUserId, String companyId, boolean autoUserId,
			String userId, boolean autoPassword, String password1,
			String password2, boolean passwordReset, String emailAddress,
			Locale locale, String firstName, String middleName, String lastName,
			String nickName, String prefixId, String suffixId, boolean male,
			int birthdayMonth, int birthdayDay, int birthdayYear,
			String jobTitle, String organizationId, String locationId,
			boolean sendEmail)
		throws PortalException, SystemException {

		// User

		userId = userId.trim().toLowerCase();
		emailAddress = emailAddress.trim().toLowerCase();
		Date now = new Date();

		boolean alwaysAutoUserId = GetterUtil.getBoolean(
			PropsUtil.get(PropsUtil.USERS_ID_ALWAYS_AUTOGENERATE));

		if (alwaysAutoUserId) {
			autoUserId = true;
		}

		validate(
			companyId, autoUserId, userId, autoPassword, password1, password2,
			emailAddress, firstName, lastName, organizationId, locationId);

		validateOrganizations(companyId, organizationId, locationId);

		if (autoUserId) {
			UserIdGenerator userIdGenerator = (UserIdGenerator)InstancePool.get(
				PropsUtil.get(PropsUtil.USERS_ID_GENERATOR));

			try {
				userId = userIdGenerator.generate(companyId);
			}
			catch (Exception e) {
				throw new SystemException(e);
			}
		}

		if (autoPassword) {
			password1 = PwdToolkitUtil.generate();
		}

		int passwordsLifespan = GetterUtil.getInteger(
			PropsUtil.get(PropsUtil.PASSWORDS_LIFESPAN));

		Date expirationDate = null;

		if (passwordsLifespan > 0) {
			expirationDate = new Date(
				System.currentTimeMillis() + Time.DAY * passwordsLifespan);
		}

		User defaultUser = getDefaultUser(companyId);

		String fullName = UserImpl.getFullName(firstName, middleName, lastName);

		String greeting = null;

		try {
			greeting =
				LanguageUtil.get(companyId, locale, "welcome") + ", " +
					fullName + "!";
		}
		catch (LanguageException le) {
			greeting = "Welcome, " + fullName + "!";
		}

		User user = UserUtil.create(userId);

		user.setCompanyId(companyId);
		user.setCreateDate(now);
		user.setModifiedDate(now);
		user.setPassword(PwdEncryptor.encrypt(password1));
		user.setPasswordUnencrypted(password1);
		user.setPasswordEncrypted(true);
		user.setPasswordExpirationDate(expirationDate);
		user.setPasswordReset(passwordReset);
		user.setEmailAddress(emailAddress);
		user.setLanguageId(locale.toString());
		user.setTimeZoneId(defaultUser.getTimeZoneId());
		user.setGreeting(greeting);
		user.setResolution(defaultUser.getResolution());
		user.setActive(true);

		UserUtil.update(user);

		// Resources

		String creatorUserName = StringPool.BLANK;

		if (Validator.isNull(creatorUserId)) {
			creatorUserId = user.getUserId();

			// Don't grab the full name from the User object because it doesn't
			// have a corresponding Contact object yet

			//creatorUserName = user.getFullName();
		}
		else {
			User creatorUser = UserUtil.findByPrimaryKey(creatorUserId);

			creatorUserName = creatorUser.getFullName();
		}

		ResourceLocalServiceUtil.addResources(
			companyId, null, creatorUserId, User.class.getName(),
			user.getPrimaryKey().toString(), false, false, false);

		// Mail

		if (user.hasCompanyMx()) {
			try {
				MailServiceUtil.addUser(
					userId, password1, firstName, middleName, lastName,
					emailAddress);
			}
			catch (RemoteException re) {
				throw new SystemException(re);
			}
		}

		// Contact

		Date birthday = PortalUtil.getDate(
			birthdayMonth, birthdayDay, birthdayYear,
			new ContactBirthdayException());

		String contactId = userId;

		Contact contact = ContactUtil.create(contactId);

		contact.setCompanyId(user.getCompanyId());
		contact.setUserId(creatorUserId);
		contact.setUserName(creatorUserName);
		contact.setCreateDate(now);
		contact.setModifiedDate(now);
		contact.setAccountId(user.getCompanyId());
		contact.setParentContactId(ContactImpl.DEFAULT_PARENT_CONTACT_ID);
		contact.setFirstName(firstName);
		contact.setMiddleName(middleName);
		contact.setLastName(lastName);
		contact.setNickName(nickName);
		contact.setPrefixId(prefixId);
		contact.setSuffixId(suffixId);
		contact.setMale(male);
		contact.setBirthday(birthday);
		contact.setJobTitle(jobTitle);

		ContactUtil.update(contact);

		// Organization and location

		UserUtil.clearOrganizations(userId);

		if (Validator.isNotNull(organizationId)) {
			UserUtil.addOrganization(userId, organizationId);
		}

		if (Validator.isNotNull(locationId)) {
			UserUtil.addOrganization(userId, locationId);
		}

		// Group

		GroupLocalServiceUtil.addGroup(
			user.getUserId(), User.class.getName(),
			user.getPrimaryKey().toString(), null, null, null, null);

		// Default groups

		List groups = new ArrayList();

		String[] defaultGroupNames = PrefsPropsUtil.getStringArray(
			companyId, PropsUtil.ADMIN_DEFAULT_GROUP_NAMES);

		for (int i = 0; i < defaultGroupNames.length; i++) {
			try {
				Group group = GroupFinder.findByC_N(
					companyId, defaultGroupNames[i]);

				groups.add(group);
			}
			catch (NoSuchGroupException nsge) {
			}
		}

		UserUtil.setGroups(userId, groups);

		// Default roles

		List roles = new ArrayList();

		String[] defaultRoleNames = PrefsPropsUtil.getStringArray(
			companyId, PropsUtil.ADMIN_DEFAULT_ROLE_NAMES);

		for (int i = 0; i < defaultRoleNames.length; i++) {
			try {
				Role role = RoleFinder.findByC_N(
					companyId, defaultRoleNames[i]);

				roles.add(role);
			}
			catch (NoSuchRoleException nsge) {
			}
		}

		UserUtil.setRoles(userId, roles);

		// Default user groups

		List userGroups = new ArrayList();

		String[] defaultUserGroupNames = PrefsPropsUtil.getStringArray(
			companyId, PropsUtil.ADMIN_DEFAULT_USER_GROUP_NAMES);

		for (int i = 0; i < defaultUserGroupNames.length; i++) {
			try {
				UserGroup userGroup = UserGroupFinder.findByC_N(
					companyId, defaultUserGroupNames[i]);

				userGroups.add(userGroup);
			}
			catch (NoSuchUserGroupException nsuge) {
			}
		}

		UserUtil.setUserGroups(userId, userGroups);

		// Email

		if (sendEmail) {
			sendEmail(user, password1);
		}

		return user;
	}

	public int authenticateByEmailAddress(
			String companyId, String emailAddress, String password,
			Map headerMap, Map parameterMap)
		throws PortalException, SystemException {

		return authenticate(
			companyId, emailAddress, password, true, headerMap, parameterMap);
	}

	public int authenticateByUserId(
			String companyId, String userId, String password, Map headerMap,
			Map parameterMap)
		throws PortalException, SystemException {

		return authenticate(
			companyId, userId, password, false, headerMap, parameterMap);
	}

	public boolean authenticateForJAAS(String userId, String encPwd)
		throws PortalException, SystemException {

		try {
			userId = userId.trim().toLowerCase();

			User user = UserUtil.findByPrimaryKey(userId);

			String password = user.getPassword();

			if (password.equals(encPwd)) {
				return true;
			}
			else if (!GetterUtil.getBoolean(PropsUtil.get(
						PropsUtil.PORTAL_JAAS_STRICT_PASSWORD))) {

				encPwd = Encryptor.digest(encPwd);

				if (password.equals(encPwd)) {
					return true;
				}
			}
		}
		catch (Exception e) {
			_log.error(e);
		}

		return false;
	}

	public KeyValuePair decryptUserId(
			String companyId, String userId, String password)
		throws PortalException, SystemException {

		Company company = CompanyUtil.findByPrimaryKey(companyId);

		try {
			userId = Encryptor.decrypt(company.getKeyObj(), userId);
		}
		catch (EncryptorException ee) {
			throw new SystemException(ee);
		}

		String liferayUserId = userId;

		try {
			PrincipalFinder principalFinder = (PrincipalFinder)InstancePool.get(
				PropsUtil.get(PropsUtil.PRINCIPAL_FINDER));

			liferayUserId = principalFinder.toLiferay(userId);
		}
		catch (Exception e) {
		}

		User user = UserUtil.findByPrimaryKey(liferayUserId);

		try {
			password = Encryptor.decrypt(company.getKeyObj(), password);
		}
		catch (EncryptorException ee) {
			throw new SystemException(ee);
		}

		String encPwd = PwdEncryptor.encrypt(password);

		if (user.getPassword().equals(encPwd)) {
			if (user.isPasswordExpired()) {
				user.setPasswordReset(true);

				UserUtil.update(user);
			}

			return new KeyValuePair(userId, password);
		}
		else {
			throw new PrincipalException();
		}
	}

	public void deleteRoleUser(String roleId, String userId)
		throws PortalException, SystemException {

		RoleUtil.removeUser(roleId, userId);
	}

	public void deleteUser(String userId)
		throws PortalException, SystemException {

		userId = userId.trim().toLowerCase();

		if (!GetterUtil.getBoolean(PropsUtil.get(PropsUtil.USERS_DELETE))) {
			throw new RequiredUserException();
		}

		User user = UserUtil.findByPrimaryKey(userId);

		// Group

		Group group = user.getGroup();

		GroupLocalServiceUtil.deleteGroup(group.getGroupId());

		// Portrait

		ImageLocalUtil.remove(userId);

		// Old passwords

		PasswordTrackerLocalServiceUtil.deletePasswordTrackers(userId);

		// External user ids

		UserIdMapperLocalServiceUtil.deleteUserIdMappers(userId);

		// Document library

		DLFileRankLocalServiceUtil.deleteFileRanks(userId);

		// Message boards

		MBMessageFlagLocalServiceUtil.deleteFlags(userId);
		MBStatsUserLocalServiceUtil.deleteStatsUserByUserId(userId);

		// Shopping cart

		ShoppingCartLocalServiceUtil.deleteUserCarts(userId);

		// Mail

		try {
			MailServiceUtil.deleteUser(userId);
		}
		catch (RemoteException re) {
			throw new SystemException(re);
		}

		// Contact

		ContactLocalServiceUtil.deleteContact(userId);

		// Resources

		ResourceLocalServiceUtil.deleteResource(
			user.getCompanyId(), User.class.getName(), ResourceImpl.TYPE_CLASS,
			ResourceImpl.SCOPE_INDIVIDUAL, user.getPrimaryKey().toString());

		// User

		UserUtil.remove(userId);
	}

	public String encryptUserId(String userId)
		throws PortalException, SystemException {

		userId = userId.trim().toLowerCase();

		String liferayUserId = userId;

		try {
			PrincipalFinder principalFinder = (PrincipalFinder)InstancePool.get(
				PropsUtil.get(PropsUtil.PRINCIPAL_FINDER));

			liferayUserId = principalFinder.toLiferay(userId);
		}
		catch (Exception e) {
		}

		User user = UserUtil.findByPrimaryKey(liferayUserId);

		Company company = CompanyUtil.findByPrimaryKey(user.getCompanyId());

		try {
			return Encryptor.encrypt(company.getKeyObj(), userId);
		}
		catch (EncryptorException ee) {
			throw new SystemException(ee);
		}
	}

	public User getDefaultUser(String companyId)
		throws PortalException, SystemException {

		return UserUtil.findByPrimaryKey(UserImpl.getDefaultUserId(companyId));
	}

	public List getGroupUsers(String groupId)
		throws PortalException, SystemException {

		return GroupUtil.getUsers(groupId);
	}

	public List getPermissionUsers(
			String companyId, String groupId, String name, String primKey,
			String actionId, String firstName, String middleName,
			String lastName, String emailAddress, boolean andOperator,
			int begin, int end)
		throws PortalException, SystemException {

		int orgGroupPermissionsCount =
			PermissionUserFinder.countByOrgGroupPermissions(
				companyId, name, primKey, actionId);

		if (orgGroupPermissionsCount > 0) {
			return PermissionUserFinder.findByUserAndOrgGroupPermission(
				companyId, name, primKey, actionId, firstName, middleName,
				lastName, emailAddress, andOperator, begin, end);
		}
		else {
			return PermissionUserFinder.findByPermissionAndRole(
				companyId, groupId, name, primKey, actionId, firstName,
				middleName, lastName, emailAddress, andOperator, begin, end);
		}
	}

	public int getPermissionUsersCount(
			String companyId, String groupId, String name, String primKey,
			String actionId, String firstName, String middleName,
			String lastName, String emailAddress, boolean andOperator)
		throws PortalException, SystemException {

		int orgGroupPermissionsCount =
			PermissionUserFinder.countByOrgGroupPermissions(
				companyId, name, primKey, actionId);

		if (orgGroupPermissionsCount > 0) {
			return PermissionUserFinder.countByUserAndOrgGroupPermission(
				companyId, name, primKey, actionId, firstName, middleName,
				lastName, emailAddress, andOperator);
		}
		else {
			return PermissionUserFinder.countByPermissionAndRole(
				companyId, groupId, name, primKey, actionId, firstName,
				middleName, lastName, emailAddress, andOperator);
		}
	}

	public List getRoleUsers(String roleId)
		throws PortalException, SystemException {

		return RoleUtil.getUsers(roleId);
	}

	public User getUserByEmailAddress(
			String companyId, String emailAddress)
		throws PortalException, SystemException {

		emailAddress = emailAddress.trim().toLowerCase();

		return UserUtil.findByC_EA(companyId, emailAddress);
	}

	public User getUserById(String userId)
		throws PortalException, SystemException {

		userId = userId.trim().toLowerCase();

		return UserUtil.findByPrimaryKey(userId);
	}

	public User getUserById(String companyId, String userId)
		throws PortalException, SystemException {

		userId = userId.trim().toLowerCase();

		return UserUtil.findByC_U(companyId, userId);
	}

	public String getUserId(String companyId, String emailAddress)
		throws PortalException, SystemException {

		emailAddress = emailAddress.trim().toLowerCase();

		User user = UserUtil.findByC_EA(companyId, emailAddress);

		return user.getUserId();
	}

	public boolean hasGroupUser(String groupId, String userId)
		throws PortalException, SystemException {

		return GroupUtil.containsUser(groupId, userId);
	}

	public boolean hasRoleUser(String roleId, String userId)
		throws PortalException, SystemException {

		return RoleUtil.containsUser(roleId, userId);
	}

	public boolean hasUserGroupUser(String userGroupId, String userId)
		throws PortalException, SystemException {

		return UserGroupUtil.containsUser(userGroupId, userId);
	}

	public List search(
			String companyId, String firstName, String middleName,
			String lastName, String emailAddress, boolean active,
			LinkedHashMap params, boolean andSearch, int begin, int end,
			OrderByComparator obc)
		throws SystemException {

		return UserFinder.findByC_FN_MN_LN_EA_A(
			companyId, firstName, middleName, lastName, emailAddress, active,
			params, andSearch, begin, end, obc);
	}

	public int searchCount(
			String companyId, String firstName, String middleName,
			String lastName, String emailAddress, boolean active,
			LinkedHashMap params, boolean andSearch)
		throws SystemException {

		return UserFinder.countByC_FN_MN_LN_EA_A(
			companyId, firstName, middleName, lastName, emailAddress, active,
			params, andSearch);
	}

	public void sendPassword(
			String companyId, String emailAddress, String remoteAddr,
			String remoteHost, String userAgent)
		throws PortalException, SystemException {

		if (!PrefsPropsUtil.getBoolean(
				companyId, PropsUtil.COMPANY_SECURITY_SEND_PASSWORD) ||
			!PrefsPropsUtil.getBoolean(
				companyId, PropsUtil.ADMIN_EMAIL_PASSWORD_SENT_ENABLED)) {

			return;
		}

		emailAddress = emailAddress.trim().toLowerCase();

		if (!Validator.isEmailAddress(emailAddress)) {
			throw new UserEmailAddressException();
		}

		Company company = CompanyUtil.findByPrimaryKey(companyId);

		User user = UserUtil.findByC_EA(companyId, emailAddress);

		/*if (user.hasCompanyMx()) {
			throw new SendPasswordException();
		}*/

		if (PwdEncryptor.PASSWORDS_ENCRYPTED) {
			user.setPassword(PwdToolkitUtil.generate());
			user.setPasswordEncrypted(false);
			user.setPasswordReset(GetterUtil.getBoolean(
				PropsUtil.get(PropsUtil.PASSWORDS_CHANGE_ON_FIRST_USE)));

			UserUtil.update(user);
		}

		try {
			String fromName = PrefsPropsUtil.getString(
				companyId, PropsUtil.ADMIN_EMAIL_FROM_NAME);
			String fromAddress = PrefsPropsUtil.getString(
				companyId, PropsUtil.ADMIN_EMAIL_FROM_ADDRESS);

			String toName = user.getFullName();
			String toAddress = user.getEmailAddress();

			String subject = PrefsPropsUtil.getContent(
				companyId, PropsUtil.ADMIN_EMAIL_PASSWORD_SENT_SUBJECT);
			String body = PrefsPropsUtil.getContent(
				companyId, PropsUtil.ADMIN_EMAIL_PASSWORD_SENT_BODY);

			subject = StringUtil.replace(
				subject,
				new String[] {
					"[$FROM_ADDRESS$]",
					"[$FROM_NAME$]",
					"[$PORTAL_URL$]",
					"[$REMOTE_ADDRESS$]",
					"[$REMOTE_HOST$]",
					"[$TO_ADDRESS$]",
					"[$TO_NAME$]",
					"[$USER_AGENT$]",
					"[$USER_ID$]",
					"[$USER_PASSWORD$]"
				},
				new String[] {
					fromAddress,
					fromName,
					company.getPortalURL(),
					remoteAddr,
					remoteHost,
					toAddress,
					toName,
					userAgent,
					user.getUserId(),
					user.getPassword()
				});

			body = StringUtil.replace(
				body,
				new String[] {
					"[$FROM_ADDRESS$]",
					"[$FROM_NAME$]",
					"[$PORTAL_URL$]",
					"[$REMOTE_ADDRESS$]",
					"[$REMOTE_HOST$]",
					"[$TO_ADDRESS$]",
					"[$TO_NAME$]",
					"[$USER_AGENT$]",
					"[$USER_ID$]",
					"[$USER_PASSWORD$]"
				},
				new String[] {
					fromAddress,
					fromName,
					company.getPortalURL(),
					remoteAddr,
					remoteHost,
					toAddress,
					toName,
					userAgent,
					user.getUserId(),
					user.getPassword()
				});

			InternetAddress from = new InternetAddress(fromAddress, fromName);

			InternetAddress to = new InternetAddress(toAddress, toName);

			MailMessage message = new MailMessage(
				from, to, subject, body, true);

			MailServiceUtil.sendEmail(message);
		}
		catch (IOException ioe) {
			throw new SystemException(ioe);
		}
	}

	public void setGroupUsers(String groupId, String[] userIds)
		throws PortalException, SystemException {

		GroupUtil.setUsers(groupId, userIds);
	}

	public void setRoleUsers(String roleId, String[] userIds)
		throws PortalException, SystemException {

		RoleUtil.setUsers(roleId, userIds);
	}

	public void setUserGroupUsers(String userGroupId, String[] userIds)
		throws PortalException, SystemException {

		UserGroupUtil.setUsers(userGroupId, userIds);
	}

	public void unsetGroupUsers(String groupId, String[] userIds)
		throws PortalException, SystemException {

		GroupUtil.removeUsers(groupId, userIds);
	}

	public void unsetRoleUsers(String roleId, String[] userIds)
		throws PortalException, SystemException {

		RoleUtil.removeUsers(roleId, userIds);
	}

	public void unsetUserGroupUsers(String userGroupId, String[] userIds)
		throws PortalException, SystemException {

		UserGroupUtil.removeUsers(userGroupId, userIds);
	}

	public User updateActive(String userId, boolean active)
		throws PortalException, SystemException {

		userId = userId.trim().toLowerCase();

		User user = UserUtil.findByPrimaryKey(userId);

		user.setActive(active);

		UserUtil.update(user);

		return user;
	}

	public User updateAgreedToTermsOfUse(
			String userId, boolean agreedToTermsOfUse)
		throws PortalException, SystemException {

		User user = UserUtil.findByPrimaryKey(userId);

		user.setAgreedToTermsOfUse(agreedToTermsOfUse);

		UserUtil.update(user);

		return user;
	}

	public User updateLastLogin(String userId, String loginIP)
		throws PortalException, SystemException {

		User user = UserUtil.findByPrimaryKey(userId);

		user.setLastLoginDate(user.getLoginDate());
		user.setLastLoginIP(user.getLoginIP());
		user.setLoginDate(new Date());
		user.setLoginIP(loginIP);
		user.setFailedLoginAttempts(0);

		UserUtil.update(user);

		return user;
	}

	public User updatePassword(
			String userId, String password1, String password2,
			boolean passwordReset)
		throws PortalException, SystemException {

		userId = userId.trim().toLowerCase();

		validatePassword(userId, password1, password2);

		User user = UserUtil.findByPrimaryKey(userId);

		String oldEncPwd = user.getPassword();

		if (!user.isPasswordEncrypted()) {
			oldEncPwd = PwdEncryptor.encrypt(user.getPassword());
		}

		String newEncPwd = PwdEncryptor.encrypt(password1);

		int passwordsLifespan = GetterUtil.getInteger(
			PropsUtil.get(PropsUtil.PASSWORDS_LIFESPAN));

		Date expirationDate = null;

		if (passwordsLifespan > 0) {
			expirationDate = new Date(
				System.currentTimeMillis() + Time.DAY * passwordsLifespan);
		}

		if (user.hasCompanyMx()) {
			try {
				MailServiceUtil.updatePassword(userId, password1);
			}
			catch (RemoteException re) {
				throw new SystemException(re);
			}
		}

		user.setPassword(newEncPwd);
		user.setPasswordUnencrypted(password1);
		user.setPasswordEncrypted(true);
		user.setPasswordExpirationDate(expirationDate);
		user.setPasswordReset(passwordReset);

		UserUtil.update(user);

		PasswordTrackerLocalServiceUtil.trackPassword(userId, oldEncPwd);

		return user;
	}

	public void updatePortrait(String userId, byte[] bytes)
		throws PortalException, SystemException {

		userId = userId.trim().toLowerCase();

		long imageMaxSize = GetterUtil.getLong(
			PropsUtil.get(PropsUtil.USERS_IMAGE_MAX_SIZE));

		if ((imageMaxSize > 0) &&
			((bytes == null) || (bytes.length > imageMaxSize))) {

			throw new UserPortraitException();
		}

		ImageLocalUtil.put(userId, bytes);
	}

	public User updateUser(
			String userId, String password, String emailAddress,
			String languageId, String timeZoneId, String greeting,
			String resolution, String comments, String firstName,
			String middleName, String lastName, String nickName,
			String prefixId, String suffixId, boolean male, int birthdayMonth,
			int birthdayDay, int birthdayYear, String smsSn, String aimSn,
			String icqSn, String jabberSn, String msnSn, String skypeSn,
			String ymSn, String jobTitle, String organizationId,
			String locationId)
		throws PortalException, SystemException {

		// User

		userId = userId.trim().toLowerCase();
		emailAddress = emailAddress.trim().toLowerCase();
		Date now = new Date();

		validate(userId, emailAddress, firstName, lastName, smsSn);

		User user = UserUtil.findByPrimaryKey(userId);

		validateOrganizations(user.getCompanyId(), organizationId, locationId);

		user.setModifiedDate(now);

		if (!emailAddress.equals(user.getEmailAddress())) {

			// test@test.com -> test@liferay.com

			try {
				if (!user.hasCompanyMx() && user.hasCompanyMx(emailAddress)) {
					MailServiceUtil.addUser(
						userId, password, firstName, middleName, lastName,
						emailAddress);
				}

				// test@liferay.com -> bob@liferay.com

				else if (user.hasCompanyMx() &&
						 user.hasCompanyMx(emailAddress)) {

					MailServiceUtil.updateEmailAddress(userId, emailAddress);
				}

				// test@liferay.com -> test@test.com

				else if (user.hasCompanyMx() &&
						 !user.hasCompanyMx(emailAddress)) {

					MailServiceUtil.deleteEmailAddress(userId);
				}
			}
			catch (RemoteException re) {
				throw new SystemException(re);
			}

			user.setEmailAddress(emailAddress);
		}

		user.setLanguageId(languageId);
		user.setTimeZoneId(timeZoneId);
		user.setGreeting(greeting);
		user.setResolution(resolution);
		user.setComments(comments);

		UserUtil.update(user);

		// Contact

		Date birthday = PortalUtil.getDate(
			birthdayMonth, birthdayDay, birthdayYear,
			new ContactBirthdayException());

		String contactId = userId;

		Contact contact = null;

		try {
			contact = ContactUtil.findByPrimaryKey(contactId);
		}
		catch (NoSuchContactException nsce) {
			contact = ContactUtil.create(contactId);

			contact.setCompanyId(user.getCompanyId());
			contact.setUserId(StringPool.BLANK);
			contact.setUserName(StringPool.BLANK);
			contact.setCreateDate(now);
			contact.setAccountId(user.getCompanyId());
			contact.setParentContactId(ContactImpl.DEFAULT_PARENT_CONTACT_ID);
		}

		contact.setModifiedDate(now);
		contact.setFirstName(firstName);
		contact.setMiddleName(middleName);
		contact.setLastName(lastName);
		contact.setNickName(nickName);
		contact.setPrefixId(prefixId);
		contact.setSuffixId(suffixId);
		contact.setMale(male);
		contact.setBirthday(birthday);
		contact.setSmsSn(smsSn);
		contact.setAimSn(aimSn);
		contact.setIcqSn(icqSn);
		contact.setJabberSn(jabberSn);
		contact.setMsnSn(msnSn);
		contact.setSkypeSn(skypeSn);
		contact.setYmSn(ymSn);
		contact.setJobTitle(jobTitle);

		ContactUtil.update(contact);

		// Organization and location

		UserUtil.clearOrganizations(userId);

		if (Validator.isNotNull(organizationId)) {
			UserUtil.addOrganization(userId, organizationId);
		}

		if (Validator.isNotNull(locationId)) {
			UserUtil.addOrganization(userId, locationId);
		}

		return user;
	}

	protected int authenticate(
			String companyId, String login, String password,
			boolean byEmailAddress, Map headerMap, Map parameterMap)
		throws PortalException, SystemException {

		login = login.trim().toLowerCase();

		if (byEmailAddress) {
			if (!Validator.isEmailAddress(login)) {
				throw new UserEmailAddressException();
			}
		}
		else {
			if (Validator.isNull(login)) {
				throw new UserIdException();
			}
		}

		if (Validator.isNull(password)) {
			throw new UserPasswordException(
				UserPasswordException.PASSWORD_INVALID);
		}

		int authResult = Authenticator.FAILURE;

		/** 
		 * yfzhu 将此段提前，以便在PortalAuth(pre)里进行密码校验
		 */
		//----------- 下面的代码本来在 $usercode 处 ------------
		User user = null;

		try {
			if (byEmailAddress) {
				user = UserUtil.findByC_EA(companyId, login);
			}
			else {
				user = UserUtil.findByC_U(companyId, login);
			}
		}
		catch (NoSuchUserException nsue) {
			return Authenticator.DNE;
		}

		if (!user.isPasswordEncrypted()) {
			user.setPassword(PwdEncryptor.encrypt(user.getPassword()));
			user.setPasswordEncrypted(true);
			user.setPasswordReset(GetterUtil.getBoolean(
				PropsUtil.get(PropsUtil.PASSWORDS_CHANGE_ON_FIRST_USE)));

			UserUtil.update(user);
		}
		else if (user.isPasswordExpired()) {
			user.setPasswordReset(true);

			UserUtil.update(user);
		}
//		 check active or not, can not put to authpipeline, since that can not display to ui
		java.util.Date activeDate = UserManager.getInstance().getActiveDate(user.getUserId());
		//logger.debug( user.getUserId()+" activedate is " + activeDate);
		
		if( activeDate!=null){
			if (activeDate.getTime()<System.currentTimeMillis()){
				// should activate the user now
				user.setFailedLoginAttempts(0);
				UserUtil.update(user);
				UserManager.getInstance().removeUser(login);
			}else{
				throw new InactiveUserException("@inactive-user-exception@", activeDate);
			}
		}
		//----------- 上面的代码本来在 $usercode 处 ------------
		
		if (byEmailAddress) {
			authResult = AuthPipeline.authenticateByEmailAddress(
				PropsUtil.getArray(PropsUtil.AUTH_PIPELINE_PRE), companyId,
				login, password, headerMap, parameterMap);
		}
		else {
			authResult = AuthPipeline.authenticateByUserId(
				PropsUtil.getArray(PropsUtil.AUTH_PIPELINE_PRE), companyId,
				login, password, headerMap, parameterMap);
		}

		/*
		 *------------ $usercode  
		 * 
		User user = null;

		try {
			if (byEmailAddress) {
				user = UserUtil.findByC_EA(companyId, login);
			}
			else {
				user = UserUtil.findByC_U(companyId, login);
			}
		}
		catch (NoSuchUserException nsue) {
			return Authenticator.DNE;
		}

		if (!user.isPasswordEncrypted()) {
			user.setPassword(PwdEncryptor.encrypt(user.getPassword()));
			user.setPasswordEncrypted(true);
			user.setPasswordReset(GetterUtil.getBoolean(
				PropsUtil.get(PropsUtil.PASSWORDS_CHANGE_ON_FIRST_USE)));

			UserUtil.update(user);
		}
		else if (user.isPasswordExpired()) {
			user.setPasswordReset(true);

			UserUtil.update(user);
		}*/

		if (authResult == Authenticator.SUCCESS) {
			if (GetterUtil.getBoolean(PropsUtil.get(
					PropsUtil.AUTH_PIPELINE_ENABLE_LIFERAY_CHECK))) {

				String encPwd = PwdEncryptor.encrypt(password);

				if (user.getPassword().equals(encPwd)) {
					authResult = Authenticator.SUCCESS;
				}
				else if (GetterUtil.getBoolean(PropsUtil.get(
							PropsUtil.AUTH_MAC_ALLOW))) {

					try {
						MessageDigest digester = MessageDigest.getInstance(
							PropsUtil.get(PropsUtil.AUTH_MAC_ALGORITHM));

						digester.update(login.getBytes("UTF8"));

						String shardKey =
							PropsUtil.get(PropsUtil.AUTH_MAC_SHARED_KEY);

						encPwd = Base64.encode(
							digester.digest(shardKey.getBytes("UTF8")));

						if (password.equals(encPwd)) {
							authResult = Authenticator.SUCCESS;
						}
						else {
							authResult = Authenticator.FAILURE;
						}
					}
					catch (NoSuchAlgorithmException nsae) {
						throw new SystemException(nsae);
					}
					catch (UnsupportedEncodingException uee) {
						throw new SystemException(uee);
					}
				}
				else {
					authResult = Authenticator.FAILURE;
				}
			}
		}

		if (authResult == Authenticator.SUCCESS) {
			if (byEmailAddress) {
				authResult = AuthPipeline.authenticateByEmailAddress(
					PropsUtil.getArray(PropsUtil.AUTH_PIPELINE_POST), companyId,
					login, password, headerMap, parameterMap);
			}
			else {
				authResult = AuthPipeline.authenticateByUserId(
					PropsUtil.getArray(PropsUtil.AUTH_PIPELINE_POST), companyId,
					login, password, headerMap, parameterMap);
			}
		}

		if (authResult == Authenticator.FAILURE) {
			try {
				if (byEmailAddress) {
					AuthPipeline.onFailureByEmailAddress(
						PropsUtil.getArray(PropsUtil.AUTH_FAILURE), companyId,
						login, headerMap, parameterMap);
				}
				else {
					AuthPipeline.onFailureByUserId(
						PropsUtil.getArray(PropsUtil.AUTH_FAILURE), companyId,
						login, headerMap, parameterMap);
				}

				int failedLoginAttempts = user.getFailedLoginAttempts();

				user.setFailedLoginAttempts(++failedLoginAttempts);

				UserUtil.update(user);

				int maxFailures = GetterUtil.getInteger(PropsUtil.get(
					PropsUtil.AUTH_MAX_FAILURES_LIMIT));

				if ((failedLoginAttempts >= maxFailures) &&
					(maxFailures != 0)) {

					if (byEmailAddress) {
						AuthPipeline.onMaxFailuresByEmailAddress(
							PropsUtil.getArray(
								PropsUtil.AUTH_MAX_FAILURES),
							companyId, login, headerMap, parameterMap);
					}
					else {
						AuthPipeline.onMaxFailuresByUserId(
							PropsUtil.getArray(
								PropsUtil.AUTH_MAX_FAILURES),
							companyId, login, headerMap, parameterMap);
					}
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}

		return authResult;
	}

	protected void sendEmail(User user, String password)
		throws PortalException, SystemException {

		if (!PrefsPropsUtil.getBoolean(
				user.getCompanyId(),
				PropsUtil.ADMIN_EMAIL_USER_ADDED_ENABLED)) {

			return;
		}

		try {
			String companyId = user.getCompanyId();

			Company company = CompanyUtil.findByPrimaryKey(companyId);

			String fromName = PrefsPropsUtil.getString(
				companyId, PropsUtil.ADMIN_EMAIL_FROM_NAME);
			String fromAddress = PrefsPropsUtil.getString(
				companyId, PropsUtil.ADMIN_EMAIL_FROM_ADDRESS);

			String toName = user.getFullName();
			String toAddress = user.getEmailAddress();

			String subject = PrefsPropsUtil.getContent(
				companyId, PropsUtil.ADMIN_EMAIL_USER_ADDED_SUBJECT);
			String body = PrefsPropsUtil.getContent(
				companyId, PropsUtil.ADMIN_EMAIL_USER_ADDED_BODY);

			subject = StringUtil.replace(
				subject,
				new String[] {
					"[$FROM_ADDRESS$]",
					"[$FROM_NAME$]",
					"[$PORTAL_URL$]",
					"[$TO_ADDRESS$]",
					"[$TO_NAME$]",
					"[$USER_ID$]",
					"[$USER_PASSWORD$]"
				},
				new String[] {
					fromAddress,
					fromName,
					company.getPortalURL(),
					toAddress,
					toName,
					user.getUserId(),
					password
				});

			body = StringUtil.replace(
				body,
				new String[] {
					"[$FROM_ADDRESS$]",
					"[$FROM_NAME$]",
					"[$PORTAL_URL$]",
					"[$TO_ADDRESS$]",
					"[$TO_NAME$]",
					"[$USER_ID$]",
					"[$USER_PASSWORD$]"
				},
				new String[] {
					fromAddress,
					fromName,
					company.getPortalURL(),
					toAddress,
					toName,
					user.getUserId(),
					password
				});

			InternetAddress from = new InternetAddress(fromAddress, fromName);

			InternetAddress to = new InternetAddress(toAddress, toName);

			MailMessage message = new MailMessage(
				from, to, subject, body, true);

			MailServiceUtil.sendEmail(message);
		}
		catch (IOException ioe) {
			throw new SystemException(ioe);
		}
	}

	protected void validate(
			String userId, String emailAddress, String firstName,
			String lastName, String smsSn)
		throws PortalException, SystemException {

		User user = UserUtil.findByPrimaryKey(userId);

		if (!Validator.isEmailAddress(emailAddress)) {
			throw new UserEmailAddressException();
		}
		else if (!UserImpl.isDefaultUser(userId)) {
			try {
				if (!user.getEmailAddress().equals(emailAddress)) {
					if (UserUtil.findByC_EA(
							user.getCompanyId(), emailAddress) != null) {

						throw new DuplicateUserEmailAddressException();
					}
				}
			}
			catch (NoSuchUserException nsue) {
			}

			String[] reservedEmailAddresses = PrefsPropsUtil.getStringArray(
				user.getCompanyId(), PropsUtil.ADMIN_RESERVED_EMAIL_ADDRESSES);

			for (int i = 0; i < reservedEmailAddresses.length; i++) {
				if (emailAddress.equalsIgnoreCase(reservedEmailAddresses[i])) {
					throw new ReservedUserEmailAddressException();
				}
			}
		}

		if (!UserImpl.isDefaultUser(userId)) {
			if (Validator.isNull(firstName)) {
				throw new ContactFirstNameException();
			}
			else if (Validator.isNull(lastName)) {
				throw new ContactLastNameException();
			}
		}

		if (Validator.isNotNull(smsSn) && !Validator.isEmailAddress(smsSn)) {
			throw new UserSmsException();
		}
	}

	protected void validate(
			String companyId, boolean autoUserId, String userId,
			boolean autoPassword, String password1, String password2,
			String emailAddress, String firstName, String lastName,
			String organizationId, String locationId)
		throws PortalException, SystemException {

		if (!autoUserId) {
			if (Validator.isNull(userId)) {
				throw new UserIdException();
			}

			UserIdValidator userIdValidator = (UserIdValidator)InstancePool.get(
				PropsUtil.get(PropsUtil.USERS_ID_VALIDATOR));

			if (userIdValidator != null) {
				if (!userIdValidator.validate(userId, companyId)) {
					throw new UserIdException();
				}
			}

			String[] anonymousNames = PrincipalSessionBean.ANONYMOUS_NAMES;

			for (int i = 0; i < anonymousNames.length; i++) {
				if (userId.equalsIgnoreCase(anonymousNames[i])) {
					throw new UserIdException();
				}
			}

			String[] companyIds = PortalInstances.getCompanyIds();

			for (int i = 0; i < companyIds.length; i++) {
				if (userId.indexOf(companyIds[i]) != -1) {
					throw new UserIdException();
				}
			}

			try {
				User user = UserUtil.findByPrimaryKey(userId);

				if (user != null) {
					throw new DuplicateUserIdException();
				}
			}
			catch (NoSuchUserException nsue) {
			}

			String[] reservedUserIds = PrefsPropsUtil.getStringArray(
				companyId, PropsUtil.ADMIN_RESERVED_USER_IDS);

			for (int i = 0; i < reservedUserIds.length; i++) {
				if (userId.equalsIgnoreCase(reservedUserIds[i])) {
					throw new ReservedUserIdException();
				}
			}
		}

		if (!autoPassword) {
			if (!password1.equals(password2)) {
				throw new UserPasswordException(
					UserPasswordException.PASSWORDS_DO_NOT_MATCH);
			}
			else if (!PwdToolkitUtil.validate(password1) ||
					 !PwdToolkitUtil.validate(password2)) {

				throw new UserPasswordException(
					UserPasswordException.PASSWORD_INVALID);
			}
		}

		if (!Validator.isEmailAddress(emailAddress)) {
			throw new UserEmailAddressException();
		}
		else {
			try {
				User user = UserUtil.findByC_EA(companyId, emailAddress);

				if (user != null) {
					throw new DuplicateUserEmailAddressException();
				}
			}
			catch (NoSuchUserException nsue) {
			}

			String[] reservedEmailAddresses = PrefsPropsUtil.getStringArray(
				companyId, PropsUtil.ADMIN_RESERVED_EMAIL_ADDRESSES);

			for (int i = 0; i < reservedEmailAddresses.length; i++) {
				if (emailAddress.equalsIgnoreCase(reservedEmailAddresses[i])) {
					throw new ReservedUserEmailAddressException();
				}
			}
		}

		if (Validator.isNull(firstName)) {
			throw new ContactFirstNameException();
		}
		else if (Validator.isNull(lastName)) {
			throw new ContactLastNameException();
		}
	}

	protected void validateOrganizations(
			String companyId, String organizationId, String locationId)
		throws PortalException, SystemException {

		boolean organizationRequired = GetterUtil.getBoolean(PropsUtil.get(
			PropsUtil.ORGANIZATIONS_PARENT_ORGANIZATION_REQUIRED));

		boolean locationRequired = GetterUtil.getBoolean(PropsUtil.get(
			PropsUtil.ORGANIZATIONS_LOCATION_REQUIRED));

		if (locationRequired) {
			organizationRequired = true;
		}

		Organization organization = null;

		if (organizationRequired || Validator.isNotNull(organizationId)) {
			organization = OrganizationUtil.findByPrimaryKey(organizationId);
		}

		Organization location = null;

		if (locationRequired || Validator.isNotNull(locationId)) {
			location = OrganizationUtil.findByPrimaryKey(locationId);
		}

		if ((organization != null) && (location != null)) {
			if (!location.getParentOrganizationId().equals(
					organization.getOrganizationId())) {

				throw new OrganizationParentException();
			}
		}
	}

	protected void validatePassword(
			String userId, String password1, String password2)
		throws PortalException, SystemException {

		if (!password1.equals(password2)) {
			throw new UserPasswordException(
				UserPasswordException.PASSWORDS_DO_NOT_MATCH);
		}
		else if (!PwdToolkitUtil.validate(password1) ||
				 !PwdToolkitUtil.validate(password2)) {

			throw new UserPasswordException(
				UserPasswordException.PASSWORD_INVALID);
		}
		else if (!PasswordTrackerLocalServiceUtil.isValidPassword(
					userId, password1)) {

			throw new UserPasswordException(
				UserPasswordException.PASSWORD_ALREADY_USED);
		}
	}

	private static Log _log = LogFactory.getLog(UserLocalServiceImpl.class);

}