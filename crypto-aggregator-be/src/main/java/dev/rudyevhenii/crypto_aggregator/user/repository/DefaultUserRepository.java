package dev.rudyevhenii.crypto_aggregator.user.repository;

import dev.rudyevhenii.crypto_aggregator.user.UserEntity;
import dev.rudyevhenii.crypto_aggregator.user.domain.User;
import dev.rudyevhenii.crypto_aggregator.user.mapper.UserEntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class DefaultUserRepository implements UserRepository {

    private final SpringDataUserRepository repository;
    private final UserEntityMapper mapper;

    @Override
    public Optional<User> findById(UUID id) {
        return repository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public User create(User user) {
        UserEntity userEntity = mapper.toCreateEntity(user);
        UserEntity savedEntity = repository.save(userEntity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public boolean existsByEmail(String email) {
        return repository.existsByEmail(email);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return repository.findByEmail(email)
                .map(mapper::toDomain);
    }
}
