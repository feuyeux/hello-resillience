package org.feuyeux.resilience.service;

import io.vavr.control.Try;
import org.feuyeux.resilience.exception.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;


import java.io.IOException;
import java.util.concurrent.CompletableFuture;

@Component(value = "backendBService")
public class BackendBBackendService implements BackendService {

    public static final String BACKEND_B = "backendB";

    @Override
    public String failure() {
        throw new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "This is a remote exception");
    }

    @Override
    public String success() {
        return "Hello World from backend B";
    }

    @Override
    public String successException() {
        throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "This is a remote client exception");
    }

    @Override
    public String ignoreException() {
        throw new BusinessException("This exception is ignored by the CircuitBreaker of backend B");
    }

    @Override
    public CompletableFuture<String> futureSuccess() {
        return CompletableFuture.completedFuture("Hello World from backend B");
    }

    @Override
    public CompletableFuture<String> futureFailure() {
        CompletableFuture<String> future = new CompletableFuture<>();
        future.completeExceptionally(new IOException("BAM!"));
        return future;
    }

    @Override
    public CompletableFuture<String> futureTimeout() {
        Try.run(() -> Thread.sleep(5000));
        return CompletableFuture.completedFuture("Hello World from backend A");
    }

    @Override
    public String failureWithFallback() {
        return Try.ofSupplier(this::failure).recover(this::fallback).get();
    }

    private String fallback(Throwable ex) {
        return "Recovered: " + ex.toString();
    }


}
