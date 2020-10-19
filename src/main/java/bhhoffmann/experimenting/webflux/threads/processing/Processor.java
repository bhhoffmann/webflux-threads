package bhhoffmann.experimenting.webflux.threads.processing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;

@Component
public class Processor {

    private static final Logger logger = LoggerFactory.getLogger(Processor.class);

    public static Mono<Integer> largestPrime(int upper) {
        Instant s = Instant.now();

        int i = 0;
        int num = 0;

        int largestPrime = 0;

        for (i = 1; i <= upper; i++) {
            int counter = 0;
            for (num = i; num >= 1; num--) {
                if (i % num == 0) {
                    counter = counter + 1;
                }
            }
            if (counter == 2) {
                //Appended the Prime number to the String
                largestPrime = i;
            }
        }

        long tte = Duration.between(s, Instant.now()).toMillis();
        logger.info("Largest prime <= {} is {}, found in {} ms", upper, largestPrime, tte);
        return Mono.just(largestPrime);
    }

    public static Mono<Integer> rLargestPrime(int upper) {
        Instant s = Instant.now();
        return Mono.just(upper)
                .doOnNext(u -> logger.info("Starting prime calculation"))
                .map(u -> {
                    int i = 0;
                    int num = 0;

                    int largestPrime = 0;

                    for (i = 1; i <= upper; i++) {
                        int counter = 0;
                        for (num = i; num >= 1; num--) {
                            if (i % num == 0) {
                                counter = counter + 1;
                            }
                        }
                        if (counter == 2) {
                            //Appended the Prime number to the String
                            largestPrime = i;
                        }
                    }

                    long tte = Duration.between(s, Instant.now()).toMillis();
                    logger.info("Largest prime <= {} is {}, found in {} ms", upper, largestPrime, tte);
                    return largestPrime;
                });
    }

}
