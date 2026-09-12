package dev.rudyevhenii.crypto_aggregator.price_alert;

import dev.rudyevhenii.crypto_aggregator.api.dto.priceAlert.PriceAlertRequestRqDto;
import dev.rudyevhenii.crypto_aggregator.api.dto.priceAlert.PriceAlertRqDto;
import dev.rudyevhenii.crypto_aggregator.api.dto.priceAlert.PriceAlertUpdateRequestRqDto;
import dev.rudyevhenii.crypto_aggregator.api.interfaces.priceAlert.PriceAlertApi;
import dev.rudyevhenii.crypto_aggregator.price_alert.domain.PriceAlert;
import dev.rudyevhenii.crypto_aggregator.price_alert.mapper.PriceAlertMapper;
import dev.rudyevhenii.crypto_aggregator.price_alert.service.PriceAlertService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class PriceAlertController implements PriceAlertApi {

    private final PriceAlertMapper mapper;
    private final PriceAlertService service;

    @Override
    public ResponseEntity<PriceAlertRqDto> createPriceAlert(PriceAlertRequestRqDto request) {
        PriceAlert response = service.create(mapper.map(request));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(mapper.map(response));
    }

    @Override
    public ResponseEntity<PriceAlertRqDto> updatePriceAlert(UUID priceAlertId, PriceAlertUpdateRequestRqDto request) {
        PriceAlert response = service.update(priceAlertId, mapper.map(request));
        return ResponseEntity.ok(mapper.map(response));
    }

    @Override
    public ResponseEntity<PriceAlertRqDto> getPriceAlertById(UUID priceAlertId) {
        PriceAlert response = service.getPriceAlertById(priceAlertId);
        return ResponseEntity.ok(mapper.map(response));
    }

    @Override
    public ResponseEntity<List<PriceAlertRqDto>> getAllPriceAlerts() {
        List<PriceAlert> response = service.getAllPriceAlerts();
        return ResponseEntity.ok(response.stream()
                .map(mapper::map)
                .toList());
    }

    @Override
    public ResponseEntity<Void> activatePriceAlert(UUID priceAlertId) {
        service.activate(priceAlertId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @Override
    public ResponseEntity<Void> deactivatePriceAlert(UUID priceAlertId) {
        service.deactivate(priceAlertId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @Override
    public ResponseEntity<Void> deletePriceAlert(UUID priceAlertId) {
        service.deleteById(priceAlertId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
