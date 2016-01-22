package ca.quadrilateral.jobsmarts.fetcher;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class HtmlUtils {
    public static Element getSingleElement(final Elements elements) {
        if (elements.size() != 1) {
            throw new IllegalArgumentException("A single element matching the criteria was expected.  Instead, found " + elements.size());
        }
        
        return elements.first();
    }
}
