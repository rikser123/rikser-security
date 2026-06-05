package rikser123.security.repository.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "user_tarif")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UserTarif {
  @Id
  @Column(name = "id")
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @JoinColumn(name = "user_id")
  @ManyToOne
  private User user;

  @Column(name = "tarif_id", nullable = false)
  private UUID tarifId;

  @Column(name = "status", nullable = false, length = 100)
  @Enumerated(EnumType.STRING)
  private TarifStatus status;

  @Column(name = "created", nullable = false, updatable = false)
  @CreationTimestamp
  private Instant created;

  @Column(name = "updated", insertable = false)
  private Instant updated;
}
