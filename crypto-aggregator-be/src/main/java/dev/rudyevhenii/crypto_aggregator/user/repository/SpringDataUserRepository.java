package dev.rudyevhenii.crypto_aggregator.user.repository;

import dev.rudyevhenii.crypto_aggregator.user.UserEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;
import java.util.UUID;

public interface SpringDataUserRepository extends CrudRepository<UserEntity, UUID> {

    boolean existsByEmail(String email);

    Optional<UserEntity> findByEmail(String email);
}
