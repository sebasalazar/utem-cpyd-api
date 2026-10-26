package cl.utem.inf.cpyd.domain.repository;

import cl.utem.inf.cpyd.domain.model.Credential;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CredentialRepository extends JpaRepository<Credential, Long> {

    Optional<Credential> findByToken(UUID token);

    Optional<Credential> findByEmailIgnoreCase(String email);
}
