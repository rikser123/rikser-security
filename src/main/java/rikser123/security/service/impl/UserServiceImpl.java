package rikser123.security.service.impl;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import rikser123.security.exception.StatusChangeException;
import rikser123.security.repository.UserRepository;
import rikser123.security.repository.entity.User;
import rikser123.security.repository.entity.UserStatus;
import rikser123.security.service.StatusMatrix;
import rikser123.security.service.UserService;


import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final StatusMatrix<UserStatus> userStatusMatrix;

    @Override
    public Optional<User> findUserByLogin(String login) {
        return userRepository.findUserByLogin(login);
    }

    @Override
    public Optional<User> findUserByEmail(String email) {
        return userRepository.findUserByEmail(email);
    }

    @Override
    public User save(User user) {
        return userRepository.save(user);
    }

    @Override
    public User findById(UUID id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException(String.format("Пользователь с id %s не найден", id)));
    }

    @Override
    public Optional<User> findUserByLoginAndIdIsNot(String login, UUID id) {
        return userRepository.findUserByLoginAndIdIsNot(login, id);
    }

    @Override
    public Optional<User> findUserByEmailAndIdIsNot(String email, UUID id) {
        return userRepository.findUserByEmailAndIdIsNot(email,id);
    }

    @Override
    public User changeStatus(User user, UserStatus status) {
        if (user.getStatus().equals(status) || !userStatusMatrix.isAvailable(user.getStatus(), status)) {
            log.warn("ERROR: while checkStatusMovement for user: {} from: {} to: {}", user.getId(),
                    user.getStatus(), status);
            throw new StatusChangeException();
        }
        user.setStatus(status);
        userRepository.save(user);
        return user;
    }

}
