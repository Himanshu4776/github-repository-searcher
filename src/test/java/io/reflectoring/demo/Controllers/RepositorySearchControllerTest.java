package io.reflectoring.demo.Controllers;

import io.reflectoring.demo.Dto.RepositoryRecord;
import io.reflectoring.demo.Dto.RepositoryRecordResponse;
import io.reflectoring.demo.Entity.GithubRepo;
import io.reflectoring.demo.Service.GithubSearchService;
import io.reflectoring.demo.Service.RepoSearchService;
import io.reflectoring.demo.cache.AppCache;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RepositorySearchControllerTest {

    @Mock
    private GithubSearchService githubSearchService;

    @Mock
    private RepoSearchService repoSearchService;

    @InjectMocks
    private RepositorySearchController repositorySearchController;

    @Mock
    private AppCache cache;

    private MockMvc mockMvc;

    @BeforeEach
    public void setup() {
        cache.cacheSize = 0;
        mockMvc = MockMvcBuilders.standaloneSetup(repositorySearchController).build();
    }

    @Test
    public void testSearchRepositories_HappyPath() {
        
        RepositoryRecord repositoryRecord = new RepositoryRecord("query", "language", "sortBy");
        RepositoryRecordResponse response = RepositoryRecordResponse.builder()
                .setMessage("Repositories saved successfully.")
                .setRepositories(List.of())
                .build();

        when(githubSearchService.searchRepositories(repositoryRecord)).thenReturn(response);
        ResponseEntity<RepositoryRecordResponse> result = repositorySearchController.searchRepositories(repositoryRecord);

        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(response, result.getBody());
    }
    
    @Test
    public void testSearchRepositories_InvalidRequest() {
        RepositoryRecord repositoryRecord = new RepositoryRecord(null, null, null);
        ResponseEntity<RepositoryRecordResponse> result = repositorySearchController.searchRepositories(repositoryRecord);

        assertNotNull(result);
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
    }

    @Test
    public void testGetRepositories_HappyPath() {
        List<GithubRepo> repositories = new ArrayList<>();
        when(repoSearchService.getRepositoriesByCriteria(any(), any(), any())).thenReturn(repositories);

        ResponseEntity<List<GithubRepo>> result = repositorySearchController.getRepositories("language",
                10, "sort");

        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(repositories, result.getBody());
    }

    @Test
    public void testGetRepositories_InvalidRequest() {
        ResponseEntity<List<GithubRepo>> result = repositorySearchController.getRepositories("", -1, "");

        assertNotNull(result);
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
    }
}