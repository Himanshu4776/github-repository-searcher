package io.reflectoring.demo.Service;

import io.reflectoring.demo.Constants.RepositorySortingModel;
import io.reflectoring.demo.Entity.GithubRepo;
import io.reflectoring.demo.Repositories.RepoSearchRepository;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class RepoSearchService {
    private final RepoSearchRepository repoSearchRepository;
    private static final Logger logger = LoggerFactory.getLogger(RepoSearchService.class);

    RepoSearchService(RepoSearchRepository repoSearchRepository) {
        this.repoSearchRepository = repoSearchRepository;
    }

    // Perform batch save repositories in the database
    @Transactional
    public List<GithubRepo> saveRepositories(List<GithubRepo> repositories) {
        try {
            List<GithubRepo> githubRepos = repoSearchRepository.saveAll(repositories);
            return githubRepos;
        } catch (DataIntegrityViolationException e) {
            // Log the error
            logger.error("Unique constraint violation during batch save", e);
            List<GithubRepo> uniqueRepositories = new ArrayList<>();

            // Fall back to individual saves with try-catch for each
            for (GithubRepo entity : repositories) {
                try {
                    GithubRepo saved = repoSearchRepository.save(entity);
                    uniqueRepositories.add(saved);
                } catch (DataIntegrityViolationException ex) {
                    logger.warn("Skipping duplicate entity: " + entity);
                }
            }
            return uniqueRepositories;
        }
    }

    public GithubRepo getRepositoryById(Long id) {
        return repoSearchRepository.findByRepositoryId(id);
    }

    public GithubRepo saveRepository(GithubRepo repository) {
        return repoSearchRepository.save(repository);
    }

    public List<GithubRepo> getAllRepositories() {
        return repoSearchRepository.findAll();
    }

    public List<GithubRepo> getRepositoriesByCriteria(String language, Integer minStars, String sort) {
        if (language != null && minStars != null) {
            return findRepositoryByLanguageAndStars(language, minStars, sort);
        }
        else if (language != null) {
            return findRepositoryByLanguage(language, sort);

        } else if (minStars != null) {
            return findByStarsGreaterCount(minStars, sort);
        }
        return null;
    }

    public List<GithubRepo> findByStarsGreaterCount(Integer minStars, String sort) {
        if(sort == null) {
            sort = "starsCount";
        }
        switch(RepositorySortingModel.valueOf(sort)) {
            case UPDATED -> {
                return repoSearchRepository.findByStarsCountGreaterThan(minStars,
                        Sort.by(Sort.Direction.ASC, "updatedAt"));
            }
            case FORKS -> {
                return repoSearchRepository.findByStarsCountGreaterThan(minStars,
                        Sort.by(Sort.Direction.ASC, "forksCount"));
            }
            default -> {
                return repoSearchRepository.findByStarsCountGreaterThan(minStars,
                        Sort.by(Sort.Direction.ASC, "starsCount"));
            }
        }
    }

    private List<GithubRepo> findRepositoryByLanguageAndStars(String language, Integer minStars, String sort) {
        if(sort == null) {
            sort = "starsCount";
        }
        switch(RepositorySortingModel.valueOf(sort.toUpperCase())) {
            case UPDATED:
                return repoSearchRepository.findByProgrammingLanguageAndStarsCountGreaterThan(language,
                        minStars,
                        Sort.by(Sort.Direction.ASC, "updatedAt"));
            case FORKS:
                return repoSearchRepository.findByProgrammingLanguageAndStarsCountGreaterThan(language,
                        minStars,
                        Sort.by(Sort.Direction.ASC, "forksCount"));
            default:
                return repoSearchRepository.findByProgrammingLanguageAndStarsCountGreaterThan(language,
                        minStars,
                        Sort.by(Sort.Direction.ASC, "starsCount"));
        }
    }

    private List<GithubRepo> findRepositoryByLanguage(String language, String sort) {
        if(sort == null) {
            sort = "starsCount";
        }
        switch(RepositorySortingModel.valueOf(sort)) {
            case UPDATED:
                return repoSearchRepository.findByProgrammingLanguage(language,
                        Sort.by(Sort.Direction.ASC, "updatedAt"));
            case FORKS:
                return repoSearchRepository.findByProgrammingLanguage(language,
                        Sort.by(Sort.Direction.ASC, "forksCount"));
            default:
                return repoSearchRepository.findByProgrammingLanguage(language,
                        Sort.by(Sort.Direction.ASC, "starsCount"));
        }
    }
}
