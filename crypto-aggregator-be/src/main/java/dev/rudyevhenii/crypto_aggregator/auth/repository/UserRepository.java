package dev.rudyevhenii.crypto_aggregator.auth.repository;

import dev.rudyevhenii.crypto_aggregator.auth.domain.User;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository {

    User create(User user);

    Optional<User> findById(UUID id);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
}
