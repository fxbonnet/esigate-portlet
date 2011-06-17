package fr.smile.portlet;

import java.io.IOException;
import java.net.URL;
import java.util.Properties;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.GenericPortlet;
import javax.portlet.PortletContext;
import javax.portlet.PortletException;
import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequest;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.PortletURL;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.webassembletool.DriverFactory;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.theme.ThemeDisplay;
import com.liferay.portal.util.PortalUtil;
import com.liferay.portal.util.WebKeys;

/**
 * Class for ESIgate portlet.
 * 
 * @author tatyanaho
 *
 */
public class ESIgatePortlet extends GenericPortlet {
	private static final Log _log = LogFactoryUtil.getLog(ESIgatePortlet.class);
	/**
	 * name for edit page parameter.
	 */
	private static final String EDIT_PAGE_PARAM = "edit-jsp";
	/**
	 * name for help page parameter.
	 */
	private static final String HELP_PAGE_PARAM = "help-jsp";

	/**
	 * {@inheritDoc}
	 */
	public void doEdit(RenderRequest request, RenderResponse response)
			throws PortletException, IOException {

		PortletContext context = getPortletContext();
		setRequestAttributes(request);
		PortletRequestDispatcher rd = context
				.getRequestDispatcher(getInitParameter(EDIT_PAGE_PARAM));
		rd.include(request, response);
	}

	/**
	 * {@inheritDoc}
	 */
	public void doHelp(RenderRequest request, RenderResponse response)
			throws PortletException, IOException {

		PortletContext context = getPortletContext();
		setRequestAttributes(request);
		PortletRequestDispatcher rd = context
				.getRequestDispatcher(getInitParameter(HELP_PAGE_PARAM));
		rd.include(request, response);
	}

