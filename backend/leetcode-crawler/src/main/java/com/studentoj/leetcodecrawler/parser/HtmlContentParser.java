package com.studentoj.leetcodecrawler.parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;

@Component
public class HtmlContentParser {
    public String toPlainText(String html) {
        if (html == null || html.isBlank()) {
            return "";
        }
        Document document = Jsoup.parse(html);
        document.select("pre").forEach(pre -> pre.before("\\n").after("\\n"));
        return document.text().replace("\\n", System.lineSeparator()).trim();
    }
}
