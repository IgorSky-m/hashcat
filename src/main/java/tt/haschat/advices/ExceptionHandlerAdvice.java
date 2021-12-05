package tt.haschat.advices;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import tt.haschat.exceptions.CustomApplicationException;
import tt.haschat.exceptions.CustomValidationException;
import tt.haschat.exceptions.EntityNotFound;

import java.util.Collections;
import java.util.Map;

/**
 * Совет для перехвата ошибок и вывода нужного текста и статуса
 */
@ControllerAdvice
@Slf4j
public class ExceptionHandlerAdvice {

    private final MessageSource messageSource;

    public ExceptionHandlerAdvice(MessageSource messageSource) {
        this.messageSource = messageSource;
    }


    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(CustomValidationException.class)
    public Map<String, String> validationExceptionHandler(CustomValidationException ex) {
        if (ex.getStructuredMessages() == null || ex.getStructuredMessages().isEmpty()){
            String msg = messageSource.getMessage("validation.error.default", null, LocaleContextHolder.getLocale());
            return Collections.singletonMap("error_msg", msg);
        }
        return ex.getStructuredMessages();
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(IllegalArgumentException.class)
    public String illegalArgumentException(IllegalArgumentException ex) {
        if (ex.getMessage() == null || ex.getMessage().isEmpty()){
            return messageSource.getMessage("error.bad.request.default", null, LocaleContextHolder.getLocale());
        }
        return ex.getMessage();
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(EntityNotFound.class)
    public void MethodArgumentNotValidExceptionHandler() {}


    @ResponseBody
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(CustomApplicationException.class)
    public String MethodArgumentNotValidExceptionHandler(CustomApplicationException ex) {
        String msg;
        if (ex.getMessage() == null || ex.getMessage().isEmpty()){
            msg = messageSource.getMessage("error.default", null, LocaleContextHolder.getLocale());
            return msg;
        }
        return ex.getMessage();
    }

    //Для отлова и логирования ошибок, которые мы по каким-либо причинам не словили ранее
    @ResponseBody
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public String unknownExceptionHandler(Exception ex) {
        String msg = messageSource.getMessage("error.default", null, LocaleContextHolder.getLocale());
        log.error(msg, ex);
        return msg;
    }







}
