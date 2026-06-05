package rikser123.security.repository.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "tarifs")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Tarif {
  @Id
  @Column(name = "id")
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "name", nullable = false, length = 100)
  private String name;

  @Column(name = "description", length = 1000)
  private String description;

  @Column(name = "request_per_day", nullable = false)
  private Integer requestPerDay;

  @Column(name = "created", nullable = false, updatable = false)
  @CreationTimestamp
  private Instant created;

  @Column(name = "updated", insertable = false)
  private Instant updated;
}
