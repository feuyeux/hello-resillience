package org.feuyeux.resilience.controller;

import lombok.extern.slf4j.Slf4j;
import org.feuyeux.resilience.service.BackendService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping(value = "/backendA")
@Slf4j
public class BackendAController {

    private final BackendService businessABackendService;

    public BackendAController(@Qualifier("backendAService") BackendService businessABackendService){
        this.businessABackendService = businessABackendService;
    }

    @GetMapping("failure")
    public String failure(){
        return businessABackendService.failure();
    }

    @GetMapping("success")
    public String success(){
        return businessABackendService.success();
    }

    @GetMapping("successException")
    public String successException(){
        return businessABackendService.successException();
    }

    @GetMapping("ignore")
    public String ignore(){
        return businessABackendService.ignoreException();
    }

    @GetMapping("futureFailure")
    public CompletableFuture<String> futureFailure(){
        return businessABackendService.futureFailure();
    }

    @GetMapping("futureSuccess")
    public CompletableFuture<String> futureSuccess(){
        return businessABackendService.futureSuccess();
    }

    @GetMapping("futureTimeout")
    public CompletableFuture<String> futureTimeout(){
        return businessABackendService.futureTimeout();
    }

    @GetMapping("fallback")
    public String failureWithFallback(){
        return businessABackendService.failureWithFallback();
    }
}
