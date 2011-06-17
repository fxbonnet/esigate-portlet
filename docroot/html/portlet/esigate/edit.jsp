<%@ page import="com.liferay.portal.kernel.util.ParamUtil" %>
<%@ page import="javax.portlet.*" %>

<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<%@ taglib uri="http://liferay.com/tld/util" prefix="liferay-util" %>
<%@ taglib uri="http://liferay.com/tld/ui" prefix="liferay-ui" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://liferay.com/tld/theme" prefix="liferay-theme" %>

<liferay-theme:defineObjects />
<portlet:defineObjects />

<%
themeDisplay.setIncludeServiceJs(false);
String tabs1 = ParamUtil.getString(request, "tabs1", "general");

PortletURL portletURL = renderResponse.createRenderURL();
portletURL.setWindowState(WindowState.NORMAL);
portletURL.setParameter("tabs1", tabs1);

%>
<liferay-util:include page="/html/portlet/esigate/tabs1.jsp" servletContext="<%=pageContext.getServletContext() %>"/>

<b><font size="+2"><liferay-ui:message key='<%=tabs1 +"-caption"%>' /></font></b><br/><br/>
<portlet:actionURL var="editAction" portletMode="edit"/>

<form action="${editAction}" method="post">
	<c:choose>
		<c:when test='<%= tabs1.equals("general")%>'>
			<%@ include file="/html/portlet/esigate/general.jspf" %>
		</c:when>
		<c:when test='<%= "clipping".equals(tabs1) %>'>
	  		<%@ include file="/html/portlet/esigate/clipping.jspf" %>	
		</c:when>
		<c:when test='<%= "cache".equals(tabs1) %>'>
	  		<%@ include file="/html/portlet/esigate/cache.jspf" %>	
		</c:when>
		<c:otherwise>
		  	<%@ include file="/html/portlet/esigate/css_rewrite.jspf" %>
		</c:otherwise>		
	</c:choose>
	<br/>
	<input type="hidden" name="<portlet:namespace/>tabs1" value="<%=tabs1%>" />
	<input type="submit" value="<liferay-ui:message key="save" />"/>
</form>
