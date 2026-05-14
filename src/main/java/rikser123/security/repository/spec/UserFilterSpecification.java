package rikser123.security.repository.spec;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;
import rikser123.security.dto.request.UserFilterDto;
import rikser123.security.repository.entity.User;

import java.util.ArrayList;
import java.util.Objects;

@RequiredArgsConstructor
public class UserFilterSpecification implements Specification<User> {
  private final UserFilterDto filter;

  @Override
  public Predicate toPredicate(Root<User> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
    var predicates = new ArrayList<Predicate>();

    if (StringUtils.isNotEmpty(filter.getLogin())) {
      predicates.add(cb.like(
        cb.lower(root.get("login")),
        "%" + filter.getLogin().toLowerCase() + "%"
      ));
    }

    if (StringUtils.isNotEmpty(filter.getEmail())) {
      predicates.add(cb.like(
        cb.lower(root.get("email")),
        "%" + filter.getEmail().toLowerCase() + "%"
      ));
    }

    if (StringUtils.isNotEmpty(filter.getFirstName())) {
      predicates.add(cb.like(
        cb.lower(root.get("firstName")),
        "%" + filter.getFirstName().toLowerCase() + "%"
      ));
    }

    if (StringUtils.isNotEmpty(filter.getLastName())) {
      predicates.add(cb.like(
        cb.lower(root.get("lastName")),
        "%" + filter.getLastName().toLowerCase() + "%"
      ));
    }

    if (StringUtils.isNotEmpty(filter.getMiddleName())) {
      predicates.add(cb.like(
        cb.lower(root.get("middle")),
        "%" + filter.getMiddleName().toLowerCase() + "%"
      ));
    }

    if (!Objects.isNull(filter.getStatus())) {
      predicates.add(cb.equal(root.get("status"), filter.getStatus()));
    }

    if (!Objects.isNull(filter.getBirthDateFrom()) && !Objects.isNull(filter.getBirthDateTo())) {
      predicates.add(
        cb.between(root.get("birthDate"), filter.getBirthDateFrom(), filter.getBirthDateTo())
      );
    } else if (!Objects.isNull(filter.getBirthDateFrom())) {
      predicates.add(cb.greaterThanOrEqualTo(root.get("birthDate"), filter.getBirthDateFrom()));
    } else if (!Objects.isNull(filter.getBirthDateTo())) {
      predicates.add(cb.lessThanOrEqualTo(root.get("birthDate"), filter.getBirthDateTo()));
    }

    return cb.and(predicates.toArray(Predicate[]::new));
  }
}
