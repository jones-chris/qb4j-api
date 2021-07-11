package net.querybuilder4j.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Not found in cache")
public class CacheMissException extends RuntimeException {

    public CacheMissException() {}

    public CacheMissException(String message) {
        super(message);
    }

}
