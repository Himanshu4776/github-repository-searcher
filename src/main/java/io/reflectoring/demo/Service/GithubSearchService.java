package io.reflectoring.demo.Service;

import com.fasterxml.jackson.databind.JsonNode;
import io.reflectoring.demo.Constants.RepositorySortingModel;
import io.reflectoring.demo.Dto.GitHubRepository;
import io.reflectoring.demo.Dto.GitHubRepositorySearchResponse;
import io.reflectoring.demo.Dto.RepositoryRecord;
import io.reflectoring.demo.Dto.RepositoryRecordResponse;
import io.reflectoring.demo.Entity.GithubRepo;
import io.reflectoring.demo.cache.AppCache;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.List;

@Service
public class GithubSearchService {
    private final RestTemplate restTemplate;
    private final RepoSearchService repoSearchService;
    private final AppCache cache;

    public GithubSearchService(RestTemplate restTemplate, RepoSearchService repoSearchService, AppCache cache) {
        this.restTemplate = restTemplate;
        this.repoSearchService = repoSearchService;
        this.cache = cache;
    }

    private static String BASE_SEARCH_URL = "https://api.github.com/search/repositories?q=";
    private static String RATE_LIMIT_API = "https://api.github.com/rate_limit";

    public RepositoryRecordResponse searchRepositories(RepositoryRecord record) {
        String query = buildQuery(record);
        String url = BASE_SEARCH_URL + query;
        System.out.println(url);

        if (cache.cacheSize == 0) {
            RepositoryRecordResponse response = RepositoryRecordResponse.builder()
                    .setMessage("Repositories saved successfully.")
                    .setRepositories(List.of())
                    .build();
            return response;
        }

//        return null;

        ResponseEntity<GitHubRepositorySearchResponse> response = restTemplate.getForEntity(url, GitHubRepositorySearchResponse.class);

        // check if the response is successful (HTTP status code 200) if not return response with error message.

        if (response.getStatusCode().is2xxSuccessful()) {
            List<GitHubRepository> repositories = response.getBody().items();
            if (repositories != null) {
                List<GithubRepo> githubRepos = repositories.stream()
                        .map(this::convertToGithubRepo)
                        .toList();
                List<GithubRepo> savedRepositories = repoSearchService.saveRepositories(githubRepos);
                if (!savedRepositories.isEmpty()) {
                    System.out.println("Repositories saved successfully.");
                    return createResponse(savedRepositories);
                } else {
                    System.out.println("Failed to save repositories.");
                }
            } else {
                System.out.println("No repositories found.");
            }
        } else {
            throw new RuntimeException("Failed to fetch GitHub issues: " + response.getStatusCode());
        }
        return null;
    }

    private GithubRepo convertToGithubRepo(GitHubRepository gitHubRepository) {
        GithubRepo githubRepo = new GithubRepo();
        githubRepo.setRepositoryId(gitHubRepository.id());
        githubRepo.setName(gitHubRepository.name().toUpperCase());
        githubRepo.setOwnerName(gitHubRepository.owner().login().toUpperCase());
        githubRepo.setDescription(gitHubRepository.description().toUpperCase());
        githubRepo.setStarsCount(gitHubRepository.stargazersCount());
        githubRepo.setForksCount(gitHubRepository.forksCount());
        githubRepo.setProgrammingLanguage(gitHubRepository.language().toUpperCase());
        githubRepo.setUpdatedAt(LocalDate.from(gitHubRepository.updatedAt()));
        return githubRepo;
    }

    private RepositoryRecordResponse createResponse(List<GithubRepo> savedRepositories) {
        RepositoryRecordResponse repositoryRecordResponse = RepositoryRecordResponse.builder()
                .setMessage("Repositories saved successfully.")
                .setRepositories(savedRepositories)
                .build();
        return repositoryRecordResponse;
    }

    private String buildQuery(RepositoryRecord record) {
        String query = record.query();
        int conditionAdded = 0;
        if (record.language() != null && !record.language().isEmpty()) {
            if (conditionAdded == 0) {
                query += "+";
            } else {
                query += "&";
            }
            conditionAdded++;
            query += "language:" + record.language();
        }
        if (record.sortBy() != null && !record.sortBy().isEmpty()) {
            if (conditionAdded == 0) {
                query += "+";
            } else {
                query += "&";
            }

            if (record.sortBy().equals(RepositorySortingModel.STARS.getValue())) {
                query += "sort:stars";
            } else if (record.sortBy().equals(RepositorySortingModel.FORKS.getValue())) {
                query += "sort:forks";
            } else if (record.sortBy().equals(RepositorySortingModel.UPDATED.getValue())) {
                query += "sort:updated";
            }
            conditionAdded++;
            query += "&order=desc";
        }
        return query;
    }

    public int getRemainingRequests() {
        ResponseEntity<JsonNode> response = restTemplate.getForEntity(RATE_LIMIT_API, JsonNode.class);
        JsonNode rootNode = response.getBody();
        if (rootNode != null) {
            JsonNode resourcesNode = rootNode.get("resources");
            if (resourcesNode != null) {
                JsonNode coreNode = resourcesNode.get("search");
                if (coreNode != null) {
                    JsonNode remainingNode = coreNode.get("remaining");
                    if (remainingNode != null) {
                        return remainingNode.asInt();
                    }
                }
            }
        }
        return 0;
    }
}
