package rikser123.security.service;

import rikser123.security.repository.entity.User;
import rikser123.security.repository.entity.UserStatus;

import java.util.Optional;
import java.util.UUID;

/**
 * Интерфейс для работы с пользователями
 *
 */
public interface UserService {
    /**
     * Поиск пользователя по логину
     * @param login Логин пользователя
     *
     * @return Optional пользователя
     */
    Optional<User> findUserByLogin(String login);

    /**
     * Поиск пользователя по Email
     * @param email Email пользователя
     *
     * @return Optional пользователя
     */
    Optional<User> findUserByEmail(String email);

    /**
     * Сохранение пользователя
     * @param user {@link User}
     *
     * @return Сохраненный пользователь
     */
    User save(User user);

    /**
     * Поиск пользователя по id
     * @param id Id пользователя
     *
     * @return Пользователь
     */
    User findById(UUID id);

    /**
     * Поиск пользователя по логину с отличным id
     * @param login Login пользователя
     * @param id Id пользователя
     *
     * @return Optional пользователя
     */
    Optional<User> findUserByLoginAndIdIsNot(String login, UUID id);

    /**
     * Поиск пользователя по email с отличным id
     * @param email Email пользователя
     * @param id Id пользователя
     *
     * @return Optional пользователя
     */
    Optional<User> findUserByEmailAndIdIsNot(String email, UUID id);

    /**
     * Изменение статуса пользователя
     * @param user {@link User}
     * @param status {@link UserStatus}
     *
     * @return Пользователь с новым статусом
     */
    User changeStatus(User user, UserStatus status);
}
