package io.reflectoring.demo.Service;

import io.reflectoring.demo.Constants.RepositorySortingModel;
import io.reflectoring.demo.Entity.GithubRepo;
import io.reflectoring.demo.Repositories.RepoSearchRepository;
import io.reflectoring.demo.Service.RepoSearchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@ExtendWith(MockitoExtension.class)
public class RepoSearchServiceTest {

    @Mock
    private RepoSearchRepository repoSearchRepository;

    @InjectMocks
    private RepoSearchService repoSearchService;

    @Mock
    private Logger logger;

    @Test
    public void testSaveRepositories_HappyPath() {
        // Arrange
        List<GithubRepo> repositories = new ArrayList<>();
        GithubRepo githubRepo = new GithubRepo();
        githubRepo.setRepositoryId(1L);
        githubRepo.setName("repoName");
        githubRepo.setOwnerName("ownerName");
        githubRepo.setDescription("repoDescription");
        githubRepo.setStarsCount(10);
        githubRepo.setForksCount(5);
        githubRepo.setProgrammingLanguage("java");
        githubRepo.setUpdatedAt(LocalDate.of(2022, 1, 1));
        repositories.add(githubRepo);

        when(repoSearchRepository.saveAll(any())).thenReturn(repositories);

        // Act
        List<GithubRepo> result = repoSearchService.saveRepositories(repositories);

        // Assert
        assertEquals(repositories, result);
    }

    @Test
    public void testSaveRepositories_DataIntegrityViolationException() {
        // Arrange
        List<GithubRepo> repositories = new ArrayList<>();
        GithubRepo githubRepo = new GithubRepo();
        githubRepo.setRepositoryId(1L);
        githubRepo.setName("repoName");
        githubRepo.setOwnerName("ownerName");
        githubRepo.setDescription("repoDescription");
        githubRepo.setStarsCount(10);
        githubRepo.setForksCount(5);
        githubRepo.setProgrammingLanguage("java");
        githubRepo.setUpdatedAt(LocalDate.of(2022, 1, 1));
        repositories.add(githubRepo);

        when(repoSearchRepository.saveAll(any())).thenThrow(DataIntegrityViolationException.class);

        // Act
        repoSearchService.saveRepositories(repositories);

        // Assert
        verify(repoSearchRepository).saveAll(any());
    }

    @Test
    public void testSearchRepositoriesByLanguage_HappyPath() {
        // Arrange
        String language = "java";
        List<GithubRepo> repositories = new ArrayList<>();
        GithubRepo githubRepo = new GithubRepo();
        githubRepo.setRepositoryId(1L);
        githubRepo.setName("repoName");
        githubRepo.setOwnerName("ownerName");
        githubRepo.setDescription("repoDescription");
        githubRepo.setStarsCount(10);
        githubRepo.setForksCount(5);
        githubRepo.setProgrammingLanguage(language);
        githubRepo.setUpdatedAt(LocalDate.of(2022, 1, 1));
        repositories.add(githubRepo);

        when(repoSearchRepository.findByProgrammingLanguage(language, Sort.by("forksCount"))).thenReturn(repositories);

        // Act
        List<GithubRepo> result = repoSearchService.getRepositoriesByCriteria(language, null, RepositorySortingModel.FORKS.getValue());

        // Assert
        assertEquals(repositories, result);
        verify(repoSearchRepository).findByProgrammingLanguage(language, Sort.by("forksCount"));
    }

    @Test
    public void testSearchRepositoriesByLanguage_NoResults() {
        // Arrange
        String language = "java";
        RepositorySortingModel sortingModel = RepositorySortingModel.FORKS; // use a valid enum constant
        List<GithubRepo> repositories = new ArrayList<>();

        when(repoSearchRepository.findByProgrammingLanguage(language, Sort.by("forksCount"))).thenReturn(repositories);

        // Act
        List<GithubRepo> result = repoSearchService.getRepositoriesByCriteria(language, null, sortingModel.getValue());

        // Assert
        assertEquals(repositories, result);
        verify(repoSearchRepository).findByProgrammingLanguage(language, Sort.by("forksCount"));
    }
}