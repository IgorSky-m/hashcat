package tt.haschat.services;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import tt.haschat.dto.Request;
import tt.haschat.dto.api.State;
import tt.haschat.exceptions.CustomApplicationException;
import tt.haschat.exceptions.EntityNotFound;
import tt.haschat.repositories.api.IRequestRepository;
import tt.haschat.services.api.IMd5DecoderService;
import tt.haschat.services.api.IRequestService;
import tt.haschat.services.api.IResponseService;
import tt.haschat.validators.api.IValidator;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class RequestService implements IRequestService {

    private static final Logger logger = LoggerFactory.getLogger(RequestService.class);

    private final IRequestRepository requestRepository;
    private final IResponseService responseService;
    private final IValidator<Request> validator;
    private final MessageSource messageSource;
    private final IMd5DecoderService md5DecoderService;

    private final TransactionTemplate transactionTemplate;


    public RequestService(
            IRequestRepository requestRepository,
            IResponseService responseService,
            IValidator<Request> validator,
            MessageSource messageSource,
            IMd5DecoderService md5DecoderService,
            TransactionTemplate transactionTemplate
    ){
        this.requestRepository = requestRepository;
        this.responseService = responseService;
        this.validator = validator;
        this.messageSource = messageSource;
        this.md5DecoderService = md5DecoderService;
        this.transactionTemplate = transactionTemplate;
    }

    @Transactional
    public Request createNewRequest(Request request) {
        request.setUuid(UUID.randomUUID());
        request.setDtCreate(new Date());
        request.setDtUpdate(request.getDtCreate());
        request.setState(State.IN_PROGRESS);
        this.validator.validate(request);
        try {
            Request savedRequest = this.requestRepository.save(request);
            this.decodeAsync(savedRequest);
            return request;
        } catch (Exception e) {
            String msg = messageSource.getMessage("error.crud.create", null, LocaleContextHolder.getLocale());
            logger.error(msg,e);
            throw new CustomApplicationException(msg);
        }
    }

    @Transactional(readOnly = true)
    @Override
    public Request getOneById(UUID uuid) {
        try {
            Request request = this.requestRepository.findById(uuid).orElseThrow(EntityNotFound::new);
            if (request.getState().isFinishState()) {
                request.setResponses(this.responseService.getAllByHashList(request.getHashes()));
            }
            return request;
        } catch (EntityNotFound e) {
            throw e;
        } catch (Exception e){
            String msg = messageSource.getMessage("error.crud.read", null, LocaleContextHolder.getLocale());
            logger.error(msg,e);
            throw new CustomApplicationException(msg);
        }

    }

    @Transactional(readOnly = true)
    @Override
    public List<Request> getAllByEmail(String email) {
        try {
            return this.requestRepository.findAllByEmail(email);
        } catch (Exception e){
            String msg = messageSource.getMessage("error.crud.read", null, LocaleContextHolder.getLocale());
            logger.error(msg,e);
            throw new CustomApplicationException(msg);
        }
    }

    @Transactional(readOnly = true)
    @Override
    public List<Request> getAll() {
        try {
            return this.requestRepository.findAll();
        } catch (Exception e) {
            String msg = messageSource.getMessage("error.crud.read", null, LocaleContextHolder.getLocale());
            logger.error(msg, e);
            throw new CustomApplicationException(msg);
        }
    }


    private void decodeAsync(Request request){
        CompletableFuture
                .supplyAsync(() -> {
                    assert request != null;
                    return this.md5DecoderService.decode(request.getHashes());
                })
                .thenAccept((r) ->
                        transactionTemplate.execute((e) -> {
                            this.responseService.saveResponses(r);
                            Request readReq = this.requestRepository.findById(request.getUuid()).orElseThrow(EntityNotFound::new);
                            readReq.setState(State.COMPLETE);
                            readReq.setDtUpdate(new Date());
                            return this.requestRepository.save(readReq);
                        })
                )
                .exceptionally((ex) -> {
                    if (ex != null) {
                        log.error(ex.getMessage());
                        transactionTemplate.execute((e) -> {
                            Request readReq = this.requestRepository.findById(request.getUuid()).orElseThrow(EntityNotFound::new);
                            readReq.setState(State.FAIL);
                            readReq.setDtUpdate(new Date());
                            return this.requestRepository.save(readReq);
                        });
                    }

                    return null;
                });
    }
}
