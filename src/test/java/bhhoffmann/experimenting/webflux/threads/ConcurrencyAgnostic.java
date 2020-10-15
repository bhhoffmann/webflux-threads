package bhhoffmann.experimenting.webflux.threads;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.RandomAccessFile;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.stream.BaseStream;

public class ConcurrencyAgnostic {

    private static final Logger logger = LoggerFactory.getLogger(ConcurrencyAgnostic.class);

    @BeforeAll
    static void setup() {

    }

    @Test
    void t1() {
        logger.info("Starting on thread {}", thread());
        logger.info("Setting up sequence.");

        Flux<Integer> f = Flux.range(1, 5)
                .map(nr -> nr*10000)
                .map(this::largestPrime)
                .doOnNext(squared -> logger.info("Prime: {}", squared));

        logger.info("Subscribing");
        f.subscribe();
        logger.info("Subscribed");


        int sleep = 3000;
        logger.info("Sleeping thread {} for {} ms", thread(), sleep);
        try {
            Thread.sleep(sleep);
        } catch (InterruptedException e) {
            logger.info("Thread sleep interrupted");
        }
        logger.info("Stopping");
    }

    @Test
    void t2() {
        logger.info("Starting on thread {}", thread());
        CountDownLatch latch = new CountDownLatch(1);

        logger.info("Setting up sequence.");
        Flux<Integer> f = Flux.range(1, 5)
                .map(nr -> nr*10000)
                //.map(this::largestPrime)
                .doOnNext(squared -> logger.info("Prime: {}", squared));

        logger.info("Subscribing");
        f.subscribe(result -> latch.countDown());
        logger.info("Subscribed");


        logger.info("Awaiting countdown latch.");
        try {
            latch.await();
        } catch (InterruptedException e) {
            logger.info("latch.await() interrupted. Exception message: {}", e.getMessage());
        }
        logger.info("Stopping");
    }

    @Test
    void readFileBlocking() {
        readFileIO("/home/bhh/dev/git/webflux-threads/src/main/resources/paragraph.txt");
    }

    private void readFileIO(String path) {
        File file = new File(path);
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            while (br.ready()) {
                sb.append(br.readLine());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        logger.info("Content read: {}", sb);
    }

    @Test
    void readFileNonBlocking() {
        ByteBuffer buffer = ByteBuffer.allocate(2048);
        logger.info("Buffer: {}", buffer);
        Future<Integer> readComplete =
                readFileNIO("/home/bhh/dev/git/webflux-threads/src/main/resources/paragraph.txt", buffer);

        logger.info("Started asynch file read");

        while (!readComplete.isDone()) {
            logger.info("File read not complete");
        }
        logger.info("File read complete");
        String fileContent = new String(buffer.array()).trim();
        buffer.clear();

        logger.info("Buffer: {}", buffer);
        logger.info("Content: {}", fileContent);

    }

    @Test
    void readFileNonBlockingReactive() {

        
    }

    private Future<Integer> readFileNIO(String path, ByteBuffer buffer) {
        Path p = Paths.get(path);
        Future<Integer> status = null;
        try {
            status = AsynchronousFileChannel
                    .open(p, StandardOpenOption.READ)
                    .read(buffer, 0);
        } catch (Exception e) {
            logger.info("Asynch read exception. Message: {}", e.getMessage());
        }
        return status;
    }

    private String thread() {
        return Thread.currentThread().getName();
    }

    private Integer largestPrime(int upper) {
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
        return largestPrime;
    }


    private Flux<String> reactorReadThatSeemsToBeBlocking(String path) {
        Path p = Path.of(path);
        return Flux.using(() -> Files.lines(p),
                Flux::fromStream,
                BaseStream::close
        );
    }

}
