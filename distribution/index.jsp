<%@ page language="java"  pageEncoding="utf-8"%>
<%@ include file="/html/nds/common/init.jsp" %>
<%@ page import="org.json.*" %>
<%@ page import="java.util.Date" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="nds.control.util.*" %>
<%@ page import="nds.web.config.*" %>
<%@ taglib uri="http://liferay.com/tld/ui" prefix="liferay-ui" %>
<%@ taglib uri="http://java.fckeditor.net" prefix="FCK" %>
<%
    response.setHeader("Pragma", "No-cache");
    response.setHeader("Cache-Control", "no-cache");
    response.setDateHeader("Expires", 0);
    String idS=request.getParameter("id");
    int id=-1;
    if (idS != null) {
        id=Integer.parseInt(idS);
    }
    if(userWeb==null || userWeb.getUserId()==userWeb.GUEST_ID){
        /*session.invalidate();
        com.liferay.util.servlet.SessionErrors.add(request,PrincipalException.class.getName());
        response.sendRedirect("/login.jsp");*/
        response.sendRedirect("/c/portal/login");
        return;
    }
    if(!userWeb.isActive()){
        session.invalidate();
        com.liferay.util.servlet.SessionErrors.add(request,"USER_NOT_ACTIVE");
        response.sendRedirect("/login.jsp");
        return;
    }
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <title>配货单</title>


    <link rel="Shortcut Icon" href="/html/nds/images/portal.ico" />
    <script language="javascript" src="/html/nds/js/top_css_ext.js"></script>
    <script language="javascript" language="javascript1.5" src="/html/nds/js/ieemu.js"></script>
    <script language="javascript" src="/html/nds/js/cb2.js"></script>
    <script language="javascript" src="/html/nds/js/xp_progress.js"></script>
    <script language="javascript" src="/html/nds/js/helptip.js"></script>
    <script language="javascript" src="/html/nds/js/common.js"></script>
    <script language="javascript" src="/html/nds/js/print.js"></script>
    <script language="javascript" src="/html/nds/js/prototype.js"></script>
    <script language="javascript" src="/html/nds/js/jquery1.2.3/jquery.js"></script>
    <script language="javascript" src="/html/nds/js/jquery1.2.3/hover_intent.js"></script>
    <script language="javascript" src="/html/nds/js/jquery1.2.3/ui.tabs.js"></script>
    <script>
        jQuery.noConflict();
    </script>
    <script language="javascript" src="/html/js/sniffer.js"></script>
    <script language="javascript" src="/html/js/ajax.js"></script>
    <script language="javascript" src="/html/js/util.js"></script>
    <script language="javascript" src="/html/js/portal.js"></script>
    <script language="javascript" src="/html/nds/js/objdropmenu.js"></script>
    <script language="javascript" src="/html/nds/js/formkey.js"></script>
    <script type="text/javascript" src="/html/nds/js/selectableelements.js"></script>
    <script type="text/javascript" src="/html/nds/js/selectabletablerows.js"></script>
    <script language="javascript" src="/html/js/dragdrop/coordinates.js"></script>
    <script language="javascript" src="/html/js/dragdrop/drag.js"></script>
    <script language="javascript" src="/html/js/dragdrop/dragdrop.js"></script>
    <script language="javascript" src="/html/nds/js/calendar.js"></script>
    <script type="text/javascript" src="/html/nds/js/dwr.Controller.js"></script>
    <script type="text/javascript" src="/html/nds/js/dwr.engine.js"></script>
    <script type="text/javascript" src="/html/nds/js/dwr.util.js"></script>
    <script language="javascript" src="/html/nds/js/application.js"></script>
    <script language="javascript" src="/html/nds/js/alerts.js"></script>
    <script language="javascript" src="/html/nds/js/dw_scroller.js"></script>
    <script type="text/javascript" src="/html/nds/js/init_object_query_zh_CN.js"></script>
    <script language="javascript" src="/html/nds/js/init_objcontrol_zh_CN.js"></script>
    <script language="javascript" src="/html/nds/js/obj_ext.js"></script>
    <script language="javascript" src="/html/nds/js/gridcontrol.js"></script>
    <script type="text/javascript" src="/html/nds/js/object_query.js"></script>
    <script language="javascript" src="/distribution/distribution.js"></script>
    <link typ e="text/css" rel="stylesheet" href="/html/nds/css/nds_header.css">
    <link type="text/css" rel="stylesheet" href="/html/nds/themes/classic/01/css/header_aio_min.css">
    <link href="ph.css" rel="stylesheet" type="text/css" />
