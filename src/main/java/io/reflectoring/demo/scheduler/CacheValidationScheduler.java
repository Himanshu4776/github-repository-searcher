package io.reflectoring.demo.scheduler;

import io.reflectoring.demo.Service.GithubSearchService;
import io.reflectoring.demo.cache.AppCache;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDateTime;

@Configuration
public class CacheValidationScheduler {
    private final AppCache appCache;
    private final GithubSearchService githubSearchService;

    private static final Logger logger = LoggerFactory.getLogger(CacheValidationScheduler.class);

    public CacheValidationScheduler(AppCache appCache, GithubSearchService githubSearchService) {
        this.appCache = appCache;
        this.githubSearchService = githubSearchService;
    }

    @Scheduled(fixedRate = 60000*60)
    public void validateCache() {
        appCache.cacheSize = githubSearchService.getRemainingRequests();
        logger.info("Currently rate limited. Reset scheduled at: {}", LocalDateTime.now());
    }

    @Scheduled(cron = "0 0/10 * ? * *")
    @PostConstruct
    public void appCacheInit() {
        appCache.cacheSize = githubSearchService.getRemainingRequests();
        logger.info("Cache initialized. Current cache size: {}", appCache.cacheSize);
    }
}
