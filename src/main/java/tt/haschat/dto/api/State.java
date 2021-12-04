package tt.haschat.dto.api;

/**
 * Статусы запроса
 */
public enum State {

    IN_PROGRESS(false), // В процессе обработки
    COMPLETE(true), //Завершено успешно
    FAIL(true); //Завершено неуспешно

    private final boolean finishState;

    State(boolean finishState) {
        this.finishState = finishState;
    }

    public boolean isFinishState() {
        return finishState;
    }
}
