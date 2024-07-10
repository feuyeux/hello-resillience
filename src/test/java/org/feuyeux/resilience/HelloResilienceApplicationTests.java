package org.feuyeux.resilience;

import lombok.extern.java.Log;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Log
class HelloResilienceApplicationTests {

	@Test
	void contextLoads() {
		log.info("hello resilience");
	}

}
