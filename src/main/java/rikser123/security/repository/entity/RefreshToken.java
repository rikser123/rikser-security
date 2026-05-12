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
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "refresh_token")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class RefreshToken {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(nullable = false, unique = true, name = "token_hash")
  private String tokenHash;

  @Column(nullable = false, name = "expires_at")
  private Instant expiresAt;

  @Column(name = "user_id")
  private UUID userId;

  @Column(name = "revoked")
  private Boolean revoked;

  @CreationTimestamp
  @Column(name = "created", updatable = false)
  private Instant created;

  @UpdateTimestamp
  @Column(name = "updated", insertable = false)
  private Instant updated;

}
