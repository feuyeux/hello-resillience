# hello-resilience

Microservices Resilience Patterns
| Pattern | Description |Implement|
| :---- | :----- |:---|
| Rate Limiting | https://learn.microsoft.com/en-us/azure/architecture/patterns/rate-limiting-pattern |https://www.baeldung.com/guava-rate-limiter https://resilience4j.readme.io/docs/examples-4|
| Circuit Breaker|<https://learn.microsoft.com/en-us/azure/architecture/patterns/circuit-breaker>|https://resilience4j.readme.io/docs/examples|
| Retry (with Statelessness and Idempotence) |https://learn.microsoft.com/en-us/azure/architecture/patterns/retry|https://resilience4j.readme.io/docs/examples-5|
| Bulkhead |https://learn.microsoft.com/en-us/azure/architecture/patterns/bulkhead|https://resilience4j.readme.io/docs/examples-3|

```xml
<dependency>
    <groupId>io.github.resilience4j</groupId>
    <artifactId>resilience4j-circuitbreaker</artifactId>
    <version>${resilience4jVersion}</version>
</dependency>
<dependency>
    <groupId>io.github.resilience4j</groupId>
    <artifactId>resilience4j-ratelimiter</artifactId>
    <version>${resilience4jVersion}</version>
</dependency>
<dependency>
    <groupId>io.github.resilience4j</groupId>
    <artifactId>resilience4j-retry</artifactId>
    <version>${resilience4jVersion}</version>
</dependency>
<dependency>
    <groupId>io.github.resilience4j</groupId>
    <artifactId>resilience4j-bulkhead</artifactId>
    <version>${resilience4jVersion}</version>
</dependency>
```

https://github.com/resilience4j/resilience4j-spring-boot3-demo

```sh
mvn test -Dtest=HelloResilienceApplicationTests#shouldOpenBackendACircuitBreaker
```