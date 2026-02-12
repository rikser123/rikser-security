package rikser123.security.service.impl;

import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import rikser123.security.repository.BlackListRepository;
import rikser123.security.repository.entity.BlackListToken;
import rikser123.security.service.BlackListService;

@Service
@RequiredArgsConstructor
@Slf4j
public class BlackListServiceImpl implements BlackListService {
  private final BlackListRepository blackListRepository;

  @Scheduled(cron = "${scheduler.clear-black-list.cron}")
  public void removeAll() {
    log.info("Start clear black list");
    blackListRepository.deleteAllInBatch();
    log.info("Finish clear black list");
  }

  @Override
  public BlackListToken addToken(String token, UUID userId) {
    var blackListToken = new BlackListToken();
    blackListToken.setToken(token);
    blackListToken.setUserId(userId);
    return blackListRepository.save(blackListToken);
  }

  @Override
  public Optional<BlackListToken> findByToken(String token) {
    return blackListRepository.findByToken(token);
  }
}
