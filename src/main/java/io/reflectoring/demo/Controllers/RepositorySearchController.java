package io.reflectoring.demo.Controllers;

import io.reflectoring.demo.Dto.RepositoryRecord;
import io.reflectoring.demo.Dto.RepositoryRecordResponse;
import io.reflectoring.demo.Entity.GithubRepo;
import io.reflectoring.demo.Service.GithubSearchService;
import io.reflectoring.demo.Service.RepoSearchService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/github")
public class RepositorySearchController {
    private final GithubSearchService githubSearchService;
    private final RepoSearchService repoSearchService;

    public RepositorySearchController(GithubSearchService githubSearchService, RepoSearchService repoSearchService) {
        this.githubSearchService = githubSearchService;
        this.repoSearchService = repoSearchService;
    }

    @GetMapping("/search")
    public ResponseEntity<RepositoryRecordResponse> searchRepositories(@RequestBody RepositoryRecord repositoryRecord) {
        if(!isRepositoryRecordValid(repositoryRecord)) {
            return new  ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }

        RepositoryRecordResponse repositoryRecordResponse = githubSearchService.searchRepositories(repositoryRecord);
        if (repositoryRecordResponse == null || repositoryRecordResponse.repositories().isEmpty()) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(repositoryRecordResponse, HttpStatus.OK);
    }

    private boolean isRepositoryRecordValid(RepositoryRecord repositoryRecord) {
        return repositoryRecord != null &&
                repositoryRecord.query() != null &&
                !repositoryRecord.query().isEmpty() &&
                repositoryRecord.language() != null &&
                !repositoryRecord.language().isEmpty() &&
                repositoryRecord.sortBy() != null &&
                !repositoryRecord.sortBy().isEmpty();
    }

    @GetMapping("/repositories")
    public ResponseEntity<List<GithubRepo>> getRepositories(
            @RequestParam(name = "language", required = false) String language,
            @RequestParam(name = "minStars", required = false, defaultValue = "0") Integer minStars,
            @RequestParam(name = "sort", required = false, defaultValue = "stars") String sort
    ) {
        if(!isSearchRequestValid(language, minStars, sort)) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }

        language = language != null ? language.toUpperCase() : "";

        List<GithubRepo> repositories = repoSearchService.getRepositoriesByCriteria(language, minStars, sort);
        return new ResponseEntity<>(repositories, HttpStatus.OK);
    }

    private boolean isSearchRequestValid(String language, Integer minStars, String sort) {
        // Check language if provided
        if (language != null && language.isEmpty()) {
            return false;
        }

        // Check minStars if provided
        if (minStars != null && minStars < 0) {
            return false;
        }

        // Check sort if provided
        if (sort != null && sort.isEmpty()) {
            return false;
        }

        return true;
    }
}
