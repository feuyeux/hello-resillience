package org.feuyeux.resilience.service;

import java.util.concurrent.CompletableFuture;

public interface BackendService {
    String failure();

    String failureWithFallback();

    String success();

    String successException();

    String ignoreException();

    CompletableFuture<String> futureSuccess();

    CompletableFuture<String> futureFailure();

    CompletableFuture<String> futureTimeout();

}
