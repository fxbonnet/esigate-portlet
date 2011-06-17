<%@page import="javax.portlet.WindowState"%>
<%@page import="com.liferay.portal.security.permission.ActionKeys"%>
<%@page import="com.liferay.portal.util.PortletKeys"%>
<%@page import="com.liferay.portal.service.permission.PortletPermissionUtil"%>
<%@page import="javax.portlet.PortletURL"%>
<%@page import="com.liferay.portal.kernel.util.ParamUtil"%>

<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<%@ taglib uri="http://liferay.com/tld/ui" prefix="liferay-ui" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<%@ taglib uri="http://liferay.com/tld/security" prefix="liferay-security" %>
<%@ taglib uri="http://liferay.com/tld/theme" prefix="liferay-theme" %>

<portlet:defineObjects />
<liferay-theme:defineObjects />

<%
long groupId = scopeGroupId;
String name = portletDisplay.getRootPortletId();
String primKey = portletDisplay.getResourcePK();
String actionId = "ADD_ENTRY";
%>


<%
String tabs1 = ParamUtil.getString(request, "tabs1", "general");

PortletURL tabs1URL = renderResponse.createRenderURL();
tabs1URL.setWindowState(WindowState.NORMAL);
tabs1URL.setParameter("tabs1", tabs1);

String tabNames = "general,clipping,cache,css-rewrite";
%>

<liferay-ui:tabs
   names="<%= tabNames %>"
   url="<%= tabs1URL.toString() %>"
/>







