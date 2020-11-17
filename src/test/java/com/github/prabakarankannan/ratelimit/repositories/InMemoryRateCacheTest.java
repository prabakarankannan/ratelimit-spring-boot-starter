package com.github.prabakarankannan.ratelimit.repositories;

import org.junit.jupiter.api.Test;

import com.github.prabakarankannan.ratelimit.Rate;
import com.github.prabakarankannan.ratelimit.RatePolicy;
import com.github.prabakarankannan.ratelimit.repositories.InMemoryRateCache;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for {@link InMemoryRateCache}.
 *
 * @author Prabakaran Kannan
 */
class InMemoryRateCacheTest {

    @Test
    void consume_CreateRateForFirstTime_TheRateRemainingValueShouldBeEqualWIth2() {
        InMemoryRateCache inMemoryRateCache = new InMemoryRateCache();
        RatePolicy ratePolicy = new RatePolicy("test", Duration.ofDays(1), 3, null);
        Rate rate = inMemoryRateCache.consume(ratePolicy);

        assertEquals(2, rate.getRemaining());
        assertEquals("test", rate.getKey());
    }

    @Test
    void consume_WhenRateRecordExpired_ShouldCreateNewRate_TheRateRemainingValueShouldBeEqualWIth2() throws InterruptedException {
        InMemoryRateCache inMemoryRateCache = new InMemoryRateCache();
        RatePolicy ratePolicy = new RatePolicy("test", Duration.ofNanos(1), 3, null);
        inMemoryRateCache.consume(ratePolicy);
        TimeUnit.SECONDS.sleep(2);
        Rate rate = inMemoryRateCache.consume(ratePolicy);

        assertEquals(2, rate.getRemaining());
        assertEquals("test", rate.getKey());
    }

    @Test
    void consume_WhenDoesNotExceed_TheRateRemainingValueShouldBeEqualWIth1() {
        InMemoryRateCache inMemoryRateCache = new InMemoryRateCache();
        RatePolicy ratePolicy = new RatePolicy("test", Duration.ofDays(1), 3, null);
        inMemoryRateCache.consume(ratePolicy);
        Rate rate = inMemoryRateCache.consume(ratePolicy);

        assertEquals(1, rate.getRemaining());
        assertEquals("test", rate.getKey());
    }

    @Test
    void consume_WhenExceed_ShouldIncreaseExpiration2MinAndRateBe0() {
        InMemoryRateCache inMemoryRateCache = new InMemoryRateCache();
        RatePolicy ratePolicy = new RatePolicy("test", Duration.ofMinutes(1), 1, Duration.ofMinutes(2));
        inMemoryRateCache.consume(ratePolicy);
        Rate rate = inMemoryRateCache.consume(ratePolicy);

        long expirationDiff = rate.getExpiration().getEpochSecond() - Instant.now().getEpochSecond();

        assertEquals(120, expirationDiff);
        assertEquals(-1, rate.getRemaining());
    }
}
