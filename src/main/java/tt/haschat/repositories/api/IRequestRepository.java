package tt.haschat.repositories.api;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tt.haschat.dto.Request;

import java.util.List;
import java.util.UUID;

@Repository
public interface IRequestRepository extends JpaRepository<Request, UUID> {

    List<Request> getByEmail(String email);
    List<Request> findAllByEmail(String email);
}
