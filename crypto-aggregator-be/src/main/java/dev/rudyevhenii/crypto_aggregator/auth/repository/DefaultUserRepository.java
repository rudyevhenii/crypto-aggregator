package dev.rudyevhenii.crypto_aggregator.auth.repository;

import dev.rudyevhenii.crypto_aggregator.auth.UserEntity;
import dev.rudyevhenii.crypto_aggregator.auth.domain.User;
import dev.rudyevhenii.crypto_aggregator.auth.mapper.UserEntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

import static dev.rudyevhenii.crypto_aggregator.core.config.RedisConfig.USER_CACHE;

@Repository
@RequiredArgsConstructor
public class DefaultUserRepository implements UserRepository {

    private final SpringDataUserRepository repository;
    private final UserEntityMapper mapper;

    @Override
    @CachePut(value = USER_CACHE, key = "#result.id")
    public User create(User user) {
        UserEntity createEntity = mapper.toCreateEntity(user);
        UserEntity savedEntity = repository.save(createEntity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    @Cacheable(value = USER_CACHE, key = "#id")
    public Optional<User> findById(UUID id) {
        return repository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return repository.findByEmail(email)
                .map(mapper::toDomain);
    }

    @Override
    public boolean existsByEmail(String email) {
        return repository.existsByEmail(email);
    }
}
