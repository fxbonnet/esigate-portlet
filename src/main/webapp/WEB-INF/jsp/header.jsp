<%@ page import="java.util.Locale" %>
<%@ page import="javax.portlet.RenderRequest" %>
<%@ page import="java.util.ResourceBundle" %>
<%@ page import="javax.portlet.PortletResponse" %>

<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/tld/fmt.tld" prefix="fmt" %>

<portlet:defineObjects />

<% 
	Locale locale = request.getLocale();
	RenderRequest pReq = (RenderRequest)request.getAttribute("javax.portlet.request");
	ResourceBundle rb = ResourceBundle.getBundle("content.Language", locale);
	PortletResponse pRsp = (PortletResponse) request.getAttribute("javax.portlet.response");
%>

<fmt:setBundle basename="content.Language"/>