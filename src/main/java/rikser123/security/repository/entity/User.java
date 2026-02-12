package rikser123.security.repository.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Entity
@Table(name = "users")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class User implements UserDetails {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "id")
  private UUID id;

  @Column(name = "login", nullable = false, length = 50, unique = true)
  private String login;

  @Column(name = "password", nullable = false)
  @JsonIgnore
  private String password;

  @Column(name = "email", nullable = false, unique = true, length = 100)
  private String email;

  @Column(name = "status", nullable = false)
  @Enumerated(EnumType.STRING)
  private UserStatus status = UserStatus.REGISTERED;

  @Column(name = "first_name", length = 100, nullable = false)
  private String firstName;

  @Column(name = "middle_name", length = 100)
  private String middleName;

  @Column(name = "last_name", length = 100, nullable = false)
  private String lastName;

  @Column(name = "birth_date", length = 100, nullable = false)
  private LocalDate birthDate;

  @CreationTimestamp
  @Column(name = "created", updatable = false)
  private LocalDateTime created;

  @OneToMany(
      mappedBy = "user",
      fetch = FetchType.EAGER,
      orphanRemoval = true,
      cascade = CascadeType.ALL)
  private Set<UserPrivilege> userPrivileges = new HashSet<>();

  @UpdateTimestamp
  @Column(name = "updated", insertable = false)
  private LocalDateTime updated;

  public Set<Privilege> getPrivileges() {
    return userPrivileges.stream().map(UserPrivilege::getPrivilege).collect(Collectors.toSet());
  }

  @Override
  @JsonIgnore
  public String getUsername() {
    return login;
  }

  @Override
  @JsonIgnore
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return userPrivileges.stream()
        .map(userPrivilege -> new SimpleGrantedAuthority(userPrivilege.getPrivilege().name()))
        .toList();
  }

  @Override
  @JsonIgnore
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  @JsonIgnore
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  @JsonIgnore
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  @JsonIgnore
  public boolean isEnabled() {
    return true;
  }
}
