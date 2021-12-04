package tt.haschat.services.api;

import tt.haschat.dto.Response;

import java.util.List;

public interface IMd5DecoderService {
    List<Response> decode(List<String> hashes);

}
