<%@page import="javax.portlet.WindowState"%>
<%@page import="javax.portlet.PortletMode"%>
<%@page import="com.liferay.portal.util.PortalUtil"%>
<%@page import="javax.portlet.PortletURL"%>

<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<%@ taglib uri="http://liferay.com/tld/ui" prefix="liferay-ui" %>

<portlet:defineObjects />
<%
String currentURL = PortalUtil.getCurrentURL(request);

PortletURL editURL = renderResponse.createRenderURL();
editURL.setPortletMode(PortletMode.EDIT);
%>

<div class="portlet-msg-info">
	<a href="<%= editURL.toString() %>">
		<liferay-ui:message key="please-configure-this-portlet-to-make-it-visible-to-all-users" />
	</a>
</div>