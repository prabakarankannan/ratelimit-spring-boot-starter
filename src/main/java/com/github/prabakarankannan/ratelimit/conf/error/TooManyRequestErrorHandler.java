package com.github.prabakarankannan.ratelimit.conf.error;

import javax.servlet.http.HttpServletResponse;

import com.github.prabakarankannan.ratelimit.Rate;

import java.io.IOException;

/**
 * In order to handle too many request error response.
 *
 * @author Sajjad Alipour
 */
public interface TooManyRequestErrorHandler {

    /**
     * @param httpServletResponse Encapsulates the http server response detail.
     * @param rate                Encapsulates the rate limit details.
     * @throws IOException when to write to response body.
     */
    void handle(HttpServletResponse httpServletResponse, Rate rate) throws IOException;
}
