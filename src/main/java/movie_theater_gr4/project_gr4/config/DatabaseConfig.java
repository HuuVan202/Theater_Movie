package movie_theater_gr4.project_gr4.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;

import java.util.Arrays;

/**
 * Cấu hình database và cache cho ứng dụng
 */
@Configuration
@EnableCaching
public class DatabaseConfig {

    /**
     * Cấu hình cache manager để quản lý tất cả các cache trong ứng dụng.
     * @return CacheManager instance
     */
    @Bean
    @ConditionalOnMissingBean
    public CacheManager cacheManager() {
        ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager();
        cacheManager.setCacheNames(Arrays.asList(
            // Employee caches
            "employees", "allEmployees", "employeeSearch",
            // Movie caches
            "movieDetail", "movieDetailComplete", "movieDetailFull",
            "nowShowingMovies", "movieDetails", "nowShowingMoviesPage", "comingSoonMoviesPage",
            // User and account caches
            "accounts",
            // Additional caches
            "cinemaRooms", "schedules", "promotions", "versions"
        ));
        return cacheManager;
    }
}
