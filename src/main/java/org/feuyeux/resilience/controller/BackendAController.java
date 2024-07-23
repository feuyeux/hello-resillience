package org.feuyeux.resilience.controller;

import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import lombok.extern.slf4j.Slf4j;
import org.feuyeux.resilience.service.BackendService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

import static org.feuyeux.resilience.service.BackendABackendService.BACKEND_A;

@RestController
@RequestMapping(value = "/backendA")
@Slf4j
public class BackendAController {

    private final BackendService businessABackendService;

    public BackendAController(@Qualifier("backendAService") BackendService businessABackendService) {
        this.businessABackendService = businessABackendService;
    }

    @GetMapping("failure")
    public String failure() {
        return businessABackendService.failure();
    }

    @GetMapping("limit")
    @RateLimiter(name = BACKEND_A, fallbackMethod = "rateLimitingFallback")
    public ResponseEntity<String> limit() {
        return ResponseEntity.ok("OK");
    }

    public ResponseEntity<String> rateLimitingFallback(RequestNotPermitted ex) {
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("Retry-After", "1s");
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .headers(responseHeaders) // send retry header
                .body("Too Many Requests");
    }

    @GetMapping("success")
    public String success() {
        return businessABackendService.success();
    }

    @GetMapping("successException")
    public String successException() {
        return businessABackendService.successException();
    }

    @GetMapping("ignore")
    public String ignore() {
        return businessABackendService.ignoreException();
    }

    @GetMapping("futureFailure")
    public CompletableFuture<String> futureFailure() {
        return businessABackendService.futureFailure();
    }

    @GetMapping("futureSuccess")
    public CompletableFuture<String> futureSuccess() {
        return businessABackendService.futureSuccess();
    }

    @GetMapping("futureTimeout")
    public CompletableFuture<String> futureTimeout() {
        return businessABackendService.futureTimeout();
    }

    @GetMapping("fallback")
    public String failureWithFallback() {
        return businessABackendService.failureWithFallback();
    }
}
