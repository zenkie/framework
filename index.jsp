<%@ page language="java" import="java.util.*,nds.velocity.*" pageEncoding="utf-8"%>
<%@ include file="/html/nds/common/init.jsp" %> 
<%@ include file="/html/portal/init.jsp" %>
<%
  Configurations conf= (Configurations)WebUtils.getServletContextManager().getActor(nds.util.WebKeys.CONFIGURATIONS);
   String defaultClientWebDomain=conf.getProperty("webclient.default.webdomain");
   int defaultClientID= Tools.getInt(conf.getProperty("webclient.default.clientid"), 37);
  //  int clientId=userWeb.getAdClientId(); 
  //  String webDomain =userWeb.getClientDomain();
  //  java.net.URL url = new java.net.URL(request.getRequestURL().toString());
	 // String webDomain=url.getHost();
	 // int clientId=WebUtils.getAdClientId(webDomain);
   // WebClient myweb=new WebClient(clientId, "",webDomain,false);
    //在/list/data_list.jsp的97行 int adClientId=37
    WebClient myweb=new WebClient(37, "","burgeon",false);
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=gb2312" />
<title>Burgeon Portal - 伯俊软件</title>
<link href="/style-portal.css" rel="stylesheet" type="text/css" />
<SCRIPT type=text/javascript>
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
<div id="head">
  <div id="nav-01"><div class="nav-right"></div>
<div class="nav-left"></div>
  </div>
<ul class="hbox" id="ctas">
<li><img src="images/btn-left.gif" width="192" height="40" />&nbsp;&nbsp;&nbsp;&nbsp;<img src="images/btn-right.gif" width="192" height="40" />
</li>
</ul>
</div>
<div id="main">
<div id="content">
<div id="content-N">
<div id="content-title">
  <p>portal 标准版</p>
  </div>
<div id="content-text"><a href="#" class="P-text">适合中小型服装企业，零投入，低风险，快速上线，纯B/S架构省去安装步骤，Saas模式无需任何硬件投入。</a></div>
</div>
<div id="content-D">
<div id="content-title">
  <p>portal 订货会</p>
</div>
<div id="content-text"><a href="#" class="P-text">适合大型服装企业，帮助管理庞大的分销网络，生产直到终端零售业务流程全面掌控。</a></div>
</div>
<div id="content-P">
<div id="content-title">
<p>portal 企业版</p>
</div>
<div id="content-text"><a href="#" class="P-text">适合大型服装企业，帮助管理庞大的分销网络，生产直到终端零售业务流程全面掌控。大型服装企业ERP管理系统，企业数据整合利器，集OA/HR/CRM/DRP等众多功能模块于一身。</a></div>
</div>
</div>
<div id="main-content">
<div id="main-content-U">
<div id="main-content-UBG">
<form action="/c/portal/login" method="post" name="fm1">
     	   <input type="hidden" value="already-registered" name="cmd"/>
   <input type="hidden" value="already-registered" name="tabs1"/> 
<div id="content_left_login">
<ul>
	<c:choose>
	<c:when test="<%= (userWeb!=null&&!userWeb.isGuest()) %>">
		<li><div class="left_text"><%= LanguageUtil.get(pageContext, "current-user")%>:</div><div class="right_text"><%=userWeb.getUserDescription() %></div></li>
		<li><div class="left_textx"><%= LanguageUtil.get(pageContext, "enter-view") %>:<a href="/html/nds/portal/portal.jsp"><%= LanguageUtil.get(pageContext, "backmanager") %></a>
	,<%= LanguageUtil.get(pageContext, "or") %>:<a href="/c/portal/logout"><%= LanguageUtil.get(pageContext, "logout") %></a></div><div></div></li>
	</c:when>
	<c:otherwise>
	 <%
 	 String  login ="";
	 if(company==null){
          company = com.liferay.portal.service.CompanyLocalServiceUtil.getCompany("liferay.com");
	 }
     login =LoginAction.getLogin(request, "login", company);
	%> 
