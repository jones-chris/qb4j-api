package net.querybuilder4j.exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.util.Map;

@ControllerAdvice
public class ExceptionMapper {

    private final Logger LOG = LoggerFactory.getLogger(this.getClass());

    @ExceptionHandler({ CriterionColumnDataTypeAndFilterMismatchException.class, UncleanSqlException.class,
            DatabaseTypeNotRecognizedException.class, SqlTypeNotRecognizedException.class })
    public ResponseEntity<?> handleExceptionsThatShouldReturn400Response(RuntimeException ex, WebRequest request) {
        LOG.error(ex.getMessage());

        return ResponseEntity.badRequest()
                .header("Content-Type", "application/json")
                .body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler({ JsonDeserializationException.class })
    public ResponseEntity<?> handleExceptionsThatShouldReturn500Response(RuntimeException ex, WebRequest request) {
        LOG.error(ex.getMessage());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .header("Content-Type", "application/json")
                .body(Map.of("message", ex.getMessage()));
    }

}
