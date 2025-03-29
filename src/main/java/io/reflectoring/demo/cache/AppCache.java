package io.reflectoring.demo.cache;

import io.reflectoring.demo.Service.GithubSearchService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class AppCache {
    public Map<Integer, Integer> appCache;
    public int cacheSize;

    @PostConstruct
    public void init(){
        appCache = new HashMap<>();
        cacheSize = 0;
    }
}
