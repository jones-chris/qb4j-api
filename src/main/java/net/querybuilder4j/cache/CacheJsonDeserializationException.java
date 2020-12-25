package net.querybuilder4j.cache;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR, reason = "Unable to deserialize cache json string to requested object")
public class CacheJsonDeserializationException extends RuntimeException {

    public CacheJsonDeserializationException(String message) {
        super(message);
    }

    public CacheJsonDeserializationException(Throwable e) {
        super(e);
    }

}
