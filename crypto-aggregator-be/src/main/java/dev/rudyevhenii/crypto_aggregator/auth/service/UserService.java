package dev.rudyevhenii.crypto_aggregator.auth.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.UUID;

public interface UserService extends UserDetailsService {

    UserDetails findById(UUID id);
}
