package fr.smile.renderer;

import java.io.IOException;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.webassembletool.HttpErrorPage;
import net.webassembletool.Renderer;
import net.webassembletool.ResourceContext;

public class FormActionRenderer implements Renderer{
	public static final int ABSOLUTE = 0;
	public static final int RELATIVE = 1;
	public static final char SLASH = '/';
	protected final String attributeSeparator = "\"";
	private String pagePath = null;
	private final Pattern pHref [] = {Pattern
			.compile("<([form][^>]+)(action)=\"([^\"]+)\"([^>]*)>", Pattern.CASE_INSENSITIVE),
		Pattern.compile("<([form][^>]+)(action)=\'([^\']+)\'([^>]*)>", Pattern.CASE_INSENSITIVE)};
					
	private String server = null;

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
	public FormActionRenderer(String baseUrl, String pageFullPath)
			throws MalformedURLException {

		// Clean up input
		String cleanBaseUrl = baseUrl;

		if (cleanBaseUrl.charAt(cleanBaseUrl.length() - 1) == SLASH) {
			cleanBaseUrl = cleanBaseUrl.substring(0, cleanBaseUrl.length() - 1);
		}

		String cleanPageFullPath = pageFullPath;
		if (cleanPageFullPath.length()> 0 && cleanPageFullPath.charAt(0) == SLASH) {
			cleanPageFullPath = cleanPageFullPath.substring(1);
		} else if (cleanPageFullPath.length()==0) {
			cleanPageFullPath += SLASH;
		}
		URL url = new URL(cleanBaseUrl + SLASH + cleanPageFullPath);

		// Split url
		server = url.getProtocol() + "://" + url.getHost();
		if (url.getPort() > -1) {
			server += ":" + url.getPort();
		}
		this.pagePath = url.getPath();
		if (pagePath != null) {
			int indexSlash = pagePath.lastIndexOf(SLASH);
			if (indexSlash >= 0) {
				pagePath = pagePath.substring(0, indexSlash);
			}
		}
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
				url = m.group(3);
				url = url.replaceAll("\\$", "\\\\\\$"); // replace '$' -> '\$' as it denotes group
				tagReplacement = "<" + m.group(1).replaceAll("\\$", "\\\\\\$") + m.group(2).replaceAll("\\$", "\\\\\\$") + "=\"" + url + "\"";
				if (m.groupCount() > 3) {
					tagReplacement += m.group(4).replaceAll("\\$", "\\\\\\$");
				}
				if (tagReplacement.indexOf("target") < 0) {
					tagReplacement += " target=\"_blank\"";
				}
				tagReplacement += ">";
				m.appendReplacement(result, tagReplacement);
			}
			m.appendTail(result);
			current = result;
		}
		return result;
	}
}
