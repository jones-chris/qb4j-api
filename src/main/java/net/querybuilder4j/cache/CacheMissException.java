package net.querybuilder4j.cache;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Not found in cache")
public class CacheMissException extends RuntimeException {

    public CacheMissException(String message) {
        super(message);
    }

    public CacheMissException(Throwable e) {
        super(e);
    }

}
