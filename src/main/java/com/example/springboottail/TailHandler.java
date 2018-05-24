package com.example.springboottail;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.PostConstruct;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * @author wuzhong on 2018/4/19.
 * @version 1.0
 */
@Component
@Slf4j
public class TailHandler implements WebSocketHandler {

    private Map<WebSocketSession, FluxSink<WebSocketMessage>> map = new HashMap<>();

    @Override
    public Mono<Void> handle(WebSocketSession session) {

        log.error("tail entered");

        //Flux<WebSocketMessage> flux = Flux.interval(Duration.ofSeconds(1)).map(
        //    aLong -> session.textMessage(String.valueOf(aLong)));
        //
        //return session.send(flux);

        String sessionId = session.getId();

        Flux<WebSocketMessage> publisher = Flux.create(webSocketMessageFluxSink -> {
            registerSession(session, webSocketMessageFluxSink);
        });

        Flux<WebSocketMessage> socketMessageFlux = publisher.publishOn(Schedulers.elastic()).subscribeOn(
            Schedulers.elastic());

        return session.send(socketMessageFlux).and(session.receive().doFinally(sig -> {
            log.error("Terminating WebSocket Session (client side) sig: [{}], [{}]", sig.name(), sessionId);
            removeSession(session);
            session.close();
        }));

    }


    //TODO broadcast by others
    @PostConstruct
    public void init(){

        Flux.interval(Duration.ofSeconds(1)).subscribe(aLong -> {
            log.info("on timer");
            broadcast(String.valueOf(aLong));
        });

    }

    private void removeSession(WebSocketSession session) {
        map.remove(session);
    }

    private void registerSession(WebSocketSession session, FluxSink<WebSocketMessage> sink) {
        map.put(session, sink);
    }

    private void broadcast(String message) {
        for (Entry<WebSocketSession, FluxSink<WebSocketMessage>> entry : map.entrySet()) {
            entry.getValue().next(entry.getKey().textMessage(message));
        }
    }

}
