package io.reflectoring.demo.Dto;

import io.reflectoring.demo.Entity.GithubRepo;

import java.util.List;

public record RepositoryRecordResponse(String message, List<GithubRepo> repositories) {
    public static class Builder {
        private String message;
        private List<GithubRepo> repositories;

        public Builder setMessage(String message) {
            this.message = message;
            return this;
        }

        public Builder setRepositories(List<GithubRepo> repositories) {
            this.repositories = repositories;
            return this;
        }

        public RepositoryRecordResponse build() {
            return new RepositoryRecordResponse(message, repositories);
        }
    }

    public static Builder builder() {
        return new Builder();
    }
}
