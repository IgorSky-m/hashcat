package tt.haschat.exceptions;

/**
 * Кастом ошибка для декодера
 */
public class DecodeException extends RuntimeException{
    public DecodeException() {
        super();
    }

    public DecodeException(String message) {
        super(message);
    }

    public DecodeException(String message, Throwable cause) {
        super(message, cause);
    }

    public DecodeException(Throwable cause) {
        super(cause);
    }

    protected DecodeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
