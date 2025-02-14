package org.feuyeux.resilience.service;


import io.github.resilience4j.bulkhead.BulkheadFullException;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import io.vavr.control.Try;
import lombok.extern.slf4j.Slf4j;
import org.feuyeux.resilience.exception.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import java.io.IOException;
import java.util.concurrent.*;

import static io.github.resilience4j.bulkhead.annotation.Bulkhead.Type;

/**
 * This Service shows how to use the CircuitBreaker annotation.
 */
@Component(value = "backendAService")
@Slf4j
public class BackendABackendService implements BackendService {

    public static final String BACKEND_A = "backendA";

    @Override
    @CircuitBreaker(name = BACKEND_A)
    @Bulkhead(name = BACKEND_A)
    @Retry(name = BACKEND_A)
    public String failure() {
        throw new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "This is a remote exception");
    }

    @Override
    @CircuitBreaker(name = BACKEND_A)
    @Bulkhead(name = BACKEND_A)
    public String ignoreException() {
        throw new BusinessException("This exception is ignored by the CircuitBreaker of backend A");
    }

    @Override
    @CircuitBreaker(name = BACKEND_A)
    @Bulkhead(name = BACKEND_A)
    @Retry(name = BACKEND_A)
    public String success() {
        return "Hello World from backend A";
    }

    @Override
    @CircuitBreaker(name = BACKEND_A)
    @Bulkhead(name = BACKEND_A)
    public String successException() {
        throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "This is a remote client exception");
    }

    @Override
    @CircuitBreaker(name = BACKEND_A, fallbackMethod = "fallback")
    public String failureWithFallback() {
        return failure();
    }

    @Override
    @Bulkhead(name = BACKEND_A, type = Type.THREADPOOL)
    public CompletableFuture<String> futureSuccess() {
        Try.run(() -> Thread.sleep(5000));
        return CompletableFuture.completedFuture("Hello World from backend A");
    }

    @Override
    @Bulkhead(name = BACKEND_A, type = Type.THREADPOOL)
    @TimeLimiter(name = BACKEND_A)
    @CircuitBreaker(name = BACKEND_A)
    @Retry(name = BACKEND_A)
    public CompletableFuture<String> futureFailure() {
        CompletableFuture<String> future = new CompletableFuture<>();
        future.completeExceptionally(new IOException("BAM!"));
        return future;
    }

    @TimeLimiter(name = BACKEND_A)
    @CircuitBreaker(name = BACKEND_A, fallbackMethod = "futureFallback")
    @Bulkhead(name = BACKEND_A, type = Type.THREADPOOL)
    public CompletableFuture<String> futureTimeout1() {
        try {
            Thread.sleep(3000);
        } catch (InterruptedException ignored) {}
        return CompletableFuture.completedFuture("Hello World from backend A");
    }

    @Override
    @TimeLimiter(name = BACKEND_A)
    @CircuitBreaker(name = BACKEND_A, fallbackMethod = "futureFallback")
    public CompletableFuture<String> futureTimeout() {
        return CompletableFuture.supplyAsync(()->{
            try {
                Thread.sleep(300);
            } catch (InterruptedException ignored) {}
            return "Hello World from backend A";
        }, Executors.newVirtualThreadPerTaskExecutor());
    }

    private String fallback(HttpServerErrorException ex) {
        return "Recovered HttpServerErrorException: " + ex.getMessage();
    }

    private String fallback(Exception ex) {
        return "Recovered: " + ex.toString();
    }

    private CompletableFuture<String> futureFallback(TimeoutException ex) {
        return CompletableFuture.completedFuture("Recovered specific TimeoutException: " + ex.toString());
    }

    private CompletableFuture<String> futureFallback(BulkheadFullException ex) {
        return CompletableFuture.completedFuture("Recovered specific BulkheadFullException: " + ex.toString());
    }

    private CompletableFuture<String> futureFallback(CallNotPermittedException ex) {
        return CompletableFuture.completedFuture("Recovered specific CallNotPermittedException: " + ex.toString());
    }
}
