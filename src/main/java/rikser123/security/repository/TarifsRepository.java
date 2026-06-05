package rikser123.security.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import rikser123.security.repository.entity.Tarif;

import java.util.UUID;

@Repository
public interface TarifsRepository extends JpaRepository<Tarif, UUID> {
}
