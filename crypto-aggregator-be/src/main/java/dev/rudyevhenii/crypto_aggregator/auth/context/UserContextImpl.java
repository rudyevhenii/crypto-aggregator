package dev.rudyevhenii.crypto_aggregator.auth.context;

import dev.rudyevhenii.crypto_aggregator.auth.security.SecurityUserDetails;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class UserContextImpl implements UserContext {

    @Override
    public UUID getUserId() {
        return getSecurityUserDetails().getUser().getId();
    }

    private SecurityUserDetails getSecurityUserDetails() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return (SecurityUserDetails) principal;
    }
}
