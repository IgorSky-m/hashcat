package tt.haschat.validators.api;

/**
 * Контракт валидатора
 * @param <T> валидируемая суцщность
 */
public interface IValidator<T> {

    /**
     * Метод валидации
     * @param obj валидируемая сущность
     */
    void validate(T obj);
}
