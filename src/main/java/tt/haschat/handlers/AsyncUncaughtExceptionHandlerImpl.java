package tt.haschat.handlers;

import com.sun.istack.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Handler для отлова и обработки необработанных исключений при асинхронном выполнении
 */
@Slf4j
public class AsyncUncaughtExceptionHandlerImpl implements AsyncUncaughtExceptionHandler {

    @Override
    public void handleUncaughtException(Throwable throwable, Method method, Object... objects) {
        log.error(
                String.format("ASYNC METHOD THROWS EXCEPTION. ERROR MSG: %s. METHOD NAME: %s. PARAMS: %s",
                        throwable.getMessage(),
                        method.getName(),
                        Arrays.stream(objects)
                                .map(Object::toString)
                                .collect(Collectors.joining(",")))
        );
    }
}
