package io.jenkins.plugins.entigo.pipeline.rest;

import java.io.IOException;

/**
 * Author: Märt Erlenheim
 * Date: 2020-09-07
 */
public class ClientException extends IOException {

    private static final long serialVersionUID = 1916610580737297383L;

    public ClientException(String message) {
        super(message);
    }

    public ClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
