package rikser123.security.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import rikser123.security.repository.entity.UserTarif;

import java.util.UUID;

@Repository
public interface UserTarifRepository extends JpaRepository<UserTarif, UUID> {
}
