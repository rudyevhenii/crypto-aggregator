package dev.rudyevhenii.crypto_aggregator.price_alert;

import dev.rudyevhenii.crypto_aggregator.api.dto.priceAlert.PriceAlertLogRqDto;
import dev.rudyevhenii.crypto_aggregator.api.dto.priceAlert.PriceAlertLogScrollRequestRqDto;
import dev.rudyevhenii.crypto_aggregator.api.interfaces.priceAlert.PriceAlertLogApi;
import dev.rudyevhenii.crypto_aggregator.price_alert.domain.PriceAlertLog;
import dev.rudyevhenii.crypto_aggregator.price_alert.mapper.PriceAlertLogMapper;
import dev.rudyevhenii.crypto_aggregator.price_alert.service.PriceAlertLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class PriceAlertLogController implements PriceAlertLogApi {

    private final PriceAlertLogService service;
    private final PriceAlertLogMapper mapper;

    @Override
    public ResponseEntity<List<PriceAlertLogRqDto>> getAllAlertLogs(PriceAlertLogScrollRequestRqDto request) {
        List<PriceAlertLog> response = service.getAllAlertLogs(mapper.map(request));
        return ResponseEntity.ok(response.stream()
                .map(mapper::map)
                .toList());
    }
}
