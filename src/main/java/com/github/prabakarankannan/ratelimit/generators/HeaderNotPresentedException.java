package com.github.prabakarankannan.ratelimit.generators;

/**
 * should be thrown when the {@link #headerKey} not presented in
 * the Http servlet request header while generating the key.
 *
 * @author Prabakaran Kannan
 */
public class HeaderNotPresentedException extends RuntimeException {

    /**
     * The header key.
     */
    private final String headerKey;

    public HeaderNotPresentedException(String message, String headerKey) {
        super(message);
        this.headerKey = headerKey;
    }

    public String getHeaderKey() {
        return headerKey;
    }
}