	/**
	 * {@inheritDoc}
	 */
	public void doView(RenderRequest request, RenderResponse response)
			throws PortletException, IOException {
		// Get portlet preferences
		PortletPreferences preferences = request.getPreferences();
		// Get url from request parameters.
		String remoteURL = request.getParameter("remoteURL");
		// if parameter for url is not defined get its value from preferences.
		if (remoteURL == null || remoteURL.isEmpty()) {
			remoteURL = preferences.getValue("remoteURL", StringPool.BLANK);
		}

		// If value for url is not defined show page for not setup portlet.
		if (Validator.isNull(remoteURL)) {
			PortletRequestDispatcher portletRequestDispatcher = getPortletContext()
					.getRequestDispatcher("/html/portlet/esigate/portlet_not_setup.jsp");
			if (portletRequestDispatcher != null) portletRequestDispatcher.include(request, response);
		} else {
			//update driver configuration
			ThemeDisplay themeDisplay = (ThemeDisplay) request.getAttribute(WebKeys.THEME_DISPLAY);
			try {
				PortletURL portletURL = response.createRenderURL();
				updateDriverConfig(request.getPreferences(), 
						PortalUtil.getPortalURL(request), 
						themeDisplay.getPortletDisplay().getNamespace(), 
						remoteURL, portletURL.toString());
			} catch (Exception e) {
				_log.error(e);
				throw new PortletException(e);
			}
			
			/**
			 * Set additional request parameters and call aggregator servlet.
			 */
			HttpServletRequest httpServletRequest = PortalUtil.getHttpServletRequest(request);
			HttpServletResponse httpServletResponse = PortalUtil.getHttpServletResponse(response);
			request.setAttribute("EsiGateProvider", themeDisplay.getPortletDisplay().getNamespace());
			ServletContext servletContext = httpServletRequest.getSession().getServletContext();

			RequestDispatcher rd = servletContext.getRequestDispatcher("/aggregator/");
			try {
			rd.forward(httpServletRequest,httpServletResponse);
			} catch (ServletException e) {
				_log.error(e);
				throw new PortletException(e);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void processAction(ActionRequest request, ActionResponse response)
			throws PortletException, IOException {
		
		//Save settings in preferences.
		PortletPreferences preferences = request.getPreferences();
		
		String tab = ParamUtil.getString(request, "tabs1");
		if (tab == null) return;
			
		if (tab.equalsIgnoreCase("general")) {
			preferences.setValue("remoteURL", request.getParameter("remoteURL"));
			preferences.setValue("rewritingURL", request.getParameter("rewritingURL"));
		}
		
		if (tab.equalsIgnoreCase("clipping")) {
				preferences.setValue("clipping",
						request.getParameter("clipping"));
				preferences.setValue("xpath", request.getParameter("xpath"));
				preferences.setValue("xslt", request.getParameter("xsltTemplate"));
		}
		
		if (tab.equalsIgnoreCase("cache")) {
			preferences.setValue("useCache",
					request.getParameter("useCache"));
			preferences.setValue("cacheRefreshDelay", request.getParameter("cacheRefreshDelay"));
		}
		if (tab.equals("css-rewrite")) {
			preferences.setValue("enableCssRwrt",
					request.getParameter("enableCssRwrt"));
			preferences.setValue("prefixCss", request.getParameter("prefixCss"));
			preferences.setValue("cssClassAttr", request.getParameter("cssClassAttr"));
		}
		preferences.store();
		/*
		try {
			ThemeDisplay themeDisplay = (ThemeDisplay) request.getAttribute(WebKeys.THEME_DISPLAY);	
			String namespace = themeDisplay.getPortletDisplay().getNamespace();
			if (namespace == null || namespace.trim().length() == 0) {
				namespace = PortalUtil.getPortletNamespace(PortalUtil.getPortletId(request));
			}
			updateDriverConfig(preferences, PortalUtil.getPortalURL(request), namespace, null, portletUrlStr);
			SessionMessages.add(request, "success");
		    response.setPortletMode(PortletMode.EDIT);
			
		} catch (Exception e) {
			throw new PortletException(e);
		}*/
	}

	/**
	 * Update configuration of EsiGate driver.
	 * 
	 * @param preferences
	 */
	private void updateDriverConfig(PortletPreferences preferences, String portalUrl, String namespace, String remoteUrl, String portletUrl)
			throws Exception {
		if (namespace == null || namespace.trim().length() == 0) return;
		
		String completeUrl = remoteUrl;
		if (completeUrl == null) completeUrl = preferences.getValue("remoteURL", null);
		if (completeUrl == null || completeUrl.isEmpty())	return;
		
		URL completeURL = new URL(completeUrl);
		String baseUrl = completeURL.getProtocol() + "://"
				+ completeURL.getAuthority() +"/";
		String remotePage = null;
		if (completeURL.getFile() != null) remotePage = completeURL.getFile();
		else remotePage = "/";
		if (completeURL.getRef() != null) {
			remotePage += "#" + completeURL.getRef(); 
		}
		Properties props = new Properties();
		props.setProperty("remoteUrlBase", baseUrl);
		props.setProperty("remotePage", remotePage); //ref
		props.setProperty("uriEncoding", "UTF-8");
		props.setProperty("timeout", "0");
		props.setProperty("portletUrl", portletUrl);
		props.setProperty("parsableContentTypes", "text/html, application/xhtml+xml, text/css, text/javascript, application/javascript");
		
		String useCache = preferences.getValue("useCache","false");
		if ("on".equalsIgnoreCase(useCache)) useCache = "true";
		if ("off".equalsIgnoreCase(useCache)) useCache = "false";
		if (useCache == null) useCache = "false";
		
		props.setProperty("useCache", useCache);
		String refreshDelay = preferences.getValue("cacheRefreshDelay", "0");
		if (refreshDelay == null || refreshDelay.trim().length() == 0) {
			refreshDelay = "0";
		}
		props.setProperty("cacheRefreshDelay", refreshDelay);
		String rewriteUrl = preferences.getValue("rewritingURL","false");
		if ("on".equalsIgnoreCase(rewriteUrl)) rewriteUrl = "true";
		if ("off".equalsIgnoreCase(rewriteUrl)) rewriteUrl = "false";
		if (rewriteUrl == null) rewriteUrl = "false";
		props.setProperty("fixResources", rewriteUrl);
		props.setProperty("visibleUrlBase",
				portalUrl + "/" + getPortletContext().getPortletContextName() + "/aggregator/-/" + namespace + "/" );
		
		String clipping = preferences.getValue("clipping", null);
		if (clipping != null && "clippingXpath".equals(clipping)) {
			props.setProperty("xpath", preferences.getValue("xpath", ""));
			props.setProperty("remotePage", completeURL.getFile());
		} else if (clipping != null && "clippingXslt".equals(clipping)) {
			props.setProperty("xslTemplate", preferences.getValue("xslt", ""));
		} 
		
		props.setProperty("enableCssRwrt", String.valueOf(preferences.getValue("enableCssRwrt", "false")));
		props.setProperty("prefixCss", String.valueOf(preferences.getValue("prefixCss", "")));
		props.setProperty("cssClassAttr", preferences.getValue("cssClassAttr", ""));
		String customCssCode = preferences.getValue("customRegexp", null);
		if (customCssCode != null) props.setProperty("customRegexp", customCssCode);
		DriverFactory.configure(namespace, props);
	}

	/**
	 * Save preferences value as request parameters.
	 * 
	 * @param request
	 */
	private void setRequestAttributes(PortletRequest request) {
		PortletPreferences preferences = request.getPreferences();
		request.setAttribute("remoteURL", preferences.getValue("remoteURL", ""));
		request.setAttribute("rewritingURL",
				preferences.getValue("rewritingURL", ""));
		request.setAttribute("clipping", preferences.getValue("clipping", ""));
		request.setAttribute("xpath", preferences.getValue("xpath", ""));
		request.setAttribute("xsltTemplate", preferences.getValue("xslt", ""));
		request.setAttribute("useCache", preferences.getValue("useCache", ""));
		request.setAttribute("cacheRefreshDelay", preferences.getValue("cacheRefreshDelay", ""));
		request.setAttribute("portletName", getPortletName());
		
		request.setAttribute("enableCssRwrt", preferences.getValue("enableCssRwrt", "false"));
		request.setAttribute("prefixCss", preferences.getValue("prefixCss", "false"));
		request.setAttribute("cssClassAttr", preferences.getValue("cssClassAttr", null));
		request.setAttribute("customRegexp", preferences.getValue("customRegexp", null));
	}

}
