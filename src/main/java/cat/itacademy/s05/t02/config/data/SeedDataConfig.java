package cat.itacademy.s05.t02.config.data;

import cat.itacademy.s05.t02.config.data.service.SeedService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SeedDataConfig {

    @Bean
    CommandLineRunner seedRunner(SeedService seedService) {
        return args -> seedService.seedRolesAndPermissions();
    }
}