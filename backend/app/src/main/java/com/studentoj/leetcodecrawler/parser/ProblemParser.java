package com.studentoj.leetcodecrawler.parser;

import com.studentoj.leetcodecrawler.dto.LeetCodeProblemDetail;
import com.studentoj.leetcodecrawler.dto.ParsedProblem;
import com.studentoj.leetcodecrawler.generator.TestCaseGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProblemParser {
    private final HtmlContentParser htmlContentParser;
    private final ExampleParser exampleParser;
    private final SchemaParser schemaParser;
    private final TestCaseGenerator testCaseGenerator;

    public ParsedProblem parse(LeetCodeProblemDetail detail) {
        String contentText = htmlContentParser.toPlainText(detail.content());
        String example = exampleParser.extractExamples(detail.content());
        String expectedOutput = exampleParser.extractExpectedOutput(example);
        SchemaParser.SchemaParseResult schema = schemaParser.parse(detail.content(), detail.mysqlSchemas(), example);
        String tags = String.join(",", detail.tags());
        String testCases = testCaseGenerator.generate(
                schema.schemaInfo(),
                schema.sampleData(),
                expectedOutput,
                contentText,
                tags
        );
        return new ParsedProblem(contentText, example, schema.schemaInfo(), schema.sampleData(), expectedOutput, testCases);
    }
}
