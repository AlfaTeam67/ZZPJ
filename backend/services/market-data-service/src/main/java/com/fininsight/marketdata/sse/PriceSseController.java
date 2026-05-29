package com.fininsight.marketdata.sse;

import com.fininsight.marketdata.service.MarketPriceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/prices")
@RequiredArgsConstructor
@Slf4j
public class PriceSseController {

    private final PriceSseBroadcaster broadcaster;
    private final MarketPriceService priceService;

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamPrices() {
        SseEmitter emitter = new SseEmitter(0L);
        try {
            emitter.send(SseEmitter.event().data(priceService.getLatestPrices(), MediaType.APPLICATION_JSON));
        } catch (Exception e) {
            log.warn("Failed to send initial SSE event: {}", e.getMessage());
            emitter.completeWithError(e);
            return emitter;
        }
        broadcaster.addEmitter(emitter);
        log.debug("New SSE subscriber connected");
        return emitter;
    }
}
