package rikser123.security;

import rikser123.security.dto.request.CreateUserRequestDto;
import rikser123.security.dto.request.EditUserDto;
import rikser123.security.repository.entity.Privilege;
import rikser123.security.repository.entity.User;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class TestData {
    public static CreateUserRequestDto createUserRequestDto() {
        var userDto = new CreateUserRequestDto();
        userDto.setLogin("login");
        userDto.setPassword("password");
        userDto.setPasswordConfirmation("passwordConfirmation");
        userDto.setFirstName("firstName");
        userDto.setMiddleName("middleName");
        userDto.setLastName("lastName");
        userDto.setBirthDate(LocalDate.of(1990, 01, 01));
        userDto.getPrivileges().addAll(List.of(Privilege.USER_EDIT, Privilege.USER_DELETE));

        return userDto;
    }

    public static EditUserDto createUserEditRequestDto() {
        var userDto = new EditUserDto();
        userDto.setLogin("loginNew");
        userDto.setPassword("passwordNew");
        userDto.setPasswordConfirmation("passwordConfirmationNew");
        userDto.setFirstName("firstNameNew");
        userDto.setMiddleName("middleNameNew");
        userDto.setLastName("lastNameNew");
        userDto.setBirthDate(LocalDate.of(1990, 01, 01));
        userDto.getPrivileges().addAll(List.of(Privilege.USER_EDIT));

        return userDto;
    }

    public static User createUser() {
        var userDto = new User();
        userDto.setId(UUID.randomUUID());
        userDto.setLogin("login");
        userDto.setPassword("password");
        userDto.setFirstName("firstName");
        userDto.setMiddleName("middleName");
        userDto.setLastName("lastName");
        userDto.setBirthDate(LocalDate.of(1990, 01, 01));
        userDto.getPrivileges().addAll(List.of(Privilege.USER_EDIT, Privilege.USER_DELETE));

        return userDto;
    }
}
