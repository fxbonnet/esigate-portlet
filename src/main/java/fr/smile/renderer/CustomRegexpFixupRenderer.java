package fr.smile.renderer;

import java.io.IOException;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.webassembletool.Renderer;
import net.webassembletool.ResourceContext;

public class CustomRegexpFixupRenderer implements Renderer {
	private static final Logger LOG = LoggerFactory.getLogger(CustomRegexpFixupRenderer.class);

	public static final int ABSOLUTE = 0;
	public static final int RELATIVE = 1;
	public static final char SLASH = '/';
	private String contextAdd = null;
	private String contextRemove = null;
	private String pagePath = null;
	private String server = null;
	private String baseUrl;
	private String replacementUrl;
	private final boolean fixRelativeUrls;
	private final int mode;

	private String regexp;
	
	public CustomRegexpFixupRenderer(String baseUrl, String visibleBaseUrl, String pageFullPath, int mode, String regexp) throws MalformedURLException {
		this(baseUrl, visibleBaseUrl, pageFullPath, mode, true);
		this.regexp = regexp;
	}
	
	public CustomRegexpFixupRenderer(String baseUrl, String visibleBaseUrl, String pageFullPath, int mode, boolean fixRelativeUrls)
						throws MalformedURLException {
					this.mode = mode;
					this.fixRelativeUrls = fixRelativeUrls;

					if (visibleBaseUrl != null && visibleBaseUrl.length() != 0) {
						this.baseUrl = removeLeadingSlash(baseUrl);
						this.replacementUrl = removeLeadingSlash(visibleBaseUrl);
					} else {
						this.baseUrl = null;
						this.replacementUrl = null;
					}

					// Clean up input
					String cleanBaseUrl = baseUrl;
					if (visibleBaseUrl != null) {
						cleanBaseUrl = visibleBaseUrl;
					}
					cleanBaseUrl = removeLeadingSlash(cleanBaseUrl);

					String cleanPageFullPath = pageFullPath;
					if (cleanPageFullPath.charAt(0) == SLASH) {
						cleanPageFullPath = cleanPageFullPath.substring(1);
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

					// Check if we are going to replace context
					if (baseUrl != null && !baseUrl.equals(visibleBaseUrl)) {
						contextRemove = new URL(baseUrl).getPath();
						contextAdd = new URL(visibleBaseUrl).getPath();
					}
				}

				private String removeLeadingSlash(String src) {
					int lastCharPosition = src.length() - 1;
					if (src.charAt(lastCharPosition) != SLASH) {
						return src;
					} else {
						return src.substring(0, lastCharPosition);
					}
				}
	
	/** {@inheritDoc} */
	public void render(ResourceContext requestContext, String src, Writer out) throws IOException {
		out.write(replaceURL(src).toString());
	}

	/**
	 * Fix all resources urls and return the result.
	 * 
	 * @param input
	 *            The original charSequence to be processed.
	 * 
	 * 
	 * @return the result of this renderer.
	 */
	CharSequence replaceURL(CharSequence input) {
		Pattern customPattern = Pattern.compile(regexp);
        Matcher matcher = customPattern.matcher(input);
        StringBuffer result = new StringBuffer();
        while(matcher.find()) {
            int group = extractGroup(matcher);
            if(group > 0) {
                String before = "";
                for (int i = 1; i < group; i++) {
                	before += "$" + String.valueOf(i);
                }               
                String url = matcher.group(group);
                String after = "";
                for (int i = group + 1; i < matcher.groupCount(); i++) {
                	before += "$" + String.valueOf(i);
                }
               	matcher.appendReplacement(result, before + fixUrl(url) + after);
            }
        }
        matcher.appendTail(result);
        return result;
	}
	
	private String fixUrl(String urlParam) {
		String url = urlParam;

		// Do not process 0-length urls
		if (url.length() == 0) {
			return url;
		}

		if (replacementUrl != null && url.startsWith(baseUrl)) {
			url = new StringBuffer(replacementUrl).append(url.substring(baseUrl.length())).toString();
			LOG.debug("fix absolute url: " + urlParam + " -> " + url);
			return url;
		}
		// Keep absolute and javascript urls untouched.
		if (url.startsWith("http://") || url.startsWith("https://")
				|| url.startsWith("#") || url.startsWith("javascript:")) {
			LOG.debug("keeping absolute url: " + url);
			return url;
		}

		// Add domain to context absolute urls
		if (url.charAt(0) == SLASH) {

			if (contextRemove != null && url.startsWith(contextRemove)) {
				url = url.substring(contextRemove.length());
				url = contextAdd + url;
			}

			if (mode == ABSOLUTE) {
				url = server + url;
			}
		} else if (fixRelativeUrls) {
			// Process relative urls
			if (mode == ABSOLUTE) {
				url = server + pagePath + SLASH + url;
			} else {
				url = pagePath + SLASH + url;
			}
		}

		LOG.debug("url fixed: " + urlParam + " -> " + url);
		return url;
	}

    private int extractGroup(Matcher matcher) {
        int matchingGroup = -1;
        for(int i = 1; i <= matcher.groupCount(); i++) {
            if(matcher.start(i) > -1) {
                matchingGroup = i;
                break;
            }
        }
        return matchingGroup;
    }
}
