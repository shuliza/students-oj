package com.studentoj.leetcodecrawler.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

@Component
public class ExampleParser {
    private static final Pattern OUTPUT_PATTERN = Pattern.compile("(?is)Output:\\s*(.*?)(?:Explanation:|$)");

    public String extractExamples(String html) {
        if (html == null || html.isBlank()) {
            return "";
        }
        Document document = Jsoup.parse(html);
        List<String> examples = new ArrayList<>();
        for (Element pre : document.select("pre")) {
            String text = pre.wholeText().trim();
            if (text.contains("Input:") || text.contains("Output:")) {
                examples.add(normalize(text));
            }
        }
        if (!examples.isEmpty()) {
            return String.join(System.lineSeparator() + System.lineSeparator(), examples);
        }
        String plainText = document.text();
        int first = indexOfIgnoreCase(plainText, "Input:");
        return first >= 0 ? plainText.substring(first).trim() : "";
    }

    public String extractExpectedOutput(String example) {
        if (example == null || example.isBlank()) {
            return "";
        }
        Matcher matcher = OUTPUT_PATTERN.matcher(example);
        if (!matcher.find()) {
            return "";
        }
        return matcher.group(1).trim();
    }

    private String normalize(String text) {
        return text.replace('\u00a0', ' ')
                .replaceAll("\\r\\n?", "\n")
                .replaceAll("[ \\t]+\\n", "\n")
                .trim();
    }

    private int indexOfIgnoreCase(String value, String needle) {
        return value.toLowerCase().indexOf(needle.toLowerCase());
    }
}
