package nds.web;

import com.liferay.portal.model.User;

import java.util.Locale;

import javax.servlet.jsp.PageContext;
import com.liferay.portal.language.*;

public class LangUtil {
	public static String get(User user, String key)  {
		try{
			return (LanguageUtil.get(user, key));
		}catch(LanguageException e){
			return "?"+key;
		}
	}

	public static String get(String companyId, Locale locale, String key)
		 {
		try{
		return (
			LanguageUtil.get(companyId, locale, key));
	}catch(LanguageException e){
		return "?"+key;
	}
	}

	public static String get(PageContext pageContext, String key)
		 {
		try{
		return (LanguageUtil.get(pageContext, key));
		}catch(LanguageException e){
			return "?"+key;
		}
	}

	public static String format(
			PageContext pageContext, String pattern, Object argument)
		 {
		try{
		return (LanguageUtil.format(
			pageContext, pattern, argument));
		}catch(LanguageException e){
			return "?"+argument;
		}
	}

	public static String format(
			PageContext pageContext, String pattern, Object argument,
			boolean translateArguments)
		 {
		try{
		return (LanguageUtil.format(
			pageContext, pattern, argument, translateArguments));
		}catch(LanguageException e){
			return "?"+argument;
		}		
	}

	public static String format(
			PageContext pageContext, String pattern, Object[] arguments)
		 {
		try{
		return (LanguageUtil.format(
			pageContext, pattern, arguments));
		}catch(LanguageException e){
			return "?"+arguments;
		}		
	}

	public static String format(
			PageContext pageContext, String pattern, Object[] arguments,
			boolean translateArguments)
		 {
		try{
		return (LanguageUtil.format(
			pageContext, pattern, arguments, translateArguments));
		}catch(LanguageException e){
			return "?"+arguments;
		}			
	}

	public static String format(
			PageContext pageContext, String pattern, LanguageWrapper argument)
		 {
		try{
		return (LanguageUtil.format(
			pageContext, pattern, argument));
		}catch(LanguageException e){
			return "?"+argument;
		}			
	}

	public static String format(
			PageContext pageContext, String pattern, LanguageWrapper argument,
			boolean translateArguments)
		 {
		try{
		return (LanguageUtil.format(
			pageContext, pattern, argument, translateArguments));
		}catch(LanguageException e){
			return "?"+argument;
		}
	}

	public static String format(
			PageContext pageContext, String pattern,
			LanguageWrapper[] arguments)
		 {
		try{
		return (LanguageUtil.format(
			pageContext, pattern, arguments));
		}catch(LanguageException e){
			return "?"+arguments;
		}		
	}

	public static String format(
			PageContext pageContext, String pattern,
			LanguageWrapper[] arguments, boolean translateArguments)
		 {
		try{
		return (LanguageUtil.format(
			pageContext, pattern, arguments, translateArguments));
		}catch(LanguageException e){
			return "?"+arguments;
		}		
	}

	public static String getTimeDescription(
			PageContext pageContext, Long milliseconds)
		 {
		try{
		return (LanguageUtil.getTimeDescription(
			pageContext, milliseconds));
		}catch(LanguageException e){
			return "?"+milliseconds;
		}		
	}

	public static String getTimeDescription(
			PageContext pageContext, long milliseconds)
		 {
		try{
		return (LanguageUtil.getTimeDescription(
			pageContext, milliseconds));
		}catch(LanguageException e){
			return "?"+milliseconds;
		}		
	}
}
