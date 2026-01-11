package ru.itwizardry.spring.module4userserviceapi.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import ru.itwizardry.spring.module4userserviceapi.dto.UserCreateRequest;
import ru.itwizardry.spring.module4userserviceapi.dto.UserDto;
import ru.itwizardry.spring.module4userserviceapi.dto.UserUpdateRequest;
import ru.itwizardry.spring.module4userserviceapi.model.User;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(source = "createdAt", target = "createdDate")
    UserDto toDto(User user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    User toEntity(UserCreateRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void updateEntity(@MappingTarget User user, UserUpdateRequest request);
}
