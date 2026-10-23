package cl.utem.inf.cpyd.domain.repository;

import cl.utem.inf.cpyd.domain.enums.Country;
import cl.utem.inf.cpyd.domain.model.Exchange;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExchangeRepository extends JpaRepository<Exchange, Long> {

    Optional<Exchange> findByCountryAndDate(Country country, LocalDate date);
}
