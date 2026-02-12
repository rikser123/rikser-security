package rikser123.security;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import rikser123.security.dto.request.EditUserDto;
import rikser123.security.repository.entity.Privilege;
import rikser123.security.repository.entity.User;
import rikser123.security.repository.entity.UserPrivilege;

public class TestData {
  public static User createUser() {
    var userDto = new User();
    userDto.setLogin("login");
    userDto.setPassword("password");
    userDto.setFirstName("firstName");
    userDto.setMiddleName("middleName");
    userDto.setLastName("lastName");
    userDto.setEmail("email");
    userDto.setBirthDate(LocalDate.of(1990, 01, 01));

    var privilege = new UserPrivilege();
    privilege.setPrivilege(Privilege.USER_EDIT);
    privilege.setUser(userDto);

    var privilege2 = new UserPrivilege();
    privilege2.setPrivilege(Privilege.USER_DELETE);
    privilege2.setUser(userDto);

    userDto.setUserPrivileges(Set.of(privilege, privilege2));

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
}