</head>
<% if(id!=-1){ %>
<script language="javascript">
    jQuery(document).ready(function(){dist.reShow();});
</script>
<%}%>
<body class="body-bg" >
<input type="hidden" id="load_model" value="metrix"/>
<input type="hidden" id="load_type" value="<%=id==-1?"load":"reload"%>"/>
<input type="hidden" id="showStyle" value="list">
<input type="hidden" id="orderStatus" value="1"/>
<input type="hidden" id="isChanged" value="false"/>
<iframe id="CalFrame" name="CalFrame" frameborder=0 src=/html/nds/common/calendar.jsp style="display:none;position:absolute; z-index:9999"></iframe>
<div id="ph-btn">
	<div id="ph-from-btn">
                <input type="hidden" id="fund_balance" value="<%=id!=-1?id:""%>"/>
                <input type="image" name="imageField" src="images/ph-btn-zj.gif"  onclick="dist.showObject('fund_balance.jsp',710,250)"/>
                <input type="image" name="imageField2" src="images/ph-btn-ph.gif" onclick="dist.showObject('auto_dist.jsp',600,450)" />
                <input type="image" name="imageField3" src="images/ph-btn-bc.gif" onclick="dist.saveDate('sav')"/>
                <input type="image" name="imageField4" src="images/ph-btn-dj.gif" onclick="dist.saveDate('ord')"/>
                <input type="image" name="imageField4" src="images/ph-btn-xz.gif" onclick="window.location='http://192.168.1.102:90/distribution/index.jsp?&&fixedcolumns=&id=-1';"/>
            </div>
