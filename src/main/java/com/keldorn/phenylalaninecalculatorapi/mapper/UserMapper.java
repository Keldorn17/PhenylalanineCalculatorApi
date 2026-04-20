package com.keldorn.phenylalaninecalculatorapi.mapper;

import com.keldorn.phenylalaninecalculatorapi.domain.entity.User;
import com.keldorn.phenylalaninecalculatorapi.dto.user.UserRequest;
import com.keldorn.phenylalaninecalculatorapi.dto.user.UserResponse;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "id", source = "userId")
    UserResponse toResponse(User user);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(UserRequest request, @MappingTarget User user);

}
