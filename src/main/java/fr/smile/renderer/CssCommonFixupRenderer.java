package fr.smile.renderer;

import java.io.IOException;
import java.io.Writer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.webassembletool.HttpErrorPage;
import net.webassembletool.Renderer;
import net.webassembletool.ResourceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CssCommonFixupRenderer implements Renderer {
	private static final Logger LOG = LoggerFactory
			.getLogger(CssRewriteRenderer.class);
	private static final Pattern CSS_PATTERNS[] = new Pattern[] {						
			Pattern.compile("(.*^[<!--])(body|h1|h2|h3|h4|h5)([ |,|{].*)", Pattern.CASE_INSENSITIVE)
	};

	private static final Pattern HTML_TAG_PATTERN = Pattern.compile("(.*)<(body|h1|h2|h3|h4|h5)(.*)>(.*)</body>(.*)");
	private String portletClassAttr;
	private boolean usePortletClassAttr;

	public CssCommonFixupRenderer(String portletClassAttr,
			boolean usePortletClassAttr) {
		this.portletClassAttr = portletClassAttr;
		this.usePortletClassAttr = usePortletClassAttr;
	}

	@Override
	public void render(ResourceContext requestContext, String src, Writer out)
			throws IOException, HttpErrorPage {
		CharSequence replacedSrc = replaceCss(src);
		out.write(replaceHtml(replacedSrc).toString());
	}

	/**
	 * Fix all css classes specific for portal and return the result.
	 * 
	 * @param input
	 *            The original charSequence to be processed.
	 * 
	 * 
	 * @return the result of this renderer.
	 */
	CharSequence replaceCss(CharSequence input) {
		if (!usePortletClassAttr
				|| (portletClassAttr == null || portletClassAttr
						.length() == 0)) {
			portletClassAttr = "esigate";
		}
		StringBuffer result = new StringBuffer(input);
		CharSequence current = input;		
		for (Pattern pattern : CSS_PATTERNS) {
			Matcher m = pattern.matcher(current);
			while (m.find()) {
				LOG.trace("found match: " + m);
				StringBuffer tagReplacement = new StringBuffer("");
				if (m.groupCount() > 3) {
					tagReplacement = new StringBuffer("<$1 $2").append(portletClassAttr).append("_$3$4>");
				} else {
					tagReplacement = new StringBuffer("$1").append("#").append(portletClassAttr).append("_$2$3");
				}
				LOG.trace("replacement: " + tagReplacement);
				m.appendReplacement(result, tagReplacement.toString());

			}
			m.appendTail(result);
			current = result;
		}
		return result;
	}
	
	/**
	 * Fix all css classes specific for portal and return the result.
	 * 
	 * @param input
	 *            The original charSequence to be processed.
	 * 
	 * 
	 * @return the result of this renderer.
	 */
	CharSequence replaceHtml(CharSequence input) {
		if (!usePortletClassAttr
				|| (portletClassAttr == null || portletClassAttr
						.length() == 0)) {
			portletClassAttr = "esigate";
		}
		StringBuffer result = new StringBuffer(input.length());

		Matcher m = HTML_TAG_PATTERN.matcher(input);
		while (m.find()) {
			LOG.trace("found match: " + m);
			StringBuffer tagReplacement = new StringBuffer("");			
			tagReplacement = new StringBuffer("$1<body$2><div class=\"").append(portletClassAttr).append("_body\">$3</div></body>");
			if (m.groupCount() > 3) 
				tagReplacement.append("$4");
			LOG.trace("replacement: " + tagReplacement);
			m.appendReplacement(result, tagReplacement.toString());

		}
		m.appendTail(result);
		return result;
	}
}
