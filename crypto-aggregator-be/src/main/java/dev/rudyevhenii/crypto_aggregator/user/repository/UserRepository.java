package dev.rudyevhenii.crypto_aggregator.user.repository;

import dev.rudyevhenii.crypto_aggregator.user.domain.User;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository {

    Optional<User> findById(UUID id);

    User create(User user);

    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);
}
