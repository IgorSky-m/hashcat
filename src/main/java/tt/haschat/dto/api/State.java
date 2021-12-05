package tt.haschat.dto.api;

/**
 * Статусы запроса
 */
public enum State {
    WAIT(false), //ожидает подтверждения
    IN_PROGRESS(false), // попытка обработки
    COMPLETE(true), //Обработано успешно
    FAIL(true); //Обработано неуспешно

    private final boolean finishState;

    State(boolean finishState) {
        this.finishState = finishState;
    }

    public boolean isFinishState() {
        return finishState;
    }
}
