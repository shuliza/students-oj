package com.studentoj.common.web;

import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

public final class HtmlSanitizer {
    private static final Safelist DESCRIPTION_HTML = Safelist.relaxed()
            .addTags("table", "thead", "tbody", "tfoot", "tr", "th", "td")
            .addAttributes("th", "colspan", "rowspan")
            .addAttributes("td", "colspan", "rowspan")
            .addProtocols("a", "href", "http", "https", "mailto")
            .addProtocols("img", "src", "http", "https");

    private HtmlSanitizer() {
    }

    public static String cleanDescription(String description) {
        if (description == null || description.isBlank()) {
            return "";
        }
        return Jsoup.clean(description, DESCRIPTION_HTML);
    }
}
