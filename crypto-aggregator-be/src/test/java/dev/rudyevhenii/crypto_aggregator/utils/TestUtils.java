package dev.rudyevhenii.crypto_aggregator.utils;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.springframework.core.io.ClassPathResource;

import java.nio.charset.StandardCharsets;

@UtilityClass
public class TestUtils {

    @SneakyThrows
    public static String readResource(String path) {
        return new ClassPathResource(path).getContentAsString(StandardCharsets.UTF_8);
    }
}
