package io.reflectoring.demo.Repositories;

import io.reflectoring.demo.Entity.GithubRepo;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RepoSearchRepository extends JpaRepository<GithubRepo, Long> {
    public GithubRepo findByRepositoryId(Long repositoryId);
    public List<GithubRepo> findByProgrammingLanguage(String programmingLanguage, Sort sort);
    public List<GithubRepo> findByStarsCountGreaterThan(Integer starsCount, Sort sort);
    public List<GithubRepo> findByProgrammingLanguageAndStarsCountGreaterThan(String programmingLanguage, Integer starsCount, Sort sort);
}
