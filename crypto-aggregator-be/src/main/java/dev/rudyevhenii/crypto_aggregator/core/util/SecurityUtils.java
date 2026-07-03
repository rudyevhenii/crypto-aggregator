package dev.rudyevhenii.crypto_aggregator.core.util;

import dev.rudyevhenii.crypto_aggregator.auth.UserEntity;
import dev.rudyevhenii.crypto_aggregator.core.exception.UnauthorizedException;
import lombok.experimental.UtilityClass;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

@UtilityClass
public class SecurityUtils {

    public UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserEntity userEntity) {
            return userEntity.getId();
        }
        throw new UnauthorizedException("Current user is not authenticated");
    }
}
