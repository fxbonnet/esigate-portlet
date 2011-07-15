<%@page import="javax.portlet.WindowState"%>
<%@page import="javax.portlet.PortletMode"%>
<%@page import="javax.portlet.PortletURL"%>

<%@ include file="/WEB-INF/jsp/header.jsp" %>

<%
PortletURL editURL = renderResponse.createRenderURL();
editURL.setPortletMode(PortletMode.EDIT);
%>

<div class="portlet-msg-info">
	<a href="<%= editURL.toString() %>">
		<fmt:message key="please-configure-this-portlet-to-make-it-visible-to-all-users" />
	</a>
</div>