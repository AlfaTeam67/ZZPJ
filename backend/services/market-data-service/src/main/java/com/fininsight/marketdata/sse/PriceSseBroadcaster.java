package com.fininsight.marketdata.sse;

import com.fininsight.marketdata.dto.price.MarketPriceResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
@Slf4j
public class PriceSseBroadcaster {

    static final int MAX_EMITTERS = 500;

    private final CopyOnWriteArrayList<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    public void addEmitter(SseEmitter emitter) {
        if (emitters.size() >= MAX_EMITTERS) {
            log.warn("SSE subscriber limit ({}) reached, rejecting new connection", MAX_EMITTERS);
            emitter.completeWithError(new IllegalStateException("SSE subscriber limit reached"));
            return;
        }
        emitters.add(emitter);
        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitter.onError(e -> emitters.remove(emitter));
    }

    public void broadcast(List<MarketPriceResponse> prices) {
        if (emitters.isEmpty()) return;
        List<SseEmitter> dead = new ArrayList<>();
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event().data(prices, MediaType.APPLICATION_JSON));
            } catch (Exception e) {
                dead.add(emitter);
            }
        }
        emitters.removeAll(dead);
        log.debug("Broadcast {} price records to {} SSE subscriber(s)", prices.size(), emitters.size());
    }

    @Scheduled(fixedDelay = 25_000)
    public void sendHeartbeat() {
        if (emitters.isEmpty()) return;
        List<SseEmitter> dead = new ArrayList<>();
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event().name("ping").data(""));
            } catch (Exception e) {
                dead.add(emitter);
            }
        }
        emitters.removeAll(dead);
    }
}
