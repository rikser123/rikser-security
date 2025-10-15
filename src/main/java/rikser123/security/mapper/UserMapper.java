package rikser123.security.mapper;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import rikser123.security.dto.request.CreateUserRequestDto;
import rikser123.security.dto.response.UserResponseDto;
import rikser123.security.repository.entity.User;
import rikser123.security.repository.entity.UserPrivilege;

import java.util.stream.Collectors;

@Mapper(componentModel = "spring" )
public abstract class UserMapper {
    @Autowired
    private PasswordEncoder passwordEncoder;

    public abstract User mapUser(CreateUserRequestDto dto);

    @Mapping(source = "privileges", target = "privileges", ignore = true)
    public abstract UserResponseDto mapUserToDto(User user);

    @AfterMapping
    protected void mapUser(CreateUserRequestDto dto, @MappingTarget User user) {
        var encodedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);
    }

    @AfterMapping
    protected void mapUserToDto(User user, @MappingTarget UserResponseDto dto) {
        var privileges = user.getPrivileges().stream().map(UserPrivilege::getPrivilege).collect(Collectors.toSet());
        dto.setPrivileges(privileges);
    }
}
