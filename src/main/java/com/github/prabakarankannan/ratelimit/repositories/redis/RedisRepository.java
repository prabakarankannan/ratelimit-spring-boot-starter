package com.github.prabakarankannan.ratelimit.repositories.redis;

import org.springframework.data.repository.CrudRepository;

/**
 * Provides data access operations over {@link RateHash} entity.
 *
 * @author Sajjad Alipour
 */
public interface RedisRepository extends CrudRepository<RateHash, String> {}
