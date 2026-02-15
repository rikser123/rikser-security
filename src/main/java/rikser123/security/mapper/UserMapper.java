package rikser123.security.mapper;

import java.util.stream.Collectors;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import rikser123.security.dto.request.CreateUserRequestDto;
import rikser123.security.dto.request.EditUserDto;
import rikser123.security.dto.response.UserResponseDto;
import rikser123.security.repository.entity.User;
import rikser123.security.repository.entity.UserPrivilege;

@Mapper(componentModel = "spring")
@Slf4j
public abstract class UserMapper {
  @Setter(onMethod = @__({@Autowired}))
  private PasswordEncoder passwordEncoder;

  @Mapping(target = "authorities", ignore = true)
  public abstract rikser123.bundle.dto.User mapToSecurityUser(User user);

  public abstract User mapUser(CreateUserRequestDto dto);

  public abstract UserResponseDto mapUserToDto(User user);

  public abstract void updateUser(EditUserDto editUserDto, @MappingTarget User user);

  @AfterMapping
  protected void mapUser(CreateUserRequestDto dto, @MappingTarget User user) {
    setEncodedPassword(user);

    var privileges =
        dto.getPrivileges().stream()
            .map(
                privilege -> {
                  var userPrivilege = new UserPrivilege();
                  userPrivilege.setPrivilege(privilege);
                  userPrivilege.setUser(user);
                  return userPrivilege;
                })
            .collect(Collectors.toSet());

    user.setUserPrivileges(privileges);
  }

  @AfterMapping
  protected void updateUserAfterMapping(EditUserDto dto, @MappingTarget User user) {
    var dtoPrivileges = dto.getPrivileges();
    var currentPrivileges = user.getPrivileges();
    var userPrivileges = user.getUserPrivileges();

    var newPrivileges =
        dtoPrivileges.stream()
            .filter(privilege -> !currentPrivileges.contains(privilege))
            .collect(Collectors.toSet());
    var deletedPrivileges =
        currentPrivileges.stream()
            .filter(privilege -> !dtoPrivileges.contains(privilege))
            .collect(Collectors.toSet());

    var deletedUserPrivileges =
        userPrivileges.stream()
            .filter(userPrivilege -> deletedPrivileges.contains(userPrivilege.getPrivilege()))
            .collect(Collectors.toSet());

    deletedUserPrivileges.forEach(
        userPrivilege -> {
          userPrivileges.remove(userPrivilege);
          userPrivilege.setUser(null);
        });

    newPrivileges.forEach(
        privilege -> {
          var userPrivilege = new UserPrivilege();
          userPrivilege.setUser(user);
          userPrivilege.setPrivilege(privilege);
          userPrivileges.add(userPrivilege);
        });

    setEncodedPassword(user);
  }

  private void setEncodedPassword(User user) {
    var encodedPassword = passwordEncoder.encode(user.getPassword());
    user.setPassword(encodedPassword);
  }
}
