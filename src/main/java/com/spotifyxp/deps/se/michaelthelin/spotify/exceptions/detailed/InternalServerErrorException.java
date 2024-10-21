package com.spotifyxp.deps.se.michaelthelin.spotify.exceptions.detailed;

import java.io.IOException;

/**
 * You should never receive this error because our clever coders catch them all ... but if you are unlucky enough to get
 * one, please report it to us.
 */
public class InternalServerErrorException extends IOException {

    public InternalServerErrorException() {
        super();
    }

    public InternalServerErrorException(String message) {
        super(message);
    }

    public InternalServerErrorException(String message, Throwable cause) {
        super(message, cause);
    }

}
