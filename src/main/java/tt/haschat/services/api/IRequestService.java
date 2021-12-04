package tt.haschat.services.api;

import tt.haschat.dto.Request;

import java.util.List;
import java.util.UUID;

public interface IRequestService {

    Request createNewRequest(Request request);

    Request getOneById(UUID uuid);

    List<Request> getAllByEmail(String email);

    List<Request>  getAll();
}
