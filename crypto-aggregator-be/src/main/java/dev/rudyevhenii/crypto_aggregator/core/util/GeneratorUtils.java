package dev.rudyevhenii.crypto_aggregator.core.util;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class GeneratorUtils {

    public UUID uuid() {
        return UUID.randomUUID();
    }

    public Instant now() {
        return Instant.now();
    }
}
