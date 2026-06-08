package dev.rudyevhenii.crypto_aggregator.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ChartInterval {
    ONE_SECOND,

    ONE_MINUTE,
    THREE_MINUTES,
    FIVE_MINUTES,
    FIFTEEN_MINUTES,
    THIRTY_MINUTES,

    ONE_HOUR,
    TWO_HOURS,
    FOUR_HOURS,
    SIX_HOURS,
    EIGHT_HOURS,
    TWELVE_HOURS,

    ONE_DAY,
    THREE_DAYS,
    FIFTEEN_DAYS,

    ONE_WEEK,

    ONE_MONTH
}
