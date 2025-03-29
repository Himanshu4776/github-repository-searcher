package io.reflectoring.demo.Dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GitHubLicense(
        String key,
        String name,
        @JsonProperty("spdx_id") String spdxId,
        String url,
        @JsonProperty("node_id") String nodeId
) {}
