<%@ page language="java" import="java.util.*" pageEncoding="utf-8"%>
<%@ include file="/html/portal/init.jsp" %>
<%
com.liferay.portal.util.CookieKeys.addSupportCookie(response);
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=gb2312" />
<title>burgeon portal--伯俊软件</title>
<link href="/style-portal.css" rel="stylesheet" type="text/css" />
<SCRIPT type=text/javascript>
	function selectTag(showContent,selfObj){
	// 2???
	var tag = document.getElementById("tags").getElementsByTagName("li");
	var taglength = tag.length;
	for(i=0; i<taglength; i++){
		tag[i].className = "";
	}
	selfObj.parentNode.className = "selectTag";
	// 2???
	for(i=0; j=document.getElementById("tagContent"+i); i++){
		j.style.display = "none";
	}
	document.getElementById(showContent).style.display = "block";	
}
 function changevalidate()
    {
     document.getElementById("imag1").src="/servlets/vms?"+Math.random();
    }

function onReturn(event){
  if (!event) event = window.event;
  if (event && event.keyCode && event.keyCode == 13) submitForm();
}
function submitForm(){
	if(document.getElementById("login").value==""){ 
		alert("请输入会员用户名");
		return;
	}
	else if(document.getElementById("password1").value==""){
		alert("请输入密码");
		return;
	}
	else if(document.getElementById("verifyCode").value==""){
		alert("请输入验证码");
		return;
	}else if(document.getElementById("verifyCode").value.length!=4){
		alert("您的输入验证码的长度不对!");
		return;
	}
	document.fm1.submit();
	document.body.innerHTML=document.getElementById("progress").innerHTML;
}
</SCRIPT>
</head>

<body>
<div id="head-login">
<div id="logo-01">
<div class="logo-right"></div>
<div class="logo-left"><a href="/index.jsp"><img src="/images/left-logo.png" width="136" height="150" border="0" /></a></div>
  </div> 
</div>
<div id="login-main">
<div id="login-user">
     <c:if test="<%=true %>">
                <c:if test='<%= SessionMessages.contains(request, "user_added") %>'>
                  <%
				String emailAddress = (String)SessionMessages.get(request, "user_added");
				String password = (String)SessionMessages.get(request, "user_added_password");
				%>
                  <div class="portlet-msg-success">
                    <c:choose>
                      <c:when test="<%= Validator.isNull(password) %>"> <%= LanguageUtil.format(pageContext, "thank-you-for-creating-an-account-your-password-has-been-sent-to-x", emailAddress) %> </c:when>
                      <c:otherwise> <%= LanguageUtil.format(pageContext, "thank-you-for-creating-an-account-your-password-is-x", new Object[] {password, emailAddress}) %> </c:otherwise>
                    </c:choose>
                  </div>
                  <br />
                  <%
				%>
                </c:if>
                <c:if test="<%= SessionErrors.contains(request, AuthException.class.getName()) %>">
                  <div class="portlet-msg-error"> <%= LanguageUtil.get(pageContext, "error-username-or-password") %> </div>
                  <br />
                </c:if>
                <c:if test="<%= SessionErrors.contains(request, "VERIFY_CODE_ERROR") %>">
                  <div class="portlet-msg-error"> <%= LanguageUtil.get(pageContext, "error-verify-code") %> </div>
                  <br />
                </c:if>
                <c:if test="<%= SessionErrors.contains(request, CookieNotSupportedException.class.getName()) %>">
                  <div class="portlet-msg-error"> <%= LanguageUtil.get(pageContext, "authentication-failed-please-enable-browser-cookies") %> </div>
                  <br />
                </c:if>
                <c:if test="<%= SessionErrors.contains(request, NoSuchUserException.class.getName()) %>">
                  <div class="portlet-msg-error"> <%= LanguageUtil.get(pageContext, "error-username-or-password") %> </div>
                  <br />
                </c:if>
                <c:if test="<%= SessionErrors.contains(request, PrincipalException.class.getName()) %>">
                  <div class="portlet-msg-error"> <%= LanguageUtil.get(pageContext, "you-have-attempted-to-access-a-section-of-the-site-that-requires-authentication") %> <%= LanguageUtil.get(pageContext, "please-sign-in-to-continue") %> </div>
                  <br />
                </c:if>
                <c:if test='<%= SessionErrors.contains(request, UserEmailAddressException.class.getName()) %>'>
                  <div class="portlet-msg-error"> <%= LanguageUtil.get(pageContext, "error-username-or-password") %> </div>
                  <br />
                </c:if>
                <c:if test="<%= SessionErrors.contains(request, UserPasswordException.class.getName()) %>">
                  <div class="portlet-msg-error"> <%= LanguageUtil.get(pageContext, "error-username-or-password") %> </div>
                  <br />
                </c:if>
              </c:if>
<form action="/c/portal/login" method="post" name="fm1" id="fm1">
      <input type="hidden" value="already-registered" name="cmd"/>
       <input type="hidden" value="already-registered" name="tabs1"/>
	<%
		  String  login ="";
		   if(company==null)
		    {
           company = com.liferay.portal.service.CompanyLocalServiceUtil.getCompany("liferay.com");
		    }
		 login =LoginAction.getLogin(request, "login", company);
		%>
<div id="login-U">
<ul>
<li>
<div class="left_text">用户名：</div>
<div class="right_text"><input id="login" name="login" type="text" class="Warning-130" size="23" value="<%=login %>" /></div>
</li>
<div class="clear"></div>
<li>
<div class="left_text">密&nbsp;&nbsp;&nbsp;&nbsp;码：</div>
<div class="right_text"><input id="password1" name="<%= SessionParameters.get(request, "password")%>" type="password" value=""  size="10" class="Warning-130"/></div>
</li>
<div class="clear"></div>
<li>
<div class="left_text">验证码：</div>
<div class="right_text"><input id="verifyCode" name="verifyCode" type="text" onKeyPress="onReturn(event)" class="Warning-70"  size="10" />
<img src="/servlets/vms" width="64" height="16" align="absmiddle" id="chkimg" onclick="javascript:document.getElementById('chkimg').src='/servlets/vms?'+Math.random()" />
</div>
</li>
<div class="clear"></div>
<li>
<div class="right_text"><a href="#" onclick="javascript:submitForm()"><input type="image" src="/images/user-btn.gif" width="81" height="25" /></a>
</div>
</li>
</ul>
</div>
</form>
</div>
</div>
<div id="bottom">
<div id="bottom-bg">
<div id="bottom-left">
了解更多产品请点击：<br />
<a href="http://www.burgeon.com.cn" target="_parent" class="bottom-text">www.burgeon.com.cn</a></div>
<div id="bottom-right">
公司简介 | 联系我们 | 法律声明 | 服务体系 | 伯俊论坛<br />
&copy;2008 上海伯俊软件科技有限公司 版权所有 保留所有权
</div>
</div>
</div>
<%@ include file="/inc_progress.jsp" %>
</body>
</html>
