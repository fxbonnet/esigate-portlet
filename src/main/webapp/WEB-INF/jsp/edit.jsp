<%@ page import="javax.portlet.*" %>

<%@ include file="/WEB-INF/jsp/header.jsp" %>

<script type="text/javascript">
			jQuery(function(){
				// Tabs
				jQuery('#tabs').tabs();				
			});
		</script>


<%
PortletURL portletURL = renderResponse.createRenderURL();
portletURL.setWindowState(WindowState.NORMAL);

%>

<div id="tabs">
	<ul>
		<li><a href="#tabs-1"><fmt:message key="general"/></a></li>
		<li><a href="#tabs-2"><fmt:message key="clipping"/></a></li>
		<li><a href="#tabs-3"><fmt:message key="cache"/></a></li>
		<li><a href="#tabs-4"><fmt:message key="css-rewrite"/></a></li>
	</ul>
	<br/>
	<portlet:actionURL var="editAction" portletMode="edit"/>
	<form action="${editAction}" method="post">
		<div id="tabs-1">
			<%@ include file="/WEB-INF/jsp/general.jspf" %>
		</div>
		<div id="tabs-2">
			<%@ include file="/WEB-INF/jsp/clipping.jspf" %>
		</div>
		<div id="tabs-3">
			<%@ include file="/WEB-INF/jsp/cache.jspf" %>
		</div>
		<div id="tabs-4">
			<%@ include file="/WEB-INF/jsp/css_rewrite.jspf" %>
		</div>
		<input type="submit" value="<fmt:message key="save" />"/>
	</form>
</div>