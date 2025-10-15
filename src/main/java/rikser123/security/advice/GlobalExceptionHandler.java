package rikser123.security.advice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import rikser123.security.dto.response.RikserResponseItem;
import rikser123.security.utils.RikserResponseUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public RikserResponseItem handleValidationException(MethodArgumentNotValidException exception) {
        var errors = new HashMap<String, List<String>>();

        exception.getBindingResult().getAllErrors().forEach(error -> {
            var fieldName = ((FieldError) error).getField();
            var message = error.getDefaultMessage();

            var fieldLastPart = getFieldLastPart(fieldName);
            errors.computeIfPresent(fieldLastPart, (key, value) -> {
                value.add(message);
                return value;
            });
            errors.putIfAbsent(fieldLastPart, new ArrayList<>(List.of(message)));
        });

        return RikserResponseUtils.createResponse(errors, null);
    }

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public RikserResponseItem handleRuntimeException(RuntimeException exception) {
        log.error("Internal server error", exception);
        return RikserResponseUtils.createResponse((exception.getMessage()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public RikserResponseItem handleAccessDeniedException(AccessDeniedException exception) {
        log.warn("access forbidden", exception);
        return RikserResponseUtils.createResponse("Доступ к запрашиваемому ресурсу запрещен");
    }

    private static String getFieldLastPart(String field) {
        var fieldParts = field.split("\\.");
        return fieldParts[fieldParts.length - 1];
    }
}
