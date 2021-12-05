package tt.haschat.repositories.api;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tt.haschat.dto.Email;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface IEmailRepository extends JpaRepository<Email, UUID> {
    Optional<Email> findByEmail(String email);
}
