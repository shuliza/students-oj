package com.studentoj.leetcodecrawler.client;

import com.studentoj.leetcodecrawler.dto.LeetCodeProblemDetail;
import com.studentoj.leetcodecrawler.dto.LeetCodeProblemSummary;
import java.util.List;

public interface LeetCodeClient {
    List<LeetCodeProblemSummary> fetchDatabaseProblems();

    LeetCodeProblemDetail fetchProblemDetail(String titleSlug);
}
