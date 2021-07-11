package net.querybuilder4j.exceptions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Map;

@ControllerAdvice
@Slf4j
public class ExceptionMapper {

    @ExceptionHandler({
            CriterionColumnDataTypeAndFilterMismatchException.class,
            UncleanSqlException.class,
            DatabaseTypeNotRecognizedException.class,
            SqlTypeNotRecognizedException.class,
            CacheTypeNotRecognizedException.class,
            SqlTypeNotRecognizedException.class,
            UncleanSqlException.class,
            IllegalArgumentException.class,
            IllegalStateException.class
    })
    public ResponseEntity<?> handleExceptionsThatShouldReturn400Response(RuntimeException ex) {
        log.error("An exception returning a 400 response occurred ", ex);

        return ResponseEntity.badRequest()
                .contentType(MediaType.APPLICATION_JSON)
                .body(
                        Map.of(
                                "message",
                                ex.getMessage() == null ? "" : ex.getMessage()
                        )
                );
    }

    @ExceptionHandler({
            JsonDeserializationException.class,
            JsonSerializationException.class,
            CacheRefreshException.class,
            CacheMissException.class,
            QueryTemplateNotFoundException.class,
            QueryFailureException.class
    })
    public ResponseEntity<?> handleExceptionsThatShouldReturn500Response(RuntimeException ex) {
        log.error("An exception returning a 500 response occurred", ex);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .contentType(MediaType.APPLICATION_JSON)
                .body(
                        Map.of(
                                "message",
                                ex.getMessage() == null ? "" : ex.getMessage()
                        )
                );
    }

}