<li>
<div class="left_text">用户名：</div>
<div class="right_text"><input id="login" name="login" type="text" class="Warning-120" size="23" value="<%=login %>" /></div>
</li>
<div class="clear"></div>
<li>
<div class="left_text">密&nbsp;&nbsp;&nbsp;&nbsp;码：</div>
<div class="right_text"><input id="password1" name="<%= SessionParameters.get(request, "password")%>" type="password" value=""  size="10" class="Warning-120"/></div>
</li>
<div class="clear"></div>
<li>
<div class="left_text">验证码：</div>
<div class="right_text"><input id="verifyCode" name="verifyCode" type="text" onKeyPress="onReturn(event)" class="Warning-60"  size="8" />
<img src="/servlets/vms" width="64" height="16" align="absmiddle" id="chkimg" onclick="javascript:document.getElementById('chkimg').src='/servlets/vms?'+Math.random()" />
</div>
</li>
<div class="clear"></div>
<li>
<div class="right_text"><a href="#" onclick="javascript:submitForm()"><image src="/images/user-btn.gif" width="81" height="25" border="0" /></a>
</div>
</li>
</c:otherwise>
</c:choose> 
</ul>
</div>
</form>
</div>
<div id="main-content-U-contant"><img src="/images/contant.jpg" width="232" height="103" /></div>
</div>
<div id="main-content-N">
<div id="main-content-NBG">
<div id="content-N-left">
<div id="content-N-title">公司动态</div>
<div id="content-N-text">
<div class="width-left"><img src="images/news_pic02.jpg" width="112" height="82" /></div> 
<%
     List newslist= QueryEngine.getInstance().doQueryList("select id,subject,content from u_news where doctype='hotspot' and ad_client_id=37 and rownum=1");
      if(newslist.size()>0){
        Object contentobj=((List)newslist.get(0)).get(2);
     String content="";
     content=((java.sql.Clob)contentobj).getSubString(1, (int) ((java.sql.Clob)contentobj).length()); 
%>
<div class="width-right"><a href="#" class="width-text"><%=((List)newslist.get(0)).get(1)%></a></div>
<%}%>
</div>
<ul>
<%
	 List latest=myweb.getList("latest","latest");
	 for(int k=0;k<latest.size();k++){
%> 
<li><a href="<%=((List)latest.get(k)).get(0)%>" class="middle_list"><%=((List)latest.get(k)).get(1)%></a></li> 
<%}%>
</div>
<div id="content-N-right">
<div id="content-N-title">行业动态</div>
<div id="content-N-text">
<div class="width-left"><img src="/images/news_pic03.jpg" width="112" height="82" /></div>
<div class="width-right01"><a href="#" class="width-text">讨论SaaS数据安全居安思危还是因噎废食？</a></div>
</div>
<ul>
<%
	 List companyportal=myweb.getList("company","company");
	 for(int i=0;i<companyportal.size();i++){
	%>
<li><a href="<%=((List)companyportal.get(i)).get(0)%>" class="middle_list"><%=((List)companyportal.get(i)).get(1)%></a></li>
<%}%>
</ul>
</div>
</div>
</div>
<div id="main-content-A">
<div id="content_A_right">
<ul>
<li><img src="/images/anli-logo.gif" width="205" height="223" /></li>
</ul>
</div>
</div>
</div>
</div>
<div id="bottom">
<div id="bottom-bg">
<div id="bottom-left">
了解更多产品请点击：<br />
<a href="http://www.burgeon.com.cn" target="_parent" class="bottom-text">www.burgeon.com.cn</a></div>
<div id="bottom-right">
公司简介 | 联系我们 | 法律声明 | 服务体系 | 伯俊论坛<br />
&copy;2008-2009 上海伯俊软件科技有限公司 版权所有
</div>
</div>
</div>
<%@ include file="/inc_progress.jsp" %>
</body>
</html>
