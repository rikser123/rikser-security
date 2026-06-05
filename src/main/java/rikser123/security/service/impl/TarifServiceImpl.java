package rikser123.security.service.impl;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import rikser123.security.dto.response.UserTarifResponseDto;
import rikser123.security.repository.TarifsRepository;
import rikser123.security.repository.entity.Tarif;
import rikser123.security.repository.entity.TarifStatus;
import rikser123.security.repository.entity.User;
import rikser123.security.repository.entity.UserTarif;
import rikser123.security.service.TarifService;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
@Slf4j
public class TarifServiceImpl implements TarifService {
  private static List<Tarif> tarifs;
  private final TarifsRepository tarifsRepository;

  @PostConstruct
  void init() {
    var allTarifs = tarifsRepository.findAll();
    tarifs = Collections.unmodifiableList(allTarifs);
  }

  public UserTarifResponseDto getUserTarif(User user) {
    var userTarif = user
      .getActiveTarif()
      .orElseThrow(() -> new EntityNotFoundException("Не удалось найти тариф пользователя"));
    var currentTarif = tarifs
      .stream()
      .filter(tarif -> tarif.getId().equals(userTarif.getTarifId()))
      .findFirst()
      .orElseThrow(() ->
        new EntityNotFoundException(String.format("Не удалось найти тариф c таким id %s", userTarif.getTarifId())));

    var responseDto = new UserTarifResponseDto();
    responseDto.setId(userTarif.getId());
    responseDto.setName(currentTarif.getName());
    responseDto.setDescription(currentTarif.getDescription());
    responseDto.setCreated(userTarif.getCreated());
    responseDto.setUpdated(userTarif.getUpdated());
    responseDto.setRequestPerDay(currentTarif.getRequestPerDay());

    return responseDto;
  }

  @Override
  public User updateTarif(User user, UUID tarifId) {
    var currentTarif = tarifs
      .stream()
      .filter(tarif -> tarif.getId().equals(tarifId))
      .findFirst()
      .orElseThrow(() ->
        new EntityNotFoundException(String.format("Не удалось найти тариф c таким id %s", tarifId)));

    var activeTarif = user.getActiveTarif();
    if (activeTarif.isPresent()) {
      var isSameTarif = activeTarif.get().getId().equals(tarifId);
      if (isSameTarif) {
        return user;
      }

      user.getTarifs().forEach(tarif -> {
        tarif.setStatus(TarifStatus.DISABLED);
      });
      var newTarif = createUserTarif(user, currentTarif);
      user.getTarifs().add(newTarif);
    } else {
      var newTarif = createUserTarif(user, currentTarif);
      user.getTarifs().add(newTarif);
    }

    return user;
  }

  private UserTarif createUserTarif(User user, Tarif tarif) {
    var userTarif = new UserTarif();
    userTarif.setStatus(TarifStatus.ACTIVE);
    userTarif.setUser(user);
    userTarif.setTarifId(tarif.getId());
    return userTarif;
  }
}
