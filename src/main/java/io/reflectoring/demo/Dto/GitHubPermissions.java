package io.reflectoring.demo.Dto;

public record GitHubPermissions(
        boolean admin,
        boolean maintain,
        boolean push,
        boolean pull,
        boolean triage
) {}
