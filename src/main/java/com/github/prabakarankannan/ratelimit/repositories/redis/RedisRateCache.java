package com.github.prabakarankannan.ratelimit.repositories.redis;

import org.springframework.util.ConcurrentReferenceHashMap;

import com.github.prabakarankannan.ratelimit.Rate;
import com.github.prabakarankannan.ratelimit.RateLimiter;
import com.github.prabakarankannan.ratelimit.RatePolicy;

import javax.annotation.Nonnull;
import java.lang.ref.WeakReference;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.ConcurrentMap;

import static org.springframework.util.ConcurrentReferenceHashMap.ReferenceType.WEAK;

/**
 * An implementation of {@link RateLimiter} to cache the rate limit data in redis.
 *
 * @author Prabakaran Kannan
 */
public class RedisRateCache implements RateLimiter {

    private final ConcurrentMap<WeakReference<String>, WeakReference<String>> lockMap = new ConcurrentReferenceHashMap<>(10, WEAK);

    /**
     * Used to persist and retrieve from to redis.
     */
    private final RedisRepository redisRepository;

    public RedisRateCache(RedisRepository redisRepository) {
        this.redisRepository = redisRepository;
    }

    /**
     * Finds the rate record from Redis by the given {@code key}, if does not exists then creates a new record
     * else checks the rate remaining value is greater than 0, decrease rate`s remaining and update item.
     *
     * @param ratePolicy Encapsulates the rate limit policy details.
     * @return Encapsulated rate details.
     */
    @Override
    public Rate consume(@Nonnull RatePolicy ratePolicy) {
        WeakReference<String> weakValue = new WeakReference<>(ratePolicy.getKey());
        lockMap.put(weakValue, weakValue);

        synchronized (lockMap.get(weakValue)) {
            Optional<RateHash> optionalRate = redisRepository.findById(ratePolicy.getKey());

            if (!optionalRate.isPresent()) {
                return createRateForFirstTime(ratePolicy);
            }

            RateHash rateHash = optionalRate.get();
            Rate rate = new Rate(
                    rateHash.getKey(),
                    rateHash.getExpiration(),
                    rateHash.getRemaining()
            );

            if (!rate.isExceed()) {
                rate.decrease();
                rateHash.setRemaining(rate.getRemaining());

                if (rate.isExceed() && ratePolicy.getBlockDuration() != null) {
                    final Instant blockedExpiration = Instant.now().plusSeconds(ratePolicy.getBlockDuration().getSeconds());
                    rateHash.setExpiration(blockedExpiration);
                }

                redisRepository.save(rateHash);
            }

            return rate;
        }
    }

    private Rate createRateForFirstTime(RatePolicy ratePolicy) {
        Instant expiration = Instant.now().plusSeconds(ratePolicy.getDuration().getSeconds());
        RateHash rateHash = new RateHash(ratePolicy.getKey(), expiration, ratePolicy.getCount() - 1);
        redisRepository.save(rateHash);

        return new Rate(ratePolicy.getKey(), expiration, rateHash.getRemaining());
    }
}
