package com.github.prabakarankannan.ratelimit;

import javax.servlet.http.HttpServletRequest;

import com.github.prabakarankannan.ratelimit.conf.properties.RateLimitProperties.Policy;

/**
 * Defines a contract to generate a key from the Http servlet request.
 *
 * @author Prabakaran Kannan
 */
public interface RateLimitKeyGenerator {

    /**
     * Returns the generated key.
     *
     * @param servletRequest Encapsulates the http servlet request.
     * @param policy         Encapsulates the rate limit policy properties.
     * @return Generated key.
     */
    String generateKey(HttpServletRequest servletRequest, Policy policy);
}
