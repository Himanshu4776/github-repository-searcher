package io.reflectoring.demo.Service;

import io.reflectoring.demo.Dto.*;
import io.reflectoring.demo.cache.AppCache;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class GithubSearchServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private RepoSearchService repoSearchService;

    @Mock
    private AppCache cache;

    @InjectMocks
    private GithubSearchService githubSearchService;

    @Test
    public void testSearchRepositories_HappyPath() {
        RepositoryRecord repositoryRecord = new RepositoryRecord("query", "language", "sortBy");
        GitHubRepositorySearchResponse response = new GitHubRepositorySearchResponse(1, false, new ArrayList<GitHubRepository>());

        ResponseEntity<Object> responseEntity = ResponseEntity.ok(response);

        when(restTemplate.getForEntity(any(), any())).thenReturn(responseEntity);

        RepositoryRecordResponse result = githubSearchService.searchRepositories(repositoryRecord);

        assertNotNull(result);
        assertEquals("Repositories saved successfully.", result.message());
        assertEquals(1, result.repositories().size());
    }

    @Test
    public void testSearchRepositories_NoResults() {
        RepositoryRecord repositoryRecord = new RepositoryRecord("query", "language", "sortBy");
        GitHubRepositorySearchResponse response = new GitHubRepositorySearchResponse(0, false, new ArrayList<GitHubRepository>());

        ResponseEntity<Object> responseEntity = ResponseEntity.ok(response);

        when(restTemplate.getForEntity(any(), any())).thenReturn(responseEntity);

        RepositoryRecordResponse result = githubSearchService.searchRepositories(repositoryRecord);

        assertNotNull(result);
        assertEquals("No repositories found.", result.message());
        assertEquals(0, result.repositories().size());
    }

    @Test
    public void testSearchRepositories_RequestLimitExceeded() {
        RepositoryRecord repositoryRecord = new RepositoryRecord("query", "language", "sortBy");
        AppCache cache = new AppCache();
        cache.cacheSize = 0;

        GithubSearchService githubSearchService = new GithubSearchService(restTemplate, repoSearchService, cache);

        RepositoryRecordResponse result = githubSearchService.searchRepositories(repositoryRecord);

        assertNotNull(result);
        assertEquals("Request Limit Exceeded.", result.message());
        assertEquals(0, result.repositories().size());
    }
}