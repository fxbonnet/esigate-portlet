package fr.smile.servlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.webassembletool.DriverConfiguration;
import net.webassembletool.DriverFactory;
import net.webassembletool.HttpErrorPage;
import net.webassembletool.Renderer;
import net.webassembletool.output.TextOnlyStringOutput;
import net.webassembletool.xml.XpathRenderer;
import net.webassembletool.xml.XsltRenderer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.smile.renderer.CustomRegexpFixupRenderer;
import fr.smile.renderer.CssRewriteRenderer;
import fr.smile.renderer.FormActionRenderer;
import fr.smile.renderer.HrefRenderer;
import fr.smile.renderer.HrefTargetRenderer;

public class AggregatorServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(AggregatorServlet.class);

	@Override
	protected void service(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		String relUrl = request.getRequestURI(); 
		relUrl = relUrl.substring(request.getContextPath().length());
		if (request.getServletPath() != null) {
			relUrl = relUrl.substring(request.getServletPath().length());
		}
		boolean isStart = false;
		String provider = null;
		if (relUrl.startsWith("/-/")) {
			provider = relUrl.substring(3, relUrl.indexOf("/", 3));
			relUrl = relUrl.substring(provider.length() + 3);
		} else {
			provider = (String) request.getAttribute("EsiGateProvider");
			isStart = true;
		}
		if (provider == null)
			return;
		LOG.debug("Aggregating " + relUrl);
		try {
			DriverConfiguration driverConfig = DriverFactory.getInstance(provider).getConfiguration();
			
			String remotePage = driverConfig.getProperties().getProperty("remotePage");
			if (isStart) {
				relUrl = remotePage;
			}
			boolean isCssRewrite = Boolean.parseBoolean(driverConfig.getProperties().getProperty("enableCssRwrt"));
			boolean isProxy = true;
			
			List<Renderer> renderers = new ArrayList<Renderer>(0);			
			
			String customRegExp = driverConfig.getProperties().getProperty("customRegexp");
			if (customRegExp != null && customRegExp.trim().length() > 0) {
				driverConfig.getProperties().setProperty("fixResources", "false");
				renderers.add(new CustomRegexpFixupRenderer(driverConfig.getBaseURL(), driverConfig.getVisibleBaseURL(), 
						remotePage, driverConfig.getFixMode(), customRegExp));
			}
			
			if (isCssRewrite) {
				renderers.add(new CssRewriteRenderer(driverConfig.getProperties().getProperty("cssClassAttr"),
						Boolean.parseBoolean(driverConfig.getProperties().getProperty("prefixCss"))));
			}
			
			String xpath = DriverFactory.getInstance(provider)
					.getConfiguration().getProperties().getProperty("xpath");
			if (xpath != null) {
				isProxy = false;
				renderers.add(new XpathRenderer(xpath));
			} else{
				String xslTemplate = driverConfig.getProperties().getProperty("xslTemplate");
				if (xslTemplate != null) {
					isProxy = false;
					renderers.add(new XsltRenderer(xslTemplate));
				}
			}
			renderers.add(new FormActionRenderer(driverConfig.getBaseURL(), remotePage));
			renderers.add(new HrefRenderer(driverConfig.getBaseURL(), 
					provider, driverConfig.getProperties().getProperty("portletUrl")));
			
			if (isProxy) {
				DriverFactory.getInstance(provider).proxy(relUrl, request, response,
					renderers.toArray(new Renderer[renderers.size()]));
			} else {
				DriverFactory.getInstance(provider).render(remotePage, null, response.getWriter(), request, response,
						renderers.toArray(new Renderer[renderers.size()]));
			}
		} catch (HttpErrorPage e) {
			throw new ServletException(e);
		}
	}

}
