package com.github.prabakarankannan.ratelimit.conf.properties;

/**
 * Represents the all supported repositories for storage.
 *
 * @author Prabakaran Kannan
 */
public enum RateLimitRepositoryKey {

    /**
     * Uses the memory as a data storage.
     */
    IN_MEMORY,

    /**
     * Uses the redis as a data storage.
     */
    REDIS
}
