package bhhoffmann.experimenting.webflux.threads;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.Date;
import java.util.Random;

public class WrappingBlockingCall {

    private final static Logger logger = LoggerFactory.getLogger(WrappingBlockingCall.class);

    // Just simulate some delay on the current thread.
    public void delay() {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public String blockingCall(Integer input) {
        delay();
        return String.format("[%d] on thread [%s] at time [%s]",
                input,
                Thread.currentThread().getName(),
                new Date());
    }

    public Mono<String> blockingCallWrapper(Integer a) {
        return Mono
                .fromCallable(() -> blockingCall(a)) // Define blocking call
                .doOnNext(r -> logger.info("Got value from blocking call on blocking thread: {}", r))
                .subscribeOn(Schedulers.elastic()); // Define the execution model
    }

    @Test
    public void blockingToNonBlockingRightWay() {
        Flux.range(1, 10)
                .flatMap(this::blockingCallWrapper)
                .doOnNext(r -> logger.info("Got value from blocking call on non-blocking thread: {}", r))
                .blockLast();
    }

    @Test
    public void concurrency() throws InterruptedException {

        Random rand = new Random();

        Flux<Integer> even = Flux.range(1, 10)
                .delayElements(Duration.ofMillis(rand.nextInt(1000)))
                .filter(nr -> nr % 2 == 0)
                .doOnNext(nr -> logger.info("even: {}", nr));




        Flux<Integer> odd = Flux.range(1,10)
                .delayElements(Duration.ofMillis(rand.nextInt(1000)))
                .filter(nr -> nr % 2 != 0)
                .doOnNext(nr -> logger.info("   odd: {}", nr));


        even.subscribe();
        odd.subscribe();
        Thread.sleep(3000);
    }


}
