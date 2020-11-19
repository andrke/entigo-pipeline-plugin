package io.jenkins.plugins.entigo.pipeline.rest;

/**
 * Author: Märt Erlenheim
 * Date: 2020-09-18
 */
public class NotFoundException extends ResponseException {

    public NotFoundException(String message) {
        super(message);
    }

    public NotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
