package tt.haschat.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tt.haschat.config.AsyncConfig;
import tt.haschat.dto.Email;
import tt.haschat.dto.Request;
import tt.haschat.dto.api.State;
import tt.haschat.exceptions.CustomApplicationException;
import tt.haschat.exceptions.DecodeException;
import tt.haschat.exceptions.EntityNotFound;
import tt.haschat.processors.DecodeProcessor;
import tt.haschat.repositories.api.IEmailRepository;
import tt.haschat.repositories.api.IRequestRepository;
import tt.haschat.services.api.IMsgSenderService;
import tt.haschat.services.api.IRequestService;
import tt.haschat.services.api.IResponseService;
import tt.haschat.validators.api.IValidator;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.SubmissionPublisher;

@Slf4j
@Service
public class RequestService extends SubmissionPublisher<Request> implements IRequestService {

    private final IRequestRepository requestRepository;
    private final IEmailRepository emailRepository;
    private final IResponseService responseService;
    private final IValidator<Request> validator;
    private final MessageSource messageSource;
    private final IMsgSenderService msgSenderService;

    public RequestService(
            @Qualifier(AsyncConfig.ASYNC_EXECUTOR_BEAN_NAME) Executor executor,
            IRequestRepository requestRepository,
            IEmailRepository emailRepository,
            IResponseService responseService,
            IValidator<Request> validator,
            MessageSource messageSource,
            IMsgSenderService msgSenderService,
            DecodeProcessor decodeProcessor
    ){
        super(executor, 10);
        this.requestRepository = requestRepository;
        this.emailRepository = emailRepository;
        this.responseService = responseService;
        this.validator = validator;
        this.messageSource = messageSource;
        this.msgSenderService = msgSenderService;

        this.subscribe(decodeProcessor);
    }

    @Transactional
    public Request createNewRequest(Request request) {
        request.setUuid(UUID.randomUUID());
        request.setDtCreate(new Date());
        request.setDtUpdate(request.getDtCreate());
        this.validator.validate(request);
        try {
            Email email = this.emailRepository.findByEmail(request.getEmail()).orElse(null);
            boolean needConfirmEmail = email == null || !email.isConfirmed();
            request.setState(needConfirmEmail ? State.WAIT : State.IN_PROGRESS);
            Request savedRequest = this.requestRepository.save(request);
            if (needConfirmEmail) {
                if (email == null) {
                    email = this.emailRepository.save(new Email(UUID.randomUUID(), request.getEmail(), false));
                }
                this.sendConfirmMaiLink(email);
            } else {
                this.startDecode(savedRequest);
            }
            return request;
        } catch (Exception e) {
            String msg = messageSource.getMessage("error.crud.create", null, LocaleContextHolder.getLocale());
            log.error(msg,e);
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
            log.error(msg,e);
            throw new CustomApplicationException(msg);
        }

    }

    @Transactional
    @Override
    public void confirmRequest(UUID mailId) {
        try {
            Email email = this.emailRepository.findById(mailId).orElse(null);
            if (email != null && email.isConfirmed()) {
                throw new IllegalArgumentException(messageSource.getMessage("email.already.confirmed.error", null, LocaleContextHolder.getLocale()));
            } else if (email == null) {
                throw new IllegalArgumentException(messageSource.getMessage("email.broken.link.error", null, LocaleContextHolder.getLocale()));
            } else {
                email.setConfirmed(true);
                this.emailRepository.save(email);
                List<Request> allByEmail = this.requestRepository.findAllByEmail(email.getEmail());
                Date dtUpdate = new Date();
                allByEmail.forEach(e -> {
                    e.setState(State.IN_PROGRESS);
                    e.setDtUpdate(dtUpdate);
                });
                this.requestRepository.saveAll(allByEmail).forEach(this::startDecode);
            }
        } catch (EntityNotFound | IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            String msg = messageSource.getMessage("error.crud.update", null, LocaleContextHolder.getLocale());
            log.error(msg,e);
            throw new CustomApplicationException(msg);
        }
    }

    private void startDecode(Request request) {
        this.offer(request, (subscriber, req) -> {
            subscriber.onError(new DecodeException("droped because of backpressure"));
            return true;
        });
    }

    @Transactional
    @Override
    public Request save(Request request) {
        try {
            return this.requestRepository.save(request);
        } catch (Exception e) {
            String msg = messageSource.getMessage("error.crud.create", null, LocaleContextHolder.getLocale());
            log.error(msg, e);
            throw new CustomApplicationException(msg);
        }
    }


    private void sendConfirmMaiLink(Email email){
            String confirmLink = ServletUriComponentsBuilder
                    .fromCurrentRequest()
                    .path("/email/{mail_id}/confirm")
                    .buildAndExpand(email.getUuid())
                    .toString();

            this.msgSenderService.sendText(
                    email.getEmail(),
                    msgSenderService.getBaseSubjectText(EmailSenderService.DefaultSubjectTypes.CONFIRM.type),
                    confirmLink
            );
        }
}
