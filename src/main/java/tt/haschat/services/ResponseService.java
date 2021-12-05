package tt.haschat.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tt.haschat.dto.Response;
import tt.haschat.exceptions.CustomApplicationException;
import tt.haschat.exceptions.EntityNotFound;
import tt.haschat.repositories.api.IResponseRepository;
import tt.haschat.services.api.IResponseService;

import java.util.List;

@Transactional(readOnly = true)
@Service
@Slf4j
public class ResponseService implements IResponseService {



    private final IResponseRepository responseRepository;
    private final MessageSource messageSource;

    public ResponseService(IResponseRepository responseRepository, MessageSource messageSource){
        this.responseRepository = responseRepository;
        this.messageSource = messageSource;
    }

    @Transactional
    @Override
    public void saveResponses(List<Response> responseList) {
        try {
            this.responseRepository.saveAll(responseList);
        } catch (Exception e) {
            String msg = messageSource.getMessage("error.crud.create", null, LocaleContextHolder.getLocale());
            log.error(msg,e);
            throw new CustomApplicationException(msg);
        }
    }

    @Override
    public Response getByHash(String hash) {
        try {
            return this.responseRepository.findById(hash).orElseThrow(EntityNotFound::new);
        } catch (EntityNotFound e) {
            throw e;
        } catch (Exception e) {
            String msg = messageSource.getMessage("error.crud.read", null, LocaleContextHolder.getLocale());
            log.error(msg,e);
        throw new CustomApplicationException(msg);
        }
    }

    @Override
    public List<Response> getAllByHashList(List<String> hashList) {
        try {
            return this.responseRepository.findAllById(hashList);
        } catch (Exception e) {
            String msg = messageSource.getMessage("error.crud.read", null, LocaleContextHolder.getLocale());
            log.error(msg,e);
            throw new CustomApplicationException(msg);
        }
    }
}
