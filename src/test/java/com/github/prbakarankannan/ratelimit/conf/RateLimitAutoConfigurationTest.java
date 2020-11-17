package com.github.prbakarankannan.ratelimit.conf;

import com.github.prabakarankannan.ratelimit.Rate;
import com.github.prabakarankannan.ratelimit.RateLimiter;
import com.github.prabakarankannan.ratelimit.RatePolicy;
import com.github.prabakarankannan.ratelimit.conf.RateLimitAutoConfiguration;
import com.github.prabakarankannan.ratelimit.conf.error.TooManyRequestErrorHandler;
import com.github.prabakarankannan.ratelimit.conf.filter.RateLimitFilter;
import com.github.prabakarankannan.ratelimit.repositories.InMemoryRateCache;
import com.github.prabakarankannan.ratelimit.repositories.redis.RedisRateCache;
import com.github.prbakarankannan.ratelimit.conf.RateLimitAutoConfigurationTest.TestAutoConfig.CustomRateLimiter;
import com.github.prbakarankannan.ratelimit.conf.RateLimitAutoConfigurationTest.TestAutoConfig.CustomTooManyRequestErrorHandler;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.context.annotation.Bean;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for {@link RateLimitAutoConfiguration}.
 *
 * @author Prabakaran Kannan
 */
class RateLimitAutoConfigurationTest {

    private final WebApplicationContextRunner contextRunner = new WebApplicationContextRunner()
            .withBean(ServerProperties.class)
            .withConfiguration(AutoConfigurations.of(RateLimitAutoConfiguration.class));

    @Test
    void whenRateLimitEnabledIsFalse_ShouldNotAutoConfigure() {
        contextRunner.withPropertyValues("rate-limit.enabled=false").run(context -> {
            assertThrows(NoSuchBeanDefinitionException.class, () -> context.getBean(RateLimiter.class));
            assertThrows(NoSuchBeanDefinitionException.class, () -> context.getBean(RateLimitFilter.class));
        });
    }

    @Test
    void whenRateLimitEnabledAndRepositoryIsInMemory_TheRatLimiterShouldBeInstanceOfInMemoryRateCache() {
        contextRunner.withPropertyValues(
                "rate-limit.enabled=true",
                "rate-limit.repository=IN_MEMORY",
                "rate-limit.policies[0].duration=5s",
                "rate-limit.policies[0].count=3",
                "rate-limit.policies[0].keyGenerator= BY_IP",
                "rate-limit.policies[0].block.duration= 1d",
                "rate-limit.policies[0].routes[0].uri=/**",
                "rate-limit.policies[0].routes[0].method=GET",
                "rate-limit.keyGenerators[0].name=BY_IP",
                "rate-limit.keyGenerators[0].generator=com.github.prabakarankannan.ratelimit.generators.HeaderBasedKeyGenerator",
                "rate-limit.keyGenerators[0].params[0]=X-FORWARD-FOR"
        ).run(context -> assertEquals(InMemoryRateCache.class, context.getBean(RateLimiter.class).getClass()));
    }

    @Test
    void whenRateLimitEnabledAndRepositoryIsRedis_TheRatLimiterShouldBeInstanceOfRedisRateCache() {
        contextRunner.withPropertyValues(
                "rate-limit.enabled=true",
                "rate-limit.repository=REDIS",
                "rate-limit.policies[0].duration=5s",
                "rate-limit.policies[0].count=3",
                "rate-limit.policies[0].keyGenerator= BY_IP",
                "rate-limit.policies[0].block.duration= 1d",
                "rate-limit.policies[0].routes[0].uri=/**",
                "rate-limit.policies[0].routes[0].method=GET",
                "rate-limit.keyGenerators[0].name=BY_IP",
                "rate-limit.keyGenerators[0].generator=com.github.prabakarankannan.ratelimit.generators.HeaderBasedKeyGenerator",
                "rate-limit.keyGenerators[0].params[0]=X-FORWARD-FOR"
        ).withConfiguration(AutoConfigurations.of(RedisAutoConfiguration.class))
                .run(context -> assertEquals(RedisRateCache.class, context.getBean(RateLimiter.class).getClass()));
    }

