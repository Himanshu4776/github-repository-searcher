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
import java.util.Map;
import java.util.stream.Collectors;

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
        List<Long> repositoryIds = repositories.stream()
                .map(GithubRepo::getRepositoryId)
                .collect(Collectors.toList());

        // Fetch existing repositories to avoid duplicates
        List<GithubRepo> existingRepositories = repoSearchRepository.findByRepositoryIdIn(repositoryIds);

        // Create a map for quick lookup
        Map<Long, GithubRepo> existingRepoMap = existingRepositories.stream()
                .collect(Collectors.toMap(GithubRepo::getRepositoryId, repo -> repo));

        List<GithubRepo> uniqueRepositories = new ArrayList<>();

        for (GithubRepo entity : repositories) {
            if (existingRepoMap.containsKey(entity.getRepositoryId())) {
                GithubRepo existingRepo = existingRepoMap.get(entity.getRepositoryId());
                updateRepositoryData(existingRepo, entity); // Update method to copy new data
                uniqueRepositories.add(existingRepo);
            } else {
                uniqueRepositories.add(entity);
            }
        }

        // Save all new and updated repositories
        return repoSearchRepository.saveAll(uniqueRepositories);
    }

    private void updateRepositoryData(GithubRepo existingRepo, GithubRepo newRepo) {
        existingRepo.setName(newRepo.getName());
        existingRepo.setDescription(newRepo.getDescription());
        existingRepo.setOwnerName(newRepo.getOwnerName());
        existingRepo.setProgrammingLanguage(newRepo.getProgrammingLanguage());
        existingRepo.setStarsCount(newRepo.getStarsCount());
        existingRepo.setForksCount(newRepo.getForksCount());
        existingRepo.setUpdatedAt(newRepo.getUpdatedAt());
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

    public List<GithubRepo> getRepositoriesByRepositoryIds(List<Long> repositoryIds) {
        return repoSearchRepository.findByRepositoryIdIn(repositoryIds);
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
        switch(RepositorySortingModel.valueOf(sort.toUpperCase())) {
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
        switch(RepositorySortingModel.valueOf(sort.toUpperCase())) {
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
