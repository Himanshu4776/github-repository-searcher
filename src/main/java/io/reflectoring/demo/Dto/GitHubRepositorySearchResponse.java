package io.reflectoring.demo.Dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GitHubRepositorySearchResponse(
        @JsonProperty("total_count") int totalCount,
        @JsonProperty("incomplete_results") boolean incompleteResults,
        List<GitHubRepository> items
) {}
