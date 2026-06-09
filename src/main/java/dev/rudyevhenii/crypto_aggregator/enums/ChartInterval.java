package dev.rudyevhenii.crypto_aggregator.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Duration;

@Getter
@AllArgsConstructor
public enum ChartInterval {
    ONE_SECOND(Duration.ofSeconds(1)),

    ONE_MINUTE(Duration.ofMinutes(1)),
    THREE_MINUTES(Duration.ofMinutes(3)),
    FIVE_MINUTES(Duration.ofMinutes(5)),
    FIFTEEN_MINUTES(Duration.ofMinutes(15)),
    THIRTY_MINUTES(Duration.ofMinutes(30)),

    ONE_HOUR(Duration.ofHours(1)),
    TWO_HOURS(Duration.ofHours(2)),
    FOUR_HOURS(Duration.ofHours(4)),
    SIX_HOURS(Duration.ofHours(6)),
    EIGHT_HOURS(Duration.ofHours(8)),
    TWELVE_HOURS(Duration.ofHours(12)),

    ONE_DAY(Duration.ofDays(1)),
    THREE_DAYS(Duration.ofDays(3)),
    FIFTEEN_DAYS(Duration.ofDays(15)),

    ONE_WEEK(Duration.ofDays(7)),

    ONE_MONTH(Duration.ofDays(30));

    private final Duration duration;
}
