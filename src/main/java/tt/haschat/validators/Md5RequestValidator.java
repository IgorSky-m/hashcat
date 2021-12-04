package tt.haschat.validators;

import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import tt.haschat.config.properties.Md5Properties;
import tt.haschat.dto.Request;
import tt.haschat.exceptions.CustomValidationException;
import tt.haschat.validators.api.IValidator;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Реализация валидатора запроса
 */
@Component
public class Md5RequestValidator implements IValidator<Request> {
    private static final String REQUEST_EMAIL_FIELD_NAME = "email";
    private static final String REQUEST_HASHES_FIELD_NAME = "hashes";

    private final Md5Properties md5Properties;
    private final MessageSource messageSource;
    private final Pattern hashCheckPattern;

    public Md5RequestValidator(MessageSource messageSource, Md5Properties md5Properties)  {
        this.messageSource = messageSource;
        this.md5Properties = md5Properties;
        this.hashCheckPattern = Pattern.compile(md5Properties.getValidationPattern());
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void validate(Request request){
        Map<String, String> errorMessages = new HashMap<>();
        if (request.getEmail() == null) {
            errorMessages.put(REQUEST_EMAIL_FIELD_NAME, messageSource.getMessage("validation.error.email.empty", null, LocaleContextHolder.getLocale()));

        } else if (!EmailValidator.getInstance().isValid(request.getEmail())) {
            errorMessages.put(REQUEST_EMAIL_FIELD_NAME, messageSource.getMessage("validation.error.email.invalid", null, LocaleContextHolder.getLocale()));
        }

        if (request.getHashes() == null || request.getHashes().isEmpty()){
            errorMessages.put(REQUEST_HASHES_FIELD_NAME, messageSource.getMessage("validation.error.hashes.empty", null, LocaleContextHolder.getLocale()));
        }

        String invalidHashes = request.getHashes().stream().filter(e -> !this.hashCheckPattern.matcher(e).matches()).collect(Collectors.joining(", "));
        if (!invalidHashes.isEmpty()) {
            errorMessages.put(REQUEST_HASHES_FIELD_NAME, messageSource.getMessage("validation.error.hashes.invalid", new Object[]{this.md5Properties.getType(), invalidHashes}, LocaleContextHolder.getLocale()));
        }
        if (!errorMessages.isEmpty()) {
            throw new CustomValidationException(errorMessages);
        }
    }
}
