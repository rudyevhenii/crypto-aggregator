package dev.rudyevhenii.crypto_aggregator.auth.config;

import dev.rudyevhenii.crypto_aggregator.auth.repository.UserRepository;
import dev.rudyevhenii.crypto_aggregator.core.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetailsService;

@Configuration
@RequiredArgsConstructor
public class UserDetailsServiceConfig {

    private final UserRepository userRepository;

    @Bean
    public UserDetailsService loadUserByUsername() {
        return email -> userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User does not exist with email: %s"
                        .formatted(email)));
    }
}
