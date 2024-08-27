package org.feuyeux.resilience.controller;

import io.github.resilience4j.bulkhead.*;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.decorators.Decorators;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.feuyeux.resilience.service.BackendService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.util.Arrays.asList;

@RestController
@RequestMapping(value = "/backendB")
@Slf4j
public class BackendBController {

    private static final String BACKEND_B = "backendB";
    private final BackendService businessBBackendService;
    private final CircuitBreaker circuitBreaker;
    private final Bulkhead bulkhead;
    private final ThreadPoolBulkhead threadPoolBulkhead;
    private final Retry retry;
    private final RateLimiter rateLimiter;
    private final TimeLimiter timeLimiter;
    private final ScheduledExecutorService scheduledExecutorService;

    public BackendBController(
            @Qualifier("backendBService") BackendService businessBBackendService,
            CircuitBreakerRegistry circuitBreakerRegistry,
            ThreadPoolBulkheadRegistry threadPoolBulkheadRegistry,
            BulkheadRegistry bulkheadRegistry,
            RetryRegistry retryRegistry,
            RateLimiterRegistry rateLimiterRegistry,
            TimeLimiterRegistry timeLimiterRegistry) {
        this.businessBBackendService = businessBBackendService;
        this.circuitBreaker = circuitBreakerRegistry.circuitBreaker(BACKEND_B);
        this.bulkhead = bulkheadRegistry.bulkhead(BACKEND_B);
        this.threadPoolBulkhead = threadPoolBulkheadRegistry.bulkhead(BACKEND_B);
        this.retry = retryRegistry.retry(BACKEND_B);
        this.rateLimiter = rateLimiterRegistry.rateLimiter(BACKEND_B);
        this.timeLimiter = timeLimiterRegistry.timeLimiter(BACKEND_B);
        this.scheduledExecutorService = Executors.newScheduledThreadPool(3);
    }

    @GetMapping("failure")
    public String failure() {
        return execute(businessBBackendService::failure);
    }

    @GetMapping("success")
    public String success() {
        return execute(businessBBackendService::success);
    }

    @GetMapping("successWithRateLimiter")
    public String successWithRateLimiter() {
        return executeRateLimiter(businessBBackendService::success);
    }


    @GetMapping("successException")
    public String successException() {
        return execute(businessBBackendService::successException);
    }

    @GetMapping("ignore")
    public String ignore() {
        return Decorators.ofSupplier(businessBBackendService::ignoreException)
                .withCircuitBreaker(circuitBreaker)
                .withBulkhead(bulkhead).get();
    }

    @GetMapping("futureFailure")
    public CompletableFuture<String> futureFailure() {
        return executeAsync(businessBBackendService::failure);
    }

    @GetMapping("futureSuccess")
    public CompletableFuture<String> futureSuccess() {
        return executeAsync(businessBBackendService::success);
    }

    @GetMapping("futureTimeout")
    public CompletableFuture<String> futureTimeout() {
        return executeAsyncWithFallback(this::timeout, this::fallback);
    }

    @GetMapping("fallback")
    public String failureWithFallback() {
        return businessBBackendService.failureWithFallback();
    }

    private String timeout() {
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            log.error("",e);
        }
        return "";
    }

    private <T> T execute(Supplier<T> supplier) {
        return Decorators.ofSupplier(supplier)
                .withCircuitBreaker(circuitBreaker)
                .withBulkhead(bulkhead)
                .withRetry(retry)
                .get();
    }

    private <T> T executeRateLimiter(Supplier<T> supplier) {
        return Decorators.ofSupplier(supplier)
                .withRateLimiter(rateLimiter)
                .get();
    }

    private <T> CompletableFuture<T> executeAsync(Supplier<T> supplier) {
        return Decorators.ofSupplier(supplier)
                .withThreadPoolBulkhead(threadPoolBulkhead)
                .withTimeLimiter(timeLimiter, scheduledExecutorService)
                .withCircuitBreaker(circuitBreaker)
                .withRetry(retry, scheduledExecutorService)
                .get().toCompletableFuture();
    }

    private <T> CompletableFuture<T> executeAsyncWithFallback(Supplier<T> supplier, Function<Throwable, T> fallback) {
        return Decorators.ofSupplier(supplier)
                .withThreadPoolBulkhead(threadPoolBulkhead)
                .withTimeLimiter(timeLimiter, scheduledExecutorService)
                .withCircuitBreaker(circuitBreaker)
                .withFallback(asList(TimeoutException.class, CallNotPermittedException.class, BulkheadFullException.class),
                        fallback)
                .get().toCompletableFuture();
    }

    private String fallback(Throwable ex) {
        return "Recovered: " + ex.toString();
    }
}
