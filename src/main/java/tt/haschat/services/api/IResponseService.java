package tt.haschat.services.api;

import tt.haschat.dto.Response;

import java.util.List;

public interface IResponseService {
    void saveResponses(List<Response> responseList);
    Response getByHash(String hash);
    List<Response> getAllByHashList(List<String> hashList);
}
