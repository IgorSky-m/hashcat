package tt.haschat.exceptions;

import lombok.Getter;

import java.util.Collections;
import java.util.Map;

/**
 * Кастомная ошибка валидации
 */
@Getter
public class CustomValidationException extends RuntimeException{

    Map<String, String> structuredMessages;

    public CustomValidationException(Map<String, String> exceptionStructMap){
        this.structuredMessages = exceptionStructMap;
    }

    public CustomValidationException(String field, String message) {
        this.structuredMessages = Collections.singletonMap(field, message);
    }

    public CustomValidationException(String field, String message, Throwable cause) {
        super(message, cause);
        this.structuredMessages = Collections.singletonMap(field, message);
    }

    public CustomValidationException() {
        super();
    }

    public CustomValidationException(String message) {
        super(message);
    }

    public CustomValidationException(String message, Throwable cause) {
        super(message, cause);
    }

    public CustomValidationException(Throwable cause) {
        super(cause);
    }

    protected CustomValidationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }


}
