package fr.smile.renderer;

import java.io.IOException;
import java.io.Writer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.webassembletool.HttpErrorPage;
import net.webassembletool.Renderer;
import net.webassembletool.ResourceContext;

/**
 * Renderer to rewrite css and change name of classes.
 * 
 * @author tatyanaho
 *
 */
public class CssRewriteRenderer implements Renderer {
	private static final Logger LOG = LoggerFactory
			.getLogger(CssRewriteRenderer.class);
	private static final Pattern CSS_PATTERNS[] = new Pattern[] {
			Pattern.compile(
					"(.*)#(footer|main-content|navigation|wrapper|banner|skip-to-content|sign-in)([ |,|{].*)",
					Pattern.CASE_INSENSITIVE),
			Pattern.compile(
					"<([^>]+)(id *= *'|\")(footer|main-content|navigation|wrapper|banner|skip-to-content|sign-in)('|\".*)>",
					Pattern.CASE_INSENSITIVE)
	};

	private String portletClassAttr;
	private boolean usePortletClassAttr;

	public CssRewriteRenderer(String portletClassAttr,
			boolean usePortletClassAttr) {
		this.portletClassAttr = portletClassAttr;
		this.usePortletClassAttr = usePortletClassAttr;
	}

	@Override
	public void render(ResourceContext requestContext, String src, Writer out)
			throws IOException, HttpErrorPage {
		out.write(replace(src).toString());
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
	CharSequence replace(CharSequence input) {
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
}
