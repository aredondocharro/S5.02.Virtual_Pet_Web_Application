package cat.itacademy.s05.t02;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication
@Slf4j
@EnableCaching
@EnableScheduling
public class S05T02Application {

    public static void main(String[] args) {
        log.info("🚀 Starting Virtual Pet Web Application...");
        SpringApplication.run(S05T02Application.class, args);
        log.info("✅ Application started successfully!");
    }

}


