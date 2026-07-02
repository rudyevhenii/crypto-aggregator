package dev.rudyevhenii.crypto_aggregator.core.util;

import dev.rudyevhenii.crypto_aggregator.core.exception.UnauthorizedException;
import dev.rudyevhenii.crypto_aggregator.user.domain.User;
import lombok.experimental.UtilityClass;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

@UtilityClass
public class SecurityUtils {

    public UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof User user) {
            return user.getId();
        }
        throw new UnauthorizedException("Current user is not authenticated");
    }
}
