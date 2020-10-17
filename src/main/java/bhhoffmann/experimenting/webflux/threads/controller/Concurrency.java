package bhhoffmann.experimenting.webflux.threads.controller;

import bhhoffmann.experimenting.webflux.threads.processing.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveValueOperations;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.Random;
import java.util.stream.BaseStream;

@RestController
public class Concurrency {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final WebClient webClient;
    private ReactiveValueOperations<String, String> redis;

    private final Random rand = new Random();

    public Concurrency(
            ReactiveRedisTemplate<String, String> redis,
            WebClient.Builder builder
    ) {
        this.redis = redis.opsForValue();
        this.webClient = builder
                .build();
    }

    @GetMapping("/process/prime")
    public Mono<String> prime() {
        logger.info(" \n Request to /process/prime");

        return Mono.just("Request to calculate prime")
                .doOnNext(msg -> logger.info("{}", msg))
                .publishOn(Schedulers.single())
                .flatMap(ignore -> Processor.rLargestPrime(30000))
                .doOnNext(p -> logger.info("RETURNING"))
                .map(p -> "OK");
                //.subscribe(result -> logger.info("sub result: {}", result));
        //return Mono.just("OK").doOnNext(it -> logger.info("controller returns: {}", it));
    }

    @GetMapping("/process/primeS")
    public Mono<String> primeS() {
        logger.info(" \n Request to /process/prime");

        Mono.just("Staring prime calculation")
                .doOnNext(msg -> logger.info("{}", msg))
                .flatMap(ignore -> Processor.rLargestPrime(10000))
                .doOnNext(p -> logger.info("prime: {}", p))
                .map(p -> "OK").log()
                .subscribe(result -> logger.info("sub result: {}", result));

        return Mono.just("OK").doOnNext(it -> logger.info("controller returns: {}", it));
    }

    @GetMapping("/redis/nio")
    public Mono<String> nio() {
        logger.info(" \n Request to /redis/nio");

        Mono.just("Reading value from REDIS")
                .doOnNext(msg -> logger.info("{}", msg))
                .then(redis.get("T"))
                .switchIfEmpty(redis.set("T", "NIO").thenReturn("Set a value"))
                .onErrorResume(err -> {
                    logger.info("Got an error: {}", err.getMessage());
                    return Mono.just("Recovered");
                }).subscribe(result -> logger.info("sub result: {}", result));

        return Mono.just("OK")
                .doOnNext(it -> logger.info("controller returns: {}", it));
    }

    @GetMapping("/webclient")
    public Mono<String> ping() {
        //Mono<String> response =
        logger.info("webclient class: {}", webClient.getClass().getSimpleName());
        Mono.just("Make request using WebClient")
                .doOnNext(msg -> logger.info("{}", msg))
                .then(
                        webClient.get()
                                .uri("http://google.com")
                                .exchange()
                                .doOnNext(r -> logger.info("Got response"))
                )
                .flatMap(clientResponse -> {
                    logger.info("Code {}", clientResponse.statusCode());
                    return clientResponse.bodyToMono(String.class);
                })
                .doOnNext(i -> logger.info("WebClient completed"))
                .subscribe();

        return Mono.just("OK")
                .doOnNext(it -> logger.info("return"));
    }

    @GetMapping("/file")
    public Mono<String> readFile() {
        //Mono<String> response =
        Mono.just("Starting file read")
                .doOnNext(msg -> logger.info("{}", msg))
                //.publishOn(Schedulers.single())
                .then(
                        fromPath(Path.of("/home/bhh/dev/git/webflux-threads/src/main/resources/book.txt"))
                        .collectList()
                )
                .doOnNext(i -> logger.info("File read complete"))
                .subscribe();

        return Mono.just("OK")
                .doOnNext(it -> logger.info("return"));
    }

    private static Flux<String> fromPath(Path path) {
        return Flux.using(() -> Files.lines(path),
                Flux::fromStream,
                BaseStream::close
        );
    }

    @GetMapping("/load/{calls}")
    public Mono<String> createLoad(@PathVariable Integer calls) {
        Instant s = Instant.now();
        return Flux.range(0, calls)
                .flatMap(nr -> webClient.get()
                        .uri(uriBuilder -> uriBuilder.path("localhost:8080/process/{id}").build(nr))
                        .retrieve()
                        .bodyToMono(String.class)
                )
                .collectList()
                .doOnNext(list -> logger.info("Return order: {}", list))
                .elapsed()
                .doOnNext(t -> logger.info("Duration: {}, Elapsed: {}",
                        Duration.between(s, Instant.now()).toMillis(), t.getT1()))
                .map(t -> t.getT2().toString());
    }

    @GetMapping("/process/{id}")
    public Mono<String> processRequest(@PathVariable Integer id) {
        int ms = rand.nextInt(3000);
        return Mono.just("Processing request")
                //.doOnNext(msg -> logger.info("{}", msg))
                //.doOnNext(msg -> logger.info("{} - Simulated processing time: {} ms", id, ms))
                //.delayElement(Duration.ofMillis(ms))
                .doOnNext(msg -> logger.info("{} - completed in {} ms", id, ms))
                .map(msg -> id.toString());
    }

}