    @Test
    void whenExistsACustomRateLimiterImplementationBeanAndRepositoryPropertyIsInMemory_ShouldNotRegisterBeanOfInMemoryRateCache() {
        contextRunner.withPropertyValues(
                "rate-limit.enabled=true",
                "rate-limit.repository=IN_MEMORY",
                "rate-limit.policies[0].duration=5s",
                "rate-limit.policies[0].count=3",
                "rate-limit.policies[0].keyGenerator= BY_IP",
                "rate-limit.policies[0].block.duration= 1d",
                "rate-limit.policies[0].routes[0].uri=/**",
                "rate-limit.policies[0].routes[0].method=GET",
                "rate-limit.keyGenerators[0].name=BY_IP",
                "rate-limit.keyGenerators[0].generator=com.github.prabakarankannan.ratelimit.generators.HeaderBasedKeyGenerator",
                "rate-limit.keyGenerators[0].params[0]=X-FORWARD-FOR"
        ).withUserConfiguration(TestAutoConfig.class)
                .run(context -> assertEquals(CustomRateLimiter.class, context.getBean(RateLimiter.class).getClass()));
    }

    @Test
    void whenExistsACustomRateLimiterImplementationBeanAndRepositoryPropertyIsRedis_ShouldNotRegisterBeanOfRedisRateCache() {
        contextRunner.withPropertyValues(
                "rate-limit.enabled=true",
                "rate-limit.repository=REDIS",
                "rate-limit.policies[0].duration=5s",
                "rate-limit.policies[0].count=3",
                "rate-limit.policies[0].keyGenerator= BY_IP",
                "rate-limit.policies[0].block.duration= 1d",
                "rate-limit.policies[0].routes[0].uri=/**",
                "rate-limit.policies[0].routes[0].method=GET",
                "rate-limit.keyGenerators[0].name=BY_IP",
                "rate-limit.keyGenerators[0].generator=com.github.prabakarankannan.ratelimit.generators.HeaderBasedKeyGenerator",
                "rate-limit.keyGenerators[0].params[0]=X-FORWARD-FOR"
        ).withUserConfiguration(TestAutoConfig.class)
                .run(context -> assertEquals(CustomRateLimiter.class, context.getBean(RateLimiter.class).getClass()));
    }

    @Test
    void whenExistsCustomTooManyRequestErrorHandlerBean_ShouldNotRegisterBeanOfTooManyRequestErrorHandler() {
        contextRunner.withPropertyValues(
                "rate-limit.enabled=true",
                "rate-limit.repository=IN_MEMORY",
                "rate-limit.policies[0].duration=5s",
                "rate-limit.policies[0].count=3",
                "rate-limit.policies[0].keyGenerator= BY_IP",
                "rate-limit.policies[0].block.duration= 1d",
                "rate-limit.policies[0].routes[0].uri=/**",
                "rate-limit.policies[0].routes[0].method=GET",
                "rate-limit.keyGenerators[0].name=BY_IP",
                "rate-limit.keyGenerators[0].generator=com.github.prabakarankannan.ratelimit.generators.HeaderBasedKeyGenerator",
                "rate-limit.keyGenerators[0].params[0]=X-FORWARD-FOR"
        ).withUserConfiguration(TestAutoConfig.class)
                .run(context -> assertEquals(CustomTooManyRequestErrorHandler.class, context.getBean(TooManyRequestErrorHandler.class).getClass()));
    }

    static class TestAutoConfig {

        @Bean
        public RateLimiter rateLimiter() {
            return new CustomRateLimiter();
        }

        @Bean
        public TooManyRequestErrorHandler tooManyRequestErrorHandler() {
            return new CustomTooManyRequestErrorHandler();
        }

        static class CustomTooManyRequestErrorHandler implements TooManyRequestErrorHandler {
            @Override
            public void handle(HttpServletResponse httpServletResponse, Rate rate) {

            }
        }

        static class CustomRateLimiter implements RateLimiter {
            @Override
            public Rate consume(@Nonnull RatePolicy ratePolicy) {
                return null;
            }
        }
    }
}