package bhhoffmann.experimenting.webflux.threads.controller;


import bhhoffmann.experimenting.webflux.threads.processing.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class SimpleController {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private Processor processor;

    public SimpleController(
            Processor processor
    ) {
        this.processor = processor;
    }

    @GetMapping("/thread")
    public Mono<ResponseEntity<String>> thread() {

        logger.info("Received a GET request on endpoint /thread");

        return Mono.just("Hello from ThreadsApplication")
                .doOnNext(msg -> logger.info("Controller doing its work on thread: {}", Thread.currentThread().getName()))
                .map(msg -> {
                    logger.info("Returning the message: {}", msg);
                    return new ResponseEntity<>(msg, HttpStatus.OK);
                });

    }

    @GetMapping("/deconstructFlat")
    public Mono<String> deconstruct() {
        return Mono.just("'May the force be with you' - Every Jedi that ever lived")
                .flatMap(q -> processor.decomposeStringMono(q))
                .flatMap(qChars -> {
                    logger.info("Quote as list of chars: {}", qChars);
                    return processor.buildStringFromCharsMono(qChars);
                })
                .doOnNext(q -> logger.info("Returning quote: {}", q));
    }

    @GetMapping("/deconstruct")
    public Mono<String> quoteMap() {
        return Mono.just("'May the force be with you' - Every Jedi that ever lived")
                .map(q -> processor.decomposeString(q))
                .map(qChars -> {
                    logger.info("Quote as list of chars: {}", qChars);
                    return processor.buildStringFromChars(qChars);
                })
                .doOnNext(q -> logger.info("Returning quote: {}", q));
    }

}
