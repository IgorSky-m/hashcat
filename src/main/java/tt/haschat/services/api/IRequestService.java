package tt.haschat.services.api;

import tt.haschat.dto.Request;

import java.util.UUID;

public interface IRequestService {

    Request createNewRequest(Request request);

    Request getOneById(UUID uuid);

    void confirmRequest(UUID mailId);

    Request save(Request request);
}
