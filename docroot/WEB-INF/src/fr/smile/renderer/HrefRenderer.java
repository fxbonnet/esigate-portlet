package fr.smile.renderer;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import net.webassembletool.HttpErrorPage;
import net.webassembletool.Renderer;
import net.webassembletool.ResourceContext;

public class HrefRenderer implements Renderer {
	private static final Logger LOG = Logger.getLogger(HrefRenderer.class);
	public static final int ABSOLUTE = 0;
	public static final int RELATIVE = 1;
	public static final char SLASH = '/';
	protected final String attributeSeparator = "\"";
	private final Pattern pHref [] = new Pattern[] { 
		Pattern.compile("<([a][^>]+)(href)=\"([^\"]+)\"([^>]*)>", Pattern.CASE_INSENSITIVE),
		Pattern.compile("<([a][^>]+)(href)=\'([^\']+)\'([^>]*)>", Pattern.CASE_INSENSITIVE)};
	
	private String baseUrl;
	private String providerName;
	private String portletUrl;

	/**
	 * Creates a renderer which fixes urls. The domain name and the url path are
	 * computed from the full url made of baseUrl + pageFullPath.
	 * 
	 * If mode is ABSOLUTE, all relative urls will be replaced by the full urls
	 * :
	 * <ul>
	 * <li>images/image.png is replaced by
	 * http://server/context/images/image.png</li>
	 * <li>/context/images/image.png is replaced by
	 * http://server/context/images/image.png</li>
	 * </ul>
	 * 
	 * If mode is RELATIVE, context will be added to relative urls :
	 * <ul>
	 * <li>images/image.png is replaced by /context/images/image.png</li>
	 * </ul>
	 * 
	 * @param visibleBaseUrl
	 *            Base url (same as configured in provider).
	 * @param pageFullPath
	 *            Page as used in tag lib or using API
	 * @param mode
	 *            ResourceFixupRenderer.ABSOLUTE or
	 *            ResourceFixupRenderer.RELATIVE
	 * 
	 * @throws MalformedURLException
	 */
	public HrefRenderer(String baseUrl, String providerName, String portletUrl)
			throws MalformedURLException {

		this.baseUrl = baseUrl;
		if (this.baseUrl.charAt(this.baseUrl.length() - 1) != SLASH) {
			this.baseUrl += SLASH;
		}
		this.providerName = providerName;
		this.portletUrl = portletUrl;
	}

	/**
	 * {@inheritDoc}
	 */
	public void render(ResourceContext requestContext, String src, Writer out)
			throws IOException, HttpErrorPage {
		out.write(replace(src).toString());
	}

	/**
	 * Fix all resources urls and return the result.
	 * 
	 * @param charSequence
	 *            The original charSequence to be processed.
	 * 
	 * 
	 * @return the result of this renderer.
	 */
	private final CharSequence replace(CharSequence input) {
		StringBuffer result = new StringBuffer(input);
		CharSequence current = input;
		for (Pattern pattern : pHref) {
			result = new StringBuffer(current.length());
			Matcher m = pattern.matcher(current);
			String url = null;
			String tagReplacement = null;
			while (m.find()) {
				if (!isDisplay(m)) continue;
				url = m.group(3);
				url = url.replaceAll("\\$", "\\\\\\$"); // replace '$' -> '\$' as it
				if (!url.toLowerCase().startsWith("javascript")) {
					url = transformUrl(url);
				}
														// denotes group
				tagReplacement = "<" + m.group(1).replaceAll("\\$", "\\\\\\$")
						+ m.group(2).replaceAll("\\$", "\\\\\\$") + "=\"" + url
						+ "\"";
				if (m.groupCount() > 3) {
					tagReplacement += m.group(4).replaceAll("\\$", "\\\\\\$");
				}
				/*if (tagReplacement.indexOf("target") < 0) {
					tagReplacement += " target=\"_blank\"";
				}*/
				tagReplacement += ">";
				m.appendReplacement(result, tagReplacement);
			}
			m.appendTail(result);
			current = result;
		}
		return result;
	}
	
	private boolean isDisplay(Matcher matcher) {
		if (matcher != null) {
			for (int i = 0; i < matcher.groupCount(); i++) {
				String str = matcher.group(i);
				if (str.indexOf("display:none") >= 0 || str.indexOf("display: none") >= 0 || str.indexOf("display : none") >= 0 || str.indexOf("display :none") >= 0) {
					return false;
				}
			}
		}
		return true;
	}
	
	private String transformUrl(String url) {
		String retValue = url;
		String targetUrl = url;
		int position = url.indexOf(providerName);
		if (position >= 0) {
			targetUrl = url.substring(position + providerName.length() + 1);
			targetUrl = baseUrl + targetUrl;
		} else if (url.indexOf(":\\\\") < 0){
			targetUrl = baseUrl + targetUrl;
		} 
		retValue = portletUrl;
		if (retValue.indexOf("?") > 0) {
			retValue += "&";
		} else {
			retValue += "?";
		}
		try {
			retValue += "remoteURL=" + URLEncoder.encode(targetUrl, "UTF-8");
		} catch (UnsupportedEncodingException e) {
		}
		return retValue;
	}
}
