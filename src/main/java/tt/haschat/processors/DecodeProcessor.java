package tt.haschat.processors;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import tt.haschat.config.AsyncConfig;
import tt.haschat.dto.Request;
import tt.haschat.dto.Response;
import tt.haschat.dto.api.State;
import tt.haschat.exceptions.EntityNotFound;
import tt.haschat.repositories.api.IRequestRepository;
import tt.haschat.services.api.IMd5DecoderService;
import tt.haschat.services.api.IResponseService;

import java.util.Date;
import java.util.List;
import java.util.concurrent.Flow;

@Slf4j
@Service
public class DecodeProcessor implements Flow.Subscriber<Request> {

    private final MessageSource messageSource;
    private final IRequestRepository requestRepository;
    private final IResponseService responseService;
    private final IMd5DecoderService md5DecoderService;
    private final TransactionTemplate transactionTemplate;


    private Flow.Subscription subscription = null;

    public DecodeProcessor(
            IRequestRepository requestRepository,
            IResponseService responseService,
            IMd5DecoderService md5DecoderService,
            TransactionTemplate transactionTemplate,
            MessageSource messageSource

    ) {
        this.requestRepository = requestRepository;
        this.responseService = responseService;
        this.md5DecoderService = md5DecoderService;
        this.transactionTemplate = transactionTemplate;
        this.messageSource = messageSource;
    }


    @Async(AsyncConfig.ASYNC_EXECUTOR_BEAN_NAME)
    public void process(Request request) {
        log.info("Decode start in thread: " +  Thread.currentThread().getName());
        try {
            List<Response> decodedResponses = this.md5DecoderService.decode(request.getHashes());
            transactionTemplate.execute((e) -> {
                this.responseService.saveResponses(decodedResponses);
                Request readReq = this.requestRepository.findById(request.getUuid()).orElseThrow(EntityNotFound::new);
                readReq.setState(State.COMPLETE);
                readReq.setDtUpdate(new Date());
                return this.requestRepository.save(readReq);
            });
        } catch (EntityNotFound e) {
            log.error(messageSource.getMessage("decode.request.not.found", null, LocaleContextHolder.getLocale()));

        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            transactionTemplate.execute((tm) -> {
                Request readReq = this.requestRepository.findById(request.getUuid()).orElse(null);
                if (readReq == null) {
                    log.error(messageSource.getMessage("decode.request.not.found", null, LocaleContextHolder.getLocale()));
                    return null;
                }
                //TODO подумать и добавить retry count , если n < retry count -> state = RETRY, иначе -> FAIL. Сделать джобу для RETRY
                readReq.setState(State.FAIL);
                readReq.setDtUpdate(new Date());
                return this.requestRepository.save(readReq);
            });
        }

    }

    @Override
    public void onSubscribe(Flow.Subscription subscription) {
        this.subscription = subscription;
        this.subscription.request(1);
    }

    @Override
    public void onNext(Request request) {
            this.process(request);
            this.subscription.request(1);
    }

    @Override
    public void onError(Throwable throwable) {
        log.error(throwable.getMessage());
    }

    @Override
    public void onComplete() {
        log.warn("requests ended");
    }
}
