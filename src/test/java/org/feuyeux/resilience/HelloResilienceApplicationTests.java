package org.feuyeux.resilience;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.vavr.collection.Stream;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = HelloResilienceApplication.class)
@Slf4j
public class HelloResilienceApplicationTests {
    private static final String BACKEND_A = "backendA";
    private static final String BACKEND_B = "backendB";

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @Autowired
    private TestRestTemplate restTemplate;

    @BeforeEach
    public void setup() {
        transitionToClosedState(BACKEND_A);
        transitionToClosedState(BACKEND_B);
    }

    private void transitionToOpenState(String circuitBreakerName) {
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker(circuitBreakerName);
        circuitBreaker.transitionToOpenState();
    }

    private void transitionToClosedState(String circuitBreakerName) {
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker(circuitBreakerName);
        circuitBreaker.transitionToClosedState();
    }

    private void checkHealthStatus(String circuitBreakerName, CircuitBreaker.State state) {
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker(circuitBreakerName);
        assertThat(circuitBreaker.getState()).isEqualTo(state);
    }

    // CircuitBreaker Test
    @Test
    public void shouldOpenBackendACircuitBreaker() {
        // When
        Stream.rangeClosed(1, 2).forEach((count) -> produceFailure(BACKEND_A));

        // Then
        checkHealthStatus(BACKEND_A, CircuitBreaker.State.OPEN);
    }

    @Test
    public void shouldOpenBackendBCircuitBreaker() {
        // When
        Stream.rangeClosed(1,4).forEach((count) -> produceFailure(BACKEND_B));

        // Then
        checkHealthStatus(BACKEND_B, CircuitBreaker.State.OPEN);
    }

    @Test
    public void shouldCloseBackendACircuitBreaker() {
        transitionToOpenState(BACKEND_A);
        circuitBreakerRegistry.circuitBreaker(BACKEND_A).transitionToHalfOpenState();

        // When
        Stream.rangeClosed(1,3).forEach((count) -> produceSuccess(BACKEND_A));

        // Then
        checkHealthStatus(BACKEND_A, CircuitBreaker.State.CLOSED);
    }

    @Test
    public void shouldCloseBackendBCircuitBreaker() {
        transitionToOpenState(BACKEND_B);
        circuitBreakerRegistry.circuitBreaker(BACKEND_B).transitionToHalfOpenState();

        // When
        Stream.rangeClosed(1,3).forEach((count) -> produceSuccess(BACKEND_B));

        // Then
        checkHealthStatus(BACKEND_B, CircuitBreaker.State.CLOSED);
    }

    private void produceFailure(String backend) {
        ResponseEntity<String> response = restTemplate.getForEntity("/" + backend + "/failure", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private void produceSuccess(String backend) {
        ResponseEntity<String> response = restTemplate.getForEntity("/" + backend + "/success", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

}