</div>
<div id="ph-container"> 
<table width="100%" border="0" cellspacing="0" cellpadding="0">
  <tr>
    <td colspan="2" class="ph-td-bg"><div id="ph-serach">
        <div id="ph-serach-title">
            <div id="ph-serach-img">
                <img src="images/btn-mx.gif" style="cursor:pointer" width="72" height="18" onclick="$('Details').style.display='';$('Documents').style.display='none'" />
                &nbsp;&nbsp;
                <img src="images/btn-djh.gif" style="cursor:pointer" width="72" height="18" onclick="$('Details').style.display='none';$('Documents').style.display=''" />
            </div>
        </div>
        <div id="ph-serach-bg">
        <div id="Details" class="obj">
            <table width="900" border="0" cellspacing="1" cellpadding="0" class="obj" align="center">
                <tr>
                    <td class="ph-desc" width="80" valign="top" nowrap="" align="right"><div class="desc-txt">订单类型<font color="red">*</font>：</div></td>
                    <td class="ph-value" width="180" valign="top" nowrap="" align="left"><select id="column_26991" class="objsl" tabindex="1" name="doctype">
                        <option value="0">--请选择--</option>
                        <option value="FWD">期货订单</option>
                        <option value="INS">现货订单</option>
                        <option selected="selected" value="ALL">全部</option>
                    </select></td>
                    <!--发货店仓-->
                    <td class="ph-desc" width="100" valign="top" nowrap="" align="right"><div class="desc-txt">发货店仓<font color="red">*</font>：</div></td>
                    <td class="ph-value" width="180" valign="top" nowrap="" align="left"><input name="c_orig_id__name" readonly="" type="text" class="ipt-4-2"  id="column_26992"  value="" />
                        <input type="hidden" id="fk_column_26992" name="C_ORIG_ID" value="">
                  <span  class="coolButton" id="cbt_26992" onaction="oq.toggle('/html/nds/query/search.jsp?table=14610&return_type=s&column=26992&accepter_id=column_26992&qdata='+encodeURIComponent(document.getElementById('column_26992').value)+'&queryindex='+encodeURIComponent(document.getElementById('queryindex_-1').value),'column_26992')">
                    <img width="16" height="16" border="0" align="absmiddle" title="Find" src="images/find.gif"/>
                  </span>
                        <script type="text/javascript" >createButton(document.getElementById("cbt_26992"));</script>
                    </td>
                    <!--收货店仓-->
                    <td class="ph-desc" width="100" valign="top" nowrap="" align="right"><div class="desc-txt">收货店仓<font color="red">*</font>：</div></td>
                    <td class="ph-value" width="180" valign="top" nowrap="" align="left">
                        <input type='hidden' id='column_26993' name="column_26993" value=''>
                        <input name="" readonly type="text" class="ipt-4-2" id='column_26993_fd' value="" >
        <span  class="coolButton" id="column_26993_link" title=popup onaction="oq.toggle_m('/html/nds/query/search.jsp?table=C_V_STORE2&return_type=f&accepter_id=column_26993', 'column_26993');">
                <img id='column_26993_img' width="16" height="16" border="0" align="absmiddle" title="Find" src="images/filterobj.gif"/>
        </span>
                        <script type="text/javascript" >createButton(document.getElementById('column_26993_link'));</script>
                    </td>
                   <!--
                    <td class="ph-value" width="80" valign="top" nowrap="" align="left"><input id="switchModel" type="button" value="切换为矩阵模式" style="font-size:14px;" onclick="dist.switchModel();"></td>
                    -->
                </tr>
                <tr>
                    <!--选择款号-->
                    <td class="ph-desc" width="80" valign="top" nowrap="" align="right"><div class="desc-txt">选择款号<font color="red">*</font>：</div></td>
                    <td class="ph-value" width="180" valign="top" nowrap="" align="left">
                        <input type='hidden' id='column_26994' name="product_filter" value=''>
                        <input type="text" class="ipt-4-2"  readonly id='column_26994_fd' value="" />
        <span  class="coolButton" id="column_26994_link" title=popup onaction="oq.toggle_m('/html/nds/query/search.jsp?table='+'m_product'+'&return_type=f&accepter_id=column_26994', 'column_26994');">
            <img id='column_26994_img' width="16" height="16" border="0" align="absmiddle" title="Find" src="images/filterobj.gif"/>
        </span>
                        <script type="text/javascript" >createButton(document.getElementById('column_26994_link'));</script>
                    </td>
                    <!--起止时间-->
                    <%
                        Date tody=new Date();
                        SimpleDateFormat fmt=new SimpleDateFormat("yyyyMMdd");
                        String end=fmt.format(tody);
                        Long stL=tody.getTime()-24*60*60*1000*10l;
                        Date std=new Date(stL);
                        String st=fmt.format(std);
                    %>
                    <td class="ph-desc" width="100" valign="top" nowrap="" align="right"><div class="desc-txt"> 订单时间(起)<font color="red">*</font>：</div></td>
                    <td class="ph-value" width="180" valign="top" nowrap="" align="left">
                        <input type="text" class="ipt-4-2" name="billdatebeg"  tabIndex="5" maxlength="10" size="20" title="8位日期，如20070823" id="column_26995" value="<%=st%>" />
        <span  class="coolButton">
            <a onclick="event.cancelBubble=true;" href="javascript:showCalendar('imageCalendar23',false,'column_26995',null,null,true);"><img id="imageCalendar23" width="16" height="18" border="0" align="absmiddle" title="Find" src="images/datenum.gif"/> </a>
        </span>
                    </td>
                    <td class="ph-desc" width="100" valign="top" nowrap="" align="right"><div class="desc-txt">订单时间(止)<font color="red">*</font>：</div></td>
                    <td class="ph-value" width="180" valign="top" nowrap="" align="left">
                        <input name="billdateend" type="text"  class="ipt-4-2" maxlength="10" size="20" title="8位日期，如20070823" id="column_269966"  value="<%=end%>"/>
        <span  class="coolButton">
            <a onclick="event.cancelBubble=true;" href="javascript:showCalendar('imageCalendar144',false,'column_269966',null,null,true);">
                <img id='imageCalendar144' width="16" height="18" border="0" align="absmiddle" title="Find" src="images/datenum.gif"/>
            </a>
        </span>
                    </td>
                    <!--查询条件提交按钮-->
                    <td class="ph-value" width="80" valign="top" nowrap="" align="left"><%if(id==-1){%><input type="image" name="imageField5" src="images/btn-search01.gif" onclick="dist.queryObject()" /><%}%>
                    </td>
                </tr>
            </table>
        </div>
          <!--单据号查询表格--> 
           <div id="Documents" class="djh-table" style="display:none">
		   <table width="600" border="0" cellspacing="1" cellpadding="0" class="obj" align="center">
               <tr>
                    <td align="right" valign="top" nowrap="nowrap" class="ph-desc"><div class="desc-txt">单据号<font color="red">*</font>：</div></td>
                    <td class="ph-value" width="176" valign="top" nowrap="nowrap" align="left"><input name="Input2" type="text" class="ipt-4-2" id="column_26996_fd" />
                        <input type="hidden" id="column_26996" name="DOCUMENT_ID" value="">
                        <span id="column_26996_link" class="coolButton"onaction="oq.toggle_m('/html/nds/query/search.jsp?table=12943&return_type=f&accepter_id=column_26996','column_26996')">
                            <img id='column_26996_img' width="16" height="16" border="0" align="absmiddle" title="Find" src="images/filterobj.gif"/>
                        </span>
                        <script type="text/javascript" >createButton(document.getElementById('column_26996_link'));</script>
                    </td>
                    <td class="ph-value" width="362" valign="top" nowrap="nowrap" align="left">
                            <%if(id==-1){%><input type="image" name="imageField5" src="images/btn-search01.gif" onclick="dist.queryObject('doc')" /><%}%></td>
               </tr>
         </table>
           </div>
    </div>
