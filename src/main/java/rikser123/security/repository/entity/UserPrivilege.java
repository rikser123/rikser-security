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
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "user_privilege")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserPrivilege {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "id")
  private UUID id;

  @Column(name = "privilege")
  @Enumerated(EnumType.STRING)
  private Privilege privilege;

  @ManyToOne()
  @JoinColumn(name = "user_id")
  private User user;

  @Column(name = "created", updatable = false)
  @CreationTimestamp
  private LocalDateTime created;

  @Column(name = "updated", insertable = false)
  private LocalDateTime updated;

  @Override
  public String toString() {
    return "userPrivilege " + "id: " + id + "privilege: " + privilege;
  }
}
