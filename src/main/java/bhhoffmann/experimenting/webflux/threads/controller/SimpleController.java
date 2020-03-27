package bhhoffmann.experimenting.webflux.threads.controller;


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

    @GetMapping("/thread")
    public Mono<ResponseEntity<String>> thread() {

        logger.info("GET on /thread");

        System.out.println(Thread.currentThread().getName());

        return Mono.just("Hello from ThreadsApplication")
                .doOnNext(msg -> logger.info("Returning msg: {}", msg))
                .map(msg -> new ResponseEntity<>(msg, HttpStatus.OK));

    }

}
