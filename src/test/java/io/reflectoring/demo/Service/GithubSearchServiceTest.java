package io.reflectoring.demo.Service;

import io.reflectoring.demo.Dto.*;
import io.reflectoring.demo.Entity.GithubRepo;
import io.reflectoring.demo.cache.AppCache;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.eq;

@ExtendWith(MockitoExtension.class)
public class GithubSearchServiceTest {
    public static GitHubRepository sampleRepo;
    public static GitHubOwner owner;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private RepoSearchService repoSearchService;

    @Mock
    private AppCache cache;

    @InjectMocks
    private GithubSearchService githubSearchService;

    @BeforeEach
    public void setUp() {
        owner = new GitHubOwner(
                "testUser",
                12345L,
                "MDQ6VXNlcjEyMzQ1",
                "https://github.com/avatar.png",
                "",
                "https://api.github.com/users/testUser",
                "https://github.com/testUser",
                "https://api.github.com/users/testUser/followers",
                "https://api.github.com/users/testUser/following",
                "https://api.github.com/users/testUser/gists",
                "https://api.github.com/users/testUser/starred",
                "https://api.github.com/users/testUser/subscriptions",
                "https://api.github.com/users/testUser/orgs",
                "https://api.github.com/users/testUser/repos",
                "https://api.github.com/users/testUser/events",
                "https://api.github.com/users/testUser/received_events",
                "User",
                false,
                "User"
        );


        sampleRepo = new GitHubRepository(
                123456L,                  // id
                "MDEwOlJlcG9zaXRvcnkxMjM0NTY=", // nodeId
                "sample-repo",            // name
                "testUser/sample-repo",   // fullName
                false,                    // isPrivate
                owner,                     // owner
                "https://github.com/testUser/sample-repo", // htmlUrl
                "A sample repository for testing", // description
                false,                    // fork
                "https://api.github.com/repos/testUser/sample-repo", // url
                "null", "null", "null", "null", "null", "null", "null", "null", "null", "null",
                "null", "null", "null", "null", "null", "null", "null", "null", "null", "null",
                "null", "null", "null", "null", "null", "null", "null", "null", "null", "null",
                "null", "null", "null", "null", "null", "null", ZonedDateTime.now(), ZonedDateTime.now(), ZonedDateTime.now(), "null",
                "null","null","null","null",0,5,0,"language",false,false,false,false,
                false, false,5,"null", false,false,0,null,false,false,

                false, new ArrayList<String>(),"null",0,0,0,"null",0,null
        );


    }

    @Test
    public void testSearchRepositories_HappyPath() {
        cache.cacheSize = 10;
        RepositoryRecord repositoryRecord = new RepositoryRecord("query", "language", "stars");
        List<GitHubRepository> repositories = new ArrayList<>();
        repositories.add(sampleRepo);

        GitHubRepositorySearchResponse response = new GitHubRepositorySearchResponse(
                repositories.size(),
                false,
                repositories
        );

        String expectedUrl = "https://api.github.com/search/repositories?q=query+language:language&sort=stars&order=desc";
        when(restTemplate.getForEntity(
                eq(expectedUrl),
                eq(GitHubRepositorySearchResponse.class)))
                .thenReturn(ResponseEntity.ok(response));

        GithubRepo githubRepo = new GithubRepo();
        githubRepo.setId(1L);
        githubRepo.setName("name");
        githubRepo.setDescription("description");
        githubRepo.setOwnerName("owner");
        githubRepo.setProgrammingLanguage("language");
        githubRepo.setStarsCount(0);
        githubRepo.setForksCount(0);
        githubRepo.setUpdatedAt(LocalDate.now());

        when(repoSearchService.saveRepositories(any())).thenReturn(List.of(githubRepo));
        RepositoryRecordResponse result = githubSearchService.searchRepositories(repositoryRecord);

        assertNotNull(result);
        assertEquals("Repositories saved successfully.", result.message());
        assertEquals(1, result.repositories().size());
    }



    @Test
    public void testSearchRepositories_NoResults() {
        cache.cacheSize = 10;

        RepositoryRecord repositoryRecord = new RepositoryRecord("query", "language", "stars");
        GitHubRepositorySearchResponse response = new GitHubRepositorySearchResponse(0, true, new ArrayList<GitHubRepository>());

        String expectedUrl = "https://api.github.com/search/repositories?q=query+language:language&sort=stars&order=desc";
        when(restTemplate.getForEntity(
                eq(expectedUrl),
                eq(GitHubRepositorySearchResponse.class)))
                .thenReturn(ResponseEntity.ok(response));

        GithubRepo githubRepo = new GithubRepo();
        githubRepo.setId(1L);
        githubRepo.setName("name");
        githubRepo.setDescription("description");
        githubRepo.setOwnerName("owner");
        githubRepo.setProgrammingLanguage("language");
        githubRepo.setStarsCount(0);
        githubRepo.setForksCount(0);
        githubRepo.setUpdatedAt(LocalDate.now());

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