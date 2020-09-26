package bhhoffmann.experimenting.webflux.threads.processing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@Component
public class Processor {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public Mono<List<Character>> decomposeStringMono(String str) {

        return Mono.just(str)
                .map(s -> {
                    logger.info("Decomposing string into list of characters: {}", s);
                    List<Character> result = new ArrayList<>();
                    for (int i = 0; i < s.length(); i++) {
                        result.add(s.charAt(i));
                    }
                    return result;
                });

    }

    public List<Character> decomposeString(String str) {


        logger.info("Decomposing string into list of characters: {}", str);
        List<Character> result = new ArrayList<>();
        for (int i = 0; i < str.length(); i++) {
            result.add(str.charAt(i));
        }
        return result;

    }

    public Mono<String> buildStringFromCharsMono(List<Character> chars) {

        return Mono.just(chars)
                .map(characters -> {
                    logger.info("Building string from list of characters: {}", characters);
                    StringBuilder sb = new StringBuilder();
                    characters.forEach(sb::append);
                    return sb.toString();
                });

    }

    public String buildStringFromChars(List<Character> chars) {

        logger.info("Building string from list of characters: {}", chars);
        StringBuilder sb = new StringBuilder();
        chars.forEach(sb::append);
        return sb.toString();

    }

}
