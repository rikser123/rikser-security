package rikser123.security.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.crypto.password.PasswordEncoder;
import rikser123.security.TestData;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Тестирование класса {@link UserMapper}
 *
 */

public class UserMapperTest {
    private UserMapper userMapper;
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void init() {
        passwordEncoder = Mockito.mock(PasswordEncoder.class);
        userMapper = new UserMapperImpl();
        userMapper.setPasswordEncoder(passwordEncoder);
    }

    @Test
    void shouldMapUser() {
        var dto = TestData.createUserRequestDto();
        var user = userMapper.mapUser(dto);

        assertThat(user.getLogin()).isEqualTo(dto.getLogin());
        assertThat(user.getFirstName()).isEqualTo(dto.getFirstName());
        assertThat(user.getLastName()).isEqualTo(dto.getLastName());
        assertThat(user.getMiddleName()).isEqualTo(dto.getMiddleName());
        assertThat(user.getBirthDate()).isEqualTo(dto.getBirthDate());
        assertThat(user.getPrivileges().size()).isEqualTo(dto.getPrivileges().size());
    }

    @Test
    void shouldUpdateUser() {
        var user = TestData.createUser();
        var editDto = TestData.createUserEditRequestDto();
        userMapper.updateUser(editDto, user);

        assertThat(user.getLogin()).isEqualTo(editDto.getLogin());
        assertThat(user.getFirstName()).isEqualTo(editDto.getFirstName());
        assertThat(user.getLastName()).isEqualTo(editDto.getLastName());
        assertThat(user.getMiddleName()).isEqualTo(editDto.getMiddleName());
        assertThat(user.getBirthDate()).isEqualTo(editDto.getBirthDate());
        assertThat(user.getPrivileges().size()).isEqualTo(editDto.getPrivileges().size());
    }

}