</div></td>
  </tr>
  <tr>
    <td colspan="2"><div class="ph-height"></div></td>
  </tr>
  <tr>
    <td colspan="2" bgcolor="#e6edf1"><div id="ph-pic">
    <div id="ph-pic-img">
        <div id="ph-pic-img-width">
            <div id="ph-pic-img-border"><img id="pdt-img" width="120" height="100" /></div>
            <div id="ph-pic-img-txt"></div>
        </div></div>
    <div id="ph-pic-left">
        <div id="ph-pic-txt">
            <ul>
                <li>
                    <div class="left">可&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;配：</div>
                    <div class="right" id="tot-can"></div>
                </li>
                <li>
                    <div class="left">未&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;配：</div>
                    <div class="right" id="tot-rem"></div>
                </li>
                <li>
                    <div class="left-red">当前已配：</div>
                    <div class="right-red" id="tot-ready"></div>
                </li>
            </ul>
        </div>
    </div>
    <div id="ph-pic-left01">
        <div id="ph-pic-txt">
            <ul>
                <li>
                    <div class="left">可&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;配：</div>
                    <div class="right" id="input-5"></div>
                </li>
                <li>
                    <div class="left">未&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;配：</div>
                    <div class="right" id="input-4"></div>
                </li>
                <li>
                    <div class="left-red">当前已配：</div>
                    <div class="right-red" id="input-2"></div>
                </li>
            </ul>
        </div>
    </div>
    <div id="ph-pic-right">
        <div id="ph-pic-txt">
            <ul>
                <li>
                    <div class="left">未&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;配：</div>
                    <div class="right" id="rs"></div>
                </li>
                <li>
                    <div class="left-red">当前可配：</div>
                    <div class="right-red" id="input-1"></div>
                </li>
            </ul>
        </div>
    </div>
</div></td>
  </tr>
  <tr>
    <td colspan="2"><div class="ph-height"></div></td>
  </tr>
  <tr>
    <td width="1%" valign="top">
    <div id="ph-from-left">
        <div id="ph-from-left-bg">
            <div class="left-search">
                <div><input name="textfield" type="text" class="left-search-input" id="pdt-search" onkeyup="dist.pdt_search()" /></div>
            </div>
            <div id="left-section-height"></div>
            <div id="left-section">
                <ul id="category_manu"></ul>
            </div>
        </div>
    </div></td>
    <td width="99%" valign="top" align="left"><div class="ph-from-right"><div id="ph-from-right-border">
        <div id="ph-from-right-b">
                <div id="forTableTitle">
                </div>
                <div id="ph-from-right-table"></div>
                <div style="height:17px"></div>
            </div>
         
    </div></div>
</td>
  </tr>
   <tr>
   	<td colspan="2"><div id="ph-footer">
    <div id="ph-footer-bg"></div>
    <div id="ph-footer-txt">&copy;2008 上海伯俊软件科技有限公司 版权所有 保留所有权 | 商标 | 隐私权声明 </div>
</div></td>
   	</tr>
</table>
</div></form>
<div id="submitImge" style="left:30px;top:80px;z-index:111;position:absolute;display:none;">
    <img src="/html/nds/images/submitted.gif"/>
</div>
<div id="obj-bottom">
    <iframe id="print_iframe" name="print_iframe" width="1" height="1" src="/html/common/null.html"></iframe>
</div>
<input type='hidden' name='queryindex_-1' id='queryindex_-1' value="-1" />
<table><tr><td>
    <script>
        jQuery(document).ready(function(){dcq.createdynlist([])});
        var ti=setInterval("dcq.dynquery();",500);
    </script>
</td></tr></table>
</body>
</html>
