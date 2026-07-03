package dev.rudyevhenii.crypto_aggregator.auth.mapper;

import dev.rudyevhenii.crypto_aggregator.auth.UserEntity;
import dev.rudyevhenii.crypto_aggregator.auth.domain.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserEntityMapper {

    @Mapping(target = UserEntity.Fields.newEntity, constant = "true")
    UserEntity toCreateEntity(User user);

    User toDomain(UserEntity userEntity);
}
