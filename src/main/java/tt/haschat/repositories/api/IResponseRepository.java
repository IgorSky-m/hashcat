package tt.haschat.repositories.api;

import org.springframework.data.jpa.repository.JpaRepository;
import tt.haschat.dto.Response;

public interface IResponseRepository extends JpaRepository<Response, String>{
}
